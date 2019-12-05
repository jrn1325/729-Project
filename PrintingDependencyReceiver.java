import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.results.InclusionDependency;
import de.metanome.backend.result_postprocessing.results.InclusionDependencyResult;

import java.util.*;
import java.util.Collection;
import java.util.Map;

public class PrintingDependencyReceiver implements FunctionalDependencyResultReceiver, InclusionDependencyResultReceiver
{
    // Declare instance variables
    private Map<String, HashSet<FunctionalDependency>> _fds = new HashMap<>();
    private HashSet<InclusionDependency> _ids = new HashSet<>();
    private Map<String, HashSet<String>> tables = new HashMap<>();
    private InclusionDependency inclusionDependency;

    /**
     * Initialize functional dependency map and inclusion dependency list
     */
    public PrintingDependencyReceiver() {
        Collection<HashSet<FunctionalDependency>> fds = _fds.values();
        HashSet<InclusionDependency> ids = _ids;
    }//end constructor

    /**
     * Return true
     * @param fd
     * @return
     */
    @Override
    public Boolean acceptedResult(FunctionalDependency fd) {
        return true;
    }//end acceptedResult

    /**
     * Return true
     * @param id
     * @return
     */
    @Override
    public Boolean acceptedResult(InclusionDependency id) {
        return true;
    }//end acceptedResult

    /**
     * Add a functional dependency to the list of functional dependencies
     * @param fd
     */
    @Override
    public void receiveResult(FunctionalDependency fd)
    {
        String table = fd.getDependant().getTableIdentifier();
        if (!_fds.containsKey(table))
        {
            _fds.put(table, new HashSet<>());
        }
        _fds.get(table).add(fd);
    }//end receivedResult

    /**
     * Add an inclusion dependency to the list of inclusion dependencies
     * @param id
     */
    @Override
    public void receiveResult(InclusionDependency id)
    {
       _ids.add(id);
    }//end receivedResult

    /**
     * Print functional and inclusion dependencies
     */
    public void output()
    {
        System.out.println("Functional Dependencies");
        for(HashSet<FunctionalDependency> fdList : _fds.values())
        {
            if(fdList.size() > 0) {
                for (FunctionalDependency fd : fdList) {
                    String detFields = fd.getDeterminant().getColumnIdentifiers().toString();
                    String depField = fd.getDependant().getColumnIdentifier();
                    System.out.println(detFields + " -> " + depField);
                }//end inner for loop
            }
        }//end for loop

        System.out.println("\nInclusion Dependencies");
        for(InclusionDependency id: _ids)
        {
            String depTable = id.getDependant().getColumnIdentifiers().get(0).getTableIdentifier().replaceAll("^\"|\"$", "");
            String depFields = id.getDependant().getColumnIdentifiers().toString();
            String refTable = id.getReferenced().getColumnIdentifiers().get(0).getTableIdentifier().replaceAll("^\"|\"$", "");
            String refFields = id.getReferenced().getColumnIdentifiers().toString();

            if (depTable != refTable) {
                System.out.println(depTable + "(" + depFields + ") = " + refTable + "(" + refFields + ")");
                //System.out.println("Referenced Table: " + refTable + ":" + refFields);
            }

        }//end for loop
    }//end output()

    public void infer()
    {

    }//end infer

    public void fold()
    {
        boolean folded = true;
        while(folded) {
            folded = false;
            // Remove tables which are not needed
            while(_ids.size() > 0) {
                for(InclusionDependency id : _ids) {
                    if (id.getDependant().getColumnIdentifiers().toString() == tables.get(id.getDependant().getColumnIdentifiers().get(0).getTableIdentifier()).toString() &&
                            id.getDependant().getColumnIdentifiers().size() >= id.getReferenced().getColumnIdentifiers().size()) {
                        tables.remove(id.getDependant().getColumnIdentifiers().get(0).getTableIdentifier());
                        _fds.remove(id.getDependant().getColumnIdentifiers().get(0).getTableIdentifier());

                        if (_fds.containsKey(id.getDependant().getColumnIdentifiers().get(0).getTableIdentifier()) && _fds.get(id.getDependant().getColumnIdentifiers().get(0).getTableIdentifier()).isEmpty()) {
                            _fds.remove(id.getDependant().getColumnIdentifiers().get(0).getTableIdentifier());
                        }
                        tables.remove(id.getDependant().getColumnIdentifiers().get(0).getTableIdentifier());
                        _ids.remove(id.getDependant().getColumnIdentifiers().get(0).getTableIdentifier());

                        folded = true;
                    } else {
                        folded = false;
                    }
                }//end for loop

                // Remove fields which are not needed
                while(_ids.size() > 0 ) {
                    for(InclusionDependency id : _ids) {
                        while(_fds.get(id.getDependant().getColumnIdentifiers().get(0)) != null)
                        {
                            for(HashSet<FunctionalDependency> fdList : _fds.values()) {
                                for (FunctionalDependency fd : fdList) {
                                    if (fd.getDependant().getColumnIdentifier().contains(id.getDependant().getColumnIdentifiers().toString()) &&
                                            fd.getDeterminant().getColumnIdentifiers().contains(id.getDependant().getColumnIdentifiers().get(0).getColumnIdentifier())) {
                                        _fds.get(id.getDependant().getColumnIdentifiers().get(0).getTableIdentifier()).remove(fd.getDeterminant().getColumnIdentifiers());
                                        _ids.remove(id.getDependant().getColumnIdentifiers().get(0).getTableIdentifier());
                                        _ids.remove(fd.getDeterminant());
                                        tables.get(id.getDependant().getColumnIdentifiers().get(0).getTableIdentifier()).remove(fd.getDeterminant());

                                        folded = true;
                                    } else {
                                        folded = false;
                                    }
                                }//end for loop
                            }//end for loop
                        }//end while loop
                    }//end for loop
                }//end while loop
            }//end while loop
        }//end while loop
    }//end fold

    public void bcnfDecompose()
    {
        boolean decomposed = true;
        while(decomposed)
        {
            // Try to find a table with a Functional Dependency which violates BCNF
            if(_fds.size() > 0)
            {
                // Loop through hashmap
                for(Map.Entry<String, HashSet<FunctionalDependency>> me : _fds.entrySet())
                {
                    boolean doesDecompose = false;
                    String table = me.getKey();
                    HashSet<FunctionalDependency> fdSet = me.getValue();

                    // Loop through hashset of functional dependencies within hashmap
                    for(FunctionalDependency fd : fdSet)
                    {
                        tables.get(table).remove(fd.getDependant().getTableIdentifier()); //revisit
                        boolean isKey = (tables.get(table).contains(fd.getDeterminant().getColumnIdentifiers()));
                        if(!isKey)
                        {
                            doesDecompose = true;

                            // Generate the newly decomposed tables
                            String newLeftTable = table + "_1";
                            String newRightTable = table + "_2";
                            HashSet<String> set1 = new HashSet<>(); //revisit
                            set1.add(fd.getDependant().getColumnIdentifier()); //revisit
                            set1.add(fd.getDeterminant().getColumnIdentifiers().toString());
                            tables.put(newLeftTable, set1);
                            HashSet<String> set2 = new HashSet<>();
                            tables.get(table).remove(fd.getDeterminant().getColumnIdentifiers());
                            set2.add(tables.get(table).toString());
                            tables.put(newRightTable, set2);

                            // Copy functional dependencies to the new tables
                            HashSet<FunctionalDependency> set3 = new HashSet<>(); //revisit
                            HashSet<FunctionalDependency> set4 = new HashSet<>(); //revisit
                            _fds.put(newLeftTable, set3);
                            _fds.put(newRightTable, set4);
                            for(FunctionalDependency f : fdSet)
                            {
                                if ((f.getDependant().getColumnIdentifier() + f.getDeterminant().getColumnIdentifiers()).contains(tables.get(newLeftTable).toString())) {
                                    set3.add(f);
                                    _fds.put(newLeftTable, set3);
                                }
                                if ((f.getDependant().getColumnIdentifier() + f.getDeterminant().getColumnIdentifiers()).contains(tables.get(newRightTable).toString())) {
                                    set4.add(f);
                                    _fds.put(newRightTable, set4);
                                }
                            }//end for loop

                            // Remove old Functional Dependencies
                            _fds.remove(table);

                            // Copy INDs to the new tables
                            for(InclusionDependency id : _ids)
                            {
                                if (id.getDependant().getColumnIdentifiers().get(0).getTableIdentifier() == table) {
                                    if (id.getDependant().getColumnIdentifiers().contains(tables.get(newLeftTable))) {
                                        _ids.add(new InclusionDependency(id.getDependant(), id.getReferenced()));//newLeftTable, id.getDependant().getColumnIdentifiers(), id.getReferenced().getColumnIdentifiers().get(0).getTableIdentifier(), id.getReferenced().getColumnIdentifiers()));
                                    }
                                    if (id.getDependant().getColumnIdentifiers().contains(tables.get(newRightTable))) {
                                        _ids.add(new InclusionDependency(id.getDependant(), id.getReferenced()));//newRightTable, id.getDependant().getColumnIdentifiers(), id.getReferenced().getColumnIdentifiers().get(0).getTableIdentifier(), id.getReferenced().getColumnIdentifiers()));
                                    }
                                }
                                if (id.getReferenced().getColumnIdentifiers().get(0).getTableIdentifier() == table) {
                                    if (id.getReferenced().getColumnIdentifiers().contains(tables.get(newLeftTable))) {
                                        _ids.add(new InclusionDependency(id.getDependant(), id.getReferenced()));//, newLeftTable, id.getReferenced().getColumnIdentifiers()));
                                    }
                                    if (id.getReferenced().getColumnIdentifiers().contains(tables.get(newRightTable))) {
                                        _ids.add(new InclusionDependency(id.getDependant(), id.getReferenced()));//, newRightTable, id.getReferenced().getColumnIdentifiers()));
                                    }
                                }
                            }//end for loop

                            // Remove old INDs
                            _ids.remove(tables.get(table));

                            // Remove the old table
                            tables.remove(table);

                            // Add a new inclusion dependency for the fields
                            // the newly decomposed tables have in common
                            tables.get(newLeftTable).retainAll(tables.get(newRightTable));
                            HashSet<String> commonFields = tables.get(newLeftTable);

                            for(String field : commonFields)
                            {
                                ArrayList<String> list = new ArrayList<>();
                                list.add(field);
                                //_ids.add(new InclusionDependency(newLeftTable, list, newRightTable, list));
                                //_ids.add(new InclusionDependency(newRightTable, list, newLeftTable, list));
                                //_ids.add(new InclusionDependency(newLeftTable, list, newRightTable, list));
                                //_ids.add(new InclusionDependency(newRightTable, list, newLeftTable, list));
                            }//end for loop
                        }

                        doesDecompose = true;
                    }//end for loop
                }//end for loop
            }
        }//end while loop
    }//end bcnfDecompose

}//end class

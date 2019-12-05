import java.util.ArrayList;

public class InclusionDependency {
    private String leftTable;
    private ArrayList<String> leftFields;
    private String rightTable;
    private ArrayList<String> rightFields;

    public InclusionDependency(String _leftTable, ArrayList<String> _leftFields, String _rightTable, ArrayList<String> _rightFields)
    {
        leftTable = _leftTable;
        leftFields = _leftFields;
        rightTable = _rightTable;
        rightFields = _rightFields;
    }//end constructor

    public void reverse()
    {
        new InclusionDependency(rightTable, rightFields, leftTable, leftFields);
    }//end reverse
}//end class

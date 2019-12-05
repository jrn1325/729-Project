import de.metanome.algorithm_integration.results.InclusionDependency;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;

public class PrintingInclusionDependencyReceiver implements InclusionDependencyResultReceiver
{
    /**
     * Always return true
     * @param id
     * @return
     */
    @Override
    public Boolean acceptedResult(InclusionDependency id)
    {
        return true;
    }

    /**
     * Print the inclusion dependency
     * @param id
     */
    @Override
    public void receiveResult(InclusionDependency id)
    {
        System.out.println(id.toString());
    }
}//end class


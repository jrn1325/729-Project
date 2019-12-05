import de.metanome.algorithm_integration.results.FunctionalDependency;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;


public class PrintingFunctionalDependencyReceiver implements FunctionalDependencyResultReceiver
{
    public Boolean acceptedResult(FunctionalDependency fd)
    {
        return true;
    }

    public void receiveResult(FunctionalDependency fd)
    {
        System.out.println(fd.toString());
    }
}

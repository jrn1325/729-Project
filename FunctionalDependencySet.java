import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class FunctionalDependencySet {

    private HashMap<Set<String>, LinkedHashSet<String>>  fds = new HashMap<>();

    public boolean contains(FunctionalDependency fd)
    {
        return true;
    }

    public void removeField(String field)
    {

    }
}

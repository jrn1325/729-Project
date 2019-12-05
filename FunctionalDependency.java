import java.util.Set;

public class FunctionalDependency {
    private Set<String> left;
    private String right;

    public FunctionalDependency(Set<String> _left, String _right)
    {
        left = _left;
        right = _right;
    }//end constructor

    public boolean isTrivial()
    {
        return left.contains(right);
    }//end isTrivial
}//end class

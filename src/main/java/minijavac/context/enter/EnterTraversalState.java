package minijavac.context.enter;

import minijavac.utils.TraversalState;

import java.util.HashSet;
import java.util.Set;

/**
 * Maintains the state of the current {@link minijavac.ast.ClassDecl} traversal being undertaken by the
 * {@link Enter} visitor implementation.
 */
public class EnterTraversalState extends TraversalState {

    /*
     * Class names that were referenced but unable to be resolved
     */
    private final Set<String> referencedClasses;

    public EnterTraversalState() {
        this.referencedClasses = new HashSet<>();
    }

    public Set<String> getReferencedClasses() {
        return referencedClasses;
    }

    public void addReferencedClass(String refClass) {
        referencedClasses.add(refClass);
    }
}

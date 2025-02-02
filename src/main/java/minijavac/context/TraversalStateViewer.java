package minijavac.context;

import minijavac.ast.ClassDecl;
import minijavac.ast.MethodDecl;
import minijavac.utils.TraversalState;

/**
 * Encloses a given {@link TraversalState} instance and provides selective access to its fields, to be used by the
 * {@link minijavac.context.SymbolTable} to construct errors.
 */
public class TraversalStateViewer {

    private final TraversalState traversalState;

    public TraversalStateViewer(TraversalState traversalState) {
        this.traversalState = traversalState;
    }

    public ClassDecl getCurrClass() {
        return traversalState.getCurrClass();
    }

    public MethodDecl getCurrMethod() {
        return traversalState.getCurrMethod();
    }
}

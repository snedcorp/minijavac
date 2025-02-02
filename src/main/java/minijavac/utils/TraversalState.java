package minijavac.utils;

import minijavac.ast.ClassDecl;
import minijavac.ast.MethodDecl;

/**
 * Base class for maintaining {@link minijavac.ast.AST} traversal state, used by {@link minijavac.ast.Visitor} implementations
 * as they traverse a {@link ClassDecl}.
 */
public class TraversalState {

    private ClassDecl currClass;
    private MethodDecl currMethod;

    public ClassDecl getCurrClass() {
        return currClass;
    }

    public void setCurrClass(ClassDecl currClass) {
        this.currClass = currClass;
    }

    public MethodDecl getCurrMethod() {
        return currMethod;
    }

    public void setCurrMethod(MethodDecl currMethod) {
        this.currMethod = currMethod;
    }
}

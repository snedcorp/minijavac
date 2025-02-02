package minijavac.gen;

import minijavac.ast.AST;

/**
 * Argument type for {@link Generator} visitor implementation. Used by certain {@link AST} nodes to pass contextual
 * information down to their child nodes.
 */
public enum GenArg {
    /**
     * Nothing to indicate to child nodes.
     */
    NONE,
    /**
     * Indicates that the current reference node is on the left side of an assignment statement.
     */
    LHS,
    /**
     * Indicates that the current expression node is also a statement (prefix & postfix).
     */
    EXPR_STMT,
    /**
     * Indicates that the current reference node is within a prefix or postfix operation.
     */
    FIX;
}

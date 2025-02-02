package minijavac.context;

import minijavac.ast.AST;

/**
 * Argument type for {@link Context} visitor implementation. Used by certain {@link AST} nodes to pass contextual
 * information down to their child nodes.
 */
public enum ConArg {
    /**
     * Nothing to indicate to child nodes.
     */
    NONE,
    /**
     * Indicates that the current node is the first (and only) statement within a conditional block.
     */
    COND_STMT,
    /**
     * Indicates that the current node is supposed to reference a method declaration.
     */
    METHOD;
}

package minijavac.context.enter;

import minijavac.ast.AST;

/**
 * Argument type for {@link Enter} visitor implementation. Used by certain {@link AST} nodes to pass contextual
 * information down to their child nodes.
 */
public enum EnterArg {
    /**
     * Nothing to indicate to child nodes.
     */
    NONE,
    /**
     * Indicates that the current node is part of the standard library, and not from a user-provided source file.
     */
    STDLIB,
    /**
     * Indicates that the current node is the leading reference of a qualified reference.
     */
    QREF;
}

package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a break statement.
 *
 * Example:
 *  - {@code break;}
 * </pre>
 */
public class BreakStmt extends Statement {

    public BreakStmt(Position pos) {
        super(pos);
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitBreakStmt(this, s, a);
    }
}

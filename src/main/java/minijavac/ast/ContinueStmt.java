package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a continue statement.
 *
 * Example:
 *  - {@code continue;}
 * </pre>
 */
public class ContinueStmt extends Statement {

    public ContinueStmt(Position pos) {
        super(pos);
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitContinueStmt(this, s, a);
    }
}

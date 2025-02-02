package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a while statement.
 *
 * Example:
 *  - {@code while (x < 10) { x++; }}
 * </pre>
 */
public class WhileStmt extends Statement {

    public Expression cond;
    public Statement body;

    public WhileStmt(Expression cond, Statement body, Position pos) {
        super(pos);
        this.cond = cond;
        this.body = body;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitWhileStmt(this, s, a);
    }
}

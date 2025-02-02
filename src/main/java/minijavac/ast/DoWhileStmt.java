package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a do-while statement.
 *
 * Example:
 *  - {@code
 *      do {
 *              num += i;
 *              i++;
 *          } while (i < 5);
 *  }
 * </pre>
 */
public class DoWhileStmt extends Statement {

    public Expression cond;
    public Statement body;

    public DoWhileStmt(Expression cond, Statement body, Position pos) {
        super(pos);
        this.cond = cond;
        this.body = body;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitDoWhileStmt(this, s, a);
    }
}

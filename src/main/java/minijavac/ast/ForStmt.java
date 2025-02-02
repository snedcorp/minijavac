package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a for statement.
 *
 * Example:
 *  - {@code
 *      for (int i=0; i<5; i++) {
 *              num += i;
 *          }
 *  }
 * </pre>
 */
public class ForStmt extends Statement {

    public VarDeclStmt initStmt;
    public Expression cond;
    public ExprStatement updateStmt;
    public Statement body;

    public ForStmt(VarDeclStmt initStmt, Expression cond, ExprStatement updateStmt, Statement body, Position pos) {
        super(pos);
        this.initStmt = initStmt;
        this.cond = cond;
        this.updateStmt = updateStmt;
        this.body = body;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitForStmt(this, s, a);
    }
}

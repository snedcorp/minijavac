package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing an if statement.
 *
 * Examples:
 *  - {@code
 *      if (num < 5) {
 *              return 0;
 *          }
 *  }
 *  - {@code
 *      if (num < 5 {
 *              return 0;
 *          } else if (num < 10) {
 *              return 1;
 *          }
 *  }
 *  - {@code
 *      if (num < 5 {
 *              return 0;
 *          } else if (num < 10) {
 *              return 1;
 *          } else {
 *              return 2;
 *          }
 *  }
 * </pre>
 */
public class IfStmt extends Statement {

    public Expression cond;
    public Statement thenStmt;
    public Statement elseStmt;

    public IfStmt(Expression cond, Statement thenStmt, Statement elseStmt, Position pos) {
        super(pos);
        this.cond = cond;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
    }
    
    public IfStmt(Expression cond, Statement thenStmt, Position pos) {
        super(pos);
        this.cond = cond;
        this.thenStmt = thenStmt;
        this.elseStmt = null;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitIfStmt(this, s, a);
    }
}
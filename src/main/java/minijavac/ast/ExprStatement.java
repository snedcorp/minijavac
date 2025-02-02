package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing an expression statement (standalone prefix & postfix expressions).
 *
 * Examples:
 *  - {@code count++;}
 *  - {@code --count;}
 * </pre>
 */
public class ExprStatement extends Statement {

    public Expression expr;

    public ExprStatement(Expression expr, Position pos) {
        super(pos);
        this.expr = expr;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitExprStmt(this, s, a);
    }
}

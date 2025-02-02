package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a ternary expression.
 *
 * Examples:
 *  - {@code x == 10 ? 1 : 0}
 *  - {@code isTrue() : foo() : bar()}
 * </pre>
 */
public class TernaryExpr extends Expression {

    public Expression cond;
    public Expression expr1;
    public Expression expr2;

    public TernaryExpr(Expression cond, Expression expr1, Expression expr2, Position pos) {
        super(pos);
        this.cond = cond;
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitTernaryExpr(this, s, a);
    }
}

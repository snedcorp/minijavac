package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a unary expression.
 *
 * Examples:
 *  - {@code -x}
 *  - {@code !cond}
 *  - {@code ~num}
 * </pre>
 */
public class UnaryExpr extends Expression {

    public Operator operator;
    public Expression expr;
    public Type type;

    public UnaryExpr(Operator operator, Expression expr, Position pos) {
        super(pos);
        this.operator = operator;
        this.expr = expr;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitUnaryExpr(this, s, a);
    }
}
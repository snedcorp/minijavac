package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a postfix expression.
 *
 * Examples:
 *  - {@code count++}
 *  - {@code num--}
 * </pre>
 */
public class PostfixExpr extends Expression {

    public Operator operator;
    public Expression expr;

    public PostfixExpr(Operator operator, Expression expr, Position pos) {
        super(pos);
        this.operator = operator;
        this.expr = expr;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitPostfixExpr(this, s, a);
    }
}

package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a binary expression.
 *
 * Examples:
 *  - {@code 1 + 2}
 *  - {@code a + b - c * d / e % f}
 * </pre>
 */
public class BinaryExpr extends Expression {

    public Operator operator;
    public Expression left;
    public Expression right;
    public Type leftType;
    public Type rightType;

    public BinaryExpr(Operator o, Expression left, Expression right, Position pos) {
        super(pos);
        operator = o;
        this.left = left;
        this.right = right;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitBinaryExpr(this, s, a);
    }
}
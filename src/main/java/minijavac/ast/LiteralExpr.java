package minijavac.ast;

import minijavac.utils.TraversalState;

/**
 * AST node representing a literal expression.
 * <br><br>
 * @see minijavac.ast.IntLiteral
 * @see minijavac.ast.FloatLiteral
 * @see minijavac.ast.BooleanLiteral
 * @see minijavac.ast.NullLiteral
 */
public class LiteralExpr extends Expression {

    public Terminal literal;

    public LiteralExpr(Terminal literal) {
        super(literal.pos);
        this.literal = literal;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitLiteralExpr(this, s, a);
    }
}
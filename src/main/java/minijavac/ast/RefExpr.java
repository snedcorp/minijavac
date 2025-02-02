package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a reference located inside an expression.
 *
 * Examples:
 *  - {@code int foo =}<b>{@code bar}</b>{@code ;}
 *  - {@code count = 10 *}<b>{@code this.count}</b>{@code ;}
 * </pre>
 */
public class RefExpr extends Expression {

    public Reference ref;

    public RefExpr(Reference ref, Position pos) {
        super(pos);
        this.ref = ref;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitRefExpr(this, s, a);
    }
}

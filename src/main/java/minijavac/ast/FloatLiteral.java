package minijavac.ast;

import minijavac.syntax.Token;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * {@link Terminal} variant representing a floating-point literal.
 *
 * Examples:
 *  - {@code 1.23}
 *  - {@code 3.14159}
 * </pre>
 */
public class FloatLiteral extends Terminal {

    public FloatLiteral(Token token) {
        super(token);
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitFloatLiteral(this, s, a);
    }
}

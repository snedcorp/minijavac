package minijavac.ast;

import minijavac.syntax.Token;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * {@link Terminal} variant representing a null literal.
 *
 * Example:
 *  - {@code null}
 * </pre>
 */
public class NullLiteral extends Terminal {

    public NullLiteral(Token token) {
        super(token);
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitNullLiteral(this, s, a);
    }
}

package minijavac.ast;

import minijavac.syntax.Token;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * {@link Terminal} variant representing operators (arithmetic, logical, assignment).
 *
 * Examples:
 *  - {@code +}
 *  - {@code >>}
 *  - {@code &&}
 *  - {@code *=}
 * </pre>
 */
public class Operator extends Terminal {

    public Operator (Token token) {
    super (token);
  }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitOperator(this, s, a);
    }
}

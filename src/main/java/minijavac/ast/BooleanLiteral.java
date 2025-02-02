package minijavac.ast;

import minijavac.syntax.Token;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * {@link Terminal} variant representing a boolean literal.
 *
 * Examples:
 *  - {@code true}
 *  - {@code false}
 * </pre>
 */
public class BooleanLiteral extends Terminal {

  public BooleanLiteral(Token token) {
    super(token);
  }

  @Override
  public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
    return v.visitBooleanLiteral(this, s, a);
  }
}

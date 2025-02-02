package minijavac.ast;

import minijavac.syntax.Token;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * {@link Terminal} variant representing an integer literal.
 *
 * Examples:
 *  - {@code 1}
 *  - {@code 2345}
 * </pre>
 */
public class IntLiteral extends Terminal {

  public IntLiteral(Token token) {
    super(token);
  }

  @Override
  public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
    return v.visitIntLiteral(this, s, a);
  }
}
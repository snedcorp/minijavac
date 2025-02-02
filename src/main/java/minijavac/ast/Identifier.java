package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.syntax.Token;
import minijavac.syntax.TokenKind;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * {@link Terminal} variant representing an identifier (non-reserved {@link String} literal).
 *
 * Examples:
 *  - {@code foo}
 *  - {@code a11y}
 *  - {@code m10_z}
 * </pre>
 */
public class Identifier extends Terminal {

  public Identifier (Token token) {
    super(token);
  }

  /**
   * Factory method for manually creating an {@code Identifier} from a given {@link String}, used for standard library
   * classes and members.
   */
  public static Identifier of(String id) {
    return new Identifier(new Token(TokenKind.IDENTIFIER, id, null));
  }

  /**
   * Factory method for creating an identical {@code Identifier} at a different position.
   * @param pos new position
   */
  public Identifier atPos(Position pos) {
    return new Identifier(new Token(kind, contents, pos));
  }

  @Override
  public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
    return v.visitIdentifier(this, s, a);
  }
}

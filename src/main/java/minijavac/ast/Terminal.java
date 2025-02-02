package minijavac.ast;

import minijavac.syntax.Token;
import minijavac.syntax.TokenKind;

/**
 * Abstract base class for all terminal nodes (literals and operators).
 * <br><br>
 * @see BooleanLiteral
 * @see FloatLiteral
 * @see Identifier
 * @see IntLiteral
 * @see NullLiteral
 * @see Operator
 */
abstract public class Terminal extends AST {

    public TokenKind kind;
    public String contents;

    public Terminal (Token token) {
	  super(token.pos);
      contents = token.contents;
      kind = token.kind;
    }
}

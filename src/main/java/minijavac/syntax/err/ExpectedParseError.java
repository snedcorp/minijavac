package minijavac.syntax.err;

import minijavac.syntax.Token;
import minijavac.syntax.TokenKind;

/**
 * {@link ParseError} that occurs when a specific {@link TokenKind} is expected, but not found.
 * <br><br>Previous token is needed, if available, for correct caret placement when printing.
 */
public class ExpectedParseError extends ParseError {

    private Token prevToken;
    private final TokenKind expectedKind;

    public ExpectedParseError(Token prevToken, Token token, TokenKind expectedKind) {
        super(prevToken.pos, token, String.format("'%s' expected", expectedKind.print()));
        this.prevToken = prevToken;
        this.expectedKind = expectedKind;
    }

    public ExpectedParseError(Token token, TokenKind expectedKind) {
        super(token.pos, token, String.format("'%s' expected", expectedKind.print()));
        this.expectedKind = expectedKind;
    }

    public Token getPrevToken() {
        return prevToken;
    }

    public TokenKind getExpectedKind() {
        return expectedKind;
    }

    @Override
    public int getCaretOffset() {
        int offset = getPos().offset() + 1;
        if (prevToken != null) {
            return offset + prevToken.contents.length();
        }
        return offset;
    }
}

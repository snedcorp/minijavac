package minijavac.syntax.err;

import minijavac.err.CompileError;
import minijavac.syntax.Position;
import minijavac.syntax.Token;

/**
 * Generic error occurring during syntactic analysis.
 */
public class ParseError extends CompileError {

    private final Token token;

    public ParseError(Token token, String msg) {
        super(token.pos, msg);
        this.token = token;
    }

    protected ParseError(Position pos, Token token, String msg) {
        super(pos, msg);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}

package minijavac.syntax;

/**
 * Smallest meaningful lexical unit recognized by the compiler.
 */
public class Token {

    public TokenKind kind;
    public String contents;
    public Position pos;

    public Token(TokenKind kind, String contents, Position pos) {
        this.kind = kind;
        this.contents = contents;
        this.pos = pos;
    }

    public Position endPos() {
        return new Position(pos.file(), pos.line(), pos.offset() + contents.length());
    }
}

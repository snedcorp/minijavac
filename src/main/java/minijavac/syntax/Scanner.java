package minijavac.syntax;

import minijavac.listener.Listener;
import minijavac.err.CompileError;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Performs lexical analysis - given a source file, repeatedly returns the next available {@link Token}.
 */
public class Scanner {

    private final InputStream stream;
    private final Path file;
    private final Listener listener;

    /*
    * Last read character from file.
    * */
    private char current;

    /*
    * Builder for contents of token currently being scanned.
    * */
    private StringBuilder contents;

    /*
    * Indicates if file has been exhausted.
    * */
    private boolean done;

    /*
    * Current line within file.
    * */
    private int line = 1;

    /*
    * Current offset within line.
    * */
    private int offset = -1;

    /*
    * Starting offset for token currently being scanned.
    * */
    private int tokenOffset;

    /*
    * Special token indicating file has been exhausted.
    * */
    private static final Token eofToken = new Token(TokenKind.EOF, null, null);

    public Scanner(InputStream stream, Path file, Listener listener) throws IOException {
        this.stream = stream;
        this.file = file;
        this.listener = listener;
        next();
    }

    /**
     * Scans for next token.
     * <br>br>
     * Wrapper function, enclosed in loop to filter out comments.
     * @return next token from file
     * @throws IOException file unable to be read
     */
    public Token scan() throws IOException {
        while (true) {
            try {
                Token token = scanToken();
                if (token.kind != TokenKind.COMMENT) {
                    return token;
                }
            } catch (CompileError err) {
                listener.err(err);
                next();
            }
        }
    }

    /**
     * Scans for next token.
     * <br><br>
     * Returns EOF token if file has been exhausted.
     * @return next token from file
     * @throws CompileError invalid character
     * @throws IOException  file unable to be read
     */
    private Token scanToken() throws CompileError, IOException {
        // skip past whitespace + newlines
        while (!done && (current == ' ' || current == '\n' || current == '\t' || current == '\r')) {
            next();
        }

        if (done) return eofToken;

        contents = new StringBuilder();
        TokenKind kind;
        tokenOffset = offset;

        if (isLeadingIdentifier(current)) {
            take();
            while (isIdentifier(current)) {
                take();
            }
            kind = TokenKind.spellingMap.getOrDefault(contentsStr(), TokenKind.IDENTIFIER);
        } else if (isNumber(current)) {
            take();
            while (isNumber(current)) {
                take();
            }
            if (current == '.') {
                take();
                while (isNumber(current)) {
                    take();
                }
                kind = TokenKind.FLOAT_NUM;
            } else {
                kind = TokenKind.NUM;
            }
        } else {
            kind = scanOpOrSymbol();
        }

        return new Token(kind, contentsStr(), new Position(file, line, tokenOffset));
    }

    /**
     * Scans for operators (+, -, %, etc.) and symbols (;, {, :, etc.)
     * @return next token kind
     * @throws CompileError invalid character
     * @throws IOException  file unable to be read
     */
    private TokenKind scanOpOrSymbol() throws CompileError, IOException {
        String currStr = String.valueOf(current);
        if (TokenKind.spellingMap.containsKey(currStr)) {
            if (current == '/') return scanPotentialComment();

            // repeat candidates -> (>, <, &, |, +, -)
            //     - returns -> (>, <, &, |, >>, <<, >>>, &&, ||, ++, --)
            // compound candidates -> (=, !, >, <, +, -, *, /, %, &, ^, |, <<, >>, >>>)

            if (TokenKind.isRepeater(currStr)) {
                char first = current;
                take();
                if (current == first) {
                    take();
                    if (first == '>' && current == '>') { // short circuit for >>>
                        take();
                    } else if (first != '>' && first != '<') { // short circuit for &&, ||, ++, --
                        return TokenKind.spellingMap.get(contentsStr());
                    }
                }
            }

            if (TokenKind.isCompoundPrefix(currOrContentsStr())) {
                if (contents.isEmpty()) {
                    take();
                }
                if (current == '=') {
                    take();
                }
                return TokenKind.spellingMap.get(contentsStr());
            }

            TokenKind kind = TokenKind.spellingMap.get(currStr);
            take();
            return kind;
        }

        throw new CompileError(new Position(file, line, offset), String.format("illegal character: '%c'", current));
    }

    /**
     * Scans for potential comments and if found, attempts to move past them.
     * @return next token kind
     * @throws CompileError unclosed comment
     * @throws IOException  file unable to be read
     */
    private TokenKind scanPotentialComment() throws CompileError, IOException {
        take();
        if (current == '/') {
            skipLine();
            return TokenKind.COMMENT;
        } else if (current == '*') {
            int startLine = line;
            next();
            skipComment(startLine);
            return TokenKind.COMMENT;
        } else if (current == '=') {
            take();
            return TokenKind.DIVIDE_ASSIGN;
        }
        return TokenKind.DIVIDE;
    }

    /**
     * Attempts to advance past a multi-line comment.
     * @param startLine originating line
     * @throws CompileError unclosed comment
     * @throws IOException  file unable to be read
     */
    private void skipComment(int startLine) throws CompileError, IOException {
        while (true) {
            if (done) {
                throw new CompileError(new Position(file, startLine, tokenOffset), "unclosed comment");
            }
            if (current == '*') {
                next();
                if (done) {
                    throw new CompileError(new Position(file, startLine, tokenOffset), "unclosed comment");
                }
                if (current == '/') {
                    next();
                    break;
                }
            } else {
                next();
            }
        }
    }

    private boolean isLeadingIdentifier(char c) {
        return isLetter(c) || c == '_' || c == '$';
    }

    private boolean isIdentifier(char c) {
        return isLetter(c) || isNumber(c) || c == '_' || c == '$';
    }

    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }

    private String contentsStr() {
        return contents.toString();
    }

    private String currOrContentsStr() {
        return !contents.isEmpty() ? contents.toString() : String.valueOf(current);
    }

    /**
     * Adds the current character to the builder for the token's contents, then advances to the next character.
     * @throws IOException file unable to be read
     */
    private void take() throws IOException {
        contents.append(current);
        next();
    }

    /**
     * Moves to the next line.
     * @throws IOException file unable to be read
     */
    private void skipLine() throws IOException {
        while (current != '\n') {
            if (done) return;
            next();
        }
        next();
    }

    /**
     * Advances to the next character in the file, while keeping track of the current line number and intra-line offset.
     * @throws IOException file unable to be read
     */
    private void next() throws IOException {
        if (done) return;
        if (current == '\n') {
            line++;
            offset = -1;
        }

        int c = stream.read();
        if (c == -1) done = true;

        current = (char) c;
        if (current == '\t') offset += 4;
        else offset++;
    }
}

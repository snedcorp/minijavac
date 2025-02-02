package minijavac.err;

import minijavac.syntax.Position;

/**
 * Base class for errors occurring at all stages of compilation.
 * <br>Contains a message string and a {@link Position} that can be used to generate a printable representation.
 */
public class CompileError extends Exception {

    private final Position pos;
    private final String msg;
    private boolean ignore = false;

    public CompileError(Position pos, String msg) {
        this.pos = pos;
        this.msg = msg;
    }

    public Position getPos() {
        return pos;
    }

    public String getMsg() {
        return msg;
    }

    /**
     * <pre>
     * Prints the error, in the real javac format.
     *
     * Example:
     *
     * {@literal Test.java:1: error: '<identifier>' expected}
     * {@literal class boolean {}}
     * {@literal      ^}
     * </pre>
     * @param line where the error occurred
     */
    public void print(String line) {
        System.err.printf("%s:%d: error: %s%n", pos.file(), pos.line(), msg);
        System.err.println(line);
        System.err.printf("%" + getCaretOffset() + "s%n", "^");
    }

    /**
     * @return offset of stored position + 1, to ensure correct, right justified printing of the caret below the
     * offending line.
     */
    public int getCaretOffset() {
        return pos.offset() + 1;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }
}

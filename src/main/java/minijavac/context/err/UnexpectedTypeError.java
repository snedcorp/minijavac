package minijavac.context.err;

import minijavac.err.CompileError;
import minijavac.syntax.Position;

/**
 * Type error that occurs when a reference is expected, but a literal is found.
 */
public class UnexpectedTypeError extends CompileError {

    public UnexpectedTypeError(Position pos) {
        super(pos, "unexpected type");
    }

    @Override
    public void print(String line) {
        super.print(line);
        System.err.println("  required: variable");
        System.err.println("  found:    value");
    }
}

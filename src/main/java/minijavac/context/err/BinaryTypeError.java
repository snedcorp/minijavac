package minijavac.context.err;

import minijavac.ast.Type;
import minijavac.err.CompileError;
import minijavac.syntax.Position;
import minijavac.syntax.TokenKind;

/**
 * Type error that occurs when a binary expression or compound assignment statement has mismatched types.
 */
public class BinaryTypeError extends CompileError {

    private final String t1;
    private final String t2;

    public BinaryTypeError(Position pos, TokenKind kind, Type t1, Type t2) {
        super(pos, String.format("bad operand types for binary operator '%s'", kind.print()));
        this.t1 = t1.print();
        this.t2 = t2.print();
    }

    @Override
    public void print(String line) {
        super.print(line);
        System.err.printf("  first type:  %s%n", t1);
        System.err.printf("  second type: %s%n", t2);
    }

    public String getT1() {
        return t1;
    }

    public String getT2() {
        return t2;
    }
}

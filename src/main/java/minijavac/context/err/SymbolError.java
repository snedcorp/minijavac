package minijavac.context.err;

import minijavac.ast.Identifier;
import minijavac.err.CompileError;
import minijavac.syntax.Position;

/**
 * <pre>
 * Error that occurs when a symbol (identifier) is unable to be matched with a corresponding declaration.
 *
 * A nested {@link SymbolError.Builder} class is provided for ease of composition, to mix and match the different
 * formats for symbol and location messages.
 *
 * Symbol examples:
 *   - "symbol:   variable foo"
 *   - "symbol:   method foo(int,bool)"
 *   - "symbol:   class Test"
 *
 * Location examples:
 *   - "location: class Test"
 *   - "location: variable foo of type Test"
 * </pre>
 */
public class SymbolError extends CompileError {

    private final String symbol;
    private final String location;
    private static final String SYMBOL_MSG = "cannot find symbol";

    public SymbolError(Builder builder) {
        super(builder.pos, SYMBOL_MSG);
        this.symbol = builder.symbol;
        this.location = builder.location;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getLocation() {
        return location;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void print(String line) {
        super.print(line);
        System.err.printf("symbol:   %s%n", symbol);
        System.err.printf("location: %s%n", location);
    }

    public static class Builder {

        private Position pos;
        private String symbol;
        private String location;

        public Builder position(Position pos) {
            this.pos = pos;
            return this;
        }

        public Builder variableSymbol(String id) {
            this.symbol = String.format("variable %s", id);
            return this;
        }

        public Builder methodSymbol(String id, String argTypes) {
            this.symbol = String.format("method %s(%s)", id, argTypes);
            return this;
        }

        public Builder classSymbol(String id) {
            this.symbol = String.format("class %s", id);
            return this;
        }

        public Builder classLocation(String id) {
            this.location = String.format("class %s", id);
            return this;
        }

        public Builder varOfTypeLocation(String id, String className) {
            this.location = String.format("variable %s of type %s", id, className);
            return this;
        }

        public Builder location(String className, Identifier prevId) {
            if (prevId != null) {
                varOfTypeLocation(prevId.contents, className);
            } else {
                classLocation(className);
            }
            return this;
        }

        public SymbolError build() {
            return new SymbolError(this);
        }
    }
}

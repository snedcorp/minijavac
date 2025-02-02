package minijavac.context.err;

import minijavac.ast.MethodDecl;
import minijavac.err.CompileError;
import minijavac.syntax.Position;

/**
 * Error that occurs when a call reference is being resolved and there is only one method declaration candidate for that
 * identifier, but the wrong number of arguments are being passed.
 */
public class ArgTypeError extends CompileError {

    private final String required;
    private final String found;
    private static final String REASON = "actual and formal argument lists differ in length";

    public ArgTypeError(Position pos, MethodDecl md, String found) {
        super(pos, String.format("%s %s in class %s cannot be applied to given types;",
                md.isConstructor() ? "constructor" : "method", md.id.contents, md.classDecl.id.contents));
        this.required = md.parameterDeclList.size() == 0 ? "no arguments" : md.parameterDeclList.signature();
        this.found = found.isEmpty() ? "no arguments" : found;
    }

    @Override
    public void print(String line) {
        super.print(line);
        System.err.printf("  required: %s%n", required);
        System.err.printf("  found:    %s%n", found);
        System.err.printf("  reason: %s%n", REASON);
    }

    public String getRequired() {
        return required;
    }

    public String getFound() {
        return found;
    }

    public String getReason() {
        return REASON;
    }
}

package minijavac.context.err;

import minijavac.ast.Identifier;
import minijavac.ast.MethodDecl;
import minijavac.err.CompileError;

import java.util.List;

/**
 * <pre>
 * Error that occurs when a call reference is being resolved and either:
 *   1) There are multiple method declaration candidates for the identifier, but none with the desired number of
 *     arguments, or:
 *   2) There are multiple method declaration candidates for the identifier with the desired number of
 *     arguments, but the call's argument types differ from the method's parameter types.
 *
 * Every method candidate is printed and in the first case, a generic length difference message is also given, but
 * in the second case, the actual mismatched types are provided as well.
 * </pre>
 */
public class NoSuitableMethodError extends CompileError {
    public final List<String> methodCandidates;
    public boolean isConstructor;
    public List<String> mismatches;

    public NoSuitableMethodError(Identifier id, String signature, List<MethodDecl> methodCandidates, boolean isConstructor) {
        super(id.pos, String.format("no suitable %s found for %s(%s)", isConstructor ? "constructor" : "method", id.contents, signature));
        this.methodCandidates = methodCandidates.stream()
                .map(md -> String.format("%s.%s(%s)", md.classDecl.id.contents, id.contents, md.parameterDeclList.signature()))
                .toList();
        this.isConstructor = isConstructor;
    }

    public NoSuitableMethodError(Identifier id, String signature, List<MethodDecl> methodCandidates,
                                 boolean isConstructor, List<String> mismatches) {
        this(id, signature, methodCandidates, isConstructor);
        this.mismatches = mismatches;
    }

    @Override
    public void print(String line) {
        super.print(line);
        for (int i=0; i<methodCandidates.size(); i++) {
            System.err.printf("    %s %s is not applicable%n", isConstructor ? "constructor" : "method", methodCandidates.get(i));
            if (mismatches != null) {
                System.err.printf("      (argument mismatch; %s)%n", mismatches.get(i));
            } else {
                System.err.println("      (actual and formal argument lists differ in length)");
            }
        }
    }
}

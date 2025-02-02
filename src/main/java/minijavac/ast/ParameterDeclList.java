package minijavac.ast;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Wrapper class for the list of parameter declarations within a {@link MethodDecl}.
 * <br><br>
 * Needs to be separate from the {@link MethodDecl} because some errors ({@link minijavac.context.err.ArgTypeError ArgTypeError})
 * need to print just the parameter portion of the signature.
 */
public class ParameterDeclList implements Iterable<ParameterDecl> {

    private final List<ParameterDecl> decls;

    public ParameterDeclList() {
    	decls = new ArrayList<>();
    }
    
    public void add(ParameterDecl decl) {
    	decls.add(decl);
    }
    
    public ParameterDecl get(int i) {
        return decls.get(i);
    }
    
    public int size() {
        return decls.size();
    }
    
    public Iterator<ParameterDecl> iterator() {
    	return decls.iterator();
    }

    /**
     * Generates parameter portion of method signature used for contextual analysis.
     */
    public String signature() {
        return decls.stream()
                .map(p -> p.type.print())
                .collect(Collectors.joining(","));
    }

    /**
     * Generates parameter portion of method descriptor used for code generation.
     */
    public String descriptor() {
        return decls.stream()
                .map(p -> p.type.descriptor())
                .collect(Collectors.joining(""));
    }

    public static ParameterDeclList of(ParameterDecl parameterDecl) {
        ParameterDeclList pdl = new ParameterDeclList();
        pdl.add(parameterDecl);
        return pdl;
    }
}

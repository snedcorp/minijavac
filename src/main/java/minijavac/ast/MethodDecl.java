package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.syntax.Token;
import minijavac.syntax.TokenKind;
import minijavac.utils.TraversalState;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * AST node representing a method declaration.
 *
 * Examples:
 *  - {@code public static void main(String[] args {...}}
 *  - {@code public Test() {...}}
 * </pre>
 */
public class MethodDecl extends MemberDecl {

    public ParameterDeclList parameterDeclList;
    public List<Statement> statementList;
    public MethodType methodType;

    /**
     * String representation of method used for error printing and contextual analysis.
     * Includes name and parameter types.
     */
    public String signature;
	
	public MethodDecl(MemberDecl memberDecl, ParameterDeclList parameterDeclList, List<Statement> statementList,
                      boolean isConstructor, Position pos) {
        super(memberDecl, pos);
        this.parameterDeclList = parameterDeclList;
        this.statementList = statementList;
        this.methodType = isConstructor ? MethodType.CONSTRUCTOR : MethodType.METHOD;
        signature = String.format("%s(%s)", id.contents, parameterDeclList.signature());
	}

    private MethodDecl(Identifier id) {
        this(new FieldDecl(Access.PACKAGE_PRIVATE, false, false, new BaseType(TypeKind.VOID, null), id, null),
                new ParameterDeclList(), new ArrayList<>(), true, null);
        classDecl = new ClassDecl(id);
    }

    /**
     * Factory method for default constructor.
     */
    public static MethodDecl defaultConstructor(String className) {
        return new MethodDecl(new Identifier(new Token(TokenKind.IDENTIFIER, className, null)));
    }

    /**
     * Factory method used for a standard library method.
     */
    public static MethodDecl stdLib(MemberDecl memberDecl, ParameterDeclList parameterDeclList) {
        return new MethodDecl(memberDecl, parameterDeclList, new ArrayList<>(), false, null);
    }

    public boolean isConstructor() {
        return this.methodType != MethodType.METHOD;
    }

    /**
     * @return String representation of method used for code generation - includes parameter and return types
     */
    public String descriptor() {
        return String.format("(%s)%s", parameterDeclList.descriptor(), type.descriptor());
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitMethodDecl(this, s, a);
    }
}

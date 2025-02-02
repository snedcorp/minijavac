package minijavac.ast;

import minijavac.syntax.Position;

/**
 * Abstract base class for all declarations, containing the {@link Identifier}, {@link Type}, and modification status.
 * <br>
 * @see minijavac.ast.ClassDecl
 * @see minijavac.ast.FieldDecl
 * @see minijavac.ast.LocalDecl
 * @see minijavac.ast.MemberDecl
 * @see minijavac.ast.MethodDecl
 * @see minijavac.ast.ParameterDecl
 * @see minijavac.ast.VarDecl
 */
public abstract class Declaration extends AST {

	public Identifier id;
	public Type type;
	public boolean isFinal;
	
	public Declaration(Identifier id, Type type, boolean isFinal, Position pos) {
		super(pos);
		this.id = id;
		this.type = type;
		this.isFinal = isFinal;
	}
}

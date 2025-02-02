package minijavac.ast;

import minijavac.syntax.Position;

/**
 * AST node representing a declaration local to a method invocation.
 * <br><br>
 * @see minijavac.ast.ParameterDecl
 * @see minijavac.ast.VarDecl
 */
public abstract class LocalDecl extends Declaration {

	// Used in code generation to determine the declaration's index within the method.
	private int localVarIndex;
	
	public LocalDecl(Identifier id, Type type, boolean isFinal, Position pos) {
		super(id, type, isFinal, pos);
	}

	public int getLocalVarIndex() {
		return localVarIndex;
	}

	public void setLocalVarIndex(int localVarIndex) {
		this.localVarIndex = localVarIndex;
	}
}

package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a local variable declaration within the method body.
 *
 * Examples:
 *  - {@code int x}
 *  - {@code final Test[][] arr}
 * </pre>
 */
public class VarDecl extends LocalDecl {
	
	public VarDecl(Identifier id, Type type, boolean isFinal, Position pos) {
		super(id, type, isFinal, pos);
	}

	@Override
	public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
		return v.visitVarDecl(this, s, a);
	}
}

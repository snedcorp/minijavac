package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a parameter declaration within the method signature.
 *
 * Examples:
 *  - {@code int x}
 *  - {@code final Test[][] arr}
 * </pre>
 */
public class ParameterDecl extends LocalDecl {
	
	public ParameterDecl(Type type, Identifier id, boolean isFinal, Position pos) {
		super(id, type, isFinal, pos);
	}

	/**
	 * Factory method for parameters in standard library methods.
	 */
	public static ParameterDecl stdLib(Type type) {
		return new ParameterDecl(type, null, false, null);
	}

	@Override
	public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
		return v.visitParameterDecl(this, s, a);
	}
}


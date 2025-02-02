package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a field declaration.
 *
 * Examples:
 * 	- {@code int x;}
 * 	- {@code private static Test t;}
 * 	- {@code final boolean b;}
 * </pre>
 */
public class FieldDecl extends MemberDecl {
	
	public FieldDecl(Access access, boolean isStatic, boolean isFinal, Type type, Identifier id, Position pos) {
    	super(access, isStatic, isFinal, type, id, pos);
	}

	public static FieldDecl stdLib(boolean isStatic, boolean isFinal, Type type, Identifier id) {
		return new FieldDecl(Access.PUBLIC, isStatic, isFinal, type, id, null);
	}

	@Override
	public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
		return v.visitFieldDecl(this, s, a);
	}
}


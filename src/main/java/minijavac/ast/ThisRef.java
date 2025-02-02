package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a reference to {@code this}.
 *
 * Contains declaration, stored at parent {@link BaseRef} level.
 *
 * Example:
 *  - {@code this}
 * </pre>
 */
public class ThisRef extends BaseRef {
	
	public ThisRef(Position pos) {
		super(pos);
	}

	@Override
	public Declaration getDecl() {
		return decl;
	}

	@Override
	public Identifier getId() {
		return null;
	}

	@Override
	public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
		return v.visitThisRef(this, s, a);
	}
	
}

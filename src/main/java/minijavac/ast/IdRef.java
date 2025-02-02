package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a plain identifier reference.
 *
 * Contains the identifier and its corresponding declaration, with the latter stored at the parent {@link BaseRef} level.
 *
 * Examples:
 *  - {@code foo}
 *  - {@code bar}
 * </pre>
 */
public class IdRef extends BaseRef {

	public Identifier id;
	
	public IdRef(Identifier id, Position pos) {
		super(pos);
		this.id = id;
	}

	@Override
	public Declaration getDecl() {
		return decl;
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
		return v.visitIdRef(this, s, a);
	}
}

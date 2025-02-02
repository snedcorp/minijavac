package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a return statement.
 *
 * Examples:
 *  - {@code return result;}
 *  - {@code return null;}
 *  - {@code return;}
 * </pre>
 */
public class ReturnStmt extends Statement {

	public Expression expr;

	public ReturnStmt(Expression expr, Position pos) {
		super(pos);
		this.expr = expr;
	}

	@Override
	public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
		return v.visitReturnStmt(this, s, a);
	}
}

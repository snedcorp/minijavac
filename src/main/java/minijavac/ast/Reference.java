package minijavac.ast;

import minijavac.syntax.Position;

/**
 * Abstract base class for all references.
 * <br><br>
 * Subclasses must implement methods for the retrieval of the referenced declaration, and the identifier within that
 * declaration.
 * <br><br>
 * Also contains boolean flag, for convenience purposes, indicating whether the reference is starting a static context.
 */
public abstract class Reference extends AST {

	public Reference(Position pos) {
		super(pos);
	}

	public abstract Declaration getDecl();
	public abstract Identifier getId();

	/**
	 * Indicates whether the reference has started a static context by referencing a class name.
	 */
	public boolean isStatic;
}

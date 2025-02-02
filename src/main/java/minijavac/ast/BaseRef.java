package minijavac.ast;

import minijavac.syntax.Position;

/**
 * Abstract class for {@link Reference} nodes that refer to a specific declaration.
 * <br><br>
 * {@link CallRef} and {@link IxRef} nodes are not subclasses, b/c they rely on their enclosed reference field to supply
 * the declaration, and exist only to add additional argument and indexing information on top of the base reference.
 * @see IdRef
 * @see QualRef
 * @see ThisRef
 */
public abstract class BaseRef extends Reference {

    public Declaration decl;

    public BaseRef(Position pos) {
        super(pos);
    }
}

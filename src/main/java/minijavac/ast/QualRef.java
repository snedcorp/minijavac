package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a "qualified" reference, of the format: {@code <reference>.<identifier>}.
 *
 * Contains the qualifying reference as well as the identifier and its corresponding declaration, with the
 * latter stored at the parent {@link BaseRef} level.
 *
 * Examples:
 *  - {@code foo.bar}
 *  - {@code this.x}
 *  - {@code foo(1).bar.baz}
 * </pre>
 */
public class QualRef extends BaseRef {

    public Reference ref;
    public Identifier id;

    public QualRef(Reference ref, Identifier id, Position pos) {
        super(pos);
        this.ref = ref;
        this.id  = id;
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
        return v.visitQualRef(this, s, a);
    }
}

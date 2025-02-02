package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

import java.util.List;

/**
 * <pre>
 * AST node representing an indexed reference.
 *
 * Contains the indexing expressions itself, and relies upon the enclosed {@code ref} field to supply the referenced
 * declaration.
 *
 * Examples:
 *  - {@code foo[1]}
 *  - {@code bar[1][2]}
 *  - {@code this.arr[1]}
 *  - {@code foo(1)[3]}
 * </pre>
 */
public class IxRef extends Reference {

    public Reference ref;
    public List<Expression> ixExprList;

    public IxRef(Reference ref, List<Expression> ixExprList, Position pos) {
        super(pos);
        this.ref = ref;
        this.ixExprList = ixExprList;
    }

    @Override
    public Declaration getDecl() {
        return ref.getDecl();
    }

    @Override
    public Identifier getId() {
        return ref.getId();
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitIxRef(this, s, a);
    }
}

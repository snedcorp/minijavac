package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

import java.util.List;

/**
 * <pre>
 * AST node representing a method invocation.
 *
 * Contains the argument list itself, and relies upon the enclosed {@code ref} field to supply the referenced method
 * declaration.
 *
 * Examples:
 *  - {@code foo()}
 *  - {@code bar(1)}
 *  - {@code this(1, null)}
 *  - {@code foo.bar(1, 2, 3)}
 * </pre>
 */
public class CallRef extends Reference {

    public Reference ref;
    public List<Expression> argList;

    public CallRef(Reference ref, List<Expression> argList, Position pos) {
        super(pos);
        this.ref = ref;
        this.argList = argList;
    }

    @Override
    public Declaration getDecl() {
        return ref.getDecl();
    }

    @Override
    public Identifier getId() {
        return null;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitCallRef(this, s, a);
    }
}

package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a call statement.
 *
 * Example:
 *  - {@code doSomething();}
 *  - {@code foo.bar(1);}
 * </pre>
 */
public class CallStmt extends Statement {

    public Reference methodRef;

    public CallStmt(Reference methodRef, Position pos) {
        super(pos);
        this.methodRef = methodRef;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitCallStmt(this, s, a);
    }
}
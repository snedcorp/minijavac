package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing an assignment statement.
 *
 * Examples:
 *      - {@code x = 10;}
 *      - {@code arr = new int[2];}
 *      - {@code b = false;}
 * </pre>
 */
public class AssignStmt extends Statement {

    public Reference ref;
    public Operator operator;
    public Expression val;

    public AssignStmt(Reference ref, Operator operator, Expression expr, Position pos) {
        super(pos);
        this.ref = ref;
        this.operator = operator;
        this.val = expr;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitAssignStmt(this, s, a);
    }
}
package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a new array expression with initialization.
 *
 * Examples:
 *  - {@code new int[]{1, 2, 3}}
 *  - {@code new boolean[][]{{true, false}, {false, true}}}
 * </pre>
 */
public class NewArrayInitExpr extends Expression {

    public Type elementType;
    public int dims;
    public ArrayType arrayType;
    public ArrayInitExpr initExpr;

    public NewArrayInitExpr(Type elementType, int dims, ArrayInitExpr initExpr, Position pos) {
        super(pos);
        this.elementType = elementType;
        this.dims = dims;
        this.initExpr = initExpr;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitNewArrayInitExpr(this, s, a);
    }
}

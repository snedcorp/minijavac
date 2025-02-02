package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

import java.util.List;

/**
 * <pre>
 * AST node representing a new array expression (without initialization).
 *
 * Examples:
 *  - {@code new int[5]}
 *  - {@code new Test[10][10]}
 * </pre>
 */
public class NewArrayExpr extends Expression {

    public Type elementType;
    public ArrayType arrayType;
    public List<Expression> sizeExprList;

    public NewArrayExpr(Type elementType, List<Expression> sizeExprList, Position pos) {
        super(pos);
        this.elementType = elementType;
        this.sizeExprList = sizeExprList;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitNewArrayExpr(this, s, a);
    }
}
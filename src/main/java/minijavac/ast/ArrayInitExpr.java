package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

import java.util.List;

/**
 * <pre>
 * AST node representing the initialization expression following an array declaration.
 *
 * Examples:
 *      - {@code int[] arr = new int[]}<b>{@code {1, 2, 3, 4}}</b>
 *      - {@code int[][] arr = new int[][]}<b>{@code {{1, 2}, {3, 4}}}</b>
 * </pre>
 */
public class ArrayInitExpr extends Expression {

    public List<Expression> exprList;

    public ArrayInitExpr(List<Expression> exprList, Position pos) {
        super(pos);
        this.exprList = exprList;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitArrayInitExpr(this, s, a);
    }
}

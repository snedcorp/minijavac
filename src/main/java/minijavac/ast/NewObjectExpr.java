package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

import java.util.List;

/**
 * <pre>
 * AST node representing a new object expression (i.e. instantiation).
 *
 * Examples:
 *  - {@code new Test()}
 *  - {@code new Test(1, false)}
 * </pre>
 */
public class NewObjectExpr extends Expression {

    public ClassType classType;
    public List<Expression> argList;
    public MethodDecl decl;

    public NewObjectExpr(ClassType classType, List<Expression> argList, Position pos) {
        super(pos);
        this.classType = classType;
        this.argList = argList;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitNewObjectExpr(this, s, a);
    }
}

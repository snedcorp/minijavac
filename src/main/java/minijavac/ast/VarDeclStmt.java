package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * AST node representing a local variable declaration statement.
 *
 * Examples:
 *  - {@code int x = 1;}
 *  - {@code Test t = new Test();}
 * </pre>
 */
public class VarDeclStmt extends Statement {

    public VarDecl decl;
    public Expression expr;

    public VarDeclStmt(VarDecl decl, Expression expr, Position pos) {
        super(pos);
        this.decl = decl;
        this.expr = expr;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitVarDeclStmt(this, s, a);
    }
}

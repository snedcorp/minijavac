package minijavac.ast;

import minijavac.syntax.Position;

/**
 * Abstract base class for all statements.
 * <br><br>
 * @see AssignStmt
 * @see BlockStmt
 * @see BreakStmt
 * @see CallStmt
 * @see ContinueStmt
 * @see DoWhileStmt
 * @see ExprStatement
 * @see ForStmt
 * @see IfStmt
 * @see ReturnStmt
 * @see VarDeclStmt
 * @see WhileStmt
 */
public abstract class Statement extends AST {

    public Statement(Position pos) {
        super(pos);
    }
}

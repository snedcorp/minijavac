package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

import java.util.List;

/**
 * <pre>
 * AST node representing a block statement.
 *
 * Examples:
 * - {@code {}}
 * - {@code {
 *      x = 2;
 *      y = 3;
 *    }}
 */
public class BlockStmt extends Statement {

    public List<Statement> statements;
    public BlockStmt(List<Statement> statements, Position pos) {
        super(pos);
        this.statements = statements;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitBlockStmt(this, s, a);
    }
}
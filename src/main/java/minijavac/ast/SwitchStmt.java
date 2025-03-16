package minijavac.ast;

import minijavac.utils.TraversalState;

import java.util.List;
import java.util.TreeMap;

import minijavac.syntax.Position;

public class SwitchStmt extends Statement {

    public Expression expr;

    public List<Expression> caseLabels;
    public List<List<Statement>> caseBodies;
    public Integer defaultIx;

    public SwitchStmt(Expression expr, List<Expression> caseLabels,
                      List<List<Statement>> caseBodies, Integer defaultIx, Position pos) {
        super(pos);
        this.expr = expr;
        this.caseLabels = caseLabels;
        this.caseBodies = caseBodies;
        this.defaultIx = defaultIx;
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitSwitchStmt(this, s, a);
    }
}

package minijavac.context;

import minijavac.ast.VarDecl;
import minijavac.utils.TraversalState;

/**
 * Maintains the state of the current {@link minijavac.ast.ClassDecl} traversal being undertaken by the
 * {@link minijavac.context.Context} visitor implementation.
 */
public class ContextTraversalState extends TraversalState {

    /*
    * Set to the var decl on the LHS of a var decl stmt, to be used when traversing the RHS.
    * */
    private VarDecl currVarDecl;

    /*
    * Number of loops enclosing the current node.
    * */
    private int loopCnt;

    /*
     * Numbers of statements traversed so far in the current method body.
     */
    private int statementCnt;

    public VarDecl getCurrVarDecl() {
        return currVarDecl;
    }

    public void setCurrVarDecl(VarDecl currVarDecl) {
        this.currVarDecl = currVarDecl;
    }

    public int getLoopCnt() {
        return loopCnt;
    }

    public void enterLoop() {
        this.loopCnt++;
    }

    public void exitLoop() {
        this.loopCnt--;
    }

    public int getStatementCnt() {
        return statementCnt;
    }

    public void setStatementCnt(int statementCnt) {
        this.statementCnt = statementCnt;
    }

    public void addStatement() {
        this.statementCnt++;
    }
}

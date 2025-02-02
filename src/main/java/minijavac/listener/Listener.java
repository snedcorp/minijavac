package minijavac.listener;

import minijavac.ast.AST;
import minijavac.err.CompileError;

/**
 * Error listener, used for logging errors during building and traversal of the {@link AST}.
 */
public interface Listener {
    void err(CompileError err);
    boolean hasErrors();
    int getErrCnt();
    void setIgnore(boolean ignore);
}

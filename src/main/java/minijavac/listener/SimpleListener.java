package minijavac.listener;

import minijavac.err.CompileError;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple {@link Listener} implementation with no printing capabilities - used for testing.
 */
public class SimpleListener extends AbstractListener {

    private final List<CompileError> errors;

    public SimpleListener() {
        errors = new ArrayList<>();
    }

    @Override
    public void err(CompileError err) {
        if (!ignore) errors.add(err);
    }

    @Override
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public int getErrCnt() {
        return errors.size();
    }

    public List<CompileError> getErrors() {
        return errors;
    }
}

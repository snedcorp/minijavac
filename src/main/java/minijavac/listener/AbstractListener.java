package minijavac.listener;

/**
 * Abstract base class for {@link Listener} implementations.
 * <br><br>
 * Provides the {@link #ignore} field, so subclasses can implement selective error recording.
 */
public abstract class AbstractListener implements Listener {

    protected boolean ignore;

    @Override
    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }
}

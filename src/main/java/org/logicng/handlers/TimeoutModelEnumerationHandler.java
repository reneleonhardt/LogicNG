package org.logicng.handlers;

import org.logicng.datastructures.Assignment;
import org.logicng.datastructures.Tristate;

/**
 * A model enumeration handler which cancels the computation process after a given timeout.
 * @version 1.6.2
 * @since 1.0
 */
public class TimeoutModelEnumerationHandler implements ModelEnumerationHandler {

    private final long timeout;
    private long designatedEnd;
    private boolean aborted;

    /**
     * Constructs a new instance with a given timeout in milliseconds.
     * <p>
     * Note that it might take a few milliseconds more until the computation is actually
     * canceled, since the handler depends on the next found model.
     * @param timeout the timeout in milliseconds
     */
    public TimeoutModelEnumerationHandler(final long timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns whether the computation was aborted by the timeout handler.
     * @return {@code true} if the computation was aborted by the timeout handler, otherwise {@code false}
     */
    public boolean aborted() {
        return this.aborted;
    }

    /**
     * Returns the remaining time until the designated end.
     * @return the remaining time in milliseconds
     */
    public long remainingTime() {
        final long remainingTime = this.designatedEnd - System.currentTimeMillis();
        return remainingTime >= 0 ? remainingTime : 0L;
    }

    @Override
    public void started() {
        final long start = System.currentTimeMillis();
        this.designatedEnd = start + this.timeout;
        this.aborted = false;
    }

    @Override
    public boolean foundModel(final Assignment assignment) {
        this.aborted = System.currentTimeMillis() >= this.designatedEnd;
        return !this.aborted;
    }

    @Override
    public boolean solverResult(final Tristate result) {
        if (result == Tristate.UNDEF) {
            this.aborted = true;
        }
        return result == Tristate.TRUE;
    }
}

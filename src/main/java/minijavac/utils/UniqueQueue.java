package minijavac.utils;

import java.util.*;

/**
 * Generic Queue implementation ensuring that once an item has been added, it cannot be added again.
 * @param <T> item type
 */
public class UniqueQueue<T> {

    private final Deque<T> queue;
    private final Set<T> set;

    public UniqueQueue() {
        this.queue = new ArrayDeque<>();
        this.set = new HashSet<>();
    }

    /**
     * @return item at head of queue
     */
    public T poll() {
        return queue.pollFirst();
    }

    /**
     * Adds item to the tail of the queue, if it hasn't been added before.
     * @param item item to be enqueued
     * @return whether item was successfully enqueued
     */
    public boolean offer(T item) {
        if (set.contains(item)) return false;

        boolean added = queue.offerLast(item);
        if (added) set.add(item);
        return added;
    }

    /**
     * @return true if queue is empty
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

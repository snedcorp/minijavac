package minijavac.gen._byte;

import minijavac.gen.file.Writable;

/**
 * Abstract base class whose subclasses represent the smallest byte units that make up a class file.
 * @see U1
 * @see U2
 * @see U4
 */
public abstract class ByteUnit implements Writable {
    private final int val;

    public ByteUnit(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }
}

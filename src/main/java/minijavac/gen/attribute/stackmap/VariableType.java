package minijavac.gen.attribute.stackmap;

/**
 * Enumeration of the supported verification type tags for a local variable or operand stack entry, to be used within a {@link StackMapFrame}.
 */
public enum VariableType {

    INTEGER(1),
    FLOAT(2),
    NULL(5),
    OBJECT(7);

    VariableType(int val) {
        this.val = val;
    }

    private final int val;

    public int getVal() {
        return val;
    }
}

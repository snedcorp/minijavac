package minijavac.gen.attribute.stackmap;

import minijavac.gen.file.Writable;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Object representation of the verification type of a local variable or operand stack entry, to be used within a {@link StackMapFrame}.
 * <br><br>
 * Additional data beyond the type tag must be supplied by subclasses.
 */
public class VariableInfo implements Writable {
    private final VariableType typeTag;

    private static final VariableInfo INT = new VariableInfo(VariableType.INTEGER);
    private static final VariableInfo FLOAT = new VariableInfo(VariableType.FLOAT);
    private static final VariableInfo _NULL = new VariableInfo(VariableType.NULL);

    protected VariableInfo(VariableType typeTag) {
        this.typeTag = typeTag;
    }

    /**
     * @return static {@code Integer_variable_info} constant
     */
    public static VariableInfo _int() {
        return INT;
    }

    /**
     * @return static {@code Float_variable_info} constant
     */
    public static VariableInfo _float() {
        return FLOAT;
    }

    /**
     * @return static {@code Null_variable_info} constant
     */
    public static VariableInfo _null() { return _NULL; }

    /**
     * Writes verification type tag to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        stream.writeByte(typeTag.getVal()); // write tag
    }
}

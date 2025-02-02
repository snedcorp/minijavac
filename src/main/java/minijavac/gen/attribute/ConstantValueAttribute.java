package minijavac.gen.attribute;

import minijavac.gen._byte.U2;
import minijavac.gen._byte.U4;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Object representation of the {@code ConstantValue_attribute} attribute, containing a single constant pool index.
 */
public class ConstantValueAttribute extends Attribute {
    /**
     * Index of {@link minijavac.gen.constant.IntConstant IntConstant} or {@link minijavac.gen.constant.FloatConstant FloatConstant} entry.
     */
    private final U2 constantValueIndex;

    public ConstantValueAttribute(U2 attributeNameIndex, U2 constantValueIndex) {
        super(attributeNameIndex, U4.of(2));
        this.constantValueIndex = constantValueIndex;
    }

    /**
     * Writes {@code ConstantValue_attribute} attribute to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        super.writeTo(stream);              // write attribute_name_index, attribute_length
        constantValueIndex.writeTo(stream); // write constantvalue_index
    }

    public U2 getConstantValueIndex() {
        return constantValueIndex;
    }
}

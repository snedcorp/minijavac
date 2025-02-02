package minijavac.gen.file;

import minijavac.gen._byte.U2;
import minijavac.gen.attribute.ConstantValueAttribute;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Object representation of a {@code field_info} entry in a {@link ClassFile}.
 */
public class FieldEntry extends MemberEntry {
    private ConstantValueAttribute constantValueAttribute;

    /**
     * Writes {@code field_info} entry to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        super.writeTo(stream);                      // write access_flags, name_index, descriptor_index
        if (constantValueAttribute != null) {
            U2.of(1).writeTo(stream);           // write attributes_count
            constantValueAttribute.writeTo(stream); // write ConstantValue_attribute
        } else {
            U2.of(0).writeTo(stream);          // write attributes_count
        }
    }

    public ConstantValueAttribute getConstantValueAttribute() {
        return constantValueAttribute;
    }

    public void setConstantValueAttribute(ConstantValueAttribute constantValueAttribute) {
        this.constantValueAttribute = constantValueAttribute;
    }
}

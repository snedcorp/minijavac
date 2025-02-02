package minijavac.gen.constant;

import minijavac.gen._byte.U2;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Object representation of a {@code CONSTANT_NameAndType_info} entry.
 */
public class NameAndTypeConstant extends ConstantEntry {
    /**
     * Index of the {@link UTF8Constant} entry containing the reference's name.
     */
    private U2 nameIndex;

    /**
     * Index of the {@link UTF8Constant} entry containing the reference's descriptor.
     */
    private U2 descriptorIndex;

    public NameAndTypeConstant() {
        super(ConstantTag.NAME_AND_TYPE);
    }

    /**
     * Writes the {@code CONSTANT_NameAndType_info} entry to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        super.writeTo(stream);           // write tag
        nameIndex.writeTo(stream);       // write name_index
        descriptorIndex.writeTo(stream); // write descriptor_index
    }

    public U2 getNameIndex() {
        return nameIndex;
    }

    public void setNameIndex(U2 nameIndex) {
        this.nameIndex = nameIndex;
    }

    public U2 getDescriptorIndex() {
        return descriptorIndex;
    }

    public void setDescriptorIndex(U2 descriptorIndex) {
        this.descriptorIndex = descriptorIndex;
    }
}

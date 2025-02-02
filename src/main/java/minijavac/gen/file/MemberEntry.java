package minijavac.gen.file;

import minijavac.gen._byte.U2;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Abstract base class for a member entry in a {@link ClassFile}, containing its access, name, and descriptor.
 * @see FieldEntry
 * @see MethodEntry
 */
public abstract class MemberEntry implements Writable {
    /**
     * Member's access mask.
     */
    private U2 accessFlags;

    /**
     * Index of the {@link minijavac.gen.constant.UTF8Constant UTF8Constant} entry containing the member's name.
     */
    private U2 nameIndex;

    /**
     * Index of the {@link minijavac.gen.constant.UTF8Constant UTF8Constant} entry containing the member's descriptor.
     */
    private U2 descriptorIndex;

    /**
     * Writes the member's access, name, and descriptor to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        accessFlags.writeTo(stream);     // write access_flags
        nameIndex.writeTo(stream);       // write name_index
        descriptorIndex.writeTo(stream); // write descriptor_index
    }

    public U2 getAccessFlags() {
        return accessFlags;
    }

    public void setAccessFlags(U2 accessFlags) {
        this.accessFlags = accessFlags;
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

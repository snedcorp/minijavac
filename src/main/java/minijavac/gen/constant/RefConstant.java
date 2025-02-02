package minijavac.gen.constant;

import minijavac.gen._byte.U2;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Object representation of a {@code CONSTANT_Fieldref_info} or {@code CONSTANT_Methodref_info} entry.
 * <br><br>
 * Denotes a member reference, and contains links to its class, name, and type.
 */
public class RefConstant extends ConstantEntry {
    /**
     * Index of the {@link ClassConstant} entry containing the member's class.
     */
    private U2 classIndex;

    /**
     * Index of the {@link NameAndTypeConstant} entry containing the member's name and type.
     */
    private U2 nameAndTypeIndex;

    public RefConstant(ConstantTag tag) {
        super(tag);
    }

    /**
     * Factory method for creating a {@code CONSTANT_Fieldref_info} entry.
     * @return entry
     */
    public static RefConstant field() {
        return new RefConstant(ConstantTag.FIELD_REF);
    }

    /**
     * Factory method for creating a {@code CONSTANT_Methodref_info} entry.
     * @return entry
     */
    public static RefConstant method() {
        return new RefConstant(ConstantTag.METHOD_REF);
    }

    /**
     * Writes the {@code CONSTANT_Fieldref_info} or {@code CONSTANT_Methodref_info} entry to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        super.writeTo(stream);            // write tag
        classIndex.writeTo(stream);       // write class_index
        nameAndTypeIndex.writeTo(stream); // write name_and_type_index
    }

    public U2 getClassIndex() {
        return classIndex;
    }

    public void setClassIndex(U2 classIndex) {
        this.classIndex = classIndex;
    }

    public U2 getNameAndTypeIndex() {
        return nameAndTypeIndex;
    }

    public void setNameAndTypeIndex(U2 nameAndTypeIndex) {
        this.nameAndTypeIndex = nameAndTypeIndex;
    }
}

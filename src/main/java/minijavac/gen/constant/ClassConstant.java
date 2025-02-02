package minijavac.gen.constant;

import minijavac.gen._byte.U2;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Object representation of a {@code CONSTANT_Class_info} entry.
 */
public class ClassConstant extends ConstantEntry {
    /**
    * Index of the {@link UTF8Constant} entry containing the name of the class.
    * */
    private U2 nameIndex;

    public ClassConstant() {
        super(ConstantTag.CLASS);
    }

    /**
     * Writes the {@code CONSTANT_Class_info} entry to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        super.writeTo(stream);     // write tag
        nameIndex.writeTo(stream); // write name_index
    }

    public U2 getNameIndex() {
        return nameIndex;
    }

    public void setNameIndex(U2 nameIndex) {
        this.nameIndex = nameIndex;
    }
}

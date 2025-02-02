package minijavac.gen.attribute.stackmap;

import minijavac.gen._byte.U2;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Object representation of an object verification type, containing both a type tag and the constant pool index to the
 * corresponding class.
 */
public class ObjectVariableInfo extends VariableInfo {

    private final U2 classIndex;

    public ObjectVariableInfo(U2 classIndex) {
        super(VariableType.OBJECT);
        this.classIndex = classIndex;
    }

    /**
     * Writes the object verification type to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        super.writeTo(stream);      // write tag
        classIndex.writeTo(stream); // write cpool_index
    }

    public U2 getClassIndex() {
        return classIndex;
    }
}

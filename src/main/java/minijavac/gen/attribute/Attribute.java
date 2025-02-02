package minijavac.gen.attribute;

import minijavac.gen._byte.U2;
import minijavac.gen._byte.U4;
import minijavac.gen.file.Writable;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Abstract base class for all attributes in a {@link minijavac.gen.file.ClassFile ClassFile}, containing the constant pool
 * index for the attribute name, as well as the length of the attribute's contents.
 */
public abstract class Attribute implements Writable {
    /**
     * Index of the {@link minijavac.gen.constant.UTF8Constant UTF8Constant} containing the attribute's name.
     */
    private U2 attributeNameIndex;

    /**
     * Length of the attribute's contents in bytes, NOT including the initial six bytes defined here.
     */
    private U4 attributeLength;

    public Attribute() {}

    public Attribute(U2 attributeNameIndex) {
        this.attributeNameIndex = attributeNameIndex;
    }

    public Attribute(U2 attributeNameIndex, U4 attributeLength) {
        this.attributeNameIndex = attributeNameIndex;
        this.attributeLength = attributeLength;
    }

    /**
     * Writes the attribute's name and length to the byte stream.
     * <br><br>
     * Note: the writing of an attribute's contents is delegated to subclasses.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        attributeNameIndex.writeTo(stream); // write attribute_name_index
        attributeLength.writeTo(stream);    // write attribute_length
    }

    public U2 getAttributeNameIndex() {
        return attributeNameIndex;
    }

    public void setAttributeNameIndex(U2 attributeNameIndex) {
        this.attributeNameIndex = attributeNameIndex;
    }

    public U4 getAttributeLength() {
        return attributeLength;
    }

    public void setAttributeLength(U4 attributeLength) {
        this.attributeLength = attributeLength;
    }
}

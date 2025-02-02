package minijavac.gen.attribute;

import minijavac.gen._byte.U2;
import minijavac.gen._byte.U4;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Object representation of a {@code SourceFile_attribute} attribute.
 */
public class SourceFileAttribute extends Attribute {

    private final U2 sourceFileIndex;

    public SourceFileAttribute(U2 attributeNameIndex, U2 sourceFileIndex) {
        super(attributeNameIndex, U4.of(2));
        this.sourceFileIndex = sourceFileIndex;
    }

    /**
     * Writes {@code SourceFile_attribute} attribute to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        super.writeTo(stream);           // write attribute_name_index, attribute_length
        sourceFileIndex.writeTo(stream); // write sourcefile_index
    }

    public U2 getSourceFileIndex() {
        return sourceFileIndex;
    }
}

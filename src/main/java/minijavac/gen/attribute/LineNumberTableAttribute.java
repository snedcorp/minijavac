package minijavac.gen.attribute;

import minijavac.gen._byte.U2;
import minijavac.gen._byte.U4;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class LineNumberTableAttribute extends Attribute {
    private U2 lineNumberTableLength;
    private List<LineNumberEntry> lineNumberEntries;

    public LineNumberTableAttribute(U2 attributeNameIndex) {
        super(attributeNameIndex);
    }

    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        try (ByteArrayOutputStream attrByteStream = new ByteArrayOutputStream();
             DataOutputStream attrStream = new DataOutputStream(attrByteStream)) {

            lineNumberTableLength.writeTo(attrStream);
            for (LineNumberEntry lineNumberEntry : lineNumberEntries) {
                lineNumberEntry.writeTo(attrStream);
            }

            setAttributeLength(U4.of(attrByteStream.size()));
            super.writeTo(stream);

            attrByteStream.writeTo(stream);
        }
    }

    public U2 getLineNumberTableLength() {
        return lineNumberTableLength;
    }

    public void setLineNumberTableLength(U2 lineNumberTableLength) {
        this.lineNumberTableLength = lineNumberTableLength;
    }

    public List<LineNumberEntry> getLineNumberEntries() {
        return lineNumberEntries;
    }

    public void setLineNumberEntries(List<LineNumberEntry> lineNumberEntries) {
        this.lineNumberEntries = lineNumberEntries;
    }
}

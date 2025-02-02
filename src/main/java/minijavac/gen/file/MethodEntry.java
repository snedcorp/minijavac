package minijavac.gen.file;

import minijavac.gen._byte.U2;
import minijavac.gen.attribute.CodeAttribute;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Object representation of a {@code method_info} entry in a {@link ClassFile}, containing a {@link CodeAttribute} for
 * its body.
 */
public class MethodEntry extends MemberEntry {
    /**
     * Representation of method's body.
     */
    private CodeAttribute codeAttribute;

    /**
     * Writes the {@code method_info} entry to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        super.writeTo(stream);         // write access_flags, name_index, descriptor_index
        U2.of(1).writeTo(stream);  // write attributes_count
        codeAttribute.writeTo(stream); // write Code_attribute
    }

    public CodeAttribute getCodeAttribute() {
        return codeAttribute;
    }

    public void setCodeAttribute(CodeAttribute codeAttribute) {
        this.codeAttribute = codeAttribute;
    }
}

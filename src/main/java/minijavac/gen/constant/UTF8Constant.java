package minijavac.gen.constant;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Object representation of a {@code CONSTANT_Utf8_info} entry.
 */
public class UTF8Constant extends ConstantEntry {
    private final String str;

    public UTF8Constant(String str) {
        super(ConstantTag.UTF_8);
        this.str = str;
    }

    /**
     * Writes the {@code CONSTANT_Utf8_info} entry to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        super.writeTo(stream); // write tag
        stream.writeUTF(str);  // write length, bytes[]
    }

    public String getStr() {
        return str;
    }
}

package minijavac.gen.constant;

import minijavac.gen.file.Writable;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Abstract base class for {@link ConstantPool} entries, containing only the {@link ConstantTag} denoting the
 * constant type.
 * @see ClassConstant
 * @see FloatConstant
 * @see IntConstant
 * @see NameAndTypeConstant
 * @see RefConstant
 * @see UTF8Constant
 */
public abstract class ConstantEntry implements Writable {
    private final ConstantTag tag;

    public ConstantEntry(ConstantTag tag) {
        this.tag = tag;
    }

    /**
     * Writes tag byte to given byte stream.
     * <br><br>
     * Note: the writing of an entry's contents is delegated to subclasses.
     * @param stream byte stream
     * @throws IOException
     */
    public void writeTo(DataOutputStream stream) throws IOException {
        stream.writeByte(tag.getVal());
    }
}

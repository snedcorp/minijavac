package minijavac.gen.constant;

import minijavac.gen._byte.U4;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Object representation of a {@code CONSTANT_Float_info} entry.
 */
public class FloatConstant extends ConstantEntry {
    private final U4 bytes;

    public FloatConstant(int num) {
        super(ConstantTag.FLOAT);
        this.bytes = U4.of(num);
    }

    /**
     * Writes the {@code CONSTANT_Float_info} entry to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        super.writeTo(stream); // write tag
        bytes.writeTo(stream); // write bytes
    }
}

package minijavac.gen.constant;

import minijavac.gen._byte.U4;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Object representation of a {@code CONSTANT_Integer_info} entry.
 */
public class IntConstant extends ConstantEntry {
    private final U4 bytes;

    public IntConstant(int num) {
        super(ConstantTag.INTEGER);
        this.bytes = U4.of(num);
    }

    /**
     * Writes the {@code CONSTANT_Integer_info} entry to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        super.writeTo(stream); // write tag
        bytes.writeTo(stream); // write bytes
    }
}

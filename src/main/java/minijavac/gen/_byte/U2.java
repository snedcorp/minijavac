package minijavac.gen._byte;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Data type representing an unsigned two-byte quantity.
 */
public class U2 extends ByteUnit {

    private U2(int val) {
        super(val);
    }

    public static U2 of(int val) {
        return new U2(val);
    }

    /**
     * Writes two bytes to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        stream.writeShort(getVal());
    }
}

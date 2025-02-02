package minijavac.gen._byte;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Data type representing an unsigned one-byte quantity.
 */
public class U1 extends ByteUnit {

    private U1(int val) {
        super(val);
    }

    public static U1 of(int val) {
        return new U1(val);
    }

    /**
     * Writes one byte to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        stream.writeByte(getVal());
    }
}

package minijavac.gen._byte;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Data type representing an unsigned four-byte quantity.
 */
public class U4 extends ByteUnit {

    private U4(int val) {
        super(val);
    }

    public static U4 of(int val) {
        return new U4(val);
    }

    /**
     * Writes four bytes to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        stream.writeInt(getVal());
    }
}

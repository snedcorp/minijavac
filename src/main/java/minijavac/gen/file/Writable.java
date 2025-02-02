package minijavac.gen.file;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Interface implemented by {@link ClassFile} and all its components, with a single method {@link #writeTo(DataOutputStream)}
 * that should convert the component's internal state into its Java bytecode representation and write it to the
 * {@link DataOutputStream}.
 */
public interface Writable {
    void writeTo(DataOutputStream stream) throws IOException;
}

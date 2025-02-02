package minijavac.gen.attribute;

import minijavac.gen._byte.U2;
import minijavac.gen.file.Writable;

import java.io.DataOutputStream;
import java.io.IOException;

public class LineNumberEntry implements Writable {
    private U2 startPC;
    private U2 lineNumber;

    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        startPC.writeTo(stream);
        lineNumber.writeTo(stream);
    }

    public U2 getStartPC() {
        return startPC;
    }

    public void setStartPC(U2 startPC) {
        this.startPC = startPC;
    }

    public U2 getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(U2 lineNumber) {
        this.lineNumber = lineNumber;
    }
}

package minijavac.gen.instruction;

import minijavac.gen.file.Writable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * Object representation of a JVM instruction.
 *
 * JVM instructions are made up of a one-byte {@link OpCode} denoting the operation to be performed, followed by zero
 * or more operand bytes that supply arguments needed for the operation's execution.
 *
 * In addition to those fields, this class also contains a {@link #size} field denoting the instruction's length in
 * bytes (including the opcode), as well as an {@link #offset} field denoting its location (in bytes) within the method
 * body. Also, {@link #constantIndex} and {@link #localIndex} fields are available for easy access to constant pool or
 * local variable array indices, if applicable.
 *
 * A nested {@link Instruction.Builder} class is provided here for ease of composition, to allow clients a shorthand for
 * common instructions and operand types.
 *
 * Note:
 *   - There are no safeguards here to ensure that the size and composition of the operands list matches what the JVM
 *     expects for that particular opcode. It is up to the clients of this class to construct their operands in a manner
 *     consistent with the desired operation.
 * </pre>
 */
public class Instruction implements Writable {

    private final OpCode opCode;

    private List<Byte> operands;

    /*
    * Length in bytes
    * */
    private int size;

    /*
    * Byte location within method
    * */
    private int offset;

    private int constantIndex = -1;
    private int localIndex = -1;

    private Instruction(Builder builder) {
        this.opCode = builder.opCode;
        this.size = 1; // always at least 1 byte long
        this.constantIndex = builder.constantIndex;
        this.localIndex = builder.localIndex;
        if (builder.operands != null) {
            this.operands = builder.operands;
            this.size += builder.operands.size(); // now add operands size to get total byte length
        }
    }

    /**
     * Initializes instruction without any operands, sets size to 1.
     * @param opCode
     */
    protected Instruction(OpCode opCode) {
        this.opCode = opCode;
        this.size = 1;
    }

    /**
     * Creates new instruction with the given opcode.
     * @param opCode
     * @return instruction
     */
    public static Instruction of(OpCode opCode) {
        return new Instruction(opCode);
    }

    /**
     * Writes the instruction to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        stream.writeByte(opCode.getCode()); // write opcode
        if (operands != null) {
            for (int operand : operands) {  // write operands
                stream.writeByte(operand);
            }
        }
    }

    public OpCode getOpCode() {
        return opCode;
    }

    public List<Byte> getOperands() {
        return operands;
    }

    /**
     * Retrieves the first operand in the list.
     * @return operand
     */
    public int getByteOperand() {
        return operands.get(0);
    }

    protected void setOperands(List<Byte> operands) {
        this.operands = operands;
    }

    public int getSize() {
        return size;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getConstantIndex() {
        return constantIndex;
    }

    public int getLocalIndex() {
        return localIndex;
    }

    /**
     * Creates a new {@link Instruction.Builder}.
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OpCode opCode;
        private List<Byte> operands;
        private int constantIndex = -1;
        private int localIndex = -1;

        /**
         * Sets opcode on instruction.
         * @param opCode
         * @return builder
         */
        public Builder opCode(OpCode opCode) {
            this.opCode = opCode;
            return this;
        }

        /**
         * Adds operand to instruction.
         * @param operand
         * @return builder
         */
        public Builder operand(int operand) {
            if (operands == null) {
                operands = new ArrayList<>();
            }
            operands.add((byte) operand);
            return this;
        }

        /**
         * <pre>
         * Adds constant pool index to instruction's operands.
         *
         * Note:
         *   - Because operands are byte-sized and a constant pool index is an unsigned two-byte value, the index is
         *     split up into two operands, with its most significant byte as the first operand and its least significant
         *     byte as the second operand.
         * </pre>
         * @param index constant pool index
         * @return builder
         */
        public Builder constantIndex(int index) {
            constantIndex = index;
            operand(index >> 8);
            operand(index);
            return this;
        }

        /**
         * Sets opcode and operand on instruction for an {@code istore} variant.
         * @see OpCode#istore
         * @see OpCode#istore_0
         * @see OpCode#istore_1
         * @see OpCode#istore_2
         * @see OpCode#istore_3
         * @param index local variable index
         * @return builder
         */
        public Builder istore(int index) {
            localOp(OpCode.istore, index);
            return this;
        }

        /**
         * Sets opcode and operand on instruction for an {@code fstore} variant.
         * @see OpCode#fstore
         * @see OpCode#fstore_0
         * @see OpCode#fstore_1
         * @see OpCode#fstore_2
         * @see OpCode#fstore_3
         * @param index local variable index
         * @return builder
         */
        public Builder fstore(int index) {
            localOp(OpCode.fstore, index);
            return this;
        }

        /**
         * Sets opcode and operand on instruction for an {@code astore} variant.
         * @see OpCode#astore
         * @see OpCode#astore_0
         * @see OpCode#astore_1
         * @see OpCode#astore_2
         * @see OpCode#astore_3
         * @param index local variable index
         * @return builder
         */
        public Builder astore(int index) {
            localOp(OpCode.astore, index);
            return this;
        }

        /**
         * Sets opcode and operand on instruction for an {@code iload} variant.
         * @see OpCode#iload
         * @see OpCode#iload_0
         * @see OpCode#iload_1
         * @see OpCode#iload_2
         * @see OpCode#iload_3
         * @param index local variable index
         * @return builder
         */
        public Builder iload(int index) {
            localOp(OpCode.iload, index);
            return this;
        }

        /**
         * Sets opcode and operand on instruction for an {@code fload} variant.
         * @see OpCode#fload
         * @see OpCode#fload_0
         * @see OpCode#fload_1
         * @see OpCode#fload_2
         * @see OpCode#fload_3
         * @param index local variable index
         * @return builder
         */
        public Builder fload(int index) {
            localOp(OpCode.fload, index);
            return this;
        }

        /**
         * Sets opcode and operand on instruction for an {@code aload} variant.
         * @see OpCode#aload
         * @see OpCode#aload_0
         * @see OpCode#aload_1
         * @see OpCode#aload_2
         * @see OpCode#aload_3
         * @param index local variable index
         * @return builder
         */
        public Builder aload(int index) {
            localOp(OpCode.aload, index);
            return this;
        }

        private void localOp(OpCode baseOpCode, int index) {
            if (index < 4) { // use shorthand instruction if possible - i.e. istore_1, aload_3, etc.
                opCode = OpCode.valueOf(String.format("%s_%d", baseOpCode.name(), index));
            } else { // otherwise index has to be operand
                opCode = baseOpCode;
                operand(index);
            }
            localIndex = index;
        }

        /**
         * Creates new {@link Instruction} from the builder.
         * @return instruction
         */
        public Instruction build() {
            return new Instruction(this);
        }
    }
}

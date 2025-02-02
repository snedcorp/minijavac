package minijavac.gen.instruction;

import java.util.List;

/**
 * <pre>
 * Object representation of a branching instruction ({@link OpCode#_goto}, {@link OpCode#ifeq}, etc.).
 *
 * Branching instructions are typically added to a method without their byte offset (target location) set, and then once
 * more of the method has been processed and the desired destination offset is known, {@link #setBranchOffset(int)} is
 * called to update the instruction's operands accordingly. Note that the byte offset stored on the instruction is not
 * the actual offset within the method, but rather the delta between the branching instruction's offset and the
 * destination offset.
 *
 * Also, branching instructions require a copy of the operand stack from the {@link minijavac.gen.attribute.stackmap.StackMapTableAttribute StackMapTableAttribute}
 * when they are added to the method, to be used later to overwrite the operand stack at the target branch location.
 * </pre>
 */
public class BranchInstruction extends Instruction {

    /*
    * Copy of operand stack at branching location.
    * */
    private List<String> operandsStack;

    private BranchInstruction(OpCode opCode) {
        super(opCode);
    }

    public static BranchInstruction of(OpCode opCode) {
        return new BranchInstruction(opCode);
    }

    /*
    * Always return 3 for size, so code size remains accurate - even if offset operands haven't been set yet (they will)
    * */
    @Override
    public int getSize() {
        return 3;
    }

    /**
     * Sets the branching instruction's operands to the delta between its offset and the given offset.
     * @param destOffset destination byte offset within the method
     */
    public void setBranchOffset(int destOffset) {
        int branchOffset = destOffset - getOffset();
        setOperands(List.of((byte) (branchOffset >> 8), (byte) branchOffset));
    }

    public List<String> getOperandsStack() {
        return operandsStack;
    }

    public void setOperandsStack(List<String> operandsStack) {
        this.operandsStack = operandsStack;
    }
}

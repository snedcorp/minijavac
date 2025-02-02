package minijavac.gen.attribute;

import minijavac.ast.*;
import minijavac.gen.attribute.stackmap.StackMapTableAttribute;
import minijavac.gen.constant.ConstantPool;
import minijavac.gen.instruction.BranchInstruction;
import minijavac.gen.instruction.Instruction;
import minijavac.gen._byte.U2;
import minijavac.gen._byte.U4;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * <pre>
 * Object representation of the {@code Code_attribute} for a method, containing all the instructions generated from its
 * body.
 *
 * Contains:
 *   - A running byte offset indicating the location of the next instruction to be added.
 *   - A stack of all looping flow instructions ({@link BreakStmt},  {@link ContinueStmt}) seen within the method body,
 *     separated by their respective scopes.
 *   - A stack of "buffered" instructions that are waiting to be added until the operand stack is in a desired state.
 *
 * Also maintains a {@link StackMapTableAttribute} instance that keeps track of the current operand stack and local
 * variable table, and provides methods to request that a new {@link minijavac.gen.attribute.stackmap.StackMapFrame StackMapFrame}
 * be added to the attribute at specific byte offsets representing the destinations of branching instructions.
 *
 * Why, when setting a branching instruction's offset to be equal to the current offset, do we "seek" a new stack map
 * frame (by waiting for the next instruction to be added), instead of just requesting it right then and there? Either
 * way, it'll get requested for the same exact offset, so is there even a difference?
 *   - <b>Yes</b> there is, because, particularly in the case of nested conditional blocks or loops, by the time the
 *     next instruction gets added for that offset, the local variable scope may have changed, thereby changing the
 *     contents of the stack map frame.
 *   - See {@code stackmapframe_ifElseStmt_nestedIf} test case for an example:
 *     - Variable {@code y} is in scope when the offset for the nested if branch is set, but the instruction that gets
 *       added at that offset is a goto that exists <b>outside</b> the scope of the outer if block - meaning variable
 *       {@code y} is no longer in scope for that frame.
 *
 * Why are operands passed when requesting a new stack map frame?
 *   - Those operands represent the state of the operand stack when the branching instruction that branches to the
 *     current offset occurs, allowing the {@link StackMapTableAttribute} to overwrite its operand stack with its
 *     contents.
 *   - Not fully correct, kind of hacky (what about multiple branches to the same location?), but good enough for now.
 *
 * Note: even though the {@link StackMapTableAttribute} is only including in the resulting bytecode when a method
 * contains branching, it is still needed by this attribute in non-branching methods, in order to calculate the
 * {@code max_stack} and {@code max_locals} fields.
 * </pre>
 */
public class CodeAttribute extends Attribute {
    /*
    * Byte offset that the next instruction added will be located at.
    * */
    private int codeSize;

    /*
    * List of JVM instructions.
    * */
    private final List<Instruction> code = new ArrayList<>();

    private final StackMapTableAttribute stackMapTableAttribute;

    /*
     * Stack of instructions that have been created, but not yet added to the attribute.
     * Used in situations when the AST traversal pattern does not mesh perfectly with JVM instruction semantics.
     */
    private final Deque<Instruction> buffer = new ArrayDeque<>();

    /*
     * Flag to indicate a new stack map frame should be requested at the current offset, when the next instruction is added.
     */
    private boolean frameNeeded;

    /*
     * State of operand stack when the branching to the current offset occurs.
     */
    private List<String> frameOperands;

    /*
     * List of instructions corresponding to break and continue statements within a loop scope.
     */
    public record LoopFlowInstructions(List<BranchInstruction> breaks, List<BranchInstruction> continues) {}

    /*
     * Each level in stack corresponds to a new loop scope - that way, break and continue statements are linked to
     * only the innermost loop they are enclosed within.
     */
    private final Deque<LoopFlowInstructions> loopFlowStack = new ArrayDeque<>();


    private CodeAttribute(U2 attributeNameIndex, ConstantPool constantPool, MethodDecl methodDecl) {
        super(attributeNameIndex);
        this.stackMapTableAttribute = new StackMapTableAttribute(constantPool, methodDecl);
    }

    /**
     * Static factory for creating a new {@link CodeAttribute} for the given method.
     * @param constantPool constant pool for method's class
     * @param methodDecl method declaration
     * @return new {@link CodeAttribute}
     */
    public static CodeAttribute create(ConstantPool constantPool, MethodDecl methodDecl) {
        U2 nameIndex = constantPool.addUTFConstant("Code");
        return new CodeAttribute(nameIndex, constantPool, methodDecl);
    }

    /**
     * Adds the given instruction to the {@link CodeAttribute} and updates the instruction and attribute states
     * accordingly.
     * @param instruction
     * @return instruction
     */
    public Instruction addInstruction(Instruction instruction) {
        code.add(instruction);
        instruction.setOffset(codeSize); // set instruction's offset to current offset within method
        if (frameNeeded) { // add stack map frame for branching target at this position, if needed
            stackMapTableAttribute.addFrame(codeSize, frameOperands);
            frameNeeded = false;
        }

        codeSize += instruction.getSize(); // increase offset by instruction's size

        // add instruction to stack map table, to maintain simulated operand stack and locals table
        stackMapTableAttribute.addInstruction(instruction);

        return instruction;
    }

    /**
     * Sets the offset for all given branching instructions to the current offset within the method.
     * @param branchInstructions branching instructions
     */
    public void setBranchOffsets(List<BranchInstruction> branchInstructions) {
        setBranchOffsets(branchInstructions, codeSize);
    }

    /**
     * Sets the offset for all given branching instructions to the current offset within the method, and seeks a new
     * {@link minijavac.gen.attribute.stackmap.StackMapFrame StackMapFrame} at the current offset.
     * @param branchInstructions branching instructions
     */
    public void setBranchOffsetsAndSeekFrame(List<BranchInstruction> branchInstructions) {
        setBranchOffsets(branchInstructions, codeSize);
        seekFrame(null);
    }

    /**
     * Sets the offset for all given branching instructions to the given offset.
     * @param branchInstructions branching instructions
     * @param offset destination byte offset
     */
    public void setBranchOffsets(List<BranchInstruction> branchInstructions, int offset) {
        for (BranchInstruction branchInstruction : branchInstructions) {
            branchInstruction.setBranchOffset(offset);
        }
    }

    /**
     * Sets the offset for the given branch instruction to the given offset.
     * @param branchInstruction branching instruction
     * @param offset destination byte offset
     */
    public void setBranchOffset(BranchInstruction branchInstruction, int offset) {
        branchInstruction.setBranchOffset(offset);
    }

    /**
     * Sets the offset for the given branch instruction to the current offset within the method, and seeks a new {@link minijavac.gen.attribute.stackmap.StackMapFrame StackMapFrame}
     * at the current offset.
     * @param branchInstruction branching instruction
     */
    public void setBranchOffsetAndSeekFrame(BranchInstruction branchInstruction) {
        setBranchOffset(branchInstruction, codeSize);
        seekFrame(branchInstruction.getOperandsStack());
    }

    /**
     * Ensures that when the next instruction is added to the method, the creation of a new {@link minijavac.gen.attribute.stackmap.StackMapFrame StackMapFrame}
     * will be requested.
     * @param operands state of operand stack when the branching to the current location occurs
     */
    public void seekFrame(List<String> operands) {
        frameNeeded = true;
        frameOperands = operands;
    }

    /**
     * Adds {@link minijavac.gen.attribute.stackmap.StackMapFrame StackMapFrame} at the current offset.
     * <br><br>
     * To be used within loops, to add a frame for the offset where the loop condition will be located - i.e. the offset
     * that later branching instructions will be using as a target.
     * @return current offset
     */
    public int addFrame() {
        stackMapTableAttribute.addFrame(codeSize, null);
        return codeSize;
    }

    /**
     * Adds given declaration to the local variable table and initializes it, if necessary.
     * @param varDecl    variable declaration
     * @param initialize true if initializing expression is present
     */
    public void addVarDecl(VarDecl varDecl, boolean initialize) {
        // retrieve index and set on decl
        int localsCnt = stackMapTableAttribute.getLocalsCnt();
        varDecl.setLocalVarIndex(localsCnt);

        // add store instruction to initialize, if necessary
        if (initialize) {
            Instruction.Builder builder = Instruction.builder();
            switch (varDecl.type.kind) {
                case INT, BOOLEAN -> builder.istore(localsCnt);
                case FLOAT -> builder.fstore(localsCnt);
                default -> builder.astore(localsCnt);
            }
            Instruction instruction = builder.build();
            addInstruction(instruction);
        }

        // add to local variable table
        stackMapTableAttribute.addLocal(varDecl);
    }

    /**
     * Pushes instruction onto the instruction buffer, to prevent its addition to the attribute until the operand
     * stack is in the desired state.
     * @param instruction
     */
    public void pushBuffer(Instruction instruction) {
        this.buffer.push(instruction);
    }

    /**
     * Pops the most recently pushed instruction from the instruction buffer and adds it to the attribute.
     */
    public void popBuffer() {
        addInstruction(buffer.pop());
    }

    /**
     * @return most recently pushed instruction on the instruction buffer
     */
    public Instruction peekBuffer() {
        return buffer.peek();
    }

    /**
     * Records argument count of an imminent call, so the {@link StackMapTableAttribute} knows how many operands to pop
     * from the stack.
     * @param argCnt number of arguments in call
     */
    public void pushArgCnt(int argCnt) {
        stackMapTableAttribute.pushArgCnt(argCnt);
    }

    /**
     * <pre>
     * Pushes:
     *   - New local variable scope onto the locals stack in {@link StackMapTableAttribute}
     *   - New {@link LoopFlowInstructions} instance onto the loop flow stack, to track breaks and continues.
     * </pre>
     */
    public void enterLoopScope() {
        enterScope();
        loopFlowStack.push(new LoopFlowInstructions(new ArrayList<>(), new ArrayList<>()));
    }

    /**
     * Records {@link BranchInstruction} as a break statement for the current innermost enclosing loop.
     * @param branchInstruction {@link minijavac.gen.instruction.OpCode#_goto} for break statement
     */
    public void addBreak(BranchInstruction branchInstruction) {
        if (!loopFlowStack.isEmpty()) loopFlowStack.peek().breaks().add(branchInstruction);
    }

    /**
     * Records {@link BranchInstruction} as a continue statement for the current innermost enclosing loop.
     * @param branchInstruction {@link minijavac.gen.instruction.OpCode#_goto} for continue statement
     */
    public void addContinue(BranchInstruction branchInstruction) {
        if (!loopFlowStack.isEmpty()) loopFlowStack.peek().continues().add(branchInstruction);
    }

    /**
     * <pre>
     * Pops:
     *   - Current local variable scope from the locals stack in {@link StackMapTableAttribute}
     *   - {@link LoopFlowInstructions} (for the loop being exited) from the loop flow stack.
     * </pre>
     * @return loop flow instructions for loop being exited
     */
    public LoopFlowInstructions exitLoopScope() {
        exitScope();
        return loopFlowStack.pop();
    }

    /**
     * @return loop flow instructions for current innermost loop
     */
    public LoopFlowInstructions peekLoopFlow() {
        return loopFlowStack.peek();
    }

    /**
     * Pushes new local variable scope onto the locals stack in {@link StackMapTableAttribute}.
     */
    public void enterScope() {
        stackMapTableAttribute.pushScope();
    }

    /**
     * Pops current local variable scope from the locals stack in {@link StackMapTableAttribute}.
     */
    public void exitScope() {
        stackMapTableAttribute.popScope();
    }

    /**
     * Writes {@code Code_attribute} to the given byte stream.
     * <br><br>
     * Note: because the attribute's length must be included in the byte stream before its contents, and the length
     * isn't known until the contents have been written, a second {@link DataOutputStream} is initially used for writing
     * those contents. Only once the contents are written to the attribute byte stream and the size is determined,
     * can the given byte stream be written to.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        // create new byte stream for attribute
        try (ByteArrayOutputStream attrByteStream = new ByteArrayOutputStream() ;
             DataOutputStream attrStream = new DataOutputStream(attrByteStream)) {

            U2 maxStack = U2.of(stackMapTableAttribute.getMaxStackSize());
            maxStack.writeTo(attrStream); // write max_stack

            U2 maxLocals = U2.of(stackMapTableAttribute.getMaxLocals());
            maxLocals.writeTo(attrStream); // write max_locals

            // create new byte stream for instructions, so code_length can be written to first
            // is this necessary? since codeSize is stored
            try (ByteArrayOutputStream codeByteStream = new ByteArrayOutputStream() ;
                 DataOutputStream codeStream = new DataOutputStream(codeByteStream)) {
                for (Instruction instruction : code) {
                    instruction.writeTo(codeStream);
                }

                U4.of(codeByteStream.size()).writeTo(attrStream); // write code_length
                codeByteStream.writeTo(attrStream);               // write code
            }

            U2.of(0).writeTo(attrStream);                    // write exception_table_length (not implemented yet)

            if (stackMapTableAttribute.hasFrames()) {
                U2.of(1).writeTo(attrStream);                // write attributes_count
                stackMapTableAttribute.writeTo(attrStream);      // write stack map table
            } else {
                U2.of(0).writeTo(attrStream);                // write attributes_count
            }

            setAttributeLength(U4.of(attrByteStream.size()));    // set attribute length to attr byte stream length
            super.writeTo(stream);                               // write attribute_name_index, attribute_length

            attrByteStream.writeTo(stream);                      // write attribute contents
        }
    }

    public int getOffset() {
        return codeSize;
    }

    public List<Instruction> getCode() {
        return code;
    }

    public Instruction getLastInstruction() {
        return code.get(code.size() - 1);
    }
}

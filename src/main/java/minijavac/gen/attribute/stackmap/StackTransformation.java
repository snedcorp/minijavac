package minijavac.gen.attribute.stackmap;

import java.util.Set;

/**
 * <pre>
 * Enumeration of the types of operand stack transformations that can occur from executing instructions, to be used by
 * the {@link StackMapTableAttribute}.
 *
 * The enclosed {@link StackPop} denotes how many operands will be popped off the stack to execute the instruction, with
 * the variants' names describing the contents of what then gets pushed onto the stack as a result.
 *
 * Note:
 *   - The {@link StackMapTableAttribute} is only concerned with the types of the items on the operand stack and the
 *     delta, if any, between those types before and after executing an instruction. As such, there are many
 *     instructions that actually do operate on the stack but don't ultimately change its size or types, so they get
 *     classified with the catch-all {@link #NOP} transformation. For these types of instructions ({@link minijavac.gen.instruction.OpCode#ineg ineg}, {@link minijavac.gen.instruction.OpCode#iinc iinc}, etc.),
 *     from the perspective of the {@link StackMapTableAttribute}, nothing has changed.
 * </pre>
 */
public enum StackTransformation {

    IPUSH,
    FPUSH,
    NULL,
    LOAD_0,
    LOAD_1,
    LOAD_2,
    LOAD_3,
    LOAD,
    IALOAD(StackPop.TWO),
    FALOAD(StackPop.TWO),
    AALOAD(StackPop.TWO),
    POP(StackPop.ONE),
    POP_2(StackPop.TWO),
    POP_3(StackPop.THREE),
    INVOKE_STATIC(StackPop.DYNAMIC),
    INVOKE(StackPop.DYNAMIC),
    DUP,
    DUP2,
    DUP_X1,
    DUP_X2,
    I_BINOP(StackPop.TWO),
    F_BINOP(StackPop.TWO),
    NOP,
    LOAD_STATIC,
    LOAD_FIELD(StackPop.ONE),
    NEW,
    NEW_ARR(StackPop.ONE),
    NEW_OBJ_ARR(StackPop.ONE),
    NEW_MULT_ARR(StackPop.DYNAMIC),
    ARR_LEN(StackPop.ONE);

    StackTransformation() {
        this.pop = StackPop.ZERO;
    }

    StackTransformation(StackPop pop) {
        this.pop = pop;
    }

    private final StackPop pop;
    private static final Set<StackTransformation> noPush;

    public StackPop getPop() {
        return pop;
    }

    static {
        noPush = Set.of(POP, POP_2, POP_3, NOP);
    }

    /**
     * @return true if the transformation pushes a result to the operand stack
     */
    public boolean hasPush() {
        return !noPush.contains(this);
    }
}

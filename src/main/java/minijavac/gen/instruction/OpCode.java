package minijavac.gen.instruction;

import minijavac.gen.attribute.stackmap.StackTransformation;

import static minijavac.gen.attribute.stackmap.StackTransformation.*;

/**
 * Enumeration of all supported JVM instruction opcodes.
 * <br><br>
 * Each variant contains a one-byte opcode recognized by the JVM as a specific instruction, as well as a
 * {@link StackTransformation} variant that details how the operand stack will be affected by executing that instruction
 * (needed by the {@link minijavac.gen.attribute.stackmap.StackMapTableAttribute StackMapTableAttribute}).
 */
public enum OpCode {
    /**
     * <pre>
     * Push {@code null} onto stack.
     *
     * Stack:
     *   ... -> ..., {@code null}
     * </pre>
     */
    aconst_null(1, NULL),

    /**
     * <pre>
     * Push -1 onto stack.
     *
     * Stack:
     *   ... -> ..., -1
     * </pre>
     */
    iconst_m1(2, IPUSH),

    /**
     * <pre>
     * Push 0 onto stack.
     *
     * Stack:
     *   ... -> ..., 0
     * </pre>
     */
    iconst_0(3, IPUSH),

    /**
     * <pre>
     * Push 1 onto stack.
     *
     * Stack:
     *   ... -> ..., 1
     * </pre>
     */
    iconst_1(4, IPUSH),

    /**
     * <pre>
     * Push 2 onto stack.
     *
     * Stack:
     *   ... -> ..., 2
     * </pre>
     */
    iconst_2(5, IPUSH),

    /**
     * <pre>
     * Push 3 onto stack.
     *
     * Stack:
     *   ... -> ..., 3
     * </pre>
     */
    iconst_3(6, IPUSH),

    /**
     * <pre>
     * Push 4 onto stack.
     *
     * Stack:
     *   ... -> ..., 4
     * </pre>
     */
    iconst_4(7, IPUSH),

    /**
     * <pre>
     * Push 5 onto stack.
     *
     * Stack:
     *   ... -> ..., 5
     * </pre>
     */
    iconst_5(8, IPUSH),

    /**
     * <pre>
     * Push 0.0 onto stack.
     *
     * Stack:
     *   ... -> ..., 0.0
     * </pre>
     */
    fconst_0(11, FPUSH),

    /**
     * <pre>
     * Push 1.0 onto stack.
     *
     * Stack:
     *   ... -> ..., 1.0
     * </pre>
     */
    fconst_1(12, FPUSH),

    /**
     * <pre>
     * Push 2.0 onto stack.
     *
     * Stack:
     *   ... -> ..., 2.0
     * </pre>
     */
    fconst_2(13, FPUSH),

    /**
     * <pre>
     * Push {@code byte} onto stack (sign-extended to {@code int}).
     *
     * Operands:
     *   - byte
     *
     * Stack:
     *   ... -> ..., value
     * </pre>
     */
    bipush(16, IPUSH),

    /**
     * <pre>
     * Push {@code short} ({@code byte1 << 8 || byte2} ) onto stack (sign-extended to {@code int}).
     *
     * Operands:
     *   - byte1
     *   - byte2
     *
     * Stack:
     *   ... -> ..., value
     * </pre>
     */
    sipush(17, IPUSH),

    /**
     * <pre>
     * Push item from constant pool onto stack.
     *
     * Operands:
     *   - index
     *
     * Stack:
     *   ... -> ..., value
     * </pre>
     */
    ldc(18, IPUSH),

    /**
     * <pre>
     * Push value of local {@code int} variable at given index onto stack.
     *
     * Operands:
     *   - index
     *
     * Stack:
     *   ... -> ..., value
     * </pre>
     */
    iload(21, IPUSH),

    /**
     * <pre>
     * Push value of local {@code float} variable at given index onto stack.
     *
     * Operands:
     *   - index
     *
     * Stack:
     *   ... -> ..., value
     * </pre>
     */
    fload(23, FPUSH),

    /**
     * <pre>
     * Push reference of local object variable at given index onto stack.
     *
     * Operands:
     *   - index
     *
     * Stack:
     *   ... -> ..., objectref
     * </pre>
     */
    aload(25, LOAD),

    /**
     * <pre>
     * Push value of local {@code int} variable at index 0 onto stack.
     *
     * Stack:
     *   ... -> ..., value
     * </pre>
     */
    iload_0(26, IPUSH),

    /**
     * <pre>
     * Push value of local {@code int} variable at index 1 onto stack.
     *
     * Stack:
     *   ... -> ..., value
     * </pre>
     */
    iload_1(27, IPUSH),

    /**
     * <pre>
     * Push value of local {@code int} variable at index 2 onto stack.
     *
     * Stack:
     *   ... -> ..., value
     * </pre>
     */
    iload_2(28, IPUSH),

    /**
     * <pre>
     * Push value of local {@code int} variable at index 3 onto stack.
     *
     * Stack:
     *   ... -> ..., value
     * </pre>
     */
    iload_3(29, IPUSH),

    /**
     * <pre>
     * Push value of local {@code float} variable at index 0 onto stack.
     *
     * Stack:
     *   ... -> ..., value
     * </pre>
     */
    fload_0(34, FPUSH),

    /**
     * <pre>
     * Push value of local {@code float} variable at index 1 onto stack.
     *
     * Stack:
     *   ... -> ..., value
     * </pre>
     */
    fload_1(35, FPUSH),

    /**
     * <pre>
     * Push value of local {@code float} variable at index 2 onto stack.
     *
     * Stack:
     *   ... -> ..., value
     * </pre>
     */
    fload_2(36, FPUSH),

    /**
     * <pre>
     * Push value of local {@code float} variable at index 3 onto stack.
     *
     * Stack:
     *   ... -> ..., value
     * </pre>
     */
    fload_3(37, FPUSH),

    /**
     * <pre>
     * Push reference of local object variable at index 0 onto stack.
     *
     * Stack:
     *   ... -> ..., objectref
     * </pre>
     */
    aload_0(42, LOAD_0),

    /**
     * <pre>
     * Push reference of local object variable at index 1 onto stack.
     *
     * Stack:
     *   ... -> ..., objectref
     * </pre>
     */
    aload_1(43, LOAD_1),

    /**
     * <pre>
     * Push reference of local object variable at index 2 onto stack.
     *
     * Stack:
     *   ... -> ..., objectref
     * </pre>
     */
    aload_2(44, LOAD_2),

    /**
     * <pre>
     * Push reference of local object variable at index 3 onto stack.
     *
     * Stack:
     *   ... -> ..., objectref
     * </pre>
     */
    aload_3(45, LOAD_3),

    /**
     * <pre>
     * Push {@code int} stored in array onto stack.
     *
     * Stack:
     *   ..., arrayref, index -> ..., value
     * </pre>
     */
    iaload(46, IALOAD),

    /**
     * <pre>
     * Push {@code float} stored in array onto stack.
     *
     * Stack:
     *   ..., arrayref, index -> ..., value
     * </pre>
     */
    faload(48, FALOAD),

    /**
     * <pre>
     * Push object reference stored in array onto stack.
     *
     * Stack:
     *   ..., arrayref, index -> ..., value
     * </pre>
     */
    aaload(50, AALOAD),

    /**
     * <pre>
     * Push {@code byte} (sign-extended from {@code int}) from boolean array onto stack.
     *
     * Stack:
     *   ..., arrayref, index -> ..., value
     * </pre>
     */
    baload(51, IALOAD),

    /**
     * <pre>
     * Pop {@code int} from stack and store in local {@code int} variable at given index.
     *
     * Operands:
     *   - index
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    istore(54, POP),

    /**
     * <pre>
     * Pop {@code float} from stack and store in local {@code float} variable at given index.
     *
     * Operands:
     *   - index
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    fstore(56, POP),

    /**
     * <pre>
     * Pop object reference from stack and store in local object variable at given index.
     *
     * Operands:
     *   - index
     *
     * Stack:
     *   ..., objectref -> ...
     * </pre>
     */
    astore(58, POP),

    /**
     * <pre>
     * Pop {@code int} from stack and store in local {@code int} variable at index 0.
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    istore_0(59, POP),

    /**
     * <pre>
     * Pop {@code int} from stack and store in local {@code int} variable at index 1.
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    istore_1(60, POP),

    /**
     * <pre>
     * Pop {@code int} from stack and store in local {@code int} variable at index 2.
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    istore_2(61, POP),

    /**
     * <pre>
     * Pop {@code int} from stack and store in local {@code int} variable at index 3.
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    istore_3(62, POP),

    /**
     * <pre>
     * Pop {@code float} from stack and store in local {@code float} variable at index 0.
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    fstore_0(67, POP),

    /**
     * <pre>
     * Pop {@code float} from stack and store in local {@code float} variable at index 1.
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    fstore_1(68, POP),

    /**
     * <pre>
     * Pop {@code float} from stack and store in local {@code float} variable at index 2.
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    fstore_2(69, POP),

    /**
     * <pre>
     * Pop {@code float} from stack and store in local {@code float} variable at index 3.
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    fstore_3(70, POP),

    /**
     * <pre>
     * Pop object reference from stack and store in local object variable at index 0.
     *
     * Stack:
     *   ..., objectref -> ...
     * </pre>
     */
    astore_0(75, POP),

    /**
     * <pre>
     * Pop object reference from stack and store in local object variable at index 1.
     *
     * Stack:
     *   ..., objectref -> ...
     * </pre>
     */
    astore_1(76, POP),

    /**
     * <pre>
     * Pop object reference from stack and store in local object variable at index 2.
     *
     * Stack:
     *   ..., objectref -> ...
     * </pre>
     */
    astore_2(77, POP),

    /**
     * <pre>
     * Pop object reference from stack and store in local object variable at index 3.
     *
     * Stack:
     *   ..., objectref -> ...
     * </pre>
     */
    astore_3(78, POP),

    /**
     * <pre>
     * Store value at given index in {@code int} array.
     *
     * Stack:
     *   ..., arrayref, index, value -> ...
     * </pre>
     */
    iastore(79, POP_3),

    /**
     * <pre>
     * Store value at given index in {@code float} array.
     *
     * Stack:
     *   ..., arrayref, index, value -> ...
     * </pre>
     */
    fastore(81, POP_3),

    /**
     * <pre>
     * Store value at given index in object array.
     *
     * Stack:
     *   ..., arrayref, index, value -> ...
     * </pre>
     */
    aastore(83, POP_3),

    /**
     * <pre>
     * Store value at given index in boolean array.
     *
     * Stack:
     *   ..., arrayref, index, value -> ...
     * </pre>
     */
    bastore(84, POP_3),

    /**
     * <pre>
     * Pop the top item from the stack.
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    pop(87, POP),

    /**
     * <pre>
     * Duplicate the top item in the stack.
     *
     * Stack:
     *   ..., value -> ..., value, value
     * </pre>
     */
    dup(89, DUP),

    /**
     * <pre>
     * Duplicate the top item in the stack and insert it two items down.
     *
     * Stack:
     *   ..., val2, val1 -> ..., val1, val2, val1
     * </pre>
     */
    dup_x1(90, DUP_X1),

    /**
     * <pre>
     * Duplicate the top item in the stack and insert it three items down.
     *
     * Stack:
     *   ..., val3, val2, val1 -> ..., val1, val3, val2, val1
     * </pre>
     */
    dup_x2(91, DUP_X2),

    /**
     * <pre>
     * Duplicate the top two items in the stack.
     *
     * Stack:
     *   ..., val2, val1 -> ..., val2, val1, val2, val1
     * </pre>
     */
    dup2(92, DUP2),

    /**
     * <pre>
     * Add two ints ({@code val1 + val2} ).
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    iadd(96, I_BINOP),

    /**
     * <pre>
     * Add two floats ({@code val1 + val2} ).
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    fadd(98, F_BINOP),

    /**
     * <pre>
     * Subtract two ints ({@code val1 - val2} ).
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    isub(100, I_BINOP),

    /**
     * <pre>
     * Subtract two floats ({@code val1 - val2} ).
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    fsub(102, F_BINOP),

    /**
     * <pre>
     * Multiply two ints ({@code val1 * val2} ).
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    imul(104, I_BINOP),

    /**
     * <pre>
     * Multiply two floats ({@code val1 * val2} ).
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    fmul(106, F_BINOP),

    /**
     * <pre>
     * Divide two ints ({@code val1 / val2} ).
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    idiv(108, I_BINOP),

    /**
     * <pre>
     * Divide two floats ({@code val1 / val2} ).
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    fdiv(110, F_BINOP),

    /**
     * <pre>
     * Modulo two ints ({@code val1 % val2} ).
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    irem(112, I_BINOP),

    /**
     * <pre>
     * Modulo two floats ({@code val1 % val2} ).
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    frem(114, F_BINOP),

    /**
     * <pre>
     * Negate int ({@code -value} ).
     *
     * Stack:
     *   ..., value -> ..., res
     * </pre>
     */
    ineg(116, NOP),

    /**
     * <pre>
     * Negate float ({@code -value} ).
     *
     * Stack:
     *   ..., value -> ..., res
     * </pre>
     */
    fneg(118, NOP),

    /**
     * <pre>
     * Shift left int ({@code val1 << val2} ).
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    ishl(120, I_BINOP),

    /**
     * <pre>
     * Shift right int ({@code val1 >> val2} ).
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    ishr(122, I_BINOP),

    /**
     * <pre>
     * Unsigned shift right int ({@code val1 >>> val2} ).
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    iushr(124, I_BINOP),

    /**
     * <pre>
     * Bitwise AND int ({@code val1 & val2} ).
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    iand(126, I_BINOP),

    /**
     * <pre>
     * Bitwise OR int ({@code val1 | val2} ).
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    ior(128, I_BINOP),

    /**
     * <pre>
     * Bitwise XOR int ({@code val1 ^ val2} ).
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    ixor(130, I_BINOP),

    /**
     * <pre>
     * Increment local variable at given index by constant.
     *
     * Operands:
     *   - index
     *   - const
     *
     * </pre>
     */
    iinc(132, NOP),

    /**
     * <pre>
     * Convert {@code int} to {@code float}.
     *
     * Stack:
     *   ..., value -> ..., res
     * </pre>
     */
    i2f(134, NOP),

    /**
     * <pre>
     * Compare two floats:
     *   - {@code val1 > val2} -> 1
     *   - {@code val1 == val2} -> 0
     *   - {@code val1 < val2} -> -1
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    fcmpl(149, I_BINOP),

    /**
     * <pre>
     * Compare two floats:
     *   - {@code val1 > val2} -> 1
     *   - {@code val1 == val2} -> 0
     *   - {@code val1 < val2} -> -1
     *
     * Stack:
     *   ..., val1, val2 -> ..., res
     * </pre>
     */
    fcmpg(150, I_BINOP),

    /**
     * <pre>
     * If {@code value == 0}, jump to offset.
     *
     * Operands:
     *   - branchbyte1
     *   - branchbyte2
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    ifeq(153, POP),

    /**
     * <pre>
     * If {@code value != 0}, jump to offset.
     *
     * Operands:
     *   - branchbyte1
     *   - branchbyte2
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    ifne(154, POP),

    /**
     * <pre>
     * If {@code value < 0}, jump to offset.
     *
     * Operands:
     *   - branchbyte1
     *   - branchbyte2
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    iflt(155, POP),

    /**
     * <pre>
     * If {@code value >= 0}, jump to offset.
     *
     * Operands:
     *   - branchbyte1
     *   - branchbyte2
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    ifge(156, POP),

    /**
     * <pre>
     * If {@code value > 0}, jump to offset.
     *
     * Operands:
     *   - branchbyte1
     *   - branchbyte2
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    ifgt(157, POP),

    /**
     * <pre>
     * If {@code value <= 0}, jump to offset.
     *
     * Operands:
     *   - branchbyte1
     *   - branchbyte2
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    ifle(158, POP),

    /**
     * <pre>
     * If {@code val1 == val2}, jump to offset.
     *
     * Operands:
     *   - branchbyte1
     *   - branchbyte2
     *
     * Stack:
     *   ..., val1, val2 -> ...
     * </pre>
     */
    if_icmpeq(159, POP_2),

    /**
     * <pre>
     * If {@code val1 != val2}, jump to offset.
     *
     * Operands:
     *   - branchbyte1
     *   - branchbyte2
     *
     * Stack:
     *   ..., val1, val2 -> ...
     * </pre>
     */
    if_icmpne(160, POP_2),

    /**
     * <pre>
     * If {@code val1 < val2}, jump to offset.
     *
     * Operands:
     *   - branchbyte1
     *   - branchbyte2
     *
     * Stack:
     *   ..., val1, val2 -> ...
     * </pre>
     */
    if_icmplt(161, POP_2),

    /**
     * <pre>
     * If {@code val1 >= val2}, jump to offset.
     *
     * Operands:
     *   - branchbyte1
     *   - branchbyte2
     *
     * Stack:
     *   ..., val1, val2 -> ...
     * </pre>
     */
    if_icmpge(162, POP_2),

    /**
     * <pre>
     * If {@code val1 > val2}, jump to offset.
     *
     * Operands:
     *   - branchbyte1
     *   - branchbyte2
     *
     * Stack:
     *   ..., val1, val2 -> ...
     * </pre>
     */
    if_icmpgt(163, POP_2),

    /**
     * <pre>
     * If {@code val1 <= val2}, jump to offset.
     *
     * Operands:
     *   - branchbyte1
     *   - branchbyte2
     *
     * Stack:
     *   ..., val1, val2 -> ...
     * </pre>
     */
    if_icmple(164, POP_2),

    /**
     * <pre>
     * Jump to offset.
     *
     * Operands:
     *   - branchbyte1
     *   - branchbyte2
     * </pre>
     */
    _goto(167, NOP),

    /**
     * <pre>
     * Return int from method.
     *
     * Stack:
     *   ..., value ->
     * </pre>
     */
    ireturn(172, POP),

    /**
     * <pre>
     * Return float from method.
     *
     * Stack:
     *   ..., value ->
     * </pre>
     */
    freturn(174, POP),

    /**
     * <pre>
     * Return object from method.
     *
     * Stack:
     *   ..., value ->
     * </pre>
     */
    areturn(176, POP),

    /**
     * <pre>
     * Return void from method.
     *
     * Stack:
     *   ..., value ->
     * </pre>
     */
    _return(177, NOP),

    /**
     * <pre>
     * Push value of static field onto stack.
     *
     * Operands:
     *   - indexbyte1
     *   - indexbyte2
     *
     * Stack:
     *   ... -> ..., value
     * </pre>
     */
    getstatic(178, LOAD_STATIC),

    /**
     * <pre>
     * Set value of static field.
     *
     * Operands:
     *   - indexbyte1
     *   - indexbyte2
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    putstatic(179, POP),

    /**
     * <pre>
     * Push value of instance field onto stack.
     *
     * Operands:
     *   - indexbyte1
     *   - indexbyte2
     *
     * Stack:
     *   ..., objectref -> ..., value
     * </pre>
     */
    getfield(180, LOAD_FIELD),

    /**
     * <pre>
     * Set value of instance field.
     *
     * Operands:
     *   - indexbyte1
     *   - indexbyte2
     *
     * Stack:
     *   ..., objectref, value -> ...
     * </pre>
     */
    putfield(181, POP_2),

    /**
     * <pre>
     * Invoke instance method.
     *
     * Operands:
     *   - indexbyte1
     *   - indexbyte2
     *
     * Stack:
     *   ..., objectref, [arg1 [, arg2 ...]] ->
     * </pre>
     */
    invokevirtual(182, INVOKE),

    /**
     * <pre>
     * Invoke instance initialization method.
     *
     * Operands:
     *   - indexbyte1
     *   - indexbyte2
     *
     * Stack:
     *   ..., objectref, [arg1 [, arg2 ...]] ->
     * </pre>
     */
    invokespecial(183, INVOKE),

    /**
     * <pre>
     * Invoke static method.
     *
     * Operands:
     *   - indexbyte1
     *   - indexbyte2
     *
     * Stack:
     *   ..., [arg1 [, arg2 ...]] ->
     * </pre>
     */
    invokestatic(184, INVOKE_STATIC),

    /**
     * <pre>
     * Create new object.
     *
     * Operands:
     *   - indexbyte1
     *   - indexbyte2
     *
     * Stack:
     *   ... -> ..., objectref
     * </pre>
     */
    _new(187, NEW),

    /**
     * <pre>
     * Create new array.
     *
     * Operands:
     *   - atype ({@link ArrType})
     *
     * Stack:
     *   ..., count -> ..., arrayref
     * </pre>
     */
    newarray(188, NEW_ARR),

    /**
     * <pre>
     * Create new object array.
     *
     * Operands:
     *   - indexbyte1
     *   - indexbyte2
     *
     * Stack:
     *   ..., count -> ..., arrayref
     * </pre>
     */
    anewarray(189, NEW_OBJ_ARR),

    /**
     * <pre>
     * Get length of array.
     *
     * Stack:
     *   ..., arrayref -> ..., length
     * </pre>
     */
    arraylength(190, ARR_LEN),

    /**
     * <pre>
     * Create new multidimensional array.
     *
     * Operands:
     *   - indexbyte1
     *   - indexbyte2
     *   - dimensions
     *
     * Stack:
     *   ..., count1 [, count2, ...] -> ..., arrayref
     * </pre>
     */
    multianewarray(197, NEW_MULT_ARR),

    /**
     * <pre>
     * If {@code value == null}, jump to offset.
     *
     * Operands:
     *   - branchbyte1
     *   - branchbyte2
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    ifnull(198, POP),

    /**
     * <pre>
     * If {@code value != null}, jump to offset.
     *
     * Operands:
     *   - branchbyte1
     *   - branchbyte2
     *
     * Stack:
     *   ..., value -> ...
     * </pre>
     */
    ifnonnull(199, POP);

    OpCode(int code, StackTransformation transformation) {
        this.code = code;
        this.transformation = transformation;
    }

    private final int code;
    private final StackTransformation transformation;

    public int getCode() {
        return code;
    }

    public StackTransformation getTransformation() {
        return transformation;
    }
}

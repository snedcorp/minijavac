package minijavac.gen.attribute.stackmap;

import minijavac.ast.*;
import minijavac.gen._byte.U2;
import minijavac.gen._byte.U4;
import minijavac.gen.attribute.Attribute;
import minijavac.gen.constant.ConstantPool;
import minijavac.gen.instruction.ArrType;
import minijavac.gen.instruction.BranchInstruction;
import minijavac.gen.instruction.Instruction;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * Object representation of the {@code StackMapTable_attribute} used by the JVM to perform verification by type
 * checking.
 *
 * To verify bytecode correctness, the JVM requires the inclusion of metadata about the types of the items
 * on the operand stack and in the local variable table, at every position within the bytecode that is the target
 * of a branching instruction (a jump). This metadata is stored within frames ({@link StackMapFrame}), with each frame
 * corresponding to a specific byte offset. These frames are written in sorted, ascending byte offset order and to save
 * space, can typically omit some type information by relying on the contents of the previous frame.
 *
 * In order to implement this functionality, a simulated operand stack and scoped local variable table is maintained
 * by this class, so that if a frame is requested for a certain byte offset, the correct operands and local variables
 * can be stored in the frame.
 *
 * Note: to avoid creating unnecessary objects, instead of storing the full {@link VariableInfo} verification type objects on
 * the operand stack and local variable table, an intermediate string representation of the types is used. Once a
 * {@link StackMapFrame} is requested for a particular offset, those string representations are then "resolved" into
 * their respective {@link VariableInfo} objects and placed into the newly created frame.
 *
 * Additionally, the maximum size seen for both the operand stack and the local variable table is tracked in this class,
 * for use by the {@link minijavac.gen.attribute.CodeAttribute CodeAttribute} when writing its bytecode.
 * </pre>
 */
public class StackMapTableAttribute extends Attribute {

    /*
     * Scoped local variable table.
     */
    private final List<List<String>> localsStack;

    /*
     * Simulated operand stack.
     */
    private List<String> operandStack;

    /*
    * Number of parameters in method (including implicit "this" in instance methods).
    * */
    private int paramsCnt;

    /*
    * Number of local variables within the method (not including parameters).
    * */
    private int localsCnt;

    /*
    * List of frames, in ascending byte offset order.
    * */
    private final List<StackMapFrame> frames;

    private final ConstantPool constantPool;

    /*
    * Stack of argument counts for imminent calls (needed b/c a call can have calls in its arguments, etc. etc.)
    * */
    private final Deque<Integer> argCnts;

    /*
     * Maximum number of items that will appear on the operand stack at any given time when executing the method.
     */
    private int maxStackSize;

    /*
     * Maximum number of local variables (including parameters) that will exist in scope at any given time when
     * executing the method.
     */
    private int maxLocals;

    /*
    * Regex patterns used for extracting types from the operand stack.
    * */
    private static final Pattern OBJ_ARR_REGEX = Pattern.compile("\\[L([\\w/]+);");
    private static final Pattern OBJ_REGEX = Pattern.compile("L([\\w/]+);");
    private static final Pattern METHOD_RETURN_REGEX = Pattern.compile("\\(.*\\)(\\[*[\\w/]+;?)");

    /*
     * Intermediate string representations of basic verification types (could be anything, as long as it doesn't
     * conflict w/ the object and descriptor notation)
     */
    private static final String INT_VAR = "%I";
    private static final String FLOAT_VAR = "%F";
    private static final String NULL_VAR = "%N";

    public StackMapTableAttribute(ConstantPool constantPool, MethodDecl methodDecl) {
        super();
        localsStack = new ArrayList<>();
        frames = new ArrayList<>();
        this.constantPool = constantPool;
        argCnts = new ArrayDeque<>();
        operandStack = new ArrayList<>();

        initLocalsStack(methodDecl);
    }

    /**
     * Initializes the locals stack for the given method.
     * @param methodDecl method
     */
    private void initLocalsStack(MethodDecl methodDecl) {
        // add scope for parameters
        pushScope();

        // if instance method, push implicit "this" ref as first local
        if (!methodDecl.isStatic) {
            pushLocal(methodDecl.classDecl.id.contents);
            paramsCnt++;
        }

        // each param is pushed as local
        for (ParameterDecl param : methodDecl.parameterDeclList) {
            addLocal(param);
            param.setLocalVarIndex(paramsCnt); // index is set on AST node
            paramsCnt++;
        }

        // add top-level scope for method body
        pushScope();
    }

    /**
     * Updates the simulated operand stack to reflect the effects of the given instruction.
     * @param instruction
     */
    public void addInstruction(Instruction instruction) {
        StackTransformation transformation = instruction.getOpCode().getTransformation();

        List<String> popped = new ArrayList<>();
        int popAmount = getPopAmount(transformation);

        // pop items from the stack, as required by the instruction
        for (int i=0; i<popAmount; i++) {
            popped.add(operandStack.remove(operandStack.size()-1));
        }

        // store a copy of the current operand stack to the branch instruction, to be used later when adding a frame
        if (!operandStack.isEmpty() && instruction instanceof BranchInstruction branchInstruction) {
            branchInstruction.setOperandsStack(new ArrayList<>(operandStack));
        }

        if (!transformation.hasPush()) return;

        // if the instruction pushes a result to the operand stack, determine that result's type
        String operand = switch (transformation) {
            case IPUSH, IALOAD, I_BINOP, ARR_LEN -> INT_VAR;
            case FPUSH, FALOAD, F_BINOP -> FLOAT_VAR;
            case NULL -> NULL_VAR;
            case LOAD_0 -> getNthLocal(0);
            case LOAD_1 -> getNthLocal(1);
            case LOAD_2 -> getNthLocal(2);
            case LOAD_3 -> getNthLocal(3);
            case LOAD -> getNthLocal(instruction.getByteOperand());
            case AALOAD -> {
                String arrDescriptor = popped.get(1);
                Matcher matcher = OBJ_ARR_REGEX.matcher(arrDescriptor);
                if (matcher.matches()) {
                    yield matcher.group(1);
                }
                yield arrDescriptor.substring(1);
            }
            case DUP, DUP2, DUP_X1, DUP_X2 -> operandStack.get(operandStack.size()-1);
            case LOAD_STATIC, LOAD_FIELD -> {
                String fieldDescriptor = constantPool.getRefDescriptor(instruction.getConstantIndex());
                if (fieldDescriptor.equals("I") || fieldDescriptor.equals("Z")) yield INT_VAR;
                if (fieldDescriptor.equals("F")) yield FLOAT_VAR;
                yield stripLAndColonIfPresent(fieldDescriptor);
            }
            case NEW -> {
                String classDescriptor = constantPool.getClassConstant(instruction.getConstantIndex());
                yield stripLAndColonIfPresent(classDescriptor);
            }
            case NEW_ARR -> {
                int aType = instruction.getByteOperand();
                yield "[" + BaseType.descriptor(ArrType.getKindFromCode(aType));
            }
            case NEW_OBJ_ARR -> {
                String classDescriptor = constantPool.getClassConstant(instruction.getConstantIndex());
                yield String.format("[L%s;", classDescriptor);
            }
            case NEW_MULT_ARR -> constantPool.getClassConstant(instruction.getConstantIndex());
            case INVOKE_STATIC, INVOKE -> {
                String methodDescriptor = constantPool.getRefDescriptor(instruction.getConstantIndex());
                Matcher matcher = METHOD_RETURN_REGEX.matcher(methodDescriptor);
                matcher.matches();
                String returnDescriptor = matcher.group(1);
                if (returnDescriptor.equals("V")) yield null;
                if (returnDescriptor.equals("I") || returnDescriptor.equals("Z")) yield INT_VAR;
                if (returnDescriptor.equals("F")) yield FLOAT_VAR;
                if (returnDescriptor.startsWith("[")) yield returnDescriptor;
                yield stripLAndColonIfPresent(returnDescriptor);
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + transformation);
        };

        if (operand != null) {
            // Add result operand at the correct position on the operand stack
            switch (transformation) {
                case DUP_X2:
                    operandStack.add(operandStack.size()-3, operand);
                    break;
                case DUP_X1:
                    operandStack.add(operandStack.size()-2, operand);
                    break;
                case DUP2:
                    operandStack.add(operandStack.get(operandStack.size()-2));
                default:
                    operandStack.add(operand);
            }
        }

        // update max stack size if exceeded
        int currSize = operandStack.size();
        if (currSize > maxStackSize) {
            maxStackSize = currSize;
        }
    }

    /**
     * @param transformation stack transformation
     * @return number of items to pop from the operand stack
     */
    private int getPopAmount(StackTransformation transformation) {
        StackPop pop = transformation.getPop();
        int popAmount;

        if (pop == StackPop.DYNAMIC) {
            /*
             * If pop amount can vary (i.e. method invocation or multidimensional array declaration), pop most recently
             * pushed argument count to get actual value.
             * */
            popAmount = argCnts.pop();
            // Increment pop amount by one if invokevirtual or invokespecial, to account for leading "this" ref
            if (transformation == StackTransformation.INVOKE) popAmount++;
        } else {
            popAmount = pop.getAmount();
        }
        return popAmount;
    }

    /**
     * Strips the enclosing "L" and ";" from a descriptor if it's in the "L{class_name};" format
     * @param descriptor type descriptor
     * @return stripped descriptor
     */
    private String stripLAndColonIfPresent(String descriptor) {
        Matcher matcher = OBJ_REGEX.matcher(descriptor);
        if (matcher.matches()) return matcher.group(1);
        return descriptor;
    }

    /**
     * Adds a {@link StackMapFrame} to this attribute for the given offset.
     * @param currOffset current byte offset within method
     * @param operands   types on the operand stack when branching to this location, if any
     */
    public void addFrame(int currOffset, List<String> operands) {
        if (frames.isEmpty()) {
            setAttributeNameIndex(constantPool.addUTFConstant("StackMapTable"));
        }

        /*
         * Short-circuit if attempting to add a frame at the same offset as the last added frame.
         * Occurs when a location is being branched to from different locations (like the end of a nested if)
         */
        if (!frames.isEmpty() && frames.get(frames.size()-1).getOffset() == currOffset) return;

        int localsDelta = localsCnt - (frames.isEmpty() ? 0 : frames.get(frames.size()-1).getLocalsCnt());

        StackMapFrame frame = new StackMapFrame();
        frame.setOffset(currOffset);
        frame.setLocalsCnt(localsCnt);

        /*
        * If first frame (after initial implicit frame), then offset_delta = current offset
        * If second frame or later, offset_delta + 1 = actual offset delta
        *   - So calculate delta, then subtract 1, for stored offset_delta
        *
        * !! Put another way, the stored offset_delta is always 1 less than it actually is !!
        *
        * Stored this way to prevent duplicate frames for the same offset, b/c min offset_delta of 0 means an actual
        * min offset delta of 1
        * */
        int offsetDelta = frames.isEmpty() ? currOffset : currOffset - frames.get(frames.size()-1).getOffset() - 1;

        if (operands != null && !operands.isEmpty()) {
            // overwrite operands stack with operands from branch
            operandStack = new ArrayList<>(operands);

            if (localsDelta == 0 && operands.size() == 1) { // same_locals_1_stack_item_frame
                frame.setFrameType(offsetDelta + 64);
                frame.setOperands(List.of(resolveVariable(operands.get(0))));
            } else { // full_frame
                frame.setFrameType(255);
                frame.setOffsetDelta(offsetDelta);
                frame.setLocals(resolveLocals(true));
                frame.setOperands(operands.stream().map(this::resolveVariable).toList());
            }
            frames.add(frame);
            return;
        } else if (!operandStack.isEmpty()) {
            // branch to this location has no operands, so can disregard what has been accumulated here
            operandStack = new ArrayList<>();
        }


        switch (localsDelta) {
            case 1, 2, 3 -> { // append_frame
                frame.setFrameType(localsDelta + 251);
                frame.setOffsetDelta(offsetDelta);
                if (localsCnt > localsDelta) {
                    frame.setLocals(resolveLastNLocals(localsDelta));
                } else {
                    frame.setLocals(resolveLocals(false));
                }
            }
            case 0 -> frame.setFrameType(offsetDelta); // same_frame
            case -1, -2, -3 -> { // chop_frame
                frame.setFrameType(localsDelta + 251);
                frame.setOffsetDelta(offsetDelta);
            }
            default -> { // full_frame
                frame.setFrameType(255);
                frame.setOffsetDelta(offsetDelta);
                frame.setLocals(resolveLocals(true));
            }
        }

        frames.add(frame);
    }

    /**
     * Traverses the local variables stack and constructs the corresponding verification types.
     * @param includeParams true for full_frame, false otherwise
     * @return list of verification types for local variables
     */
    private List<VariableInfo> resolveLocals(boolean includeParams) {
        List<VariableInfo> locals = new ArrayList<>();
        for (int i=(includeParams ? 0 : 1); i<localsStack.size(); i++) {
            localsStack.get(i).stream()
                    .map(this::resolveVariable)
                    .forEach(locals::add);
        }
        return locals;
    }

    /**
     * Traverses the local variables stack and constructs the corresponding verification types for the last {@code n}
     * variables to be added.
     * @param n number of variables to include
     * @return list of verification types for {@code n} most recently added variables
     */
    private List<VariableInfo> resolveLastNLocals(int n) {
        List<VariableInfo> locals = new ArrayList<>();
        for (int i=localsStack.size()-1; i>=0; i--) {
            List<String> scope = localsStack.get(i);
            for (int j=scope.size()-1; j>=0; j--) {
                locals.add(0, resolveVariable(scope.get(j)));
                if (locals.size() == n) {
                    return locals;
                }
            }
        }
        return locals;
    }

    /**
     * Convert given variable from its intermediate string representation to its corresponding verification type object.
     * @param variable local variable or operand stack entry
     * @return verification type
     */
    public VariableInfo resolveVariable(String variable) {
        return switch (variable) {
            case INT_VAR -> VariableInfo._int();
            case FLOAT_VAR -> VariableInfo._float();
            case NULL_VAR -> VariableInfo._null();
            default -> new ObjectVariableInfo(constantPool.addClassConstant(variable));
        };
    }

    /**
     * Retrieves string representation of the type of the local variable at the given index.
     * @param n local variable index
     * @return variable type
     */
    public String getNthLocal(int n) {
        int inc = 0;
        for (List<String> scope : localsStack) {
            for (String local : scope) {
                if (inc == n) return local;
                inc++;
            }
        }
        return null;
    }

    /**
     * Determines the verification type (string representation) of the given variable declaration, and adds it to the
     * local variable table.
     * @param localDecl local variable declaration
     */
    public void addLocal(LocalDecl localDecl) {
        String local = switch (localDecl.type.kind) {
            case INT, BOOLEAN -> INT_VAR;
            case FLOAT -> FLOAT_VAR;
            case NULL -> NULL_VAR;
            case CLASS -> localDecl.type.print();
            default -> localDecl.type.descriptor();
        };
        pushLocal(local);
    }

    /**
     * <pre>
     * Adds local variable type to the current scope, overwrites {@link #maxLocals} if current {@link #localsCnt}
     * is now larger.
     *
     * Note - {@link #localsCnt} doesn't get incremented for parameters.
     * </pre>
     * @param local local variable type
     */
    private void pushLocal(String local) {
        localsStack.get(localsStack.size()-1).add(local);
        if (localsStack.size() > 1) localsCnt++;
        if (localsCnt > maxLocals) maxLocals = localsCnt;
    }

    /**
     * Pushes new local variable scope onto the {@link #localsStack}.
     */
    public void pushScope() {
        localsStack.add(new ArrayList<>());
    }

    /**
     * Removes current local variable scope from the {@link #localsStack} and decrements the {@link #localsCnt}
     * accordingly.
     */
    public void popScope() {
        List<String> scope = localsStack.remove(localsStack.size()-1);
        localsCnt -= scope.size();
    }

    public boolean hasFrames() {
        return !frames.isEmpty();
    }

    /**
     * Pushes argument count of an imminent call to the arg count stack, to be used when manipulating the operand stack.
     * @param argCnt number of arguments in call
     */
    public void pushArgCnt(int argCnt) {
        this.argCnts.push(argCnt);
    }

    /**
     * @return maximum number of items that will appear on the operand stack at any given time when executing the method
     */
    public int getMaxStackSize() {
        return maxStackSize;
    }

    /**
     * @return maximum number of local variables (including parameters) that will exist in scope at any given time when
     * executing the method
     */
    public int getMaxLocals() {
        return maxLocals + paramsCnt;
    }

    /**
     * @return number of local variables (including parameters) that are currently in scope within the method
     */
    public int getLocalsCnt() {
        return paramsCnt + localsCnt;
    }

    /**
     * Writes the {StackMapTable_attribute} to the given byte stream, if frames have been added.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        // short-circuit if no frames added
        if (frames.isEmpty()) return;

        // first, write frames to separate byte stream to determine attribute length, then write to given byte stream
        try (ByteArrayOutputStream framesByteStream = new ByteArrayOutputStream();
             DataOutputStream framesStream = new DataOutputStream(framesByteStream)) {
            U2.of(frames.size()).writeTo(framesStream);         // write number_of_entries
            for (StackMapFrame frame : frames) {                // write entries
                frame.writeTo(framesStream);
            }

            setAttributeLength(U4.of(framesByteStream.size())); // set attribute_length
            super.writeTo(stream);                              // write attribute_name_index, attribute_length

            framesByteStream.writeTo(stream);                   // write attribute contents
        }
    }
}

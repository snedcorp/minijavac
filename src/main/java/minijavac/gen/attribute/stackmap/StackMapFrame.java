package minijavac.gen.attribute.stackmap;

import minijavac.gen.file.Writable;
import minijavac.gen._byte.U1;
import minijavac.gen._byte.U2;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * <pre>
 * Object representation of a {@code stack_map_frame}, containing information about the expected types on the operand
 * stack, and in the local variable table, at a certain byte offset within a method. This is needed by the JVM during
 * verification by type checking, to prevent the need for full dataflow analysis of branching statements.
 *
 * A {@code full_frame} contains the frame type, offset delta from the previous frame, and full local and operand types
 * - but to save space, there are several different frame types that can be used in certain situations to omit some of
 * that information, by relying on the context of the frames that came before.
 *
 * Note:
 *   - Each method has an implicit first frame containing its parameters (if any) as locals, and any future frames are
 *     built with it in mind.
 *   - Unless it's the first explicit frame in the method, the value of {@code offset_delta} is always 1 less than it
 *     actually is (see comment in {@link StackMapTableAttribute#addFrame(int, List) addFrame}).
 *
 * Frame types:
 *
 * {@code same_frame}:
 *   - {@code frame_type = 0-63}
 *     - Local variable types have not changed since the previous frame, and the operand stack is empty
 *     - {@code offset_delta} is given implicitly, equals the {@code frame_type}
 *
 * {@code same_locals_1_stack_item_frame}:
 *   - {@code frame_type = 64-127}
 *     - Local variable types have not changed since the previous frame, and the operand stack contains one item
 *     - {@code offset_delta} is given implicitly, equals {@code frame_type - 64}
 *   - {@code operand}: Type of single item on operand stack
 *
 * {@code chop_frame}:
 *   - {@code frame_type = 248-250}
 *     - Last {@code 251 - frame_type} local variables have been removed since the previous frame
 *   - {@code offset_delta}: delta from previous frame's offset
 *
 * {@code append_frame}:
 *   - {@code frame_type = 252-254}
 *     - {@code frame_type - 251} additional local variables have been added since the previous frame
 *   - {@code offset_delta}: delta from previous frame's offset
 *   - {@code locals}: types of the additional locals
 *
 * {@code full_frame}:
 *   - {@code frame_type = 255}
 *   - {@code offset_delta}: delta from previous frame's offset
 *   - {@code locals}: full list of local variable types (including parameters)
 *   - {@code operands}: full list of operand stack types
 *
 * There is another frame type called {@code same_locals_1_stack_item_frame_extended}, but it's not currently
 * implemented - didn't seem to occur much in practice.
 * </pre>
 */
public class StackMapFrame implements Writable {

    private int frameType;
    private int offsetDelta;
    private List<VariableInfo> locals;
    private List<VariableInfo> operands;

    /*
    * Byte offset within method - used by subsequent frames.
    * */
    private int offset;

    /*
     * Number of non-parameter local variables in scope when frame was created - used by subsequent frames.
     */
    private int localsCnt;

    /**
     * Writes the {@code stack_map_frame} to the given byte stream, with the format determined by the {@code frame_type}.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        U1.of(frameType).writeTo(stream);                // write frame_type

        // full_frame
        if (frameType == 255) {
            U2.of(offsetDelta).writeTo(stream);          // write offset_delta
            U2.of(locals.size()).writeTo(stream);        // write number_of_locals
            for (VariableInfo local : locals) {          // write locals
                local.writeTo(stream);
            }
            if (operands == null || operands.isEmpty()) {
                U2.of(0).writeTo(stream);           // write number_of_stack_items
            } else {
                U2.of(operands.size()).writeTo(stream); // write number_of_stack_items
                for (VariableInfo operand : operands) { // write stack
                    operand.writeTo(stream);
                }
            }
        } else if (frameType > 63 && frameType < 128) { // same_locals_1_stack_item_frame
            operands.get(0).writeTo(stream);
        } else if (frameType > 247) { // chop_frame or append_frame
            U2.of(offsetDelta).writeTo(stream);
            if (frameType > 251) { // append_frame
                for (VariableInfo local : locals) {     // write locals
                    local.writeTo(stream);
                }
            }
        }
    }

    public int getFrameType() {
        return frameType;
    }

    public void setFrameType(int frameType) {
        this.frameType = frameType;
    }

    public int getOffsetDelta() {
        return offsetDelta;
    }

    public void setOffsetDelta(int offsetDelta) {
        this.offsetDelta = offsetDelta;
    }

    public List<VariableInfo> getLocals() {
        return locals;
    }

    public void setLocals(List<VariableInfo> locals) {
        this.locals = locals;
    }

    public List<VariableInfo> getOperands() {
        return operands;
    }

    public void setOperands(List<VariableInfo> operands) {
        this.operands = operands;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLocalsCnt() {
        return localsCnt;
    }

    public void setLocalsCnt(int localsCnt) {
        this.localsCnt = localsCnt;
    }
}

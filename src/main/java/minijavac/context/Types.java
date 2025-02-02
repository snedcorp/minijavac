package minijavac.context;

import minijavac.ast.*;
import minijavac.err.CompileError;
import minijavac.syntax.Position;

/**
 * Utility class used by {@link Context} to evaluate type equality and create common type errors.
 * <br><br>
 * Note: The ERROR {@link TypeKind} is special, as it indicates to a node that some type checking error occurred in one
 * of its children. That error was, presumably, logged when it was caught, so to avoid the possibility of
 * cascading errors as the ERROR type bubbles up an expression tree, any comparison with the ERROR type will return true.
 * That way, only the original error will be logged, and not any further errors that result as a consequence.
 */
public class Types {

    /**
     * Generic ERROR type instance, used to bubble up errors during type checking.
     */
    public static final Type ERR = new BaseType(TypeKind.ERROR, null);

    /**
     * Evaluates whether a given {@link Type} node has the expected {@link TypeKind}.
     * @param typeKind expected kind
     * @param type     actual type
     * @return whether type has expected kind
     */
    public static boolean match(TypeKind typeKind, Type type) {
        return type.kind == TypeKind.ERROR || typeKind == type.kind;
    }

    /**
     * Evaluates whether the two given {@link Type} nodes are equal.
     * @param t1 first type
     * @param t2 second type
     * @return equality status
     */
    public static boolean match(Type t1, Type t2) {
        // types can match if kinds are different, as long as one is ERROR, or if one is NULL and the other is nullable
        if (t1.kind == TypeKind.ERROR || t2.kind == TypeKind.ERROR ||
                (t1.kind == TypeKind.NULL && !(t2 instanceof BaseType)) ||
                (t2.kind == TypeKind.NULL && !(t1 instanceof BaseType))) {
            return true;
        }
        if (t1.kind != t2.kind) return false; // otherwise, type kinds need to match

        if (t1 instanceof ClassType ct1) { // if both are class types, class names must match
            return ct1.className.contents.equals(((ClassType) t2).className.contents);
        }

        if (t1 instanceof ArrayType at1) { // if both are array types, element types and dimension counts must match.
            ArrayType at2 = (ArrayType) t2;
            return match(at1.elementType, at2.elementType) && at1.dims == at2.dims;
        }

        return true;
    }

    /**
     * Evaluates whether a {@link Type} is numeric or not.
     * @param type type
     * @return numeric status
     */
    public static boolean isNumeric(Type type) {
        return type.kind == TypeKind.ERROR || type.kind.isNumeric();
    }

    /**
     * Factory method for creating an "incompatible types" error.
     * @param pos position
     * @param t1  first type
     * @param t2  second type
     * @return {@link CompileError}
     */
    public static CompileError incompatibleTypes(Position pos, Type t1, String t2) {
        return new CompileError(pos,
                String.format("incompatible types: %s cannot be converted to %s", t1.print(), t2));
    }

    /**
     * Factory method for creating an "incompatible types" error.
     * @param pos position
     * @param t1  first type
     * @param t2  second type
     * @return {@link CompileError}
     */
    public static CompileError incompatibleTypes(Position pos, Type t1, Type t2) {
        return new CompileError(pos,
                String.format("incompatible types: %s cannot be converted to %s", t1.print(), t2.print()));
    }

    /**
     * Factory method for creating an "incompatible types: possible lossy conversion" error.
     * @param pos position
     * @param t1  first type
     * @param t2  second type
     * @return {@link CompileError}
     */
    public static CompileError lossyConversion(Position pos, Type t1, Type t2) {
        return new CompileError(pos,
                String.format("incompatible types: possible lossy conversion from %s to %s", t1.print(), t2.print()));
    }
}

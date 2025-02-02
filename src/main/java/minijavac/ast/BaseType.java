package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * {@link Type} variant corresponding to the implemented primitive types, as well as {@code void} and {@code null}.
 *
 * Examples:
 *  - {@code int}
 *  - {@code float}
 *  - {@code boolean}
 *  - {@code void}
 *  - {@code null}
 * </pre>
 */
public class BaseType extends Type {

    public BaseType(TypeKind t, Position pos){
        super(t, pos);
    }

    /**
     * Factory method used for standard library members.
     */
    public static BaseType stdLib(TypeKind kind) {
        return new BaseType(kind, null);
    }

    /**
     * {@inheritDoc}
     * <pre>
     * Examples:
     *  - {@code "int"}
     *  - {@code "float"}
     *  - {@code "boolean"}
     *  - {@code "void"}
     *  - {@code "<null>"}
     * </pre>
     */
    @Override
    public String print() {
        return kind == TypeKind.NULL ? "<null>" : kind.name().toLowerCase();
    }

    /**
     * {@inheritDoc}
     * <pre>
     * Examples:
     *  - {@code "I"}
     *  - {@code "F"}
     *  - {@code "B"}
     *  - {@code "Z"}
     * </pre>
     */
    @Override
    public String descriptor() {
        return descriptor(this.kind);
    }

    /**
     * {@inheritDoc}
     * <pre>
     * Generates a string representation of the given {@link TypeKind}, suitable for code generation.
     * Examples:
     *  - {@code "I"}
     *  - {@code "F"}
     *  - {@code "B"}
     *  - {@code "Z"}
     * </pre>
     */
    public static String descriptor(TypeKind kind) {
        return switch (kind) {
            case VOID -> "V";
            case INT -> "I";
            case FLOAT -> "F";
            case BOOLEAN -> "Z";
            default -> throw new IllegalArgumentException("Unexpected type kind: " + kind);
        };
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitBaseType(this, s, a);
    }
}

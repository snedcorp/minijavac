package minijavac.gen.instruction;

import minijavac.ast.BaseType;
import minijavac.ast.TypeKind;

import static minijavac.ast.TypeKind.*;

/**
 * Enumeration of all supported primitive array types - needed by {@link OpCode#newarray} instruction.
 */
public enum ArrType {
    T_BOOLEAN(4),
    T_FLOAT(6),
    T_INT(10);

    private final int code;

    ArrType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    /**
     * @param type base type
     * @return corresponding array type code for the given type's kind
     */
    public static int getCodeFromType(BaseType type) {
        return switch (type.kind) {
            case BOOLEAN -> T_BOOLEAN.getCode();
            case FLOAT -> T_FLOAT.getCode();
            case INT -> T_INT.getCode();
            default -> throw new IllegalArgumentException("Unexpected type " + type.kind);
        };
    }

    /**
     * @param code array type code
     * @return corresponding type kind for the given array type code
     */
    public static TypeKind getKindFromCode(int code) {
        return switch (code) {
            case 4 -> BOOLEAN;
            case 6 -> FLOAT;
            case 10 -> INT;
            default -> throw new IllegalArgumentException("Unexpected code " + code);
        };
    }
}

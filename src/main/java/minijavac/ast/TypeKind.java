package minijavac.ast;

/**
 * Enumeration of all the different possible kinds of types.
 * <br><br>
 * ERROR is included for the purposes of type checking.
 */
public enum TypeKind {
    VOID,
    INT,
    FLOAT,
    BOOLEAN,
    CLASS,
    ARRAY,
    ERROR,
    NULL;

    public boolean isNumeric() {
        return this == INT || this == FLOAT;
    }
}

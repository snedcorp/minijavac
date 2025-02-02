package minijavac.ast;

/**
 * Enumeration of different method types.
 * <br><br>
 * Special consideration is given to a chaining constructor (i.e. a constructor that calls another constructor) because
 * it is handled differently in code generation.
 */
public enum MethodType {
    METHOD,
    CONSTRUCTOR,
    CHAINING_CONSTRUCTOR
}

package minijavac.ast;

import minijavac.syntax.Position;

/**
 * Abstract base class for all types, containing just the {@link TypeKind}.
 * <br>
 * @see minijavac.ast.ArrayType
 * @see minijavac.ast.BaseType
 * @see minijavac.ast.ClassType
 */
abstract public class Type extends AST {

    public TypeKind kind;

    public Type(TypeKind kind, Position pos){
        super(pos);
        this.kind = kind;
    }

    /**
     * Generates a {@link String} representation of the {@link Type}, suitable for printing errors.
     */
    abstract public String print();

    /**
     * Generates a {@link String} representation of the {@link Type}, suitable for code generation.
     */
    abstract public String descriptor();
}

        
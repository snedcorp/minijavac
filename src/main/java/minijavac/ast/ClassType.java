package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * {@link Type} variant corresponding to an instantiation of a class - i.e. an object.
 *
 * Examples:
 *  - {@code Test t = new Test();}
 * </pre>
 */
public class ClassType extends Type {

    public Identifier className;
    public Declaration decl;

    public ClassType(Identifier className, Position pos) {
        super(TypeKind.CLASS, pos);
        this.className = className;
    }

    /**
     * Factory method used for standard library classes and methods.
     */
    public static ClassType stdLib(String className) {
        return new ClassType(Identifier.of(className), null);
    }

    /**
     * {@inheritDoc}
     * <pre>
     * Examples:
     *  - {@code "Test"}
     *  - {@code "String"}
     * </pre>
     */
    @Override
    public String print() {
        return className.contents;
    }

    /**
     * {@inheritDoc}
     * <pre>
     * Examples:
     *  - {@code "LTest;"}
     *  - {@code "Ljava/lang/String;"}
     * </pre>
     */
    @Override
    public String descriptor() {
        return String.format("L%s;", decl != null ? decl.id.contents : className.contents);
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitClassType(this, s, a);
    }
}

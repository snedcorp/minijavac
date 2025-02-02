package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

import java.util.List;

/**
 * <pre>
 * AST node representing a class declaration.
 *
 * Example:
 *  - {@code class Foo {...}}
 * </pre>
 */
public class ClassDecl extends Declaration {

    public List<FieldDecl> fieldDecls;
    public List<MethodDecl> methodDecls;
    public String shortName;

    public ClassDecl(Identifier id, List<FieldDecl> fieldDecls, List<MethodDecl> methodDecls, Position pos) {
        super(id, new ClassType(id, pos), false, pos);
        this.fieldDecls = fieldDecls;
        this.methodDecls = methodDecls;
    }

    public static ClassDecl stdLib(String className, List<FieldDecl> fieldDecls, List<MethodDecl> methodDecls,
                                   String shortName) {
        ClassDecl classDecl = new ClassDecl(Identifier.of(className), fieldDecls, methodDecls, null);
        classDecl.shortName = shortName;
        return classDecl;
    }

    public ClassDecl(Identifier id) {
        super(id, new ClassType(id, null), false, null);
    }

    @Override
    public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
        return v.visitClassDecl(this, s, a);
    }
}

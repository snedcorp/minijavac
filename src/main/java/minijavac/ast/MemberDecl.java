package minijavac.ast;

import minijavac.syntax.Position;

/**
 * AST node representing a declaration at the class level.
 * <br><br>
 * @see minijavac.ast.FieldDecl
 * @see minijavac.ast.MethodDecl
 */
abstract public class MemberDecl extends Declaration {

    public Access access;
    public boolean isStatic;

    public ClassDecl classDecl;

    public MemberDecl(Access access, boolean isStatic, boolean isFinal, Type type, Identifier id, Position pos) {
        super(id, type, isFinal, pos);
        this.access = access;
        this.isStatic = isStatic;
    }
    
    public MemberDecl(MemberDecl memberDecl, Position pos) {
    	super(memberDecl.id, memberDecl.type, memberDecl.isFinal, pos);
    	this.access = memberDecl.access;
    	this.isStatic = memberDecl.isStatic;
    }
}

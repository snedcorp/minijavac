package minijavac.context.enter;

import minijavac.ast.*;
import minijavac.context.Context;
import minijavac.context.SymbolTable;
import minijavac.context.TraversalStateViewer;
import minijavac.listener.Listener;

import java.util.Set;

/**
 * <pre>
 * {@link Visitor} implementation that enters class and member information into the given {@link SymbolTable} for the
 * given {@link ClassDecl} node.
 *
 * Uses an {@link EnterTraversalState} instance to maintain a set of class names that are referred to within the
 * traversed class but were unable to be resolved. These class names are returned to the caller of the {@link #enter(ClassDecl)}
 * method, to enable the parsing of additional classes as needed, even if unspecified by the user.
 *
 * Note that to accurately determine if a class is potentially referencing another, yet to be parsed, class, the {@link ClassDecl}
 * must be fully traversed, while maintaining a correctly scoped {@link SymbolTable} with all parameter and local
 * variable declarations added as they are encountered.
 *
 * Because errors may occur in the {@link SymbolTable} when attempting to resolve a {@link ClassType} or {@link IdRef},
 * or when adding a {@link ParameterDecl} or {@link VarDecl} to the current scope, these operations are preceded by
 * setting the ignore flag on the given {@link Listener} to true, so they are not recorded.
 *
 * The only errors that should be thrown here are at the member level, anything beyond that will be handled by the
 * {@link Context} visitor implementation once all classes have been parsed and entered.
 * </pre>
 */
public class Enter implements Visitor<EnterTraversalState, EnterArg, Object> {

    private final SymbolTable symbolTable;
    private final Listener listener;

    public Enter(SymbolTable symbolTable, Listener listener) {
        this.symbolTable = symbolTable;
        this.listener = listener;
    }

    /**
     * Enters class and member information into the {@link SymbolTable} for the given {@link ClassDecl}.
     * @param classDecl class declaration
     * @return set of referenced class names that were unable to be resolved
     */
    public Set<String> enter(ClassDecl classDecl) {
        EnterTraversalState state = new EnterTraversalState();
        classDecl.visit(this, state, EnterArg.NONE);
        return state.getReferencedClasses();
    }

    /**
     * Enters class and member information into the {@link SymbolTable} for the given std lib {@link ClassDecl}.
     * @param stdClassDecl std lib class declaration
     */
    public void enterStd(ClassDecl stdClassDecl) {
        EnterTraversalState state = new EnterTraversalState();
        stdClassDecl.visit(this, state, EnterArg.STDLIB);
    }

    @Override
    public Object visitClassDecl(ClassDecl classDecl, EnterTraversalState state, EnterArg arg) {
        state.setCurrClass(classDecl);

        // add class and ready symbol table for traversal, short-circuit if unable to be added
        boolean added = symbolTable.addAndEnterClass(classDecl, new TraversalStateViewer(state));
        if (!added) return null;

        for (FieldDecl fieldDecl : classDecl.fieldDecls) {
            fieldDecl.visit(this, state, arg);
        }

        for (MethodDecl methodDecl : classDecl.methodDecls) {
            methodDecl.visit(this, state,arg);
        }

        if (arg != EnterArg.STDLIB) {
            // if no constructors found, add default
            MethodDecl defaultConstructor = symbolTable.addDefaultConstructorIfNecessary();
            if (defaultConstructor != null) {
                defaultConstructor.classDecl = classDecl;
                classDecl.methodDecls.add(0, defaultConstructor);
            }   
        }

        symbolTable.exitClass();
        return null;
    }

    @Override
    public Object visitFieldDecl(FieldDecl fieldDecl, EnterTraversalState state, EnterArg arg) {
        fieldDecl.type.visit(this, state, arg);
        fieldDecl.classDecl = state.getCurrClass();
        symbolTable.addFieldDecl(fieldDecl); // add field to symbol table
        return null;
    }

    @Override
    public Object visitMethodDecl(MethodDecl methodDecl, EnterTraversalState state, EnterArg arg) {
        state.setCurrMethod(methodDecl);

        methodDecl.type.visit(this, state, arg);
        methodDecl.classDecl = state.getCurrClass();
        // add method to symbol table
        symbolTable.addMethodOrConstructorDecl(methodDecl);

        if (arg == EnterArg.STDLIB) return null;

        // if in user-defined class, traverse parameters and body to populate referenced classes
        symbolTable.pushScope();
        for (ParameterDecl parameterDecl : methodDecl.parameterDeclList) {
            parameterDecl.visit(this, state, arg);
        }

        symbolTable.pushScope();
        for (Statement statement : methodDecl.statementList) {
            statement.visit(this, state, arg);
        }
        symbolTable.popScope();

        symbolTable.popScope();

        return null;
    }

    @Override
    public Object visitParameterDecl(ParameterDecl parameterDecl, EnterTraversalState state, EnterArg arg) {
        parameterDecl.type.visit(this, state, arg);
        listener.setIgnore(true);
        symbolTable.addLocalDecl(parameterDecl);
        listener.setIgnore(false);
        return null;
    }

    @Override
    public Object visitVarDecl(VarDecl decl, EnterTraversalState state, EnterArg arg) {
        decl.type.visit(this, state, arg);
        listener.setIgnore(true);
        symbolTable.addLocalDecl(decl);
        listener.setIgnore(false);
        return null;
    }

    @Override
    public Object visitBaseType(BaseType type, EnterTraversalState state, EnterArg arg) {
        return null;
    }

    @Override
    public Object visitClassType(ClassType type, EnterTraversalState state, EnterArg arg) {
        if (arg == EnterArg.STDLIB) return null;

        listener.setIgnore(true);
        // if class unable to be resolved, add to referenced classes
        if (symbolTable.getClassDecl(type.className) == null) {
            state.addReferencedClass(type.className.contents);
        }
        listener.setIgnore(false);
        return null;
    }

    @Override
    public Object visitArrayType(ArrayType type, EnterTraversalState state, EnterArg arg) {
        type.elementType.visit(this, state, arg);
        return null;
    }

    @Override
    public Object visitBlockStmt(BlockStmt stmt, EnterTraversalState state, EnterArg arg) {
        for (Statement s : stmt.statements) {
            s.visit(this, state, arg);
        }
        return null;
    }

    @Override
    public Object visitVarDeclStmt(VarDeclStmt stmt, EnterTraversalState state, EnterArg arg) {
        stmt.decl.visit(this, state, arg);
        if (stmt.expr != null) stmt.expr.visit(this, state, arg);
        return null;
    }

    @Override
    public Object visitAssignStmt(AssignStmt stmt, EnterTraversalState state, EnterArg arg) {
        stmt.ref.visit(this, state, arg);
        stmt.val.visit(this, state, arg);
        return null;
    }

    @Override
    public Object visitCallStmt(CallStmt stmt, EnterTraversalState state, EnterArg arg) {
        stmt.methodRef.visit(this, state, arg);
        return null;
    }

    @Override
    public Object visitReturnStmt(ReturnStmt stmt, EnterTraversalState state, EnterArg arg) {
        if (stmt.expr != null) stmt.expr.visit(this, state, arg);
        return null;
    }

    @Override
    public Object visitIfStmt(IfStmt stmt, EnterTraversalState state, EnterArg arg) {
        stmt.cond.visit(this, state, arg);

        symbolTable.pushScope();
        stmt.thenStmt.visit(this, state, arg);
        symbolTable.popScope();

        if (stmt.elseStmt != null) {
            symbolTable.pushScope();
            stmt.elseStmt.visit(this, state, arg);
            symbolTable.popScope();
        }
        return null;
    }

    @Override
    public Object visitWhileStmt(WhileStmt stmt, EnterTraversalState state, EnterArg arg) {
        stmt.cond.visit(this, state, arg);
        symbolTable.pushScope();
        stmt.body.visit(this, state, arg);
        symbolTable.popScope();
        return null;
    }

    @Override
    public Object visitDoWhileStmt(DoWhileStmt stmt, EnterTraversalState state, EnterArg arg) {
        symbolTable.pushScope();
        stmt.body.visit(this, state, arg);
        symbolTable.popScope();
        stmt.cond.visit(this, state, arg);
        return null;
    }

    @Override
    public Object visitExprStmt(ExprStatement stmt, EnterTraversalState state, EnterArg arg) {
        stmt.expr.visit(this, state, arg);
        return null;
    }

    @Override
    public Object visitBreakStmt(BreakStmt stmt, EnterTraversalState state, EnterArg arg) {
        return null;
    }

    @Override
    public Object visitContinueStmt(ContinueStmt stmt, EnterTraversalState state, EnterArg arg) {
        return null;
    }

    @Override
    public Object visitForStmt(ForStmt stmt, EnterTraversalState state, EnterArg arg) {
        symbolTable.pushScope();
        stmt.initStmt.visit(this, state, arg);
        stmt.cond.visit(this, state, arg);
        stmt.updateStmt.visit(this, state, arg);
        stmt.body.visit(this, state, arg);
        symbolTable.popScope();
        return null;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr expr, EnterTraversalState state, EnterArg arg) {
        expr.expr.visit(this, state, arg);
        return null;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr expr, EnterTraversalState state, EnterArg arg) {
        expr.left.visit(this, state, arg);
        expr.right.visit(this, state, arg);
        return null;
    }

    @Override
    public Object visitRefExpr(RefExpr expr, EnterTraversalState state, EnterArg arg) {
        expr.ref.visit(this, state, arg);
        return null;
    }

    @Override
    public Object visitLiteralExpr(LiteralExpr expr, EnterTraversalState state, EnterArg arg) {
        return null;
    }

    @Override
    public Object visitNewObjectExpr(NewObjectExpr expr, EnterTraversalState state, EnterArg arg) {
        expr.classType.visit(this, state, arg);

        for (Expression argExpr : expr.argList) {
            argExpr.visit(this, state, arg);
        }
        return null;
    }

    @Override
    public Object visitNewArrayExpr(NewArrayExpr expr, EnterTraversalState state, EnterArg arg) {
        expr.elementType.visit(this, state, arg);

        for (Expression sizeExpr : expr.sizeExprList) {
            sizeExpr.visit(this, state, arg);
        }
        return null;
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr expr, EnterTraversalState state, EnterArg arg) {
        expr.expr.visit(this, state, arg);
        return null;
    }

    @Override
    public Object visitTernaryExpr(TernaryExpr expr, EnterTraversalState state, EnterArg arg) {
        expr.cond.visit(this, state, arg);
        expr.expr1.visit(this, state, arg);
        expr.expr2.visit(this, state, arg);
        return null;
    }

    @Override
    public Object visitNewArrayInitExpr(NewArrayInitExpr expr, EnterTraversalState state, EnterArg arg) {
        expr.elementType.visit(this, state, arg);

        expr.initExpr.visit(this, state, arg);
        return null;
    }

    @Override
    public Object visitArrayInitExpr(ArrayInitExpr expr, EnterTraversalState state, EnterArg arg) {
        for (Expression initExpr : expr.exprList) {
            initExpr.visit(this, state, arg);
        }
        return null;
    }

    @Override
    public Object visitThisRef(ThisRef ref, EnterTraversalState state, EnterArg arg) {
        return null;
    }

    @Override
    public Object visitIdRef(IdRef ref, EnterTraversalState state, EnterArg arg) {
        listener.setIgnore(true);

        // if to the immediate left of a qualified ref and the symbol is unable to be resolved, add to referenced classes
        if (arg == EnterArg.QREF && symbolTable.getDecl(ref.id, false) == null) {
            state.addReferencedClass(ref.id.contents);
        }
        listener.setIgnore(false);
        return null;
    }

    @Override
    public Object visitIxRef(IxRef ref, EnterTraversalState state, EnterArg arg) {
        ref.ref.visit(this, state, EnterArg.NONE);

        for (Expression ixExpr : ref.ixExprList) {
            ixExpr.visit(this, state, arg);
        }
        return null;
    }

    @Override
    public Object visitCallRef(CallRef ref, EnterTraversalState state, EnterArg arg) {
        ref.ref.visit(this, state, EnterArg.NONE);

        for (Expression argExpr : ref.argList) {
            argExpr.visit(this, state, arg);
        }
        return null;
    }

    @Override
    public Object visitQualRef(QualRef ref, EnterTraversalState state, EnterArg arg) {
        ref.ref.visit(this, state, EnterArg.QREF); // notify left reference that it's part of a qualified reference
        return null;
    }

    @Override
    public Object visitIdentifier(Identifier id, EnterTraversalState state, EnterArg arg) {
        return null;
    }

    @Override
    public Object visitOperator(Operator op, EnterTraversalState state, EnterArg arg) {
        return null;
    }

    @Override
    public Object visitIntLiteral(IntLiteral num, EnterTraversalState state, EnterArg arg) {
        return null;
    }

    @Override
    public Object visitBooleanLiteral(BooleanLiteral bool, EnterTraversalState state, EnterArg arg) {
        return null;
    }

    @Override
    public Object visitNullLiteral(NullLiteral nul, EnterTraversalState state, EnterArg arg) {
        return null;
    }

    @Override
    public Object visitFloatLiteral(FloatLiteral num, EnterTraversalState state, EnterArg arg) {
        return null;
    }
}

package minijavac.context;

import minijavac.listener.Listener;
import minijavac.ast.*;
import minijavac.context.err.*;
import minijavac.err.CompileError;
import minijavac.syntax.TokenKind;

import java.util.*;
import java.util.stream.Collectors;

import static minijavac.context.Types.*;

/**
 * <pre>
 * {@link Visitor} implementation that performs semantic / contextual analysis and type checking on the given {@link ClassDecl}
 * node.
 *
 * Uses a {@link ContextTraversalState} instance to maintain state about the current traversal, and {@link ConArg} to
 * pass down contextual information to child nodes.
 * </pre>
 */
public class Context implements Visitor<ContextTraversalState, ConArg, Type> {

    private final SymbolTable symbolTable;
    private final Listener listener;

    public Context(SymbolTable symbolTable, Listener listener) {
        this.symbolTable = symbolTable;
        this.listener = listener;
    }

    public void resolve(ClassDecl classDecl) {
        classDecl.visit(this, new ContextTraversalState(), ConArg.NONE);
    }

    @Override
    public Type visitClassDecl(ClassDecl classDecl, ContextTraversalState state, ConArg arg) {
        state.setCurrClass(classDecl);
        symbolTable.enterClass(classDecl, new TraversalStateViewer(state));

        for (FieldDecl fieldDecl : classDecl.fieldDecls) {
            fieldDecl.visit(this, state, ConArg.NONE);
        }

        for (MethodDecl methodDecl : classDecl.methodDecls) {
            methodDecl.visit(this, state, ConArg.NONE);
        }

        symbolTable.exitClass();
        return null;
    }

    @Override
    public Type visitFieldDecl(FieldDecl fieldDecl, ContextTraversalState state, ConArg arg) {
        fieldDecl.type.visit(this, state, ConArg.NONE);
        return null;
    }

    @Override
    public Type visitMethodDecl(MethodDecl methodDecl, ContextTraversalState state, ConArg arg) {
        state.setCurrMethod(methodDecl);
        state.setStatementCnt(0);

        symbolTable.pushScope();
        for (ParameterDecl parameterDecl : methodDecl.parameterDeclList) {
            parameterDecl.visit(this, state, ConArg.NONE);
        }

        symbolTable.pushScope();
        for (Statement statement : methodDecl.statementList) {
            statement.visit(this, state, ConArg.NONE);
            state.addStatement();
        }
        symbolTable.popScope();

        symbolTable.popScope();
        return null;
    }

    @Override
    public Type visitParameterDecl(ParameterDecl parameterDecl, ContextTraversalState state, ConArg arg) {
        parameterDecl.type.visit(this, state, ConArg.NONE);
        symbolTable.addLocalDecl(parameterDecl);
        return null;
    }

    @Override
    public Type visitVarDecl(VarDecl decl, ContextTraversalState state, ConArg arg) {
        decl.type.visit(this, state, ConArg.NONE);
        symbolTable.addLocalDecl(decl);

        return decl.type;
    }

    @Override
    public Type visitBaseType(BaseType type, ContextTraversalState state, ConArg arg) {
        return null;
    }

    @Override
    public Type visitClassType(ClassType type, ContextTraversalState state, ConArg arg) {
        Declaration decl = symbolTable.getClassDecl(type.className);
        // retrieve declaration for class name, set on type if found
        if (decl != null) {
            type.decl = decl;
        }
        return null;
    }

    @Override
    public Type visitArrayType(ArrayType type, ContextTraversalState state, ConArg arg) {
        type.elementType.visit(this, state, ConArg.NONE);
        return null;
    }

    @Override
    public Type visitBlockStmt(BlockStmt stmt, ContextTraversalState state, ConArg arg) {
        for (Statement s : stmt.statements) {
            s.visit(this, state, ConArg.NONE);
        }
        return null;
    }

    @Override
    public Type visitVarDeclStmt(VarDeclStmt stmt, ContextTraversalState state, ConArg arg) {
        if (arg == ConArg.COND_STMT) { // only statement in conditional block, can't declare a variable
            listener.err(new CompileError(stmt.decl.id.pos, "variable declaration not allowed here"));
        }
        Type varDeclType = stmt.decl.visit(this, state, ConArg.NONE);

        if (stmt.expr == null) return null;

        // set currVarDecl so RHS expr can log error if same (uninitialized) variable appears
        state.setCurrVarDecl(stmt.decl);
        Type exprType = stmt.expr.visit(this, state, ConArg.NONE);
        state.setCurrVarDecl(null);

        // types must match, or int to float widening allowed
        if (match(varDeclType, exprType) ||
                (varDeclType.kind == TypeKind.FLOAT && exprType.kind == TypeKind.INT)) {
            return null;
        }

        // float to int narrowing not allowed
        if (varDeclType.kind == TypeKind.INT && exprType.kind == TypeKind.FLOAT) {
            listener.err(lossyConversion(stmt.expr.pos, exprType, varDeclType));
            return null;
        }

        listener.err(incompatibleTypes(stmt.expr.pos, exprType, varDeclType));
        return null;
    }

    @Override
    public Type visitAssignStmt(AssignStmt stmt, ContextTraversalState state, ConArg arg) {
        if (stmt.ref instanceof ThisRef) {
            listener.err(new CompileError(stmt.pos, "cannot assign to 'this'"));
            return null;
        }

        Type refType = stmt.ref.visit(this, state, ConArg.NONE);
        Type valType = stmt.val.visit(this, state, ConArg.NONE);

        Declaration refDecl = stmt.ref.getDecl();
        if (refDecl != null && refDecl.isFinal) { // cannot assign to a variable declared as final
            String msg;
            if (refDecl instanceof ParameterDecl) {
                msg = "final parameter %s may not be assigned";
            } else {
                msg = "cannot assign a value to final variable %s";
            }
            listener.err(new CompileError(stmt.pos, String.format(msg, refDecl.id.contents)));
        }

        if (stmt.operator.kind != TokenKind.ASSIGN) { // if compound assignment (+=, -=, ...), types must be numeric.
            if (!(isNumeric(refType) && isNumeric(valType))) {
                listener.err(new BinaryTypeError(stmt.operator.pos, stmt.operator.kind.getBaseFromCompound(),
                        refType, valType));
            }
            return null;
        }

        // types must match, or int to float widening allowed
        if (match(refType, valType) ||
                (refType.kind == TypeKind.FLOAT && valType.kind == TypeKind.INT)) {
            return null;
        }

        // float to int narrowing not allowed
        if (refType.kind == TypeKind.INT && valType.kind == TypeKind.FLOAT) {
            listener.err(lossyConversion(stmt.val.pos, valType, refType));
            return null;
        }

        listener.err(incompatibleTypes(stmt.val.pos, valType, refType));
        return null;
    }

    @Override
    public Type visitCallStmt(CallStmt stmt, ContextTraversalState state, ConArg arg) {
        stmt.methodRef.visit(this, state, ConArg.NONE);
        return null;
    }

    @Override
    public Type visitReturnStmt(ReturnStmt stmt, ContextTraversalState state, ConArg arg) {
        MethodDecl currMethod = state.getCurrMethod();

        // must have return value in non-void method
        if (stmt.expr == null) {
            if (currMethod.type.kind != TypeKind.VOID) {
                listener.err(new CompileError(stmt.pos, "incompatible types: missing return value"));
            }
            return null;
        }

        // can't have return value in void method
        if (currMethod.type.kind == TypeKind.VOID) {
            listener.err(new CompileError(stmt.expr.pos, "incompatible types: unexpected return value"));
            return null;
        }

        Type exprType = stmt.expr.visit(this, state, ConArg.NONE);
        // actual and expected return types must match
        if (!match(exprType, currMethod.type)) {
            listener.err(incompatibleTypes(stmt.expr.pos, exprType, currMethod.type));
        }
        return null;
    }

    @Override
    public Type visitIfStmt(IfStmt stmt, ContextTraversalState state, ConArg arg) {
        Type condType = stmt.cond.visit(this, state, ConArg.NONE);
        // conditional expression must evaluate to a boolean
        if (!match(TypeKind.BOOLEAN, condType)) {
            listener.err(incompatibleTypes(stmt.cond.pos, condType, "boolean"));
        }

        symbolTable.pushScope(); // TODO: add tests for scoping
        stmt.thenStmt.visit(this, state, ConArg.COND_STMT);
        symbolTable.popScope();

        if (stmt.elseStmt != null) {
            symbolTable.pushScope();
            stmt.elseStmt.visit(this, state, ConArg.COND_STMT);
            symbolTable.popScope();
        }

        return null;
    }

    @Override
    public Type visitWhileStmt(WhileStmt stmt, ContextTraversalState state, ConArg arg) {
        Type condType = stmt.cond.visit(this, state, ConArg.NONE);
        // conditional expression must evaluate to a boolean
        if (!match(TypeKind.BOOLEAN, condType)) {
            listener.err(incompatibleTypes(stmt.cond.pos, condType, "boolean"));
        }

        symbolTable.pushScope(); // TODO: add tests for scoping
        state.enterLoop();
        stmt.body.visit(this, state, ConArg.COND_STMT);
        state.enterLoop();
        symbolTable.popScope();

        return null;
    }

    @Override
    public Type visitDoWhileStmt(DoWhileStmt stmt, ContextTraversalState state, ConArg arg) {
        symbolTable.pushScope(); // TODO: add tests for scoping
        state.enterLoop();
        stmt.body.visit(this, state, ConArg.COND_STMT);
        state.exitLoop();
        symbolTable.popScope();

        Type condType = stmt.cond.visit(this, state, ConArg.NONE);
        // conditional expression must evaluate to a boolean
        if (!match(TypeKind.BOOLEAN, condType)) {
            listener.err(incompatibleTypes(stmt.cond.pos, condType, "boolean"));
        }

        return null;
    }

    @Override
    public Type visitExprStmt(ExprStatement stmt, ContextTraversalState state, ConArg arg) {
        stmt.expr.visit(this, state, ConArg.NONE);
        return null;
    }

    @Override
    public Type visitBreakStmt(BreakStmt stmt, ContextTraversalState state, ConArg arg) {
        if (state.getLoopCnt() == 0) {
            listener.err(new CompileError(stmt.pos, "break outside of loop"));
        }
        return null;
    }

    @Override
    public Type visitContinueStmt(ContinueStmt stmt, ContextTraversalState state, ConArg arg) {
        if (state.getLoopCnt() == 0) {
            listener.err(new CompileError(stmt.pos, "continue outside of loop"));
        }
        return null;
    }

    @Override
    public Type visitForStmt(ForStmt stmt, ContextTraversalState state, ConArg arg) {
        symbolTable.pushScope();
        // initialization statement scoped to inside the for loop
        stmt.initStmt.visit(this, state, ConArg.NONE);

        Type condType = stmt.cond.visit(this, state, ConArg.NONE);
        // conditional expression must evaluate to a boolean
        if (!match(TypeKind.BOOLEAN, condType)) {
            listener.err(incompatibleTypes(stmt.cond.pos, condType, "boolean"));
        }

        stmt.updateStmt.visit(this, state, ConArg.NONE);

        state.enterLoop();
        stmt.body.visit(this, state, ConArg.COND_STMT);
        state.exitLoop();
        symbolTable.popScope();

        return null;
    }

    @Override
    public Type visitUnaryExpr(UnaryExpr expr, ContextTraversalState state, ConArg arg) {
        Type exprType = expr.expr.visit(this, state, ConArg.NONE);
        TokenKind kind = expr.operator.kind;

        expr.type = exprType;

        // for prefix expression, must be operating on a reference (not a literal), and that reference must not be a method invocation
        if ((kind == TokenKind.INCREMENT || kind == TokenKind.DECREMENT) &&
                (!(expr.expr instanceof RefExpr) || ((RefExpr) expr.expr).ref.getDecl() instanceof MethodDecl)) {
            listener.err(new UnexpectedTypeError(expr.expr.pos));
            return ERR;
        }

        // ++, --, - -> must be numeric
        // ~ -> must be int
        // ! -> must be boolean
        if (
                ((kind == TokenKind.INCREMENT || kind == TokenKind.DECREMENT || kind == TokenKind.MINUS)
                        && !isNumeric(exprType)) ||
                (kind == TokenKind.COMPLEMENT && !match(TypeKind.INT, exprType)) ||
                (kind == TokenKind.NOT && !match(TypeKind.BOOLEAN, exprType))
        ) {
            listener.err(new CompileError(expr.pos,
                    String.format("bad operand type %s for unary operator '%s'", exprType.print(), kind.print())));
            return ERR;
        }

        return exprType;
    }

    @Override
    public Type visitBinaryExpr(BinaryExpr expr, ContextTraversalState state, ConArg arg) {
        Type leftType = expr.left.visit(this, state, ConArg.NONE);
        Type rightType = expr.right.visit(this, state, ConArg.NONE);

        // if either side has an error, short circuit here to avoid comparing with other side
        if (leftType.kind == TypeKind.ERROR) return leftType;
        if (rightType.kind == TypeKind.ERROR) return rightType;

        expr.leftType = leftType;
        expr.rightType = rightType;

        TokenKind op = expr.operator.kind;

        // logical operators must have boolean operands
        if (op == TokenKind.AND || op == TokenKind.OR) {
            if (leftType.kind == TypeKind.BOOLEAN && rightType.kind == TypeKind.BOOLEAN) return leftType;
        }

        // arithmetic operators must have numeric operands
        if (op.isArithmeticOp()) {
            if (isNumeric(leftType) && isNumeric(rightType)) {
                // in case numeric types are different, return widest
                return rightType.kind == TypeKind.FLOAT ? rightType : leftType;
            }
        }

        // numerical comparison operators must have numeric operands
        if (op == TokenKind.LT || op == TokenKind.GT ||
                op == TokenKind.LTE || op == TokenKind.GTE) {
            if (isNumeric(leftType) && isNumeric(rightType)) {
                return new BaseType(TypeKind.BOOLEAN, leftType.pos); // results in boolean type
            }
        }

        // equality operators must have operands with equal types - or unequal, but numeric, types
        if (op == TokenKind.EQ || op == TokenKind.NOT_EQ) {
            if (match(leftType, rightType) ||
                    (isNumeric(leftType) && isNumeric(rightType))) {
                return new BaseType(TypeKind.BOOLEAN, leftType.pos); // results in boolean type
            }
        }

        listener.err(new BinaryTypeError(expr.operator.pos, expr.operator.kind, leftType, rightType));
        return ERR;
    }

    @Override
    public Type visitRefExpr(RefExpr expr, ContextTraversalState state, ConArg arg) {
        Type refType = expr.ref.visit(this, state, ConArg.NONE);
        if (expr.ref.isStatic) { // cannot refer to class without also specifying a static member
            SymbolError err = SymbolError.builder()
                    .position(expr.ref.pos)
                    .variableSymbol(expr.ref.getDecl().id.contents)
                    .classLocation(state.getCurrClass().id.contents)
                    .build();
            listener.err(err);
        }
        return refType;
    }

    @Override
    public Type visitLiteralExpr(LiteralExpr expr, ContextTraversalState state, ConArg arg) {
        return expr.literal.visit(this, state, ConArg.NONE);
    }

    @Override
    public Type visitNewObjectExpr(NewObjectExpr expr, ContextTraversalState state, ConArg arg) {
        expr.classType.visit(this, state, ConArg.NONE);
        // class type needs to have been linked to a declaration
        if (expr.classType.decl == null) return ERR;
        // push argument types onto stack for method resolution
        visitCallArgs(expr.argList, state);

        // retrieve constructor decl for given class name and pushed argument types
        MethodDecl decl = symbolTable.getConstructorDecl(expr.classType.className);
        if (decl == null) return ERR; // if no decl found, return error type

        // cannot use private constructor from a different class
        if (!state.getCurrClass().id.contents.equals(decl.classDecl.id.contents) && decl.access == Access.PRIVATE) {
            listener.err(new CompileError(expr.pos,
                    String.format("%s has private access in %s",
                            decl.signature, decl.classDecl.id.contents)
            ));
            return ERR;
        }
        expr.decl = decl;

        return expr.classType;
    }

    @Override
    public Type visitNewArrayExpr(NewArrayExpr expr, ContextTraversalState state, ConArg arg) {
        expr.elementType.visit(this, state, ConArg.NONE);
        for (Expression sizeExpr : expr.sizeExprList) {
            Type sizeExprType = sizeExpr.visit(this, state, ConArg.NONE);
            // size expression must evaluate to int
            if (!match(TypeKind.INT, sizeExprType)) {
                listener.err(incompatibleTypes(sizeExpr.pos, sizeExprType, "int"));
                return ERR;
            }
        }
        // create array type, attach to expression
        expr.arrayType = new ArrayType(expr.elementType, expr.elementType.pos, expr.sizeExprList.size());
        return expr.arrayType;
    }

    @Override
    public Type visitPostfixExpr(PostfixExpr expr, ContextTraversalState state, ConArg arg) {
        Type exprType = expr.expr.visit(this, state, ConArg.NONE);

        // postfix must operate on a non-method invocation reference
        if (!(expr.expr instanceof RefExpr) || ((RefExpr) expr.expr).ref.getDecl() instanceof MethodDecl) {
            listener.err(new UnexpectedTypeError(expr.expr.pos));
            return ERR;
        }
        // operand must be numeric
        if (!isNumeric(exprType)) {
            listener.err(new CompileError(expr.operator.pos,
                    String.format("bad operand type %s for unary operator '%s'", exprType.print(), expr.operator.kind.print())));
            return ERR;
        }
        return exprType;
    }

    @Override
    public Type visitTernaryExpr(TernaryExpr expr, ContextTraversalState state, ConArg arg) {
        Type condType = expr.cond.visit(this, state, ConArg.NONE);
        Type expr1Type = expr.expr1.visit(this, state, ConArg.NONE);
        Type expr2Type = expr.expr2.visit(this, state, ConArg.NONE);

        // ternary condition must evaluate to boolean
        if (!match(TypeKind.BOOLEAN, condType)) {
            listener.err(incompatibleTypes(expr.cond.pos, condType, "boolean"));
            return ERR;
        }
        // branches must evaluate to the same type
        if (!match(expr1Type, expr2Type)) {
            listener.err(incompatibleTypes(expr.pos, expr1Type, expr2Type));
            return ERR;
        }

        return expr1Type;
    }

    @Override
    public Type visitNewArrayInitExpr(NewArrayInitExpr expr, ContextTraversalState state, ConArg arg) {
        expr.elementType.visit(this, state, ConArg.NONE);

        int errCnt = listener.getErrCnt();
        arrayInitExpr(expr.initExpr, expr.dims, expr.elementType, state, 1); // recursively type check

        if (listener.getErrCnt() != errCnt) return ERR;
        // create array type, attach to expression
        expr.arrayType = new ArrayType(expr.elementType, expr.elementType.pos, expr.dims);
        return expr.arrayType;
    }

    /**
     * Helper method that recursively type checks an {@link ArrayInitExpr}.
     * <br><br>
     * If given depth in an invocation is less than the expected number of dims, ensure that all expressions at this depth
     * are themselves initialization expressions.
     * If given depth in an invocation is equal to the expected number of dims, ensure that all expressions at this depth have the
     * correct element type.
     * @param initExpr    initialization expression
     * @param dims        expected number of dimensions
     * @param elementType base type of array
     * @param depth       current depth
     */
    private void arrayInitExpr(ArrayInitExpr initExpr, int dims, Type elementType, ContextTraversalState state, int depth) {
        for (Expression expr : initExpr.exprList) {
            if (depth == dims) {
                if (expr instanceof ArrayInitExpr) {
                    listener.err(new CompileError(expr.pos, String.format("illegal initializer for %s", elementType.print())));
                    continue;
                }
                Type exprType = expr.visit(this, state, ConArg.NONE);
                if (!match(elementType, exprType)) {
                    listener.err(incompatibleTypes(expr.pos, exprType, elementType));
                }
            } else {
                if (!(expr instanceof ArrayInitExpr childInitExpr)) {
                    Type exprType = expr.visit(this, state, ConArg.NONE);
                    listener.err(incompatibleTypes(expr.pos, exprType, elementType.print() + "[]".repeat(dims - depth)));
                    continue;
                }
                arrayInitExpr(childInitExpr, dims, elementType, state, depth+1);
            }
        }
    }

    @Override
    public Type visitArrayInitExpr(ArrayInitExpr initExpr, ContextTraversalState state, ConArg arg) {
        return null;
    }

    @Override
    public Type visitThisRef(ThisRef ref, ContextTraversalState state, ConArg arg) {
        ClassDecl currClass = state.getCurrClass();
        MethodDecl currMethod = state.getCurrMethod();

        if (currMethod.isStatic) { // "this" can only be located in instance methods
            listener.err(new CompileError(ref.pos, "non-static variable this cannot be referenced from a static context"));
        }

        if (arg == ConArg.METHOD) {
            // chained constructor call must be a) within constructor method and b) the first statement in that method
            if (!currMethod.isConstructor() || state.getStatementCnt() != 0) {
                listener.err(new CompileError(ref.pos, "call to this must be first statement in constructor"));
            }
            /*
            * Retrieve corresponding method declaration from id table and assign to reference.
            * Have to pass in new identifier containing class name at the current node's position, to ensure proper
            * caret placement when printing error.
            * */
            ref.decl = symbolTable.getConstructorDecl(currClass.id.atPos(ref.pos));
            if (ref.decl == null) return ERR; // return error if no declaration found
            currMethod.methodType = MethodType.CHAINING_CONSTRUCTOR;
        } else {
            ref.decl = currClass; // if non-method reference, just assign the class decl
        }

        return currClass.type;
    }

    @Override
    public Type visitIdRef(IdRef ref, ContextTraversalState state, ConArg arg) {
        // cannot reference a variable that's being declared in the same statement
        VarDecl currVarDecl = state.getCurrVarDecl();
        if (currVarDecl != null && currVarDecl.id.contents.equals(ref.id.contents)) {
            listener.err(new CompileError(ref.id.pos,
                    String.format("variable %s might not have been initialized", ref.id.contents)));
        }

        // retrieve declaration from id table
        ref.decl = symbolTable.getDecl(ref.id, arg == ConArg.METHOD);

        if (ref.decl == null) return ERR; // return error if no declaration found

        if (ref.decl instanceof ClassDecl) {
            ref.isStatic = true;
        } else if (state.getCurrMethod().isStatic && ref.decl instanceof MemberDecl md && !md.isStatic) {
            // no instance calls from static method
            listener.err(new CompileError(ref.id.pos,
                    String.format("non-static %s cannot be referenced from a static context",
                            arg == ConArg.METHOD ? "method " + ((MethodDecl) ref.decl).signature : "variable " +
                                    ref.id.contents)));
        }

        return ref.decl.type;
    }

    @Override
    public Type visitIxRef(IxRef ref, ContextTraversalState state, ConArg arg) {
        ref.ref.visit(this, state, ConArg.NONE);

        for (Expression ixExpr : ref.ixExprList) {
            Type ixType = ixExpr.visit(this, state, ConArg.NONE);
            // index expression can only evaluate to int
            if (!match(TypeKind.INT, ixType)) {
                listener.err(incompatibleTypes(ixExpr.pos, ixType, "int"));
                return ERR;
            }
        }

        if (ref.getDecl() == null) return ERR;

        Type declType = ref.getDecl().type;
        // must be referencing an array
        if (!(declType instanceof ArrayType arrayType)) {
            listener.err(new CompileError(ref.pos,
                    String.format("array required, but %s found", declType.print())));
            return ERR;
        }

        int dimsDiff = arrayType.dims - ref.ixExprList.size();
        // number of index expressions cannot exceed total dimensions of array
        if (dimsDiff < 0) {
            listener.err(new CompileError(ref.ixExprList.get(arrayType.dims).pos,
                    String.format("array required, but %s found", arrayType.elementType.print())));
            return ERR;
        } else if (dimsDiff == 0) {
            return arrayType.elementType;
        }
        return new ArrayType(arrayType.elementType, arrayType.pos, dimsDiff);
    }

    @Override
    public Type visitCallRef(CallRef ref, ContextTraversalState state, ConArg arg) {
        visitCallArgs(ref.argList, state); // push argument types onto stack

        // visit enclosed ref, signal that it should be referencing a method
        ref.ref.visit(this, state, ConArg.METHOD);

        if (ref.getDecl() == null) return ERR; // return error if no declaration attached by ref visit

        return ref.getDecl().type;
    }

    /**
     * Helper method that traverses an argument list and pushes its types onto the {@link SymbolTable}, to be used
     * later for method resolution.
     * <br><br>
     * Stack is needed because a call's arguments can themselves contain further calls with arguments, which then can
     * contain further calls, and so on. Using a stack guarantees that when resolving a method, the types from the most
     * recently visited argument list are used, like you'd expect.
     * @param argList argument expressions
     */
    private void visitCallArgs(List<Expression> argList, ContextTraversalState state) {
        List<SymbolTable.ArgType> argTypesList = new ArrayList<>();
        for (Expression argExpr : argList) {
            argTypesList.add(new SymbolTable.ArgType(argExpr.pos, argExpr.visit(this, state, ConArg.NONE)));
        }
        SymbolTable.ArgTypes argTypes = new SymbolTable.ArgTypes(argTypesList,
                argTypesList.stream()
                        .map(SymbolTable.ArgType::type)
                        .map(Type::print)
                        .collect(Collectors.joining(",")));

        symbolTable.pushArgTypes(argTypes);
    }

    /**
     * <pre>
     * Qualified references contain an identifier and a reference, which itself can be any type of reference, including
     * another qualified reference, which could then point to another qualified reference, etc etc.
     *
     * As an example, the qualified reference {@code this.state.person.name} would be parsed into the following AST node:
     *
     * QualRef:
     *   - id:
     *     - "name"
     *   - ref:
     *     - QualRef:
     *       - id:
     *         - "person"
     *       - ref:
     *         - QualRef:
     *           - id:
     *             - "state"
     *           - ref:
     *             - ThisRef
     * </pre>
     *
     * To resolve the qualified reference, its enclosed reference must first be resolved, and so in the case of multiple
     * qualified references chained together, its traversal can be thought of as recursive:
     *   - A qualified reference's enclosed reference is visited before any processing is done on the current node, so
     *     the call stack just keeps growing as the chain is traversed from right to left.
     *   - Once the "base case" of reaching a non-qualified reference is achieved and a declaration is attached to that
     *     base reference (or an error is found), then resolution can begin on the left-most qualified reference.
     *   - When resolving a qualified reference in the chain, either it is attached a declaration, or an error is
     *     logged.
     *   - Once the invocation for that specific reference is exited, then the reference to its immediate right is free
     *     to start resolution, and it uses the declaration just attached to its enclosed reference in order to resolve
     *     the identifier it contains.
     *   - In this way, resolution proceeds across the chain from left to right, until, lastly, the original qualified
     *     reference is resolved.
     *   - If, at any point, a declaration is unable to be attached, then the ERROR type will be bubbled upward and no
     *     resolution will be attempted on any references to its right in the chain.
     */
    @Override
    public Type visitQualRef(QualRef qRef, ContextTraversalState state, ConArg arg) {
        // mark if supposed to be referencing a method
        boolean isCall = arg == ConArg.METHOD;
        // recursively visit enclosed reference
        Type refType = qRef.ref.visit(this, state, ConArg.NONE);

        if (refType.kind == TypeKind.INT) { // catch .length.<id>
            listener.err(new CompileError(qRef.id.pos, "int cannot be dereferenced"));
            return ERR;
        }

        Declaration refDecl = qRef.ref.getDecl();
        // if enclosed reference has no declaration, bubble ERR upwards
        if (refDecl == null) return ERR;

        if (refDecl.type instanceof BaseType) { // cannot dereference a base type
            listener.err(new CompileError(qRef.id.pos, String.format("%s cannot be dereferenced", refDecl.type.print())));
            return ERR;
        }

        String className = null; // class name for member resolution
        Identifier prevId = null; // identifier of previous reference in chain, used when printing SymbolError

        if (qRef.ref.isStatic) {
            // if ref started a static context (i.e. Test.<>), class name is just the ref decl's identifier
            className = refDecl.id.contents;
        } else if (refDecl.type instanceof ClassType classType) {
            // if ref decl is a class type, ref is an object, class name is found on its type
            className = classType.className.contents;
            if (!(refDecl instanceof MethodDecl)) {
                prevId = qRef.ref.getId();
            }
        } else if (refDecl.type instanceof ArrayType arrayType) {
            // handle <array>.length, not an actual qualified reference
            if (qRef.id.contents.equals("length")) return new BaseType(TypeKind.INT, qRef.id.pos);

            // if ref is an array type, must have been indexed into in order to dereference
            if (!(qRef.ref instanceof IxRef)) {
                listener.err(SymbolError.builder()
                        .position(qRef.id.pos)
                        .variableSymbol(qRef.id.contents)
                        .varOfTypeLocation(qRef.ref.getDecl().id.contents, qRef.ref.getDecl().type.print())
                        .build()
                );
                return ERR;
            }

            // referenced array must contain objects in order to be dereferenced
            if (arrayType.elementType instanceof BaseType) {
                listener.err(new CompileError(qRef.id.pos, String.format("%s cannot be dereferenced",
                        arrayType.elementType.print())));
                return ERR;
            }

            // referenced array must have been indexed to the element level in order to be dereferenced
            int ixDepth = ((IxRef) qRef.ref).ixExprList.size();
            if (ixDepth < arrayType.dims) {
                listener.err(SymbolError.builder()
                        .position(qRef.id.pos)
                        .variableSymbol(qRef.id.contents)
                        .classLocation(arrayType.printToDepth(arrayType.dims - ixDepth))
                        .build()
                );
                return ERR;
            }
            className = ((ClassType) arrayType.elementType).className.contents; // valid array ref, get class from type
        }

        // search for member declaration corresponding to class name + id combination
        MemberDecl idDecl = symbolTable.getMemberDecl(className, qRef.id, isCall, prevId);

        if (idDecl == null) return ERR; // if no declaration found, return error

        // if declaration is private, not allowed to access from different class
        if (idDecl.access == Access.PRIVATE && !Objects.equals(className, state.getCurrClass().id.contents)) {
            listener.err(new CompileError(qRef.id.pos,
                    String.format("%s has private access in %s",
                            isCall ? ((MethodDecl) idDecl).signature : idDecl.id.contents,
                            className)
            ));
            return ERR;
        }

        // if previous reference started a static context, declaration must be static
        if (qRef.ref.isStatic && !idDecl.isStatic) {
            listener.err(new CompileError(qRef.id.pos,
                    String.format("non-static %s cannot be referenced from a static context",
                            isCall ? "method " + ((MethodDecl) idDecl).signature : "variable " + idDecl.id.contents)));
            return ERR;
        }
        // all checks passed, declaration can be attached to reference
        qRef.decl = idDecl;

        return qRef.decl.type;
    }

    @Override
    public Type visitIdentifier(Identifier id, ContextTraversalState state, ConArg arg) {
        return null;
    }

    @Override
    public Type visitOperator(Operator op, ContextTraversalState state, ConArg arg) {
        return null;
    }

    @Override
    public Type visitIntLiteral(IntLiteral num, ContextTraversalState state, ConArg arg) {
        return new BaseType(TypeKind.INT, num.pos);
    }

    @Override
    public Type visitBooleanLiteral(BooleanLiteral bool, ContextTraversalState state, ConArg arg) {
        return new BaseType(TypeKind.BOOLEAN, bool.pos);
    }

    @Override
    public Type visitNullLiteral(NullLiteral nul, ContextTraversalState state, ConArg arg) {
        return new BaseType(TypeKind.NULL, nul.pos);
    }

    @Override
    public Type visitFloatLiteral(FloatLiteral num, ContextTraversalState state, ConArg arg) {
        return new BaseType(TypeKind.FLOAT, num.pos);
    }
}

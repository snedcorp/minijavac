package minijavac.gen;

import minijavac.ast.*;
import minijavac.gen.attribute.CodeAttribute;
import minijavac.gen.constant.ConstantPool;
import minijavac.gen.file.AccessFlag;
import minijavac.gen.file.ClassFile;
import minijavac.gen.file.FieldEntry;
import minijavac.gen.file.MethodEntry;
import minijavac.gen.instruction.ArrType;
import minijavac.gen.instruction.BranchInstruction;
import minijavac.gen.instruction.Instruction;
import minijavac.gen.instruction.OpCode;
import minijavac.syntax.TokenKind;

import java.util.List;

/**
 * <pre>
 * {@link Visitor} implementation that traverses the given {@link AST} to populate a {@link ClassFile} instance that can
 * then be used to generate valid Java bytecode.
 *
 * Uses a {@link GenTraversalState} instance to maintain state about the current traversal, and {@link GenArg} to
 * pass down contextual information to child nodes.
 * </pre>
 */
public class Generator implements Visitor<GenTraversalState, GenArg, Object> {

    public ClassFile gen(ClassDecl classDecl) {
        GenTraversalState state = new GenTraversalState();
        state.setClassFile(new ClassFile(classDecl.id.contents));
        classDecl.visit(this, state, GenArg.NONE);
        return state.getClassFile();
    }

    @Override
    public Object visitClassDecl(ClassDecl classDecl, GenTraversalState state, GenArg arg) {
        for (FieldDecl fieldDecl : classDecl.fieldDecls) {
            fieldDecl.visit(this, state, GenArg.NONE);
        }

        for (MethodDecl methodDecl : classDecl.methodDecls) {
            methodDecl.visit(this, state, GenArg.NONE);
        }

        state.getClassFile().addSourceFileAttribute(classDecl.pos.file());

        return null;
    }

    @Override
    public Object visitFieldDecl(FieldDecl fieldDecl, GenTraversalState state, GenArg arg) {
        ClassFile currClass = state.getClassFile();

        FieldEntry fieldEntry = new FieldEntry();
        fieldEntry.setAccessFlags(AccessFlag.mask(fieldDecl));

        fieldEntry.setNameIndex(currClass.getConstantPool().addUTFConstant(fieldDecl.id.contents));
        fieldEntry.setDescriptorIndex(currClass.getConstantPool().addUTFConstant(fieldDecl.type.descriptor()));

        currClass.getFields().add(fieldEntry);

        return null;
    }

    @Override
    public Object visitMethodDecl(MethodDecl methodDecl, GenTraversalState state, GenArg arg) {
        state.setCurrMethod(methodDecl);

        if (methodDecl.isConstructor()) {
            addConstructor(methodDecl, state);
            return null;
        }

        MethodEntry methodEntry = state.getClassFile().addMethod(methodDecl);

        for (Statement statement : methodDecl.statementList) {
            statement.visit(this, state, GenArg.NONE);
        }

        // for void methods, add return statement if none provided
        if (methodDecl.type.kind == TypeKind.VOID) {
            List<Instruction> instructions = methodEntry.getCodeAttribute().getCode();
            if (instructions.isEmpty() || instructions.get(instructions.size()-1).getOpCode() != OpCode._return) {
                methodEntry.getCodeAttribute().addInstruction(Instruction.of(OpCode._return));
            }
        }

        return null;
    }

    private void addConstructor(MethodDecl methodDecl, GenTraversalState state) {
        ClassFile currClass = state.getClassFile();

        ConstantPool constantPool = currClass.getConstantPool();
        currClass.addMethod(methodDecl);

        CodeAttribute code = currClass.getCode();

        // i.e. not a chaining constructor
        if (methodDecl.methodType == MethodType.CONSTRUCTOR) {
            String superClassName = "java/lang/Object";
            String methodName = "<init>";
            String superDescriptor = "()V";

            int superMethodIndex = constantPool
                    .addMethodRefConstant(superClassName, methodName, superDescriptor).getVal();

            code.pushArgCnt(0);
            code.addInstruction(Instruction.of(OpCode.aload_0));

            Instruction invokeSpecial = Instruction.builder()
                    .opCode(OpCode.invokespecial)
                    .constantIndex(superMethodIndex)
                    .build();
            code.addInstruction(invokeSpecial);
        }

        for (Statement statement : methodDecl.statementList) {
            statement.visit(this, state, GenArg.NONE);
        }

        code.addInstruction(Instruction.of(OpCode._return));
    }

    @Override
    public Object visitParameterDecl(ParameterDecl parameterDecl, GenTraversalState state, GenArg arg) {
        return null;
    }

    @Override
    public Object visitVarDecl(VarDecl decl, GenTraversalState state, GenArg arg) {
        return null;
    }

    @Override
    public Object visitBaseType(BaseType type, GenTraversalState state, GenArg arg) {
        return null;
    }

    @Override
    public Object visitClassType(ClassType type, GenTraversalState state, GenArg arg) {
        return null;
    }

    @Override
    public Object visitArrayType(ArrayType type, GenTraversalState state, GenArg arg) {
        return null;
    }

    @Override
    public Object visitBlockStmt(BlockStmt stmt, GenTraversalState state, GenArg arg) {
        for (Statement statement : stmt.statements) {
            statement.visit(this, state, GenArg.NONE);
        }
        return null;
    }

    @Override
    public Object visitVarDeclStmt(VarDeclStmt stmt, GenTraversalState state, GenArg arg) {
        // visit initializing expr, if exists
        if (stmt.expr != null) {
            stmt.expr.visit(this, state, GenArg.NONE);
        }

        // add to local variable table
        state.getCode().addVarDecl(stmt.decl, stmt.expr != null);

        return null;
    }

    @Override
    public Object visitAssignStmt(AssignStmt stmt, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();
        // local -> val, _store -> val then ref
        // field -> ref, val, putfield -> ref (cache putfield), val, flush putfield
        // static field -> val, putstatic -> val then ref
        // array -> ref, index, value, _astore -> ref (cache _astore), val, flush astore

        Declaration refDecl = stmt.ref.getDecl();
        if (!(stmt.ref instanceof IxRef) &&
                (refDecl instanceof LocalDecl || (refDecl instanceof FieldDecl && ((FieldDecl) refDecl).isStatic))) {
            if (stmt.operator.kind != TokenKind.ASSIGN) {
                stmt.ref.visit(this, state, GenArg.NONE);
                stmt.val.visit(this, state, GenArg.NONE);
                code.addInstruction(Instruction.of(getArithmeticOpCode(stmt)));
            } else {
                stmt.val.visit(this, state, GenArg.NONE);
            }
            stmt.ref.visit(this, state, GenArg.LHS);
            return null;
        }

        stmt.ref.visit(this, state, GenArg.LHS);
        if (stmt.operator.kind != TokenKind.ASSIGN) {
            if (stmt.ref instanceof IxRef) {
                // duplicate arrayref and index
                code.addInstruction(Instruction.of(OpCode.dup2));
                OpCode opCode = ((ArrayType) stmt.ref.getDecl().type).elementType.kind == TypeKind.FLOAT ?
                        OpCode.faload : OpCode.iaload;
                code.addInstruction(Instruction.of(opCode));
            } else {
                // duplicate objectref
                code.addInstruction(Instruction.of(OpCode.dup));
                Instruction getField = Instruction.builder()
                        .opCode(OpCode.getfield)
                        .constantIndex(code.peekBuffer().getConstantIndex())
                        .build();
                code.addInstruction(getField);
            }

            stmt.val.visit(this, state, GenArg.NONE);
            code.addInstruction(Instruction.of(getArithmeticOpCode(stmt)));
        } else {
            stmt.val.visit(this, state, GenArg.NONE);
        }
        // add buffered putfield or _astore instruction
        code.popBuffer();

        return null;
    }

    @Override
    public Object visitCallStmt(CallStmt stmt, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();
        stmt.methodRef.visit(this, state, GenArg.NONE);

        // pop unused return value from operand stack
        if (stmt.methodRef.getDecl().type.kind != TypeKind.VOID) {
            code.addInstruction(Instruction.of(OpCode.pop));
        }
        return null;
    }

    @Override
    public Object visitReturnStmt(ReturnStmt stmt, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();
        OpCode opCode;
        if (stmt.expr != null) {
            stmt.expr.visit(this, state, GenArg.NONE);
            opCode = switch (state.getCurrMethod().type.kind) {
                case INT, BOOLEAN -> OpCode.ireturn;
                case FLOAT -> OpCode.freturn;
                default -> OpCode.areturn;
            };
        } else {
            opCode = OpCode._return;
        }

        code.addInstruction(Instruction.of(opCode));
        return null;
    }

    @Override
    public Object visitIfStmt(IfStmt stmt, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();
        stmt.cond.visit(this, state, GenArg.NONE); // visit condition

        BranchInstruction cond = BranchInstruction.of(OpCode.ifeq); // branch if condition is false
        code.addInstruction(cond);

        code.enterScope();
        stmt.thenStmt.visit(this, state, GenArg.NONE); // visit "if" body
        code.exitScope();

        if (stmt.elseStmt != null) {
            code.enterScope();
            // here if condition is true, so branch past "else"
            BranchInstruction gotoPastElse = BranchInstruction.of(OpCode._goto);
            code.addInstruction(gotoPastElse);

            // if condition is false, branch to "else"
            code.setBranchOffsetAndSeekFrame(cond);
            stmt.elseStmt.visit(this, state, GenArg.NONE); // visit "else" body

            // set offset for branch past "else"
            code.setBranchOffsetAndSeekFrame(gotoPastElse);
            code.exitScope();
        } else { // if condition is false, branch past "if" body
            code.setBranchOffsetAndSeekFrame(cond);
        }
        return null;
    }

    @Override
    public Object visitWhileStmt(WhileStmt stmt, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();

        // add frame at condition's offset, will be target for later branching instructions
        int condOffset = code.addFrame();
        stmt.cond.visit(this, state, GenArg.NONE); // visit condition

        BranchInstruction cond = BranchInstruction.of(OpCode.ifeq); // branch if condition is false
        code.addInstruction(cond);
        code.enterLoopScope();

        stmt.body.visit(this, state, GenArg.NONE); // visit loop body

        CodeAttribute.LoopFlowInstructions loopFlowInstructions = code.exitLoopScope();
        // continue statements branch back to condition
        if (!loopFlowInstructions.continues().isEmpty()) {
            code.setBranchOffsets(loopFlowInstructions.continues(), condOffset);
        }

        BranchInstruction gotoCond = BranchInstruction.of(OpCode._goto);
        code.addInstruction(gotoCond);
        code.setBranchOffset(gotoCond, condOffset); // end of loop body, branch back to condition

        // break statements branch here, past loop (piggyback on frame seek from cond)
        if (!loopFlowInstructions.breaks().isEmpty()) {
            code.setBranchOffsets(loopFlowInstructions.breaks());
        }

        // if condition is false, branch here, past loop (and seek frame)
        code.setBranchOffsetAndSeekFrame(cond);

        return null;
    }

    @Override
    public Object visitDoWhileStmt(DoWhileStmt stmt, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();

        // add frame at body's offset, will be target for later branching instructions
        int bodyOffset = code.addFrame();
        code.enterLoopScope();

        stmt.body.visit(this, state, GenArg.NONE); // visit loop body

        CodeAttribute.LoopFlowInstructions loopFlowInstructions = code.exitLoopScope();
        int condOffset = code.addFrame(); // add frame at condition's offset

        // continue statements branch to here, the condition
        if (!loopFlowInstructions.continues().isEmpty()) {
            code.setBranchOffsets(loopFlowInstructions.continues(), condOffset);
        }

        stmt.cond.visit(this, state, GenArg.NONE); // visit condition

        BranchInstruction cond = BranchInstruction.of(OpCode.ifne);
        code.addInstruction(cond);
        code.setBranchOffset(cond, bodyOffset); // if condition is true, branch back to body

        // break statements branch to here, past the condition (and seek frame)
        if (!loopFlowInstructions.breaks().isEmpty()) {
            code.setBranchOffsetsAndSeekFrame(loopFlowInstructions.breaks());
        }

        return null;
    }

    @Override
    public Object visitExprStmt(ExprStatement stmt, GenTraversalState state, GenArg arg) {
        stmt.expr.visit(this, state, GenArg.EXPR_STMT);
        return null;
    }

    @Override
    public Object visitBreakStmt(BreakStmt stmt, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();
        BranchInstruction _goto = BranchInstruction.of(OpCode._goto);
        code.addInstruction(_goto);
        code.addBreak(_goto); // register break with current loop scope
        return null;
    }

    @Override
    public Object visitContinueStmt(ContinueStmt stmt, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();
        BranchInstruction _goto = BranchInstruction.of(OpCode._goto);
        code.addInstruction(_goto);
        code.addContinue(_goto); // register continue with current loop scope
        return null;
    }

    @Override
    public Object visitForStmt(ForStmt stmt, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();

        code.enterLoopScope();
        // visit initializing statement
        stmt.initStmt.visit(this, state, GenArg.NONE);

        // add frame at condition's offset, will be target for later branching instructions
        int condOffset = code.addFrame();
        stmt.cond.visit(this, state, GenArg.NONE); // visit condition

        BranchInstruction cond = BranchInstruction.of(OpCode.ifeq); // branch if condition is false
        code.addInstruction(cond);

        stmt.body.visit(this, state, GenArg.NONE); // visit body

        // add frame if continues exist (becomes branching target), get current offset regardless
        int updateOffset = !code.peekLoopFlow().continues().isEmpty() ? code.addFrame() : code.getOffset();
        stmt.updateStmt.visit(this, state, GenArg.NONE); // visit update statement

        CodeAttribute.LoopFlowInstructions loopFlowInstructions = code.exitLoopScope();
        // continue statements branch to the update statement
        for (BranchInstruction cont : loopFlowInstructions.continues()) {
            code.setBranchOffset(cont, updateOffset);
        }

        BranchInstruction gotoCond = BranchInstruction.of(OpCode._goto);
        code.addInstruction(gotoCond);
        code.setBranchOffset(gotoCond, condOffset); // end of loop body, branch back to condition

        // break statements branch here, past loop (piggyback on frame seek from cond)
        if (!loopFlowInstructions.breaks().isEmpty()) {
            code.setBranchOffsets(loopFlowInstructions.breaks());
        }

        // if condition is false, branch here, past loop (and seek frame)
        code.setBranchOffsetAndSeekFrame(cond);

        return null;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr expr, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();
        TokenKind op = expr.operator.kind;

        if (op == TokenKind.DECREMENT || op == TokenKind.INCREMENT) {
            expr.expr.visit(this, state, GenArg.FIX);
            generateFix((RefExpr) expr.expr, op == TokenKind.INCREMENT, true,
                    arg == GenArg.EXPR_STMT, state);
            return null;
        }

        expr.expr.visit(this, state, GenArg.NONE);

        if (op == TokenKind.MINUS) {
            OpCode opCode = switch (expr.type.kind) {
                case INT -> OpCode.ineg;
                case FLOAT -> OpCode.fneg;
                default -> throw new IllegalArgumentException("Unexpected value: " + expr.type.kind);
            };
            code.addInstruction(Instruction.of(opCode));
            return null;
        }

        if (op == TokenKind.COMPLEMENT) {
            code.addInstruction(Instruction.of(OpCode.iconst_m1));
            code.addInstruction(Instruction.of(OpCode.ixor));
            return null;
        }

        // to reach here, operator must be "!"
        BranchInstruction branch = BranchInstruction.of(OpCode.ifeq); // branch if value is false
        code.addInstruction(branch);
        code.addInstruction(Instruction.of(OpCode.iconst_0));

        // here if value is true, false has been pushed, now branch to skip past pushing true
        BranchInstruction _goto = BranchInstruction.of(OpCode._goto);
        code.addInstruction(_goto);

        // branch to here if value is false, true is then pushed
        code.setBranchOffsetAndSeekFrame(branch);
        code.addInstruction(Instruction.of(OpCode.iconst_1));

        // branch to here if value is true, false has been pushed
        code.setBranchOffsetAndSeekFrame(_goto);
        return null;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr expr, GenTraversalState state, GenArg arg) {
        TokenKind kind = expr.operator.kind;

        if (kind == TokenKind.AND || kind == TokenKind.OR) {
            visitLogicalOp(expr, state);
            return null;
        }

        if (kind.isComparisonOp()) {
            generateComparisonOp(expr, state);
            return null;
        }

        // if here, must be arithmetic
        expr.left.visit(this, state, GenArg.NONE);
        expr.right.visit(this, state, GenArg.NONE);

        state.getCode().addInstruction(Instruction.of(getArithmeticOpCode(expr)));
        return null;
    }

    private void visitLogicalOp(BinaryExpr expr, GenTraversalState state) {
        CodeAttribute code = state.getCode();

        expr.left.visit(this, state, GenArg.NONE); // visit left expr

        // branch past the right expr if short-circuit condition is true
        OpCode branchOpCode = expr.operator.kind == TokenKind.AND ? OpCode.ifeq : OpCode.ifne;
        BranchInstruction branch = BranchInstruction.of(branchOpCode);
        code.addInstruction(branch);

        expr.right.visit(this, state, GenArg.NONE); // visit right expr

        // right expr has been visited, branch past short-circuit result
        BranchInstruction _goto = BranchInstruction.of(OpCode._goto);
        code.addInstruction(_goto);

        /*
         * AND -> if left is false, branch past right expr to here, push false
         * OR -> if left is true, branch past right expr to here, push true
         */
        code.setBranchOffsetAndSeekFrame(branch);
        code.addInstruction(Instruction.of(expr.operator.kind == TokenKind.AND ? OpCode.iconst_0 : OpCode.iconst_1));

        // if short-circuit condition isn't true, will branch to here after right expr has been evaluated
        code.setBranchOffsetAndSeekFrame(_goto);
    }

    private void generateComparisonOp(BinaryExpr expr, GenTraversalState state) {
        TokenKind kind = expr.operator.kind;
        CodeAttribute code = state.getCode();

        OpCode branchOpCode = null;

        /*
        * Handle case where one side of the expression is a null literal.
        * Visit only the non-null side, then set the branching opcode accordingly
        * */
        if (kind == TokenKind.EQ || kind == TokenKind.NOT_EQ) {
            boolean singleNull = false;
            if (expr.leftType.kind == TypeKind.NULL && expr.rightType.kind != TypeKind.NULL) {
                expr.right.visit(this, state, GenArg.NONE);
                singleNull = true;
            } else if (expr.leftType.kind != TypeKind.NULL && expr.rightType.kind == TypeKind.NULL) {
                expr.left.visit(this, state, GenArg.NONE);
                singleNull = true;
            }
            if (singleNull) {
                branchOpCode = kind == TokenKind.EQ ? OpCode.ifnonnull : OpCode.ifnull;
            }
        }

        // if single null has already been detected, then skip
        if (branchOpCode == null) {
            expr.left.visit(this, state, GenArg.NONE); // visit left expr
            expr.right.visit(this, state, GenArg.NONE); // visit right expr

            /*
            * Note: operations get flipped when generating, so if original comparison fails, flipped comparison is true,
            * and therefore branches further ahead.
            * */

            boolean isFloat = expr.leftType.kind == TypeKind.FLOAT || expr.rightType.kind == TypeKind.FLOAT;
            if (isFloat) {
                OpCode cmpOpCode = OpCode.fcmpl;
                branchOpCode = switch(kind) {
                    case EQ -> OpCode.ifne;
                    case NOT_EQ -> OpCode.ifeq;
                    case LT -> {
                        cmpOpCode = OpCode.fcmpg;
                        yield OpCode.ifge;
                    }
                    case LTE -> {
                        cmpOpCode = OpCode.fcmpg;
                        yield OpCode.ifgt;
                    }
                    case GT -> OpCode.ifle;
                    case GTE -> OpCode.iflt;
                    default -> throw new IllegalArgumentException("Unexpected value: " + kind);
                };
                code.addInstruction(Instruction.of(cmpOpCode));
            } else {
                branchOpCode = switch (kind) {
                    case EQ -> OpCode.if_icmpne;
                    case NOT_EQ -> OpCode.if_icmpeq;
                    case LT -> OpCode.if_icmpge;
                    case LTE -> OpCode.if_icmpgt;
                    case GT -> OpCode.if_icmple;
                    case GTE -> OpCode.if_icmplt;
                    default -> throw new IllegalArgumentException("Unexpected value: " + kind);
                };
            }
        }

        // branch if (flipped) comparison is true
        BranchInstruction branch = BranchInstruction.of(branchOpCode);
        code.addInstruction(branch);

        // here if (flipped) comparison failed, push true
        code.addInstruction(Instruction.of(OpCode.iconst_1));
        BranchInstruction _goto = BranchInstruction.of(OpCode._goto);
        code.addInstruction(_goto);

        // branch here if (flipped) comparison is true, push false
        code.setBranchOffsetAndSeekFrame(branch);
        code.addInstruction(Instruction.of(OpCode.iconst_0));

        // branch here if (flipped) comparison is false, true has been pushed
        code.setBranchOffsetAndSeekFrame(_goto);
    }

    /**
     * @param expr binary expression
     * @return correctly typed opcode for the given binop
     */
    private OpCode getArithmeticOpCode(BinaryExpr expr) {
        TokenKind kind = expr.operator.kind;
        boolean isFloat = expr.leftType.kind == TypeKind.FLOAT || expr.rightType.kind == TypeKind.FLOAT;

        if (isFloat) return getFloatArithmeticOpCode(kind);
        return getIntArithmeticOpCode(kind);
    }

    /**
     * @param stmt assign statement
     * @return correctly typed opcode for the given compound assignment statement
     */
    private OpCode getArithmeticOpCode(AssignStmt stmt) {
        TokenKind kind = stmt.operator.kind.getBaseFromCompound();
        Type type = stmt.ref.getDecl().type;
        boolean isFloat = type.kind == TypeKind.FLOAT ||
                (type instanceof ArrayType arrayType && arrayType.elementType.kind == TypeKind.FLOAT);
        if (isFloat) {
            return getFloatArithmeticOpCode(kind);
        }
        return getIntArithmeticOpCode(kind);
    }

    /**
     * @param kind operator token kind
     * @return opcode for the given operator token kind, assuming an integer context
     */
    private OpCode getIntArithmeticOpCode(TokenKind kind) {
        return switch (kind) {
            case PLUS -> OpCode.iadd;
            case MINUS -> OpCode.isub;
            case MULTIPLY -> OpCode.imul;
            case DIVIDE -> OpCode.idiv;
            case MODULO -> OpCode.irem;
            case LSHIFT -> OpCode.ishl;
            case RSHIFT -> OpCode.ishr;
            case UN_RSHIFT -> OpCode.iushr;
            case BTW_AND -> OpCode.iand;
            case BTW_EXC_OR -> OpCode.ixor;
            case BTW_INC_OR -> OpCode.ior;
            default -> throw new IllegalArgumentException("Unexpected value: " + kind);
        };
    }

    /**
     * @param kind operator token kind
     * @return opcode for the given operator token kind, assuming a float context
     */
    private OpCode getFloatArithmeticOpCode(TokenKind kind) {
        return switch(kind) {
            case PLUS -> OpCode.fadd;
            case MINUS -> OpCode.fsub;
            case MULTIPLY -> OpCode.fmul;
            case DIVIDE -> OpCode.fdiv;
            case MODULO -> OpCode.frem;
            default -> throw new IllegalArgumentException("Unexpected value: " + kind);
        };
    }

    @Override
    public Object visitRefExpr(RefExpr expr, GenTraversalState state, GenArg arg) {
        expr.ref.visit(this, state, arg); // need to pass through for pre + postfix
        return null;
    }

    @Override
    public Object visitLiteralExpr(LiteralExpr expr, GenTraversalState state, GenArg arg) {
        expr.literal.visit(this, state, GenArg.NONE);
        return null;
    }

    @Override
    public Object visitNewObjectExpr(NewObjectExpr expr, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();
        ConstantPool constantPool = state.getConstantPool();
        String className = expr.classType.className.contents;

        int classIndex = constantPool.addClassConstant(className).getVal();
        Instruction _new = Instruction.builder()
                .opCode(OpCode._new)
                .constantIndex(classIndex)
                .build();
        code.addInstruction(_new);

        code.addInstruction(Instruction.of(OpCode.dup));

        code.pushArgCnt(expr.argList.size());
        for (Expression e : expr.argList) {
            e.visit(this, state, GenArg.NONE);
        }

        int refIndex = constantPool.addConstructorMethodRefConstant(expr.decl).getVal();
        Instruction invokeSpecial = Instruction.builder()
                .opCode(OpCode.invokespecial)
                .constantIndex(refIndex)
                .build();
        code.addInstruction(invokeSpecial);

        return null;
    }

    @Override
    public Object visitNewArrayExpr(NewArrayExpr expr, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();
        ConstantPool constantPool = state.getConstantPool();

        for (Expression sizeExpr : expr.sizeExprList) {
            sizeExpr.visit(this, state, GenArg.NONE);
        }

        if (expr.sizeExprList.size() > 1) {
            code.pushArgCnt(expr.sizeExprList.size());

            int classIndex = constantPool.addClassConstant(expr.arrayType.descriptor()).getVal();

            Instruction multiANewArray = Instruction.builder()
                    .opCode(OpCode.multianewarray)
                    .constantIndex(classIndex)
                    .operand(expr.arrayType.dims)
                    .build();
            code.addInstruction(multiANewArray);

            return null;
        }

        if (expr.elementType instanceof BaseType baseType) {
            int aType = ArrType.getCodeFromType(baseType);
            Instruction newarray = Instruction.builder()
                    .opCode(OpCode.newarray)
                    .operand(aType)
                    .build();
            code.addInstruction(newarray);
        } else if (expr.elementType instanceof ClassType classType) {
            String className = classType.className.contents;
            int classIndex = constantPool.addClassConstant(className).getVal();

            Instruction anewarray = Instruction.builder()
                    .opCode(OpCode.anewarray)
                    .constantIndex(classIndex)
                    .build();
            code.addInstruction(anewarray);
        }

        return null;
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr expr, GenTraversalState state, GenArg arg) {
        expr.expr.visit(this, state, GenArg.FIX);
        generateFix((RefExpr) expr.expr, expr.operator.kind == TokenKind.INCREMENT, false,
                arg == GenArg.EXPR_STMT, state);
        return null;
    }

    @Override
    public Object visitTernaryExpr(TernaryExpr expr, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();

        expr.cond.visit(this, state, GenArg.NONE); // visit condition

        BranchInstruction branch = BranchInstruction.of(OpCode.ifeq); // branch if condition is false
        code.addInstruction(branch);
        expr.expr1.visit(this, state, GenArg.NONE); // visit left expr

        BranchInstruction _goto  = BranchInstruction.of(OpCode._goto);
        code.addInstruction(_goto);
        code.setBranchOffsetAndSeekFrame(branch); // branch here, after the left expr, if condition is false (and seek frame)

        expr.expr2.visit(this, state, GenArg.NONE); // visit right expr
        // branch here, after the right expr, if condition is true and left expr has been evaluated (and seek frame)
        code.setBranchOffsetAndSeekFrame(_goto);
        return null;
    }

    @Override
    public Object visitNewArrayInitExpr(NewArrayInitExpr newInitExpr, GenTraversalState state, GenArg arg) {
        generateArrayInitExpr(newInitExpr.initExpr, newInitExpr.dims, newInitExpr.elementType, state,
                newInitExpr.arrayType.descriptor().substring(1), 1);

        return null;
    }

    /**
     * Recursively generates instructions for creating and then initializing a new array.
     * @param initExpr          expression list for current depth and location
     * @param dims              max number of dimensions
     * @param elementType       element type at furthest depth
     * @param state             state of ongoing traversal
     * @param arrTypeDescriptor array type descriptor for current depth, only used if depth < dims
     * @param depth             dimension depth of current invocation
     */
    private void generateArrayInitExpr(ArrayInitExpr initExpr, int dims, Type elementType, GenTraversalState state,
                                       String arrTypeDescriptor, int depth) {
        CodeAttribute code = state.getCode();

        int size = initExpr.exprList.size();
        generateInt(size, state); // push array length onto stack

        TypeKind typeKind = elementType.kind;

        OpCode storeCode;
        if (depth == dims && elementType instanceof BaseType baseType) {
            // base case at deepest depth, create new array from base type
            int aType = ArrType.getCodeFromType(baseType);
            Instruction newarray = Instruction.builder()
                    .opCode(OpCode.newarray)
                    .operand(aType)
                    .build();
            code.addInstruction(newarray);

            // store the correctly typed "_astore" instruction, to be used later when initializing elements at this depth
            storeCode = switch(typeKind) {
                case INT -> OpCode.iastore;
                case BOOLEAN -> OpCode.bastore;
                case FLOAT -> OpCode.fastore;
                default -> throw new IllegalArgumentException("Unexpected type " + typeKind);
            };
        } else {
            String className;
            // if at deepest depth, use class from element type
            if (depth == dims && elementType instanceof ClassType classType) {
                className = classType.className.contents;
            } else { // if not, use array type descriptor for current depth
                className = arrTypeDescriptor;
            }

            int classIndex = state.getConstantPool().addClassConstant(className).getVal();

            Instruction anewarray = Instruction.builder()
                    .opCode(OpCode.anewarray)
                    .constantIndex(classIndex)
                    .build();
            code.addInstruction(anewarray);

            storeCode = OpCode.aastore;
        }

        for (int i=0; i<initExpr.exprList.size(); i++) {
            Expression expr = initExpr.exprList.get(i);
            // duplicate arrayref from above, to allow for initialization
            code.addInstruction(Instruction.of(OpCode.dup));

            generateInt(i, state); // push index onto stack

            if (expr instanceof ArrayInitExpr childInitExpr) {
                /*
                * Not at deepest depth, so make recursive call with incremented depth, using the child init expr.
                * Also, chop off the leading [ from the array type descriptor.
                * */
                generateArrayInitExpr(childInitExpr, dims, elementType, state,
                        arrTypeDescriptor.substring(1), depth+1);
            } else {
                // At deepest depth, so just visit expression
                expr.visit(this, state, GenArg.NONE);
            }

            // arrayref, index, expr on stack now - so it's time to store expr at that index
            code.addInstruction(Instruction.of(storeCode));
        }
    }

    @Override
    public Object visitArrayInitExpr(ArrayInitExpr expr, GenTraversalState state, GenArg arg) {
        return null;
    }

    @Override
    public Object visitThisRef(ThisRef ref, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();
        code.addInstruction(Instruction.of(OpCode.aload_0));

        if (ref.getDecl() instanceof MethodDecl methodDecl) {
            int refIndex = state.getConstantPool().addConstructorMethodRefConstant(methodDecl).getVal();

            Instruction invokeSpecial = Instruction.builder()
                        .opCode(OpCode.invokespecial)
                        .constantIndex(refIndex)
                        .build();
            code.pushBuffer(invokeSpecial);
        }
        return null;
    }

    @Override
    public Object visitIdRef(IdRef ref, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();

        if (ref.getDecl() instanceof LocalDecl localDecl) {
            int index = localDecl.getLocalVarIndex();

            Instruction.Builder builder = Instruction.builder();
            if (localDecl.type instanceof BaseType) {
                if (localDecl.type.kind == TypeKind.FLOAT) {
                    if (arg == GenArg.LHS) builder.fstore(index);
                    else builder.fload(index);
                } else {
                    if (arg == GenArg.LHS) builder.istore(index);
                    else builder.iload(index);
                }
            } else if (localDecl.type instanceof ClassType || localDecl.type instanceof ArrayType) {
                if (arg == GenArg.LHS) builder.astore(index);
                else builder.aload(index);
            }

            if (arg == GenArg.FIX) {
                code.pushBuffer(builder.build());
            } else {
                code.addInstruction(builder.build());
            }
            return null;
        }

        if (ref.getDecl() instanceof FieldDecl fieldDecl) {
            generateFieldDeclRef(fieldDecl, state, arg);
            return null;
        }

        // create invoke instruction for call ref, buffer til after args are pushed
        if (ref.getDecl() instanceof MethodDecl methodDecl) {
            int index = state.getConstantPool().addMethodRefConstant(methodDecl).getVal();
            if (methodDecl.isStatic) {
                Instruction invokestatic = Instruction.builder()
                        .opCode(OpCode.invokestatic)
                        .constantIndex(index)
                        .build();
                code.pushBuffer(invokestatic);
            } else {
                code.addInstruction(Instruction.of(OpCode.aload_0));
                Instruction invokevirtual = Instruction.builder()
                        .opCode(OpCode.invokevirtual)
                        .constantIndex(index)
                        .build();
                code.pushBuffer(invokevirtual);
            }
            return null;
        }
        return null;
    }

    @Override
    public Object visitIxRef(IxRef ref, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();

        // visit reference
        if (ref.ref instanceof IdRef) {
            if (ref.getDecl() instanceof LocalDecl localDecl) {
                code.addInstruction(Instruction.builder().aload(localDecl.getLocalVarIndex()).build());
            } else if (ref.getDecl() instanceof FieldDecl fieldDecl) {
                // always get (then ixExpr, val, _astore) for arrays, never put
                generateFieldDeclRef(fieldDecl, state, GenArg.NONE);
            }
        } else {
            ref.ref.visit(this, state, GenArg.NONE);
        }

        /*
         * Three cases:
         *   - single dim
         *   - multi dim - fewer than max ix expr
         *   - multi dim - max ix expr
         */

        int dims = ((ArrayType) ref.getDecl().type).dims;
        int ixSize = ref.ixExprList.size();

        // visit all ix expr until the last, aaload each to index further into intermediate arrays
        int lastIndex = 0;
        if (ixSize > 1) {
            lastIndex = ref.ixExprList.size()-1;
            for (int i=0; i<lastIndex; i++) {
                ref.ixExprList.get(i).visit(this, state, GenArg.NONE);
                code.addInstruction(Instruction.of(OpCode.aaload));
            }
        }

        // visit last ix expr
        ref.ixExprList.get(lastIndex).visit(this, state, GenArg.NONE);

        // duplicate arrayref and index for postfix/prefix expression
        if (arg == GenArg.FIX) {
            code.addInstruction(Instruction.of(OpCode.dup2));
        }

        Instruction instruction;
        if (dims > 1 && ixSize < dims) { // if multidim array hasn't been fully indexed, has to be aaload
            instruction = Instruction.of(arg == GenArg.LHS ? OpCode.aastore : OpCode.aaload);
        } else { // otherwise choose based off element type
            ArrayType arrayType = (ArrayType) ref.getDecl().type;
            instruction = switch (arrayType.elementType.kind) {
                case INT -> Instruction.of(arg == GenArg.LHS ? OpCode.iastore : OpCode.iaload);
                case BOOLEAN -> Instruction.of(arg == GenArg.LHS ? OpCode.bastore : OpCode.baload);
                case FLOAT -> Instruction.of(arg == GenArg.LHS ? OpCode.fastore : OpCode.faload);
                default -> Instruction.of(arg == GenArg.LHS ? OpCode.aastore : OpCode.aaload);
            };
        }

        if (arg == GenArg.LHS) {
            code.pushBuffer(instruction);
        } else {
            code.addInstruction(instruction);
        }
        return null;
    }

    @Override
    public Object visitCallRef(CallRef callRef, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();
        callRef.ref.visit(this, state, GenArg.NONE);

        code.pushArgCnt(callRef.argList.size());
        for (Expression e : callRef.argList) {
            e.visit(this, state, GenArg.NONE);
        }

        code.popBuffer(); // add buffered invoke instruction
        return null;
    }

    @Override
    public Object visitQualRef(QualRef ref, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();

        if (ref.getDecl() instanceof MethodDecl methodDecl) {
            int methodRefIndex = state.getConstantPool().addMethodRefConstant(methodDecl).getVal();

            if (ref.ref.isStatic || methodDecl.isStatic) {
                // if static context, don't need to visit ref
                // if ref is instance but id is static -> javac pushes the getfield then pops - why?
                Instruction invokestatic = Instruction.builder()
                        .opCode(OpCode.invokestatic)
                        .constantIndex(methodRefIndex)
                        .build();
                code.pushBuffer(invokestatic);
            } else {
                ref.ref.visit(this, state, GenArg.NONE);

                Instruction invokevirtual = Instruction.builder()
                        .opCode(OpCode.invokevirtual)
                        .constantIndex(methodRefIndex)
                        .build();
                code.pushBuffer(invokevirtual);
            }
            return null;
        }

        if (ref.ref.getDecl().type instanceof ArrayType && ref.id.contents.equals("length")) {
            ref.ref.visit(this, state, GenArg.NONE);
            code.addInstruction(Instruction.of(OpCode.arraylength));
            return null;
        }

        FieldDecl fieldDecl = (FieldDecl) ref.getDecl();
        int fieldRefIndex = state.getConstantPool().addFieldRefConstant(fieldDecl).getVal();

        if (ref.ref.isStatic || fieldDecl.isStatic) {
            // if static context, don't need to visit ref
            Instruction instruction = Instruction.builder()
                    .opCode(arg == GenArg.LHS ? OpCode.putstatic : OpCode.getstatic)
                    .constantIndex(fieldRefIndex)
                    .build();
            code.addInstruction(instruction);
        } else {
            // only gets to the left, so make sure to not pass LHS down
            ref.ref.visit(this, state, GenArg.NONE);

            if (arg == GenArg.FIX) {
                code.addInstruction(Instruction.of(OpCode.dup));
            }

            Instruction instruction = Instruction.builder()
                    .opCode(arg == GenArg.LHS ? OpCode.putfield : OpCode.getfield)
                    .constantIndex(fieldRefIndex)
                    .build();

            if (arg == GenArg.LHS) {
                code.pushBuffer(instruction);
            } else {
                code.addInstruction(instruction);
            }

        }
        return null;
    }

    @Override
    public Object visitIdentifier(Identifier id, GenTraversalState state, GenArg arg) {
        return null;
    }

    @Override
    public Object visitOperator(Operator op, GenTraversalState state, GenArg arg) {
        return null;
    }

    @Override
    public Object visitIntLiteral(IntLiteral numLit, GenTraversalState state, GenArg arg) {
        int num = Integer.parseInt(numLit.contents); // NumberFormatException
        generateInt(num, state);
        return null;
    }

    /**
     * Generates the most space-efficient instruction possible for pushing the given integer literal onto the operand
     * stack.
     * @param num integer literal
     */
    private void generateInt(int num, GenTraversalState state) {
        Instruction instruction;
        if (num <= Byte.MAX_VALUE && num >= Byte.MIN_VALUE) {
            if (num >= 0 && num <= 5) {
                OpCode opCode = switch (num) {
                    case 0 -> OpCode.iconst_0;
                    case 1 -> OpCode.iconst_1;
                    case 2 -> OpCode.iconst_2;
                    case 3 -> OpCode.iconst_3;
                    case 4 -> OpCode.iconst_4;
                    case 5 -> OpCode.iconst_5;
                    default -> throw new IllegalArgumentException("Unexpected value: " + num);
                };
                instruction = Instruction.of(opCode);
            } else {
                instruction = Instruction.builder()
                        .opCode(OpCode.bipush)
                        .operand(num)
                        .build();
            }
        } else if (num <= Short.MAX_VALUE && num >= Short.MIN_VALUE) {
            instruction = Instruction.builder()
                    .opCode(OpCode.sipush)
                    .operand(num >> 8)
                    .operand(num)
                    .build();
        } else {
            int numConstantIndex = state.getConstantPool().addIntConstant(num).getVal();
            instruction = Instruction.builder()
                    .opCode(OpCode.ldc)
                    .operand(numConstantIndex) // TODO: only works if index <= 256, add ldc_w instruction
                    .build();
        }

        state.getCode().addInstruction(instruction);
    }

    @Override
    public Object visitBooleanLiteral(BooleanLiteral bool, GenTraversalState state, GenArg arg) {
        OpCode opCode = bool.kind == TokenKind.TRUE ? OpCode.iconst_1 : OpCode.iconst_0;
        state.getCode().addInstruction(Instruction.of(opCode));
        return null;
    }

    @Override
    public Object visitNullLiteral(NullLiteral nul, GenTraversalState state, GenArg arg) {
        state.getCode().addInstruction(Instruction.of(OpCode.aconst_null));
        return null;
    }

    @Override
    public Object visitFloatLiteral(FloatLiteral numLit, GenTraversalState state, GenArg arg) {
        Instruction instruction;
        float f = Float.parseFloat(numLit.contents);
        // Attempt to use one of the built-in float constants, otherwise have to add to constant pool and load
        if (f == 0.0) {
            instruction = Instruction.of(OpCode.fconst_0);
        } else if (f == 1.0) {
            instruction = Instruction.of(OpCode.fconst_1);
        } else if (f == 2.0) {
            instruction = Instruction.of(OpCode.fconst_2);
        } else {
            int numConstantIndex = state.getConstantPool().addFloatConstant(f).getVal();
            instruction = Instruction.builder()
                    .opCode(OpCode.ldc)
                    .operand(numConstantIndex)
                    .build();
        }

        state.getCode().addInstruction(instruction);
        return null;
    }

    /**
     * Generates instructions for a field reference.
     * @param fieldDecl
     * @param arg
     */
    private void generateFieldDeclRef(FieldDecl fieldDecl, GenTraversalState state, GenArg arg) {
        CodeAttribute code = state.getCode();

        int fieldRefIndex = state.getConstantPool().addFieldRefConstant(fieldDecl).getVal();
        if (fieldDecl.isStatic) {
            Instruction instruction = Instruction.builder()
                    .opCode(arg == GenArg.LHS ? OpCode.putstatic : OpCode.getstatic)
                    .constantIndex(fieldRefIndex)
                    .build();
            code.addInstruction(instruction);
        } else {
            code.addInstruction(Instruction.of(OpCode.aload_0));
            if (arg == GenArg.FIX) {
                code.addInstruction(Instruction.of(OpCode.dup));
            }
            Instruction instruction = Instruction.builder()
                    .opCode(arg == GenArg.LHS ? OpCode.putfield : OpCode.getfield)
                    .constantIndex(fieldRefIndex)
                    .build();
            if (arg == GenArg.LHS) {
                code.pushBuffer(instruction);
            } else {
                code.addInstruction(instruction);
            }
        }
    }

    /**
     * Generates prefix or postfix expression for the given reference.
     * @param refExpr expression referencing the variable to be incremented/decremented
     * @param inc     true if incrementing, false if decrementing
     * @param pre     true if prefix, false if postfix
     * @param stmt    true if within expression statement
     */
    private void generateFix(RefExpr refExpr, boolean inc, boolean pre, boolean stmt, GenTraversalState state) {
        CodeAttribute code = state.getCode();
        Declaration refDecl = refExpr.ref.getDecl();
        Type type = refDecl.type;

        if (type instanceof ArrayType arrayType) {
            // calculate new value and duplicate either original or new value, if necessary
            generateFix(arrayType.elementType, inc, pre, stmt, OpCode.dup_x2, state);
            // store new value to array
            OpCode opCode = arrayType.elementType.kind == TypeKind.FLOAT ? OpCode.fastore : OpCode.iastore;
            code.addInstruction(Instruction.of(opCode));
            return;
        }

        if (refDecl instanceof LocalDecl) {
            if (type.kind == TypeKind.INT) {
                // use buffered load instruction to create iinc instruction
                Instruction load = code.peekBuffer();
                Instruction iinc = Instruction.builder()
                        .opCode(OpCode.iinc)
                        .operand(load.getLocalIndex())
                        .operand(inc ? 1 : -1)
                        .build();

                // iinc before load (if needed)
                if (stmt || pre) {
                    code.addInstruction(iinc);
                }
                // no load needed, short circuit
                if (stmt) return;
                // add load instruction
                code.popBuffer();
                // iinc after load for postfix
                if (!pre) code.addInstruction(iinc);
            } else if (type.kind == TypeKind.FLOAT) {
                Instruction load = code.peekBuffer();
                // add load instruction
                code.popBuffer();
                // calculate new value and duplicate either original or new value, if necessary
                generateFix(type, inc, pre, stmt, OpCode.dup, state);
                // store calculated value, use index from load
                code.addInstruction(Instruction.builder().fstore(load.getLocalIndex()).build());
            }
            return;
        }

        if (refDecl instanceof FieldDecl fieldDecl) {
            // get field index from last getfield or getstatic instruction
            int index = code.getLastInstruction().getConstantIndex();
            // calculate new value and duplicate either original or new value, if necessary
            generateFix(type, inc, pre, stmt, fieldDecl.isStatic ? OpCode.dup : OpCode.dup_x1, state);
            // store calculated value
            code.addInstruction(Instruction.builder()
                    .opCode(fieldDecl.isStatic ? OpCode.putstatic : OpCode.putfield)
                    .constantIndex(index).build());
        }
    }

    /**
     * Increments or decrements the original value of the reference and duplicates either the original value or the new
     * value, if necessary.
     * @param type    base numeric type of the reference being operated upon
     * @param inc     true if incrementing, false if decrementing
     * @param pre     true if prefix, false if postfix
     * @param stmt    true if within expression statement
     * @param dupCode opcode for potential duplication instruction
     */
    private void generateFix(Type type, boolean inc, boolean pre, boolean stmt, OpCode dupCode,
                             GenTraversalState state) {
        CodeAttribute code = state.getCode();
        // duplicate original value if postfix and not expr_stmt - so enclosing context has it to work with
        if (!stmt && !pre) {
            code.addInstruction(Instruction.of(dupCode));
        }

        // calculate new value
        if (type.kind == TypeKind.INT) {
            code.addInstruction(Instruction.of(OpCode.iconst_1));
            code.addInstruction(Instruction.of(inc ? OpCode.iadd : OpCode.isub));
        } else if (type.kind == TypeKind.FLOAT) {
            code.addInstruction(Instruction.of(OpCode.fconst_1));
            code.addInstruction(Instruction.of(inc ? OpCode.fadd : OpCode.fsub));
        }

        // duplicate calculated value if prefix and not expr_stmt - so enclosing context has it to work with
        if (!stmt && pre) {
            code.addInstruction(Instruction.of(dupCode));
        }
    }
}

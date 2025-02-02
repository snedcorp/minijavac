package minijavac.unit;

import minijavac.ast.*;
import minijavac.syntax.Token;
import minijavac.syntax.TokenKind;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ASTUtils {

    public static void classes(List<ClassDecl> classDecls, List<String> classNames) {
        assertEquals(classNames.size(), classDecls.size());
        for (int i = 0; i < classDecls.size(); i++) {
            assertEquals(classNames.get(i), classDecls.get(i).id.contents);
        }
    }

    public static void clazz(ClassDecl classDecl, String name, List<Asserter<FieldDecl>> fieldAsserters,
                       List<Asserter<MethodDecl>> methodAsserters) {
        assertEquals(name, classDecl.id.contents);

        if (fieldAsserters == null) {
            assertEquals(0, classDecl.fieldDecls.size());
        } else {
            assertEquals(fieldAsserters.size(), classDecl.fieldDecls.size());
            for (int i = 0; i<classDecl.fieldDecls.size(); i++) {
                fieldAsserters.get(i).assertIt(classDecl.fieldDecls.get(i));
            }
        }

        if (methodAsserters == null) {
            assertEquals(0, classDecl.methodDecls.size());
        } else {
            assertEquals(methodAsserters.size(), classDecl.methodDecls.size());
            for (int i = 0; i<classDecl.methodDecls.size(); i++) {
                methodAsserters.get(i).assertIt(classDecl.methodDecls.get(i));
            }
        }
    }

    public static Asserter<FieldDecl> field(Access access, boolean isStatic, Type type, String name) {
        return (FieldDecl field) -> {
            assertEquals(access, field.access);
            assertEquals(isStatic, field.isStatic);
            type(type, field.type);
            assertEquals(name, field.id.contents);
        };
    }

    public static Asserter<MethodDecl> method(Access access, boolean isStatic, Type type, String name,
                                              List<Asserter<ParameterDecl>> paramAsserters, List<Asserter<Statement>> stmtAsserters) {
        return (MethodDecl method) -> {
            assertEquals(access, method.access);
            assertEquals(isStatic, method.isStatic);
            type(type, method.type);
            assertEquals(name, method.id.contents);

            if (paramAsserters == null) {
                assertEquals(0, method.parameterDeclList.size());
            } else {
                assertEquals(paramAsserters.size(), method.parameterDeclList.size());
                for (int i=0; i<method.parameterDeclList.size(); i++) {
                    paramAsserters.get(i).assertIt(method.parameterDeclList.get(i));
                }
            }

            if (stmtAsserters == null) {
                assertEquals(0, method.statementList.size());
            } else {
                assertEquals(stmtAsserters.size(), method.statementList.size());
                for (int i=0; i<method.statementList.size(); i++) {
                    stmtAsserters.get(i).assertIt(method.statementList.get(i));
                }
            }
        };
    }

    public static Asserter<ParameterDecl> param(Type type, String name) {
        return (ParameterDecl param) -> {
            type(type, param.type);
            assertEquals(name, param.id.contents);
        };
    }

    public static Type intType() {
        return new BaseType(TypeKind.INT, null);
    }

    public static Type floatType() {
        return new BaseType(TypeKind.FLOAT, null);
    }

    public static Type boolType() {
        return new BaseType(TypeKind.BOOLEAN, null);
    }

    public static Type voidType() {
        return new BaseType(TypeKind.VOID, null);
    }

    public static Type classType(String name) {
        return new ClassType(new Identifier(new Token(TokenKind.IDENTIFIER, name, null)), null);
    }

    public static Type arrayType(Type type, int dims) {
        return new ArrayType(type, null, dims);
    }

    public static void type(Type exp, Type actual) {
        if (exp instanceof BaseType) {
            assertTrue(actual instanceof BaseType);
            assertEquals(exp.kind, actual.kind);
        } else if (exp instanceof ClassType expC) {
            assertTrue(actual instanceof ClassType);
            assertEquals(expC.className.contents, ((ClassType) actual).className.contents);
        } else if (exp instanceof ArrayType expA) {
            assertTrue(actual instanceof ArrayType);
            ArrayType arrayType = (ArrayType) actual;
            type(expA.elementType, arrayType.elementType);
            assertEquals(expA.dims, arrayType.dims);
        }
    }

    public static Asserter<Statement> varDeclStmt(Type type, String name, Asserter<Expression> exprAsserter) {
        return (Statement statement) -> {
            assertTrue(statement instanceof VarDeclStmt);
            VarDeclStmt vds = (VarDeclStmt) statement;
            type(type, vds.decl.type);
            assertEquals(name, vds.decl.id.contents);
            if (exprAsserter == null) {
                assertNull(vds.expr);
            } else {
                exprAsserter.assertIt(vds.expr);
            }
        };
    }

    public static Asserter<Statement> blockStmt(List<Asserter<Statement>> stmts) {
        return (Statement statement) -> {
            assertTrue(statement instanceof BlockStmt);
            BlockStmt blockStmt = (BlockStmt) statement;
            if (stmts == null) {
                assertEquals(0, blockStmt.statements.size());
            } else {
                assertEquals(stmts.size(), blockStmt.statements.size());
                for (int i = 0; i<blockStmt.statements.size(); i++) {
                    stmts.get(i).assertIt(blockStmt.statements.get(i));
                }
            }
        };
    }

    public static Asserter<Statement> assignStmt(Asserter<Reference> refAsserter, Asserter<Expression> exprAsserter) {
        return (Statement statement) -> {
            assertTrue(statement instanceof AssignStmt);
            AssignStmt as = (AssignStmt) statement;
            refAsserter.assertIt(as.ref);
            assertEquals(TokenKind.ASSIGN, as.operator.kind);
            exprAsserter.assertIt(as.val);
        };
    }

    public static Asserter<Statement> assignStmt(Asserter<Reference> refAsserter, TokenKind op, Asserter<Expression> exprAsserter) {
        return (Statement statement) -> {
            assertTrue(statement instanceof AssignStmt);
            AssignStmt as = (AssignStmt) statement;
            refAsserter.assertIt(as.ref);
            assertEquals(op, as.operator.kind);
            exprAsserter.assertIt(as.val);
        };
    }

    public static Asserter<Statement> returnStmt(Asserter<Expression> exprAsserter) {
        return (Statement statement) -> {
            assertTrue(statement instanceof ReturnStmt);
            ReturnStmt ret = (ReturnStmt) statement;
            if (exprAsserter == null) {
                assertNull(ret.expr);
            } else {
                exprAsserter.assertIt(ret.expr);
            }
        };
    }

    public static Asserter<Statement> callStmt(Asserter<Reference> refAsserter) {
        return (Statement statement) -> {
            assertTrue(statement instanceof CallStmt);
            CallStmt callStmt = (CallStmt) statement;
            refAsserter.assertIt(callStmt.methodRef);
        };
    }

    public static Asserter<Statement> ifStmt(Asserter<Expression> exprAsserter, Asserter<Statement> stmtAsserter,
                                                     Asserter<Statement> elseStmtAsserter) {
        return (Statement statement) -> {
            assertTrue(statement instanceof IfStmt);
            IfStmt ifStmt = (IfStmt) statement;
            exprAsserter.assertIt(ifStmt.cond);
            stmtAsserter.assertIt(ifStmt.thenStmt);
            if (elseStmtAsserter == null) {
                assertNull(ifStmt.elseStmt);
            } else {
                elseStmtAsserter.assertIt(ifStmt.elseStmt);
            }
        };
    }

    public static Asserter<Statement> whileStmt(Asserter<Expression> exprAsserter, Asserter<Statement> stmtAsserter) {
        return (Statement statement) -> {
            assertTrue(statement instanceof WhileStmt);
            WhileStmt whileStmt = (WhileStmt) statement;
            exprAsserter.assertIt(whileStmt.cond);
            stmtAsserter.assertIt(whileStmt.body);
        };
    }

    public static Asserter<Statement> doWhileStmt(Asserter<Expression> exprAsserter, Asserter<Statement> stmtAsserter) {
        return (Statement statement) -> {
            assertTrue(statement instanceof DoWhileStmt);
            DoWhileStmt doWhileStmt = (DoWhileStmt) statement;
            exprAsserter.assertIt(doWhileStmt.cond);
            stmtAsserter.assertIt(doWhileStmt.body);
        };
    }

    public static Asserter<Statement> exprStmt(Asserter<Expression> exprAsserter) {
        return (Statement statement) -> {
            assertTrue(statement instanceof ExprStatement);
            ExprStatement exprStmt = (ExprStatement) statement;
            exprAsserter.assertIt(exprStmt.expr);
        };
    }

    public static Asserter<Statement> breakStmt() {
        return (Statement statement) -> {
            assertTrue(statement instanceof BreakStmt);
        };
    }

    public static Asserter<Statement> continueStmt() {
        return (Statement statement) -> {
            assertTrue(statement instanceof ContinueStmt);
        };
    }

    public static Asserter<Statement> forStmt(Asserter<Statement> initAsserter, Asserter<Expression> condAsserter,
                                              Asserter<Statement> updateAsserter, Asserter<Statement> bodyAsserter) {
        return (Statement statement) -> {
            assertTrue(statement instanceof ForStmt);
            ForStmt forStmt = (ForStmt) statement;
            initAsserter.assertIt(forStmt.initStmt);
            condAsserter.assertIt(forStmt.cond);
            updateAsserter.assertIt(forStmt.updateStmt);
            bodyAsserter.assertIt(forStmt.body);
        };
    }

    public static Asserter<Reference> idRef(String id) {
        return (Reference ref) -> {
            assertTrue(ref instanceof IdRef);
            IdRef idRef = (IdRef) ref;
            assertEquals(id, idRef.id.contents);
        };
    }

    public static Asserter<Reference> ixRef(Asserter<Reference> refAsserter, List<Asserter<Expression>> exprAsserters) {
        return (Reference ref) -> {
            assertTrue(ref instanceof IxRef);
            IxRef ixRef = (IxRef) ref;
            refAsserter.assertIt(ixRef.ref);

            assertEquals(exprAsserters.size(), ixRef.ixExprList.size());
            for (int i=0; i<ixRef.ixExprList.size(); i++) {
                exprAsserters.get(i).assertIt(ixRef.ixExprList.get(i));
            }
        };
    }

    public static Asserter<Reference> callRef(Asserter<Reference> refAsserter, List<Asserter<Expression>> argAsserters) {
        return (Reference ref) -> {
            assertTrue(ref instanceof CallRef);
            CallRef callRef = (CallRef) ref;
            refAsserter.assertIt(callRef.ref);
            if (argAsserters == null) {
                assertEquals(0, callRef.argList.size());
            } else {
                assertEquals(argAsserters.size(), callRef.argList.size());
                for (int i=0; i<callRef.argList.size(); i++) {
                    argAsserters.get(i).assertIt(callRef.argList.get(i));
                }
            }
        };
    }

    public static Asserter<Reference> thisRef() {
        return (Reference ref) -> {
            assertTrue(ref instanceof ThisRef);
        };
    }

    public static Asserter<Reference> qRef(Asserter<Reference> refAsserter, String id) {
        return (Reference ref) -> {
            assertTrue(ref instanceof QualRef);
            QualRef qRef = (QualRef) ref;
            refAsserter.assertIt(qRef.ref);
            assertEquals(id, qRef.id.contents);
        };
    }

    public static Asserter<Expression> refExpr(Asserter<Reference> refAsserter) {
        return (Expression expr) -> {
            assertTrue(expr instanceof RefExpr);
            RefExpr refExpr = (RefExpr) expr;
            refAsserter.assertIt(refExpr.ref);
        };
    }

    public static Asserter<Expression> newObjectExpr(String name, List<Asserter<Expression>> argAsserters) {
        return (Expression expr) -> {
            assertTrue(expr instanceof NewObjectExpr);
            NewObjectExpr nex = (NewObjectExpr) expr;
            assertEquals(name, nex.classType.className.contents);

            if (argAsserters == null) {
                assertEquals(0, nex.argList.size());
            } else {
                assertEquals(argAsserters.size(), nex.argList.size());
                for (int i=0; i<nex.argList.size(); i++) {
                    argAsserters.get(i).assertIt(nex.argList.get(i));
                }
            }
        };
    }

    public static Asserter<Expression> newArrayExpr(Type type, List<Asserter<Expression>> sizeExpAsserters) {
        return (Expression expr) -> {
            assertTrue(expr instanceof NewArrayExpr);
            NewArrayExpr nax = (NewArrayExpr) expr;
            type(type, nax.elementType);

            assertEquals(sizeExpAsserters.size(), nax.sizeExprList.size());
            for (int i=0; i<nax.sizeExprList.size(); i++) {
                sizeExpAsserters.get(i).assertIt(nax.sizeExprList.get(i));
            }
        };
    }

    public static Asserter<Expression> newArrayInitExpr(Type type, int dims, Asserter<Expression> initAsserter) {
        return (Expression expr) -> {
            assertTrue(expr instanceof NewArrayInitExpr);
            NewArrayInitExpr newExpr = (NewArrayInitExpr) expr;
            type(type, newExpr.elementType);

            assertEquals(dims, newExpr.dims);
            initAsserter.assertIt(newExpr.initExpr);
        };
    }

    public static Asserter<Expression> arrayInitExpr(List<Asserter<Expression>> expAsserters) {
        return (Expression expr) -> {
            assertTrue(expr instanceof ArrayInitExpr);
            ArrayInitExpr initExpr = (ArrayInitExpr) expr;

            assertEquals(expAsserters.size(), initExpr.exprList.size());
            for (int i=0; i<initExpr.exprList.size(); i++) {
                expAsserters.get(i).assertIt(initExpr.exprList.get(i));
            }
        };
    }

    public static Asserter<Expression> intLit(int lit) {
        return (Expression expr) -> {
            assertTrue(expr instanceof LiteralExpr);
            LiteralExpr lex = (LiteralExpr) expr;
            assertTrue(lex.literal instanceof IntLiteral);
            assertEquals(TokenKind.NUM, lex.literal.kind);
            assertEquals(lit, Integer.valueOf(lex.literal.contents));
        };
    }

    public static Asserter<Expression> floatLit(float lit) {
        return (Expression expr) -> {
            assertTrue(expr instanceof LiteralExpr);
            LiteralExpr lex = (LiteralExpr) expr;
            assertTrue(lex.literal instanceof FloatLiteral);
            assertEquals(TokenKind.FLOAT_NUM, lex.literal.kind);
            assertEquals(lit, Float.parseFloat(lex.literal.contents));
        };
    }

    public static Asserter<Expression> boolLit(boolean lit) {
        return (Expression expr) -> {
            assertTrue(expr instanceof LiteralExpr);
            LiteralExpr lex = (LiteralExpr) expr;
            assertTrue(lex.literal instanceof BooleanLiteral);
            if (lit) {
                assertEquals(TokenKind.TRUE, lex.literal.kind);
            } else {
                assertEquals(TokenKind.FALSE, lex.literal.kind);
            }
        };
    }

    public static Asserter<Expression> nullLit() {
        return (Expression expr) -> {
            assertTrue(expr instanceof LiteralExpr);
            LiteralExpr lex = (LiteralExpr) expr;
            assertTrue(lex.literal instanceof NullLiteral);
        };
    }

    public static Asserter<Expression> binop(Asserter<Expression> exprAsserter, TokenKind op, Asserter<Expression> exprAsserter2) {
        return (Expression expr) -> {
            assertTrue(expr instanceof BinaryExpr);
            BinaryExpr binop = (BinaryExpr) expr;
            exprAsserter.assertIt(binop.left);
            assertEquals(op, binop.operator.kind);
            exprAsserter2.assertIt(binop.right);
        };
    }

    public static Asserter<Expression> unop(TokenKind op, Asserter<Expression> exprAsserter) {
        return (Expression expr) -> {
            assertTrue(expr instanceof UnaryExpr);
            UnaryExpr unop = (UnaryExpr) expr;
            assertEquals(op, unop.operator.kind);
            exprAsserter.assertIt(unop.expr);
        };
    }

    public static Asserter<Expression> postfix(TokenKind op, Asserter<Expression> exprAsserter) {
        return (Expression expr) -> {
            assertTrue(expr instanceof PostfixExpr);
            PostfixExpr post = (PostfixExpr) expr;
            assertEquals(op, post.operator.kind);
            exprAsserter.assertIt(post.expr);
        };
    }

    public static Asserter<Expression> ternary(Asserter<Expression> condAsserter, Asserter<Expression> expr1Asserter,
                                               Asserter<Expression> expr2Asserter) {
        return (Expression expr) -> {
            assertTrue(expr instanceof TernaryExpr);
            TernaryExpr ternary = (TernaryExpr) expr;
            condAsserter.assertIt(ternary.cond);
            expr1Asserter.assertIt(ternary.expr1);
            expr2Asserter.assertIt(ternary.expr2);
        };
    }


}

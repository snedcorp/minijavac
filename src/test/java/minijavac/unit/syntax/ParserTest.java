package minijavac.unit.syntax;

import minijavac.Compiler;
import minijavac.unit.Asserter;
import minijavac.listener.Listener;
import minijavac.listener.SimpleListener;
import minijavac.ast.*;
import minijavac.err.CompileError;
import minijavac.syntax.err.ExpectedParseError;
import minijavac.syntax.TokenKind;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

import static minijavac.unit.ASTUtils.*;
import static minijavac.unit.TestUtils.*;
import static minijavac.unit.TestUtils.assertErr;
import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    private static final Path RESOURCES_PATH;
    private static final Path PARSE_PATH;
    private static final Function<String, Asserter<Path>> fileAsserter;

    static {
        RESOURCES_PATH = Paths.get("src/test/resources");
        PARSE_PATH = RESOURCES_PATH.resolve("unit/parse");
        fileAsserter = getFileAsserter(PARSE_PATH);
    }

    private static Asserter<Path> isFile(String file) {
        return fileAsserter.apply(file);
    }

    private List<CompileError> fail(String file, int expectedCount) {
        SimpleListener listener = new SimpleListener();
        test(file, listener);
        assertTrue(listener.hasErrors());
        assertEquals(expectedCount, listener.getErrors().size());
        return listener.getErrors();
    }

    private List<ClassDecl> pass(String file) {
        SimpleListener listener = new SimpleListener();
        List<ClassDecl> classDecls = test(file, listener);
        assertFalse(listener.hasErrors());
        return classDecls;
    }

    private List<ClassDecl> test(String file, Listener listener) {
        Path filePath = PARSE_PATH.resolve(file);
        String resourceStr = "/" + RESOURCES_PATH.relativize(filePath);
        
        try (InputStream stream = getClass().getResourceAsStream(resourceStr)) {
            Compiler compiler = new Compiler(listener);
            return compiler.parse(stream, filePath);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            Assertions.fail();
        }
        return null;
    }

    @Test
    public void fail_boolClassName() {
        String file = "class_decl/fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.CLASS, 1, 0,
                TokenKind.BOOLEAN, isFile(file), 1, 6,
                TokenKind.IDENTIFIER);
    }

    @Test
    public void fail_periodAfterClass() {
        String file = "class_decl/fail2.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.RCBRACKET, 1, 11,
                TokenKind.PERIOD, isFile(file), 1, 12,
                TokenKind.CLASS);
    }

    @Test
    public void fail_multipleClass_trueClassName() {
        String file = "class_decl/fail3.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.CLASS, 2, 0,
                TokenKind.TRUE, isFile(file), 2, 6,
                TokenKind.IDENTIFIER);
    }

    @Test
    public void fail_invalidDecl() {
        String file = "var_decl/fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.IDENTIFIER, 4, 13,
                TokenKind.LPAREN, isFile(file), 4, 15,
                TokenKind.ASSIGN);
    }

    @Test
    public void fail_nestedComment() {
        String file = "class_decl/fail4.java";
        List<CompileError> errs = fail(file, 1);
        assertParseErr(errs.get(0), TokenKind.MULTIPLY, isFile(file), 1, 26, "illegal start of type");
    }

    @Test
    public void fail_voidVariable() {
        String file = "var_decl/fail2.java";
        List<CompileError> errs = fail(file, 1);
        assertParseErr(errs.get(0), TokenKind.VOID, isFile(file), 3, 8, "not a statement");
    }

    @Test
    public void fail_voidParam() {
        String file = "method_decl/fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertParseErr(errs.get(0), TokenKind.VOID, isFile(file), 2, 12, "'void' type not allowed here");
    }

    @Test
    public void fail_assignInBinop() {
        String file = "expr/binop/fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.NUM, 3, 22,
                TokenKind.ASSIGN, isFile(file), 3, 24,
                TokenKind.SEMICOLON);
    }

    @Test
    public void fail_spaceBetweenGTE() {
        String file = "expr/binop/fail3.java";
        List<CompileError> errs = fail(file, 1);
        assertParseErr(errs.get(0), TokenKind.ASSIGN, isFile(file), 3, 20, "illegal start of expression");
    }

    @Test
    public void fail_oneSlashAfterClass() {
        String file = "class_decl/fail5.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.RCBRACKET, 4, 0,
                TokenKind.DIVIDE, isFile(file), 4, 2,
                TokenKind.CLASS);
    }

    @Test
    public void fail_staticBeforeVisibility() {
        String file = "field_decl/fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertParseErr(errs.get(0), TokenKind.PRIVATE, isFile(file), 2, 11, "illegal start of type");
    }

    @Test
    public void fail_bothVisibilities() {
        String file = "field_decl/fail2.java";
        List<CompileError> errs = fail(file, 1);
        assertParseErr(errs.get(0), TokenKind.PRIVATE, isFile(file), 2, 11, "illegal start of type");
    }

    @Test
    public void fail_memberInitializationOutsideMethod() {
        String file = "field_decl/fail3.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.IDENTIFIER, 2, 8,
                TokenKind.ASSIGN, isFile(file), 2, 10,
                TokenKind.LPAREN);
    }

    @Test
    public void fail_voidFieldVariable() {
        String file = "field_decl/fail4.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.IDENTIFIER, 2, 9,
                TokenKind.SEMICOLON, isFile(file), 2, 10,
                TokenKind.LPAREN);
    }

    @Test
    public void fail_outOfOrderArrayDecl() {
        String file = "field_decl/fail5.java";
        List<CompileError> errs = fail(file, 1);
        assertParseErr(errs.get(0), TokenKind.LBRACKET, isFile(file), 2, 11, "illegal start of type");
    }

    @Test
    public void fail_voidArrayDecl() {
        String file = "field_decl/fail6.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.VOID, 2, 11,
                TokenKind.LBRACKET, isFile(file), 2, 16,
                TokenKind.IDENTIFIER);
    }

    @Test
    public void fail_indexRefInVarDeclStmt() {
        String file = "var_decl/fail3.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.IDENTIFIER, 3, 17,
                TokenKind.LBRACKET, isFile(file), 3, 18,
                TokenKind.ASSIGN);
    }

    @Test
    public void fail_callInVarDeclStmt() {
        String file = "var_decl/fail4.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.IDENTIFIER, 3, 15,
                TokenKind.LPAREN, isFile(file), 3, 16,
                TokenKind.ASSIGN);
    }

    @Test
    public void fail_indexInArrayDecl() {
        String file = "var_decl/fail7.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.IDENTIFIER, 3, 15,
                TokenKind.LBRACKET, isFile(file), 3, 16,
                TokenKind.ASSIGN);
    }

    @Test
    public void fail_invalidAssignStmt() {
        String file = "stmt/assign/fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertParseErr(errs.get(0), TokenKind.TRUE, isFile(file), 3, 8, "not a statement");
    }

    @Test
    public void fail_invalidThisQRef() {
        String file = "ref/fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.PERIOD, 3, 15,
                TokenKind.THIS, isFile(file), 3, 16,
                TokenKind.IDENTIFIER);
    }

    @Test
    public void fail_invalidNotEqualsInBinop() {
        String file = "expr/binop/fail5.java";
        List<CompileError> errs = fail(file, 1);
        assertParseErr(errs.get(0), TokenKind.NOT_EQ, isFile(file), 3, 14, "illegal start of expression");
    }

    @Test
    public void fail_justThisInStmt() {
        String file = "stmt/fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 3, 12, "not a statement");
    }

    @Test
    public void fail_assignInComparison() {
        String file = "expr/binop/fail6.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.IDENTIFIER, 3, 12,
                TokenKind.ASSIGN, isFile(file), 3, 14,
                TokenKind.RPAREN);
    }

    @Test
    public void fail_binopWithNot() {
        String file = "expr/binop/fail7.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.IDENTIFIER, 3, 12,
                TokenKind.NOT, isFile(file), 3, 13,
                TokenKind.SEMICOLON);
    }

    @Test
    public void fail_arrayAssignWithoutId() {
        String file = "stmt/assign/fail2.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.RBRACKET, 4, 15,
                TokenKind.ASSIGN, isFile(file), 4, 18,
                TokenKind.IDENTIFIER);
    }

    @Test
    public void fail_noAssignOpInStmt() {
        String file = "stmt/assign/fail3.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 4, 10, "not a statement");
    }

    @Test
    public void fail_thisAtEndOfQRef() {
        String file = "ref/fail3.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.PERIOD, 4, 12,
                TokenKind.THIS, isFile(file), 4, 13,
                TokenKind.IDENTIFIER);
    }

    @Test
    public void fail_callInVarDecl() {
        String file = "var_decl/fail8.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.IDENTIFIER, 3, 10,
                TokenKind.LPAREN, isFile(file), 3, 11,
                TokenKind.ASSIGN);
    }

    @Test
    public void fail_invalidBinop() {
        String file = "expr/binop/fail8.java";
        List<CompileError> errs = fail(file, 1);
        assertParseErr(errs.get(0), TokenKind.LT, isFile(file), 3, 16, "illegal start of expression");
    }

    @Test
    public void fail_invalidBinopMult() {
        String file = "expr/binop/fail9.java";
        List<CompileError> errs = fail(file, 1);
        assertParseErr(errs.get(0), TokenKind.MULTIPLY, isFile(file), 3, 20, "illegal start of expression");
    }

    @Test
    public void fail_invalidBinopNot() {
        String file = "expr/binop/fail10.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.IDENTIFIER, 3, 16,
                TokenKind.NOT, isFile(file), 3, 18,
                TokenKind.SEMICOLON);
    }

    @Test
    public void fail_invalidThisInQRef() {
        String file = "ref/fail5.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.PERIOD, 3, 13,
                TokenKind.THIS, isFile(file), 3, 14,
                TokenKind.IDENTIFIER);
    }

    @Test
    public void fail_voidInNewExpr() {
        String file = "expr/newarr/fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertParseErr(errs.get(0), TokenKind.VOID, isFile(file), 3, 16, "'void' type not allowed here");
    }

    @Test
    public void fail_indexedThisRef() {
        String file = "ref/fail6.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.THIS, 3, 14,
                TokenKind.LBRACKET, isFile(file), 3, 18,
                TokenKind.SEMICOLON);
    }

    @Test
    public void fail_multErr_bothScanAndParseErrors() {
        String file = "mult_err/fail1.java";
        List<CompileError> errs = fail(file, 4);
        assertErr(errs.get(0),isFile(file), 1, 14, "illegal character: '#'");
        assertErr(errs.get(1),isFile(file), 3, 4, "illegal character: '`'");
        assertErr(errs.get(2), isFile(file), 5, 8, "not a statement");
        assertErr(errs.get(3),isFile(file), 8, 0, "unclosed comment");
    }

    @Test
    public void fail_eof_classDecl() {
        String file = "eof/fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0),isFile(file), 1, 12, "reached end of file while parsing");
    }

    @Test
    public void fail_eof_fieldDecl() {
        String file = "eof/fail2.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0),isFile(file), 2, 9, "reached end of file while parsing");
    }

    @Test
    public void fail_eof_methodDecl() {
        String file = "eof/fail3.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0),isFile(file), 4, 12, "reached end of file while parsing");
    }

    @Test
    public void fail_eof_overrideParseError() {
        String file = "eof/fail4.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0),isFile(file), 4, 14, "reached end of file while parsing");
    }

    @Test
    public void fail_eof_overrideAcceptError() {
        String file = "eof/fail5.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0),isFile(file), 5, 13, "reached end of file while parsing");
    }

    @Test
    public void fail_multErr_semicolonStatements() {
        String file = "mult_err/fail2.java";
        List<CompileError> errs = fail(file, 4);
        assertExpParseErr(errs.get(0), TokenKind.INT, 3, 8,
                TokenKind.SEMICOLON, isFile(file), 3, 12, TokenKind.IDENTIFIER);
        assertErr(errs.get(1),isFile(file), 4, 8, "not a statement");
        assertExpParseErr(errs.get(2), TokenKind.NUM, 5, 16,
                TokenKind.NOT, isFile(file), 5, 18, TokenKind.SEMICOLON);
        assertErr(errs.get(3),isFile(file), 6, 15, "illegal start of expression");
    }

    @Test
    public void fail_multErr_semicolonStatements_noSemicolon() {
        String file = "mult_err/fail3.java";
        List<CompileError> errs = fail(file, 3);
        assertExpParseErr(errs.get(0), TokenKind.INT, 3, 8,
                TokenKind.RETURN, isFile(file), 4, 8, TokenKind.IDENTIFIER);
        assertErr(errs.get(1),isFile(file), 4, 15, "illegal start of expression");
        assertErr(errs.get(2),isFile(file), 5, 12, "illegal start of expression");
    }

    @Test
    public void fail_multErr_rcBracketStatements() {
        String file = "mult_err/fail4.java";
        List<CompileError> errs = fail(file, 4);
        assertErr(errs.get(0),isFile(file), 3, 12, "illegal start of expression");
        assertErr(errs.get(1),isFile(file), 4, 16, "illegal start of expression");
        assertErr(errs.get(2),isFile(file), 5, 15, "illegal start of expression");
        assertErr(errs.get(3),isFile(file), 6, 8, "not a statement");
    }

    @Test
    public void fail_multErr_rcBracketStatements_noRcBracket() {
        String file = "mult_err/fail5.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0),isFile(file), 4, 8, "illegal start of expression");
        assertErr(errs.get(1),isFile(file), 4, 15, "illegal start of expression");
    }

    @Test
    public void fail_multErr_semicolonStatement_eofWhileSyncing_ignore() {
        String file = "mult_err/fail6.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.IDENTIFIER, 3, 12,
                TokenKind.PLUS, isFile(file), 3, 14, TokenKind.ASSIGN);
    }

    @Test
    public void fail_multErr_rcBracketStatement_eofWhileSyncing_ignore() {
        String file = "mult_err/fail7.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0),isFile(file), 3, 12, "illegal start of expression");
    }

    @Test
    public void fail_multErr_nestedStatements() {
        String file = "mult_err/fail8.java";
        List<CompileError> errs = fail(file, 3);
        assertExpParseErr(errs.get(0), TokenKind.IDENTIFIER, 4, 16,
                TokenKind.PLUS, isFile(file), 4, 18, TokenKind.ASSIGN);
        assertErr(errs.get(1),isFile(file), 6, 16, "not a statement");
        assertErr(errs.get(2),isFile(file), 9, 28, "illegal start of expression");
    }

    @Test
    public void fail_missingClassKeywordInDecl_expectedParseErrWithNoPrevToken() {
        String file = "fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertTrue(errs.get(0) instanceof ExpectedParseError);
        ExpectedParseError err = (ExpectedParseError) errs.get(0);
        assertNull(err.getPrevToken());
        assertEquals(1, err.getPos().line());
        assertEquals(0, err.getPos().offset());
        assertEquals(TokenKind.IDENTIFIER, err.getToken().kind);
        assertEquals(TokenKind.CLASS, err.getExpectedKind());
    }

    @Test
    public void fail_this_qref_statement() {
        String file = "ref/fail7.java";
        List<CompileError> errs = fail(file, 1);
        assertParseErr(errs.get(0), TokenKind.SEMICOLON, isFile(file), 3, 14, "not a statement");
    }

    @Test
    public void fail_callRef_assignStatement() {
        String file = "ref/fail8.java";
        List<CompileError> errs = fail(file, 2);
        assertExpParseErr(errs.get(0), TokenKind.RPAREN, 3, 10,
                TokenKind.ASSIGN, isFile(file), 3, 12, TokenKind.SEMICOLON);
        assertExpParseErr(errs.get(1), TokenKind.RPAREN, 4, 14,
                TokenKind.ASSIGN, isFile(file), 4, 16, TokenKind.SEMICOLON);
    }

    @Test
    public void fail_callAfterIxRef() {
        String file = "ref/fail9.java";
        List<CompileError> errs = fail(file, 2);
        assertExpParseErr(errs.get(0), TokenKind.RBRACKET, 4, 21,
                TokenKind.LPAREN, isFile(file), 4, 22, TokenKind.SEMICOLON);
        assertErr(errs.get(1), isFile(file), 5, 16, "invalid statement");
    }

    @Test
    public void fail_callAfterCallRef() {
        String file = "ref/fail10.java";
        List<CompileError> errs = fail(file, 1);
        assertExpParseErr(errs.get(0), TokenKind.RPAREN, 3, 12,
                TokenKind.LPAREN, isFile(file), 3, 13, TokenKind.SEMICOLON);
    }

    @Test
    public void fail_stmt_expr() {
        String file = "stmt/expr/fail1.java";
        List<CompileError> errs = fail(file, 2);
        assertExpParseErr(errs.get(0), TokenKind.IDENTIFIER, 3, 10,
                TokenKind.ASSIGN, isFile(file), 3, 12, TokenKind.SEMICOLON);
        assertExpParseErr(errs.get(1), TokenKind.INCREMENT, 4, 9,
                TokenKind.ASSIGN, isFile(file), 4, 12, TokenKind.SEMICOLON);
    }

    @Test
    public void fail_ternary() {
        String file = "expr/ternary/fail1.java";
        List<CompileError> errs = fail(file, 2);
        assertExpParseErr(errs.get(0), TokenKind.IDENTIFIER, 3, 24,
                TokenKind.SEMICOLON, isFile(file), 3, 25, TokenKind.COLON);
        assertExpParseErr(errs.get(1), TokenKind.IDENTIFIER, 4, 16,
                TokenKind.COLON, isFile(file), 4, 18, TokenKind.SEMICOLON);
    }

    @Test
    public void fail_static_constructor() {
        String file = "constructor_decl/fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertParseErr(errs.get(0), TokenKind.LPAREN, isFile(file), 2, 15, "modifier static not allowed here");
    }

    @Test
    public void fail_final_constructor() {
        String file = "constructor_decl/fail2.java";
        List<CompileError> errs = fail(file, 1);
        assertParseErr(errs.get(0), TokenKind.LPAREN, isFile(file), 2, 14, "modifier final not allowed here");
    }

    @Test
    public void fail_static_final_constructor() {
        String file = "constructor_decl/fail3.java";
        List<CompileError> errs = fail(file, 1);
        assertParseErr(errs.get(0), TokenKind.LPAREN, isFile(file), 2, 21, "modifier static,final not allowed here");
    }

    @Test
    public void pass_classDecls() {
        String file = "class_decl/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test", "Other"));

        Asserter<MethodDecl> main = method(Access.PUBLIC, true, voidType(), "main",
                List.of(param(arrayType(classType("String"), 1), "args")), null);

        clazz(classDecls.get(0), "Test", null, List.of(main));

        Asserter<FieldDecl> a01 = field(Access.PACKAGE_PRIVATE, false, intType(), "A_01");
        clazz(classDecls.get(1), "Other", List.of(a01), null);
    }

    @Test
    public void pass_keywords() {
        String file = "pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Keywords"));

        List<Asserter<Statement>> pStmts = List.of(
                varDeclStmt(intType(), "format", refExpr(idRef("while_1"))),
                varDeclStmt(intType(), "Int", refExpr(idRef("New"))),
                assignStmt(idRef("For"), refExpr(idRef("Class"))),
                assignStmt(idRef("FOR"), refExpr(idRef("RETURN")))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, pStmts);

        Asserter<Statement> blockStmt = blockStmt(List.of(assignStmt(idRef("else1"),
                binop(refExpr(idRef("iF")), TokenKind.EQ, refExpr(idRef("Then"))))));

        Asserter<Statement> ifStmt = ifStmt(
                binop(boolLit(true), TokenKind.EQ, boolLit(false)),
                blockStmt,
                null
        );

        List<Asserter<Statement>> declareStmts = List.of(
                varDeclStmt(boolType(), "iF", boolLit(true)),
                varDeclStmt(boolType(), "Then", boolLit(false)),
                varDeclStmt(boolType(), "else1", boolLit(false)),
                ifStmt
        );

        Asserter<MethodDecl> decl = method(Access.PUBLIC, false, intType(), "declare", null, declareStmts);

        clazz(classDecls.get(0), "Keywords", null, List.of(p, decl));
    }

    @Test
    public void pass_newObjectDecl() {
        String file = "expr/newobj/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("MainClass"));

        Asserter<Statement> varDeclStmt = varDeclStmt(classType("SecondSubClass"),
                "newobj", newObjectExpr("SecondSubClass", null));

        Asserter<MethodDecl> main = method(Access.PUBLIC, true, voidType(), "main",
                List.of(param(arrayType(classType("String"), 1), "args")), List.of(varDeclStmt));

        clazz(classDecls.get(0), "MainClass", null, List.of(main));
    }

    @Test
    public void pass_newIntArrayDecl() {
        String file = "expr/newarr/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Foo"));

        Asserter<Statement> varDeclStmt = varDeclStmt(arrayType(intType(), 1), "newarr",
                newArrayExpr(intType(), List.of(intLit(20))));

        Asserter<MethodDecl> bar = method(Access.PACKAGE_PRIVATE, false, voidType(), "bar",
                null, List.of(varDeclStmt));

        clazz(classDecls.get(0), "Foo", null, List.of(bar));
    }

    @Test
    public void pass_newBoolArrayDecl() {
        String file = "expr/newarr/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Foo"));

        Asserter<Statement> varDeclStmt = varDeclStmt(arrayType(boolType(), 1), "newarr",
                newArrayExpr(boolType(), List.of(intLit(20))));

        Asserter<MethodDecl> bar = method(Access.PACKAGE_PRIVATE, false, voidType(), "bar",
                null, List.of(varDeclStmt));

        clazz(classDecls.get(0), "Foo", null, List.of(bar));
    }

    @Test
    public void pass_methodDecls() {
        String file = "method_decl/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("MainClass", "SuperClass"));

        Asserter<MethodDecl> main = method(Access.PUBLIC, true, voidType(), "main",
                List.of(param(arrayType(classType("String"), 1), "args")), null);

        clazz(classDecls.get(0), "MainClass", null, List.of(main));

        Asserter<Statement> assignStmt = assignStmt(idRef("integer"), refExpr(idRef("worth")));

        Asserter<MethodDecl> setWorth = method(Access.PUBLIC, false, voidType(), "setWorth",
                List.of(param(intType(), "worth")), List.of(assignStmt));

        Asserter<Statement> returnStmt = returnStmt(refExpr(qRef(thisRef(), "integer")));

        Asserter<MethodDecl> getWorth = method(Access.PUBLIC, false, intType(), "getWorth",
                null, List.of(returnStmt));

        Asserter<Statement> setTruthAssign = assignStmt(idRef("bool"), refExpr(idRef("truth")));

        Asserter<MethodDecl> setTruth = method(Access.PUBLIC, false, voidType(), "setTruth",
                List.of(param(boolType(), "truth")), List.of(setTruthAssign));

        Asserter<Statement> getTruthReturn = returnStmt(refExpr(qRef(thisRef(), "bool")));

        Asserter<MethodDecl> getTruth = method(Access.PUBLIC, false, intType(), "getTruth",
                null, List.of(getTruthReturn));

        clazz(classDecls.get(1), "SuperClass", null, List.of(setWorth, getWorth, setTruth, getTruth));
    }

    @Test
    public void pass_nestedWhileInIf() {
        String file = "stmt/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("MainClass", "SuperClass"));

        Asserter<Statement> varDeclStmt = varDeclStmt(classType("SecondSubClass"),
                "newobj", newObjectExpr("SecondSubClass", null));

        Asserter<MethodDecl> main = method(Access.PUBLIC, true, voidType(), "main",
                List.of(param(arrayType(classType("String"), 1), "args")), List.of(varDeclStmt));

        clazz(classDecls.get(0), "MainClass", null, List.of(main));

        List<Asserter<ParameterDecl>> fillupParams = List.of(
                param(boolType(), "open"),
                param(arrayType(intType(), 1), "jar"),
                param(intType(), "marble"),
                param(intType(), "upto")
        );

        List<Asserter<Statement>> fillupStmts = List.of(
                varDeclStmt(intType(), "index", intLit(0)),
                ifStmt(
                        binop(
                                refExpr(idRef("open")),
                                TokenKind.EQ,
                                boolLit(true)
                        ),
                        blockStmt(List.of(
                                whileStmt(
                                        binop(
                                                refExpr(idRef("index")),
                                                TokenKind.LT,
                                                refExpr(idRef("upto"))
                                        ),
                                        blockStmt(List.of(
                                                assignStmt(
                                                        ixRef(idRef("ownjar"), List.of(refExpr(idRef("index")))),
                                                        refExpr(ixRef(idRef("jar"), List.of(refExpr(idRef("index")))))
                                                ),
                                                assignStmt(
                                                        ixRef(idRef("jar"), List.of(refExpr(idRef("index")))),
                                                        refExpr(idRef("marble"))
                                                )
                                        ))
                                )
                        )),
                        null
                )
        );

        Asserter<MethodDecl> fillup = method(Access.PRIVATE, false, voidType(), "fillup", fillupParams,
                fillupStmts);

        clazz(classDecls.get(1), "SuperClass", null, List.of(fillup));
    }

    @Test
    public void pass_binops() {
        String file = "expr/binop/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> pStmts = List.of(
                varDeclStmt(intType(), "x", binop(intLit(1), TokenKind.PLUS, intLit(2))),
                varDeclStmt(intType(), "y", binop(refExpr(idRef("x")), TokenKind.MULTIPLY, intLit(3))),
                varDeclStmt(intType(), "z", binop(refExpr(idRef("y")), TokenKind.DIVIDE, intLit(4))),
                varDeclStmt(boolType(), "a", binop(refExpr(idRef("z")), TokenKind.GT, intLit(5))),
                varDeclStmt(boolType(), "b", binop(refExpr(idRef("y")), TokenKind.GTE, intLit(6))),
                varDeclStmt(boolType(), "c", binop(refExpr(idRef("x")), TokenKind.LT, intLit(7))),
                varDeclStmt(intType(), "v", binop(refExpr(idRef("x")), TokenKind.LTE, intLit(8))),
                varDeclStmt(intType(), "w", binop(refExpr(idRef("d")), TokenKind.NOT_EQ, intLit(9))),
                varDeclStmt(boolType(), "d", binop(refExpr(idRef("a")), TokenKind.AND, boolLit(false))),
                varDeclStmt(boolType(), "r", binop(refExpr(idRef("d")), TokenKind.OR, boolLit(true)))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, pStmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_ifWithNoBraces() {
        String file = "stmt/if/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> fooStmts = List.of(
                ifStmt(
                        binop(refExpr(idRef("x")), TokenKind.NOT_EQ, intLit(0)),
                        returnStmt(refExpr(idRef("x"))),
                        null
                ),
                returnStmt(refExpr(idRef("y")))
        );

        Asserter<MethodDecl> foo = method(Access.PACKAGE_PRIVATE, false, voidType(), "foo", null, fooStmts);

        clazz(classDecls.get(0), "Test", null, List.of(foo));
    }

    @Test
    public void pass_refs() {
        String file = "ref/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> pStmts = List.of(
                assignStmt(idRef("a"), boolLit(true)),
                assignStmt(ixRef(idRef("a"), List.of(refExpr(idRef("b")))), refExpr(idRef("c"))),
                callStmt(callRef(idRef("p"), null)),
                assignStmt(qRef(idRef("a"), "b"), refExpr(idRef("d"))),
                callStmt(callRef(qRef(idRef("c"), "p"), List.of(refExpr(idRef("e")))))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, pStmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_refs2() {
        String file = "ref/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<FieldDecl>> fields = List.of(
                field(Access.PACKAGE_PRIVATE, false, arrayType(intType(), 1), "a"),
                field(Access.PACKAGE_PRIVATE, false, arrayType(classType("Test"), 1), "t")
        );

        List<Asserter<Statement>> pStmts = List.of(
                assignStmt(
                        ixRef(idRef("t"), List.of(refExpr(idRef("e")))),
                        binop(
                                refExpr(thisRef()),
                                TokenKind.PLUS,
                                intLit(1)
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "p", null, pStmts);

        clazz(classDecls.get(0), "Test", fields, List.of(p));
    }

    @Test
    public void pass_refs3() {
        String file = "ref/pass3.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> pStmts = List.of(
                varDeclStmt(classType("A"), "a", intLit(23)),
                varDeclStmt(boolType(), "b", refExpr(idRef("c")))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, pStmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_refs_thisCall() {
        String file = "ref/pass4.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<ParameterDecl>> params = List.of(
                param(intType(), "a"),
                param(boolType(), "b")
        );

        List<Asserter<Statement>> pStmts = List.of(
                callStmt(
                        callRef(
                                qRef(
                                        thisRef(),
                                        "p"
                                ),
                                List.of(
                                        refExpr(idRef("a")),
                                        refExpr(idRef("b"))
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", params, pStmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_arrayDecl() {
        String file = "var_decl/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<ParameterDecl>> params = List.of(
                param(intType(), "a")
        );

        List<Asserter<Statement>> pStmts = List.of(
                varDeclStmt(arrayType(classType("Test"), 1), "v", refExpr(idRef("a")))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", params, pStmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_arrayAssign() {
        String file = "stmt/assign/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<ParameterDecl>> params = List.of(
                param(intType(), "a")
        );

        List<Asserter<Statement>> pStmts = List.of(
                assignStmt(
                        ixRef(idRef("Test"), List.of(binop(refExpr(idRef("a")), TokenKind.PLUS, intLit(1)))),
                        binop(refExpr(idRef("a")), TokenKind.MULTIPLY, intLit(3))
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", params, pStmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_newThenCall() {
        String file = "expr/newobj/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<ParameterDecl>> params = List.of(
                param(intType(), "a")
        );

        List<Asserter<Statement>> pStmts = List.of(
                varDeclStmt(classType("C"), "c", newObjectExpr("C", null)),
                callStmt(callRef(qRef(idRef("c"), "p"), List.of(intLit(2), intLit(3))))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", params, pStmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_qRef() {
        String file = "ref/pass5.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<ParameterDecl>> params = List.of(
                param(intType(), "a")
        );

        List<Asserter<Statement>> pStmts = List.of(
                callStmt(callRef(qRef(thisRef(), "p"), List.of(intLit(2), intLit(3)))),
                assignStmt(qRef(idRef("a"), "v"), intLit(4))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", params, pStmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_qRef2() {
        String file = "ref/pass6.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> pStmts = List.of(
                assignStmt(qRef(idRef("x"), "y"), refExpr(idRef("z")))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, pStmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_arrayDecl2() {
        String file = "expr/newarr/pass3.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> pStmts = List.of(
                varDeclStmt(intType(), "a", intLit(3)),
                varDeclStmt(arrayType(intType(), 1), "b", newArrayExpr(intType(), List.of(intLit(4))))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, pStmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_arrayDecl3() {
        String file = "expr/newarr/pass4.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> pStmts = List.of(
                varDeclStmt(arrayType(classType("Foo"), 1), "foo", newArrayExpr(classType("Foo"),
                        List.of(intLit(10))))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, pStmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_arrayDecl_multiDim() {
        String file = "expr/newarr/pass5.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(
                        arrayType(intType(), 2), "ix2",
                        newArrayExpr(intType(), List.of(intLit(2), intLit(2)))
                ),
                varDeclStmt(
                        arrayType(boolType(), 2), "bx2",
                        newArrayExpr(boolType(), List.of(intLit(3), intLit(5)))
                ),
                varDeclStmt(
                        arrayType(classType("Test"), 2), "tx2",
                        newArrayExpr(classType("Test"), List.of(intLit(1), intLit(2)))
                ),
                varDeclStmt(
                        arrayType(intType(), 3), "ix3",
                        newArrayExpr(intType(), List.of(intLit(2), intLit(2), intLit(10)))
                ),
                varDeclStmt(
                        arrayType(boolType(), 3), "bx3",
                        newArrayExpr(boolType(), List.of(intLit(3), intLit(5), intLit(1)))
                ),
                varDeclStmt(
                        arrayType(classType("Test"), 3), "tx3",
                        newArrayExpr(classType("Test"), List.of(intLit(1), intLit(2), intLit(3)))
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "main", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_arrayDecl_init() {
        String file = "expr/newarr/pass6.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(
                        arrayType(intType(), 1), "ix",
                        newArrayInitExpr(
                                intType(),
                                1,
                                arrayInitExpr(
                                        List.of(
                                                intLit(1),
                                                intLit(2),
                                                intLit(3),
                                                intLit(4)
                                        )
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "main", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_arrayDecl_init_twoDim() {
        String file = "expr/newarr/pass7.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(
                        arrayType(intType(), 2), "ix",
                        newArrayInitExpr(
                                intType(),
                                2,
                                arrayInitExpr(
                                        List.of(
                                                arrayInitExpr(
                                                        List.of(
                                                                intLit(1),
                                                                intLit(2)
                                                        )
                                                ),
                                                arrayInitExpr(
                                                        List.of(
                                                                intLit(3),
                                                                intLit(4)
                                                        )
                                                ),
                                                arrayInitExpr(
                                                        List.of(
                                                                intLit(5),
                                                                intLit(6)
                                                        )
                                                ),
                                                arrayInitExpr(
                                                        List.of(
                                                                intLit(7),
                                                                intLit(8)
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "main", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_arrayDecl_init_threeDim() {
        String file = "expr/newarr/pass8.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(
                        arrayType(intType(), 3), "ix",
                        newArrayInitExpr(
                                intType(),
                                3,
                                arrayInitExpr(
                                        List.of(
                                                arrayInitExpr(
                                                        List.of(
                                                                arrayInitExpr(
                                                                        List.of(
                                                                                intLit(1),
                                                                                intLit(2)
                                                                        )
                                                                ),
                                                                arrayInitExpr(
                                                                        List.of(
                                                                                intLit(3),
                                                                                intLit(4)
                                                                        )
                                                                )
                                                        )
                                                ),
                                                arrayInitExpr(
                                                        List.of(
                                                                arrayInitExpr(
                                                                        List.of(
                                                                                intLit(5),
                                                                                intLit(6)
                                                                        )
                                                                ),
                                                                arrayInitExpr(
                                                                        List.of(
                                                                                intLit(7),
                                                                                intLit(8)
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "main", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_refs4() {
        String file = "ref/pass7.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> pStmts = List.of(
                assignStmt(thisRef(), refExpr(idRef("that"))),
                callStmt(callRef(thisRef(), null)),
                callStmt(callRef(qRef(thisRef(), "that"), List.of(intLit(5)))),
                assignStmt(ixRef(qRef(thisRef(), "that"), List.of(intLit(2))), intLit(3)),
                assignStmt(ixRef(qRef(qRef(thisRef(), "that"), "those"), List.of(intLit(3))), refExpr(idRef("them"))),
                callStmt(callRef(qRef(qRef(thisRef(), "that"), "those"), null)),
                varDeclStmt(arrayType(intType(), 1), "x", intLit(1)),
                varDeclStmt(classType("a"), "b", refExpr(idRef("c"))),
                callStmt(callRef(idRef("p"), null)),
                assignStmt(ixRef(qRef(idRef("p"), "b"), List.of(intLit(4))), intLit(5)),
                callStmt(callRef(qRef(idRef("p"), "b"), List.of(intLit(3)))),
                varDeclStmt(intType(), "z",
                        binop(
                                binop(
                                        refExpr(callRef(qRef(thisRef(), "p"), List.of(refExpr(idRef("x"))))),
                                        TokenKind.MULTIPLY,
                                        refExpr(callRef(qRef(idRef("that"), "q"), null))
                                ),
                                TokenKind.PLUS,
                                refExpr(ixRef(qRef(idRef("those"), "r"), List.of(refExpr(qRef(idRef("a"), "p")))))
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "p", null, pStmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_emptyWhile() {
        String file = "stmt/while/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> pStmts = List.of(
                whileStmt(boolLit(true), blockStmt(null))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, classType("A"), "p", null, pStmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_doWhile() {
        String file = "stmt/while/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> pStmts = List.of(
                doWhileStmt(
                        binop(refExpr(idRef("b")), TokenKind.LT, intLit(3)),
                        blockStmt(List.of(varDeclStmt(intType(), "x", intLit(1))))
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, classType("A"), "p", null, pStmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_staticMethod() {
        String file = "method_decl/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, true, voidType(), "p", null, null);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_privateMethod() {
        String file = "method_decl/pass3.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> pStmts = List.of(
                returnStmt(intLit(0))
        );

        Asserter<MethodDecl> p = method(Access.PRIVATE, false, voidType(), "p", null, pStmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_localDecl() {
        String file = "var_decl/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> pStmts = List.of(
                varDeclStmt(classType("Foo"), "x", intLit(3)),
                varDeclStmt(intType(), "y", intLit(4))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "p", null, pStmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_localDecl_no_init() {
        String file = "var_decl/pass3.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> pStmts = List.of(
                varDeclStmt(classType("Foo"), "x", null),
                varDeclStmt(intType(), "y", null),
                varDeclStmt(arrayType(classType("Foo"), 1), "z", null),
                varDeclStmt(arrayType(intType(), 1), "a", null)
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "p", null, pStmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_objArrayParam() {
        String file = "method_decl/pass4.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<ParameterDecl>> params = List.of(
                param(arrayType(classType("A"), 1), "s")
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", params, null);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_intArrayParam() {
        String file = "method_decl/pass5.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<ParameterDecl>> params = List.of(
                param(arrayType(intType(), 1), "m")
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", params, null);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_boolArrayParam() {
        String file = "method_decl/pass6.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<ParameterDecl>> params = List.of(
                param(arrayType(boolType(), 1), "m")
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", params, null);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_newObj() {
        String file = "expr/newobj/pass3.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> pStmts = List.of(
                varDeclStmt(classType("A"), "a", newObjectExpr("A", null))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, pStmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_newObj_params() {
        String file = "expr/newobj/pass4.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(classType("Test"), "t", newObjectExpr("Test", List.of(intLit(1)))),
                varDeclStmt(classType("Test"), "t2", newObjectExpr("Test",
                        List.of(boolLit(false), intLit(3), floatLit(4.5f))))
        );

        Asserter<MethodDecl> main = method(Access.PACKAGE_PRIVATE, false, voidType(), "main", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(main));
    }

    @Test
    public void pass_qRefCall() {
        String file = "ref/pass8.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> pStmts = List.of(
                callStmt(callRef(qRef(idRef("x"), "y"), List.of(intLit(3))))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, pStmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_arrayAssign2() {
        String file = "stmt/assign/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> pStmts = List.of(
                assignStmt(ixRef(idRef("x"), List.of(intLit(2))), intLit(3))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, pStmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_compoundAssign() {
        String file = "stmt/assign/pass3.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                assignStmt(idRef("x"), TokenKind.PLUS_ASSIGN, refExpr(idRef("y"))),
                assignStmt(idRef("x"), TokenKind.MINUS_ASSIGN, refExpr(idRef("y"))),
                assignStmt(idRef("x"), TokenKind.MULTIPLY_ASSIGN, refExpr(idRef("y"))),
                assignStmt(idRef("x"), TokenKind.DIVIDE_ASSIGN, refExpr(idRef("y"))),
                assignStmt(idRef("x"), TokenKind.MODULO_ASSIGN, refExpr(idRef("y"))),
                assignStmt(idRef("x"), TokenKind.BTW_AND_ASSIGN, refExpr(idRef("y"))),
                assignStmt(idRef("x"), TokenKind.BTW_EXC_OR_ASSIGN, refExpr(idRef("y"))),
                assignStmt(idRef("x"), TokenKind.BTW_INC_OR_ASSIGN, refExpr(idRef("y"))),
                assignStmt(idRef("x"), TokenKind.RSHIFT_ASSIGN, refExpr(idRef("y"))),
                assignStmt(idRef("x"), TokenKind.LSHIFT_ASSIGN, refExpr(idRef("y"))),
                assignStmt(idRef("x"), TokenKind.UN_RSHIFT_ASSIGN, refExpr(idRef("y"))),
                assignStmt(ixRef(idRef("arr"), List.of(intLit(1))), TokenKind.PLUS_ASSIGN, intLit(1))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_stmt_expr_prefix_increment_decrement() {
        String file = "stmt/expr/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                exprStmt(unop(TokenKind.DECREMENT, refExpr(idRef("a")))),
                exprStmt(unop(TokenKind.INCREMENT, refExpr(idRef("a"))))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_stmt_expr_postfix_increment_decrement_idRef() {
        String file = "stmt/expr/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                exprStmt(postfix(TokenKind.DECREMENT, refExpr(idRef("a")))),
                exprStmt(postfix(TokenKind.INCREMENT, refExpr(idRef("a"))))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_stmt_expr_postfix_increment_decrement_qRef_ixRef_thisRef() {
        String file = "stmt/expr/pass3.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                exprStmt(postfix(TokenKind.DECREMENT, refExpr(qRef(callRef(idRef("a"), null), "b")))),
                exprStmt(postfix(TokenKind.INCREMENT, refExpr(qRef(idRef("a"), "b")))),
                exprStmt(postfix(TokenKind.DECREMENT, refExpr(qRef(ixRef(idRef("a"), List.of(intLit(1))), "b")))),
                exprStmt(postfix(TokenKind.INCREMENT, refExpr(ixRef(idRef("a"), List.of(intLit(1)))))),
                exprStmt(postfix(TokenKind.DECREMENT, refExpr(qRef(thisRef(), "a")))),
                exprStmt(postfix(TokenKind.INCREMENT, refExpr(ixRef(qRef(thisRef(), "a"), List.of(intLit(1))))))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_returnThis() {
        String file = "ref/pass9.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> pStmts = List.of(
                returnStmt(refExpr(thisRef()))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, classType("A"), "p", null, pStmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_ixRef() {
        String file = "ref/pass10.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> pStmts = List.of(
                varDeclStmt(classType("A"), "x", refExpr(ixRef(idRef("x"), List.of(intLit(3)))))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, pStmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_assignThis() {
        String file = "ref/pass11.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> pStmts = List.of(
                varDeclStmt(classType("A"), "x", refExpr(thisRef()))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, pStmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_qRefThis() {
        String file = "ref/pass12.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> pStmts = List.of(
                varDeclStmt(classType("A"), "x", refExpr(callRef(qRef(thisRef(), "p"), null)))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, pStmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_ifSingleStatement() {
        String file = "stmt/if/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<ParameterDecl>> params = List.of(
                param(intType(), "a"),
                param(intType(), "b")
        );

        List<Asserter<Statement>> stmts = List.of(
                ifStmt(
                        binop(
                                refExpr(idRef("a")),
                                TokenKind.LT,
                                refExpr(idRef("b"))
                        ),
                        assignStmt(idRef("b"), refExpr(idRef("a"))),
                        null
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "f", params, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_unop() {
        String file = "expr/unop/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "x", unop(TokenKind.MINUS, refExpr(idRef("b")))),
                varDeclStmt(boolType(), "y", unop(TokenKind.NOT, refExpr(idRef("y")))),
                varDeclStmt(boolType(), "y", unop(TokenKind.NOT, unop(TokenKind.NOT, refExpr(idRef("y"))))),
                varDeclStmt(intType(), "z", unop(TokenKind.COMPLEMENT, refExpr(idRef("x")))),
                varDeclStmt(intType(), "z", unop(TokenKind.COMPLEMENT, unop(TokenKind.COMPLEMENT, refExpr(idRef("x")))))
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_binop_logical() {
        String file = "expr/binop/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(
                        boolType(),
                        "x",
                        binop(
                                binop(
                                        boolLit(true),
                                        TokenKind.AND,
                                        boolLit(false)
                                ),
                                TokenKind.OR,
                                refExpr(idRef("x"))
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_refs5() {
        String file = "ref/pass13.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("T"));

        List<Asserter<Statement>> stmts = List.of(
                callStmt(callRef(idRef("foo"), List.of(intLit(4)))),
                callStmt(callRef(qRef(thisRef(), "foo"), List.of(intLit(5)))),
                returnStmt(
                        binop(
                                refExpr(ixRef(idRef("p"), List.of(intLit(3)))),
                                TokenKind.PLUS,
                                refExpr(qRef(idRef("a"), "b"))
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "foo", null, stmts);

        clazz(classDecls.get(0), "T", null, List.of(p));
    }

    @Test
    public void pass_refs6() {
        String file = "ref/pass14.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("C"));

        List<Asserter<Statement>> stmts = List.of(
                callStmt(callRef(qRef(thisRef(), "foo"), List.of(intLit(3), refExpr(thisRef())))),
                callStmt(callRef(qRef(idRef("other"), "foo"), List.of(intLit(4), refExpr(idRef("other")))))
        );

        Asserter<MethodDecl> p = method(Access.PUBLIC, false, voidType(), "foo", null, stmts);

        clazz(classDecls.get(0), "C", null, List.of(p));
    }

    @Test
    public void pass_multipleClasses() {
        String file = "class_decl/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A", "B", "C"));

        clazz(classDecls.get(0), "A", null, null);

        List<Asserter<FieldDecl>> bFields = List.of(
                field(Access.PRIVATE, false, arrayType(intType(), 1), "v"),
                field(Access.PACKAGE_PRIVATE, false, classType("C"), "c"),
                field(Access.PRIVATE, false, intType(), "x")
        );

        List<Asserter<ParameterDecl>> fooParams = List.of(
                param(intType(), "a"),
                param(classType("B"), "other")
        );

        Asserter<MethodDecl> foo = method(Access.PUBLIC, false, voidType(), "foo", fooParams, null);

        clazz(classDecls.get(1), "B", bFields, List.of(foo));

        List<Asserter<Statement>> tryitStmts = List.of(
                varDeclStmt(intType(), "x", unop(
                        TokenKind.MINUS,
                        unop(
                                TokenKind.MINUS,
                                refExpr(idRef("x"))
                        )
                )),
                returnStmt(newArrayExpr(intType(), List.of(intLit(20))))
        );

        Asserter<MethodDecl> tryit = method(Access.PUBLIC, false, arrayType(intType(), 1), "tryit",
                null, tryitStmts);

        clazz(classDecls.get(2), "C",
                List.of(field(Access.PRIVATE, false, boolType(), "b")), List.of(tryit));
    }

    @Test
    public void pass_unop2() {
        String file = "expr/unop/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "x",
                        binop(
                                refExpr(idRef("b")),
                                TokenKind.MINUS,
                                unop(
                                        TokenKind.MINUS,
                                        refExpr(idRef("b"))
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_unop3() {
        String file = "expr/unop/pass3.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "x",
                        binop(
                                refExpr(idRef("b")),
                                TokenKind.MINUS,
                                unop(
                                        TokenKind.MINUS,
                                        unop(
                                                TokenKind.MINUS,
                                                unop(
                                                        TokenKind.MINUS,
                                                        refExpr(idRef("b"))
                                                )
                                        )
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_unop_increment_decrement() {
        String file = "expr/unop/pass4.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "x",
                        binop(
                                intLit(5),
                                TokenKind.PLUS,
                                unop(
                                        TokenKind.INCREMENT,
                                        refExpr(idRef("b"))
                                )
                        )
                ),
                varDeclStmt(intType(), "y",
                        binop(
                                intLit(5),
                                TokenKind.MINUS,
                                unop(
                                        TokenKind.DECREMENT,
                                        refExpr(idRef("b"))
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_postfix() {
        String file = "expr/postfix/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "x",
                        postfix(
                                TokenKind.INCREMENT,
                                refExpr(idRef("a"))
                        )
                ),
                varDeclStmt(intType(), "y",
                        postfix(
                                TokenKind.DECREMENT,
                                refExpr(idRef("a"))
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_postfix_precedence() {
        String file = "expr/postfix/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "x",
                        binop(
                                intLit(5),
                                TokenKind.PLUS,
                                postfix(
                                        TokenKind.INCREMENT,
                                        refExpr(idRef("a"))
                                )
                        )
                ),
                varDeclStmt(intType(), "y",
                        binop(
                                intLit(5),
                                TokenKind.MULTIPLY,
                                unop(
                                        TokenKind.COMPLEMENT,
                                        postfix(
                                                TokenKind.DECREMENT,
                                                refExpr(idRef("a"))
                                        )
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_ternary() {
        String file = "expr/ternary/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "x",
                        ternary(
                                boolLit(true),
                                intLit(1),
                                intLit(0)
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_ternary2() {
        String file = "expr/ternary/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "x",
                        ternary(
                                binop(
                                        refExpr(callRef(idRef("getB"), null)),
                                        TokenKind.EQ,
                                        boolLit(false)
                                ),
                                binop(
                                        intLit(10),
                                        TokenKind.MINUS,
                                        intLit(13)
                                ),
                                refExpr(ixRef(idRef("ix"), List.of(intLit(1), intLit(2))))
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_ternary_nested() {
        String file = "expr/ternary/pass3.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "x",
                        ternary(
                                refExpr(idRef("a")),
                                refExpr(idRef("b")),
                                ternary(
                                        refExpr(idRef("c")),
                                        refExpr(idRef("d")),
                                        refExpr(idRef("e"))
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_ternary_nested2() {
        String file = "expr/ternary/pass4.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "x",
                        ternary(
                                refExpr(idRef("a")),
                                ternary(
                                        refExpr(idRef("b")),
                                        ternary(
                                                refExpr(idRef("c")),
                                                refExpr(idRef("d")),
                                                refExpr(idRef("e"))
                                        ),
                                        refExpr(idRef("f"))
                                ),
                                refExpr(idRef("g"))
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_ternary_nested3() {
        String file = "expr/ternary/pass5.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "x",
                        ternary(
                                refExpr(idRef("a")),
                                refExpr(idRef("b")),
                                ternary(
                                        refExpr(idRef("c")),
                                        refExpr(idRef("d")),
                                        ternary(
                                                refExpr(idRef("e")),
                                                refExpr(idRef("f")),
                                                refExpr(idRef("g"))
                                        )
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_null() {
        String file = "expr/null/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(classType("Test"), "t", nullLit())
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(p));
    }

    @Test
    public void pass_refs7() {
        String file = "ref/pass15.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "a", refExpr(ixRef(qRef(idRef("x"), "y"), List.of(intLit(4)))))
        );

        Asserter<MethodDecl> p = method(Access.PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_refs8() {
        String file = "ref/pass16.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "a", refExpr(qRef(ixRef(idRef("x"), List.of(intLit(4))), "y")))
        );

        Asserter<MethodDecl> p = method(Access.PRIVATE, false, voidType(), "p", null, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_callRefs_stmt() {
        String file = "ref/pass17.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                callStmt(callRef(thisRef(), null)),
                callStmt(callRef(thisRef(), List.of(intLit(1), intLit(2)))),
                callStmt(callRef(idRef("x"), List.of(intLit(1), intLit(2)))),
                callStmt(callRef(qRef(thisRef(), "x"), List.of(intLit(1), intLit(2)))),
                callStmt(callRef(qRef(idRef("Test"), "x"), null)),
                callStmt(callRef(qRef(qRef(idRef("x"), "p"), "q"), List.of(intLit(1), intLit(2), intLit(3)))),
                callStmt(callRef(qRef(callRef(qRef(callRef(idRef("x"), List.of(intLit(1))), "p"), List.of(intLit(2))), "q"), List.of(intLit(3)))),
                assignStmt(qRef(callRef(qRef(thisRef(), "x"), null), "p"), intLit(1))
        );

        Asserter<MethodDecl> main = method(Access.PRIVATE, false, voidType(), "main", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(main));
    }

    @Test
    public void pass_callRefs_expr() {
        String file = "ref/pass18.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                assignStmt(idRef("r"), refExpr(callRef(qRef(thisRef(), "x"), null))),
                assignStmt(idRef("r"), refExpr(callRef(qRef(qRef(idRef("x"), "p"), "q"), null))),
                assignStmt(idRef("r"), refExpr(callRef(qRef(ixRef(qRef(idRef("x"), "p"), List.of(intLit(1))), "q"), null))),
                assignStmt(idRef("r"), refExpr(qRef(callRef(idRef("x"), null), "p"))),
                assignStmt(idRef("r"), refExpr(
                        qRef(
                                callRef(
                                        qRef(
                                                callRef(idRef("x"), List.of(intLit(1))),
                                                "p"
                                        ),
                                        List.of(intLit(1), intLit(2))
                                ),
                                "q"
                        )
                )),
                assignStmt(idRef("r"), refExpr(
                        callRef(
                                qRef(
                                        callRef(
                                                qRef(
                                                        callRef(
                                                                idRef("x"),
                                                                List.of(intLit(1))
                                                        ),
                                                        "p"
                                                ),
                                                List.of(intLit(1), intLit(2))
                                        ),
                                        "q"
                                ),
                                List.of(intLit(1), intLit(2), intLit(3)))
                ))
        );

        Asserter<MethodDecl> main = method(Access.PRIVATE, false, voidType(), "main", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(main));
    }

    @Test
    public void pass_multiDim_ixRef() {
        String file = "ref/pass19.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                assignStmt(
                        ixRef(idRef("a"), List.of(intLit(1), intLit(2))), intLit(3)
                ),
                assignStmt(
                        ixRef(qRef(idRef("a"), "b"), List.of(refExpr(idRef("x")), intLit(2), intLit(3))), intLit(3)
                ),
                assignStmt(
                        idRef("x"), refExpr(ixRef(idRef("a"), List.of(intLit(1), intLit(2))))
                ),
                assignStmt(
                        idRef("x"), refExpr(callRef(qRef(ixRef(qRef(thisRef(), "a"),
                                List.of(intLit(1), intLit(2), intLit(3))), "get"), null))
                )
        );

        Asserter<MethodDecl> main = method(Access.PRIVATE, false, voidType(), "main", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(main));
    }

    @Test
    public void pass_ixAndCallRefCombined() {
        String file = "ref/pass20.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                assignStmt(
                        idRef("x"),
                        refExpr(
                                ixRef(
                                        callRef(
                                                qRef(
                                                        idRef("c"),
                                                        "p"
                                                ),
                                                List.of(intLit(2), intLit(3))
                                        ),
                                        List.of(intLit(4))
                                )
                        )
                ),
                assignStmt(
                        qRef(
                                callRef(
                                        qRef(
                                                ixRef(
                                                        callRef(
                                                                qRef(
                                                                        ixRef(
                                                                                idRef("x"),
                                                                                List.of(intLit(1))
                                                                        ),
                                                                        "y"
                                                                ),
                                                                List.of(intLit(2))
                                                        ),
                                                        List.of(intLit(3), intLit(4))
                                                ),
                                                "z"
                                        ),
                                        List.of(intLit(5), intLit(6))
                                ),
                                "z"
                        ),
                        intLit(7)
                )
        );

        Asserter<MethodDecl> main = method(Access.PRIVATE, false, voidType(), "main", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(main));
    }

    @Test
    public void pass_binop_multiplicative() {
        String file = "expr/binop/pass3.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> stmts = List.of(
                assignStmt(idRef("x"),
                        binop(
                                binop(
                                        intLit(1),
                                        TokenKind.MULTIPLY,
                                        intLit(2)
                                ),
                                TokenKind.DIVIDE,
                                intLit(3)
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "f", null, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_binop_multiplicative2() {
        String file = "expr/binop/pass4.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> stmts = List.of(
                assignStmt(idRef("x"),
                        binop(
                                binop(
                                        intLit(1),
                                        TokenKind.DIVIDE,
                                        intLit(2)
                                ),
                                TokenKind.MULTIPLY,
                                intLit(3)
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "f", null, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_binop_precedence() {
        String file = "expr/binop/pass5.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> stmts = List.of(
                assignStmt(idRef("x"),
                        binop(
                                binop(
                                        intLit(1),
                                        TokenKind.PLUS,
                                        intLit(2)
                                ),
                                TokenKind.LT,
                                intLit(3)
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "f", null, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_binop_precedence2() {
        String file = "expr/binop/pass6.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(boolType(), "x",
                        binop(
                                intLit(1),
                                TokenKind.LT,
                                binop(
                                        intLit(2),
                                        TokenKind.PLUS,
                                        intLit(3)
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "f", null, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_binop_precedence3() {
        String file = "expr/binop/pass7.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(boolType(), "x",
                        binop(
                                unop(TokenKind.NOT, boolLit(true)),
                                TokenKind.LT,
                                boolLit(false)
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "f", null, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_binop_precedence4() {
        String file = "expr/binop/pass8.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(boolType(), "x",
                        binop(
                                binop(
                                        boolLit(true),
                                        TokenKind.AND,
                                        boolLit(false)
                                ),
                                TokenKind.OR,
                                boolLit(true)
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "f", null, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_binop_precedence5() {
        String file = "expr/binop/pass9.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> stmts = List.of(
                assignStmt(idRef("x"),
                        binop(
                                binop(
                                        intLit(1),
                                        TokenKind.PLUS,
                                        intLit(2)
                                ),
                                TokenKind.MULTIPLY,
                                intLit(3)
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "f", null, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_binop_precedence6() {
        String file = "expr/binop/pass10.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> stmts = List.of(
                assignStmt(idRef("x"),
                        binop(
                                intLit(1),
                                TokenKind.MINUS,
                                binop(
                                        intLit(2),
                                        TokenKind.MULTIPLY,
                                        unop(TokenKind.MINUS, intLit(3))
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "f", null, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_binop_precedence7() {
        String file = "expr/binop/pass11.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> stmts = List.of(
                assignStmt(idRef("x"),
                        binop(
                                binop(
                                        unop(TokenKind.MINUS, unop(TokenKind.MINUS, intLit(1))),
                                        TokenKind.MULTIPLY,
                                        intLit(2)
                                ),
                                TokenKind.MINUS,
                                intLit(3)
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "f", null, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_binop_precedence8() {
        String file = "expr/binop/pass12.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(boolType(), "x",
                        binop(
                                binop(
                                        intLit(2),
                                        TokenKind.LT,
                                        intLit(1)
                                ),
                                TokenKind.AND,
                                binop(
                                        intLit(3),
                                        TokenKind.GTE,
                                        intLit(4)
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "f", null, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_binop_precedence9() {
        String file = "expr/binop/pass13.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(boolType(), "x",
                        binop(
                                binop(
                                        boolLit(false),
                                        TokenKind.AND,
                                        binop(
                                                intLit(2),
                                                TokenKind.GTE,
                                                intLit(3)
                                        )
                                ),
                                TokenKind.OR,
                                boolLit(true)
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "f", null, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_binop_precedence10() {
        String file = "expr/binop/pass14.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "x",
                        binop(
                                binop(
                                        intLit(1),
                                        TokenKind.PLUS,
                                        intLit(2)
                                ),
                                TokenKind.MINUS,
                                binop(
                                        binop(
                                                intLit(3),
                                                TokenKind.MULTIPLY,
                                                intLit(4)
                                        ),
                                        TokenKind.DIVIDE,
                                        intLit(5)
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "f", null, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_binop_precedence12() {
        String file = "expr/binop/pass15.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(boolType(), "x",
                        binop(
                                binop(
                                        boolLit(true),
                                        TokenKind.OR,
                                        binop(
                                                boolLit(false),
                                                TokenKind.AND,
                                                boolLit(false)
                                        )
                                ),
                                TokenKind.OR,
                                binop(
                                        boolLit(true),
                                        TokenKind.EQ,
                                        boolLit(false)
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "f", null, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_binop_precedence13() {
        String file = "expr/binop/pass16.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "d",
                        binop(
                                binop(
                                        intLit(2),
                                        TokenKind.PLUS,
                                        unop(
                                                TokenKind.MINUS,
                                                refExpr(idRef("x"))
                                        )
                                ),
                                TokenKind.MINUS,
                                unop(
                                        TokenKind.MINUS,
                                        refExpr(idRef("x"))
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, intType(), "f", null, stmts);

        clazz(classDecls.get(0), "A", null, List.of(p));
    }

    @Test
    public void pass_nestedIf() {
        String file = "stmt/if/pass3.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("B"));

        List<Asserter<Statement>> stmts = List.of(
                ifStmt(
                        refExpr(idRef("b")),
                        ifStmt(
                                refExpr(idRef("c")),
                                assignStmt(idRef("x"), intLit(1)),
                                assignStmt(idRef("x"), intLit(2))
                        ),
                        ifStmt(
                                refExpr(idRef("d")),
                                assignStmt(idRef("x"), intLit(11)),
                                assignStmt(idRef("x"), intLit(22))
                        )
                ),
                ifStmt(
                        boolLit(true),
                        ifStmt(
                                boolLit(false),
                                assignStmt(idRef("x"), intLit(3)),
                                assignStmt(idRef("x"), intLit(4))
                        ),
                        null
                ),
                ifStmt(
                        unop(TokenKind.NOT, boolLit(true)),
                        assignStmt(idRef("x"), intLit(33)),
                        ifStmt(
                                unop(TokenKind.NOT, boolLit(false)),
                                assignStmt(idRef("x"), intLit(44)),
                                assignStmt(idRef("x"), intLit(55))
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PUBLIC, false, voidType(), "foo", null, stmts);

        clazz(classDecls.get(0), "B", null, List.of(p));
    }

    @Test
    public void pass_if_danglingElse() {
        String file = "stmt/if/pass4.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                ifStmt(
                        refExpr(idRef("b")),
                        ifStmt(
                                refExpr(idRef("c")),
                                callStmt(callRef(idRef("get"), List.of(intLit(1)))),
                                callStmt(callRef(idRef("get"), List.of(intLit(2))))
                        ),
                        null
                )
        );

        Asserter<MethodDecl> main = method(Access.PACKAGE_PRIVATE, false, voidType(), "main", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(main));
    }

    @Test
    public void pass_if_elseIf() {
        String file = "stmt/if/pass5.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                ifStmt(
                        refExpr(idRef("b")),
                        callStmt(callRef(idRef("get"), List.of(intLit(1)))),
                        ifStmt(
                                refExpr(idRef("c")),
                                callStmt(callRef(idRef("get"), List.of(intLit(2)))),
                                ifStmt(
                                        refExpr(idRef("d")),
                                        callStmt(callRef(idRef("get"), List.of(intLit(3)))),
                                        callStmt(callRef(idRef("get"), List.of(intLit(4))))
                                )
                        )
                )
        );

        Asserter<MethodDecl> main = method(Access.PACKAGE_PRIVATE, false, voidType(), "main", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(main));
    }

    @Test
    public void pass_break_continue() {
        String file = "stmt/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                breakStmt(),
                continueStmt()
        );

        Asserter<MethodDecl> main = method(Access.PACKAGE_PRIVATE, false, voidType(), "main", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(main));
    }

    @Test
    public void pass_for() {
        String file = "stmt/for/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        List<Asserter<Statement>> stmts = List.of(
                forStmt(
                        varDeclStmt(
                            intType(),
                            "i",
                            intLit(0)
                        ),
                        binop(
                                refExpr(idRef("i")),
                                TokenKind.LT,
                                intLit(10)
                        ),
                        exprStmt(
                                postfix(
                                        TokenKind.INCREMENT,
                                        refExpr(idRef("i"))
                                )
                        ),
                        blockStmt(List.of(
                                callStmt(callRef(idRef("get"), List.of(refExpr(idRef("i")))))
                        ))
                )
        );

        Asserter<MethodDecl> main = method(Access.PACKAGE_PRIVATE, false, voidType(), "main", null, stmts);

        clazz(classDecls.get(0), "Test", null, List.of(main));
    }

    @Test
    public void pass_binop_precedence14() {
        String file = "expr/binop/pass17.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("A"));


        Asserter<Statement> stmt = varDeclStmt(boolType(), "b",
                binop(
                        boolLit(false),
                        TokenKind.OR,
                        binop(
                                binop(
                                        boolLit(true),
                                        TokenKind.EQ,
                                        binop(
                                                intLit(2),
                                                TokenKind.LT,
                                                binop(
                                                        unop(TokenKind.MINUS, intLit(3)),
                                                        TokenKind.MINUS,
                                                        binop(
                                                                intLit(4),
                                                                TokenKind.DIVIDE,
                                                                intLit(5)
                                                        )
                                                )
                                        )
                                ),
                                TokenKind.AND,
                                unop(TokenKind.NOT, unop(TokenKind.NOT, boolLit(false)))
                        )
                )
        );

        Asserter<MethodDecl> main = method(Access.PUBLIC, true, voidType(), "main",
                List.of(param(arrayType(classType("String"), 1), "args")), List.of(stmt, stmt));

        clazz(classDecls.get(0), "A", null, List.of(main));
    }

    @Test
    public void pass_binop_precedence15() {
        String file = "expr/binop/pass18.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "x",
                        binop(
                                binop(
                                        binop(
                                                binop(
                                                        binop(
                                                                binop(
                                                                        binop(
                                                                                binop(
                                                                                        unop(TokenKind.MINUS, intLit(1)),
                                                                                        TokenKind.PLUS,
                                                                                        binop(
                                                                                                binop(
                                                                                                        intLit(2),
                                                                                                        TokenKind.MULTIPLY,
                                                                                                        intLit(3)
                                                                                                ),
                                                                                                TokenKind.DIVIDE,
                                                                                                intLit(4)
                                                                                        )
                                                                                ),
                                                                                TokenKind.GT,
                                                                                intLit(5)
                                                                        ),
                                                                        TokenKind.GTE,
                                                                        intLit(6)
                                                                ),
                                                                TokenKind.LT,
                                                                intLit(7)
                                                        ),
                                                        TokenKind.LTE,
                                                        intLit(8)
                                                ),
                                                TokenKind.NOT_EQ,
                                                intLit(9)
                                        ),
                                        TokenKind.AND,
                                        unop(TokenKind.NOT, unop(TokenKind.NOT, boolLit(false)))
                                ),
                                TokenKind.OR,
                                boolLit(true)
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p",
                null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_binop_modulo() {
        String file = "expr/binop/pass19.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(intType(), "x",
                        binop(
                                intLit(5),
                                TokenKind.MODULO,
                                intLit(2)
                        )
                ),
                varDeclStmt(intType(), "y",
                        binop(
                                binop(
                                        intLit(5),
                                        TokenKind.MODULO,
                                        intLit(2)
                                ),
                                TokenKind.PLUS,
                                intLit(3)
                        )
                ),
                varDeclStmt(intType(), "z",
                        binop(
                                intLit(5),
                                TokenKind.PLUS,
                                binop(
                                        intLit(2),
                                        TokenKind.MODULO,
                                        intLit(3)
                                )
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "main",
                null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_binop_bit() {
        String file = "expr/binop/pass20.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                assignStmt(idRef("x"),
                        binop(
                                refExpr(idRef("y")),
                                TokenKind.LSHIFT,
                                refExpr(idRef("z"))
                        )
                ),
                assignStmt(idRef("x"),
                        binop(
                                refExpr(idRef("y")),
                                TokenKind.RSHIFT,
                                refExpr(idRef("z"))
                        )
                ),
                assignStmt(idRef("x"),
                        binop(
                                refExpr(idRef("y")),
                                TokenKind.UN_RSHIFT,
                                refExpr(idRef("z"))
                        )
                ),
                assignStmt(idRef("x"),
                        binop(
                                refExpr(idRef("y")),
                                TokenKind.BTW_AND,
                                refExpr(idRef("z"))
                        )
                ),
                assignStmt(idRef("x"),
                        binop(
                                refExpr(idRef("y")),
                                TokenKind.BTW_EXC_OR,
                                refExpr(idRef("z"))
                        )
                ),
                assignStmt(idRef("x"),
                        binop(
                                refExpr(idRef("y")),
                                TokenKind.BTW_INC_OR,
                                refExpr(idRef("z"))
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p",
                null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_binop_bitwise_precedence() {
        String file = "expr/binop/pass21.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                assignStmt(idRef("x"),
                    binop(
                            binop(
                                    binop(
                                            binop(
                                                    binop(
                                                            intLit(1),
                                                            TokenKind.EQ,
                                                            intLit(2)
                                                    ),
                                                    TokenKind.BTW_AND,
                                                    intLit(3)
                                            ),
                                            TokenKind.BTW_EXC_OR,
                                            intLit(4)
                                    ),
                                    TokenKind.BTW_INC_OR,
                                    intLit(5)
                            ),
                            TokenKind.AND,
                            boolLit(true)
                    )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p",
                null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_binop_shift_precedence() {
        String file = "expr/binop/pass22.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                assignStmt(idRef("x"),
                        binop(
                                binop(
                                        binop(
                                                intLit(1),
                                                TokenKind.PLUS,
                                                intLit(2)
                                        ),
                                        TokenKind.RSHIFT,
                                        intLit(3)
                                ),
                                TokenKind.LT,
                                intLit(4)
                        )
                ),
                assignStmt(idRef("x"),
                        binop(
                                binop(
                                        binop(
                                                intLit(1),
                                                TokenKind.PLUS,
                                                intLit(2)
                                        ),
                                        TokenKind.LSHIFT,
                                        intLit(3)
                                ),
                                TokenKind.LT,
                                intLit(4)
                        )
                ),
                assignStmt(idRef("x"),
                        binop(
                                binop(
                                        binop(
                                                intLit(1),
                                                TokenKind.PLUS,
                                                intLit(2)
                                        ),
                                        TokenKind.UN_RSHIFT,
                                        intLit(3)
                                ),
                                TokenKind.LT,
                                intLit(4)
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p",
                null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_float_literal() {
        String file = "expr/literal/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(
                        floatType(),
                        "f",
                        floatLit(1.0f)
                ),
                varDeclStmt(
                        floatType(),
                        "g",
                        floatLit(13.1f)
                ),
                varDeclStmt(
                        floatType(),
                        "h",
                        floatLit(200.023f)
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p",
                null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_float_literal_binop_unop() {
        String file = "expr/literal/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("id"));

        List<Asserter<Statement>> stmts = List.of(
                varDeclStmt(
                        floatType(),
                        "f",
                        binop(
                                floatLit(10.2f),
                                TokenKind.PLUS,
                                floatLit(7.4f)
                        )
                ),
                varDeclStmt(
                        floatType(),
                        "g",
                        unop(
                                TokenKind.MINUS,
                                floatLit(4.333f)
                        )
                ),
                varDeclStmt(
                        floatType(),
                        "h",
                        binop(
                                binop(
                                        floatLit(1.2f),
                                        TokenKind.MULTIPLY,
                                        floatLit(2.3f)
                                ),
                                TokenKind.DIVIDE,
                                floatLit(3.4f)
                        )
                )
        );

        Asserter<MethodDecl> p = method(Access.PACKAGE_PRIVATE, false, voidType(), "p",
                null, stmts);

        clazz(classDecls.get(0), "id", null, List.of(p));
    }

    @Test
    public void pass_constructor() {
        String file = "constructor_decl/pass1.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        Asserter<MethodDecl> constructor = method(Access.PACKAGE_PRIVATE, false, voidType(), "Test", null, null);

        clazz(classDecls.get(0), "Test", null, List.of(constructor));
    }

    @Test
    public void pass_constructor_params_and_body() {
        String file = "constructor_decl/pass2.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));
        List<Asserter<ParameterDecl>> params = List.of(
                param(intType(), "a"),
                param(boolType(), "b")
        );

        List<Asserter<Statement>> statements = List.of(
                assignStmt(idRef("a"), intLit(2))
        );

        Asserter<MethodDecl> constructor = method(Access.PACKAGE_PRIVATE, false, voidType(), "Test",
                params, statements);

        clazz(classDecls.get(0), "Test", null, List.of(constructor));
    }

    @Test
    public void pass_constructor_private() {
        String file = "constructor_decl/pass3.java";
        List<ClassDecl> classDecls = pass(file);

        classes(classDecls, List.of("Test"));

        Asserter<MethodDecl> constructor = method(Access.PRIVATE, false, voidType(), "Test", null, null);

        clazz(classDecls.get(0), "Test", null, List.of(constructor));
    }

}

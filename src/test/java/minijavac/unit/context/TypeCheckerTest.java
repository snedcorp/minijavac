package minijavac.unit.context;

import minijavac.cli.Args;
import minijavac.Compiler;
import minijavac.ast.ClassDecl;
import minijavac.listener.Listener;
import minijavac.listener.SimpleListener;
import minijavac.err.CompileError;
import minijavac.context.err.SymbolError;
import minijavac.unit.Asserter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static minijavac.unit.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class TypeCheckerTest {

    private static final Path TYPE_PATH;
    private static final Function<String, Asserter<Path>> fileAsserter;

    static {
        TYPE_PATH = Paths.get("src/test/resources/unit/type");
        fileAsserter = getFileAsserter(TYPE_PATH);
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
        Args args = new Args();
        args.files = List.of(TYPE_PATH.resolve(file));
        args.sourcePath = TYPE_PATH;

        Compiler compiler = new Compiler(listener, args);

        List<ClassDecl> classes = new ArrayList<>();

        try {
            classes = compiler.prepare();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            Assertions.fail();
        }
        return classes;
    }

    @Test
    public void fail_unop_nonInt() {
        String file = "expr/unary/fail1.java";
        List<CompileError> errs = fail(file, 6);
        assertErr(errs.get(0), isFile(file), 3, 16, "bad operand type boolean for unary operator '-'");
        assertErr(errs.get(1), isFile(file), 4, 16, "bad operand type boolean for unary operator '~'");
        assertErr(errs.get(2), isFile(file), 5, 16, "bad operand type boolean for unary operator '--'");
        assertErr(errs.get(3), isFile(file), 6, 17, "bad operand type boolean for unary operator '++'");
        assertErr(errs.get(4), isFile(file), 7, 18, "bad operand type float for unary operator '~'");
        assertErr(errs.get(5), isFile(file), 8, 16, "bad operand type Test for unary operator '++'");
    }

    @Test
    public void fail_unop_notNonBoolean() {
        String file = "expr/unary/fail2.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 3, 20, "bad operand type int for unary operator '!'");
        assertErr(errs.get(1), isFile(file), 4, 20, "bad operand type float for unary operator '!'");
    }

    @Test
    public void fail_binop_logical_nonBool() {
        String file = "expr/binary/fail1.java";
        List<CompileError> errs = fail(file, 3);
        assertTypeErr(errs.get(0), isFile(file), 3, 22, "bad operand types for binary operator '&&'", "int", "boolean");
        assertTypeErr(errs.get(1), isFile(file), 4, 25, "bad operand types for binary operator '||'", "boolean", "int");
        assertTypeErr(errs.get(2), isFile(file), 5, 22, "bad operand types for binary operator '&&'", "int", "int");
    }

    @Test
    public void fail_binop_arithmetic() {
        String file = "expr/binary/fail2.java";
        List<CompileError> errs = fail(file, 12);
        assertTypeErr(errs.get(0), isFile(file), 3, 21, "bad operand types for binary operator '+'", "boolean", "int");
        assertTypeErr(errs.get(1), isFile(file), 4, 18, "bad operand types for binary operator '-'", "int", "boolean");
        assertTypeErr(errs.get(2), isFile(file), 5, 18, "bad operand types for binary operator '*'", "int", "boolean");
        assertTypeErr(errs.get(3), isFile(file), 6, 22, "bad operand types for binary operator '/'", "boolean", "int");
        assertTypeErr(errs.get(4), isFile(file), 7, 21, "bad operand types for binary operator '+'", "boolean", "boolean");
        assertTypeErr(errs.get(5), isFile(file), 8, 21, "bad operand types for binary operator '%'", "boolean", "int");
        assertTypeErr(errs.get(6), isFile(file), 9, 18, "bad operand types for binary operator '<<'", "int", "boolean");
        assertTypeErr(errs.get(7), isFile(file), 10, 21, "bad operand types for binary operator '>>'", "boolean", "int");
        assertTypeErr(errs.get(8), isFile(file), 11, 18, "bad operand types for binary operator '>>>'", "int", "boolean");
        assertTypeErr(errs.get(9), isFile(file), 12, 22, "bad operand types for binary operator '&'", "boolean", "int");
        assertTypeErr(errs.get(10), isFile(file), 13, 18, "bad operand types for binary operator '^'", "int", "boolean");
        assertTypeErr(errs.get(11), isFile(file), 14, 21, "bad operand types for binary operator '|'", "boolean", "int");
    }

    @Test
    public void fail_binop_arithmetic_compare() {
        String file = "expr/binary/fail3.java";
        List<CompileError> errs = fail(file, 5);
        assertTypeErr(errs.get(0), isFile(file), 3, 21, "bad operand types for binary operator '<'", "boolean", "int");
        assertTypeErr(errs.get(1), isFile(file), 4, 18, "bad operand types for binary operator '>'", "int", "boolean");
        assertTypeErr(errs.get(2), isFile(file), 5, 22, "bad operand types for binary operator '<='", "boolean", "int");
        assertTypeErr(errs.get(3), isFile(file), 6, 18, "bad operand types for binary operator '>='", "int", "boolean");
        assertTypeErr(errs.get(4), isFile(file), 7, 21, "bad operand types for binary operator '<'", "boolean", "boolean");
    }

    @Test
    public void fail_binop_compare() {
        String file = "expr/binary/fail4.java";
        List<CompileError> errs = fail(file, 2);
        assertTypeErr(errs.get(0), isFile(file), 3, 21, "bad operand types for binary operator '=='", "boolean", "int");
        assertTypeErr(errs.get(1), isFile(file), 4, 18, "bad operand types for binary operator '!='", "int", "boolean");
    }

    @Test
    public void fail_binop_errPassThrough() {
        String file = "expr/binary/fail5.java";
        List<CompileError> errs = fail(file, 3);
        assertTypeErr(errs.get(0), isFile(file), 3, 18, "bad operand types for binary operator '+'", "int", "boolean");
        assertTypeErr(errs.get(1), isFile(file), 4, 23, "bad operand types for binary operator '<'", "int", "boolean");
        assertErr(errs.get(2), isFile(file), 5, 21, "bad operand type boolean for unary operator '-'");
    }

    @Test
    public void fail_varDeclStmt() {
        String file = "stmt/var_decl/fail1.java";
        List<CompileError> errs = fail(file, 8);
        assertErr(errs.get(0), isFile(file), 3, 16, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(1), isFile(file), 4, 22, "incompatible types: int cannot be converted to boolean");
        assertErr(errs.get(2), isFile(file), 5, 18, "incompatible types: Test cannot be converted to Other");
        assertErr(errs.get(3), isFile(file), 6, 19, "incompatible types: Test[] cannot be converted to Other");
        assertErr(errs.get(4), isFile(file), 7, 22, "incompatible types: Test cannot be converted to Other[]");
        assertErr(errs.get(5), isFile(file), 8, 23, "incompatible types: Test[] cannot be converted to Other[]");
        assertErr(errs.get(6), isFile(file), 9, 22, "incompatible types: Test[] cannot be converted to Test[][]");
        assertErr(errs.get(7), isFile(file), 10, 20, "incompatible types: Test[][] cannot be converted to Test[]");
    }

    @Test
    public void fail_newArrayExpr_nonIntSize() {
        String file = "expr/newarr/fail1.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 3, 26, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(1), isFile(file), 4, 32, "incompatible types: boolean cannot be converted to int");
    }

    @Test
    public void fail_ixRef_ixQRef_nonIntSize() {
        String file = "ref/fail1.java";
        List<CompileError> errs = fail(file, 4);
        assertErr(errs.get(0), isFile(file), 5, 20, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(1), isFile(file), 6, 28, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(2), isFile(file), 7, 25, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(3), isFile(file), 8, 31, "incompatible types: boolean cannot be converted to int");
    }

    @Test
    public void fail_assignToThis() {
        String file = "stmt/assign/fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 3, 8, "cannot assign to 'this'");
    }

    @Test
    public void fail_assignStmt() {
        String file = "stmt/assign/fail2.java";
        List<CompileError> errs = fail(file, 6);
        assertErr(errs.get(0), isFile(file), 10, 12, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(1), isFile(file), 11, 12, "incompatible types: int cannot be converted to boolean");
        assertErr(errs.get(2), isFile(file), 12, 13, "incompatible types: boolean[] cannot be converted to int[]");
        assertErr(errs.get(3), isFile(file), 13, 13, "incompatible types: int[] cannot be converted to boolean[]");
        assertErr(errs.get(4), isFile(file), 14, 12, "incompatible types: Other cannot be converted to Test");
        assertErr(errs.get(5), isFile(file), 15, 14, "incompatible types: Other[] cannot be converted to Test[]");
    }

    @Test
    public void fail_ifStmt() {
        String file = "stmt/if/fail1.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 3, 14, "incompatible types: int cannot be converted to boolean");
        assertErr(errs.get(1), isFile(file), 4, 16, "incompatible types: Other cannot be converted to boolean");
    }

    @Test
    public void fail_whileStmt() {
        String file = "stmt/while/fail1.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 3, 17, "incompatible types: int cannot be converted to boolean");
        assertErr(errs.get(1), isFile(file), 4, 19, "incompatible types: Other cannot be converted to boolean");
    }

    @Test
    public void fail_missingReturnValue() {
        String file = "stmt/return/fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 3, 8, "incompatible types: missing return value");
    }

    @Test
    public void fail_unexpectedReturnValue() {
        String file = "stmt/return/fail2.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 3, 15, "incompatible types: unexpected return value");
    }

    @Test
    public void fail_returnStmt() {
        String file = "stmt/return/fail3.java";
        List<CompileError> errs = fail(file, 6);
        assertErr(errs.get(0), isFile(file), 3, 15, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(1), isFile(file), 7, 15, "incompatible types: int cannot be converted to boolean");
        assertErr(errs.get(2), isFile(file), 11, 15, "incompatible types: boolean[] cannot be converted to int[]");
        assertErr(errs.get(3), isFile(file), 15, 15, "incompatible types: int[] cannot be converted to boolean[]");
        assertErr(errs.get(4), isFile(file), 19, 15, "incompatible types: Other cannot be converted to Test");
        assertErr(errs.get(5), isFile(file), 23, 15, "incompatible types: Other[] cannot be converted to Test[]");
    }

    @Test
    public void fail_callStmt_diffLenArgList() {
        String file = "stmt/call/fail1.java";
        List<CompileError> errs = fail(file, 4);
        assertArgTypeErr(errs.get(0), isFile(file), 3, 8, "method f in class Test cannot be applied to given types;",
                "no arguments", "int");
        assertArgTypeErr(errs.get(1), isFile(file), 4, 14, "method f in class Other cannot be applied to given types;",
                "no arguments", "int");
        assertArgTypeErr(errs.get(2), isFile(file), 5, 8, "method g in class Test cannot be applied to given types;",
                "int,boolean", "no arguments");
        assertArgTypeErr(errs.get(3), isFile(file), 6, 8, "method g in class Test cannot be applied to given types;",
                "int,boolean", "int,boolean,int");
    }

    @Test
    public void fail_callStmt() {
        String file = "stmt/call/fail2.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 3, 10, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(1), isFile(file), 4, 10, "incompatible types: int cannot be converted to boolean");
    }

    @Test
    public void fail_callStmt_idError_noTypeError() {
        String file = "stmt/call/fail3.java";
        List<CompileError> errs = fail(file, 1);
        assertTrue(errs.get(0) instanceof SymbolError);
    }

    @Test
    public void fail_idRef() {
        String file = "ref/fail2.java";
        List<CompileError> errs = fail(file, 4);
        assertErr(errs.get(0), isFile(file), 8, 16, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(1), isFile(file), 9, 12, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(2), isFile(file), 10, 20, "incompatible types: int cannot be converted to boolean");
        assertErr(errs.get(3), isFile(file), 11, 12, "incompatible types: int cannot be converted to boolean");
    }

    @Test
    public void fail_qRef() {
        String file = "ref/fail3.java";
        List<CompileError> errs = fail(file, 5);
        assertErr(errs.get(0), isFile(file), 7, 16, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(1), isFile(file), 8, 20, "incompatible types: int cannot be converted to boolean");
        assertErr(errs.get(2), isFile(file), 11, 12, "incompatible types: int cannot be converted to boolean");
        assertErr(errs.get(3), isFile(file), 13, 17, "incompatible types: int cannot be converted to boolean");
        assertErr(errs.get(4), isFile(file), 14, 18, "incompatible types: int cannot be converted to boolean");
    }

    @Test
    public void fail_callExpr() {
        String file = "ref/fail4.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 6, 16, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(1), isFile(file), 7, 20, "incompatible types: int cannot be converted to boolean");
    }

    @Test
    public void fail_callExpr_idError_noTypeError() {
        String file = "ref/fail5.java";
        List<CompileError> errs = fail(file, 1);
        assertTrue(errs.get(0) instanceof SymbolError);
    }

    @Test
    public void fail_ref_idError_noTypeError() {
        String file = "ref/fail6.java";
        List<CompileError> errs = fail(file, 6);
        assertTrue(errs.get(0).getMsg().contains("static context"));
        for (int i=1; i<errs.size(); i++) {
            assertTrue(errs.get(i) instanceof SymbolError);
        }
    }

    @Test
    public void fail_callRef() {
        String file = "ref/fail7.java";
        List<CompileError> errs = fail(file, 4);
        assertErr(errs.get(0), isFile(file), 5, 16, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(1), isFile(file), 6, 12, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(2), isFile(file), 7, 20, "incompatible types: int cannot be converted to boolean");
        assertErr(errs.get(3), isFile(file), 8, 12, "incompatible types: int cannot be converted to boolean");
    }

    @Test
    public void fail_returnThis() {
        String file = "stmt/return/fail4.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 3, 15, "incompatible types: Test cannot be converted to Other");
    }

    @Test
    public void fail_ixRef() {
        String file = "ref/fail8.java";
        List<CompileError> errs = fail(file, 3);
        assertErr(errs.get(0), isFile(file), 4, 13, "incompatible types: int[][] cannot be converted to int[][][]");
        assertErr(errs.get(1), isFile(file), 5, 21, "incompatible types: int[] cannot be converted to int[][]");
        assertErr(errs.get(2), isFile(file), 6, 20, "incompatible types: int cannot be converted to boolean");
    }

    @Test
    public void fail_ixRefAfterCall() {
        String file = "ref/fail9.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 5, 16, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(1), isFile(file), 6, 20, "incompatible types: int cannot be converted to boolean");
    }

    @Test
    public void fail_assignStmt_compound() {
        String file = "stmt/assign/fail3.java";
        List<CompileError> errs = fail(file, 11);
        assertTypeErr(errs.get(0), isFile(file), 5, 10, "bad operand types for binary operator '+'", "int", "boolean");
        assertTypeErr(errs.get(1), isFile(file), 6, 10, "bad operand types for binary operator '-'", "boolean", "int");
        assertTypeErr(errs.get(2), isFile(file), 7, 10, "bad operand types for binary operator '*'", "boolean", "boolean");
        assertTypeErr(errs.get(3), isFile(file), 8, 10, "bad operand types for binary operator '/'", "int", "boolean");
        assertTypeErr(errs.get(4), isFile(file), 9, 10, "bad operand types for binary operator '%'", "boolean", "int");
        assertTypeErr(errs.get(5), isFile(file), 10, 10, "bad operand types for binary operator '<<'", "boolean", "boolean");
        assertTypeErr(errs.get(6), isFile(file), 11, 10, "bad operand types for binary operator '>>'", "int", "boolean");
        assertTypeErr(errs.get(7), isFile(file), 12, 10, "bad operand types for binary operator '>>>'", "boolean", "int");
        assertTypeErr(errs.get(8), isFile(file), 13, 10, "bad operand types for binary operator '&'", "boolean", "boolean");
        assertTypeErr(errs.get(9), isFile(file), 14, 10, "bad operand types for binary operator '^'", "int", "boolean");
        assertTypeErr(errs.get(10), isFile(file), 15, 10, "bad operand types for binary operator '|'", "boolean", "int");
    }

    @Test
    public void fail_postfixExpr() {
        String file = "expr/fix/fail1.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 4, 17, "bad operand type boolean for unary operator '++'");
        assertErr(errs.get(1), isFile(file), 5, 17, "bad operand type boolean for unary operator '--'");
    }

    @Test
    public void fail_exprStatement_unary_postfix() {
        String file = "expr/fix/fail2.java";
        List<CompileError> errs = fail(file, 4);
        assertErr(errs.get(0), isFile(file), 4, 8, "bad operand type boolean for unary operator '--'");
        assertErr(errs.get(1), isFile(file), 5, 8, "bad operand type boolean for unary operator '++'");
        assertErr(errs.get(2), isFile(file), 6, 9, "bad operand type boolean for unary operator '--'");
        assertErr(errs.get(3), isFile(file), 7, 9, "bad operand type boolean for unary operator '++'");
    }

    @Test
    public void fail_unexpectedTypeError_variable_value() {
        String file = "expr/fix/fail3.java";
        List<CompileError> errs = fail(file, 6);
        assertUnexpectedTypeErr(errs.get(0), isFile(file), 3, 18);
        assertUnexpectedTypeErr(errs.get(1), isFile(file), 4, 18);
        assertUnexpectedTypeErr(errs.get(2), isFile(file), 5, 16);
        assertUnexpectedTypeErr(errs.get(3), isFile(file), 6, 16);
        assertUnexpectedTypeErr(errs.get(4), isFile(file), 7, 10);
        assertUnexpectedTypeErr(errs.get(5), isFile(file), 8, 10);
    }

    @Test
    public void fail_unexpectedTypeError_variable_value_methodRef() {
        String file = "expr/fix/fail4.java";
        List<CompileError> errs = fail(file, 6);
        assertUnexpectedTypeErr(errs.get(0), isFile(file), 3, 10);
        assertUnexpectedTypeErr(errs.get(1), isFile(file), 4, 10);
        assertUnexpectedTypeErr(errs.get(2), isFile(file), 5, 18);
        assertUnexpectedTypeErr(errs.get(3), isFile(file), 5, 26);
        assertUnexpectedTypeErr(errs.get(4), isFile(file), 5, 32);
        assertUnexpectedTypeErr(errs.get(5), isFile(file), 5, 40);
    }

    @Test
    public void fail_ternary() {
        String file = "expr/ternary/fail1.java";
        List<CompileError> errs = fail(file, 3);
        assertErr(errs.get(0), isFile(file), 4, 16, "incompatible types: int cannot be converted to boolean");
        assertErr(errs.get(1), isFile(file), 5, 18, "incompatible types: int cannot be converted to boolean");
        assertErr(errs.get(2), isFile(file), 6, 22, "incompatible types: int cannot be converted to boolean");
    }

    @Test
    public void fail_for_nonBoolCondition() {
        String file = "stmt/for/fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 3, 23, "incompatible types: int cannot be converted to boolean");
    }

    @Test
    public void fail_arr_len() {
        String file = "ref/fail10.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 4, 20, "incompatible types: int cannot be converted to boolean");
        assertErr(errs.get(1), isFile(file), 5, 26, "int cannot be dereferenced");
    }

    @Test
    public void fail_arr_init() {
        String file = "expr/newarr/fail2.java";
        List<CompileError> errs = fail(file, 12);
        assertErr(errs.get(0), isFile(file), 3, 32, "illegal initializer for int");
        assertErr(errs.get(1), isFile(file), 3, 40, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(2), isFile(file), 4, 34, "incompatible types: int cannot be converted to int[]");
        assertErr(errs.get(3), isFile(file), 4, 38, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(4), isFile(file), 4, 45, "illegal initializer for int");
        assertErr(errs.get(5), isFile(file), 4, 51, "incompatible types: int cannot be converted to int[]");
        assertErr(errs.get(6), isFile(file), 4, 54, "incompatible types: int cannot be converted to int[]");
        assertErr(errs.get(7), isFile(file), 5, 36, "incompatible types: int cannot be converted to int[][]");
        assertErr(errs.get(8), isFile(file), 5, 41, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(9), isFile(file), 5, 50, "incompatible types: int cannot be converted to int[][]");
        assertErr(errs.get(10), isFile(file), 5, 53, "incompatible types: int cannot be converted to int[][]");
        assertErr(errs.get(11), isFile(file), 6, 23, "incompatible types: int[] cannot be converted to int[][]");
    }

    @Test
    public void fail_null() {
        String file = "expr/null/fail1.java";
        List<CompileError> errs = fail(file, 3);
        assertErr(errs.get(0), isFile(file), 3, 16, "incompatible types: <null> cannot be converted to int");
        assertErr(errs.get(1), isFile(file), 4, 20, "incompatible types: <null> cannot be converted to boolean");
        assertTypeErr(errs.get(2), isFile(file), 5, 18, "bad operand types for binary operator '+'", "int", "<null>");
    }

    @Test
    public void fail_base_type_dereference() {
        String file = "ref/fail11.java";
        List<CompileError> errs = fail(file, 3);
        assertErr(errs.get(0), isFile(file), 6, 18, "int cannot be dereferenced");
        assertErr(errs.get(1), isFile(file), 7, 19, "float cannot be dereferenced");
        assertErr(errs.get(2), isFile(file), 8, 20, "boolean cannot be dereferenced");
    }

    @Test
    public void fail_lossy_conversion_float_int() {
        String file = "stmt/assign/fail4.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 4, 12, "incompatible types: possible lossy conversion from float to int");
        assertErr(errs.get(1), isFile(file), 5, 16, "incompatible types: possible lossy conversion from float to int");
    }

    @Test
    public void fail_varDecl_late_init() {
        String file = "stmt/assign/fail5.java";
        List<CompileError> errs = fail(file, 4);
        assertErr(errs.get(0), isFile(file), 9, 12, "incompatible types: Test cannot be converted to int");
        assertErr(errs.get(1), isFile(file), 10, 12, "incompatible types: int cannot be converted to Test");
        assertErr(errs.get(2), isFile(file), 11, 12, "incompatible types: boolean cannot be converted to float");
        assertErr(errs.get(3), isFile(file), 12, 12, "incompatible types: float cannot be converted to boolean");
    }

    @Test
    public void fail_doWhileStmt() {
        String file = "stmt/while/fail2.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 5, 19, "incompatible types: Other cannot be converted to boolean");
        assertErr(errs.get(1), isFile(file), 6, 19, "incompatible types: int cannot be converted to boolean");
    }

    @Test
    public void pass_unop() {
        pass("expr/unary/pass1.java");
    }

    @Test
    public void pass_binop() {
        pass("expr/binary/pass1.java");
    }

    @Test
    public void pass_varDeclStmt() {
        pass("stmt/var_decl/pass1.java");
    }

    @Test
    public void pass_newArrSize_ixSizeExpr() {
        pass("expr/newarr/pass1.java");
    }

    @Test
    public void pass_assignStmt() {
        pass("stmt/assign/pass1.java");
    }

    @Test
    public void pass_ifStmt() {
        pass("stmt/if/pass1.java");
    }

    @Test
    public void pass_whileStmt() {
        pass("stmt/while/pass1.java");
    }

    @Test
    public void pass_noReturnExpr_voidMethod() {
        pass("stmt/return/pass1.java");
    }

    @Test
    public void pass_returnStmt() {
        pass("stmt/return/pass2.java");
    }

    @Test
    public void pass_callStmt() {
        pass("stmt/call/pass1.java");
    }

    @Test
    public void pass_refs() {
        pass("ref/pass1.java");
    }

    @Test
    public void pass_returnThis() {
        pass("stmt/return/pass3.java");
    }

    @Test
    public void pass_arrays() {
        pass("ref/pass2.java");
    }

    @Test
    public void pass_ixAfterCall() {
        pass("ref/pass3.java");
    }

    @Test
    public void pass_binop_bit() {
        pass("expr/binary/pass2.java");
    }

    @Test
    public void pass_assignStmt_compound() {
        pass("stmt/assign/pass2.java");
    }

    @Test
    public void pass_increment_decrement() {
        pass("expr/fix/pass1.java");
    }

    @Test
    public void pass_ternary() {
        pass("expr/ternary/pass1.java");
    }

    @Test
    public void pass_for() {
        pass("stmt/for/pass1.java");
    }

    @Test
    public void pass_arr_len() {
        pass("ref/pass4.java");
    }

    @Test
    public void pass_arr_init() {
        pass("expr/newarr/pass2.java");
    }

    @Test
    public void pass_null() {
        pass("expr/null/pass1.java");
    }

    @Test
    public void pass_float() {
        pass("stmt/assign/pass3.java");
    }

    @Test
    public void pass_float_assign() {
        pass("stmt/assign/pass4.java");
    }

    @Test
    public void pass_varDecl_late_init() {
        pass("stmt/assign/pass5.java");
    }

    @Test
    public void pass_doWhileStmt() {
        pass("stmt/while/pass2.java");
    }
}

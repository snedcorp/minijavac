package minijavac.unit.context;

import minijavac.Compiler;
import minijavac.ast.ClassDecl;
import minijavac.cli.Args;
import minijavac.err.CompileError;
import minijavac.listener.SimpleListener;
import minijavac.syntax.TokenKind;
import minijavac.unit.Asserter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static minijavac.unit.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class ContextEnterTest {

    private static final Path PATH;
    private static final Function<String, Asserter<Path>> fileAsserter;

    static {
        PATH = Paths.get("src/test/resources/unit/enter/context");
        fileAsserter = getFileAsserter(PATH);
    }

    private static Asserter<Path> isFile(String file) {
        return fileAsserter.apply(file);
    }

    private List<ClassDecl> fail(String dir, List<String> files, Consumer<SimpleListener> errAssertions) {
        SimpleListener listener = new SimpleListener();
        return test(dir, files, listener, errAssertions);
    }

    private List<ClassDecl> pass(String dir, List<String> files) {
        SimpleListener listener = new SimpleListener();
        Consumer<SimpleListener> errAssertions = l -> assertFalse(l.hasErrors());
        List<ClassDecl> classDecls = test(dir, files, listener, errAssertions);
        assertFalse(listener.hasErrors());
        return classDecls;
    }

    private List<ClassDecl> test(String dir, List<String> files, SimpleListener listener, Consumer<SimpleListener> errAssertions) {
        Args args = new Args();
        Path dirPath = PATH.resolve(dir);
        args.files = files.stream().map(dirPath::resolve).collect(Collectors.toList());
        args.sourcePath = dirPath;

        Compiler compiler = new Compiler(listener, args);

        List<ClassDecl> classes = new ArrayList<>();
        try {
            classes = compiler.prepare();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            Assertions.fail();
        }

        errAssertions.accept(listener);

        return classes;
    }

    @Test
    public void find_single_other_file_err() {
        String dir = "1";
        String file = "Test.java";

        Asserter<Path> fileAsserter = isFile(String.format("%s/%s", dir, file));

        Consumer<SimpleListener> errAssertions = listener -> {
            assertEquals(2, listener.getErrCnt());
            List<CompileError> errs = listener.getErrors();
            assertSymbolErr(errs.get(0), fileAsserter, 5, 10,
                    "variable x", "variable o of type Other");
            assertErr(errs.get(1), fileAsserter, 6, 14, "incompatible types: int cannot be converted to boolean");
        };

        fail(dir, List.of(file), errAssertions);
    }

    @Test
    public void find_parse_error_stops_chain() {
        String dir = "2";
        String file = "Test.java";

        Consumer<SimpleListener> errAssertions = listener -> {
            assertEquals(2, listener.getErrCnt());
            List<CompileError> errs = listener.getErrors();
            assertExpParseErr(errs.get(0), TokenKind.IDENTIFIER, 2, 12, TokenKind.IDENTIFIER,
                    isFile(String.format("%s/%s", dir, "Other.java")), 3, 4, TokenKind.LPAREN);
            assertSymbolErr(errs.get(1), isFile(String.format("%s/%s", dir, "Test.java")), 2, 4,
                    "class Other", "class Test");
        };

        List<ClassDecl> classes = fail(dir, List.of(file), errAssertions);

        assertEquals(1, classes.size());
        assertEquals("Test", classes.get(0).id.contents);
    }

    @Test
    public void find_context_errors() {
        String dir = "3";
        String file = "Test.java";

        Consumer<SimpleListener> errAssertions = listener -> {
            assertEquals(5, listener.getErrCnt());
            List<CompileError> errs = listener.getErrors();
            assertErr(errs.get(0), isFile(String.format("%s/%s", dir, "Test.java")), 5, 14,
                    "incompatible types: boolean cannot be converted to int");
            assertArgTypeErr(errs.get(1), isFile(String.format("%s/%s", dir, "A.java")), 6, 16,
                    "method getC in class B cannot be applied to given types;", "no arguments", "int");
            assertArgTypeErr(errs.get(2), isFile(String.format("%s/%s", dir, "B.java")), 3, 19,
                    "constructor C in class C cannot be applied to given types;", "int", "no arguments");
            assertErr(errs.get(3), isFile(String.format("%s/%s", dir, "B.java")), 8, 16,
                    "incompatible types: possible lossy conversion from float to int");
            assertSymbolErr(errs.get(4), isFile(String.format("%s/%s", dir, "C.java")), 9, 18, "variable y",
                    "class A");
        };

        List<ClassDecl> classes = fail(dir, List.of(file), errAssertions);

        assertEquals(4, classes.size());
    }

    @Test
    public void find_context_pass() {
        String dir = "4";
        String file = "Test.java";

        Consumer<SimpleListener> errAssertions = listener -> {
            assertEquals(0, listener.getErrCnt());
        };

        List<ClassDecl> classes = fail(dir, List.of(file), errAssertions);

        assertEquals(4, classes.size());
    }
}

package minijavac.unit.context;

import minijavac.Compiler;
import minijavac.ast.*;
import minijavac.cli.Args;
import minijavac.context.SymbolTable;
import minijavac.err.CompileError;
import minijavac.listener.SimpleListener;
import minijavac.unit.Asserter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static minijavac.unit.TestUtils.assertErr;
import static minijavac.unit.TestUtils.getFileAsserter;
import static org.junit.jupiter.api.Assertions.*;

public class EnterTest {

    private static final Path ENTER_PATH;
    private static final Function<String, Asserter<Path>> fileAsserter;

    static {
        ENTER_PATH = Paths.get("src/test/resources/unit/enter");
        fileAsserter = getFileAsserter(ENTER_PATH);
    }

    private static Asserter<Path> isFile(String file) {
        return fileAsserter.apply(file);
    }

    private void test(String dir, List<String> files, Consumer<SymbolTable> symbolAssertions, Consumer<SimpleListener>
            errAssertions) {
        test(dir, files, symbolAssertions, errAssertions, null);
    }

    private void test(String dir, List<String> files, Consumer<IOException> exAssertions) {
        test(dir, files, null, null, exAssertions);
    }

    private void test(String dir, List<String> files, Consumer<SymbolTable> symbolAssertions, Consumer<SimpleListener>
            errAssertions, Consumer<IOException> exAssertions) {
        SimpleListener listener = new SimpleListener();

        Args args = new Args();
        Path dirPath = ENTER_PATH.resolve(dir);
        args.files = files.stream().map(dirPath::resolve).collect(Collectors.toList());
        args.sourcePath = dirPath;

        Compiler compiler = new Compiler(listener, args);

        List<ClassDecl> classes = null;

        try {
            classes = compiler.parseAndEnter();
        } catch (IOException ex) {
            assertNotNull(exAssertions);
            exAssertions.accept(ex);
            return;
        }

        listener.setIgnore(true);
        symbolAssertions.accept(compiler.getSymbolTable());
        errAssertions.accept(listener);
    }

    private void assertClass(SymbolTable symbolTable, String classname, List<String> fields) {
        Declaration clazz = symbolTable.getClassDecl(Identifier.of(classname));
        assertNotNull(clazz);

        for (String field : fields) {
            FieldDecl fieldDecl = (FieldDecl) symbolTable.getMemberDecl(classname, Identifier.of(field),
                    false, null);
            assertNotNull(fieldDecl);
            assertEquals(clazz, fieldDecl.classDecl);
        }
    }

    @Test
    public void single_file_single_class() {
        Consumer<SymbolTable> symbolAssertions = symbolTable -> {
            Declaration testClass = symbolTable.getClassDecl(Identifier.of("Test"));
            assertNotNull(testClass);
            FieldDecl fieldA = (FieldDecl) symbolTable.getMemberDecl("Test", Identifier.of("a"),
                    false, null);
            assertNotNull(fieldA);
            assertEquals(testClass, fieldA.classDecl);

            SymbolTable.ArgType argType = new SymbolTable.ArgType(null, new BaseType(TypeKind.INT, null));
            SymbolTable.ArgTypes argTypes = new SymbolTable.ArgTypes(List.of(argType), null);
            symbolTable.pushArgTypes(argTypes);

            MethodDecl methodMain = (MethodDecl) symbolTable.getMemberDecl("Test", Identifier.of("main"),
                    true, null);
            assertNotNull(methodMain);
            assertEquals(testClass, methodMain.classDecl);
        };

        Consumer<SimpleListener> errAssertions = listener -> {
            assertFalse(listener.hasErrors());
        };

        test("1", List.of("Test.java"), symbolAssertions, errAssertions);
    }

    @Test
    public void single_file_multi_class() {
        Consumer<SymbolTable> symbolAssertions = symbolTable -> {
            Declaration testClass = symbolTable.getClassDecl(Identifier.of("Test"));
            assertNotNull(testClass);
            FieldDecl fieldA = (FieldDecl) symbolTable.getMemberDecl("Test", Identifier.of("a"),
                    false, null);
            assertNotNull(fieldA);
            assertEquals(testClass, fieldA.classDecl);

            SymbolTable.ArgType argType = new SymbolTable.ArgType(null, new BaseType(TypeKind.INT, null));
            SymbolTable.ArgTypes argTypes = new SymbolTable.ArgTypes(List.of(argType), null);
            symbolTable.pushArgTypes(argTypes);

            MethodDecl methodMain = (MethodDecl) symbolTable.getMemberDecl("Test", Identifier.of("main"),
                    true, null);
            assertNotNull(methodMain);
            assertEquals(testClass, methodMain.classDecl);

            Declaration otherClass = symbolTable.getClassDecl(Identifier.of("Other"));
            assertNotNull(otherClass);
            FieldDecl fieldB = (FieldDecl) symbolTable.getMemberDecl("Other", Identifier.of("b"),
                    false, null);
            assertNotNull(fieldB);
            assertEquals(otherClass, fieldB.classDecl);

            argType = new SymbolTable.ArgType(null, new BaseType(TypeKind.FLOAT, null));
            argTypes = new SymbolTable.ArgTypes(List.of(argType), null);
            symbolTable.pushArgTypes(argTypes);

            methodMain = (MethodDecl) symbolTable.getMemberDecl("Other", Identifier.of("main"),
                    true, null);
            assertNotNull(methodMain);
            assertEquals(otherClass, methodMain.classDecl);
        };

        Consumer<SimpleListener> errAssertions = listener -> {
            assertFalse(listener.hasErrors());
        };

        test("2", List.of("Test.java"), symbolAssertions, errAssertions);
    }

    @Test
    public void multi_file() {
        Consumer<SymbolTable> symbolAssertions = symbolTable -> {
            Declaration testClass = symbolTable.getClassDecl(Identifier.of("Test"));
            assertNotNull(testClass);
            FieldDecl fieldA = (FieldDecl) symbolTable.getMemberDecl("Test", Identifier.of("a"),
                    false, null);
            assertNotNull(fieldA);
            assertEquals(testClass, fieldA.classDecl);

            SymbolTable.ArgType argType = new SymbolTable.ArgType(null, new BaseType(TypeKind.INT, null));
            SymbolTable.ArgTypes argTypes = new SymbolTable.ArgTypes(List.of(argType), null);
            symbolTable.pushArgTypes(argTypes);

            MethodDecl methodMain = (MethodDecl) symbolTable.getMemberDecl("Test", Identifier.of("main"),
                    true, null);
            assertNotNull(methodMain);
            assertEquals(testClass, methodMain.classDecl);

            Declaration otherClass = symbolTable.getClassDecl(Identifier.of("Other"));
            assertNotNull(otherClass);
            FieldDecl fieldB = (FieldDecl) symbolTable.getMemberDecl("Other", Identifier.of("b"),
                    false, null);
            assertNotNull(fieldB);
            assertEquals(otherClass, fieldB.classDecl);

            argType = new SymbolTable.ArgType(null, new BaseType(TypeKind.FLOAT, null));
            argTypes = new SymbolTable.ArgTypes(List.of(argType), null);
            symbolTable.pushArgTypes(argTypes);

            methodMain = (MethodDecl) symbolTable.getMemberDecl("Other", Identifier.of("main"),
                    true, null);
            assertNotNull(methodMain);
            assertEquals(otherClass, methodMain.classDecl);
        };

        Consumer<SimpleListener> errAssertions = listener -> {
            assertFalse(listener.hasErrors());
        };

        test("3", List.of("Test.java", "Other.java"), symbolAssertions, errAssertions);
    }

    @Test
    public void multi_file_duplicateClass() {
        String dir = "4";
        Consumer<SymbolTable> symbolAssertions = symbolTable -> {
            assertClass(symbolTable, "Test", List.of("x"));
        };

        Consumer<SimpleListener> errAssertions = listener -> {
            List<CompileError> errs = listener.getErrors();
            assertEquals(1, errs.size());
            assertErr(errs.get(0), isFile(String.format("%s/Other.java", dir)), 1, 0, "duplicate class: Test");
        };

        test(dir, List.of("Test.java", "Other.java"), symbolAssertions, errAssertions);
    }

    @Test
    public void single_file_find_duplicateClass() {
        String dir = "5";
        Consumer<SymbolTable> symbolAssertions = symbolTable -> {
            assertClass(symbolTable, "Test", List.of("o", "x"));
        };

        Consumer<SimpleListener> errAssertions = listener -> {
            List<CompileError> errs = listener.getErrors();
            assertEquals(1, errs.size());
            assertErr(errs.get(0), isFile(String.format("%s/Other.java", dir)), 1, 0, "duplicate class: Test");
        };

        test(dir, List.of("Test.java"), symbolAssertions, errAssertions);
    }

    @Test
    public void single_file_find_field_success() {
        String dir = "6";
        Consumer<SymbolTable> symbolAssertions = symbolTable -> {
            assertClass(symbolTable, "Test", List.of("o"));
            assertClass(symbolTable, "Other", List.of("x"));
        };

        Consumer<SimpleListener> errAssertions = listener -> {
            assertFalse(listener.hasErrors());
        };

        test(dir, List.of("Test.java"), symbolAssertions, errAssertions);
    }

    @Test
    public void single_file_find_multi_ref_success() {
        String dir = "7";
        Consumer<SymbolTable> symbolAssertions = symbolTable -> {
            assertClass(symbolTable, "Test", List.of("o"));
            assertClass(symbolTable, "Other", List.of("x"));
        };

        Consumer<SimpleListener> errAssertions = listener -> {
            assertFalse(listener.hasErrors());
        };

        test(dir, List.of("Test.java"), symbolAssertions, errAssertions);
    }

    @Test
    public void single_file_find_many_sources_success() {
        String dir = "8";
        Consumer<SymbolTable> symbolAssertions = symbolTable -> {
            assertClass(symbolTable, "Test", List.of("a"));
            assertClass(symbolTable, "A", List.of("val"));
            assertClass(symbolTable, "B", List.of("val"));
            assertClass(symbolTable, "C", List.of("val"));
            assertClass(symbolTable, "D", List.of("val"));
            assertClass(symbolTable, "E", List.of("val"));
            assertClass(symbolTable, "F", List.of("val"));
            // hacky, these classes shouldn't be in symbol table, so error is added, but b/c there's no active
            // traversal state in the symbol table, null pointer when building error
            // TODO: make this better
            assertThrows(Exception.class, () -> symbolTable.getClassDecl(Identifier.of("G")));
            assertThrows(Exception.class, () -> symbolTable.getClassDecl(Identifier.of("H")));
            assertThrows(Exception.class, () -> symbolTable.getClassDecl(Identifier.of("I")));
            assertThrows(Exception.class, () -> symbolTable.getClassDecl(Identifier.of("J")));
            assertThrows(Exception.class, () -> symbolTable.getClassDecl(Identifier.of("K")));
        };

        Consumer<SimpleListener> errAssertions = listener -> {
            assertFalse(listener.hasErrors());
        };

        test(dir, List.of("Test.java"), symbolAssertions, errAssertions);
    }

    @Test
    public void single_file_find_chain_success() {
        String dir = "9";
        Consumer<SymbolTable> symbolAssertions = symbolTable -> {
            assertClass(symbolTable, "Test", List.of("a"));
            assertClass(symbolTable, "A", List.of("b"));
            assertClass(symbolTable, "B", List.of("c"));
            assertClass(symbolTable, "C", List.of("d"));
            assertClass(symbolTable, "D", List.of("e"));
            assertClass(symbolTable, "E", List.of("f"));
            assertClass(symbolTable, "F", List.of("val"));
        };

        Consumer<SimpleListener> errAssertions = listener -> {
            assertFalse(listener.hasErrors());
        };

        test(dir, List.of("Test.java"), symbolAssertions, errAssertions);
    }

    @Test
    public void single_file_find_enqueue_attempt_when_already_in_queue() {
        String dir = "10";
        Consumer<SymbolTable> symbolAssertions = symbolTable -> {
            assertClass(symbolTable, "Test", List.of("a"));
            assertClass(symbolTable, "A", List.of("b"));
            assertClass(symbolTable, "B", List.of("c"));
            assertClass(symbolTable, "C", List.of("val"));
        };

        Consumer<SimpleListener> errAssertions = listener -> {
            assertFalse(listener.hasErrors());
        };

        test(dir, List.of("Test.java"), symbolAssertions, errAssertions);
    }

    @Test
    public void single_file_find_enqueue_attempt_when_was_already_in_queue() {
        String dir = "11";
        Consumer<SymbolTable> symbolAssertions = symbolTable -> {
            assertClass(symbolTable, "Test", List.of("a"));
            assertClass(symbolTable, "A", List.of("t", "b"));
            assertClass(symbolTable, "B", List.of("c", "a"));
            assertClass(symbolTable, "C", List.of("b"));
        };

        Consumer<SimpleListener> errAssertions = listener -> {
            assertFalse(listener.hasErrors());
        };

        test(dir, List.of("Test.java"), symbolAssertions, errAssertions);
    }

    @Test
    public void single_file_selective_find() {
        String dir = "12";
        Consumer<SymbolTable> symbolAssertions = symbolTable -> {
            assertClass(symbolTable, "Test", List.of("A"));
            assertClass(symbolTable, "D", List.of("val"));
        };

        Consumer<SimpleListener> errAssertions = listener -> {
            assertFalse(listener.hasErrors());
        };

        test(dir, List.of("Test.java"), symbolAssertions, errAssertions);
    }

    @Test
    public void single_file_no_find() {
        String dir = "13";
        Consumer<SymbolTable> symbolAssertions = symbolTable -> {
            assertClass(symbolTable, "Test", List.of("t", "o"));
            assertClass(symbolTable, "Other", List.of("t"));
        };

        Consumer<SimpleListener> errAssertions = listener -> {
            assertFalse(listener.hasErrors());
        };

        test(dir, List.of("Test.java"), symbolAssertions, errAssertions);
    }

    @Test
    public void user_file_not_found() {
        String dir = "14";

        Consumer<IOException> exAssertions = ex -> {
            assertTrue(ex.getMessage().endsWith("Fake.java"));
        };

        test(dir, List.of("Fake.java"), exAssertions);
    }

    @Test
    public void user_file_not_found_other_valid_files() {
        String dir = "14";

        Consumer<IOException> exAssertions = ex -> {
            assertTrue(ex.getMessage().endsWith("Fake.java"));
        };

        test(dir, List.of("Test.java", "Fake.java"), exAssertions);
    }

}

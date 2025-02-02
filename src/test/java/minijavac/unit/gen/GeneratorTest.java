package minijavac.unit.gen;

import minijavac.cli.Args;
import minijavac.Compiler;
import minijavac.ast.ClassDecl;
import minijavac.listener.SimpleListener;
import minijavac.gen.file.ClassFile;
import minijavac.unit.Asserter;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static minijavac.unit.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class GeneratorTest {

    private static final Path GEN_PATH;

    static {
        GEN_PATH = Paths.get("src/test/resources/unit/gen");
    }

    private JavaClass testWithConstructor(String file) {
        return testWithConstructor(file, "Test");
    }

    private JavaClass testWithConstructor(String file, String classWithConstructor) {
        return test(List.of(file), Set.of(classWithConstructor), null).get(0);
    }

    private JavaClass test(String file) {
        return test(List.of(file), new HashSet<>(), null).get(0);
    }

    private List<JavaClass> test(List<String> files, Set<String> hasConstructor, String dir) {
        SimpleListener listener = new SimpleListener();

        Args args = new Args();
        Path dirPath = dir == null ? GEN_PATH : GEN_PATH.resolve(dir);
        args.files = files.stream().map(dirPath::resolve).collect(Collectors.toList());
        args.sourcePath = dirPath;

        Compiler compiler = new Compiler(listener, args);

        try {
            List<ClassDecl> classes = compiler.prepare();
            List<ClassFile> classFiles = compiler.generate(classes);

            List<JavaClass> parsedClasses = new ArrayList<>();
            for (int i=0; i<classFiles.size(); i++) {
                ClassFile classFile = classFiles.get(i);

                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                DataOutputStream stream = new DataOutputStream(byteOutputStream);
                classFile.writeTo(stream);

                ByteArrayInputStream byteInputStream = new ByteArrayInputStream(byteOutputStream.toByteArray());
                ClassParser classParser = new ClassParser(byteInputStream, classFile.getClassName());
                JavaClass parsedClass = classParser.parse();

                assertClass(parsedClass, classes.get(i).pos.file(), classFile.getClassName(),
                        hasConstructor.contains(parsedClass.getClassName()));
                parsedClasses.add(parsedClass);
            }
            return parsedClasses;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            Assertions.fail();
        }

        return null;
    }

    private void assertClass(JavaClass testClass, Path file, String className,
                                 boolean hasConstructor) {
        assertNotNull(testClass);
        assertEquals(0, testClass.getMinor());
        assertEquals(61, testClass.getMajor());
        assertEquals(className, testClass.getClassName());
        assertEquals(32, testClass.getAccessFlags());

        ConstantClass superClassConstant = testClass.getConstantPool()
                .getConstant(testClass.getSuperclassNameIndex(), ConstantClass.class);
        assertEquals("java/lang/Object",
                testClass.getConstantPool().getConstantUtf8(superClassConstant.getNameIndex()).getBytes());

        if (hasConstructor) return;

        assertTrue(testClass.getMethods().length > 0);
        Method constructor = testClass.getMethods()[0];
        assertEquals("<init>", constructor.getName());
        assertEquals("()V", constructor.getSignature());
        assertEquals(0, constructor.getAccessFlags());

        assertEquals(1, constructor.getCode().getMaxLocals());
        assertEquals(1, constructor.getCode().getMaxStack());

        List<String> instructions = List.of(
                "aload_0",
                "invokespecial java/lang/Object/<init>()V", // trailing forward slash added by lib
                "return"
        );

        assertInstructions(constructor.getCode(), testClass.getConstantPool(), instructions);

        assertEquals(1, testClass.getAttributes().length);
        assertTrue(testClass.getAttributes()[0] instanceof SourceFile);
        assertSourceFileAttribute(file, testClass.getAttributes()[0]);
    }

    private void assertInstructions(Code code, ConstantPool constantPool, List<String> expected) {
        List<String> actual = getInstructions(code, constantPool);
        assertEquals(expected.size(), actual.size());
        for (int i=0; i<expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }

    private List<String> getInstructions(Code code, ConstantPool constantPool) {
        InstructionList il = new InstructionList(code.getCode());

        List<String> instructions = new ArrayList<>();

        Iterator<InstructionHandle> iter = il.iterator();

        while (iter.hasNext()) {
            instructions.add(iter.next().getInstruction().toString(constantPool));
        }

        return instructions;
    }

    private void assertSourceFileAttribute(Path file, Attribute attribute) {
        SourceFile sourceFile = (SourceFile) attribute;
        assertEquals(file.getFileName().toString(), sourceFile.getSourceFileName());
    }

    @Test
    public void emptyClass() {
        test("empty.java");
    }

    @Test
    public void field_int() {
        JavaClass res = test("field/1.java");
        assert res != null;
        assertEquals(12, res.getFields().length);

        Field ipub = res.getFields()[0];
        assertEquals("ipub", ipub.getName());
        assertEquals("I", ipub.getSignature());
        assertEquals(1, ipub.getAccessFlags());

        Field ipriv = res.getFields()[1];
        assertEquals("ipriv", ipriv.getName());
        assertEquals("I", ipriv.getSignature());
        assertEquals(2, ipriv.getAccessFlags());

        Field ipubstat = res.getFields()[2];
        assertEquals("ipubstat", ipubstat.getName());
        assertEquals("I", ipubstat.getSignature());
        assertEquals(9, ipubstat.getAccessFlags());

        Field iprivstat = res.getFields()[3];
        assertEquals("iprivstat", iprivstat.getName());
        assertEquals("I", iprivstat.getSignature());
        assertEquals(10, iprivstat.getAccessFlags());

        Field i = res.getFields()[4];
        assertEquals("i", i.getName());
        assertEquals("I", i.getSignature());
        assertEquals(0, i.getAccessFlags());

        Field istat = res.getFields()[5];
        assertEquals("istat", istat.getName());
        assertEquals("I", istat.getSignature());
        assertEquals(8, istat.getAccessFlags());

        Field ipubfin = res.getFields()[6];
        assertEquals("ipubfin", ipubfin.getName());
        assertEquals("I", ipubfin.getSignature());
        assertEquals(17, ipubfin.getAccessFlags());

        Field iprivfin = res.getFields()[7];
        assertEquals("iprivfin", iprivfin.getName());
        assertEquals("I", iprivfin.getSignature());
        assertEquals(18, iprivfin.getAccessFlags());

        Field ipubstatfin = res.getFields()[8];
        assertEquals("ipubstatfin", ipubstatfin.getName());
        assertEquals("I", ipubstatfin.getSignature());
        assertEquals(25, ipubstatfin.getAccessFlags());

        Field iprivstatfin = res.getFields()[9];
        assertEquals("iprivstatfin", iprivstatfin.getName());
        assertEquals("I", iprivstatfin.getSignature());
        assertEquals(26, iprivstatfin.getAccessFlags());

        Field ifin = res.getFields()[10];
        assertEquals("ifin", ifin.getName());
        assertEquals("I", ifin.getSignature());
        assertEquals(16, ifin.getAccessFlags());

        Field istatfin = res.getFields()[11];
        assertEquals("istatfin", istatfin.getName());
        assertEquals("I", istatfin.getSignature());
        assertEquals(24, istatfin.getAccessFlags());
    }

    @Test
    public void field_bool() {
        JavaClass res = test("field/2.java");
        assert res != null;
        assertEquals(4, res.getFields().length);

        Field bpub = res.getFields()[0];
        assertEquals("bpub", bpub.getName());
        assertEquals("Z", bpub.getSignature());
        assertEquals(1, bpub.getAccessFlags());

        Field bpriv = res.getFields()[1];
        assertEquals("bpriv", bpriv.getName());
        assertEquals("Z", bpriv.getSignature());
        assertEquals(2, bpriv.getAccessFlags());

        Field bpubstat = res.getFields()[2];
        assertEquals("bpubstat", bpubstat.getName());
        assertEquals("Z", bpubstat.getSignature());
        assertEquals(9, bpubstat.getAccessFlags());

        Field bprivstat = res.getFields()[3];
        assertEquals("bprivstat", bprivstat.getName());
        assertEquals("Z", bprivstat.getSignature());
        assertEquals(10, bprivstat.getAccessFlags());
    }

    @Test
    public void method_void() {
        JavaClass res = test("method/1.java");
        assert res != null;
        assertEquals(7, res.getMethods().length);

        Method vpub = res.getMethods()[1];
        assertEquals("vpub", vpub.getName());
        assertEquals("()V", vpub.getSignature());
        assertEquals(1, vpub.getAccessFlags());

        Method vpriv = res.getMethods()[2];
        assertEquals("vpriv", vpriv.getName());
        assertEquals("()V", vpriv.getSignature());
        assertEquals(2, vpriv.getAccessFlags());

        Method vpubstat = res.getMethods()[3];
        assertEquals("vpubstat", vpubstat.getName());
        assertEquals("()V", vpubstat.getSignature());
        assertEquals(9, vpubstat.getAccessFlags());

        Method vprivstat = res.getMethods()[4];
        assertEquals("vprivstat", vprivstat.getName());
        assertEquals("()V", vprivstat.getSignature());
        assertEquals(10, vprivstat.getAccessFlags());

        Method v = res.getMethods()[5];
        assertEquals("v", v.getName());
        assertEquals("()V", v.getSignature());
        assertEquals(0, v.getAccessFlags());

        Method vstat = res.getMethods()[6];
        assertEquals("vstat", vstat.getName());
        assertEquals("()V", vstat.getSignature());
        assertEquals(8, vstat.getAccessFlags());
    }

    @Test
    public void method_int() {
        JavaClass res = test("method/2.java");
        assert res != null;
        assertEquals(7, res.getMethods().length);

        Method ipub = res.getMethods()[1];
        assertEquals("ipub", ipub.getName());
        assertEquals("()I", ipub.getSignature());
        assertEquals(1, ipub.getAccessFlags());

        Method ipriv = res.getMethods()[2];
        assertEquals("ipriv", ipriv.getName());
        assertEquals("()I", ipriv.getSignature());
        assertEquals(2, ipriv.getAccessFlags());

        Method ipubstat = res.getMethods()[3];
        assertEquals("ipubstat", ipubstat.getName());
        assertEquals("()I", ipubstat.getSignature());
        assertEquals(9, ipubstat.getAccessFlags());

        Method iprivstat = res.getMethods()[4];
        assertEquals("iprivstat", iprivstat.getName());
        assertEquals("()I", iprivstat.getSignature());
        assertEquals(10, iprivstat.getAccessFlags());

        Method i = res.getMethods()[5];
        assertEquals("i", i.getName());
        assertEquals("()I", i.getSignature());
        assertEquals(0, i.getAccessFlags());

        Method istat = res.getMethods()[6];
        assertEquals("istat", istat.getName());
        assertEquals("()I", istat.getSignature());
        assertEquals(8, istat.getAccessFlags());
    }

    @Test
    public void method_bool() {
        JavaClass res = test("method/3.java");
        assert res != null;
        assertEquals(7, res.getMethods().length);

        Method bpub = res.getMethods()[1];
        assertEquals("bpub", bpub.getName());
        assertEquals("()Z", bpub.getSignature());
        assertEquals(1, bpub.getAccessFlags());

        Method bpriv = res.getMethods()[2];
        assertEquals("bpriv", bpriv.getName());
        assertEquals("()Z", bpriv.getSignature());
        assertEquals(2, bpriv.getAccessFlags());

        Method bpubstat = res.getMethods()[3];
        assertEquals("bpubstat", bpubstat.getName());
        assertEquals("()Z", bpubstat.getSignature());
        assertEquals(9, bpubstat.getAccessFlags());

        Method bprivstat = res.getMethods()[4];
        assertEquals("bprivstat", bprivstat.getName());
        assertEquals("()Z", bprivstat.getSignature());
        assertEquals(10, bprivstat.getAccessFlags());

        Method b = res.getMethods()[5];
        assertEquals("b", b.getName());
        assertEquals("()Z", b.getSignature());
        assertEquals(0, b.getAccessFlags());

        Method bstat = res.getMethods()[6];
        assertEquals("bstat", bstat.getName());
        assertEquals("()Z", bstat.getSignature());
        assertEquals(8, bstat.getAccessFlags());
    }

    @Test
    public void method_class() {
        JavaClass res = test("method/4.java");
        assert res != null;
        assertEquals(7, res.getMethods().length);

        Method clspub = res.getMethods()[1];
        assertEquals("clspub", clspub.getName());
        assertEquals("()LOther;", clspub.getSignature());
        assertEquals(1, clspub.getAccessFlags());

        Method clspriv = res.getMethods()[2];
        assertEquals("clspriv", clspriv.getName());
        assertEquals("()LOther;", clspriv.getSignature());
        assertEquals(2, clspriv.getAccessFlags());

        Method clspubstat = res.getMethods()[3];
        assertEquals("clspubstat", clspubstat.getName());
        assertEquals("()LOther;", clspubstat.getSignature());
        assertEquals(9, clspubstat.getAccessFlags());

        Method clsprivstat = res.getMethods()[4];
        assertEquals("clsprivstat", clsprivstat.getName());
        assertEquals("()LOther;", clsprivstat.getSignature());
        assertEquals(10, clsprivstat.getAccessFlags());

        Method cls = res.getMethods()[5];
        assertEquals("cls", cls.getName());
        assertEquals("()LOther;", cls.getSignature());
        assertEquals(0, cls.getAccessFlags());

        Method clsstat = res.getMethods()[6];
        assertEquals("clsstat", clsstat.getName());
        assertEquals("()LOther;", clsstat.getSignature());
        assertEquals(8, clsstat.getAccessFlags());
    }

    @Test
    public void method_array() {
        JavaClass res = test("method/5.java");
        assert res != null;
        assertEquals(7, res.getMethods().length);

        Method arrpub = res.getMethods()[1];
        assertEquals("arrpub", arrpub.getName());
        assertEquals("()[I", arrpub.getSignature());
        assertEquals(1, arrpub.getAccessFlags());

        Method arrpriv = res.getMethods()[2];
        assertEquals("arrpriv", arrpriv.getName());
        assertEquals("()[Z", arrpriv.getSignature());
        assertEquals(2, arrpriv.getAccessFlags());

        Method arrpubstat = res.getMethods()[3];
        assertEquals("arrpubstat", arrpubstat.getName());
        assertEquals("()[LOther;", arrpubstat.getSignature());
        assertEquals(9, arrpubstat.getAccessFlags());

        Method arrprivstat = res.getMethods()[4];
        assertEquals("arrprivstat", arrprivstat.getName());
        assertEquals("()[LOther;", arrprivstat.getSignature());
        assertEquals(10, arrprivstat.getAccessFlags());

        Method arr = res.getMethods()[5];
        assertEquals("arr", arr.getName());
        assertEquals("()[LOther;", arr.getSignature());
        assertEquals(0, arr.getAccessFlags());

        Method arrstat = res.getMethods()[6];
        assertEquals("arrstat", arrstat.getName());
        assertEquals("()[LOther;", arrstat.getSignature());
        assertEquals(8, arrstat.getAccessFlags());
    }

    @Test
    public void method_params() {
        JavaClass res = test("method/6.java");
        assert res != null;
        assertEquals(6, res.getMethods().length);

        Method a = res.getMethods()[1];
        assertEquals("a", a.getName());
        assertEquals("(I)V", a.getSignature());

        Method b = res.getMethods()[2];
        assertEquals("b", b.getName());
        assertEquals("(Z)V", b.getSignature());

        Method c = res.getMethods()[3];
        assertEquals("c", c.getName());
        assertEquals("(LOther;)V", c.getSignature());

        Method d = res.getMethods()[4];
        assertEquals("d", d.getName());
        assertEquals("([I)V", d.getSignature());

        Method e = res.getMethods()[5];
        assertEquals("e", e.getName());
        assertEquals("(IZLOther;[ILjava/lang/String;[LOther;)I", e.getSignature());
    }

    @Test
    public void method_params_localVarCount() {
        JavaClass res = test("method/7.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        Method f = res.getMethods()[1];
        assertEquals(0, f.getAccessFlags());
        assertEquals(7, f.getCode().getMaxLocals());

        Method fstat = res.getMethods()[2];
        assertEquals(0, f.getAccessFlags());
        assertEquals(6, fstat.getCode().getMaxLocals());
    }

    @Test
    public void literal_int_iconst() {
        JavaClass res = test("lit/int/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);
        assertEquals(3, res.getMethods()[1].getCode().getMaxLocals());

        List<String> instructions = List.of(
                "iconst_0", "iconst_1", "iadd", "istore_0",
                "iconst_2", "iconst_3", "iadd", "istore_1",
                "iconst_4", "iconst_5", "iadd", "istore_2",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void literal_int_bipush() {
        JavaClass res = test("lit/int/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);
        assertEquals(2, res.getMethods()[1].getCode().getMaxLocals());

        List<String> instructions = List.of(
                "bipush 6", "istore_0",
                "bipush 127", "istore_1",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void literal_int_sipush() {
        JavaClass res = test("lit/int/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);
        assertEquals(2, res.getMethods()[1].getCode().getMaxLocals());

        List<String> instructions = List.of(
                "sipush 128", "istore_0",
                "sipush 32767", "istore_1",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void literal_int_ldc() {
        JavaClass res = test("lit/int/4.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);
        assertEquals(2, res.getMethods()[1].getCode().getMaxLocals());

        List<String> instructions = List.of(
                "ldc 32768", "istore_0",
                "ldc 2147483647", "istore_1",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void literal_boolean() {
        JavaClass res = test("lit/bool/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);
        assertEquals(2, res.getMethods()[1].getCode().getMaxLocals());

        List<String> instructions = List.of(
                "iconst_1", "istore_0",
                "iconst_0", "istore_1",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_unary_minus() {
        JavaClass res = test("expr/unary/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);
        assertEquals(1, res.getMethods()[1].getCode().getMaxLocals());

        List<String> instructions = List.of(
                "iconst_1", "ineg", "istore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_unary_not() {
        JavaClass res = test("expr/unary/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);
        assertEquals(1, res.getMethods()[1].getCode().getMaxLocals());

        List<String> instructions = List.of(
                "iconst_1", // 0
                "ifeq -> 8", // 1
                "iconst_0", // 4
                "goto -> 9", // 5
                "iconst_1", // 8
                "istore_0", // 9
                "return" // 10
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_unary_complement() {
        JavaClass res = test("expr/unary/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);
        assertEquals(1, res.getMethods()[1].getCode().getMaxLocals());

        List<String> instructions = List.of(
                "iconst_1", "iconst_m1", "ixor", "istore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_unary_minus_float() {
        JavaClass res = test("expr/unary/4.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);
        assertEquals(1, res.getMethods()[1].getCode().getMaxLocals());

        List<String> instructions = List.of(
                "ldc 1.2", "fneg", "fstore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_binop_arithmetic() {
        JavaClass res = test("expr/binary/arithmetic/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);
        assertEquals(5, res.getMethods()[1].getCode().getMaxLocals());

        List<String> instructions = List.of(
                "iconst_1", "iconst_2", "iadd", "istore_0",
                "iconst_1", "iconst_2", "isub", "istore_1",
                "iconst_1", "iconst_2", "imul", "istore_2",
                "iconst_1", "iconst_2", "idiv", "istore_3",
                "iconst_1", "iconst_2", "irem", "istore 4",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_binop_arithmetic_compound() {
        JavaClass res = test("expr/binary/arithmetic/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", "iconst_2", "iadd",
                "iconst_3", "iconst_4", "imul", "iconst_5", "idiv",
                "isub", "istore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_binop_arithmetic_with_unary() {
        JavaClass res = test("expr/binary/arithmetic/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_3", "iconst_1", "ineg", "iadd", "istore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_binop_bit() {
        JavaClass res = test("expr/binary/arithmetic/4.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);
        assertEquals(6, res.getMethods()[1].getCode().getMaxLocals());

        List<String> instructions = List.of(
                "iconst_1", "iconst_2", "ishl", "istore_0",
                "iconst_1", "iconst_2", "ishr", "istore_1",
                "iconst_1", "iconst_2", "iushr", "istore_2",
                "iconst_1", "iconst_2", "iand", "istore_3",
                "iconst_1", "iconst_2", "ixor", "istore 4",
                "iconst_1", "iconst_2", "ior", "istore 5",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_binop_arithmetic_float() {
        JavaClass res = test("expr/binary/arithmetic/float/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);
        assertEquals(5, res.getMethods()[1].getCode().getMaxLocals());

        List<String> instructions = List.of(
                "ldc 1.1", "ldc 2.2", "fadd", "fstore_0",
                "ldc 1.1", "ldc 2.2", "fsub", "fstore_1",
                "ldc 1.1", "ldc 2.2", "fmul", "fstore_2",
                "ldc 1.1", "ldc 2.2", "fdiv", "fstore_3",
                "ldc 1.1", "ldc 2.2", "frem", "fstore 4",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_binop_arithmetic_compound_float() {
        JavaClass res = test("expr/binary/arithmetic/float/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "ldc 1.1", "ldc 2.2", "fadd",
                "ldc 3.3", "ldc 4.4", "fmul", "ldc 5.5", "fdiv",
                "fsub", "fstore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_binop_arithmetic_with_unary_float() {
        JavaClass res = test("expr/binary/arithmetic/float/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "ldc 3.3", "ldc 1.9", "fneg", "fadd", "fstore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_binop_logical() {
        JavaClass res = test("expr/binary/logical/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", // 0
                "ifeq -> 8", // 1
                "iconst_0", // 4
                "goto -> 9", // 5
                "iconst_0", // 8
                "istore_0", // 9
                "iconst_0", // 10
                "ifne -> 18", // 11
                "iconst_1", // 14
                "goto -> 19", // 15
                "iconst_1", // 18
                "istore_1", // 19
                "return"    // 20
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_binop_logical_compound() {
        JavaClass res = test("expr/binary/logical/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", // 0
                "ifeq -> 8", // 1
                "iconst_0", // 4
                "goto -> 9", // 5
                "iconst_0", // 8
                "ifne -> 16", // 9
                "iconst_1", // 12
                "goto -> 17", // 13
                "iconst_1", // 16
                "istore_0", // 17
                "return" // 18
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_binop_logical_with_unary() {
        JavaClass res = test("expr/binary/logical/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", // 0
                "ifeq -> 16", // 1

                "iconst_0", // 4
                "ifeq -> 12", // 5
                "iconst_0", // 8
                "goto -> 13", // 9
                "iconst_1", // 12

                "goto -> 17", // 13
                "iconst_0", // 16
                "istore_0", // 17
                "return" // 18
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_binop_comparison_eq() {
        JavaClass res = test("expr/binary/comparison/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", // 0
                "iconst_0", // 1
                "if_icmpne -> 9", // 2
                "iconst_1", // 5
                "goto -> 10", // 6
                "iconst_0", // 9
                "istore_0", // 10
                "iconst_1", // 11
                "iconst_2", // 12
                "if_icmpne -> 20", // 13
                "iconst_1", // 16
                "goto -> 21", // 17
                "iconst_0", // 20
                "istore_1", // 21
                "iconst_1", // 22
                "iconst_0", // 23
                "if_icmpeq -> 31", // 24
                "iconst_1", // 27
                "goto -> 32", // 28
                "iconst_0", // 31
                "istore_2", // 32
                "iconst_1", // 33
                "iconst_2", // 34
                "if_icmpeq -> 42", // 35
                "iconst_1", // 38
                "goto -> 43", // 39
                "iconst_0", // 42
                "istore_3", // 43
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_binop_comparison_lt_gt() {
        JavaClass res = test("expr/binary/comparison/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", // 0
                "iconst_2", // 1
                "if_icmpge -> 9", // 2
                "iconst_1", // 5
                "goto -> 10", // 6
                "iconst_0", // 9
                "istore_0", // 10
                "iconst_1", // 11
                "iconst_2", // 12
                "if_icmpgt -> 20", // 13
                "iconst_1", // 16
                "goto -> 21", // 17
                "iconst_0", // 20
                "istore_1", // 21
                "iconst_1", // 22
                "iconst_2", // 23
                "if_icmple -> 31", // 24
                "iconst_1", // 27
                "goto -> 32", // 28
                "iconst_0", // 31
                "istore_2", // 32
                "iconst_1", // 33
                "iconst_2", // 34
                "if_icmplt -> 42", // 35
                "iconst_1", // 38
                "goto -> 43", // 39
                "iconst_0", // 42
                "istore_3", // 43
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_binop_comparison_null() {
        JavaClass res = test("expr/binary/comparison/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", // 0
                "ifnonnull -> 8", // 1
                "iconst_1", // 4
                "goto -> 9", // 5
                "iconst_0", // 8
                "istore_1", // 9
                "aload_0", // 10
                "ifnonnull -> 18", // 11
                "iconst_1", // 14
                "goto -> 19", // 15
                "iconst_0", // 18
                "istore_2", // 19
                "aload_0", // 20
                "ifnull -> 28", // 21
                "iconst_1", // 24
                "goto -> 29", // 25
                "iconst_0", // 28
                "istore_3", // 29
                "aload_0", // 30
                "ifnull -> 38", // 31
                "iconst_1", // 34
                "goto -> 39", // 35
                "iconst_0", // 38
                "istore 4", // 39
                "aconst_null", // 41
                "aconst_null", // 42
                "if_icmpne -> 50", // 43
                "iconst_1", // 46
                "goto -> 51", // 47
                "iconst_0", // 50
                "istore 5", // 51
                "aconst_null", // 53
                "aconst_null", // 54
                "if_icmpeq -> 62", // 55
                "iconst_1", // 58
                "goto -> 63", // 59
                "iconst_0", // 62
                "istore 6", // 63
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_binop_comparison_eq_float() {
        JavaClass res = test("expr/binary/comparison/float/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "fconst_1",
                "fconst_1", // 1
                "fcmpl", // 2
                "ifne -> 10", // 3
                "iconst_1", // 6
                "goto -> 11", // 7
                "iconst_0", // 10
                "istore_0", // 11
                "fconst_1", // 12
                "fconst_1", // 13
                "fcmpl", // 14
                "ifeq -> 22", // 15
                "iconst_1", // 18
                "goto -> 23", // 19
                "iconst_0", // 22
                "istore_1", // 23
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_binop_comparison_lt_gt_float() {
        JavaClass res = test("expr/binary/comparison/float/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "fconst_1", // 0
                "fconst_2", // 1
                "fcmpg", // 2
                "ifge -> 10", // 3
                "iconst_1", // 6
                "goto -> 11", // 7
                "iconst_0", // 10
                "istore_0", // 11
                "fconst_1", // 12
                "fconst_2", // 13
                "fcmpg", // 14
                "ifgt -> 22", // 15
                "iconst_1", // 18
                "goto -> 23", // 19
                "iconst_0", // 22
                "istore_1", // 23
                "fconst_1", // 24
                "fconst_2", // 25
                "fcmpl", // 26
                "ifle -> 34", // 27
                "iconst_1", // 30
                "goto -> 35", // 31
                "iconst_0", // 34
                "istore_2", // 35
                "fconst_1", // 36
                "fconst_2", // 37
                "fcmpl", // 38
                "iflt -> 46", // 39
                "iconst_1", // 42
                "goto -> 47", // 43
                "iconst_0", // 46
                "istore_3", // 47
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_binop_logical_and_comparison() {
        JavaClass res = test("expr/binary/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", // 0
                "iconst_2", // 1
                "if_icmpge -> 9", // 2
                "iconst_1", // 5
                "goto -> 10", // 6
                "iconst_0", // 9

                "ifne -> 26", // 10

                "iconst_1", // 13
                "iconst_2", // 14
                "if_icmpne -> 22", // 15
                "iconst_1", // 18
                "goto -> 23", // 19
                "iconst_0", // 22

                "goto -> 27", // 23
                "iconst_1", // 26
                "istore_0", // 27
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_fix_local() {
        JavaClass res = test("expr/fix/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", "istore 4",
                "iinc 4 1", "iload 4", "istore_1",
                "iinc 4 -1", "iload 4", "istore_1",
                "iload 4", "iinc 4 1", "istore_1",
                "iload 4", "iinc 4 -1", "istore_1",
                "iinc 1 1",
                "iinc 1 -1",
                "iinc 1 1",
                "iinc 1 -1",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_fix_field() {
        JavaClass res = test("expr/fix/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "dup", "getfield Test.x I", "iconst_1", "iadd", "dup_x1", "putfield Test.x I", "istore_1",
                "aload_0", "dup", "getfield Test.x I", "iconst_1", "isub", "dup_x1", "putfield Test.x I", "istore_1",
                "aload_0", "dup", "getfield Test.x I", "dup_x1", "iconst_1", "iadd", "putfield Test.x I", "istore_1",
                "aload_0", "dup", "getfield Test.x I", "dup_x1", "iconst_1", "isub", "putfield Test.x I", "istore_1",
                "aload_0", "dup", "getfield Test.x I", "iconst_1", "iadd", "putfield Test.x I",
                "aload_0", "dup", "getfield Test.x I", "iconst_1", "isub", "putfield Test.x I",
                "aload_0", "dup", "getfield Test.x I", "iconst_1", "iadd", "putfield Test.x I",
                "aload_0", "dup", "getfield Test.x I", "iconst_1", "isub", "putfield Test.x I",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_fix_field_static() {
        JavaClass res = test("expr/fix/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "getstatic Test.x I", "iconst_1", "iadd", "dup", "putstatic Test.x I", "istore_1",
                "getstatic Test.x I", "iconst_1", "isub", "dup", "putstatic Test.x I", "istore_1",
                "getstatic Test.x I", "dup", "iconst_1", "iadd", "putstatic Test.x I", "istore_1",
                "getstatic Test.x I", "dup", "iconst_1", "isub", "putstatic Test.x I", "istore_1",
                "getstatic Test.x I", "iconst_1", "iadd", "putstatic Test.x I",
                "getstatic Test.x I", "iconst_1", "isub", "putstatic Test.x I",
                "getstatic Test.x I", "iconst_1", "iadd", "putstatic Test.x I",
                "getstatic Test.x I", "iconst_1", "isub", "putstatic Test.x I",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_fix_field_qref() {
        JavaClass res = test("expr/fix/4.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x I", "iconst_1", "iadd", "dup_x1", "putfield Other.x I", "istore_1",
                "aload_0", "getfield Test.o LOther;", "getfield Other.o LOther;", "dup", "getfield Other.x I", "iconst_1", "iadd", "dup_x1", "putfield Other.x I", "istore_1",
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x I", "iconst_1", "isub", "dup_x1", "putfield Other.x I", "istore_1",
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x I", "dup_x1", "iconst_1", "iadd", "putfield Other.x I", "istore_1",
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x I", "dup_x1", "iconst_1", "isub", "putfield Other.x I", "istore_1",
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x I", "iconst_1", "iadd", "putfield Other.x I",
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x I", "iconst_1", "isub", "putfield Other.x I",
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x I", "iconst_1", "iadd", "putfield Other.x I",
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x I", "iconst_1", "isub", "putfield Other.x I",
                "aload_0", "getfield Test.o LOther;", "getfield Other.o LOther;", "dup", "getfield Other.x I", "iconst_1", "isub", "putfield Other.x I",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_fix_field_qref_static() {
        JavaClass res = test("expr/fix/5.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "getstatic Other.x I", "iconst_1", "iadd", "dup", "putstatic Other.x I", "istore_1",
                "getstatic Another.x I", "iconst_1", "iadd", "dup", "putstatic Another.x I", "istore_1",
                "getstatic Other.x I", "iconst_1", "isub", "dup", "putstatic Other.x I", "istore_1",
                "getstatic Other.x I", "dup", "iconst_1", "iadd", "putstatic Other.x I", "istore_1",
                "getstatic Other.x I", "dup", "iconst_1", "isub", "putstatic Other.x I", "istore_1",
                "getstatic Other.x I", "iconst_1", "iadd", "putstatic Other.x I",
                "getstatic Other.x I", "iconst_1", "isub", "putstatic Other.x I",
                "getstatic Other.x I", "iconst_1", "iadd", "putstatic Other.x I",
                "getstatic Other.x I", "iconst_1", "isub", "putstatic Other.x I",
                "getstatic Another.x I", "iconst_1", "isub", "putstatic Another.x I",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_fix_ix() {
        JavaClass res = test("expr/fix/6.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_2", "newarray int", "astore_2",
                "iconst_2", "iconst_2", "multianewarray [[I 2", "astore_3",
                "aload_2", "iconst_1", "dup2", "iaload", "iconst_1", "iadd", "dup_x2", "iastore", "istore_1",
                "aload_3", "iconst_1", "aaload", "iconst_2", "dup2", "iaload", "iconst_1", "iadd", "dup_x2", "iastore", "istore_1",
                "aload_0", "getfield Test.fix [I", "iconst_1", "dup2", "iaload", "iconst_1", "iadd", "dup_x2", "iastore", "istore_1",
                "aload_2", "iconst_1", "dup2", "iaload", "iconst_1", "isub", "dup_x2", "iastore", "istore_1",
                "aload_2", "iconst_1", "dup2", "iaload", "dup_x2", "iconst_1", "iadd", "iastore", "istore_1",
                "aload_2", "iconst_1", "dup2", "iaload", "dup_x2", "iconst_1", "isub", "iastore", "istore_1",
                "aload_2", "iconst_1", "dup2", "iaload", "iconst_1", "iadd", "iastore",
                "aload_2", "iconst_1", "dup2", "iaload", "iconst_1", "isub", "iastore",
                "aload_2", "iconst_1", "dup2", "iaload", "iconst_1", "iadd", "iastore",
                "aload_2", "iconst_1", "dup2", "iaload", "iconst_1", "isub", "iastore",
                "getstatic Other.fix [I", "iconst_1", "dup2", "iaload", "iconst_1", "isub", "iastore",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_fix_local_float() {
        JavaClass res = test("expr/fix/float/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "fconst_1", "fstore 4",
                "fload 4", "fconst_1", "fadd", "dup", "fstore 4", "fstore_1",
                "fload 4", "fconst_1", "fsub", "dup", "fstore 4", "fstore_1",
                "fload 4", "dup", "fconst_1", "fadd", "fstore 4", "fstore_1",
                "fload 4", "dup", "fconst_1", "fsub", "fstore 4", "fstore_1",
                "fload_1", "fconst_1", "fadd", "fstore_1",
                "fload_1", "fconst_1", "fsub", "fstore_1",
                "fload_1", "fconst_1", "fadd", "fstore_1",
                "fload_1", "fconst_1", "fsub", "fstore_1",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_fix_field_float() {
        JavaClass res = test("expr/fix/float/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "dup", "getfield Test.x F", "fconst_1", "fadd", "dup_x1", "putfield Test.x F", "fstore_1",
                "aload_0", "dup", "getfield Test.x F", "fconst_1", "fsub", "dup_x1", "putfield Test.x F", "fstore_1",
                "aload_0", "dup", "getfield Test.x F", "dup_x1", "fconst_1", "fadd", "putfield Test.x F", "fstore_1",
                "aload_0", "dup", "getfield Test.x F", "dup_x1", "fconst_1", "fsub", "putfield Test.x F", "fstore_1",
                "aload_0", "dup", "getfield Test.x F", "fconst_1", "fadd", "putfield Test.x F",
                "aload_0", "dup", "getfield Test.x F", "fconst_1", "fsub", "putfield Test.x F",
                "aload_0", "dup", "getfield Test.x F", "fconst_1", "fadd", "putfield Test.x F",
                "aload_0", "dup", "getfield Test.x F", "fconst_1", "fsub", "putfield Test.x F",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_fix_field_static_float() {
        JavaClass res = test("expr/fix/float/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "getstatic Test.x F", "fconst_1", "fadd", "dup", "putstatic Test.x F", "fstore_1",
                "getstatic Test.x F", "fconst_1", "fsub", "dup", "putstatic Test.x F", "fstore_1",
                "getstatic Test.x F", "dup", "fconst_1", "fadd", "putstatic Test.x F", "fstore_1",
                "getstatic Test.x F", "dup", "fconst_1", "fsub", "putstatic Test.x F", "fstore_1",
                "getstatic Test.x F", "fconst_1", "fadd", "putstatic Test.x F",
                "getstatic Test.x F", "fconst_1", "fsub", "putstatic Test.x F",
                "getstatic Test.x F", "fconst_1", "fadd", "putstatic Test.x F",
                "getstatic Test.x F", "fconst_1", "fsub", "putstatic Test.x F",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_fix_field_qref_float() {
        JavaClass res = test("expr/fix/float/4.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x F", "fconst_1", "fadd", "dup_x1", "putfield Other.x F", "fstore_1",
                "aload_0", "getfield Test.o LOther;", "getfield Other.o LOther;", "dup", "getfield Other.x F", "fconst_1", "fadd", "dup_x1", "putfield Other.x F", "fstore_1",
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x F", "fconst_1", "fsub", "dup_x1", "putfield Other.x F", "fstore_1",
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x F", "dup_x1", "fconst_1", "fadd", "putfield Other.x F", "fstore_1",
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x F", "dup_x1", "fconst_1", "fsub", "putfield Other.x F", "fstore_1",
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x F", "fconst_1", "fadd", "putfield Other.x F",
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x F", "fconst_1", "fsub", "putfield Other.x F",
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x F", "fconst_1", "fadd", "putfield Other.x F",
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x F", "fconst_1", "fsub", "putfield Other.x F",
                "aload_0", "getfield Test.o LOther;", "getfield Other.o LOther;", "dup", "getfield Other.x F", "fconst_1", "fsub", "putfield Other.x F",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_fix_field_qref_static_float() {
        JavaClass res = test("expr/fix/float/5.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "getstatic Other.x F", "fconst_1", "fadd", "dup", "putstatic Other.x F", "fstore_1",
                "getstatic Another.x F", "fconst_1", "fadd", "dup", "putstatic Another.x F", "fstore_1",
                "getstatic Other.x F", "fconst_1", "fsub", "dup", "putstatic Other.x F", "fstore_1",
                "getstatic Other.x F", "dup", "fconst_1", "fadd", "putstatic Other.x F", "fstore_1",
                "getstatic Other.x F", "dup", "fconst_1", "fsub", "putstatic Other.x F", "fstore_1",
                "getstatic Other.x F", "fconst_1", "fadd", "putstatic Other.x F",
                "getstatic Other.x F", "fconst_1", "fsub", "putstatic Other.x F",
                "getstatic Other.x F", "fconst_1", "fadd", "putstatic Other.x F",
                "getstatic Other.x F", "fconst_1", "fsub", "putstatic Other.x F",
                "getstatic Another.x F", "fconst_1", "fsub", "putstatic Another.x F",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_fix_ix_float() {
        JavaClass res = test("expr/fix/float/6.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_2", "newarray float", "astore_2",
                "iconst_2", "iconst_2", "multianewarray [[F 2", "astore_3",
                "aload_2", "iconst_1", "dup2", "faload", "fconst_1", "fadd", "dup_x2", "fastore", "fstore_1",
                "aload_3", "iconst_1", "aaload", "iconst_2", "dup2", "faload", "fconst_1", "fadd", "dup_x2", "fastore", "fstore_1",
                "aload_0", "getfield Test.fix [F", "iconst_1", "dup2", "faload", "fconst_1", "fadd", "dup_x2", "fastore", "fstore_1",
                "aload_2", "iconst_1", "dup2", "faload", "fconst_1", "fsub", "dup_x2", "fastore", "fstore_1",
                "aload_2", "iconst_1", "dup2", "faload", "dup_x2", "fconst_1", "fadd", "fastore", "fstore_1",
                "aload_2", "iconst_1", "dup2", "faload", "dup_x2", "fconst_1", "fsub", "fastore", "fstore_1",
                "aload_2", "iconst_1", "dup2", "faload", "fconst_1", "fadd", "fastore",
                "aload_2", "iconst_1", "dup2", "faload", "fconst_1", "fsub", "fastore",
                "aload_2", "iconst_1", "dup2", "faload", "fconst_1", "fadd", "fastore",
                "aload_2", "iconst_1", "dup2", "faload", "fconst_1", "fsub", "fastore",
                "getstatic Other.fix [F", "iconst_1", "dup2", "faload", "fconst_1", "fsub", "fastore",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_ternary() {
        JavaClass res = test("expr/ternary/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iload_1",
                "ifeq -> 8", // 1
                "iconst_1", // 4
                "goto -> 9", // 5
                "iconst_2", // 8
                "istore_2", // 9
                "return" // 10
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(8),
                        stmEntry(64, stmIntType())
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void expr_ternary2() {
        JavaClass res = test("expr/ternary/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iload_1",
                "ifeq -> 8", // 1
                "iconst_1", // 4
                "goto -> 17", // 5
                "iload_2", // 8
                "ifeq -> 16", // 9
                "iconst_2", // 12
                "goto -> 17", // 13
                "iconst_3", // 16
                "istore_3", // 17
                "return" // 18
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(8),
                        stmEntry(7),
                        stmEntry(64, stmIntType())
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void expr_null() {
        JavaClass res = test("expr/null/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aconst_null", "astore_1", "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_newobj() {
        JavaClass res = test("expr/newobj/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "new Test", "dup", "invokespecial Test/<init>()V", "astore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_newobj_otherClass() {
        JavaClass res = test("expr/newobj/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "new Other", "dup", "invokespecial Other/<init>()V", "astore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_newobj_params() {
        JavaClass res = testWithConstructor("expr/newobj/3.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        List<String> instructions = List.of(
                "new Test", "dup", "iconst_1", "invokespecial Test/<init>(I)V", "astore_0",
                "new Test", "dup", "iconst_3", "iconst_1", "invokespecial Test/<init>(IZ)V", "astore_1",
                "return"
        );

        assertInstructions(res.getMethods()[2].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_newobj_params_otherClass() {
        JavaClass res = testWithConstructor("expr/newobj/4.java", "Other");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "new Other", "dup", "iconst_1", "invokespecial Other/<init>(I)V", "astore_0",
                "new Other", "dup", "iconst_3", "iconst_1", "invokespecial Other/<init>(IZ)V", "astore_1",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_newarr() {
        JavaClass res = test("expr/newarr/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", "newarray int", "astore_0",
                "iconst_2", "newarray boolean", "astore_1",
                "iconst_3", "anewarray Test", "astore_2",
                "iconst_4", "newarray float", "astore_3",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_multiNewArr() {
        JavaClass res = test("expr/newarr/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_2", "iconst_3", "multianewarray [[I 2", "astore_0",
                "iconst_4", "iconst_5", "multianewarray [[F 2", "astore_1",
                "iconst_2", "iconst_3", "iconst_4", "multianewarray [[[Z 3", "astore_2",
                "iconst_3", "iconst_4", "iconst_5", "bipush 6", "multianewarray [[[[LTest; 4", "astore_3",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_newArrInit() {
        JavaClass res = test("expr/newarr/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_3", "newarray int", "dup", "iconst_0", "iconst_1", "iastore", "dup", "iconst_1", "iconst_2",
                "iastore", "dup", "iconst_2", "iconst_3", "iastore", "astore_1",
                "iconst_3", "newarray float", "dup", "iconst_0", "ldc 1.1", "fastore", "dup", "iconst_1", "ldc 2.2",
                "fastore", "dup", "iconst_2", "ldc 3.3", "fastore", "astore_2",
                "iconst_2", "newarray boolean", "dup", "iconst_0", "iconst_1", "bastore", "dup", "iconst_1", "iconst_0",
                "bastore", "astore_3",
                "iconst_1", "anewarray Test", "dup", "iconst_0", "new Test", "dup", "invokespecial Test/<init>()V",
                "aastore", "astore 4",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_newArrInit_2D() {
        JavaClass res = test("expr/newarr/4.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_2", "anewarray [I", "dup", "iconst_0", "iconst_2", "newarray int", "dup", "iconst_0", "iconst_1",
                "iastore", "dup", "iconst_1", "iconst_2", "iastore", "aastore",
                "dup", "iconst_1", "iconst_2", "newarray int", "dup", "iconst_0", "iconst_3", "iastore", "dup",
                "iconst_1", "iconst_4", "iastore", "aastore", "astore_1",
                "iconst_2", "anewarray [F", "dup", "iconst_0", "iconst_2", "newarray float", "dup", "iconst_0", "ldc 1.1",
                "fastore", "dup", "iconst_1", "ldc 2.2", "fastore", "aastore",
                "dup", "iconst_1", "iconst_2", "newarray float", "dup", "iconst_0", "ldc 3.3", "fastore", "dup",
                "iconst_1", "ldc 4.4", "fastore", "aastore", "astore_2",
                "iconst_2", "anewarray [Z", "dup", "iconst_0", "iconst_2", "newarray boolean", "dup", "iconst_0", "iconst_1",
                "bastore", "dup", "iconst_1", "iconst_0", "bastore", "aastore",
                "dup", "iconst_1", "iconst_2", "newarray boolean", "dup", "iconst_0", "iconst_1", "bastore", "dup",
                "iconst_1", "iconst_0", "bastore", "aastore", "astore_3",
                "iconst_2", "anewarray [LTest;", "dup", "iconst_0", "iconst_2", "anewarray Test", "dup", "iconst_0", "aload_0",
                "aastore", "dup", "iconst_1", "aload_0", "aastore", "aastore",
                "dup", "iconst_1", "iconst_2", "anewarray Test", "dup", "iconst_0", "aload_0", "aastore", "dup",
                "iconst_1", "aload_0", "aastore", "aastore", "astore 4",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void expr_newArrInit_3D() {
        JavaClass res = test("expr/newarr/5.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", "anewarray [[LTest;", "dup", "iconst_0", "iconst_1", "anewarray [LTest;", "dup",
                "iconst_0", "iconst_1", "anewarray Test", "dup", "iconst_0", "aload_0", "aastore", "aastore",
                "aastore", "astore_1",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_return_void() {
        JavaClass res = test("stmt/return/1.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), List.of("return"));
        assertInstructions(res.getMethods()[2].getCode(), res.getConstantPool(), List.of("return"));
    }

    @Test
    public void stmt_return_int_bool() {
        JavaClass res = test("stmt/return/2.java");
        assert res != null;
        assertEquals(5, res.getMethods().length);

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), List.of("iconst_1", "ireturn"));
        assertInstructions(res.getMethods()[2].getCode(), res.getConstantPool(), List.of("iconst_0", "ireturn"));
        assertInstructions(res.getMethods()[3].getCode(), res.getConstantPool(),
                List.of("iconst_3", "newarray int", "areturn"));
        assertInstructions(res.getMethods()[4].getCode(), res.getConstantPool(),
                List.of("iconst_3", "newarray boolean", "areturn"));
    }

    @Test
    public void stmt_return_object() {
        JavaClass res = test("stmt/return/3.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(),
                List.of("new Test", "dup", "invokespecial Test/<init>()V", "areturn"));
        assertInstructions(res.getMethods()[2].getCode(), res.getConstantPool(),
                List.of("iconst_3", "anewarray Test", "areturn"));
    }

    @Test
    public void stmt_return_float() {
        JavaClass res = test("stmt/return/4.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), List.of("ldc 3.3", "freturn"));
        assertInstructions(res.getMethods()[2].getCode(), res.getConstantPool(),
                List.of("iconst_3", "newarray float", "areturn"));
    }

    @Test
    public void stmt_if() {
        JavaClass res = test("stmt/if/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", // 0
                "ifeq -> 6", // 1
                "iconst_1", // 4
                "istore_0", // 5
                "return" // 6
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_if_else() {
        JavaClass res = test("stmt/if/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", // 0
                "ifeq -> 9", // 1
                "iconst_1", // 4
                "istore_0", // 5
                "goto -> 11", // 6
                "iconst_2", // 9
                "istore_0", // 10
                "return" // 11
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_if_elseIf() {
        JavaClass res = test("stmt/if/3.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        List<String> instructions = List.of(
                "iload_1",
                "ifeq -> 11", // 1
                "iconst_1", // 4
                "invokestatic Test/get(I)V", // 5
                "goto -> 37", // 8
                "iload_2", // 11
                "ifeq -> 22", // 12
                "iconst_2", // 15
                "invokestatic Test/get(I)V", // 16
                "goto -> 37", // 19
                "iload_3", // 22
                "ifeq -> 33", // 23
                "iconst_3", // 26
                "invokestatic Test/get(I)V", // 27
                "goto -> 37", // 30
                "iconst_4", // 33
                "invokestatic Test/get(I)V", // 34
                "return" // 37
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(11),
                        stmEntry(10),
                        stmEntry(10),
                        stmEntry(3)

                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stmt_while() {
        JavaClass res = test("stmt/while/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_0", // 0
                "ifeq -> 9", // 1
                "iconst_1", // 4
                "istore_0", // 5
                "goto -> 0", // 6
                "return" // 9
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_while_break_continue() {
        JavaClass res = test("stmt/while/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iload_1", // 0
                "ifeq -> 13", // 1
                "goto -> 0", // 4
                "goto -> 13", // 7
                "goto -> 0", // 10
                "return" // 13
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_while_break_continue_nested() {
        JavaClass res = test("stmt/while/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iload_1", // 0
                "ifeq -> 23", // 1
                "goto -> 0", // 4
                "iload_2", // 7
                "ifeq -> 17", // 8
                "goto -> 17", // 11
                "goto -> 7", // 14
                "goto -> 23", // 17
                "goto -> 0", // 20
                "return" // 23
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_do_while() {
        JavaClass res = test("stmt/doWhile/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_0", // 0
                "istore_0", // 1
                "iinc 0 1", // 2
                "iload_0",  // 5
                "bipush 10", // 6
                "if_icmpge -> 15", // 8
                "iconst_1", // 11
                "goto -> 16", // 12
                "iconst_0", // 15
                "ifne -> 2", // 16
                "return" // 19
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_do_while_break() {
        JavaClass res = test("stmt/doWhile/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_0", // 0
                "istore_0", // 1
                "iinc 0 1", // 2
                "iload_0", // 5
                "iconst_5", // 6
                "if_icmpne -> 14", // 7
                "iconst_1", // 10
                "goto -> 15", // 11
                "iconst_0", // 14
                "ifeq -> 21", // 15
                "goto -> 35", // 18
                "iload_0",  // 21
                "bipush 10", // 22
                "if_icmpge -> 31", // 24
                "iconst_1", // 27
                "goto -> 32", // 28
                "iconst_0", // 31
                "ifne -> 2", // 32
                "return" // 35
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_do_while_continue() {
        JavaClass res = test("stmt/doWhile/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_0", // 0
                "istore_0", // 1
                "iconst_0", // 2
                "istore_1", // 3
                "iinc 0 1", // 4
                "iload_0", // 7
                "iconst_3", // 8
                "if_icmpge -> 16", // 9
                "iconst_1", // 12
                "goto -> 17", // 13
                "iconst_0", // 16
                "ifeq -> 23", // 17
                "goto -> 26", // 20
                "iinc 1 1", // 23
                "iload_0",  // 26
                "bipush 10", // 27
                "if_icmpge -> 36", // 29
                "iconst_1", // 32
                "goto -> 37", // 33
                "iconst_0", // 36
                "ifne -> 4", // 37
                "return" // 40
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_for() {
        JavaClass res = test("stmt/for/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_0",
                "istore_1", // 1
                "iload_1", // 2
                "iconst_3", // 3
                "if_icmpge -> 11", // 4
                "iconst_1", // 7
                "goto -> 12", // 8
                "iconst_0", // 11
                "ifeq -> 23", // 12
                "iload_1", // 15
                "istore_2", // 16
                "iinc 1 1", // 17
                "goto -> 2", // 20
                "return" // 23
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(252, 2, List.of(stmIntType())),
                        stmEntry(8),
                        stmEntry(64, stmIntType()),
                        stmEntry(250, 10)
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stmt_for_break_continue() {
        JavaClass res = test("stmt/for/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_0",
                "istore_1", // 1
                "iload_1", // 2
                "iconst_3", // 3
                "if_icmpge -> 11", // 4
                "iconst_1", // 7
                "goto -> 12", // 8
                "iconst_0", // 11
                "ifeq -> 27", // 12
                "goto -> 21", // 15
                "goto -> 27", // 18
                "iinc 1 1", // 21
                "goto -> 2", // 24
                "return" // 27
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(252, 2, List.of(stmIntType())),
                        stmEntry(8),
                        stmEntry(64, stmIntType()),
                        stmEntry(8),
                        stmEntry(250, 5)
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stmt_for_nested_break_continue() {
        JavaClass res = test("stmt/for/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_0",
                "istore_1", // 1
                "iload_1", // 2
                "iconst_3", // 3
                "if_icmpge -> 11", // 4
                "iconst_1", // 7
                "goto -> 12", // 8
                "iconst_0", // 11
                "ifeq -> 48", // 12
                "iconst_0", // 15
                "istore_2", // 16
                "iload_2", // 17
                "iconst_3", // 18
                "if_icmpge -> 26", // 19
                "iconst_1", // 22
                "goto -> 27", // 23
                "iconst_0", // 26
                "ifeq -> 42", // 27
                "goto -> 36", // 30
                "goto -> 42", // 33
                "iinc 2 1", // 36
                "goto -> 17", // 39
                "iinc 1 1", // 42
                "goto -> 2", // 45
                "return" // 48
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(252, 2, List.of(stmIntType())),
                        stmEntry(8),
                        stmEntry(64, stmIntType()),
                        stmEntry(252, 4, List.of(stmIntType())),
                        stmEntry(8),
                        stmEntry(64, stmIntType()),
                        stmEntry(8),
                        stmEntry(250, 5),
                        stmEntry(250, 5)
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stmt_vardecl() {
        JavaClass res = test("stmt/vardecl/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_0", "istore_0",
                "iconst_1", "istore_1",
                "iconst_2", "istore_2",
                "iconst_3", "istore_3",
                "iconst_4", "istore 4",
                "iconst_5", "istore 5",
                "bipush 6", "istore 6",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_vardecl_newobj() {
        JavaClass res = test("stmt/vardecl/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "new Test", "dup", "invokespecial Test/<init>()V", "astore_0",
                "new Test", "dup", "invokespecial Test/<init>()V", "astore_1",
                "new Test", "dup", "invokespecial Test/<init>()V", "astore_2",
                "new Test", "dup", "invokespecial Test/<init>()V", "astore_3",
                "iconst_1", "istore 4",
                "new Test", "dup", "invokespecial Test/<init>()V", "astore 5",
                "iconst_2", "istore 6",
                "new Test", "dup", "invokespecial Test/<init>()V", "astore 7",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_vardecl_instance_method() {
        JavaClass res = test("stmt/vardecl/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "new Test", "dup", "invokespecial Test/<init>()V", "astore_1",
                "new Test", "dup", "invokespecial Test/<init>()V", "astore_2",
                "new Test", "dup", "invokespecial Test/<init>()V", "astore_3",
                "new Test", "dup", "invokespecial Test/<init>()V", "astore 4",
                "iconst_1", "istore 5",
                "new Test", "dup", "invokespecial Test/<init>()V", "astore 6",
                "iconst_2", "istore 7",
                "new Test", "dup", "invokespecial Test/<init>()V", "astore 8",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_vardecl_localsCount() {
        JavaClass res = test("stmt/vardecl/4.java");
        assert res != null;
        assertEquals(7, res.getMethods().length);

        assertEquals(1, res.getMethods()[1].getCode().getMaxLocals());
        assertEquals(0, res.getMethods()[2].getCode().getMaxLocals());

        assertEquals(2, res.getMethods()[3].getCode().getMaxLocals());
        assertInstructions(res.getMethods()[3].getCode(), res.getConstantPool(),
                List.of(
                    "iconst_1", "istore_1",
                    "return"
                )
        );

        assertEquals(1, res.getMethods()[4].getCode().getMaxLocals());
        assertInstructions(res.getMethods()[4].getCode(), res.getConstantPool(),
                List.of(
                        "iconst_1", "istore_0",
                        "return"
                )
        );

        assertEquals(4, res.getMethods()[5].getCode().getMaxLocals());
        assertInstructions(res.getMethods()[5].getCode(), res.getConstantPool(),
                List.of(
                        "iconst_1", "istore_3",
                        "iload_1", "istore_2",
                        "return"
                )
        );

        assertEquals(3, res.getMethods()[6].getCode().getMaxLocals());
        assertInstructions(res.getMethods()[6].getCode(), res.getConstantPool(),
                List.of(
                        "iconst_1", "istore_2",
                        "iload_0", "istore_1",
                        "return"
                )
        );
    }

    @Test
    public void stmt_vardecl_localsCount_branching() {
        JavaClass res = test("stmt/vardecl/5.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        assertEquals(2, res.getMethods()[1].getCode().getMaxLocals());
        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(),
                List.of(
                        "iload_0",  // 0
                        "ifeq -> 9", // 1
                        "iconst_1", // 4
                        "istore_1", // 5
                        "goto -> 11", // 6
                        "iconst_2", // 9
                        "istore_1", // 10
                        "iconst_3", // 11
                        "istore_1", // 12
                        "return"    // 13
                )
        );

        assertEquals(2, res.getMethods()[2].getCode().getMaxLocals());
        assertInstructions(res.getMethods()[2].getCode(), res.getConstantPool(),
                List.of(
                        "iload_0",  // 0
                        "ifeq -> 9", // 1
                        "iconst_1", // 4
                        "istore_1", // 5
                        "goto -> 0", // 6
                        "iconst_2", // 9
                        "istore_1", // 10
                        "return"    // 11
                )
        );
    }

    @Test
    public void stmt_vardecl_float() {
        JavaClass res = test("stmt/vardecl/6.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "fconst_0", "fstore_0",
                "fconst_1", "fstore_1",
                "fconst_2", "fstore_2",
                "ldc 3.14159", "fstore_3",
                "ldc 13.01", "fstore 4",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_vardecl_late_init() {
        JavaClass res = test("stmt/vardecl/7.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", "istore_0",
                "new Test", "dup", "invokespecial Test/<init>()V", "astore_1",
                "ldc 1.2", "fstore_2",
                "iconst_2", "newarray int", "astore 4",
                "iconst_0", "istore_3",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_simple() {
        JavaClass res = test("stmt/assign/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", "istore_0",
                "iconst_2", "istore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign() {
        JavaClass res = test("stmt/assign/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", "istore_1",
                "iconst_2", "istore_2",
                "iconst_3", "istore_2",
                "iconst_4", "istore_0",
                "iconst_5", "istore_3",
                "iconst_1", "istore 4",
                "bipush 6", "istore_1",
                "iconst_0", "istore 4",
                "bipush 7", "istore_3",
                "bipush 8", "istore_2",
                "bipush 9", "istore_3",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_newobj() {
        JavaClass res = test("stmt/assign/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "new Test", "dup", "invokespecial Test/<init>()V", "astore_0",
                "new Test", "dup", "invokespecial Test/<init>()V", "astore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_field() {
        JavaClass res = test("stmt/assign/4.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "iconst_1", "putfield Test.i I",
                "iconst_2", "putstatic Test.istat I",
                "aload_0", "getstatic Test.tstat LTest;", "putfield Test.t LTest;",
                "aload_0", "getfield Test.t LTest;", "putstatic Test.tstat LTest;",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_ix() {
        JavaClass res = test("stmt/assign/5.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_3", "newarray int", "astore_1",
                "iconst_3", "newarray boolean", "astore_2",
                "iconst_3", "anewarray Test", "astore_3",
                "aload_0", "getfield Test.ix [I", "iconst_0", "iconst_2", "iastore",
                "aload_1", "iconst_0", "iconst_2", "iastore",
                "aload_0", "getfield Test.bx [Z", "iconst_1", "iconst_1", "bastore",
                "aload_2", "iconst_1", "iconst_0", "bastore",
                "aload_0", "getfield Test.tx [LTest;", "iconst_2", "aload_3", "iconst_2", "aaload", "aastore",
                "aload_3", "iconst_2", "aload_0", "getfield Test.tx [LTest;", "iconst_2", "aaload", "aastore",
                "getstatic Test.ixstat [I", "iconst_3", "aload_0", "getfield Test.ix [I", "iconst_3", "iaload", "iastore",
                "aload_0", "getfield Test.ix [I", "iconst_3", "getstatic Test.ixstat [I", "iconst_3", "iaload", "iastore",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_qref() {
        JavaClass res = test("stmt/assign/6.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "iconst_1", "putfield Test.i I",
                "iconst_2", "putstatic Other.istat I",
                "aload_0", "getfield Test.o LOther;", "iconst_3", "putfield Other.i I",
                "aload_0", "getfield Test.o LOther;", "getfield Other.o LOther;", "iconst_4", "putfield Other.i I",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_ixqref() {
        JavaClass res = test("stmt/assign/7.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "getfield Test.ix [I", "iconst_0", "iconst_1", "iastore",
                "getstatic Other.ixstat [I", "iconst_1", "iconst_2", "iastore",
                "aload_0", "getfield Test.o LOther;", "getfield Other.ix [I", "iconst_2", "iconst_3", "iastore",
                "aload_0", "getfield Test.o LOther;", "getfield Other.ox [LOther;", "iconst_3", "aaload",
                    "getfield Other.ix [I", "iconst_4", "iconst_4", "iastore",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_put_and_invoke_in_buffer() {
        JavaClass res = test("stmt/assign/8.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "aload_0", "iconst_2", "iconst_2", "invokevirtual Test/add(II)I", "putfield Test.res I",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_float() {
        JavaClass res = test("stmt/assign/9.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "ldc 1.1", "fstore_2",
                "ldc 2.2", "fstore_2",
                "fload_1", "fstore_2",
                "fload_2", "fstore_1",
                "aload_0", "fconst_0", "putfield Test.f F",
                "fconst_1", "putstatic Test.fstat F",
                "aload_0", "getstatic Test.fstat F", "putfield Test.f F",
                "aload_0", "getfield Test.f F", "putstatic Test.fstat F",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_ix_float() {
        JavaClass res = test("stmt/assign/10.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "bipush 10", "newarray float", "astore_1",
                "aload_1", "iconst_0", "ldc 3.3", "fastore",
                "aload_0", "getfield Test.fx [F", "iconst_0", "ldc 4.4", "fastore",
                "aload_1", "iconst_1", "faload", "fstore_2",
                "getstatic Test.fxstat [F", "iconst_3", "aload_0", "getfield Test.fx [F", "iconst_4", "faload", "fastore",
                "aload_0", "getfield Test.fx [F", "iconst_5", "getstatic Test.fxstat [F", "bipush 6", "faload", "fastore",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_compound_local() {
        JavaClass res = test("stmt/assign/compound/int/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", "istore_0",
                "iload_0", "iconst_2", "imul", "istore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_compound_static() {
        JavaClass res = test("stmt/assign/compound/int/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "getstatic Test.x I", "iconst_2", "irem", "putstatic Test.x I",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_compound_field() {
        JavaClass res = test("stmt/assign/compound/int/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "dup", "getfield Test.x I", "iconst_2", "isub", "putfield Test.x I",
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x I", "iconst_3", "ishr", "putfield Other.x I",
                "aload_0", "getfield Test.o LOther;", "getfield Other.t LTest;", "dup", "getfield Test.x I", "iconst_4", "iand", "putfield Test.x I",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_compound_ix() {
        JavaClass res = test("stmt/assign/compound/int/4.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_2", "newarray int", "astore_1",
                "iconst_2", "iconst_2", "multianewarray [[I 2", "astore_2",
                "aload_1", "iconst_1", "dup2", "iaload", "iconst_3", "imul", "iastore",
                "aload_2", "iconst_1", "aaload", "iconst_0", "dup2", "iaload", "iconst_4", "irem", "iastore",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_compound_local_float() {
        JavaClass res = test("stmt/assign/compound/float/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "ldc 1.4", "fstore_0",
                "fload_0", "ldc 2.7", "fmul", "fstore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_compound_static_float() {
        JavaClass res = test("stmt/assign/compound/float/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "getstatic Test.x F", "ldc 4.0", "frem", "putstatic Test.x F",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_compound_field_float() {
        JavaClass res = test("stmt/assign/compound/float/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "dup", "getfield Test.x F", "ldc 2.2", "fsub", "putfield Test.x F",
                "aload_0", "getfield Test.o LOther;", "dup", "getfield Other.x F", "ldc 3.3", "fadd", "putfield Other.x F",
                "aload_0", "getfield Test.o LOther;", "getfield Other.t LTest;", "dup", "getfield Test.x F", "ldc 4.4", "fdiv", "putfield Test.x F",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_assign_compound_ix_float() {
        JavaClass res = test("stmt/assign/compound/float/4.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_2", "newarray float", "astore_1",
                "iconst_2", "iconst_2", "multianewarray [[F 2", "astore_2",
                "aload_1", "iconst_1", "dup2", "faload", "ldc 3.3", "fmul", "fastore",
                "aload_2", "iconst_1", "aaload", "iconst_0", "dup2", "faload", "ldc 4.4", "frem", "fastore",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void stmt_maxStack() {
        JavaClass res = test("stmt/maxStack/1.java");
        assert res != null;
        assertEquals(6, res.getMethods().length);

        assertEquals(2, res.getMethods()[1].getCode().getMaxStack());
        assertEquals(2, res.getMethods()[2].getCode().getMaxStack());
        assertEquals(2, res.getMethods()[3].getCode().getMaxStack());
        assertEquals(4, res.getMethods()[4].getCode().getMaxStack());
        assertEquals(5, res.getMethods()[5].getCode().getMaxStack());
    }

    @Test
    public void stmt_maxStack_calls() {
        JavaClass res = test("stmt/maxStack/2.java");
        assert res != null;
        assertEquals(7, res.getMethods().length);

        assertEquals(4, res.getMethods()[1].getCode().getMaxStack());
        assertEquals(7, res.getMethods()[2].getCode().getMaxStack());
        assertEquals(8, res.getMethods()[3].getCode().getMaxStack());
        assertEquals(6, res.getMethods()[4].getCode().getMaxStack());
    }

    @Test
    public void ref_id_simple() {
        JavaClass res = test("ref/id/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", "istore_0",
                "iload_0", "istore_1",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void ref_id() {
        JavaClass res = test("ref/id/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_0", "istore_0",
                "iconst_1", "istore_1",
                "iconst_0", "istore_2",
                "new Test", "dup", "invokespecial Test/<init>()V", "astore_3",
                "new Test", "dup", "invokespecial Test/<init>()V", "astore 4",
                "iconst_5", "istore 5",
                "iload_0", "istore 5",
                "iload_2", "istore_1",
                "iload_1", "istore_2",
                "aload_3", "astore 4",
                "aload 4", "astore 6",
                "iload 5", "istore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void ref_ix() {
        JavaClass res = test("ref/ix/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_3", "newarray int", "astore_0",
                "iconst_3", "newarray boolean", "astore_1",
                "iconst_3", "anewarray Test", "astore_2",
                "aload_0", "iconst_1", "iaload", "istore_3",
                "aload_1", "iconst_1", "baload", "istore 4",
                "aload_2", "iconst_1", "aaload", "astore 5",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void ref_ix_multi() {
        JavaClass res = test("ref/ix/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_0", "iconst_1", "multianewarray [[I 2", "astore_1",
                "aload_1", "iconst_1", "aaload", "iconst_2", "iaload", "istore_2",
                "aload_1", "iconst_2", "aaload", "astore_3",
                "aload_1", "iconst_3", "aload_3", "aastore",
                "aload_1", "iconst_3", "aaload", "astore_3",
                "aload_1", "iconst_4", "aaload", "iconst_5", "iconst_3", "iastore",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void ref_ix_afterCall() {
        JavaClass res = test("ref/ix/3.java");
        assert res != null;
        assertEquals(4, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "invokevirtual Test/get()[I", "iconst_0", "iaload", "istore_1",
                "aload_0", "invokevirtual Test/getT()[LTest;", "iconst_1", "aaload", "getfield Test.p I", "istore_2",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void ref_ix_length() {
        JavaClass res = test("ref/ix/4.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_2", "newarray int", "astore_1", "aload_1", "arraylength", "istore_2", "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void ref_ix_3d() {
        JavaClass res = test("ref/ix/5.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1", "iconst_2", "iconst_3", "multianewarray [[[I 3", "astore_1",
                "aload_1", "iconst_0", "aaload", "iconst_1", "aaload", "iconst_2", "iaload", "istore_2",
                "iconst_1", "iconst_2", "iconst_3", "multianewarray [[[LTest; 3", "astore_3",
                "aload_3", "iconst_0", "aaload", "iconst_1", "aaload", "iconst_2", "aaload", "astore 4",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void ref_id_field() {
        JavaClass res = test("ref/id/field/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "getfield Test.i I", "istore_1",
                "aload_0", "getfield Test.b Z", "istore_2",
                "aload_0", "getfield Test.t LTest;", "astore_3",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void ref_id_field_static() {
        JavaClass res = test("ref/id/field/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "getstatic Test.istat I", "istore_0",
                "getstatic Test.bstat Z", "istore_1",
                "getstatic Test.tstat LTest;", "astore_2",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void ref_ix_field() {
        JavaClass res = test("ref/ix/field/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "getfield Test.i [I", "iconst_1", "iaload", "istore_1",
                "aload_0", "getfield Test.b [Z", "iconst_2", "baload", "istore_2",
                "aload_0", "getfield Test.t [LTest;", "iconst_3", "aaload", "astore_3",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void ref_ix_field_static() {
        JavaClass res = test("ref/ix/field/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "getstatic Test.istat [I", "iconst_1", "iaload", "istore_0",
                "getstatic Test.bstat [Z", "iconst_2", "baload", "istore_1",
                "getstatic Test.tstat [LTest;", "iconst_3", "aaload", "astore_2",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void qref_static() {
        JavaClass res = test("ref/q/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "getstatic Other.istat I", "istore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void qref_instance() {
        JavaClass res = test("ref/q/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "new Other", "dup", "invokespecial Other/<init>()V", "astore_1",
                "aload_1", "getfield Other.i I", "istore_2",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void qref_instance_nested() {
        JavaClass res = test("ref/q/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "new Other", "dup", "invokespecial Other/<init>()V", "astore_1",
                "aload_1", "getfield Other.o LOther;", "getfield Other.i I", "istore_2",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void qref_fields() {
        JavaClass res = test("ref/q/4.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "getfield Test.o LOther;", "getfield Other.i I", "istore_1",
                "aload_0", "getfield Test.o LOther;", "getfield Other.o LOther;", "getfield Other.i I", "istore_2",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void qref_this() {
        JavaClass res = test("ref/q/5.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "getfield Test.o LOther;", "getfield Other.i I", "istore_1",
                "aload_0", "getfield Test.o LOther;", "getfield Other.o LOther;", "getfield Other.i I", "istore_2",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void qref_static_field_classname_prefix() {
        JavaClass res = test("ref/q/7.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "getstatic Test.o LOther;", "getfield Other.i I", "istore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void qref_static_after_instance() {
        JavaClass res = test("ref/q/8.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "getstatic Other.istat I", "istore_1",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void qref_static_before_after_instance() {
        JavaClass res = test("ref/q/9.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "getstatic Other.ostat LOther;", "getfield Other.i I", "istore_1",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void ixqref_static() {
        JavaClass res = test("ref/ixq/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "getstatic Other.ixstat [I", "iconst_2", "iaload", "istore_0",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void ixqref_instance() {
        JavaClass res = test("ref/ixq/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "new Other", "dup", "invokespecial Other/<init>()V", "astore_1",
                "aload_1", "getfield Other.ix [I", "iconst_2", "iaload", "istore_2",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void ixqref_instance_nested() {
        JavaClass res = test("ref/ixq/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "new Other", "dup", "invokespecial Other/<init>()V", "astore_1",
                "aload_1", "getfield Other.ox [LOther;", "iconst_2", "aaload",
                    "getfield Other.ix [I", "iconst_3", "iaload", "istore_2",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void ixqref_instance_nested_full() {
        JavaClass res = test("ref/ixq/4.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "getfield Test.ox [LOther;", "iconst_1", "aaload",
                    "getfield Other.ox [LOther;", "iconst_2", "aaload",
                        "getfield Other.bx [Z", "iconst_3", "baload", "istore_1",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void ixqref_instance_nested_mixed() {
        JavaClass res = test("ref/ixq/5.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "getfield Test.ox [LOther;", "iconst_1", "aaload",
                    "getfield Other.o LOther;",
                        "getfield Other.ox [LOther;", "iconst_2", "aaload",
                            "getfield Other.o LOther;",
                                "getfield Other.bx [Z", "iconst_3", "baload", "istore_1",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void call_idref() {
        JavaClass res = test("ref/call/1.java");
        assert res != null;
        assertEquals(4, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "invokevirtual Test/f()I", "istore_1",
                "invokestatic Test/fstat()I", "istore_2",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void call_idref_params() {
        JavaClass res = test("ref/call/2.java");
        assert res != null;
        assertEquals(4, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "iconst_1", "invokevirtual Test/f(I)I", "istore_1",
                "iconst_2", "iconst_3", "invokestatic Test/fstat(II)I", "istore_2",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void call_qref() {
        JavaClass res = test("ref/call/3.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "iconst_1", "invokevirtual Test/f(I)I", "istore_1",
                "aload_0", "getfield Test.o LOther;", "iconst_2", "invokevirtual Other/i(I)I", "istore_2",
                "iconst_3", "iconst_4", "invokestatic Other/istat(II)I", "istore_3",
                "aload_0", "getfield Test.o LOther;", "getfield Other.o LOther;", "iconst_5", "invokevirtual Other/i(I)I", "istore 4",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void call_stmt() {
        JavaClass res = test("ref/call/4.java");
        assert res != null;
        assertEquals(5, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "iconst_1", "iconst_2", "invokevirtual Test/f(II)V",
                "invokestatic Test/fstat()V",
                "aload_0", "iconst_1", "iconst_2", "invokevirtual Test/f(II)V",
                "invokestatic Test/fstat()V",
                "aload_0", "getfield Test.o LOther;", "iconst_1", "invokevirtual Other/i(I)V",
                "aload_0", "getfield Test.o LOther;", "iconst_3", "invokestatic Other/istat(LOther;I)V",
                "aload_0", "getfield Test.o LOther;", "getfield Other.o LOther;", "iconst_4", "invokevirtual Other/i(I)V",
                "aload_0", "invokevirtual Test/getO()LOther;", "iconst_1", "invokevirtual Other/i(I)V",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void call_stmt_non_void_pop() {
        JavaClass res = test("ref/call/5.java");
        assert res != null;
        assertEquals(4, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "iconst_1", "invokevirtual Test/i(Z)I", "pop",
                "aload_0", "iconst_0", "invokevirtual Test/v(Z)V",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void call_qref_2() {
        JavaClass res = test("ref/call/6.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0", "invokevirtual Test/getO()LOther;", "iconst_2", "invokevirtual Other/i(I)I", "istore_1",
                "aload_0", "invokevirtual Test/getO()LOther;", "iload_1", "putfield Other.x I",
                "iconst_1", "iconst_2", "invokestatic Other/tstat(II)LTest;", "invokevirtual Test/getO()LOther;", "getfield Other.x I", "istore_1",
                "aload_0", "invokevirtual Test/getO()LOther;", "getfield Other.tx [LTest;", "iconst_1", "aaload", "invokevirtual Test/getO()LOther;", "astore_2",
                "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void call_this() {
        JavaClass res = testWithConstructor("ref/call/7.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        List<String> instructions1 = List.of(
                "aload_0", "iload_1", "iconst_0", "invokespecial Test/<init>(IZ)V", "return"
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions1);

        List<String> instructions2 = List.of(
                "aload_0", "bipush 13", "invokespecial Test/<init>(I)V", "return"
        );

        assertInstructions(res.getMethods()[2].getCode(), res.getConstantPool(), instructions2);
    }

    @Test
    public void stackmapframe_ifStmt_same() {
        JavaClass res = test("stackmapframe/if/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iload_0",
                "ifeq -> 6", // 1
                "iconst_0", // 4
                "istore_0", // 5
                "return" // 6
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(6) // same, offset at 6
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_ifStmt_append() {
        JavaClass res = test("stackmapframe/if/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "iload_0", // 2
                "ifeq -> 8", // 3
                "iconst_0", // 6
                "istore_0", // 7
                "return" // 8
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(252, 8,  // append
                                List.of(
                                     stmIntType()
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_ifStmt_append_two() {
        JavaClass res = test("stackmapframe/if/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "new Test", // 2
                "dup", // 5
                "invokespecial Test/<init>()V", // 6
                "astore_2", // 9
                "iload_0", // 10
                "ifeq -> 16", // 11
                "iconst_0", // 14
                "istore_0", // 15
                "return" // 16
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(253, 16,  // append
                                List.of(
                                        stmIntType(),
                                        stmObjectType("Test")
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_ifStmt_append_three() {
        JavaClass res = test("stackmapframe/if/4.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "new Test", // 2
                "dup", // 5
                "invokespecial Test/<init>()V", // 6
                "astore_2", // 9
                "iconst_2", // 10
                "anewarray Test", // 11
                "astore_3", // 14
                "iload_0", // 15
                "ifeq -> 21", // 16
                "iconst_0", // 19
                "istore_0", // 20
                "return" // 21
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(254, 21,  // append
                                List.of(
                                        stmIntType(),
                                        stmObjectType("Test"),
                                        stmObjectType("[LTest;")
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_ifStmt_full() {
        JavaClass res = test("stackmapframe/if/5.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "new Test", // 2
                "dup", // 5
                "invokespecial Test/<init>()V", // 6
                "astore_2", // 9
                "iconst_2", // 10
                "anewarray Test", // 11
                "astore_3", // 14
                "iconst_2", // 15
                "newarray int", // 16
                "astore 4", // 18
                "iload_0", // 20
                "ifeq -> 26", // 21
                "iconst_0", // 24
                "istore_0", // 25
                "return" // 26
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(255, 26,  // full
                                List.of(
                                        stmIntType(),
                                        stmIntType(),
                                        stmObjectType("Test"),
                                        stmObjectType("[LTest;"),
                                        stmObjectType("[I")
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_nestedIfStmt_full_multipleJumpsToSameLoc() {
        JavaClass res = test("stackmapframe/if/6.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "new Test", // 2
                "dup", // 5
                "invokespecial Test/<init>()V", // 6
                "astore_2", // 9
                "iconst_2", // 10
                "anewarray Test", // 11
                "astore_3", // 14
                "iconst_2", // 15
                "newarray int", // 16
                "astore 4", // 18
                "iload_0", // 20
                "ifeq -> 33", // 21
                "iconst_2", // 24
                "istore 5", // 25
                "iload_0", // 27
                "ifeq -> 33", // 28
                "iconst_1", // 31
                "istore_0", // 32
                "return" // 33
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(255, 33,  // full
                                List.of(
                                        stmIntType(),
                                        stmIntType(),
                                        stmObjectType("Test"),
                                        stmObjectType("[LTest;"),
                                        stmObjectType("[I")
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_nestedIfStmt_full_same() {
        JavaClass res = test("stackmapframe/if/7.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "new Test", // 2
                "dup", // 5
                "invokespecial Test/<init>()V", // 6
                "astore_2", // 9
                "iconst_2", // 10
                "anewarray Test", // 11
                "astore_3", // 14
                "iconst_2", // 15
                "newarray int", // 16
                "astore 4", // 18
                "iload_0", // 20
                "ifeq -> 32", // 21
                "iload_0", // 24
                "ifeq -> 30", // 25
                "iconst_1", // 28
                "istore_0", // 29
                "iconst_2", // 30
                "istore_1", // 31
                "return" // 32
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(255, 30,  // full
                                List.of(
                                        stmIntType(),
                                        stmIntType(),
                                        stmObjectType("Test"),
                                        stmObjectType("[LTest;"),
                                        stmObjectType("[I")
                                )
                        ),
                        stmEntry(1) // same
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_nestedIfStmt_append_chop() {
        JavaClass res = test("stackmapframe/if/8.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "iload_0", // 2
                "ifeq -> 16", // 3
                "iconst_2", // 6
                "istore_2", // 7
                "iload_0", // 8
                "ifeq -> 14", // 9
                "iconst_1", // 12
                "istore_0", // 13
                "iconst_3", // 14
                "istore_2", // 15
                "return" // 16
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(253, 14,  // append
                                List.of(
                                        stmIntType(),
                                        stmIntType()
                                )
                        ),
                        stmEntry(250, 1) // chop
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_nestedIfStmt_append_chop_two() {
        JavaClass res = test("stackmapframe/if/9.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "iload_0", // 2
                "ifeq -> 21", // 3
                "iconst_2", // 6
                "istore_2", // 7
                "iconst_3", // 8
                "istore_3", // 9
                "iload_0", // 10
                "ifeq -> 16", // 11
                "iconst_1", // 14
                "istore_0", // 15
                "iconst_3", // 16
                "istore_2", // 17
                "iconst_4", // 18
                "istore 4", // 19
                "return" // 21
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(254, 16,  // full
                                List.of(
                                        stmIntType(),
                                        stmIntType(),
                                        stmIntType()
                                )
                        ),
                        stmEntry(249, 4) // chop
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_nestedIfStmt_full_full_four() {
        JavaClass res = test("stackmapframe/if/11.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "iload_0", // 2
                "ifeq -> 27", // 3
                "iconst_2", // 6
                "istore_2", // 7
                "iconst_3", // 8
                "istore_3", // 9
                "iconst_4", // 10
                "istore 4", // 11
                "iconst_5", // 13
                "istore 5", // 14
                "iload_0", // 16
                "ifeq -> 22", // 17
                "iconst_1", // 20
                "istore_0", // 21
                "iconst_3", // 22
                "istore_2", // 23
                "iconst_4", // 24
                "istore 6", // 25
                "return" // 27
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(255, 22,  // full
                                List.of(
                                        stmIntType(),
                                        stmIntType(),
                                        stmIntType(),
                                        stmIntType(),
                                        stmIntType(),
                                        stmIntType()
                                )
                        ),
                        stmEntry(255, 4,  // full
                                List.of(
                                        stmIntType(),
                                        stmIntType()
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_nestedIfStmt_append_full_append_append_full() {
        JavaClass res = test("stackmapframe/if/12.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "iload_0", // 2
                "ifeq -> 54", // 3
                "iconst_2", // 6
                "istore_2", // 7
                "iload_0", // 8
                "ifeq -> 14", // 9
                "iconst_1", // 12
                "istore_0", // 13
                "iconst_3", // 14
                "istore_3", // 15
                "iconst_4", // 16
                "istore 4", // 17
                "iconst_5", // 19
                "istore 5", // 20
                "iconst_5", // 22
                "istore 6", // 23
                "iload_0", // 25
                "ifeq -> 31", // 26
                "iconst_1", // 29
                "istore_0", // 30
                "iconst_5", // 31
                "istore 7", // 32
                "iload_0", // 34
                "ifeq -> 40", // 35
                "iconst_1", // 38
                "istore_0", // 39
                "iconst_1", // 40
                "istore 8", // 41
                "iconst_1", // 43
                "istore 9", // 44
                "iload_0", // 46
                "ifeq -> 52", // 47
                "iconst_1", // 50
                "istore_0", // 51
                "iconst_0", // 52
                "istore_0", // 53
                "return" // 54
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(253, 14,  // append
                                List.of(
                                        stmIntType(),
                                        stmIntType()
                                )
                        ),
                        stmEntry(255, 16,  // full
                                List.of(
                                        stmIntType(),
                                        stmIntType(),
                                        stmIntType(),
                                        stmIntType(),
                                        stmIntType(),
                                        stmIntType(),
                                        stmIntType()
                                )
                        ),
                        stmEntry(252, 8,  // append
                                List.of(
                                        stmIntType()
                                )
                        ),
                        stmEntry(253, 11,  // append
                                List.of(
                                        stmIntType(),
                                        stmIntType()
                                )
                        ),
                        stmEntry(255, 1,  // full
                                List.of(
                                        stmIntType(),
                                        stmIntType()
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_ifElseStmt_append_same() {
        JavaClass res = test("stackmapframe/if/13.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "iload_0", // 2
                "ifeq -> 11", // 3
                "iconst_2", // 6
                "istore_1", // 7
                "goto -> 13", // 8
                "iconst_3", // 11
                "istore_1", // 12
                "return" // 13
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(252, 11,  // append
                                List.of(
                                        stmIntType()
                                )
                        ),
                        stmEntry(1) // same
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_ifElseStmt_nestedIf() {
        JavaClass res = test("stackmapframe/if/14.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "iload_0", // 2
                "ifeq -> 17", // 3
                "iconst_2", // 6
                "istore_2", // 7
                "iload_0", // 8
                "ifeq -> 14", // 9
                "iconst_0", // 12
                "istore_0", // 13
                "goto -> 19", // 14
                "iconst_3", // 17
                "istore_1", // 18
                "return" // 19
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(252, 14,  // append
                                List.of(
                                        stmIntType()
                                )
                        ),
                        stmEntry(2),
                        stmEntry(1)
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_ifElseStmt_nestedIfInBoth_append_chop_same() {
        JavaClass res = test("stackmapframe/if/15.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "iload_0", // 2
                "ifeq -> 19", // 3
                "iconst_2", // 6
                "istore_2", // 7
                "iload_0", // 8
                "ifeq -> 14", // 9
                "iconst_0", // 12
                "istore_0", // 13
                "iconst_1", // 14
                "istore_0", // 15
                "goto -> 27", // 16
                "iconst_3", // 19
                "istore_2", // 20
                "iload_0", // 21
                "ifeq -> 27", // 22
                "iconst_0", // 25
                "istore_0", // 26
                "return" // 27
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(253, 14,  // append
                                List.of(
                                        stmIntType(),
                                        stmIntType()
                                )
                        ),
                        stmEntry(250, 4), // chop
                        stmEntry(7) // same
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_ifElseStmt_nestedIfInBoth_append_chop_append_chop() {
        JavaClass res = test("stackmapframe/if/16.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "iload_0", // 2
                "ifeq -> 19", // 3
                "iconst_2", // 6
                "istore_2", // 7
                "iload_0", // 8
                "ifeq -> 14", // 9
                "iconst_0", // 12
                "istore_0", // 13
                "iconst_1", // 14
                "istore_0", // 15
                "goto -> 29", // 16
                "iconst_3", // 19
                "istore_2", // 20
                "iload_0", // 21
                "ifeq -> 27", // 22
                "iconst_0", // 25
                "istore_0", // 26
                "iconst_1", // 27
                "istore_0", // 28
                "return" // 29
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(253, 14,  // append
                                List.of(
                                        stmIntType(),
                                        stmIntType()
                                )
                        ),
                        stmEntry(250, 4), // chop
                        stmEntry(252, 7, // append
                                List.of(
                                        stmIntType()
                                )
                        ),
                        stmEntry(250, 1) // chop
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_tripleNestedIfStmt_same() {
        JavaClass res = test("stackmapframe/if/17.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iload_0",
                "ifeq -> 14", // 1
                "iload_0", // 4
                "ifeq -> 14", // 5
                "iload_0", // 8
                "ifeq -> 14", // 9
                "iconst_0", // 12
                "istore_0", // 13
                "return" // 14
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(14) // same
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_ifStmt_full_instance_method() {
        JavaClass res = test("stackmapframe/if/18.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_2", // 1
                "new Test", // 2
                "dup", // 5
                "invokespecial Test/<init>()V", // 6
                "astore_3", // 9
                "iconst_2", // 10
                "anewarray Test", // 11
                "astore 4", // 14
                "iconst_2", // 16
                "newarray int", // 17
                "astore 5", // 19
                "iload_1", // 21
                "ifeq -> 27", // 22
                "iconst_0", // 25
                "istore_1", // 26
                "return" // 27
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(255, 27,  // full
                                List.of(
                                        stmObjectType("Test"),
                                        stmIntType(),
                                        stmIntType(),
                                        stmObjectType("Test"),
                                        stmObjectType("[LTest;"),
                                        stmObjectType("[I")
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_ifStmt_append_float() {
        JavaClass res = test("stackmapframe/if/19.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "ldc 1.1",
                "fstore_1", // 2
                "iload_0", // 3
                "ifeq -> 9", // 4
                "iconst_0", // 7
                "istore_0", // 8
                "return" // 9
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(252, 9,  // append
                                List.of(
                                        stmFloatType()
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_whileStmt() {
        JavaClass res = test("stackmapframe/while/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "iload_0", // 2
                "ifeq -> 11", // 3
                "iconst_2", // 6
                "istore_1", // 7
                "goto -> 2", // 8
                "return" // 11
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(252, 2, // append
                                List.of(
                                        stmIntType()
                                )
                        ),
                        stmEntry(8) // same
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_nestedWhileStmt() {
        JavaClass res = test("stackmapframe/while/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "iload_0", // 2
                "ifeq -> 20", // 3
                "iconst_2", // 6
                "istore_2", // 7
                "iload_0", // 8
                "ifeq -> 17", // 9
                "iconst_3", // 12
                "istore_2", // 13
                "goto -> 8", // 14
                "goto -> 2", // 17
                "return" // 20
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(252, 2, // append
                                List.of(
                                        stmIntType()
                                )
                        ),
                        stmEntry(252, 5, // append
                                List.of(
                                        stmIntType()
                                )
                        ),
                        stmEntry(250, 8), // chop
                        stmEntry(2) // same
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_nestedWhileStmt2() {
        JavaClass res = test("stackmapframe/while/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "istore_1", // 1
                "iload_0", // 2
                "ifeq -> 22", // 3
                "iconst_2", // 6
                "istore_2", // 7
                "iload_0", // 8
                "ifeq -> 17", // 9
                "iconst_3", // 12
                "istore_2", // 13
                "goto -> 8", // 14
                "iconst_4", // 17
                "istore_2", // 18
                "goto -> 2", // 19
                "return" // 22
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(252, 2, // append
                                List.of(
                                        stmIntType()
                                )
                        ),
                        stmEntry(252, 5, // append
                                List.of(
                                        stmIntType()
                                )
                        ),
                        stmEntry(8), // same
                        stmEntry(250, 4) // chop
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_expr_not() {
        JavaClass res = test("stackmapframe/expr/1.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iload_0",
                "ifeq -> 8", // 1
                "iconst_0", // 4
                "goto -> 9", // 5
                "iconst_1", // 8
                "istore_0", // 9
                "return" // 10
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(8), // same
                        stmEntry(64, stmIntType()) // same locals 1 stack item
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_expr_not_not() {
        JavaClass res = test("stackmapframe/expr/2.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iload_0",
                "ifeq -> 8", // 1
                "iconst_0", // 4
                "goto -> 9", // 5
                "iconst_1", // 8
                "ifeq -> 16", // 9
                "iconst_0", // 12
                "goto -> 17", // 13
                "iconst_1", // 16
                "istore_0", // 17
                "return" // 18
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(8), // same
                        stmEntry(64, stmIntType()), // same locals 1 stack item
                        stmEntry(6), // same
                        stmEntry(64, stmIntType()) // same locals 1 stack item
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_expr_and() {
        JavaClass res = test("stackmapframe/expr/3.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iload_0",
                "ifeq -> 8", // 1
                "iload_1", // 4
                "goto -> 9", // 5
                "iconst_0", // 8
                "istore_0", // 9
                "return" // 10
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(8), // same
                        stmEntry(64, stmIntType()) // same locals 1 stack item
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_expr_or() {
        JavaClass res = test("stackmapframe/expr/4.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iload_0",
                "ifne -> 8", // 1
                "iload_1", // 4
                "goto -> 9", // 5
                "iconst_1", // 8
                "istore_0", // 9
                "return" // 10
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(8), // same
                        stmEntry(64, stmIntType()) // same locals 1 stack item
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_expr_eq() {
        JavaClass res = test("stackmapframe/expr/5.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iload_0",
                "iload_1", // 1
                "if_icmpne -> 9", // 2
                "iconst_1", // 5
                "goto -> 10", // 6
                "iconst_0", // 9
                "istore_2", // 10
                "return" // 11
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(9), // same
                        stmEntry(64, stmIntType()) // same locals 1 stack item
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_expr_and_or() {
        JavaClass res = test("stackmapframe/expr/6.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iload_0",
                "ifeq -> 16", // 1
                "iload_1", // 4
                "ifne -> 12", // 5
                "iload_2", // 8
                "goto -> 13", // 9
                "iconst_1", // 12
                "goto -> 17", // 13
                "iconst_0", // 16
                "istore_0", // 17
                "return" // 18
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(12), // same
                        stmEntry(64, stmIntType()), // same locals 1 stack item
                        stmEntry(2), // same
                        stmEntry(64, stmIntType()) // same locals 1 stack item
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_expr_method_call_full_frame() {
        JavaClass res = test("stackmapframe/expr/7.java");
        assert res != null;
        assertEquals(4, res.getMethods().length);

        List<String> instructions = List.of(
                "iload_0",
                "iload_1", // 1
                "ifeq -> 9", // 2
                "iload_2", // 5
                "goto -> 10", // 6
                "iconst_0", // 9
                "iload_2", // 10
                "ifeq -> 18", // 11
                "iconst_0", // 14
                "goto -> 19", // 15
                "iconst_1", // 18
                "invokestatic Test/or(ZZ)Z", // 19
                "invokestatic Test/and(ZZ)Z", // 22
                "ireturn" // 25
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(73, stmIntType()), // same locals 1 stack item
                        stmEntry(255, 0,
                                List.of(stmIntType(), stmIntType(), stmIntType()),
                                List.of(stmIntType(), stmIntType())),
                        stmEntry(255, 7, List.of(stmIntType(), stmIntType(), stmIntType()),
                                List.of(stmIntType(), stmIntType())),
                        stmEntry(255, 0, List.of(stmIntType(), stmIntType(), stmIntType()),
                                List.of(stmIntType(), stmIntType(), stmIntType()))
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_expr_and_stmt_jumps() {
        JavaClass res = test("stackmapframe/expr/8.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_0", // 0
                "istore_0", // 1
                "iload_0", // 2
                "bipush 10", // 3
                "if_icmpge -> 12", // 5
                "iconst_1", // 8
                "goto -> 13", // 9
                "iconst_0", // 12
                "ifeq -> 23", // 13
                "iload_0", // 16
                "iconst_1", // 17
                "iadd", // 18
                "istore_0", // 19
                "goto -> 2", // 20
                "return" // 23
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(252, 2, List.of(stmIntType())), // append
                        stmEntry(9), // same
                        stmEntry(64, stmIntType()), // same locals 1 stack item
                        stmEntry(9) // same
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_multiArr() {
        JavaClass res = test("stackmapframe/expr/9.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_2", // 0
                "iconst_3", // 1
                "multianewarray [[I 2", // 2
                "astore_1", // 6
                "iconst_2", // 7
                "iconst_3", // 8
                "iconst_4", // 9
                "multianewarray [[[LTest; 3", // 10
                "astore_2", // 14
                "aload_1", // 15
                "iconst_1", // 16
                "aaload", // 17
                "astore_3", // 18
                "aload_2", // 19
                "iconst_0", // 20
                "aaload", // 21
                "astore 4", // 22
                "iload_0", // 24
                "ifeq -> 30", // 27
                "iconst_0", // 28
                "istore_0", // 29
                "return" // 30
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(255, 30,
                                List.of(
                                        stmIntType(),
                                        stmObjectType("[[I"),
                                        stmObjectType("[[[LTest;"),
                                        stmObjectType("[I"),
                                        stmObjectType("[[LTest;")
                                )
                        ) // full
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_null_operand() {
        JavaClass res = test("stackmapframe/expr/10.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0",
                "iconst_1", // 1
                "aconst_null", // 2
                "iload_1", // 3
                "ifeq -> 11", // 4
                "iconst_0", // 7
                "goto -> 12", // 8
                "iconst_1", // 11
                "invokevirtual Test/get(ILTest;Z)I", // 12
                "istore_2", // 15
                "return" // 16
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(255, 11,
                                List.of(stmObjectType("Test"), stmIntType()),
                                List.of(stmObjectType("Test"), stmIntType(), stmNullType())
                        ), // full
                        stmEntry(255, 0,
                                List.of(stmObjectType("Test"), stmIntType()),
                                List.of(stmObjectType("Test"), stmIntType(), stmNullType(), stmIntType())
                        ) // full
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_null_local() {
        JavaClass res = test("stackmapframe/expr/11.java");
        assert res != null;
        assertEquals(2, res.getMethods().length);

        List<String> instructions = List.of(
                "aconst_null",
                "astore_2", // 1
                "iload_1", // 2
                "ifeq -> 14", // 3
                "new Test", // 6
                "dup", // 9
                "invokespecial Test/<init>()V", // 10
                "astore_2", // 13
                "aload_2", // 14
                "areturn" // 15
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(List.of(
                stmEntry(252, 14, List.of(stmObjectType("Test"))))
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_operand_types_int() {
        JavaClass res = test("stackmapframe/expr/12.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "newarray int", // 1
                "astore_2", // 3
                "aload_0", // 4
                "iconst_1", // 5
                "aload_2", // 6
                "iconst_0", // 7
                "iaload", // 8
                "iconst_2", // 9
                "iconst_2", // 10
                "iadd", // 11
                "aload_2", // 12
                "arraylength", // 13
                "iload_1", // 14
                "ifeq -> 22", // 15
                "iconst_0", // 18
                "goto -> 23", // 19
                "iconst_1", // 22
                "invokevirtual Test/get(IIIIZ)I", // 23
                "istore_3", // 26
                "return" // 27
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                    stmEntry(
                            255,
                            22,
                            List.of(
                                    stmObjectType("Test"),
                                    stmIntType(),
                                    stmObjectType("[I")
                            ),
                            List.of(
                                    stmObjectType("Test"),
                                    stmIntType(),
                                    stmIntType(),
                                    stmIntType(),
                                    stmIntType()
                            )
                    ),
                    stmEntry(
                            255,
                            0,
                            List.of(
                                    stmObjectType("Test"),
                                    stmIntType(),
                                    stmObjectType("[I")
                            ),
                            List.of(
                                    stmObjectType("Test"),
                                    stmIntType(),
                                    stmIntType(),
                                    stmIntType(),
                                    stmIntType(),
                                    stmIntType()
                            )
                    )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_operand_types_float() {
        JavaClass res = test("stackmapframe/expr/13.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "newarray float", // 1
                "astore_2", // 3
                "aload_0", // 4
                "fconst_1", // 5
                "aload_2", // 6
                "iconst_0", // 7
                "faload", // 8
                "fconst_2", // 9
                "fconst_2", // 10
                "fadd", // 11
                "iload_1", // 12
                "ifeq -> 20", // 13
                "iconst_0", // 16
                "goto -> 21", // 17
                "iconst_1", // 20
                "invokevirtual Test/get(FFFZ)I", // 21
                "istore_3", // 24
                "return" // 25
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(
                                255,
                                20,
                                List.of(
                                        stmObjectType("Test"),
                                        stmIntType(),
                                        stmObjectType("[F")
                                ),
                                List.of(
                                        stmObjectType("Test"),
                                        stmFloatType(),
                                        stmFloatType(),
                                        stmFloatType()
                                )
                        ),
                        stmEntry(
                                255,
                                0,
                                List.of(
                                        stmObjectType("Test"),
                                        stmIntType(),
                                        stmObjectType("[F")
                                ),
                                List.of(
                                        stmObjectType("Test"),
                                        stmFloatType(),
                                        stmFloatType(),
                                        stmFloatType(),
                                        stmIntType()
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_operand_types_load_local() {
        JavaClass res = test("stackmapframe/expr/14.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        List<String> instructions = List.of(
                "fconst_1",
                "fstore_1", // 1
                "new Test", // 2
                "dup", // 5
                "invokespecial Test/<init>()V", // 6
                "astore_2", // 9
                "iconst_2", // 10
                "anewarray Test", // 11
                "astore_3", // 14
                "iconst_2", // 15
                "iconst_2", // 16
                "multianewarray [[I 2", // 17
                "astore 4", // 21
                "fload_1", // 23
                "aload_2", // 24
                "aload_3", // 25
                "aload 4", // 26
                "iload_0", // 28
                "ifeq -> 36", // 29
                "iconst_0", // 32
                "goto -> 37", // 33
                "iconst_1", // 36
                "invokestatic Test/get(FLTest;[LTest;[[IZ)I", // 37
                "istore 5", // 40
                "return" // 42
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(
                                255,
                                36,
                                List.of(
                                        stmIntType(),
                                        stmFloatType(),
                                        stmObjectType("Test"),
                                        stmObjectType("[LTest;"),
                                        stmObjectType("[[I")
                                ),
                                List.of(
                                        stmFloatType(),
                                        stmObjectType("Test"),
                                        stmObjectType("[LTest;"),
                                        stmObjectType("[[I")
                                )
                        ),
                        stmEntry(
                                255,
                                0,
                                List.of(
                                        stmIntType(),
                                        stmFloatType(),
                                        stmObjectType("Test"),
                                        stmObjectType("[LTest;"),
                                        stmObjectType("[[I")
                                ),
                                List.of(
                                        stmFloatType(),
                                        stmObjectType("Test"),
                                        stmObjectType("[LTest;"),
                                        stmObjectType("[[I"),
                                        stmIntType()
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_operand_types_aaload() {
        JavaClass res = test("stackmapframe/expr/15.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "iconst_2", // 1
                "multianewarray [[I 2", // 2
                "astore_1", // 6
                "iconst_1", // 7
                "iconst_2", // 8
                "multianewarray [[LTest; 2", // 9
                "astore_2", // 13
                "aload_1", // 14
                "iconst_0", // 15
                "aaload", // 16
                "iconst_1", // 17
                "iaload", // 18
                "aload_2", // 19
                "iconst_0", // 20
                "aaload", // 21
                "iconst_1", // 22
                "aaload", // 23
                "iload_0", // 24
                "ifeq -> 32", // 25
                "iconst_0", // 28
                "goto -> 33", // 29
                "iconst_1", // 32
                "invokestatic Test/get(ILTest;Z)I", // 33
                "istore_3", // 36
                "return" // 37
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(
                                255,
                                32,
                                List.of(
                                        stmIntType(),
                                        stmObjectType("[[I"),
                                        stmObjectType("[[LTest;")
                                ),
                                List.of(
                                        stmIntType(),
                                        stmObjectType("Test")
                                )
                        ),
                        stmEntry(
                                255,
                                0,
                                List.of(
                                        stmIntType(),
                                        stmObjectType("[[I"),
                                        stmObjectType("[[LTest;")
                                ),
                                List.of(
                                        stmIntType(),
                                        stmObjectType("Test"),
                                        stmIntType()
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_operand_types_load_static_field() {
        JavaClass res = test("stackmapframe/expr/16.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        List<String> instructions = List.of(
                "getstatic Test.i I",
                "aload_0", // 3
                "getfield Test.f F", // 4
                "aload_0", // 7
                "getfield Test.t LTest;", // 8
                "aload_0", // 11
                "getfield Test.i2 [[I", // 12
                "aload_0", // 15
                "getfield Test.t3 [[[LTest;", // 16
                "iload_1", // 19
                "ifeq -> 27", // 20
                "iconst_0", // 23
                "goto -> 28", // 24
                "iconst_1", // 27
                "invokestatic Test/get(IFLTest;[[I[[[LTest;Z)I", // 28
                "istore_2", // 31
                "return" // 32
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(
                                255,
                                27,
                                List.of(
                                        stmObjectType("Test"),
                                        stmIntType()
                                ),
                                List.of(
                                        stmIntType(),
                                        stmFloatType(),
                                        stmObjectType("Test"),
                                        stmObjectType("[[I"),
                                        stmObjectType("[[[LTest;")
                                )
                        ),
                        stmEntry(
                                255,
                                0,
                                List.of(
                                        stmObjectType("Test"),
                                        stmIntType()
                                ),
                                List.of(
                                        stmIntType(),
                                        stmFloatType(),
                                        stmObjectType("Test"),
                                        stmObjectType("[[I"),
                                        stmObjectType("[[[LTest;"),
                                        stmIntType()
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_operand_types_new_arr() {
        JavaClass res = test("stackmapframe/expr/17.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_2",
                "newarray int", // 1
                "iconst_2", // 3
                "newarray float", // 4
                "iconst_2", // 6
                "newarray boolean", // 7
                "iconst_2", // 9,
                "anewarray Test", // 10,
                "iconst_2", // 13
                "iconst_2", // 14
                "multianewarray [[I 2", // 15
                "iconst_2", // 19
                "iconst_2", // 20
                "iconst_2", // 21
                "multianewarray [[[LTest; 3", // 22
                "iload_1", // 26
                "ifeq -> 34", // 27
                "iconst_0", // 30
                "goto -> 35", // 31
                "iconst_1", // 34
                "invokestatic Test/get([I[F[Z[LTest;[[I[[[LTest;Z)I", // 35
                "istore_2", // 38
                "return" // 39
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(
                                255,
                                34,
                                List.of(
                                        stmObjectType("Test"),
                                        stmIntType()
                                ),
                                List.of(
                                        stmObjectType("[I"),
                                        stmObjectType("[F"),
                                        stmObjectType("[Z"),
                                        stmObjectType("[LTest;"),
                                        stmObjectType("[[I"),
                                        stmObjectType("[[[LTest;")
                                )
                        ),
                        stmEntry(
                                255,
                                0,
                                List.of(
                                        stmObjectType("Test"),
                                        stmIntType()
                                ),
                                List.of(
                                        stmObjectType("[I"),
                                        stmObjectType("[F"),
                                        stmObjectType("[Z"),
                                        stmObjectType("[LTest;"),
                                        stmObjectType("[[I"),
                                        stmObjectType("[[[LTest;"),
                                        stmIntType()
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_operand_types_invoke() {
        JavaClass res = test("stackmapframe/expr/18.java");
        assert res != null;
        assertEquals(9, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_1",
                "invokestatic Test/getI(I)I", // 1
                "aload_0", // 4
                "invokevirtual Test/getF()F", // 5
                "aload_0", // 8
                "invokevirtual Test/getIa()[I", // 9
                "invokestatic Test/getTa()[[LTest;", // 12
                "aload_0", // 15
                "invokevirtual Test/getT()LTest;", // 16
                "aload_0", // 19
                "iload_1", // 20
                "invokevirtual Test/getB(Z)Z", // 21
                "ifeq -> 31", // 24
                "iconst_0", // 27
                "goto -> 32", // 28
                "iconst_1", // 31
                "invokestatic Test/get(IF[I[[LTest;LTest;Z)I", // 32
                "istore_2", // 35
                "return" // 36
        );

        assertInstructions(res.getMethods()[7].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(
                                255,
                                31,
                                List.of(
                                        stmObjectType("Test"),
                                        stmIntType()
                                ),
                                List.of(
                                        stmIntType(),
                                        stmFloatType(),
                                        stmObjectType("[I"),
                                        stmObjectType("[[LTest;"),
                                        stmObjectType("Test")
                                )
                        ),
                        stmEntry(
                                255,
                                0,
                                List.of(
                                        stmObjectType("Test"),
                                        stmIntType()
                                ),
                                List.of(
                                        stmIntType(),
                                        stmFloatType(),
                                        stmObjectType("[I"),
                                        stmObjectType("[[LTest;"),
                                        stmObjectType("Test"),
                                        stmIntType()
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[7].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_operand_types_dup() {
        JavaClass res = test("stackmapframe/expr/19.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        List<String> instructions = List.of(
                "new Test",
                "dup", // 3
                "invokespecial Test/<init>()V", // 4
                "iload_0", // 7
                "ifeq -> 15", // 8
                "iconst_0", // 11
                "goto -> 16", // 12
                "iconst_1", // 15
                "invokestatic Test/get(LTest;Z)I", // 16
                "istore_1", // 19
                "return" // 20
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(
                                79, stmObjectType("Test")
                        ),
                        stmEntry(
                                255,
                                0,
                                List.of(
                                        stmIntType()
                                ),
                                List.of(
                                        stmObjectType("Test"),
                                        stmIntType()
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void stackmapframe_operand_types_dup2_dupx2() {
        JavaClass res = test("stackmapframe/expr/20.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        List<String> instructions = List.of(
                "iconst_2",
                "newarray int", // 1
                "astore_1", // 3
                "aload_1", // 4
                "iconst_1", // 5
                "dup2", // 6
                "iaload", // 7
                "iconst_1", // 8
                "iadd", // 9
                "dup_x2", // 10
                "iastore", // 11
                "iload_0", // 12
                "ifeq -> 20", // 13
                "iconst_0", // 16
                "goto -> 21", // 17
                "iconst_1", // 20
                "invokestatic Test/get(IZ)I", // 21
                "istore_2", // 24
                "return" // 25
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(
                                255,
                                20,
                                List.of(
                                        stmIntType(),
                                        stmObjectType("[I")
                                ),
                                List.of(
                                        stmIntType()
                                )
                        ),
                        stmEntry(
                                255,
                                0,
                                List.of(
                                        stmIntType(),
                                        stmObjectType("[I")
                                ),
                                List.of(
                                        stmIntType(),
                                        stmIntType()
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    // "aload_0", "dup", "getfield Test.x I", "iconst_1", "iadd", "dup_x1", "putfield Test.x I"

    @Test
    public void stackmapframe_operand_types_dupx1() {
        JavaClass res = test("stackmapframe/expr/21.java");
        assert res != null;
        assertEquals(3, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0",
                "aload_0", // 1
                "dup", // 2
                "getfield Test.x I", // 3
                "iconst_1", // 6
                "iadd", // 7
                "dup_x1", // 8
                "putfield Test.x I", // 9
                "iload_1", // 12
                "ifeq -> 20", // 13
                "iconst_0", // 16
                "goto -> 21", // 17
                "iconst_1", // 20
                "invokevirtual Test/get(IZ)I", // 21
                "istore_2", // 24
                "return" // 25
        );

        assertInstructions(res.getMethods()[1].getCode(), res.getConstantPool(), instructions);

        Asserter<StackMap> stackMapTableAsserter = stmTable(
                List.of(
                        stmEntry(
                                255,
                                20,
                                List.of(
                                        stmObjectType("Test"),
                                        stmIntType()
                                ),
                                List.of(
                                        stmObjectType("Test"),
                                        stmIntType()
                                )
                        ),
                        stmEntry(
                                255,
                                0,
                                List.of(
                                        stmObjectType("Test"),
                                        stmIntType()
                                ),
                                List.of(
                                        stmObjectType("Test"),
                                        stmIntType(),
                                        stmIntType()
                                )
                        )
                )
        );
        stackMapTableAsserter.assertIt((StackMap) res.getMethods()[1].getCode().getAttributes()[0]);
    }

    @Test
    public void test_defaultConstructor() {
        JavaClass res = test("constructor/1.java");
        assert res != null;
        assertEquals(1, res.getMethods().length);

        List<String> instructions = List.of(
                "aload_0",
                "invokespecial java/lang/Object/<init>()V",
                "return"
        );

        assertInstructions(res.getMethods()[0].getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void test_constructor_params_statements() {
        JavaClass res = testWithConstructor("constructor/2.java");
        assert res != null;
        assertEquals(2, res.getFields().length);
        assertEquals(1, res.getMethods().length);

        Field x = res.getFields()[0];
        assertEquals("x", x.getName());
        assertEquals("I", x.getSignature());
        assertEquals(0, x.getAccessFlags());

        Field y = res.getFields()[1];
        assertEquals("y", y.getName());
        assertEquals("Z", y.getSignature());
        assertEquals(0, y.getAccessFlags());

        List<String> instructions = List.of(
                "aload_0",
                "invokespecial java/lang/Object/<init>()V",
                "aload_0",
                "iload_1",
                "putfield Test.x I",
                "aload_0",
                "iload_2",
                "putfield Test.y Z",
                "return"
        );

        Method constructor = res.getMethods()[0];
        assertEquals("<init>", constructor.getName());
        assertEquals(0, constructor.getAccessFlags());
        assertEquals("(IZ)V", constructor.getSignature());
        assertEquals(3, constructor.getCode().getMaxLocals());
        assertEquals(2, constructor.getCode().getMaxStack());

        assertInstructions(constructor.getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void test_multipleConstructors() {
        JavaClass res = testWithConstructor("constructor/3.java");
        assert res != null;
        assertEquals(2, res.getFields().length);
        assertEquals(2, res.getMethods().length);

        Field x = res.getFields()[0];
        assertEquals("x", x.getName());
        assertEquals("I", x.getSignature());
        assertEquals(0, x.getAccessFlags());

        Field y = res.getFields()[1];
        assertEquals("y", y.getName());
        assertEquals("Z", y.getSignature());
        assertEquals(0, y.getAccessFlags());

        List<String> instructions1 = List.of(
                "aload_0",
                "invokespecial java/lang/Object/<init>()V",
                "aload_0",
                "iload_1",
                "putfield Test.x I",
                "aload_0",
                "iload_2",
                "putfield Test.y Z",
                "return"
        );

        Method constructor1 = res.getMethods()[0];
        assertEquals("<init>", constructor1.getName());
        assertEquals(0, constructor1.getAccessFlags());
        assertEquals("(IZ)V", constructor1.getSignature());
        assertEquals(3, constructor1.getCode().getMaxLocals());
        assertEquals(2, constructor1.getCode().getMaxStack());

        assertInstructions(constructor1.getCode(), res.getConstantPool(), instructions1);

        List<String> instructions2 = List.of(
                "aload_0",
                "invokespecial java/lang/Object/<init>()V",
                "aload_0",
                "iload_1",
                "putfield Test.x I",
                "aload_0",
                "iconst_0",
                "putfield Test.y Z",
                "return"
        );

        Method constructor2 = res.getMethods()[1];
        assertEquals("<init>", constructor2.getName());
        assertEquals(0, constructor2.getAccessFlags());
        assertEquals("(I)V", constructor2.getSignature());
        assertEquals(2, constructor2.getCode().getMaxLocals());
        assertEquals(2, constructor2.getCode().getMaxStack());

        assertInstructions(constructor2.getCode(), res.getConstantPool(), instructions2);
    }

    @Test
    public void test_constructor_private() {
        JavaClass res = testWithConstructor("constructor/4.java");
        assert res != null;
        assertEquals(1, res.getFields().length);
        assertEquals(1, res.getMethods().length);

        Field x = res.getFields()[0];
        assertEquals("x", x.getName());
        assertEquals("I", x.getSignature());
        assertEquals(0, x.getAccessFlags());

        List<String> instructions = List.of(
                "aload_0",
                "invokespecial java/lang/Object/<init>()V",
                "aload_0",
                "iload_1",
                "putfield Test.x I",
                "return"
        );

        Method constructor = res.getMethods()[0];
        assertEquals("<init>", constructor.getName());
        assertEquals(2, constructor.getAccessFlags());
        assertEquals("(I)V", constructor.getSignature());
        assertEquals(2, constructor.getCode().getMaxLocals());
        assertEquals(2, constructor.getCode().getMaxStack());

        assertInstructions(constructor.getCode(), res.getConstantPool(), instructions);
    }

    @Test
    public void multi_file() {
        List<JavaClass> classes = test(List.of("Test.java", "Other.java"), new HashSet<>(), "enter/1");
        assertNotNull(classes);
        assertEquals(2, classes.size());

        JavaClass testClass = classes.get(0);
        Field[] fields = testClass.getFields();
        Method[] methods = testClass.getMethods();

        assertEquals(1, fields.length);
        assertEquals("o", fields[0].getName());
        assertEquals("LOther;", fields[0].getSignature());
        assertEquals(2, methods.length);
        assertEquals("main", methods[1].getName());
        assertEquals("()V", methods[1].getSignature());

        List<String> instructions = List.of(
                "aload_0", "getfield Test.o LOther;", "getfield Other.i I", "istore_1",
                "aload_0", "getfield Test.o LOther;", "getfield Other.o LOther;", "getfield Other.i I", "istore_2",
                "return"
        );

        assertInstructions(testClass.getMethods()[1].getCode(), testClass.getConstantPool(), instructions);

        JavaClass otherClass = classes.get(1);
        fields = otherClass.getFields();
        methods = otherClass.getMethods();

        assertEquals(2, fields.length);
        assertEquals("i", fields[0].getName());
        assertEquals("I", fields[0].getSignature());
        assertEquals("o", fields[1].getName());
        assertEquals("LOther;", fields[1].getSignature());
        assertEquals(1, methods.length);
    }

    @Test
    public void multi_file_find() {
        List<JavaClass> classes = test(List.of("Test.java"), new HashSet<>(), "enter/1");
        assertNotNull(classes);
        assertEquals(2, classes.size());

        JavaClass testClass = classes.get(0);
        Field[] fields = testClass.getFields();
        Method[] methods = testClass.getMethods();

        assertEquals(1, fields.length);
        assertEquals("o", fields[0].getName());
        assertEquals("LOther;", fields[0].getSignature());
        assertEquals(2, methods.length);
        assertEquals("main", methods[1].getName());
        assertEquals("()V", methods[1].getSignature());

        List<String> instructions = List.of(
                "aload_0", "getfield Test.o LOther;", "getfield Other.i I", "istore_1",
                "aload_0", "getfield Test.o LOther;", "getfield Other.o LOther;", "getfield Other.i I", "istore_2",
                "return"
        );

        assertInstructions(testClass.getMethods()[1].getCode(), testClass.getConstantPool(), instructions);

        JavaClass otherClass = classes.get(1);
        fields = otherClass.getFields();
        methods = otherClass.getMethods();

        assertEquals(2, fields.length);
        assertEquals("i", fields[0].getName());
        assertEquals("I", fields[0].getSignature());
        assertEquals("o", fields[1].getName());
        assertEquals("LOther;", fields[1].getSignature());
        assertEquals(1, methods.length);
    }

    @Test
    public void multi_file_find_many() {
        List<JavaClass> classes = test(List.of("Test.java"), Set.of("C"), "enter/2");
        assertNotNull(classes);
        assertEquals(5, classes.size());

        JavaClass TestClass = classes.get(0);
        Field[] fields = TestClass.getFields();
        Method[] methods = TestClass.getMethods();

        assertEquals(1, fields.length);
        assertEquals("a", fields[0].getName());
        assertEquals("LA;", fields[0].getSignature());

        assertEquals(2, methods.length);
        assertEquals("main", methods[1].getName());
        assertEquals("(LB;)V", methods[1].getSignature());

        List<String> mainInstructions = List.of(
                "aload_0", "getfield Test.a LA;", "bipush 13", "invokevirtual A/getC(I)LC;", "astore_2",
                "aload_2", "iconst_3", "invokevirtual C/mult(I)I", "istore_3",
                "aload_1", "aload_0", "getfield Test.a LA;", "getfield A.val I", "invokevirtual B/done(I)Z", "istore 4",
                "return"
        );

        assertInstructions(TestClass.getMethods()[1].getCode(), TestClass.getConstantPool(), mainInstructions);

        JavaClass AClass = classes.get(1);
        fields = AClass.getFields();
        methods = AClass.getMethods();

        assertEquals(1, fields.length);
        assertEquals("val", fields[0].getName());
        assertEquals("I", fields[0].getSignature());

        assertEquals(2, methods.length);
        assertEquals("getC", methods[1].getName());
        assertEquals("(I)LC;", methods[1].getSignature());

        List<String> getCInstructions = List.of(
                "new C", "dup", "iload_1", "invokespecial C/<init>(I)V", "areturn"
        );

        assertInstructions(AClass.getMethods()[1].getCode(), AClass.getConstantPool(), getCInstructions);

        JavaClass BClass = classes.get(2);
        fields = BClass.getFields();
        methods = BClass.getMethods();

        assertEquals(0, fields.length);
        assertEquals(2, methods.length);
        assertEquals("done", methods[1].getName());
        assertEquals("(I)Z", methods[1].getSignature());

        List<String> doneInstructions = List.of(
                "iload_1", "bipush 10", "if_icmpge -> 10", "iconst_1", "goto -> 11", "iconst_0", "ireturn"
        );

        assertInstructions(BClass.getMethods()[1].getCode(), BClass.getConstantPool(), doneInstructions);

        JavaClass CClass = classes.get(3);
        fields = CClass.getFields();
        methods = CClass.getMethods();

        assertEquals(2, fields.length);
        assertEquals("x", fields[0].getName());
        assertEquals("I", fields[0].getSignature());
        assertEquals("d", fields[1].getName());
        assertEquals("LD;", fields[1].getSignature());

        assertEquals(2, methods.length);
        assertEquals("<init>", methods[0].getName());
        assertEquals("(I)V", methods[0].getSignature());
        assertEquals("mult", methods[1].getName());
        assertEquals("(I)I", methods[1].getSignature());

        List<String> constructorInstructions = List.of(
                "aload_0",
                "invokespecial java/lang/Object/<init>()V",
                "aload_0", "iload_1", "putfield C.x I", "return"
        );

        assertInstructions(CClass.getMethods()[0].getCode(), CClass.getConstantPool(), constructorInstructions);

        List<String> multInstructions = List.of(
                "aload_0", "getfield C.x I", "iload_1", "imul", "ireturn"
        );

        assertInstructions(CClass.getMethods()[1].getCode(), CClass.getConstantPool(), multInstructions);

        JavaClass DClass = classes.get(4);
        fields = DClass.getFields();
        methods = DClass.getMethods();

        assertEquals(1, fields.length);
        assertEquals("val", fields[0].getName());
        assertEquals("F", fields[0].getSignature());
    }

}

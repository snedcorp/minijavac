package minijavac.unit.context;

import minijavac.cli.Args;
import minijavac.Compiler;
import minijavac.listener.Listener;
import minijavac.listener.SimpleListener;
import minijavac.ast.*;
import minijavac.err.CompileError;
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

public class IdCheckerTest {

    private static final Path ID_PATH;
    private static final Function<String, Asserter<Path>> fileAsserter;

    static {
        ID_PATH = Paths.get("src/test/resources/unit/id");
        fileAsserter = getFileAsserter(ID_PATH);
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
        args.files = List.of(ID_PATH.resolve(file));
        args.sourcePath = ID_PATH;

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
    public void fail_undeclaredClassInFieldDecl() {
        String file = "fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertSymbolErr(errs.get(0), isFile(file), 5, 4, "class Missing", "class Test");
    }

    @Test
    public void fail_shadowParamInBlock() {
        String file = "fail3.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 5, 16, "variable param is already defined in method foo(int)");
    }

    @Test
    public void fail_shadowLocalVarInBlock() {
        String file = "fail4.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 6, 16, "variable x is already defined in method foo(int)");
    }

    @Test
    public void fail_undeclaredIdRef() {
        String file = "fail5.java";
        List<CompileError> errs = fail(file, 1);
        assertSymbolErr(errs.get(0), isFile(file), 6, 20, "variable z", "class Test");
    }

    @Test
    public void fail_undeclaredClassInParameter() {
        String file = "fail6.java";
        List<CompileError> errs = fail(file, 1);
        assertSymbolErr(errs.get(0), isFile(file), 5, 20, "class weird", "class Test");
    }

    @Test
    public void fail_duplicateParams() {
        String file = "fail7.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 3, 31, "variable x is already defined in method foo");
    }

    @Test
    public void fail_singleVarDeclStmtInConditionalBranch() {
        String file = "fail8.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 5, 16, "variable declaration not allowed here");
    }

    @Test
    public void fail_instanceVariableReferencedFromStaticMethod() {
        String file = "fail9.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 3, 16, "non-static variable x cannot be referenced from a static context");
        assertErr(errs.get(1), isFile(file), 3, 24, "non-static method getI(int) cannot be referenced from a static context");
    }

    @Test
    public void fail_duplicateClass() {
        String file = "fail10.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 5, 0, "duplicate class: Test");
    }

    @Test
    public void fail_duplicateMembers() {
        String file = "fail11.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 5, 16, "variable x is already defined in class Test");
    }

    @Test
    public void fail_singleVarDeclStmtInElseBranch() {
        String file = "fail12.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 5, 17, "variable declaration not allowed here");
    }

    @Test
    public void fail_dereferenceArray() {
        String file = "fail13.java";
        List<CompileError> errs = fail(file, 1);
        assertSymbolErr(errs.get(0), isFile(file), 6, 18, "variable x", "variable d of type D[]");
    }

    @Test
    public void fail_dereferenceInt() {
        String file = "fail14.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 4, 14, "int cannot be dereferenced");
    }

    @Test
    public void fail_singleVarDeclStmtInWhileStmt() {
        String file = "fail15.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 5, 16, "variable declaration not allowed here");
    }

    @Test
    public void fail_assignClassReferenceToVariable() {
        String file = "fail17.java";
        List<CompileError> errs = fail(file, 1);
        assertSymbolErr(errs.get(0), isFile(file), 3, 16, "variable F02", "class Test");
    }

    @Test
    public void fail_qRef_refMethodAsField() {
        String file = "fail18.java";
        List<CompileError> errs = fail(file, 1);
        assertSymbolErr(errs.get(0), isFile(file), 4, 14, "variable foo", "variable c of type F05");
    }

    @Test
    public void fail_refVarDeclInInitExp() {
        String file = "fail19.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 7, 20, "variable x might not have been initialized");
    }

    @Test
    public void fail_thisInStaticMethod() {
        String file = "fail20.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 4, 22, "non-static variable this cannot be referenced from a static context");
    }

    @Test
    public void fail_thisQRefInStaticMethod() {
        String file = "fail21.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 4, 16, "non-static variable this cannot be referenced from a static context");
    }

    @Test
    public void fail_qRefStaticPrivate() {
        String file = "fail22.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 4, 22, "privstatfield has private access in Other");
        assertErr(errs.get(1), isFile(file), 5, 22, "privstatmethod() has private access in Other");
    }

    @Test
    public void fail_classRefExp() {
        String file = "fail23.java";
        List<CompileError> errs = fail(file, 1);
        assertSymbolErr(errs.get(0), isFile(file), 7, 12, "variable TestClass", "class TestClass");
    }

    @Test
    public void fail_refMethodAsVariable() {
        String file = "fail24.java";
        List<CompileError> errs = fail(file, 1);
        assertSymbolErr(errs.get(0), isFile(file), 7, 12, "variable pubfn", "class TestClass");
    }

    @Test
    public void fail_instanceRefFromStaticMethod() {
        String file = "fail25.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 4, 26, "non-static variable pubfield cannot be referenced from a static context");
        assertErr(errs.get(1), isFile(file), 5, 22, "non-static method getI(int,int) cannot be referenced from a static context");
    }

    @Test
    public void fail_multipleQRefMethodAsVariable() {
        String file = "fail26.java";
        List<CompileError> errs = fail(file, 1);
        assertSymbolErr(errs.get(0), isFile(file), 5, 40, "variable privfn", "variable opubstatTest of type TestClass");
    }

    @Test
    public void fail_IxRefToInt() {
        String file = "fail27.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 4, 8, "array required, but int found");
    }

    @Test
    public void fail_IxRefToClassTypeVariable() {
        String file = "fail28.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 4, 8, "array required, but Test found");
    }

    @Test
    public void fail_IxQRefToInt() {
        String file = "fail29.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 6, 11, "array required, but int found");
    }

    @Test
    public void fail_IxQRefToClassTypeVariable() {
        String file = "fail30.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 6, 11, "array required, but Test found");
    }

    @Test
    public void fail_dereferenceIntArray() {
        String file = "fail31.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 4, 12, "int cannot be dereferenced");
    }

    @Test
    public void fail_qRefPrivate() {
        String file = "fail32.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 5, 10, "x has private access in Other");
        assertErr(errs.get(1), isFile(file), 6, 18, "xm(int,boolean,int[][]) has private access in Other");
    }

    @Test
    public void fail_qRefPrevArrayRefInvalidId() {
        String file = "fail33.java";
        List<CompileError> errs = fail(file, 1);
        assertSymbolErr(errs.get(0), isFile(file), 4, 13,
                "variable x", "class Other");
    }

    @Test
    public void fail_qRefPrevArrayRefPrivate() {
        String file = "fail34.java";
        List<CompileError> errs = fail(file, 1);
        assertEquals(1, errs.size());
        assertErr(errs.get(0), isFile(file), 4, 13, "x has private access in Other");
    }

    @Test
    public void fail_multiLevelQRefWithIx() {
        String file = "fail35.java";
        List<CompileError> errs = fail(file, 1);
        assertSymbolErr(errs.get(0), isFile(file), 4, 17,
                "variable d", "variable c of type C");
    }

    @Test
    public void fail_dereferenceIntReturnValue() {
        String file = "fail36.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 5, 22, "int cannot be dereferenced");
    }

    @Test
    public void fail_dereferenceVoidReturnValue() {
        String file = "fail37.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 5, 22, "void cannot be dereferenced");
    }

    @Test
    public void fail_qualifiedCallRef() {
        String file = "fail38.java";
        List<CompileError> errs = fail(file, 5);
        assertSymbolErr(errs.get(0), isFile(file), 5, 21,
                "method i()", "class Test");
        assertSymbolErr(errs.get(1), isFile(file), 6, 17,
                "method i()", "class Test");
        assertSymbolErr(errs.get(2), isFile(file), 7, 19,
                "method i(int)", "variable o of type Other");
        assertErr(errs.get(3), isFile(file), 8, 19, "im(int,int) has private access in Other");
        assertSymbolErr(errs.get(4), isFile(file), 9, 24,
                "variable p", "class Test");
    }

    @Test
    public void fail_ixRef_tooManyDimsIndexed() {
        String file = "fail39.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 5, 22, "array required, but int found");
        assertErr(errs.get(1), isFile(file), 6, 26, "array required, but Test found");
    }

    @Test
    public void fail_ixQRef_tooManyDimsIndexed() {
        String file = "fail40.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 5, 22, "array required, but int found");
        assertErr(errs.get(1), isFile(file), 6, 26, "array required, but Test found");
    }

    @Test
    public void fail_qRef_prevArray_tooFewDimsIndexed() {
        String file = "fail41.java";
        List<CompileError> errs = fail(file, 2);
        assertSymbolErr(errs.get(0), isFile(file), 6, 22,
                "variable x", "class Test[]");
        assertSymbolErr(errs.get(1), isFile(file), 7, 30,
                "variable x", "class Test[][]");
    }

    @Test
    public void fail_qRef_refToNonExistentClass_shortCircuit_getMemberDecl() {
        String file = "fail42.java";
        List<CompileError> errs = fail(file, 2);
        assertSymbolErr(errs.get(0), isFile(file), 3, 8,
                "class A", "class Test");
        assertSymbolErr(errs.get(1), isFile(file), 3, 18,
                "class A", "class Test");
    }

    @Test
    public void fail_IxRefAfterCallRef_nonArrayReturnType() {
        String file = "fail43.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 3, 21, "array required, but int found");
    }

    @Test
    public void fail_duplicateMethodSignatures() {
        String file = "fail44.java";
        List<CompileError> errs = fail(file, 3);
        assertErr(errs.get(0), isFile(file), 6, 8, "method x(int) is already defined in class Test");
        assertErr(errs.get(1), isFile(file), 14, 8, "method y(boolean,int) is already defined in class Test");
        assertErr(errs.get(2), isFile(file), 22, 12, "method z(Test[]) is already defined in class Test");
    }

    @Test
    public void fail_noMethodsWithId() {
        String file = "fail45.java";
        List<CompileError> errs = fail(file, 3);
        assertSymbolErr(errs.get(0), isFile(file), 4, 8, "method get()", "class Test");
        assertSymbolErr(errs.get(1), isFile(file), 5, 8, "method get(int)", "class Test");
        assertSymbolErr(errs.get(2), isFile(file), 6, 10, "method get(boolean,int)", "variable o of type Other");
    }

    @Test
    public void fail_singleMethodCandidate_differentSize() {
        String file = "fail46.java";
        List<CompileError> errs = fail(file, 1);
        assertArgTypeErr(errs.get(0), isFile(file), 3, 16, "method f in class Test cannot be applied to given types;",
                "no arguments", "int");
    }

    @Test
    public void fail_singleMethodCandidate_sameSize_typeMismatch() {
        String file = "fail47.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 3, 18, "incompatible types: int cannot be converted to boolean");
        assertErr(errs.get(1), isFile(file), 4, 21, "incompatible types: boolean cannot be converted to int");
    }

    @Test
    public void fail_multipleMethodCandidates_noneSameSize() {
        String file = "fail48.java";
        List<CompileError> errs = fail(file, 1);
        assertNoSuitableMethodError(errs.get(0), isFile(file), 3, 16,
                "no suitable method found for f(int)", List.of("Test.f()", "Test.f(int,int)"), null);
    }

    @Test
    public void fail_multipleMethodCandidates_singleSameSize_typeMismatch() {
        String file = "fail49.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 3, 18, "incompatible types: int cannot be converted to boolean");
    }

    @Test
    public void fail_multipleMethodCandidates_multipleSameSize_allTypeMismatch() {
        String file = "fail50.java";
        List<CompileError> errs = fail(file, 1);
        assertNoSuitableMethodError(errs.get(0), isFile(file), 3, 16,
                "no suitable method found for f(int)", List.of("Test.f(boolean)", "Test.f(Test[])"),
                List.of("int cannot be converted to boolean", "int cannot be converted to Test[]"));
    }

    @Test
    public void fail_methodChaining_argTypes() {
        String file = "fail51.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 3, 13, "incompatible types: int cannot be converted to boolean");
        assertErr(errs.get(1), isFile(file), 4, 24, "incompatible types: boolean cannot be converted to int");
    }

    @Test
    public void fail_methodNesting_argTypes() {
        String file = "fail52.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 3, 18, "incompatible types: boolean cannot be converted to int");
        assertErr(errs.get(1), isFile(file), 4, 33, "incompatible types: boolean cannot be converted to int");
    }

    @Test
    public void fail_break_continue_outsideLoop() {
        String file = "fail53.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 3, 8, "break outside of loop");
        assertErr(errs.get(1), isFile(file), 4, 8, "continue outside of loop");
    }

    @Test
    public void fail_for_invalidCondReference() {
        String file = "fail54.java";
        List<CompileError> errs = fail(file, 1);
        assertSymbolErr(errs.get(0), isFile(file), 3, 22,
                "variable j", "class Test");
    }

    @Test
    public void fail_for_invalidReferenceAfter() {
        String file = "fail55.java";
        List<CompileError> errs = fail(file, 1);
        assertSymbolErr(errs.get(0), isFile(file), 6, 8,
                "variable i", "class Test");
    }

    @Test
    public void fail_assignStmt_final() {
        String file = "fail56.java";
        List<CompileError> errs = fail(file, 3);
        assertErr(errs.get(0), isFile(file), 6, 8, "final parameter i may not be assigned");
        assertErr(errs.get(1), isFile(file), 7, 8, "cannot assign a value to final variable x");
        assertErr(errs.get(2), isFile(file), 9, 8, "cannot assign a value to final variable b");
    }

    @Test
    public void fail_singleVarDeclStmtInDoWhileStmt() {
        String file = "fail57.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 3, 15, "variable declaration not allowed here");
    }

    @Test
    public void fail_duplicateConstructorSignatures() {
        String file = "constructor/fail1.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 4, 4, "constructor Test(int) is already defined in class Test");
        assertErr(errs.get(1), isFile(file), 8, 4, "constructor Test() is already defined in class Test");
    }

    @Test
    public void fail_newObj_fakeClass() {
        String file = "newobj/fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertSymbolErr(errs.get(0), isFile(file), 4, 21, "class Fake", "class Test");
    }

    @Test
    public void fail_newObj_singleConstructorCandidate_differentSize() {
        String file = "newobj/fail2.java";
        List<CompileError> errs = fail(file, 1);
        assertArgTypeErr(errs.get(0), isFile(file), 3, 22, "constructor Other in class Other cannot be applied to given types;",
                "no arguments", "int");
    }

    @Test
    public void fail_singleConstructorCandidate_sameSize_typeMismatch() {
        String file = "newobj/fail3.java";
        List<CompileError> errs = fail(file, 2);
        assertErr(errs.get(0), isFile(file), 3, 28, "incompatible types: int cannot be converted to boolean");
        assertErr(errs.get(1), isFile(file), 4, 32, "incompatible types: boolean cannot be converted to int");
    }

    @Test
    public void fail_multipleConstructorCandidates_noneSameSize() {
        String file = "newobj/fail4.java";
        List<CompileError> errs = fail(file, 1);
        assertNoSuitableMethodError(errs.get(0), isFile(file), 3, 22,
                "no suitable constructor found for Other(int)", List.of("Other.Other()", "Other.Other(int,int)"), null);
    }

    @Test
    public void fail_multipleConstructorCandidates_singleSameSize_typeMismatch() {
        String file = "newobj/fail5.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 3, 28, "incompatible types: int cannot be converted to boolean");
    }

    @Test
    public void fail_multipleConstructorCandidates_multipleSameSize_allTypeMismatch() {
        String file = "newobj/fail6.java";
        List<CompileError> errs = fail(file, 1);
        assertNoSuitableMethodError(errs.get(0), isFile(file), 3, 22,
                "no suitable constructor found for Other(int)", List.of("Other.Other(boolean)", "Other.Other(Test[])"),
                List.of("int cannot be converted to boolean", "int cannot be converted to Test[]"));
    }

    @Test
    public void fail_newObjectExpr_privateConstructor() {
        String file = "newobj/fail7.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 3, 18, "Other(int) has private access in Other");
    }

    @Test
    public void fail_thisCall_outsideConstructor() {
        String file = "this/fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 5, 8, "call to this must be first statement in constructor");
    }

    @Test
    public void fail_thisCall_inConstructor_notFirstLine() {
        String file = "this/fail2.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 6, 8, "call to this must be first statement in constructor");
    }

    @Test
    public void fail_thisCall_multipleConstructorCandidates_noneSameSize() {
        String file = "this/fail3.java";
        List<CompileError> errs = fail(file, 1);
        assertNoSuitableMethodError(errs.get(0), isFile(file), 7, 8,
                "no suitable constructor found for Test(int)",
                List.of("Test.Test()", "Test.Test(int,int)", "Test.Test(int,int,int)"), null);
    }

    @Test
    public void fail_thisCall_multipleConstructorCandidates_singleSameSize_typeMismatch() {
        String file = "this/fail4.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 8, 13, "incompatible types: int cannot be converted to boolean");
    }

    @Test
    public void fail_thisCall_multipleConstructorCandidates_multipleSameSize_allTypeMismatch() {
        String file = "this/fail5.java";
        List<CompileError> errs = fail(file, 1);
        assertNoSuitableMethodError(errs.get(0), isFile(file), 9, 8,
                "no suitable constructor found for Test(int)",
                List.of("Test.Test(boolean)", "Test.Test(Test[])"),
                List.of("int cannot be converted to boolean", "int cannot be converted to Test[]"));
    }

    @Test
    public void pass_mainMethod() {
        pass("pass1.java");
    }

    @Test
    public void pass_classTypeInstanceVariable() {
        List<ClassDecl> classDecls = pass("pass2.java");
        FieldDecl a = classDecls.get(0).fieldDecls.get(0);
        Declaration decl = getClassTypeDecl(a);
        assertDecl(decl, ClassDecl.class, "A02", 6);
    }

    @Test
    public void pass_classShadowedByMemberVariable() {
        List<ClassDecl> classDecls = pass("pass3.java");
        Statement s = classDecls.get(0).methodDecls.get(1).statementList.get(0);
        Declaration decl = ((AssignStmt)s).ref.getDecl();
        assertDecl(decl, FieldDecl.class, "C", 7);
    }

    @Test
    public void pass_classBothShadowedAndReferencedInMethod() {
        List<ClassDecl> classDecls = pass("pass4.java");
        Statement s = classDecls.get(0).methodDecls.get(2).statementList.get(1);
        Declaration decl = ((RefExpr)((VarDeclStmt)s).expr).ref.getDecl();
        assertDecl(decl, ParameterDecl.class, "x", 4);
    }

    @Test
    public void pass_classBothShadowedAndReferencedInFields() {
        List<ClassDecl> classDecls = pass("pass5.java");
        FieldDecl c = classDecls.get(0).fieldDecls.get(1);
        Declaration decl = getClassTypeDecl(c);
        assertDecl(decl, ClassDecl.class, "C", 8);
    }

    @Test
    public void pass_paramShadowAndRefSameClass() {
        List<ClassDecl> classDecls = pass("pass6.java");

        ParameterDecl a = classDecls.get(1).methodDecls.get(1).parameterDeclList.get(0);
        Declaration decl = getClassTypeDecl(a);

        assertDecl(decl, ClassDecl.class, "A", 5);
    }

    @Test
    public void pass_staticMethodInvocation() {
        List<ClassDecl> classDecls = pass("pass7.java");

        VarDeclStmt s = (VarDeclStmt) classDecls.get(0).methodDecls.get(1).statementList.get(0);
        Declaration decl = getCallRefDecl(s.expr);

        assertDecl(decl, MethodDecl.class, "foo", 6);
    }

    @Test
    public void pass_staticMemberAccess() {
        List<ClassDecl> classDecls = pass("pass8.java");

        VarDeclStmt s = (VarDeclStmt) classDecls.get(0).methodDecls.get(1).statementList.get(0);
        Declaration decl = ((RefExpr)s.expr).ref.getDecl();

        assertDecl(decl, FieldDecl.class, "x", 6);
    }

    @Test
    public void pass_arrayUpdates() {
        List<ClassDecl> classDecls = pass("pass9.java");

        MethodDecl main = classDecls.get(0).methodDecls.get(1);

        AssignStmt aa0 = (AssignStmt) main.statementList.get(4);
        Declaration aa0Decl = aa0.ref.getDecl();
        assertDecl(aa0Decl, VarDecl.class, "aa", 5);

        AssignStmt aaI = (AssignStmt) ((BlockStmt)((WhileStmt)main.statementList.get(5)).body).statements.get(0);
        Declaration aaIDecl = aaI.ref.getDecl();
        assertDecl(aaIDecl, VarDecl.class, "aa", 5);
        Declaration aaI1Decl = ((RefExpr)((BinaryExpr)aaI.val).left).ref.getDecl();
        assertDecl(aaIDecl, VarDecl.class, "aa", 5);
    }

    @Test
    public void pass_qRefLinkedList_allowNestedPrivateAccessInSameClass() {
        List<ClassDecl> classDecls = pass("pass10.java");

        MethodDecl main = classDecls.get(0).methodDecls.get(1);

        QualRef qRef = (QualRef) ((AssignStmt) main.statementList.get(1)).ref;
        assertDecl(qRef.ref.getDecl(), VarDecl.class, "p", 3);
        assertDecl(qRef.getDecl(), FieldDecl.class, "next", 8);

        QualRef q2 = (QualRef) ((AssignStmt) main.statementList.get(2)).ref;
        QualRef q1 = (QualRef) q2.ref;
        QualRef q0 = (QualRef) q1.ref;

        assertDecl(q0.ref.getDecl(), VarDecl.class, "p", 3);
        assertDecl(q1.ref.getDecl(), FieldDecl.class, "next", 8);
        assertDecl(q2.ref.getDecl(), FieldDecl.class, "next", 8);
        assertDecl(q2.getDecl(), FieldDecl.class, "x", 9);
    }

    @Test
    public void pass_shadowReturnParameterNotClass() {
        List<ClassDecl> classDecls = pass("pass11.java");

        ReturnStmt r = (ReturnStmt) classDecls.get(0).methodDecls.get(2).statementList.get(0);
        Declaration decl = ((RefExpr) r.expr).ref.getDecl();
        assertDecl(decl, ParameterDecl.class, "A11", 8);
    }

    @Test
    public void pass_refFieldAndMethod() {
        List<ClassDecl> classDecls = pass("pass12.java");

        VarDeclStmt s = (VarDeclStmt) classDecls.get(0).methodDecls.get(1).statementList.get(1);
        BinaryExpr binop = (BinaryExpr) s.expr;

        QualRef lref = (QualRef) ((CallRef) ((RefExpr)binop.left).ref).ref;
        assertDecl(lref.ref.getDecl(), VarDecl.class, "p", 3);
        assertDecl(lref.getDecl(), MethodDecl.class, "p", 9);

        QualRef rref = (QualRef) ((RefExpr)binop.right).ref;
        assertDecl(rref.ref.getDecl(), VarDecl.class, "p", 3);
        assertDecl(rref.getDecl(), FieldDecl.class, "x", 7);
    }

    @Test
    public void pass_nestedStaticQRef() {
        List<ClassDecl> classDecls = pass("pass13.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(0);
        QualRef q1 = (QualRef) s.ref;
        QualRef q0 = (QualRef) q1.ref;
        assertDecl(q0.ref.getDecl(), ClassDecl.class, "Other", 11);
        assertDecl(q1.ref.getDecl(), FieldDecl.class, "back", 15);
        assertDecl(q1.getDecl(), FieldDecl.class, "mine", 7);
    }

    @Test
    public void pass_thisShadowedInstanceVariable() {
        List<ClassDecl> classDecls = pass("pass14.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(1);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();
        assertDecl(decl, VarDecl.class, "x", 6);

        QualRef qRef = (QualRef) s.ref;
        assertDecl(qRef.ref.getDecl(), ClassDecl.class, "Test", 1);
        assertDecl(qRef.getDecl(), FieldDecl.class, "x", 3);
    }

    @Test
    public void pass_varDecls() {
        List<ClassDecl> classDecls = pass("pass15.java");

        MethodDecl main = classDecls.get(0).methodDecls.get(1);

        Declaration pDecl = ((RefExpr)((IfStmt) main.statementList.get(2)).cond).ref.getDecl();
        assertDecl(pDecl, VarDecl.class, "p", 5);

        Declaration xDecl = ((AssignStmt)((IfStmt) main.statementList.get(2)).elseStmt).ref.getDecl();
        assertDecl(xDecl, VarDecl.class, "x", 6);
    }

    @Test
    public void pass_QRefWithIxRef() {
        List<ClassDecl> classDecls = pass("pass16.java");

        QualRef qRef = (QualRef) ((RefExpr)((VarDeclStmt)classDecls.get(0).methodDecls.get(1).statementList.get(1)).expr).ref;
        assertDecl(qRef.ref.getDecl(), VarDecl.class, "d", 4);
        assertDecl(qRef.getDecl(), FieldDecl.class, "x", 9);
    }

    @Test
    public void pass_thisixQRef() {
        List<ClassDecl> classDecls = pass("pass17.java");

        Reference ref = ((RefExpr)((VarDeclStmt)classDecls.get(0).methodDecls.get(1).statementList.get(0)).expr).ref;
        QualRef qRef = (QualRef) ((IxRef) ref).ref;
        assertDecl(qRef.ref.getDecl(), ClassDecl.class, "Test", 1);
        assertDecl(qRef.getDecl(), FieldDecl.class, "d", 3);
    }

    @Test
    public void pass_static_publicStaticMethod() {
        List<ClassDecl> classDecls = pass("pass18.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(2);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, MethodDecl.class, "pubstatfn", 28);
    }

    @Test
    public void pass_static_publicField() {
        List<ClassDecl> classDecls = pass("pass19.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(2);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, FieldDecl.class, "pubfield", 18);
    }

    @Test
    public void pass_static_qRef_publicStaticField() {
        List<ClassDecl> classDecls = pass("pass20.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(2);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, FieldDecl.class, "pubstatfield", 21);
    }

    @Test
    public void pass_static_qRef_publicStaticMethod() {
        List<ClassDecl> classDecls = pass("pass21.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(2);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, MethodDecl.class, "pubstatfn", 28);
    }

    @Test
    public void pass_static_other_qRef_publicStaticField() {
        List<ClassDecl> classDecls = pass("pass22.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(3);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, FieldDecl.class, "opubstatfield", 41);
    }

    @Test
    public void pass_static_other_qRef_publicStaticMethod() {
        List<ClassDecl> classDecls = pass("pass23.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(3);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, MethodDecl.class, "opubstatfn", 47);
    }

    @Test
    public void pass_static_qRef_classRef_publicStaticField() {
        List<ClassDecl> classDecls = pass("pass24.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(2);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, FieldDecl.class, "pubstatfield", 19);
    }

    @Test
    public void pass_static_other_qRef_classRef_publicStaticField() {
        List<ClassDecl> classDecls = pass("pass25.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(3);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, FieldDecl.class, "opubstatfield", 40);
    }

    @Test
    public void pass_instance_assignVarToItself() {
        List<ClassDecl> classDecls = pass("pass26.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(2);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, VarDecl.class, "x", 6);
    }

    @Test
    public void pass_static_qRef_privateField() {
        List<ClassDecl> classDecls = pass("pass27.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(2);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, FieldDecl.class, "privfield", 19);
    }

    @Test
    public void pass_static_qRef_privateStaticField() {
        List<ClassDecl> classDecls = pass("pass28.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(2);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, FieldDecl.class, "privstatfield", 23);
    }

    @Test
    public void pass_static_qRef_privateStaticMethod() {
        List<ClassDecl> classDecls = pass("pass29.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(2);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, MethodDecl.class, "privstatfn", 30);
    }

    @Test
    public void pass_instance_assignFromThis() {
        List<ClassDecl> classDecls = pass("pass30.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(3);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, ClassDecl.class, "TestClass", 1);
    }

    @Test
    public void pass_static_qRef_static_privateStaticField() {
        List<ClassDecl> classDecls = pass("pass31.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(2);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, FieldDecl.class, "privstatfield", 21);
    }

    @Test
    public void pass_static_qRef_static_privateStaticMethod() {
        List<ClassDecl> classDecls = pass("pass32.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(2);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, MethodDecl.class, "privstatfn", 27);
    }

    @Test
    public void pass_static_qRef_static_circle_privateField() {
        List<ClassDecl> classDecls = pass("pass33.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(3);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, FieldDecl.class, "privfield", 19);
    }

    @Test
    public void pass_static_qRef_static_circle_privateStaticMethod() {
        List<ClassDecl> classDecls = pass("pass34.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(3);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, MethodDecl.class, "privfn", 25);
    }

    @Test
    public void pass_instance_qRef_static_privateStaticField() {
        List<ClassDecl> classDecls = pass("pass35.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(2);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, FieldDecl.class, "privstatfield", 21);
    }

    @Test
    public void pass_instance_qRef_static_privateStaticMethod() {
        List<ClassDecl> classDecls = pass("pass36.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(2);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, MethodDecl.class, "privstatfn", 27);
    }

    @Test
    public void pass_instance_qRef_static_circle_privateField() {
        List<ClassDecl> classDecls = pass("pass37.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(3);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, FieldDecl.class, "privfield", 19);
    }

    @Test
    public void pass_instance_qRef_static_circle_privateStaticMethod() {
        List<ClassDecl> classDecls = pass("pass38.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(3);
        Declaration decl = ((RefExpr) s.val).ref.getDecl();

        assertDecl(decl, MethodDecl.class, "privfn", 25);
    }

    @Test
    public void pass_ixQRef() {
        List<ClassDecl> classDecls = pass("pass39.java");

        VarDeclStmt s = (VarDeclStmt) classDecls.get(0).methodDecls.get(1).statementList.get(1);
        Declaration decl = ((RefExpr) s.expr).ref.getDecl();
        assertDecl(decl, FieldDecl.class, "row", 10);

        VarDeclStmt s2 = (VarDeclStmt) classDecls.get(0).methodDecls.get(1).statementList.get(2);
        Declaration decl2 = ((RefExpr) s2.expr).ref.getDecl();
        assertDecl(decl2, FieldDecl.class, "col", 12);
    }

    @Test
    public void pass_mutualDependencies() {
        List<ClassDecl> classDecls = pass("pass40.java");

        CallStmt s = (CallStmt) classDecls.get(0).methodDecls.get(1).statementList.get(2);
        Declaration decl = s.methodRef.getDecl();
        assertDecl(decl, MethodDecl.class, "set", 17);

        IfStmt ifStmt = (IfStmt) classDecls.get(0).methodDecls.get(1).statementList.get(3);
        Declaration decl2 = ((RefExpr)((BinaryExpr) ifStmt.cond).right).ref.getDecl();
        assertDecl(decl2, MethodDecl.class, "get", 26);
    }

    @Test
    public void pass_nestedQRefWithIx() {
        List<ClassDecl> classDecls = pass("pass41.java");

        AssignStmt s = (AssignStmt) classDecls.get(0).methodDecls.get(1).statementList.get(1);
        QualRef e = (QualRef) s.ref;
        QualRef d = (QualRef) e.ref;
        IxRef cix = (IxRef) d.ref;
        QualRef c = (QualRef) cix.ref;
        QualRef b = (QualRef) c.ref;
        IxRef aix = (IxRef) b.ref;
        IdRef a = (IdRef) aix.ref;

        assertDecl(a.getDecl(), VarDecl.class, "a", 3);
        assertDecl(b.getDecl(), FieldDecl.class, "b", 9);
        assertDecl(c.getDecl(), FieldDecl.class, "c", 13);
        assertDecl(d.getDecl(), FieldDecl.class, "d", 17);
        assertDecl(e.getDecl(), FieldDecl.class, "e", 21);
    }

    @Test
    public void pass_callStmt() {
        List<ClassDecl> classDecls = pass("pass42.java");

        List<Statement> sl = classDecls.get(0).methodDecls.get(1).statementList;

        assertDecl(((CallStmt) sl.get(0)).methodRef.getDecl(), MethodDecl.class, "i", 14);
        assertDecl(((CallStmt) sl.get(1)).methodRef.getDecl(), MethodDecl.class, "i", 14);
        assertDecl(((CallStmt) sl.get(2)).methodRef.getDecl(), MethodDecl.class, "istat", 18);
        assertDecl(((CallStmt) sl.get(3)).methodRef.getDecl(), MethodDecl.class, "istat", 18);
        assertDecl(((CallStmt) sl.get(4)).methodRef.getDecl(), MethodDecl.class, "oistat", 28);
        assertDecl(((CallStmt) sl.get(5)).methodRef.getDecl(), MethodDecl.class, "oi", 24);
        assertDecl(((CallStmt) sl.get(6)).methodRef.getDecl(), MethodDecl.class, "i", 14);
    }

    @Test
    public void pass_callRefExpr() {
        List<ClassDecl> classDecls = pass("pass43.java");

        List<Statement> sl = classDecls.get(0).methodDecls.get(1).statementList;

        assertDecl(((RefExpr) ((AssignStmt) sl.get(0)).val).ref.getDecl(), MethodDecl.class, "i", 14);
        assertDecl(((RefExpr) ((AssignStmt) sl.get(1)).val).ref.getDecl(), MethodDecl.class, "i", 14);
        assertDecl(((RefExpr) ((AssignStmt) sl.get(2)).val).ref.getDecl(), MethodDecl.class, "istat", 18);
        assertDecl(((RefExpr) ((AssignStmt) sl.get(3)).val).ref.getDecl(), MethodDecl.class, "istat", 18);
        assertDecl(((RefExpr) ((AssignStmt) sl.get(4)).val).ref.getDecl(), MethodDecl.class, "oistat", 28);
        assertDecl(((RefExpr) ((AssignStmt) sl.get(5)).val).ref.getDecl(), MethodDecl.class, "oi", 24);
        assertDecl(((RefExpr) ((AssignStmt) sl.get(6)).val).ref.getDecl(), MethodDecl.class, "i", 14);
    }

    @Test
    public void pass_callRefArgs() {
        List<ClassDecl> classDecls = pass("pass44.java");

        List<Statement> sl = classDecls.get(0).methodDecls.get(1).statementList;

        CallRef callRef = (CallRef) ((RefExpr) ((VarDeclStmt) sl.get(1)).expr).ref;
        assertDecl(((RefExpr) callRef.argList.get(0)).ref.getDecl(), FieldDecl.class, "i", 2);
        assertDecl(((RefExpr) callRef.argList.get(1)).ref.getDecl(), LocalDecl.class, "i2", 4);

        CallRef callRef2 = (CallRef) ((RefExpr) ((VarDeclStmt) sl.get(2)).expr).ref;
        assertDecl(((RefExpr) callRef2.argList.get(0)).ref.getDecl(), LocalDecl.class, "i2", 4);
        assertDecl(((RefExpr) callRef2.argList.get(1)).ref.getDecl(), FieldDecl.class, "i", 2);
    }

    @Test
    public void pass_callThenIxRef() {
        List<ClassDecl> classDecls = pass("pass45.java");

        List<Statement> sl = classDecls.get(0).methodDecls.get(1).statementList;

        IxRef ixRef = (IxRef) ((RefExpr) ((VarDeclStmt) sl.get(0)).expr).ref;
        assertDecl(ixRef.getDecl(), MethodDecl.class, "get", 9);

        IxRef ix2 = (IxRef) ((RefExpr) ((VarDeclStmt) sl.get(1)).expr).ref;
        CallRef getCall = (CallRef) ix2.ref;
        QualRef get = (QualRef) getCall.ref;
        IxRef ix1 = (IxRef) get.ref;
        CallRef getTxCall = (CallRef) ix1.ref;
        QualRef getTx = (QualRef) getTxCall.ref;
        IxRef ix0 = (IxRef) getTx.ref;
        IdRef tx = (IdRef) ix0.ref;

        assertDecl(tx.getDecl(), FieldDecl.class, "tx", 3);
        assertDecl(getTx.getDecl(), MethodDecl.class, "getTx", 13);
        assertDecl(get.getDecl(), MethodDecl.class, "get", 9);
    }

    @Test
    public void pass_duplicateFieldAndMethod() {
        List<ClassDecl> classDecls = pass("pass46.java");

        assertEquals("x", classDecls.get(0).fieldDecls.get(0).id.contents);
        assertEquals("x", classDecls.get(0).methodDecls.get(1).id.contents);

        List<Statement> sl = classDecls.get(0).methodDecls.get(2).statementList;

        assertDecl(((RefExpr) ((VarDeclStmt) sl.get(0)).expr).ref.getDecl(), FieldDecl.class, "x", 3);
        assertDecl((((CallStmt) sl.get(1)).methodRef).getDecl(), MethodDecl.class, "x", 5);
    }

    @Test
    public void pass_duplicateMethods_differentSignatures() {
        pass("pass47.java");
    }

    @Test
    public void pass_method_singleCandidate() {
        List<ClassDecl> classDecls = pass("pass48.java");

        List<Statement> sl = classDecls.get(0).methodDecls.get(1).statementList;

        assertDecl((((CallStmt) sl.get(0)).methodRef).getDecl(), MethodDecl.class, "f", 7);
        assertDecl((((CallStmt) sl.get(1)).methodRef).getDecl(), MethodDecl.class, "f", 11);
    }

    @Test
    public void pass_method_multipleCandidates_singleSameSize() {
        List<ClassDecl> classDecls = pass("pass49.java");

        List<Statement> sl = classDecls.get(0).methodDecls.get(1).statementList;

        assertDecl((((CallStmt) sl.get(0)).methodRef).getDecl(), MethodDecl.class, "f", 8);
        assertDecl((((CallStmt) sl.get(1)).methodRef).getDecl(), MethodDecl.class, "f", 13);
    }

    @Test
    public void pass_method_multipleCandidates_multipleSameSize() {
        List<ClassDecl> classDecls = pass("pass50.java");

        List<Statement> sl = classDecls.get(0).methodDecls.get(1).statementList;

        assertDecl((((CallStmt) sl.get(0)).methodRef).getDecl(), MethodDecl.class, "f", 9);
        assertDecl((((CallStmt) sl.get(1)).methodRef).getDecl(), MethodDecl.class, "f", 14);
    }

    @Test
    public void pass_method_chaining_argTypes() {
        List<ClassDecl> classDecls = pass("pass51.java");

        CallStmt stmt = (CallStmt) classDecls.get(0).methodDecls.get(1).statementList.get(0);

        QualRef f = (QualRef) ((CallRef) stmt.methodRef).ref;
        IdRef getO = (IdRef) ((CallRef) f.ref).ref;

        assertDecl(getO.getDecl(), MethodDecl.class, "getO", 6);
        assertDecl(f.getDecl(), MethodDecl.class, "f", 12);
    }

    @Test
    public void pass_method_nesting() {
        List<ClassDecl> classDecls = pass("pass52.java");

        CallStmt stmt = (CallStmt) classDecls.get(0).methodDecls.get(1).statementList.get(0);

        CallRef fCall = (CallRef) stmt.methodRef;
        QualRef f = (QualRef) fCall.ref;
        CallRef getOCall = (CallRef) f.ref;
        IdRef getO = (IdRef) getOCall.ref;

        assertDecl(((RefExpr) getOCall.argList.get(0)).ref.getDecl(), MethodDecl.class, "getB", 6);
        assertDecl(getO.getDecl(), MethodDecl.class, "getO", 14);
        assertDecl(((RefExpr) fCall.argList.get(1)).ref.getDecl(), MethodDecl.class, "getI", 10);
        assertDecl(f.getDecl(), MethodDecl.class, "f", 20);
    }

    @Test
    public void pass_break_continue_inside_loop() {
        pass("pass53.java");
    }

    @Test
    public void pass_for_loop() {
        List<ClassDecl> classDecls = pass("pass54.java");

        ForStmt forStmt = (ForStmt) classDecls.get(0).methodDecls.get(1).statementList.get(0);

        assertDecl(((RefExpr) ((BinaryExpr) forStmt.cond).left).ref.getDecl(), VarDecl.class, "i", 3);
        assertDecl(((RefExpr) ((PostfixExpr) forStmt.updateStmt.expr).expr).ref.getDecl(), VarDecl.class, "i", 3);
        assertDecl(((RefExpr) ((BinaryExpr) ((IfStmt) ((BlockStmt) forStmt.body).statements.get(0)).cond).left).ref.getDecl(), VarDecl.class, "i", 3);
    }

    @Test
    public void pass_varDeclNoInit() {
        List<ClassDecl> classDecls = pass("pass55.java");

        List<Statement> sl = classDecls.get(0).methodDecls.get(1).statementList;

        assertDecl(((AssignStmt) sl.get(2)).ref.getDecl(), VarDecl.class, "i", 3);
        assertDecl(((AssignStmt) sl.get(3)).ref.getDecl(), VarDecl.class, "t", 4);
    }

    @Test
    public void pass_manyConstructors_differentSignatures() {
        List<ClassDecl> classDecls = pass("constructor/pass1.java");
        assertEquals(5, classDecls.get(0).methodDecls.size());
    }

    @Test
    public void pass_newObjectExpr_defaultConstructor() {
        List<ClassDecl> classDecls = pass("newobj/pass1.java");

        List<Statement> sl = classDecls.get(0).methodDecls.get(1).statementList;

        assertDecl(((NewObjectExpr) ((VarDeclStmt) sl.get(0)).expr).decl,
                MethodDecl.class, "Other");
    }

    @Test
    public void pass_newObjectExpr_privateConstructor_sameClass() {
        List<ClassDecl> classDecls = pass("newobj/pass2.java");

        List<Statement> sl = classDecls.get(0).methodDecls.get(1).statementList;

        assertDecl(((NewObjectExpr) ((VarDeclStmt) sl.get(0)).expr).decl,
                MethodDecl.class, "Test", 3);
    }

    @Test
    public void pass_newObjectExpr_constructor_overloading() {
        List<ClassDecl> classDecls = pass("newobj/pass3.java");

        List<Statement> sl = classDecls.get(0).methodDecls.get(1).statementList;

        assertDecl(((NewObjectExpr) ((VarDeclStmt) sl.get(0)).expr).decl,
                MethodDecl.class, "Other", 12);
        assertDecl(((NewObjectExpr) ((VarDeclStmt) sl.get(1)).expr).decl,
                MethodDecl.class, "Other", 14);
        assertDecl(((NewObjectExpr) ((VarDeclStmt) sl.get(2)).expr).decl,
                MethodDecl.class, "Other", 16);
        assertDecl(((NewObjectExpr) ((VarDeclStmt) sl.get(3)).expr).decl,
                MethodDecl.class, "Other", 18);
    }

    @Test
    public void pass_thisCall_constructor_overloading() {
        List<ClassDecl> classDecls = pass("this/pass1.java");

        List<Statement> sl = classDecls.get(0).methodDecls.get(3).statementList;
        assertDecl(((CallStmt) sl.get(0)).methodRef.getDecl(), MethodDecl.class, "Test", 3);

        sl = classDecls.get(0).methodDecls.get(4).statementList;
        assertDecl(((CallStmt) sl.get(0)).methodRef.getDecl(), MethodDecl.class, "Test", 5);

        sl = classDecls.get(0).methodDecls.get(5).statementList;
        assertDecl(((CallStmt) sl.get(0)).methodRef.getDecl(), MethodDecl.class, "Test", 7);
    }
}

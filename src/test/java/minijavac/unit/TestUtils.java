package minijavac.unit;

import minijavac.ast.*;
import minijavac.context.err.*;
import minijavac.err.*;
import minijavac.syntax.err.ExpectedParseError;
import minijavac.syntax.Token;
import minijavac.syntax.TokenKind;
import minijavac.syntax.err.ParseError;
import org.apache.bcel.classfile.StackMap;
import org.apache.bcel.classfile.StackMapEntry;
import org.apache.bcel.classfile.StackMapType;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class TestUtils {

    public static Function<String, Asserter<Path>> getFileAsserter(Path dir) {
        return file -> path -> assertEquals(dir.resolve(file), path);
    }

    public static void assertErr(CompileError err, Asserter<Path> file, int line, int offset, String msg) {
        file.assertIt(err.getPos().file());
        assertEquals(line, err.getPos().line());
        assertEquals(offset, err.getPos().offset());
        assertEquals(msg, err.getMsg());
    }

    public static void assertTokens(List<Token> tokens, List<TokenKind> kinds, List<String> ids, int[][] pos,
                                    Asserter<Path> file) {
        assertTokens(tokens, kinds, ids, pos, file, null, null);
    }

    public static void assertTokens(List<Token> tokens, List<TokenKind> kinds, List<String> ids, int[][] pos,
                                    Asserter<Path> file, List<String> nums, List<String> floats) {
        assertEquals(kinds.size(), tokens.size());

        Iterator<String> iter = ids != null ? ids.iterator() : null;
        Iterator<String> nIter = nums != null ? nums.iterator() : null;
        Iterator<String> fIter = floats != null ? floats.iterator() : null;

        for (int i=0; i<tokens.size(); i++) {
            Token token = tokens.get(i);
            TokenKind kind = kinds.get(i);

            assertEquals(kind, token.kind);
            if (kind == TokenKind.IDENTIFIER) {
                assertNotNull(iter);
                assertEquals(iter.next(), token.contents);
            } else if (kind == TokenKind.NUM) {
                assertNotNull(nIter);
                assertEquals(nIter.next(), token.contents);
            } else if (kind == TokenKind.FLOAT_NUM) {
                assertNotNull(fIter);
                assertEquals(fIter.next(), token.contents);
            }
            if (kind != TokenKind.EOF) {
                file.assertIt(token.pos.file());
                assertEquals(pos[i][0], token.pos.line());
                assertEquals(pos[i][1], token.pos.offset());
            }
        }
    }

    public static void assertParseErr(CompileError e, TokenKind kind, Asserter<Path> file, int line, int offset, String msg) {
        assertTrue(e instanceof ParseError);
        ParseError err = (ParseError) e;
        assertEquals(kind, err.getToken().kind);
        assertErr(err, file, line, offset, msg);
    }

    public static void assertExpParseErr(CompileError e, TokenKind prevKind, int prevLine, int prevOffset,
                                         TokenKind kind, Asserter<Path> file, int line, int offset, TokenKind expKind) {
        assertTrue(e instanceof ExpectedParseError);
        ExpectedParseError err = (ExpectedParseError) e;
        assertEquals(prevKind, err.getPrevToken().kind);
        assertEquals(prevLine, err.getPrevToken().pos.line());
        assertEquals(prevOffset, err.getPrevToken().pos.offset());
        assertEquals(kind, err.getToken().kind);
        file.assertIt(err.getToken().pos.file());
        assertEquals(line, err.getToken().pos.line());
        assertEquals(offset, err.getToken().pos.offset());
        assertEquals(expKind, err.getExpectedKind());
    }

    public static void assertSymbolErr(CompileError e, Asserter<Path> file, int line, int offset, String symbol,
                                       String location) {
        assertErr(e, file, line, offset, "cannot find symbol");
        assertTrue(e instanceof SymbolError);
        SymbolError err = (SymbolError) e;
        assertEquals(symbol, err.getSymbol());
        assertEquals(location, err.getLocation());
    }

    public static void assertDecl(Declaration decl, Class<?> clazz, String id, int line) {
        assertTrue(clazz.isInstance(decl));
        assertEquals(id, decl.id.contents);
        assertEquals(line, decl.id.pos.line());
    }

    public static void assertDecl(Declaration decl, Class<?> clazz, String id) {
        assertTrue(clazz.isInstance(decl));
        assertEquals(id, decl.id.contents);
    }

    public static Declaration getClassTypeDecl(Declaration decl) {
        return ((ClassType)decl.type).decl;
    }

    public static Declaration getCallRefDecl(Expression expr) {
        return ((RefExpr)expr).ref.getDecl();
    }

    public static void assertTypeErr(CompileError e, Asserter<Path> file, int line, int offset, String msg, String t1, String t2) {
        assertErr(e, file, line, offset, msg);
        assertTrue(e instanceof BinaryTypeError);
        BinaryTypeError err = (BinaryTypeError) e;
        assertEquals(t1, err.getT1());
        assertEquals(t2, err.getT2());
    }

    public static void assertArgTypeErr(CompileError e, Asserter<Path> file, int line, int offset, String msg, String required,
                                        String found) {
        assertErr(e, file, line, offset, msg);
        assertTrue(e instanceof ArgTypeError);
        ArgTypeError err = (ArgTypeError) e;
        assertEquals(required, err.getRequired());
        assertEquals(found, err.getFound());
        assertEquals("actual and formal argument lists differ in length", err.getReason());
    }

    public static void assertNoSuitableMethodError(CompileError e, Asserter<Path> file, int line, int offset, String msg,
                                                   List<String> candidates, List<String> mismatches) {
        assertErr(e, file, line, offset, msg);
        assertTrue(e instanceof NoSuitableMethodError);
        NoSuitableMethodError err = (NoSuitableMethodError) e;
        assertEquals(candidates.size(), err.methodCandidates.size());
        for (int i=0; i<candidates.size(); i++) {
            assertEquals(candidates.get(i), err.methodCandidates.get(i));
        }
        if (mismatches != null) {
            assertEquals(mismatches.size(), err.mismatches.size());
            for (int i=0; i<mismatches.size(); i++) {
                assertEquals(mismatches.get(i), err.mismatches.get(i));
            }
        }
    }

    public static void assertUnexpectedTypeErr(CompileError e, Asserter<Path> file, int line, int offset) {
        assertTrue(e instanceof UnexpectedTypeError);
        assertErr(e, file, line, offset, "unexpected type");
    }

    public static Asserter<StackMap> stmTable(List<Asserter<StackMapEntry>> entryAsserters) {
        return (StackMap stackMap) -> {
            assertEquals(entryAsserters.size(), stackMap.getMapLength());
            for (int i=0; i<entryAsserters.size(); i++) {
                entryAsserters.get(i).assertIt(stackMap.getStackMap()[i]);
            }
        };
    }

    public static Asserter<StackMapEntry> stmEntry(int frameType) {
        return (StackMapEntry stackMapEntry) -> {
            assertEquals(frameType, stackMapEntry.getFrameType());
        };
    }

    public static Asserter<StackMapEntry> stmEntry(int frameType, int offsetDelta) {
        return (StackMapEntry stackMapEntry) -> {
            assertEquals(frameType, stackMapEntry.getFrameType());
            assertEquals(offsetDelta, stackMapEntry.getByteCodeOffset());
        };
    }

    public static Asserter<StackMapEntry> stmEntry(int frameType, Asserter<StackMapType> operandAsserter) {
        return (StackMapEntry stackMapEntry) -> {
            assertEquals(frameType, stackMapEntry.getFrameType());
            assertEquals(1, stackMapEntry.getNumberOfStackItems());
            operandAsserter.assertIt(stackMapEntry.getTypesOfStackItems()[0]);
        };
    }

    public static Asserter<StackMapEntry> stmEntry(int frameType, int offsetDelta, List<Asserter<StackMapType>> localAsserters) {
        return (StackMapEntry stackMapEntry) -> {
            assertEquals(frameType, stackMapEntry.getFrameType());
            assertEquals(offsetDelta, stackMapEntry.getByteCodeOffset());
            assertEquals(localAsserters.size(), stackMapEntry.getNumberOfLocals());
            for (int i=0; i<localAsserters.size(); i++) {
                localAsserters.get(i).assertIt(stackMapEntry.getTypesOfLocals()[i]);
            }
        };
    }

    public static Asserter<StackMapEntry> stmEntry(int frameType, int offsetDelta,
                                                   List<Asserter<StackMapType>> localAsserters,
                                                   List<Asserter<StackMapType>> operandAsserters) {
        return (StackMapEntry stackMapEntry) -> {
            assertEquals(frameType, stackMapEntry.getFrameType());
            assertEquals(offsetDelta, stackMapEntry.getByteCodeOffset());
            assertEquals(localAsserters.size(), stackMapEntry.getNumberOfLocals());
            for (int i=0; i<localAsserters.size(); i++) {
                localAsserters.get(i).assertIt(stackMapEntry.getTypesOfLocals()[i]);
            }
            assertEquals(operandAsserters.size(), stackMapEntry.getNumberOfStackItems());
            for (int i=0; i<operandAsserters.size(); i++) {
                operandAsserters.get(i).assertIt(stackMapEntry.getTypesOfStackItems()[i]);
            }
        };
    }

    public static Asserter<StackMapType> stmIntType() {
        return (StackMapType stackMapType) -> {
            assertEquals("(type=Integer)", stackMapType.toString());
        };
    }

    public static Asserter<StackMapType> stmFloatType() {
        return (StackMapType stackMapType) -> {
            assertEquals("(type=Float)", stackMapType.toString());
        };
    }

    public static Asserter<StackMapType> stmNullType() {
        return (StackMapType stackMapType) -> {
            assertEquals("(type=Null)", stackMapType.toString());
        };
    }

    public static Asserter<StackMapType> stmObjectType(String objectType) {
        return (StackMapType stackMapType) -> {
            assertEquals(String.format("(type=Object, class=%s)", objectType), stackMapType.toString());
        };
    }
}

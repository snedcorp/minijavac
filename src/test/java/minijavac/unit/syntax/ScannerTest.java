package minijavac.unit.syntax;

import minijavac.listener.Listener;
import minijavac.listener.SimpleListener;
import minijavac.err.CompileError;
import minijavac.syntax.Scanner;
import minijavac.syntax.Token;
import minijavac.syntax.TokenKind;
import minijavac.unit.Asserter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static minijavac.syntax.TokenKind.*;
import static minijavac.unit.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class ScannerTest {

    private static final Path RESOURCES_PATH;
    private static final Path SCAN_PATH;
    private static final Function<String, Asserter<Path>> fileAsserter;

    static {
       RESOURCES_PATH = Paths.get("src/test/resources");
       SCAN_PATH = RESOURCES_PATH.resolve("unit/scan");
       fileAsserter = getFileAsserter(SCAN_PATH);
    }

    private static Asserter<Path> isFile(String file) {
        return fileAsserter.apply(file);
    }

    private List<CompileError> fail(String file, int expectedCount)  {
        SimpleListener listener = new SimpleListener();
        test(file, listener);
        assertTrue(listener.hasErrors());
        assertEquals(expectedCount, listener.getErrors().size());
        return listener.getErrors();
    }

    private List<Token> pass(String file) {
        SimpleListener listener = new SimpleListener();
        List<Token> tokens = test(file, listener);
        assertFalse(listener.hasErrors());
        return tokens;
    }

    private List<Token> test(String file, Listener listener) {
        List<Token> tokens = new ArrayList<>();
        Path filePath = SCAN_PATH.resolve(file);
        String resourceStr = "/" + RESOURCES_PATH.relativize(filePath);

        try (InputStream stream = getClass().getResourceAsStream(resourceStr)) {
            Scanner scanner = new Scanner(stream, filePath, listener);
            Token token;
            do {
                token = scanner.scan();
                tokens.add(token);
            } while (token.kind != TokenKind.EOF);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            Assertions.fail();
        }
        return tokens;
    }

    @Test
    public void fail_illegalChar() {
        String file = "fail1.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 1, 14, "illegal character: '#'");
    }

    @Test
    public void fail_unterminated_ml_comment() {
        String file = "fail3.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file), 1, 12, "unclosed comment");
    }

    @Test
    public void fail_partially_unterminated_ml_comment() {
        String file = "fail4.java";
        List<CompileError> errs = fail(file, 1);
        assertErr(errs.get(0), isFile(file),4, 1, "unclosed comment");
    }

    @Test
    public void fail_multipleErrors() {
        String file = "fail5.java";
        List<CompileError> errs = fail(file, 3);
        assertErr(errs.get(0), isFile(file), 1, 14, "illegal character: '#'");
        assertErr(errs.get(1), isFile(file),3, 4, "illegal character: '`'");
        assertErr(errs.get(2), isFile(file),5, 0, "unclosed comment");
    }

    @Test
    public void pass_empty_class() {
        String file = "pass1.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = List.of("id");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9}, {1, 10}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_id_with_underscore() {
        String file = "pass2.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET,TokenKind.EOF);
        List<String> ids = List.of("id_", "_id");
        int[][] pos = {{1, 0}, {1, 6}, {1, 10}, {1, 11}, {2, 0}, {2, 6}, {2, 10}, {2, 11}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_id_underscore_digits() {
        String file = "pass3.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = List.of("id_0_1__");
        int[][] pos = {{1, 0}, {1, 6}, {1, 14}, {1, 15}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_id_capital() {
        String file = "pass4.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = List.of("Class");
        int[][] pos = {{1, 0}, {1, 6}, {1, 11}, {1, 12}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_cr_newline() {
        String file = "pass5.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = List.of("id");
        int[][] pos = {{1, 0}, {2, 0}, {2, 3}, {2, 4}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_trailing_comment() {
        String file = "pass6.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = List.of("id");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9}, {1, 10}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_multiline_comment() {
        String file = "pass7.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = List.of("id");
        int[][] pos = {{1, 0}, {1, 20}, {1, 23}, {1, 24}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_multiline_comment_no_space_between() {
        String file = "pass8.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = List.of("id");
        int[][] pos = {{1, 0}, {1, 11}, {1, 14}, {1, 15}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_multiline_comment_nest_attempt() {
        String file = "pass9.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = List.of("id");
        int[][] pos = {{1, 0}, {1, 13}, {1, 16}, {1, 17}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_multiline_comment_garbage_between() {
        String file = "pass10.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = List.of("id");
        int[][] pos = {{1, 0}, {1, 13}, {1, 16}, {1, 17}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_unop() {
        String file = "pass11.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.VOID, IDENTIFIER, TokenKind.LPAREN, TokenKind.RPAREN, TokenKind.LCBRACKET,
                TokenKind.INT, IDENTIFIER, TokenKind.ASSIGN, TokenKind.MINUS, IDENTIFIER, TokenKind.SEMICOLON,
                TokenKind.BOOLEAN, IDENTIFIER, TokenKind.ASSIGN, TokenKind.NOT, IDENTIFIER, TokenKind.SEMICOLON,
                TokenKind.INT, IDENTIFIER, TokenKind.ASSIGN, TokenKind.COMPLEMENT, IDENTIFIER, TokenKind.SEMICOLON,
                TokenKind.RCBRACKET, TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = Arrays.asList("id", "p", "x", "b", "y", "y", "z", "x");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 9}, {2, 10}, {2, 11}, {2, 12},
                {3, 8}, {3, 12}, {3, 14}, {3, 17}, {3, 19}, {3, 20},
                {4, 8}, {4, 16}, {4, 18}, {4, 20}, {4, 21}, {4, 22},
                {5, 8}, {5, 12}, {5, 14}, {5, 16}, {5, 17}, {5, 18},
                {6, 4}, {7, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_unop_repeated() {
        String file = "pass12.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.VOID, IDENTIFIER, TokenKind.LPAREN, TokenKind.RPAREN, TokenKind.LCBRACKET,
                TokenKind.BOOLEAN, IDENTIFIER, TokenKind.ASSIGN,
                TokenKind.NOT, TokenKind.NOT, TokenKind.NOT, TokenKind.NOT,
                TokenKind.NOT, IDENTIFIER, TokenKind.SEMICOLON,
                TokenKind.INT, IDENTIFIER, TokenKind.ASSIGN,
                TokenKind.COMPLEMENT, TokenKind.COMPLEMENT, TokenKind.COMPLEMENT, IDENTIFIER, TokenKind.SEMICOLON,
                TokenKind.RCBRACKET, TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = Arrays.asList("id", "p", "x", "b", "y", "c");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 9}, {2, 10}, {2, 11}, {2, 12},
                {3, 8}, {3, 16}, {3, 18}, {3, 21}, {3, 22}, {3, 23}, {3, 24}, {3, 25}, {3, 26}, {3, 27},
                {4, 8}, {4, 12}, {4, 14}, {4, 16}, {4, 17}, {4, 18}, {4, 19}, {4, 20},
                {5, 4}, {6, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_unop_num() {
        String file = "pass13.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.VOID, IDENTIFIER, TokenKind.LPAREN, TokenKind.RPAREN,
                TokenKind.LCBRACKET, TokenKind.BOOLEAN, IDENTIFIER, TokenKind.ASSIGN,
                TokenKind.NUM, TokenKind.GT, TokenKind.MINUS, IDENTIFIER, TokenKind.SEMICOLON,
                TokenKind.RCBRACKET, TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = Arrays.asList("id", "p", "x", "b");
        List<String> nums = Arrays.asList("10");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 9}, {2, 10}, {2, 11}, {2, 12},
                {3, 8}, {3, 16}, {3, 18}, {3, 21}, {3, 24}, {3, 25}, {3, 27}, {3, 28},
                {4, 4}, {5, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file), nums, null);
    }

    @Test
    public void pass_binop_logical() {
        String file = "pass14.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.VOID, IDENTIFIER, TokenKind.LPAREN, TokenKind.RPAREN, TokenKind.LCBRACKET,
                TokenKind.BOOLEAN, IDENTIFIER, TokenKind.ASSIGN,
                TokenKind.TRUE, TokenKind.AND, TokenKind.FALSE, TokenKind.OR, IDENTIFIER,
                TokenKind.SEMICOLON, TokenKind.RCBRACKET, TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = Arrays.asList("id", "p", "x", "x");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 9}, {2, 10}, {2, 11}, {2, 12},
                {3, 8}, {3, 16}, {3, 18}, {3, 20}, {3, 25}, {3, 28}, {3, 34}, {3, 37}, {3, 38},
                {4, 4}, {5, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_unop_inBinop() {
        String file = "pass16.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.VOID, IDENTIFIER, TokenKind.LPAREN, TokenKind.RPAREN,
                TokenKind.LCBRACKET, TokenKind.INT, IDENTIFIER, TokenKind.ASSIGN,
                TokenKind.MINUS, IDENTIFIER, TokenKind.MINUS, TokenKind.MINUS, IDENTIFIER,
                TokenKind.SEMICOLON, TokenKind.RCBRACKET, TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = Arrays.asList("id", "p", "x", "b", "b");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 9}, {2, 10}, {2, 11}, {2, 12},
                {3, 8}, {3, 12}, {3, 14}, {3, 17}, {3, 18}, {3, 20}, {3, 22}, {3, 24}, {3, 25},
                {4, 4}, {5, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_unop_repeated_inBinop() {
        String file = "pass17.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.VOID, IDENTIFIER, TokenKind.LPAREN, TokenKind.RPAREN,
                TokenKind.LCBRACKET, TokenKind.INT, IDENTIFIER, TokenKind.ASSIGN,
                IDENTIFIER, TokenKind.MINUS, TokenKind.MINUS, TokenKind.MINUS,
                TokenKind.MINUS, IDENTIFIER,
                TokenKind.SEMICOLON, TokenKind.RCBRACKET, TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = Arrays.asList("id", "p", "x", "b", "b");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 9}, {2, 10}, {2, 11}, {2, 12},
                {3, 8}, {3, 12}, {3, 14}, {3, 17}, {3, 19}, {3, 21}, {3, 23}, {3, 25}, {3, 26}, {3, 27},
                {4, 4}, {5, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_empty_file() {
        String file = "pass18.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.EOF);

        assertTokens(tokens, kinds, null, null, null);
    }

    @Test
    public void pass_long_multiline_comment() {
        String file = "pass19.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = List.of("Test");
        int[][] pos = {{1, 0}, {1, 6}, {1, 11}, {10, 0}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_comment_newline() {
        String file = "pass20.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = List.of("id");
        int[][] pos = {{1, 0}, {2, 0}, {2, 3}, {2, 4}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_comment_cr() {
        String file = "pass21.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = List.of("id");
        int[][] pos = {{1, 0}, {2, 1}, {2, 4}, {2, 5}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_trailing_comment_cr() {
        String file = "pass22.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = List.of("id");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9}, {1, 10}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_trailing_comment_newline() {
        String file = "pass23.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = List.of("id");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9}, {1, 10}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_no_trailing() {
        String file = "pass24.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, TokenKind.IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = List.of("id");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9}, {1, 10}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_repeat_candidates_no_repeat() {
        String file = "pass25.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(CLASS, IDENTIFIER, LCBRACKET,
                VOID, IDENTIFIER, LPAREN, RPAREN, LCBRACKET,
                INT, IDENTIFIER, ASSIGN, LT, GT, BTW_AND, BTW_INC_OR, SEMICOLON,
                RCBRACKET, RCBRACKET, EOF);
        List<String> ids = List.of("id", "p", "x");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 9}, {2, 10}, {2, 11}, {2, 13},
                {3, 8}, {3, 12}, {3, 14}, {3, 16}, {3, 18}, {3, 20}, {3, 22}, {3, 23},
                {4, 4}, {5, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_repeats() {
        String file = "pass26.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(CLASS, IDENTIFIER, LCBRACKET,
                VOID, IDENTIFIER, LPAREN, RPAREN, LCBRACKET,
                INT, IDENTIFIER, ASSIGN, LSHIFT, RSHIFT, AND, OR, UN_RSHIFT, SEMICOLON,
                RCBRACKET, RCBRACKET, EOF);
        List<String> ids = List.of("id", "p", "x");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 9}, {2, 10}, {2, 11}, {2, 13},
                {3, 8}, {3, 12}, {3, 14}, {3, 16}, {3, 19}, {3, 22}, {3, 25}, {3, 28}, {3, 31},
                {4, 4}, {5, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_compound_candidates() {
        String file = "pass27.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(CLASS, IDENTIFIER, LCBRACKET,
                VOID, IDENTIFIER, LPAREN, RPAREN, LCBRACKET,
                INT, IDENTIFIER, ASSIGN, PLUS, MINUS, MULTIPLY, DIVIDE, MODULO, NOT, BTW_EXC_OR, SEMICOLON,
                RCBRACKET, RCBRACKET, EOF);
        List<String> ids = List.of("id", "p", "x");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 9}, {2, 10}, {2, 11}, {2, 13},
                {3, 8}, {3, 12}, {3, 14}, {3, 16}, {3, 18}, {3, 20}, {3, 22}, {3, 24}, {3, 26}, {3, 28}, {3, 29},
                {4, 4}, {5, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_compound_assignment() {
        String file = "pass28.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(CLASS, IDENTIFIER, LCBRACKET,
                VOID, IDENTIFIER, LPAREN, RPAREN, LCBRACKET,
                INT, IDENTIFIER, EQ, PLUS_ASSIGN, MINUS_ASSIGN, MULTIPLY_ASSIGN, DIVIDE_ASSIGN, MODULO_ASSIGN, NOT_EQ,
                BTW_EXC_OR_ASSIGN, GTE, LTE, BTW_AND_ASSIGN, BTW_INC_OR_ASSIGN, LSHIFT_ASSIGN, RSHIFT_ASSIGN, UN_RSHIFT_ASSIGN, SEMICOLON,
                RCBRACKET, RCBRACKET, EOF);
        List<String> ids = List.of("id", "p", "x");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 9}, {2, 10}, {2, 11}, {2, 13},
                {3, 8}, {3, 12},
                {3, 14}, {3, 17}, {3, 20}, {3, 23}, {3, 26}, {3, 29}, {3, 32}, {3, 35}, {3, 38}, {3, 41}, {3, 44}, {3, 47},
                {3, 50}, {3, 54}, {3, 58}, {3, 62},
                {4, 4}, {5, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_increment_decrement() {
        String file = "pass29.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(CLASS, IDENTIFIER, LCBRACKET,
                VOID, IDENTIFIER, LPAREN, RPAREN, LCBRACKET,
                INT, IDENTIFIER, ASSIGN, INCREMENT, DECREMENT, SEMICOLON,
                RCBRACKET, RCBRACKET, EOF);
        List<String> ids = List.of("id", "p", "x");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 9}, {2, 10}, {2, 11}, {2, 13},
                {3, 8}, {3, 12}, {3, 14}, {3, 16}, {3, 19}, {3, 21},
                {4, 4}, {5, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_ternary() {
        String file = "pass30.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(CLASS, IDENTIFIER, LCBRACKET,
                VOID, IDENTIFIER, LPAREN, RPAREN, LCBRACKET,
                INT, IDENTIFIER, ASSIGN, QUESTION, COLON, SEMICOLON,
                RCBRACKET, RCBRACKET, EOF);
        List<String> ids = List.of("id", "p", "x");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 9}, {2, 10}, {2, 11}, {2, 13},
                {3, 8}, {3, 12}, {3, 14}, {3, 16}, {3, 18}, {3, 19},
                {4, 4}, {5, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_for_control_flow() {
        String file = "pass31.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(CLASS, IDENTIFIER, LCBRACKET,
                VOID, IDENTIFIER, LPAREN, RPAREN, LCBRACKET,
                INT, IDENTIFIER, ASSIGN, FOR, BREAK, CONTINUE, SEMICOLON,
                RCBRACKET, RCBRACKET, EOF);
        List<String> ids = List.of("id", "p", "x");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 9}, {2, 10}, {2, 11}, {2, 13},
                {3, 8}, {3, 12}, {3, 14}, {3, 16}, {3, 20}, {3, 26}, {3, 34},
                {4, 4}, {5, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_null() {
        String file = "pass32.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(CLASS, IDENTIFIER, LCBRACKET,
                VOID, IDENTIFIER, LPAREN, RPAREN, LCBRACKET,
                INT, IDENTIFIER, ASSIGN, NULL, SEMICOLON,
                RCBRACKET, RCBRACKET, EOF);
        List<String> ids = List.of("id", "p", "x");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 9}, {2, 10}, {2, 11}, {2, 13},
                {3, 8}, {3, 12}, {3, 14}, {3, 16}, {3, 20},
                {4, 4}, {5, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_float() {
        String file = "pass33.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(CLASS, IDENTIFIER, LCBRACKET,
                VOID, IDENTIFIER, LPAREN, RPAREN, LCBRACKET,
                FLOAT, IDENTIFIER, ASSIGN, FLOAT_NUM, SEMICOLON,
                FLOAT, IDENTIFIER, ASSIGN, FLOAT_NUM, SEMICOLON,
                RCBRACKET, RCBRACKET, EOF);
        List<String> ids = List.of("id", "p", "x", "y");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 9}, {2, 10}, {2, 11}, {2, 13},
                {3, 8}, {3, 14}, {3, 16}, {3, 18}, {3, 21},
                {4, 8}, {4, 14}, {4, 16}, {4, 18}, {4, 25},
                {5, 4}, {6, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file), null, Arrays.asList("1.0", "200.023"));
    }

    @Test
    public void pass_final() {
        String file = "pass34.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(CLASS, IDENTIFIER, LCBRACKET,
                FINAL, INT, IDENTIFIER, SEMICOLON, RCBRACKET, EOF);
        List<String> ids = List.of("id", "x");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 10}, {2, 14}, {2, 15},
                {3, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_do_while() {
        String file = "pass35.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(CLASS, IDENTIFIER, LCBRACKET,
                VOID, IDENTIFIER, LPAREN, RPAREN, LCBRACKET, DO, LCBRACKET, RCBRACKET,
                WHILE, LPAREN, RPAREN, SEMICOLON, RCBRACKET, RCBRACKET, EOF);
        List<String> ids = List.of("id", "main");
        int[][] pos = {{1, 0}, {1, 6}, {1, 9},
                {2, 4}, {2, 9}, {2, 13}, {2, 14}, {2, 16},
                {3, 8}, {3, 11}, {3, 12}, {3, 14}, {3, 20}, {3, 21}, {3, 22},
                {4, 4}, {5, 0}
        };

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }

    @Test
    public void pass_id_with_dollar_sign() {
        String file = "pass36.java";
        List<Token> tokens = pass(file);

        List<TokenKind> kinds = Arrays.asList(TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.CLASS, IDENTIFIER, TokenKind.LCBRACKET,
                TokenKind.RCBRACKET, TokenKind.EOF);
        List<String> ids = List.of("id$", "$id");
        int[][] pos = {{1, 0}, {1, 6}, {1, 10}, {1, 11}, {2, 0}, {2, 6}, {2, 10}, {2, 11}};

        assertTokens(tokens, kinds, ids, pos, isFile(file));
    }
}

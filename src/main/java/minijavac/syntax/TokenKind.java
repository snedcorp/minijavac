package minijavac.syntax;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains all {@link Token} types - most with a defined spelling, and some (identifiers, numbers) without.
 */
public enum TokenKind {
    CLASS ("class"),
    VOID ("void"),
    PUBLIC ("public"),
    PRIVATE ("private"),
    STATIC ("static"),
    INT ("int"),
    FLOAT("float"),
    BOOLEAN ("boolean"),
    THIS ("this"),
    NULL ("null"),
    RETURN ("return"),
    IF ("if"),
    ELSE ("else"),
    WHILE ("while"),
    TRUE ("true"),
    FALSE ("false"),
    NEW ("new"),
    ASSIGN ("="),
    LCBRACKET ("{"),
    RCBRACKET ("}"),
    LBRACKET ("["),
    RBRACKET ("]"),
    SEMICOLON (";"),
    LPAREN ("("),
    RPAREN (")"),
    PERIOD ("."),
    COMMA (","),
    EQ ("=="),
    LT ("<"),
    GT (">"),
    LTE ("<="),
    GTE (">="),
    NOT_EQ ("!="),
    AND ("&&"),
    OR ("||"),
    NOT ("!"),
    PLUS ("+"),
    MINUS ("-"),
    MULTIPLY ("*"),
    DIVIDE ("/"),
    MODULO("%"),
    COMPLEMENT("~"),
    LSHIFT("<<"),
    RSHIFT(">>"),
    UN_RSHIFT(">>>"),
    BTW_AND("&"),
    BTW_EXC_OR("^"),
    BTW_INC_OR("|"),
    PLUS_ASSIGN("+="),
    MINUS_ASSIGN("-="),
    MULTIPLY_ASSIGN("*="),
    DIVIDE_ASSIGN("/="),
    MODULO_ASSIGN("%="),
    BTW_AND_ASSIGN("&="),
    BTW_EXC_OR_ASSIGN("^="),
    BTW_INC_OR_ASSIGN("|="),
    LSHIFT_ASSIGN("<<="),
    RSHIFT_ASSIGN(">>="),
    UN_RSHIFT_ASSIGN(">>>="),
    INCREMENT("++"),
    DECREMENT("--"),
    QUESTION("?"),
    COLON(":"),
    FOR("for"),
    BREAK("break"),
    CONTINUE("continue"),
    FINAL("final"),
    DO("do"),
    PACKAGE("package"),
    IMPORT("import"),
    NUM, FLOAT_NUM, IDENTIFIER, COMMENT, EOF;

    private String spelling;
    public static final Map<String, TokenKind> spellingMap;
    private static final Set<String> compoundPrefixes;
    private static final Set<String> repeaters;
    private static final Set<TokenKind> comparisonOps;
    private static final Set<TokenKind> assignmentOps;
    private static final Set<TokenKind> arithmeticOps;
    private static final Map<TokenKind, TokenKind> compoundToBaseMap;

    private static final Set<TokenKind> recognizableStatementStarters;

    TokenKind() {}

    TokenKind(String spelling) {
        this.spelling = spelling;
    }

    static {
        spellingMap = Arrays.stream(TokenKind.values())
                .filter(tk -> tk.spelling != null)
                .collect(Collectors.toMap(
                        tk -> tk.spelling,
                        tk -> tk
                ));

        repeaters = Stream.of(LT, GT, BTW_AND, BTW_INC_OR, PLUS, MINUS)
                .map(tk -> tk.spelling).collect(Collectors.toSet());

        compoundPrefixes = Stream.of(ASSIGN, LT, GT, NOT, PLUS, MINUS, MULTIPLY, MODULO, BTW_AND, BTW_EXC_OR,
                BTW_INC_OR, LSHIFT, RSHIFT, UN_RSHIFT).map(tk -> tk.spelling).collect(Collectors.toSet());

        comparisonOps = Stream.of(EQ, LT, GT, LTE, GTE, NOT_EQ).collect(Collectors.toSet());

        assignmentOps = Stream.of(ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, MULTIPLY_ASSIGN, DIVIDE_ASSIGN, MODULO_ASSIGN,
                BTW_AND_ASSIGN, BTW_EXC_OR_ASSIGN, BTW_INC_OR_ASSIGN, RSHIFT_ASSIGN, LSHIFT_ASSIGN, UN_RSHIFT_ASSIGN)
                .collect(Collectors.toSet());

        arithmeticOps = Stream.of(PLUS, MINUS, MULTIPLY, DIVIDE, MODULO, LSHIFT, RSHIFT, UN_RSHIFT, BTW_AND, BTW_EXC_OR,
                BTW_INC_OR).collect(Collectors.toSet());

        compoundToBaseMap = new HashMap<>(Map.ofEntries(
                Map.entry(PLUS_ASSIGN, PLUS), Map.entry(MINUS_ASSIGN, MINUS), Map.entry(MULTIPLY_ASSIGN, MULTIPLY),
                Map.entry(DIVIDE_ASSIGN, DIVIDE), Map.entry(MODULO_ASSIGN, MODULO), Map.entry(BTW_AND_ASSIGN, BTW_AND),
                Map.entry(BTW_EXC_OR_ASSIGN, BTW_EXC_OR), Map.entry(BTW_INC_OR_ASSIGN, BTW_INC_OR),
                Map.entry(LSHIFT_ASSIGN, LSHIFT), Map.entry(RSHIFT_ASSIGN, RSHIFT), Map.entry(UN_RSHIFT_ASSIGN, UN_RSHIFT)
        ));

        recognizableStatementStarters = Stream.of(IF, WHILE, FOR, DO, RETURN, BREAK, CONTINUE)
                .collect(Collectors.toSet());
    }

    /**
     * Evaluates whether the offered (1 char) string is a prefix to an operator comprised of repeating characters.
     * <br><br>Valid prefixes: {@code <, >, &, |, +, -}
     */
    public static boolean isRepeater(String s) {
        return repeaters.contains(s);
    }

    /**
     * Evaluates whether the offered string is a prefix to a compound assignment operator.
     * <br><br>Valid prefixes: {@code =, <, >, !, +, -, *, %, &, ^, |, <<, >>, >>>}
     */
    public static boolean isCompoundPrefix(String s) {
        return compoundPrefixes.contains(s);
    }

    /**
     * Evaluates whether the {@link TokenKind} instance is a comparison operator.
     * <br><br>Valid kinds: {@code ==, <, >, <=, >=, !=}
     */
    public boolean isComparisonOp() {
        return comparisonOps.contains(this);
    }

    /**
     * Evaluates whether the {@link TokenKind} instance is an assignment operator.
     * <br><br>Valid kinds: {@code =, +=, -=, *=, /=, %=, &=, ^=, |=, >>=, <<=, >>>=}
     */
    public boolean isAssignmentOp() {
        return assignmentOps.contains(this);
    }

    /**
     * Evaluates whether the {@link TokenKind} instance is an arithmetic operator.
     * <br><br>Valid kinds: {@code +, -, *, /, %, <<, >>, >>>, &, |}
     */
    public boolean isArithmeticOp() {
        return arithmeticOps.contains(this);
    }

    /**
     * Returns the base operator from a given compound assignment operator.
     * <br><br>For example, {@code + from +=, % from %=, >> from >>=, etc.}
     */
    public TokenKind getBaseFromCompound() {
        return compoundToBaseMap.get(this);
    }

    /**
     * Evaluates whether the {@link TokenKind} instance must be located at the beginning of a statement.
     * <br><br>Valid kinds: {@code for, do, while, if, break, continue, return}
     */
    public boolean isRecognizableStatementStarter() {
        return recognizableStatementStarters.contains(this);
    }

    public String print() {
       if (this == TokenKind.NUM || this == TokenKind.IDENTIFIER) {
           return String.format("<%s>", this.name().toLowerCase());
       }
       return spelling;
    }
}

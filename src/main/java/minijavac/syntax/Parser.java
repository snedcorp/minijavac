package minijavac.syntax;

import minijavac.listener.Listener;
import minijavac.ast.*;
import minijavac.err.CompileError;
import minijavac.syntax.err.ParseError;
import minijavac.syntax.err.ExpectedParseError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Recursive descent parser that performs syntactic analysis on scanned tokens to construct an {@link AST} of the
 * source file. If errors occur while parsing, they are logged to the given {@link Listener} implementation.
 */
public class Parser {

    private final Scanner scanner;
    private final Listener listener;
    private Token token;
    private Token prevToken;

    public Parser(Scanner scanner, Listener listener) {
        this.scanner = scanner;
        this.listener = listener;
    }

    /**
     * <pre>
     * Parses {@link Package} from source file.
     *
     * Unlike statement-level errors, which are caught and logged further down in the call stack, parse errors occurring
     * at the class, field, and method levels are caught and logged here, as well as EOF errors.
     *
     * Errors with the ignore flag set to true (i.e. EOF errors that occur while attempting to sync to next statement)
     * are not logged.
     * </pre>
     * @throws IOException file unable to be parsed
     */
    public List<ClassDecl> parse() throws IOException {
        try {
            token = scanner.scan();
            return parseFile();
        } catch (CompileError err) {
            if (!err.isIgnore()) listener.err(err);
        }
        return null;
    }

    private List<ClassDecl> parseFile() throws CompileError, IOException {
        List<ClassDecl> classDecls = new ArrayList<>();

        while (token.kind != TokenKind.EOF) {
            classDecls.add(parseClass());
        }
        accept();
        return classDecls;
    }

    private ClassDecl parseClass() throws CompileError, IOException {
        Position classPos = token.pos;
        accept(TokenKind.CLASS);
        Identifier className = new Identifier(token);
        accept(TokenKind.IDENTIFIER);
        accept(TokenKind.LCBRACKET);

        List<FieldDecl> fieldDeclList = new ArrayList<>();
        List<MethodDecl> methodDeclList = new ArrayList<>();

        while (token.kind != TokenKind.RCBRACKET) {
            Declaration decl = parseDeclaration(className.contents);
            if (decl instanceof FieldDecl) {
                fieldDeclList.add((FieldDecl) decl);
            } else {
                methodDeclList.add((MethodDecl) decl);
            }
        }
        accept();
        return new ClassDecl(className, fieldDeclList, methodDeclList, classPos);
    }

    private Declaration parseDeclaration(String className) throws CompileError, IOException {
        Position pos = token.pos;

        Set<TokenKind> modifiers = new HashSet<>();

        while (token.kind.isModifier()) {
            if (modifiers.contains(token.kind)) {
                listener.err(new CompileError(token.pos, "repeated modifier"));
            } else if ((token.kind == TokenKind.PRIVATE && modifiers.contains(TokenKind.PUBLIC)) ||
                            (token.kind == TokenKind.PUBLIC && modifiers.contains(TokenKind.PRIVATE))) {
                listener.err(new CompileError(token.pos, "illegal combination of modifiers: public and private"));
            } else {
                modifiers.add(token.kind);
            }
            accept();
        }

        Access access;
        if (modifiers.contains(TokenKind.PRIVATE)) access = Access.PRIVATE;
        else if (modifiers.contains(TokenKind.PUBLIC)) access = Access.PUBLIC;
        else access = Access.PACKAGE_PRIVATE;

        boolean isStatic = modifiers.contains(TokenKind.STATIC);
        boolean isFinal = modifiers.contains(TokenKind.FINAL);

        if (token.kind == TokenKind.VOID) {
            Type voidType = new BaseType(TypeKind.VOID, token.pos);
            accept();
            Identifier id = new Identifier(token);
            accept(TokenKind.IDENTIFIER);
            return parseMethod(access, isStatic, isFinal, voidType, id, pos);
        }

        Type type = parseType();

        if (token.kind == TokenKind.LPAREN &&
                type instanceof ClassType classType &&
                classType.className.contents.equals(className)) { // constructor
            return parseConstructor(access, isStatic, isFinal, classType, pos);
        }

        Identifier id = new Identifier(token);
        accept(TokenKind.IDENTIFIER);
        if (token.kind == TokenKind.SEMICOLON) {
            accept();
            return new FieldDecl(access, isStatic, isFinal, type, id, pos);
        }
        return parseMethod(access, isStatic, isFinal, type, id, pos);
    }

    private MethodDecl parseConstructor(Access access, boolean isStatic, boolean isFinal, ClassType type, Position pos)
            throws CompileError, IOException {
        if (isStatic || isFinal) {
            throw err(String.format("modifier %s%s%s not allowed here", isStatic ? "static" : "",
                    isStatic && isFinal ? "," : "", isFinal ? "final" : ""));
        }

        ParameterDeclList parameterDeclList = parseParameterList();
        List<Statement> statementList = parseStatementList();

        FieldDecl fieldDecl = new FieldDecl(access, false, false,
                new BaseType(TypeKind.VOID, null), type.className, pos);
        return new MethodDecl(fieldDecl, parameterDeclList, statementList, true, pos);
    }

    private MethodDecl parseMethod(Access access, boolean isStatic, boolean isFinal, Type type, Identifier id,
                                   Position pos)
            throws CompileError, IOException {
        ParameterDeclList parameterDeclList = parseParameterList();
        List<Statement> statementList = parseStatementList();

        return new MethodDecl(new FieldDecl(access, isStatic, isFinal, type, id, pos), parameterDeclList,
                statementList, false, pos);
    }

    private ParameterDeclList parseParameterList() throws CompileError, IOException {
        ParameterDeclList parameterDeclList = new ParameterDeclList();
        accept(TokenKind.LPAREN);

        while (token.kind != TokenKind.RPAREN) {
            Position paramPos = token.pos;
            boolean isFinal = false;
            if (token.kind == TokenKind.FINAL) {
                accept();
                isFinal = true;
            }
            Type paramType = parseType();
            Identifier paramId = new Identifier(token);
            accept(TokenKind.IDENTIFIER);
            parameterDeclList.add(new ParameterDecl(paramType, paramId, isFinal, paramPos));
            if (token.kind == TokenKind.COMMA) {
                accept();
                continue;
            }
            break;
        }

        accept(TokenKind.RPAREN);
        return parameterDeclList;
    }

    private List<Statement> parseStatementList() throws CompileError, IOException {
        accept(TokenKind.LCBRACKET);
        List<Statement> statementList = new ArrayList<>();
        while (token.kind != TokenKind.RCBRACKET) {
            Statement statement = parseStatement();
            if (statement != null) {
                statementList.add(statement);
            }
        }
        accept();
        return statementList;
    }

    /**
     * Parses statement.
     * If a {@link ParseError} occurs, logs it to the {@link Listener} and attempts to move to the next statement to
     * continue parsing.
     */
    private Statement parseStatement() throws CompileError, IOException {
        Position pos = token.pos;
        boolean ifOrWhile = token.kind == TokenKind.IF || token.kind == TokenKind.WHILE;

        try {
            switch (token.kind) {
                case LCBRACKET -> {
                    List<Statement> statementList = parseStatementList();
                    return new BlockStmt(statementList, pos);
                }
                case RETURN -> {
                    return parseReturnStatement();
                }
                case IF -> {
                    return parseIfStatement();
                }
                case WHILE -> {
                    return parseWhileStatement();
                }
                case DO -> {
                    return parseDoWhileStatement();
                }
                case FOR -> {
                    return parseForStatement();
                }
                case BREAK -> {
                    accept();
                    accept(TokenKind.SEMICOLON);
                    return new BreakStmt(pos);
                }
                case CONTINUE -> {
                    accept();
                    accept(TokenKind.SEMICOLON);
                    return new ContinueStmt(pos);
                }
                case IDENTIFIER -> {
                    return parseLeadingIdStatement();
                }
                case THIS -> {
                    Reference ref = parseReference();
                    if (token.kind == TokenKind.INCREMENT || token.kind == TokenKind.DECREMENT) {
                        return parsePostfixExprStatement(ref, pos);
                    }
                    if (token.kind == TokenKind.ASSIGN) {
                        return parseAssignStatement(ref);
                    }
                    if (ref instanceof CallRef) {
                        return parseCallStatement(ref);
                    }
                    throw err("not a statement");
                }
                case INT, BOOLEAN, FLOAT, FINAL -> {
                    return parseVarDeclStatement();
                }
                case INCREMENT, DECREMENT -> {
                    Expression expr = parseUnaryExpression();
                    accept(TokenKind.SEMICOLON);
                    return new ExprStatement(expr, pos);
                }
                default -> throw err("not a statement");
            }
        } catch (ParseError err) {
            listener.err(err);
            syncToNextStatement(ifOrWhile);
            return null;
        }
    }

    private IfStmt parseIfStatement() throws CompileError, IOException {
        Position pos = token.pos;
        accept();
        accept(TokenKind.LPAREN);
        Expression expr = parseExpression();
        accept(TokenKind.RPAREN);
        Statement ifStatement = parseStatement();
        if (token.kind == TokenKind.ELSE) {
            accept();
            Statement elseStatement = parseStatement();
            return new IfStmt(expr, ifStatement, elseStatement, pos);
        }
        return new IfStmt(expr, ifStatement, pos);
    }

    private WhileStmt parseWhileStatement() throws CompileError, IOException {
        Position pos = token.pos;
        accept();
        accept(TokenKind.LPAREN);
        Expression expr = parseExpression();
        accept(TokenKind.RPAREN);
        Statement statement = parseStatement();
        return new WhileStmt(expr, statement, pos);
    }

    private DoWhileStmt parseDoWhileStatement() throws CompileError, IOException {
        Position pos = token.pos;
        accept();
        Statement statement = parseStatement();
        accept(TokenKind.WHILE);
        accept(TokenKind.LPAREN);
        Expression expr = parseExpression();
        accept(TokenKind.RPAREN);
        accept(TokenKind.SEMICOLON);
        return new DoWhileStmt(expr, statement, pos);
    }

    private ForStmt parseForStatement() throws CompileError, IOException {
        Position pos = token.pos;
        accept();
        accept(TokenKind.LPAREN);

        VarDeclStmt initStmt = parseVarDeclStatement();
        Expression cond = parseExpression();
        accept(TokenKind.SEMICOLON);

        Position updatePos = token.pos;
        ExprStatement updateStmt = new ExprStatement(parseUnaryExpression(), updatePos);

        accept(TokenKind.RPAREN);
        Statement statement = parseStatement();
        return new ForStmt(initStmt, cond, updateStmt, statement, pos);
    }

    private ReturnStmt parseReturnStatement() throws CompileError, IOException {
        Position pos = token.pos;
        accept();
        Expression expr = null;
        if (token.kind != TokenKind.SEMICOLON) {
            expr = parseExpression();
        }
        accept(TokenKind.SEMICOLON);
        return new ReturnStmt(expr, pos);
    }

    private Statement parseLeadingIdStatement() throws CompileError, IOException {
        Position pos = token.pos;
        Identifier id = new Identifier(token);
        IdRef idRef = new IdRef(id, pos);
        accept();

        Reference ref;

        if (token.kind == TokenKind.IDENTIFIER) {
            ClassType type = new ClassType(id, pos);
            return parseVarDeclStatement(type, false);
        } else if (token.kind == TokenKind.LPAREN) {
            List<Expression> exprList = parseExpressionList();
            ref = parseReference(new CallRef(idRef, exprList, pos));
        } else if (token.kind == TokenKind.LBRACKET) {
            accept();
            if (token.kind == TokenKind.RBRACKET) {
                ArrayType type = parseArrayType(new ClassType(id, pos), pos);
                return parseVarDeclStatement(type, false);
            } else {
                List<Expression> ixExprList = parseIndexExpression();
                ref = parseReference(new IxRef(idRef, ixExprList, pos));
            }
        } else if (token.kind == TokenKind.PERIOD) {
            ref = parseQualifiedReference(idRef);
        } else if (token.kind == TokenKind.INCREMENT || token.kind == TokenKind.DECREMENT) {
            return parsePostfixExprStatement(idRef, pos);
        }
        else {
            return parseAssignStatement(idRef);
        }

        if (ref instanceof CallRef) {
            return parseCallStatement(ref);
        }

        if (token.kind == TokenKind.INCREMENT || token.kind == TokenKind.DECREMENT) {
            return parsePostfixExprStatement(ref, pos);
        }

        if (token.kind.isAssignmentOp()) {
            return parseAssignStatement(ref);
        }

        throw err("invalid statement");
    }

    private ExprStatement parsePostfixExprStatement(Reference ref, Position pos) throws CompileError, IOException {
        PostfixExpr postfixExpr = new PostfixExpr(new Operator(token), new RefExpr(ref, pos), pos);
        accept();
        accept(TokenKind.SEMICOLON);
        return new ExprStatement(postfixExpr, pos);
    }

    private VarDeclStmt parseVarDeclStatement() throws CompileError, IOException {
        boolean isFinal = false;
        if (token.kind == TokenKind.FINAL) {
            accept();
            isFinal = true;
        }
        Type type = parseType();
        return parseVarDeclStatement(type, isFinal);
    }

    private VarDeclStmt parseVarDeclStatement(Type type, boolean isFinal) throws CompileError, IOException {
        Identifier id = new Identifier(token);
        accept(TokenKind.IDENTIFIER);
        Expression expr = null;
        if (token.kind != TokenKind.SEMICOLON) {
            accept(TokenKind.ASSIGN);
            expr = parseExpression();
        }
        accept(TokenKind.SEMICOLON);
        return new VarDeclStmt(new VarDecl(id, type, isFinal, type.pos), expr, type.pos);
    }

    private AssignStmt parseAssignStatement(Reference ref) throws CompileError, IOException {
        if (!token.kind.isAssignmentOp()) throw err("not a statement");
        Operator op = new Operator(token);
        accept();
        Expression expr = parseExpression();
        accept(TokenKind.SEMICOLON);
        return new AssignStmt(ref, op, expr, ref.pos);
    }

    private CallStmt parseCallStatement(Reference ref) throws CompileError, IOException {
        accept(TokenKind.SEMICOLON);
        return new CallStmt(ref, ref.pos);
    }

    private List<Expression> parseExpressionList() throws CompileError, IOException {
        accept(TokenKind.LPAREN);
        List<Expression> exprList = new ArrayList<>();
        while (token.kind != TokenKind.RPAREN) {
            exprList.add(parseExpression());
            if (token.kind == TokenKind.COMMA) {
                accept();
                continue;
            }
            break;
        }
        accept(TokenKind.RPAREN);
        return exprList;
    }

    private List<Expression> parseIndexExpression() throws CompileError, IOException {
        List<Expression> ixExprList = new ArrayList<>();

        ixExprList.add(parseExpression());
        accept(TokenKind.RBRACKET);
        while (token.kind == TokenKind.LBRACKET) {
            accept();
            ixExprList.add(parseExpression());
            accept(TokenKind.RBRACKET);
        }
        return ixExprList;
    }

    private Reference parseReference() throws CompileError, IOException {
        Reference ref = parseSingleReference();
        return parseQualifiedReference(ref);
    }

    private Reference parseReference(Reference ref) throws CompileError, IOException {
        return parseQualifiedReference(parseIndexedOrCallReference(ref));
    }

    private Reference parseSingleReference() throws CompileError, IOException {
        Position pos = token.pos;
        if (token.kind == TokenKind.IDENTIFIER) {
           Reference ref = new IdRef(new Identifier(token), pos);
           accept();
           return parseIndexedOrCallReference(ref);
        }

        accept(TokenKind.THIS);
        if (token.kind == TokenKind.LPAREN) {
            List<Expression> exprList = parseExpressionList();
            return new CallRef(new ThisRef(pos), exprList, pos);
        }
        return new ThisRef(pos);
    }

    private Reference parseIndexedOrCallReference(Reference ref) throws CompileError, IOException {
        while (token.kind == TokenKind.LBRACKET || token.kind == TokenKind.LPAREN) {
            Position pos = token.pos;
            if (token.kind == TokenKind.LBRACKET) {
                accept();
                List<Expression> ixExprList = parseIndexExpression();
                ref = new IxRef(ref, ixExprList, pos);
            } else {
                // identifier or "this" must immediately precede method call, no concept of callables
                if (ref instanceof IxRef || ref instanceof CallRef) {
                    break;
                }
                List<Expression> exprList = parseExpressionList();
                ref = new CallRef(ref, exprList, pos);
            }
        }
        return ref;
    }

    private Reference parseQualifiedReference(Reference ref) throws CompileError, IOException {
        while (token.kind == TokenKind.PERIOD) {
            Position pos = token.pos;
            accept();
            Identifier id = new Identifier(token);
            ref = new QualRef(ref, id, pos);
            accept(TokenKind.IDENTIFIER);
            ref = parseIndexedOrCallReference(ref);
        }
        return ref;
    }

    private Expression parseExpression() throws CompileError, IOException {
        return parseTernaryExpression();
    }

    private Expression parseTernaryExpression() throws CompileError, IOException {
        Expression expr = parseOrExpression();
        if (token.kind == TokenKind.QUESTION) {
            Position pos = token.pos;
            accept();
            Expression expr1 = parseTernaryExpression();
            accept(TokenKind.COLON);
            Expression expr2 = parseTernaryExpression();
            expr = new TernaryExpr(expr, expr1, expr2, pos);
        }
        return expr;
    }

    private Expression parseOrExpression() throws CompileError, IOException {
        Expression expr = parseAndExpression();
        while (token.kind == TokenKind.OR) {
            Position pos = token.pos;
            Operator or = new Operator(token);
            accept();
            Expression expr2 = parseAndExpression();
            expr = new BinaryExpr(or, expr, expr2, pos);
        }
        return expr;
    }

    private Expression parseAndExpression() throws CompileError, IOException {
        Expression expr = parseBitwiseInclusiveOrExpression();
        while (token.kind == TokenKind.AND) {
            Position pos = token.pos;
            Operator and = new Operator(token);
            accept();
            Expression expr2 = parseBitwiseInclusiveOrExpression();
            expr = new BinaryExpr(and, expr, expr2, pos);
        }
        return expr;
    }

    private Expression parseBitwiseInclusiveOrExpression() throws CompileError, IOException {
        Expression expr = parseBitwiseExclusiveOrExpression();
        while (token.kind == TokenKind.BTW_INC_OR) {
            Position pos = token.pos;
            Operator and = new Operator(token);
            accept();
            Expression expr2 = parseBitwiseExclusiveOrExpression();
            expr = new BinaryExpr(and, expr, expr2, pos);
        }
        return expr;
    }

    private Expression parseBitwiseExclusiveOrExpression() throws CompileError, IOException {
        Expression expr = parseBitwiseAndExpression();
        while (token.kind == TokenKind.BTW_EXC_OR) {
            Position pos = token.pos;
            Operator and = new Operator(token);
            accept();
            Expression expr2 = parseBitwiseAndExpression();
            expr = new BinaryExpr(and, expr, expr2, pos);
        }
        return expr;
    }

    private Expression parseBitwiseAndExpression() throws CompileError, IOException {
        Expression expr = parseEqualityExpression();
        while (token.kind == TokenKind.BTW_AND) {
            Position pos = token.pos;
            Operator and = new Operator(token);
            accept();
            Expression expr2 = parseEqualityExpression();
            expr = new BinaryExpr(and, expr, expr2, pos);
        }
        return expr;
    }

    private Expression parseEqualityExpression() throws CompileError, IOException {
        Expression expr = parseRelationalExpression();
        while (token.kind == TokenKind.EQ || token.kind == TokenKind.NOT_EQ) {
            Position pos = token.pos;
            Operator eq = new Operator(token);
            accept();
            Expression expr2 = parseRelationalExpression();
            expr = new BinaryExpr(eq, expr, expr2, pos);
        }
        return expr;
    }

    private Expression parseRelationalExpression() throws CompileError, IOException {
        Expression expr = parseShiftExpression();
        while (token.kind == TokenKind.LT || token.kind == TokenKind.GT || token.kind == TokenKind.LTE || token.kind == TokenKind.GTE) {
            Position pos = token.pos;
            Operator comp = new Operator(token);
            accept();
            Expression expr2 = parseShiftExpression();
            expr = new BinaryExpr(comp, expr, expr2, pos);
        }
        return expr;
    }

    private Expression parseShiftExpression() throws CompileError, IOException {
        Expression expr = parseAdditiveExpression();
        while (token.kind == TokenKind.LSHIFT || token.kind == TokenKind.RSHIFT || token.kind == TokenKind.UN_RSHIFT) {
            Position pos = token.pos;
            Operator comp = new Operator(token);
            accept();
            Expression expr2 = parseAdditiveExpression();
            expr = new BinaryExpr(comp, expr, expr2, pos);
        }
        return expr;
    }

    private Expression parseAdditiveExpression() throws CompileError, IOException {
        Expression expr = parseMultiplicativeExpression();
        while (token.kind == TokenKind.PLUS || token.kind == TokenKind.MINUS) {
            Position pos = token.pos;
            Operator add = new Operator(token);
            accept();
            Expression expr2 = parseMultiplicativeExpression();
            expr = new BinaryExpr(add, expr, expr2, pos);
        }
        return expr;
    }

    private Expression parseMultiplicativeExpression() throws CompileError, IOException {
        Expression expr = parseUnaryExpression();
        while (token.kind == TokenKind.MULTIPLY || token.kind == TokenKind.DIVIDE || token.kind == TokenKind.MODULO) {
            Position pos = token.pos;
            Operator op = new Operator(token);
            accept();
            Expression expr2 = parseUnaryExpression();
            expr = new BinaryExpr(op, expr, expr2, pos);
        }
        return expr;
    }

    private Expression parseUnaryExpression() throws CompileError, IOException {
        if (token.kind == TokenKind.MINUS || token.kind == TokenKind.NOT || token.kind == TokenKind.COMPLEMENT ||
                token.kind == TokenKind.INCREMENT || token.kind == TokenKind.DECREMENT) {
            Position pos = token.pos;
            Operator unary = new Operator(token);
            accept();
            Expression expr = parseUnaryExpression();
            return new UnaryExpr(unary, expr, pos);
        }
        return parsePostfixExpression();
    }

    private Expression parsePostfixExpression() throws CompileError, IOException {
        Expression expr = parseSingleExpression();
        if (token.kind == TokenKind.INCREMENT || token.kind == TokenKind.DECREMENT) {
            Position pos = token.pos;
            Operator postfix = new Operator(token);
            accept();
            return new PostfixExpr(postfix, expr, pos);
        }
        return expr;
    }

    private Expression parseSingleExpression() throws CompileError, IOException {
        Position pos = token.pos;
        switch (token.kind) {
            case LPAREN -> {
                accept();
                Expression expr = parseExpression();
                accept(TokenKind.RPAREN);
                return expr;
            }
            case NUM -> {
                IntLiteral intLiteral = new IntLiteral(token);
                accept();
                return new LiteralExpr(intLiteral);
            }
            case FLOAT_NUM -> {
                FloatLiteral floatLiteral = new FloatLiteral(token);
                accept();
                return new LiteralExpr(floatLiteral);
            }
            case TRUE, FALSE -> {
                BooleanLiteral booleanLiteral = new BooleanLiteral(token);
                accept();
                return new LiteralExpr(booleanLiteral);
            }
            case NULL -> {
                NullLiteral nullLiteral = new NullLiteral(token);
                accept();
                return new LiteralExpr(nullLiteral);
            }
            case NEW -> {
                accept();
                Type type = parseNonArrayType();
                if (token.kind == TokenKind.LBRACKET) {
                    accept();
                    if (token.kind != TokenKind.RBRACKET) {
                        List<Expression> ixExprList = parseIndexExpression();
                        return new NewArrayExpr(type, ixExprList, pos);
                    }
                    return parseNewArrayInitExpr(type, pos);
                }

                if (!(type instanceof ClassType)) {
                    throw err("New keyword on non-class type");
                }
                List<Expression> exprList = parseExpressionList();
                return new NewObjectExpr((ClassType) type, exprList, pos);
            }
            case IDENTIFIER, THIS -> {
                Reference ref = parseReference();
                return new RefExpr(ref, pos);
            }
            default -> throw err("illegal start of expression");
        }
    }

    private NewArrayInitExpr parseNewArrayInitExpr(Type eltType, Position pos) throws CompileError, IOException {
        accept(TokenKind.RBRACKET);
        int dims = 1;
        while (token.kind == TokenKind.LBRACKET) {
            accept();
            accept(TokenKind.RBRACKET);
            dims++;
        }
        ArrayInitExpr initExpr = parseArrayInitExpr();
        return new NewArrayInitExpr(eltType, dims, initExpr, pos);
    }

    private ArrayInitExpr parseArrayInitExpr() throws CompileError, IOException {
        Position pos = token.pos;
        accept(TokenKind.LCBRACKET);
        List<Expression> exprList = new ArrayList<>();

        while (token.kind != TokenKind.RCBRACKET) {
            Expression expr = token.kind == TokenKind.LCBRACKET ? parseArrayInitExpr() : parseExpression();
            exprList.add(expr);
            if (token.kind == TokenKind.COMMA) {
                accept();
                continue;
            }
            break;
        }
        accept(TokenKind.RCBRACKET);

        return new ArrayInitExpr(exprList, pos);
    }


    private Type parseType() throws CompileError, IOException {
        return parseType(true);
    }

    private Type parseNonArrayType() throws CompileError, IOException {
        return parseType(false);
    }

    private Type parseType(boolean arrayAllowed) throws CompileError, IOException {
        Type type;
        Position pos = token.pos;
        switch (token.kind) {
            case INT -> {
                accept();
                type = new BaseType(TypeKind.INT, pos);
            }
            case FLOAT -> {
                accept();
                type = new BaseType(TypeKind.FLOAT, pos);
            }
            case BOOLEAN -> {
                accept();
                type = new BaseType(TypeKind.BOOLEAN, pos);
            }
            case IDENTIFIER -> {
                Identifier id = new Identifier(token);
                accept();
                type = new ClassType(id, pos);
            }
            default -> {
                if (token.kind == TokenKind.VOID) {
                    throw err("'void' type not allowed here");
                }
                throw err("illegal start of type");
            }
        }

        if (arrayAllowed && token.kind == TokenKind.LBRACKET) {
            accept();
            return parseArrayType(type, pos);
        }
        return type;
    }

    private ArrayType parseArrayType(Type type, Position pos) throws CompileError, IOException {
        accept(TokenKind.RBRACKET);
        int dims = 1;
        while (token.kind == TokenKind.LBRACKET) {
            accept();
            accept(TokenKind.RBRACKET);
            dims++;
        }

        return new ArrayType(type, pos, dims);
    }

    /**
     * Throws error if end of file has been reached, otherwise returns true.
     * @return whether file has additional contents
     * @throws CompileError EOF error
     * @throws IOException  file unable to be parsed
     */
    private boolean checkEOF() throws CompileError, IOException {
        if (token.kind == TokenKind.EOF) {
            throw new CompileError(prevToken.endPos(), "reached end of file while parsing");
        }
        return true;
    }

    /**
     * @return {@link ParseError} containing current token and error message.
     * @throws CompileError EOF error
     * @throws IOException  file unable to be parsed
     */
    private ParseError err(String msg) throws CompileError, IOException {
        checkEOF();
        return new ParseError(token, msg);
    }

    /**
     * Accepts current token if it's the specified {@link TokenKind}, otherwise throws an error.
     * @param kind acceptable {@link TokenKind} at the current position.
     * @throws CompileError if EOF, otherwise {@link ExpectedParseError}
     * @throws IOException  file unable to be parsed
     */
    private void accept(TokenKind kind) throws CompileError, IOException {
        if (token.kind != kind) {
            checkEOF();
            throw prevToken != null ? new ExpectedParseError(prevToken, token, kind) : new ExpectedParseError(token, kind);
        }
        accept();
    }

    /**
     * Accepts current token and advances to the next one.
     * @throws IOException file unable to be parsed
     */
    private void accept() throws IOException {
        prevToken = token;
        token = scanner.scan();
    }

    /**
     * Attempts to move past the current, erroneous statement to the next one.
     * <br>If EOF error occurs, throw it to unwind the stack but set the ignore flag, so only the original error is logged.
     * @param ifOrWhile true if statement is a recognizable attempt at a loop
     * @throws CompileError EOF reached before next statement is found
     * @throws IOException  file unable to be parsed
     */
    private void syncToNextStatement(boolean ifOrWhile) throws CompileError, IOException {
        try {
            while (checkEOF()) {
                if ((ifOrWhile && token.kind == TokenKind.RCBRACKET) || (!ifOrWhile && token.kind == TokenKind.SEMICOLON)) {
                    accept();
                    return;
                }
                if (token.kind.isRecognizableStatementStarter()) return;
                accept();
            }
        } catch (CompileError err) {
            err.setIgnore(true);
            throw err;
        }
    }
}

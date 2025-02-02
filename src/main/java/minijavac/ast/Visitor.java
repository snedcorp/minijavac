package minijavac.ast;

import minijavac.utils.TraversalState;

/**
 * Defines a generic contract for AST traversal - implementations must provide a method {@code visit<node>} for each
 * concrete AST class {@code <node>}.
 * <br><br>
 * Generic declaration allows for each implementation to define their own traversal state, argument, and return types.
 */
public interface Visitor<S extends TraversalState,A,R> {

    // Declarations
    R visitClassDecl(ClassDecl classDecl, S state, A arg);
    R visitFieldDecl(FieldDecl fieldDecl, S state, A arg);
    R visitMethodDecl(MethodDecl methodDecl, S state, A arg);
    R visitParameterDecl(ParameterDecl parameterDecl, S state, A arg);
    R visitVarDecl(VarDecl decl, S state, A arg);
 
    // Types
    R visitBaseType(BaseType type, S state, A arg);
    R visitClassType(ClassType type, S state, A arg);
    R visitArrayType(ArrayType type, S state, A arg);
    
    // Statements
    R visitBlockStmt(BlockStmt stmt, S state, A arg);
    R visitVarDeclStmt(VarDeclStmt stmt, S state, A arg);
    R visitAssignStmt(AssignStmt stmt, S state, A arg);
    R visitCallStmt(CallStmt stmt, S state, A arg);
    R visitReturnStmt(ReturnStmt stmt, S state, A arg);
    R visitIfStmt(IfStmt stmt, S state, A arg);
    R visitWhileStmt(WhileStmt stmt, S state, A arg);
    R visitDoWhileStmt(DoWhileStmt stmt, S state, A arg);
    R visitExprStmt(ExprStatement stmt, S state, A arg);
    R visitBreakStmt(BreakStmt stmt, S state, A arg);
    R visitContinueStmt(ContinueStmt stmt, S state, A arg);
    R visitForStmt(ForStmt stmt, S state, A arg);
    
    // Expressions
    R visitUnaryExpr(UnaryExpr expr, S state, A arg);
    R visitBinaryExpr(BinaryExpr expr, S state, A arg);
    R visitRefExpr(RefExpr expr, S state, A arg);
    R visitLiteralExpr(LiteralExpr expr, S state, A arg);
    R visitNewObjectExpr(NewObjectExpr expr, S state, A arg);
    R visitNewArrayExpr(NewArrayExpr expr, S state, A arg);
    R visitPostfixExpr(PostfixExpr expr, S state, A arg);
    R visitTernaryExpr(TernaryExpr expr, S state, A arg);
    R visitNewArrayInitExpr(NewArrayInitExpr expr, S state, A arg);
    R visitArrayInitExpr(ArrayInitExpr expr, S state, A arg);
    
    // References
    R visitThisRef(ThisRef ref, S state, A arg);
    R visitIdRef(IdRef ref, S state, A arg);
    R visitIxRef(IxRef ref, S state, A arg);
    R visitCallRef(CallRef ref, S state, A arg);
    R visitQualRef(QualRef ref, S state, A arg);

    // Terminals
    R visitIdentifier(Identifier id, S state, A arg);
    R visitOperator(Operator op, S state, A arg);
    R visitIntLiteral(IntLiteral num, S state, A arg);
    R visitBooleanLiteral(BooleanLiteral bool, S state, A arg);
    R visitNullLiteral(NullLiteral nul, S state, A arg);
    R visitFloatLiteral(FloatLiteral num, S state, A arg);
}

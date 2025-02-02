package minijavac.ast;

import minijavac.syntax.Position;

/**
 * Abstract base class for all expressions.
 * <br>
 * @see minijavac.ast.ArrayInitExpr
 * @see minijavac.ast.BinaryExpr
 * @see minijavac.ast.LiteralExpr
 * @see minijavac.ast.NewArrayExpr
 * @see minijavac.ast.NewArrayInitExpr
 * @see minijavac.ast.NewObjectExpr
 * @see minijavac.ast.PostfixExpr
 * @see minijavac.ast.RefExpr
 * @see minijavac.ast.TernaryExpr
 * @see minijavac.ast.UnaryExpr
 */
public abstract class Expression extends AST {

  public Expression(Position pos) {
    super (pos);
  }
  
}

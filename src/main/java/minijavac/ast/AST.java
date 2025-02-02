package minijavac.ast;

import minijavac.syntax.Parser;
import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * Abstract base class, containing just the {@link Position}, for every node of the Abstract Syntax Tree constructed by
 * the {@link Parser}.
 */
public abstract class AST {

  public Position pos;

  public AST (Position pos) {
    this.pos = pos;
  }

  public abstract <S extends TraversalState,A,R> R visit(Visitor<S,A,R> v, S s, A a);
}

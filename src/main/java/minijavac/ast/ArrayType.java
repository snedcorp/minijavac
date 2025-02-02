package minijavac.ast;

import minijavac.syntax.Position;
import minijavac.utils.TraversalState;

/**
 * <pre>
 * {@link Type} variant corresponding to an array of arbitrary depth.
 *
 * Examples:
 *  - {@code int[]}
 *  - {@code boolean[][]}
 *  - {@code float[][][]}
 *  - {@code Test[]}
 * </pre>
 */
public class ArrayType extends Type {

	public Type elementType;
	public int dims;

	public ArrayType(Type elementType, Position pos, int dims) {
		super(TypeKind.ARRAY, pos);
		this.elementType = elementType;
		this.dims = dims;
	}

	/**
	 * {@inheritDoc}
	 * <pre>
	 * Examples:
     * 	- {@code "int[]"}
	 * 	- {@code "bool[][]"}
	 * 	- {@code "float[][][]"}
	 * 	- {@code "Test[]"}
	 * </pre>
	 */
	@Override
	public String print() {
		return printToDepth(dims);
	}

	/**
	 * Generates printable representation to a specified depth.
	 * @see ArrayType#print()
	 */
	public String printToDepth(int depth) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<depth; i++) {
			sb.append("[]");
		}
		return String.format("%s%s", elementType.print(), sb);
	}

	/**
	 * {@inheritDoc}
	 * <pre>
	 * Examples:
	 *  - {@code "[I"}
	 *  - {@code "[[Z"}
	 *  - {@code "[[[F"}
	 *  - {@code "[[LTest;"}
	 * </pre>
	 * @return
	 */
	@Override
	public String descriptor() {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<dims; i++) {
			sb.append("[");
		}
		return String.format("%s%s", sb, elementType.descriptor());
	}

	@Override
	public <S extends TraversalState, A, R> R visit(Visitor<S, A, R> v, S s, A a) {
		return v.visitArrayType(this, s, a);
	}
}


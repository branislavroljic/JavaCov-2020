package util;

import java.util.function.BinaryOperator;

public class NumBuffer<T extends Number> extends GenericBuffer<T>{

	private static final long serialVersionUID = 1L;

	public NumBuffer(int size) {
		super(size);
	}
	
	/*
	 * sumiranje svih elemenata u baferu
	 */
	public T sum(BinaryOperator<T> sumOp) {
		return getElements().stream().reduce(sumOp).orElse(null);	
	}
	
}

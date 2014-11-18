package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.mathematics.expression.BiFunctionExpression;
import uk.co.strangeskies.mathematics.expression.Expression;

public class NAND<O extends NANDable<?, ? super T>, T>
		extends
		BiFunctionExpression</* @ReadOnly */NANDable<? extends O, ? super T>, /*
																																								 * @
																																								 * ReadOnly
																																								 */T, /*
																																											 * @
																																											 * ReadOnly
																																											 */O> {
	public NAND(
			Expression<? extends /* @ReadOnly */NANDable<? extends O, ? super T>> firstOperand,
			Expression<? extends /* @ReadOnly */T> secondOperand) {
		super(firstOperand, secondOperand, new NANDOperation<O, T>());
	}
}
/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.expressions.
 *
 * uk.co.strangeskies.expressions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.expressions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.expression;

import java.util.function.Function;

/**
 * An implementation of {@link Expression} with a single data dependency, whose
 * value is derived through the application of a function to the value of that
 * dependency. This function may also be provided through an {@link Expression}
 * dependency.
 * 
 * @author Elias N Vasylenko
 * @param <O>
 *          The type of the operand.
 * @param <R>
 *          The type of the result.
 */
public abstract class UnaryExpression<S extends UnaryExpression<S, O, R>, O, R> extends DependentExpression<S, R> {
	private Expression<?, ? extends O> operand;
	private final Expression<?, ? extends Function<? super O, ? extends R>> operation;

	/**
	 * @param operand
	 *          An expression providing an operand for the function.
	 * @param operation
	 *          A expression providing a function transforming an operand into a
	 *          value of this expression's type.
	 */
	public UnaryExpression(Expression<?, ? extends O> operand,
			Expression<?, ? extends Function<? super O, ? extends R>> operation) {
		super(operand, operation);

		this.operand = operand;

		this.operation = operation;
	}

	/**
	 * @param operand
	 *          An expression providing an operand for the function.
	 * @param operation
	 *          A function transforming an operand into a value of this
	 *          expression's type.
	 */
	public UnaryExpression(Expression<?, ? extends O> operand, Function<? super O, ? extends R> operation) {
		super(operand);

		this.operand = operand;

		this.operation = Expression.immutable(operation);
	}

	/**
	 * @return The operand expression.
	 */
	public Expression<?, ? extends O> getOperand() {
		return operand;
	}

	public Expression<?, ? extends Function<? super O, ? extends R>> getOperation() {
		return operation;
	}

	/**
	 * @param operand
	 *          A new operand.
	 */
	public void setOperand(Expression<?, ? extends O> operand) {
		try {
			beginWrite();

			if (this.operand != operand) {
				getDependencies().remove(this.operand);

				this.operand = operand;
				getDependencies().add(this.operand);
			}
		} finally {
			endWrite();
		}
	}

	@Override
	protected R evaluate() {
		return operation.getValue().apply(operand.getValue());
	}
}
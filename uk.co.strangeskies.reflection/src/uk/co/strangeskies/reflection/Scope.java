/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

/**
 * Represents a static, compile-time scope for evaluation of {@link ExpressionDefinition
 * java expressions}. A scope defines which local variables are available, as
 * well as the enclosing type of the expression (i.e. the type of the "this"
 * reference).
 * <p>
 * Available local variables may conceptually include method parameters of any
 * enclosing methods or lambdas.
 * 
 * @author Elias N Vasylenko
 *
 * @param <I>
 *          the type of the enclosing instance
 */
public class Scope<I> {
	private final TypeToken<I> receiverType;
	private final ValueExpressionDefinition<I, I> receiverExpression;

	public Scope(TypeToken<I> receiverType) {
		this.receiverType = receiverType;
		this.receiverExpression = new ValueExpressionDefinition<I, I>() {
			@Override
			public ValueResult<I> evaluate(State state) {
				return () -> state.getEnclosingInstance(Scope.this);
			}

			@Override
			public TypeToken<I> getType() {
				return getReceiverType();
			}
		};
	}

	public TypeToken<I> getReceiverType() {
		return receiverType;
	}

	public ValueExpressionDefinition<I, I> receiver() {
		return receiverExpression;
	}
}
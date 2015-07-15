/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.expressions.
 *
 * uk.co.strangeskies.expressions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.expressions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.expressions.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.expression.buffer;

import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.utilities.Observer;

public class ExpressionBuffer<F extends Expression<?>, T> extends
		FunctionBuffer<F, T> {
	private Observer<Expression<?>> backObserver;

	public ExpressionBuffer(T front, F back,
			BiFunction<? super T, ? super F, ? extends T> operation) {
		super(front, back, operation);
	}

	public ExpressionBuffer(F back, Function<? super F, ? extends T> function) {
		super(back, function);
	}

	public ExpressionBuffer(T front, F back,
			Function<? super F, ? extends T> function) {
		super(front, back, function);
	}

	public ExpressionBuffer(FunctionBuffer<F, T> doubleBuffer) {
		super(doubleBuffer);
	}

	@Override
	public F setBack(F next) {
		if (getBack() != null) {
			getBack().removeObserver(getBackObserver());
		}

		if (next != null) {
			next.addObserver(nextBackObserver());
		}

		return super.setBack(next);
	}

	private Observer<Expression<?>> nextBackObserver() {
		return backObserver = m -> invalidateBack();
	}

	private Observer<Expression<?>> getBackObserver() {
		return backObserver;
	}
}
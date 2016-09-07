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

public class AssignmentExpressionDefinition<T, I> implements ValueExpressionDefinition<T, I> {
	private final VariableExpressionDefinition<T, ? super I> target;
	private final ValueExpressionDefinition<? extends T, ? super I> value;

	protected AssignmentExpressionDefinition(VariableExpressionDefinition<T, ? super I> target,
			ValueExpressionDefinition<? extends T, ? super I> value) {
		this.target = target;
		this.value = value;
	}

	@Override
	public ValueResult<T> evaluate(State state) {
		VariableResult<T> targetResult = target.evaluate(state);

		T result = value.evaluate(state).get();

		targetResult.set(result);

		return () -> result;
	}

	@Override
	public TypeToken<T> getType() {
		return target.getType();
	}

	public static <T, I> ValueExpressionDefinition<T, I> assign(
			VariableExpressionDefinition<T, ? super I> target,
			ValueExpressionDefinition<? extends T, ? super I> value) {
		return new AssignmentExpressionDefinition<>(target, value);
	}
}
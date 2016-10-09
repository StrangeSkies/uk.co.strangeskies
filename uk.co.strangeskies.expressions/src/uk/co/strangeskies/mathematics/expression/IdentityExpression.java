/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,L__   __||  _ `.        / \     |  \   | |  ,-`__`]  ,-`__`]
 *   ( (_`-`   | |   | | ) |       / . \    | . \  | | / .`  `  / .`  `
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `-`.   / /   \ \  | | \ \| || |   | || +--J
 *  \ \__.` /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,-
 *   `.__.-`   L_|   L_|    L_|/_/       \_\L_|   \__|  `-.__.'  `-.__.]
 *                   __    _         _      __      __
 *                 ,`_ `, | |   _   | |  ,-`__`]  ,`_ `,
 *                ( (_`-` | '-.) |  | | / .`  `  ( (_`-`
 *                 `._ `. | +-. <   | || '--.     `._ `.
 *                _   `. \| |  `-`. | || +--J    _   `. \
 *               \ \__.` /| |    \ \| | \ `.__,-\ \__.` /
 *                `.__.-` L_|    L_|L_|  `-.__.] `.__.-`
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

import java.util.Observer;

import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;

/**
 * An {@link Expression} based on the behaviour of the {@link IdentityProperty}
 * class, with the lazy updating behaviour of {@link LockingExpression} for
 * {@link Observer}s.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          The type of the expression.
 */
public class IdentityExpression<T> extends ExpressionImpl<IdentityExpression<T>, T> implements Property<T, T> {
	private T value;

	/**
	 * Construct with a default value of {@code null}.
	 */
	public IdentityExpression() {}

	/**
	 * Construct with the given default value.
	 * 
	 * @param value
	 *          The initial value of the expression.
	 */
	public IdentityExpression(T value) {
		this.value = value;
	}

	@Override
	public T set(T value) {
		beginWrite();

		try {
			T previous = this.value;
			this.value = value;
			return previous;
		} finally {
			endWrite();
		}
	}

	@Override
	protected final T getValueImpl(boolean dirty) {
		return value;
	}

	@Override
	public final T get() {
		return getValue();
	}

	@Override
	public IdentityExpression<T> copy() {
		return this;
	}
}

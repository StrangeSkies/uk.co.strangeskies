/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.tuples;

/**
 * A two tuple.
 * 
 * @author Elias N Vasylenko
 *
 * @param <L>
 *          The type of the first, left, item.
 * @param <R>
 *          The type of the second, right, item.
 */
public class Pair<L, R> extends Tuple<L, Unit<R>> {
	/**
	 * Initialise a pair with the given two values.
	 * 
	 * @param left
	 *          The first, left, item.
	 * @param right
	 *          The second, right, item.
	 */
	public Pair(L left, R right) {
		super(left, new Unit<>(right));
	}

	/**
	 * @return The head value.
	 */
	public L get0() {
		return getHead();
	}

	/**
	 * @return The head value.
	 */
	public L getLeft() {
		return getHead();
	}

	/**
	 * @return The tail value.
	 */
	public R get1() {
		return getTail().getHead();
	}

	/**
	 * @return The tail value.
	 */
	public R getRight() {
		return getTail().getHead();
	}
}
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
package uk.co.strangeskies.utilities.parser;

import java.util.function.BiFunction;

import uk.co.strangeskies.utilities.tuples.Pair;

public class ParserJoin<T, U, V> extends AbstractParser<T> {
	private final Parser<U> first;
	private final Parser<V> second;
	private final BiFunction<U, V, T> combinor;

	public ParserJoin(Parser<U> first, Parser<V> second,
			BiFunction<U, V, T> combinor) {
		this.first = first;
		this.second = second;
		this.combinor = combinor;
	}

	@Override
	public Pair<T, Integer> parseSubstring(String literal) {
		Pair<U, Integer> firstValue = first.parseSubstring(literal);
		Pair<V, Integer> secondValue = second.parseSubstring(literal
				.substring(firstValue.getRight()));
		return new Pair<>(combinor.apply(firstValue.getLeft(),
				secondValue.getLeft()), firstValue.getRight() + secondValue.getRight());
	}

	@Override
	public String toString() {
		return "Joining Parser (" + first + " & " + second + ")";
	}
}

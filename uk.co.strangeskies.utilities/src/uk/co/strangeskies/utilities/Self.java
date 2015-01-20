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
package uk.co.strangeskies.utilities;

/**
 * For classes which follow the self-bounding pattern. The self-bounding pattern
 * is the use of the final derived class as a type parameter.
 *
 * @author Elias N Vasylenko
 *
 * @param <S>
 *          The final implementing class.
 */
public interface Self<S extends Self<S>> extends Copyable<S> {
	@SuppressWarnings("unchecked")
	public default S getThis(Self<S> this) {
		return (S) this;
	}
}

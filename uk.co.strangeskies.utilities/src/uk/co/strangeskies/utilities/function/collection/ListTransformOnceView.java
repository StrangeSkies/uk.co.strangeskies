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
package uk.co.strangeskies.utilities.function.collection;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import uk.co.strangeskies.utilities.IdentityComparator;

/**
 * A view of a list which will be automatically updated along with the original,
 * but who's elements will be a transformation of the original associated
 * elements by way of the function passed to the constructor. The implementation
 * employs lazy evaluation, so try to use get() as little as possible by reusing
 * the result.
 * 
 * @author Elias N Vasylenko
 * 
 * @param <T>
 *          The type of the elements of this list.
 * @param <F>
 *          The type of the elements of the backing list.
 */
public class ListTransformOnceView<F, T> extends AbstractList<T> {
	private final List<? extends F> backingList;
	private final Function<? super F, ? extends T> function;

	private final Map<F, T> transformations;

	public ListTransformOnceView(List<? extends F> backingList,
			final Function<? super F, ? extends T> function) {
		transformations = new TreeMap<>(new IdentityComparator<>());

		this.backingList = backingList;
		this.function = function;
	}

	@Override
	public final T get(int index) {
		F backingElement = backingList.get(index);
		T transformation = transformations.get(backingElement);
		if (transformation == null) {
			transformation = function.apply(backingElement);
			transformations.put(backingElement, transformation);

			if (transformations.keySet().size() > backingList.size() * 1.5) {
				transformations.keySet().retainAll(backingList);
			}
		}
		return transformation;
	}

	public final List<? extends F> getBackingList() {
		return backingList;
	}

	public final Function<? super F, ? extends T> getFunction() {
		return function;
	}

	@Override
	public final int size() {
		return backingList.size();
	}
}

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
package uk.co.strangeskies.utilities.collection.decorator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.utilities.Property;

public class FilteredListDecorator<E> extends ListDecorator<E> {
	public interface Filter<E> {
		public boolean filter(E element);
	}

	private final Filter<E> filter;

	public FilteredListDecorator(Filter<E> filter) {
		super(new ArrayList<E>());

		this.filter = filter;
	}

	public FilteredListDecorator(List<E> component, Filter<E> filter) {
		super(component);

		this.filter = filter;
	}

	public FilteredListDecorator(Property<List<E>, ? super List<E>> component,
			Filter<E> filter) {
		super(component);

		this.filter = filter;
	}

	@Override
	public boolean add(E e) {
		return filter.filter(e) && super.add(e);
	}

	@Override
	public void add(int index, E element) {
		if (filter.filter(element))
			super.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean changed = false;

		for (E e : c)
			changed = add(e) || changed;

		return changed;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		for (E e : c)
			add(index++, e);

		return true;
	}
}

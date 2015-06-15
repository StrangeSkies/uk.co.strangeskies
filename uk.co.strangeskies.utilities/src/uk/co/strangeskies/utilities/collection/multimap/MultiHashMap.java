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
package uk.co.strangeskies.utilities.collection.multimap;

import java.util.Collection;
import java.util.HashMap;

import uk.co.strangeskies.utilities.factory.Factory;

public class MultiHashMap<K, V, C extends Collection<V>> extends HashMap<K, C>
		implements MultiMap<K, V, C> {
	private static final long serialVersionUID = 1L;

	private final Factory<C> collectionFactory;

	public MultiHashMap(Factory<C> collectionFactory) {
		this.collectionFactory = collectionFactory;
	}

	public MultiHashMap(Factory<C> collectionFactory,
			MultiMap<? extends K, ? extends V, ? extends C> that) {
		this(collectionFactory);

		addAll(that);
	}

	@Override
	public C createCollection() {
		return collectionFactory.create();
	}
}
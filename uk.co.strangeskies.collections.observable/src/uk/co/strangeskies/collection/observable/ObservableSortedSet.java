/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.collections.observable.
 *
 * uk.co.strangeskies.collections.observable is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.collections.observable is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/*
* Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utility.
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
 * along with uk.co.strangeskies.utility.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.collection.observable;

import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import uk.co.strangeskies.collection.observable.ObservableSortedSetDecorator.ObservableSortedSetDecoratorImpl;
import uk.co.strangeskies.collection.observable.SynchronizedObservableSortedSet.SynchronizedObservableSortedSetImpl;
import uk.co.strangeskies.collection.observable.UnmodifiableObservableSortedSet.UnmodifiableObservableSortedSetImpl;
import uk.co.strangeskies.utility.Self;

/**
 * A set which can be observed for changes, as per the contract of
 * {@link ObservableCollection}.
 * 
 * @author Elias N Vasylenko
 * @param <S>
 *          the self-bound, as per {@link Self}
 * @param <E>
 *          the element type, as per {@link Collection}
 */
public interface ObservableSortedSet<S extends ObservableSortedSet<S, E>, E> extends SortedSet<E>, ObservableSet<S, E> {
	@Override
	default ObservableSet<?, E> unmodifiableView() {
		return new UnmodifiableObservableSortedSetImpl<>(this);
	}

	/**
	 * As {@link #unmodifiableView()}, but a little more lenient with target type,
	 * taking advantage of the variance properties of a read-only collection.
	 * 
	 * @param <E>
	 *          the target element type
	 * @param set
	 *          the list over which we want a view
	 * @return an unmodifiable view over the given list
	 */
	static <E> ObservableSet<?, E> unmodifiableViewOf(ObservableSortedSet<?, ? extends E> set) {
		return new UnmodifiableObservableSortedSetImpl<>(set);
	}

	@Override
	default ObservableSet<?, E> synchronizedView() {
		return new SynchronizedObservableSortedSetImpl<>(this);
	}

	public static <C extends SortedSet<E>, E> ObservableSortedSet<?, E> over(C set,
			Function<? super C, ? extends C> copy) {
		return new ObservableSortedSetDecoratorImpl<C, E>(set, copy);
	}

	public static <E> ObservableSortedSet<?, E> ofElements(Collection<? extends E> elements) {
		ObservableSortedSet<?, E> set = new ObservableSortedSetDecoratorImpl<>(new TreeSet<>(), s -> new TreeSet<>(s));
		set.addAll(elements);
		return set;
	}

	@SafeVarargs
	public static <E> ObservableSet<?, E> ofElements(E... elements) {
		return ofElements(Arrays.asList(elements));
	}
}
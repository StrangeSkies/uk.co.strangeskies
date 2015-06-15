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
package uk.co.strangeskies.mathematics.expression.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import uk.co.strangeskies.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.utilities.IdentityComparator;
import uk.co.strangeskies.utilities.Observer;

public class ExpressionTreeSet<E extends Expression<?>> extends TreeSet<E>
		implements SortedExpressionSet<ExpressionTreeSet<E>, E>,
		CopyDecouplingExpression<ExpressionTreeSet<E>> {
	private static final long serialVersionUID = 1L;

	private boolean evaluated = true;

	private Observer<Expression<?>> dependencyObserver;

	private Set<Observer<? super Expression<ExpressionTreeSet<E>>>> observers;

	private ReadWriteLock lock;

	public ExpressionTreeSet(Comparator<? super E> comparator) {
		super(comparator);

		dependencyObserver = message -> update();

		observers = new TreeSet<>(new IdentityComparator<>());

		lock = new ReentrantReadWriteLock();
	}

	public ExpressionTreeSet() {
		this((Comparator<E>) null);
	}

	@SafeVarargs
	public ExpressionTreeSet(E... expressions) {
		this();

		addAll(Arrays.asList(expressions));
	}

	public ExpressionTreeSet(Collection<E> expressions) {
		this();

		addAll(expressions);
	}

	protected final void update() {
		getWriteLock().lock();

		if (evaluated) {
			evaluated = false;
			postUpdate();
		}

		getWriteLock().unlock();
	}

	protected final void postUpdate() {
		for (Observer<? super Expression<ExpressionTreeSet<E>>> observer : observers) {
			observer.notify(null);
		}
	}

	@Override
	public final boolean add(E expression) {
		getWriteLock().lock();

		boolean added = super.add(expression);

		if (added) {
			expression.addObserver(dependencyObserver);

			update();
		}

		getWriteLock().unlock();

		return added;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean remove(Object expression) {
		getWriteLock().lock();

		boolean removed = super.remove(expression);

		if (removed) {
			((E) expression).removeObserver(dependencyObserver);

			update();
		}

		getWriteLock().unlock();

		return removed;
	}

	@Override
	public final boolean addAll(Collection<? extends E> expressions) {
		getWriteLock().lock();

		boolean changed = false;

		for (E expression : expressions)
			if (super.add(expression)) {
				expression.addObserver(dependencyObserver);
				changed = true;
			}

		if (changed)
			update();

		getWriteLock().unlock();

		return changed;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean removeAll(Collection<?> expressions) {
		getWriteLock().lock();

		boolean changed = false;

		for (Object expression : expressions)
			if (super.remove(expression)) {
				((E) expression).removeObserver(dependencyObserver);
				changed = true;
			}

		if (changed)
			update();

		getWriteLock().unlock();

		return changed;
	}

	@Override
	public final void clear() {
		clear(true);
	}

	protected final void clear(boolean update) {
		getWriteLock().lock();
		if (!isEmpty()) {
			for (E expression : this)
				expression.removeObserver(dependencyObserver);

			super.clear();

			if (update)
				update();
		}
		getWriteLock().unlock();
	}

	@Override
	public final void set(Collection<? extends E> expressions) {
		getWriteLock().lock();
		retainAll(expressions);
		addAll(expressions);
		getWriteLock().unlock();
	}

	@Override
	public final boolean retainAll(Collection<?> expressions) {
		getWriteLock().lock();

		TreeSet<E> toRemove = new TreeSet<>();

		for (E expression : this)
			if (!expressions.contains(expression))
				toRemove.add(expression);

		boolean changed = removeAll(toRemove);

		getWriteLock().unlock();

		return changed;
	}

	@Override
	public final Collection<E> getUnmodifiableView() {
		return Collections.unmodifiableSet(this);
	}

	@Override
	public final ExpressionTreeSet<E> getValue() {
		getWriteLock().lock();
		evaluated = true;
		getWriteLock().unlock();

		return this;
	}

	@Override
	public final void clearObservers() {
		observers.clear();
	}

	@Override
	public final ExpressionTreeSet<E> copy() {
		getReadLock().lock();
		ExpressionTreeSet<E> copy = new ExpressionTreeSet<>(this);
		getReadLock().unlock();

		return copy;
	}

	@Override
	public boolean addObserver(
			Observer<? super Expression<ExpressionTreeSet<E>>> observer) {
		return observers.add(observer);
	}

	@Override
	public boolean removeObserver(
			Observer<? super Expression<ExpressionTreeSet<E>>> observer) {
		return observers.remove(observer);
	}

	@Override
	public Lock getReadLock() {
		return lock.readLock();
	}

	public Lock getWriteLock() {
		return lock.writeLock();
	}
}
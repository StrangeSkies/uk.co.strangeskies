/**
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.
 *
 *     uk.co.strangeskies is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     uk.co.strangeskies is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with uk.co.strangeskies.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.values;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import uk.co.strangeskies.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.expression.Variable;
import uk.co.strangeskies.mathematics.operation.Incrementable;
import uk.co.strangeskies.mathematics.operation.Multipliable;
import uk.co.strangeskies.mathematics.operation.Negatable;
import uk.co.strangeskies.mathematics.operation.Scalable;
import uk.co.strangeskies.mathematics.operation.Subtractable;
import uk.co.strangeskies.utilities.Copyable;
import uk.co.strangeskies.utilities.IdentityComparator;
import uk.co.strangeskies.utilities.Observer;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.Self;

public abstract class Value<S extends Value<S>> extends Number implements
		Multipliable<S, Value<?>>, Subtractable<S, Value<?>>, Negatable<S, S>,
		Scalable<S>, Property<S, Value<?>>, Incrementable<S>, Self<S>, Variable<S>,
		Copyable<S>, Comparable<Value<?>>, CopyDecouplingExpression<S> {
	private static final long serialVersionUID = -979949605176385397L;

	private final Set<Observer<? super Expression<S>>> observers;
	private boolean evaluated = true;
	private final ReadWriteLock lock;

	public Value() {
		this(0);
	}

	public Value(Number value) {
		observers = new TreeSet<Observer<? super Expression<S>>>(
				new IdentityComparator<>());
		lock = new ReentrantReadWriteLock();

		setValue(value);
	}

	public Value(Value<?> value) {
		this((Number) value);
	}

	@Override
	public ReadWriteLock getLock() {
		return lock;
	}

	@Override
	public int compareTo(Value<?> other) {
		int comparison = compareToAtSupportedPrecision(other);

		if (comparison == 0) {
			comparison = -other.compareToAtSupportedPrecision(this);
		}

		return comparison;
	}

	@Override
	public abstract S negate();

	public abstract S reciprocate();

	public S getReciprocal() {
		return copy().reciprocate();
	}

	@Override
	public final S getMultiplied(Value<?> value) {
		return Scalable.super.getMultiplied(value);
	}

	@Override
	public abstract String toString();

	@Override
	public abstract S set(Value<?> value);

	public abstract S setValue(Number value);

	@Override
	public abstract boolean equals(Object that);

	protected abstract boolean equals(Value<?> that);

	public abstract S unitInTheLastPlaceAbove();

	public abstract S unitInTheLastPlaceBelow();

	public final S unitInTheLastPlaceLarger() {
		return maximum(unitInTheLastPlaceAbove(), unitInTheLastPlaceBelow());
	}

	public final S unitInTheLastPlaceSmaller() {
		return minimum(unitInTheLastPlaceAbove(), unitInTheLastPlaceBelow());
	}

	public static <S extends Value<S>> S maximum(S first, S second) {
		if (first.compareTo(second) > 0) {
			return first;
		} else {
			return second;
		}
	}

	public static <S extends Value<S>> S minimum(S first, S second) {
		if (first.compareTo(second) < 0) {
			return first;
		} else {
			return second;
		}
	}

	@Override
	public final S get() {
		return getThis();
	}

	@Override
	public final S getValue() {
		getLock().readLock().lock();
		evaluated = true;
		S result = getThis();
		getLock().readLock().unlock();
		return result;
	}

	protected final void update() {
		if (evaluated) {
			evaluated = false;
			postUpdate();
		}
	}

	protected final S update(BooleanSupplier runnable) {
		getLock().writeLock().lock();
		if (runnable.getAsBoolean())
			update();
		getLock().writeLock().unlock();

		return getThis();
	}

	protected final S update(Runnable runnable) {
		getLock().writeLock().lock();
		runnable.run();
		update();
		getLock().writeLock().unlock();

		return getThis();
	}

	protected final <T> T read(Supplier<T> supplier) {
		getLock().readLock().lock();
		T result = supplier.get();
		getLock().readLock().unlock();

		return result;
	}

	protected final void postUpdate() {
		for (Observer<? super Expression<S>> observer : observers)
			observer.notify(null);
	}

	@Override
	public final boolean addObserver(Observer<? super Expression<S>> observer) {
		return observers.add(observer);
	}

	@Override
	public final boolean removeObserver(Observer<? super Expression<S>> observer) {
		return observers.remove(observer);
	}

	@Override
	public final void clearObservers() {
		observers.clear();
	}

	@Override
	public abstract S multiply(Value<?> value);

	@Override
	public abstract S divide(Value<?> value);

	public abstract boolean equals(double value);

	public abstract boolean equals(float value);

	public abstract boolean equals(int value);

	public abstract boolean equals(long value);

	public abstract int compareToAtSupportedPrecision(
	/*  */Value<?> other);

	public abstract int getMultipliedPrimitive(int value);

	public abstract long getMultipliedPrimitive(long value);

	public abstract float getMultipliedPrimitive(float value);

	public abstract double getMultipliedPrimitive(double value);

	public abstract int getDividedPrimitive(int value);

	public abstract long getDividedPrimitive(long value);

	public abstract float getDividedPrimitive(float value);

	public abstract double getDividedPrimitive(double value);

	@Override
	public abstract int intValue();

	@Override
	public abstract long longValue();

	@Override
	public abstract float floatValue();

	@Override
	public abstract double doubleValue();

	public abstract S square();

	public S getSquared() {
		return copy().square();
	}

	public abstract S squareRoot();

	public S getSquareRoot() {
		return copy().squareRoot();
	}

	public abstract S exponentiate(Value<?> exponential);

	public S getExponentiated(Value<?> exponential) {
		return copy().exponentiate(exponential);
	}

	public abstract S root(Value<?> root);

	public S getRoot(Value<?> root) {
		return copy().root(root);
	}

	public abstract S modulus();

	public S getModulus() {
		return copy().modulus();
	}
}

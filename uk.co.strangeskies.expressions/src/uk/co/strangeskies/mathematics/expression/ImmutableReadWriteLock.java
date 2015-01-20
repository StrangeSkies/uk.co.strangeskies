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
package uk.co.strangeskies.mathematics.expression;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ImmutableReadWriteLock implements Lock {
	private class ImmutableCondition implements Condition {
		@Override
		public void signalAll() {
		}

		@Override
		public void signal() {
		}

		@Override
		public boolean awaitUntil(Date deadline) {
			return true;
		}

		@Override
		public void awaitUninterruptibly() {
		}

		@Override
		public long awaitNanos(long nanosTimeout) {
			return 0;
		}

		@Override
		public boolean await(long time, TimeUnit unit) {
			return false;
		}

		@Override
		public void await() {
		}
	}

	@Override
	public void unlock() {
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) {
		return true;
	}

	@Override
	public boolean tryLock() {
		return true;
	}

	@Override
	public Condition newCondition() {
		return new ImmutableCondition();
	}

	@Override
	public void lockInterruptibly() {
	}

	@Override
	public void lock() {
	}
}
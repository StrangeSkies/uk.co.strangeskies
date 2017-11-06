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
 * This file is part of uk.co.strangeskies.observable.
 *
 * uk.co.strangeskies.observable is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.observable is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.observable;

import java.util.concurrent.Executor;

import org.junit.Test;

import mockit.Expectations;
import mockit.FullVerificationsInOrder;
import mockit.Injectable;
import mockit.VerificationsInOrder;

@SuppressWarnings("javadoc")
public class ExecutorObserverTest {
  interface MockObserver<T> extends Observer<T> {}

  interface MockObservation extends Observation {}

  @Injectable
  MockObservation upstreamObservation;

  @Injectable
  MockObserver<String> downstreamObserver;

  @Test
  public void messageEventOnInlineExecutorTest() {
    SafeObserver<String> test = new ExecutorObserver<>(downstreamObserver, r -> r.run());

    test.onObserve(upstreamObservation);
    test.getObservation().requestNext();
    test.onNext("message");

    new VerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("message");
      }
    };
  }

  @Test
  public void messageEventOnDiscardingExecutorTest() {
    Observer<String> test = new ExecutorObserver<>(downstreamObserver, r -> {});

    test.onObserve(upstreamObservation);
    test.onNext("message");

    new FullVerificationsInOrder() {
      {}
    };
  }

  @Test
  public void messageEventOnMockedExecutorTest(@Injectable Executor executor) {
    Observer<String> test = new ExecutorObserver<>(downstreamObserver, executor);

    test.onObserve(upstreamObservation);
    test.onNext("message");

    new FullVerificationsInOrder() {
      {
        executor.execute((Runnable) any);
        executor.execute((Runnable) any);
      }
    };
  }

  @Test
  public void throwFromOnObserveTest() {
    Throwable throwable = new Exception();

    new Expectations() {
      {
        downstreamObserver.onObserve((Observation) any);
        result = throwable;
      }
    };

    Observer<String> test = new ExecutorObserver<>(downstreamObserver, r -> r.run());
    test.onObserve(upstreamObservation);

    new VerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onFail(throwable);
      }
    };
  }

  @Test
  public void throwFromOnNextTest() {
    Throwable throwable = new Exception();

    new Expectations() {
      {
        downstreamObserver.onNext(anyString);
        result = throwable;
      }
    };

    SafeObserver<String> test = new ExecutorObserver<>(downstreamObserver, r -> r.run());

    test.onObserve(upstreamObservation);
    test.getObservation().requestNext();
    test.onNext("message");

    new VerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("message");
        downstreamObserver.onFail(throwable);
      }
    };
  }

  @Test(expected = NullPointerException.class)
  public void nullExecutorTest() {
    new ExecutorObserver<>(downstreamObserver, null);
  }
}

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
package uk.co.strangeskies.utilities.tuples;

public class Quadruple<A, B, C, D> extends Tuple<A, Triple<B, C, D>> {
  public Quadruple(A a, B b, C c, D d) {
    super(a, new Triple<>(b, c, d));
  }

  public A get0() {
    return getHead();
  }

  public B get1() {
    return getTail().getHead();
  }

  public C get2() {
    return getTail().getTail().getHead();
  }

  public D get3() {
    return getTail().getTail().getTail().getHead();
  }
}

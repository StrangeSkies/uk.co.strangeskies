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
package uk.co.strangeskies.utilities.collection;

import java.util.AbstractList;
import java.util.List;

public class SubList<T> extends AbstractList<T> {
  private final List<T> backingList;

  private int startPosition = 0;
  private int endPosition = 0;

  public SubList(List<T> backingList, int startPosition, int endPosition) {
    this.backingList = backingList;

    this.startPosition = startPosition;
    this.endPosition = endPosition;
  }

  @Override
  public int size() {
    return endPosition - startPosition;
  }

  @Override
  public T get(int index) {
    if (index + startPosition >= endPosition) {
      throw new ArrayIndexOutOfBoundsException(index);
    }
    return backingList.get(index + startPosition);
  }
}
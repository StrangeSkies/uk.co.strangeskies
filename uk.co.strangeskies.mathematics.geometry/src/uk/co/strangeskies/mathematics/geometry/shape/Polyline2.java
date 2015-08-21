/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.mathematics.geometry.
 *
 * uk.co.strangeskies.mathematics.geometry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics.geometry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.mathematics.geometry.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.geometry.shape;

import java.util.List;

import uk.co.strangeskies.mathematics.geometry.Bounds2;
import uk.co.strangeskies.mathematics.geometry.PolylineN;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.values.IntValue;
import uk.co.strangeskies.mathematics.values.Value;

public interface Polyline2<S extends Polyline2<S, V>, V extends Value<V>>
		extends Shape<S>, PolylineN<Vector2<V>, V> {
	@Override
	public Bounds2<V> getBounds();

	@Override
	public List<Line2<V>> lines();

	@Override
	default Value<?> getArea() {
		return new IntValue();
	}

	@Override
	default Value<?> getPerimeter() {
		return getLength().multiply(2);
	}

	@Override
	default boolean contains(Vector2<?> point) {
		return false;
	}

	@Override
	default boolean touches(Vector2<?> point) {
		return lines().stream().anyMatch(l -> l.touches(point));
	}
}
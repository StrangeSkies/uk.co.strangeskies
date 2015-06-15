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

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.values.DoubleValue;
import uk.co.strangeskies.mathematics.values.Value;

/**
 * A potentially self-touching, but non-self-crossing, and otherwise
 * traditionally 'simple', polygon.
 *
 * @author eli
 *
 * @param <S>
 * @param <V>
 */
public interface SimplePolygon<S extends SimplePolygon<S, V>, V extends Value<V>>
		extends /*  */ComplexPolygon<S, V> {
	public enum WindingDirection {
		CLOCKWISE, COUNTER_CLOCKWISE;
	}

	/**
	 * Returns the direction which the vertices of this polygon are wound in.
	 * 
	 * @return
	 */
	WindingDirection getWindingDirection();

	@Override
	default WindingRule windingRule() {
		return WindingRule.EVEN_ODD;
	}

	@Override
	default Value<?> getArea() {
		return new DoubleValue(getSignedArea(WindingDirection.CLOCKWISE)).modulus();
	}

	default double getSignedArea(WindingDirection windingDirection) {
		List<Vector2<V>> vertices = boundary().vertices();

		double area = 0;

		Vector2<V> previousVertex = vertices.get(vertices.size() - 1);
		for (Vector2<V> vertex : vertices) {
			area += (vertex.getY().doubleValue() - previousVertex.getY()
					.doubleValue())
					* (vertex.getX().doubleValue() + vertex.getX().doubleValue()) * 0.5;

			previousVertex = vertex;
		}

		return area;
	}
}
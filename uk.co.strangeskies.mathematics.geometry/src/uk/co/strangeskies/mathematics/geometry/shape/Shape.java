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

import uk.co.strangeskies.mathematics.expression.Variable;
import uk.co.strangeskies.mathematics.geometry.Bounds2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.shape.mesh.Mesh;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.Copyable;
import uk.co.strangeskies.utilities.Property;
import uk.co.strangeskies.utilities.Self;

/**
 * TODO: Decide whether the following decision is sensible! Maybe we should
 * require all shapes be affinely transformable? Would be okay for polygons,
 * ellipses, and beziers. Maybe we should use homogeneous coordinates after all?
 * 
 * Also TODO, think about passing in triangulator to polygon builder and have
 * shapes triangulate internally. This would help in cases where triangulation
 * is necessary for calculation of other properties, so as not to have to
 * recalculate it...
 * 
 * In general, classes and interfaces which extend Shape should deal with
 * <em>non-homogeneous</em> vectors, in the interests of a simple and consistent
 * API. Shapes don't generally support affine transformations or related
 * operations, so there is little value in trying to make homogeneous coordinate
 * space a consistent part of the API at this level. Classes related to
 * {@link Mesh}, on the other hand, do deal with homogeneous coordinate space.
 * 
 * @author eli
 *
 * @param <S>
 */
public interface Shape<S extends Shape<S>> extends Self<S>, Copyable<S>,
		Property<S, S>, Variable<S> {
	Value<?> getArea(/*  this */);

	Value<?> getPerimeter(/*  this */);

	boolean contains(
	/*  this, *//*  */Vector2<?> point);

	boolean touches(/*  this, *//*  */Vector2<?> point);

	boolean intersects(
	/*  this, *//*  */Shape<?> shape);

	boolean touches(/*  this, *//*  */Shape<?> shape);

	Bounds2<?> getBounds(/*  this */);

	@Override
	default S get() {
		return getThis();
	}
}
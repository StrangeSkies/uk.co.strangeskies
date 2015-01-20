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
package uk.co.strangeskies.mathematics.geometry.matrix.vector;

import uk.co.strangeskies.mathematics.geometry.Translatable;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix;
import uk.co.strangeskies.mathematics.operation.Addable;
import uk.co.strangeskies.mathematics.operation.NonCommutativelyMultipliable;
import uk.co.strangeskies.mathematics.operation.Scalable;
import uk.co.strangeskies.mathematics.operation.Subtractable;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.Self;

public interface VectorH<S extends VectorH<S, V>, V extends Value<V>> extends
/**/Vector<S, V>, Translatable<S>, /**/
Addable<S, Matrix<?, ?>>,
/**/Subtractable<S, Matrix<?, ?>>,
/**/NonCommutativelyMultipliable<S, Matrix<?, ?>>, Scalable<S>,
		Self<S> {
	public enum Type {
		Absolute, Relative;
	}

	public Type getType();

	public void setType(Type type);

	public int getProjectedDimensions();

	public Vector<?, V> getMutableVector();
}

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
package uk.co.strangeskies.mathematics.geometry.shape.tessellation;

import uk.co.strangeskies.mathematics.expression.Expression;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.shape.SimplePolygon;
import uk.co.strangeskies.mathematics.geometry.shape.mesh.Mesh;
import uk.co.strangeskies.mathematics.geometry.shape.mesh.Mesh.MeshingScheme;
import uk.co.strangeskies.mathematics.values.Value;

public interface Tessellation<V extends Value<V>> extends
		Expression<Mesh<Vector2<V>>> {
	public void setPolygon(SimplePolygon<?, V> polygon);

	public void setLimit(int limit);

	public void setTessellationScheme(MeshingScheme tessellationScheme);

	public MeshingScheme getTessellationScheme();
}

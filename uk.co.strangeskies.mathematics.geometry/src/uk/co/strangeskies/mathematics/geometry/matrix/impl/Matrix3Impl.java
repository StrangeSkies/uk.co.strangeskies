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
package uk.co.strangeskies.mathematics.geometry.matrix.impl;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.Matrix;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix3;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector3;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.Vector3Impl;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.factory.Factory;

public class Matrix3Impl<V extends Value<V>> extends MatrixSImpl<Matrix3<V>, V>
		implements Matrix3<V> {
	public Matrix3Impl(Order order, Factory<V> valueFactory) {
		super(3, order, valueFactory);
	}

	public Matrix3Impl(Order order, List<? extends List<? extends V>> values) {
		super(order, values);

		Matrix.assertDimensions(this, 3);
	}

	@Override
	public Matrix3<V> copy() {
		return new Matrix3Impl<V>(getOrder(), getData2());
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector3<V>> getRowVectors() {
		return (List<Vector3<V>>) super.getRowVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector3<V>> getColumnVectors() {
		return (List<Vector3<V>>) super.getColumnVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final Vector3<V> getRowVector(int row) {
		return (Vector3Impl<V>) super.getRowVector(row);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final Vector3<V> getColumnVector(int column) {
		return (Vector3Impl<V>) super.getColumnVector(column);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector3<V>> getMajorVectors() {
		return (List<Vector3<V>>) super.getMajorVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector3<V>> getMinorVectors() {
		return (List<Vector3<V>>) super.getMinorVectors();
	}

	@Override
	public final Vector3<V> getMajorVector(int index) {
		return new Vector3Impl<V>(getOrder(),
				getOrder().getAssociatedOrientation(), getData2().get(index));
	}

	@Override
	public final Vector3<V> getMinorVector(int index) {
		List<V> minorElements = new ArrayList<V>();
		for (List<V> elements : getData2()) {
			minorElements.add(elements.get(index));
		}
		return new Vector3Impl<V>(getOrder(), getOrder().getAssociatedOrientation()
				.getOther(), minorElements);
	}
}

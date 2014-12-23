package uk.co.strangeskies.mathematics.geometry.matrix.vector.impl;

import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector3;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.factory.Factory;

public class Vector3Impl<V extends Value<V>> extends VectorImpl<Vector3<V>, V>
		implements Vector3<V> {
	public Vector3Impl(Order order, Orientation orientation,
			Factory<V> valueFactory) {
		super(3, order, orientation, valueFactory);
	}

	public Vector3Impl(Order order, Orientation orientation,
			List<? extends V> values) {
		super(order, orientation, values);

		assertDimensions(this, 3);
	}

	@Override
	public Vector3<V> copy() {
		return new Vector3Impl<V>(getOrder(), getOrientation(), getData());
	}
}
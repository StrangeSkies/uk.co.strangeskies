package uk.co.strangeskies.mathematics.geometry.matrix.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.mathematics.expression.CompoundExpression;
import uk.co.strangeskies.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.mathematics.geometry.DimensionalityException;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.VectorNImpl;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.collection.MergeIndicesListView;
import uk.co.strangeskies.utilities.collection.NullPointerInCollectionException;
import uk.co.strangeskies.utilities.factory.Factory;
import uk.co.strangeskies.utilities.function.TriFunction;
import uk.co.strangeskies.utilities.function.collection.ListTransformationView;

public abstract class MatrixImpl<S extends Matrix<S, V>, V extends Value<V>>
		extends CompoundExpression<S> implements Matrix<S, V>,
		CopyDecouplingExpression<S> {
	private final List<List<V>> data;
	private final Order order;

	/* All constructors must go through here */
	private MatrixImpl(Order order) {
		super(false);

		if (order == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		data = new ArrayList<>();

		this.order = order;
	}

	public MatrixImpl(int rows, int columns, Order order, Factory<V> valueFactory) {
		this(order);

		try {
			if (valueFactory == null) {
				throw new NullPointerException();
			}

			DimensionalityException.checkValid(rows);
			DimensionalityException.checkValid(columns);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		int major;
		int minor;
		if (order == Order.RowMajor) {
			major = columns;
			minor = rows;
		} else {
			major = rows;
			minor = columns;
		}

		for (int i = 0; i < major; i++) {
			List<V> elements = new ArrayList<V>();
			data.add(elements);
			for (int j = 0; j < minor; j++) {
				elements.add(valueFactory.create());
			}
		}

		getDependencies().set(getData());
	}

	public MatrixImpl(Order order, List<? extends List<? extends V>> values) {
		this(order);

		try {
			if (values == null || order == null) {
				throw new NullPointerException();
			}
			NullPointerInCollectionException.checkList(values);

			DimensionalityException.checkValid(values.size());

			Iterator<? extends List<? extends V>> majorIterator = values.iterator();

			List<? extends V> firstMajor = majorIterator.next();
			DimensionalityException.checkValid(firstMajor.size());

			NullPointerInCollectionException.checkList(firstMajor);

			while (majorIterator.hasNext()) {
				List<? extends V> major = majorIterator.next();
				DimensionalityException.checkEquivalence(firstMajor.size(),
						major.size());

				NullPointerInCollectionException.checkList(major);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		for (List<? extends V> major : values) {
			List<V> elements = new ArrayList<V>();
			data.add(elements);
			for (V value : major) {
				elements.add(value);
			}
		}

		getDependencies().set(getData());
	}

	@Override
	public final Order getOrder() {
		return order;
	}

	@Override
	public final S transpose() {
		Matrix.assertIsSquare(this);

		List<List<V>> transposedData = Matrix.transposeData(data);
		data.clear();
		data.addAll(transposedData);

		update();

		return getThis();
	}

	@Override
	public final int getMajorSize() {
		return data.get(0).size();
	}

	@Override
	public final int getMinorSize() {
		return data.size();
	};

	protected List<V> getRowVectorData(int row) {
		if (getOrder() == Order.RowMajor) {
			return getMajorVectorData(row);
		} else {
			return getMinorVectorData(row);
		}
	}

	protected List<V> getColumnVectorData(int column) {
		if (getOrder() == Order.ColumnMajor) {
			return getMajorVectorData(column);
		} else {
			return getMinorVectorData(column);
		}
	}

	@Override
	public Vector<?, V> getMajorVector(int index) {
		return new VectorNImpl<V>(getOrder(),
				getOrder().getAssociatedOrientation(), getMajorVectorData(index));
	}

	protected List<V> getMajorVectorData(int index) {
		return data.get(index);
	}

	@Override
	public Vector<?, V> getMinorVector(int index) {
		return new VectorNImpl<V>(getOrder(), getOrder().getOther()
				.getAssociatedOrientation(), getMinorVectorData(index));
	}

	protected List<V> getMinorVectorData(int index) {
		List<V> minorElements = new ArrayList<V>();
		for (List<V> elements : data) {
			minorElements.add(elements.get(index));
		}
		return minorElements;
	}

	@Override
	public final List<List<V>> getData2() {
		return new ListTransformationView<>(data,
				l -> Collections.unmodifiableList(l));
	}

	@Override
	public final List<V> getData() {
		return new MergeIndicesListView<>(data);
	}

	@Override
	public final String toString() {
		String string = new String();

		string += "[";

		boolean first = true;
		for (List<V> elements : data) {
			if (first) {
				first = false;
			} else {
				string += ", ";
			}

			Iterator<V> dataIterator = elements.iterator();
			string += "[" + dataIterator.next();
			while (dataIterator.hasNext()) {
				string += ", " + dataIterator.next();
			}
			string += "]";
		}

		string += "]";

		return string;
	}

	@Override
	public final boolean equals(Object that) {
		if (this == that) {
			return true;
		}

		if (!(that instanceof Matrix<?, ?>)) {
			return false;
		}

		Matrix<?, ?> thatMatrix = (Matrix<?, ?>) that;

		if (!getDimensions2().equals(thatMatrix.getDimensions2())) {
			return false;
		}

		if (!getOrder().equals(thatMatrix.getOrder()))
			return withOrder(thatMatrix.getOrder()).equals(thatMatrix);

		List<? extends List<? extends Value<?>>> thoseElements = thatMatrix
				.getData2();
		int i = 0;
		for (List<V> elements : data) {
			int j = 0;
			for (V element : elements) {
				if (!element.equals(thoseElements.get(i).get(j)))
					return false;

				j++;
			}

			i++;
		}

		return true;
	}

	@Override
	public final int hashCode() {
		return getRowSize() + getColumnSize() + getOrder().hashCode()
				+ data.hashCode();
	}

	@Override
	public final int compareTo(Matrix<?, ?> other) {
		int comparison;

		comparison = getDimensions2().compareTo(other.getDimensions2());
		if (comparison != 0)
			return comparison;

		if (!getOrder().equals(other.getOrder()))
			return withOrder(other.getOrder()).compareTo(other);

		List<? extends List<? extends Value<?>>> thoseElements = other.getData2();
		int i = 0;
		for (List<V> elements : data) {
			int j = 0;
			for (V element : elements) {
				comparison = element.compareTo(thoseElements.get(i).get(j));
				if (comparison != 0)
					return comparison;

				j++;
			}

			i++;
		}

		return 0;
	}

	@Override
	protected final S evaluate() {
		return getThis();
	}

	@Override
	public V getElement(int major, int minor) {
		return data.get(major).get(minor);
	}

	@Override
	public final S operateOnData(Function<? super V, ? extends V> operator) {
		for (List<V> major : data)
			for (V element : major)
				element = operator.apply(element);

		getDependencies().set(getData());

		return getThis();
	}

	@Override
	public final S operateOnData(
			BiFunction<? super V, Integer, ? extends V> operator) {
		int i = 0;
		for (List<V> elements : data)
			for (V element : elements)
				element = operator.apply(element, i++);

		getDependencies().set(getData());

		return getThis();
	}

	@Override
	public final S operateOnData2(
			TriFunction<? super V, Integer, Integer, ? extends V> operator) {
		int i = 0;
		int j = 0;
		for (List<V> major : data) {
			for (V element : major) {
				element = operator.apply(element, i, j);
				j++;
			}
			i++;
		}

		getDependencies().set(getData());

		return getThis();
	}
}

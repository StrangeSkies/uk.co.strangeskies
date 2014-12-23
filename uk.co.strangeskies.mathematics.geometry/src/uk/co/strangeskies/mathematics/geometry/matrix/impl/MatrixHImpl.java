package uk.co.strangeskies.mathematics.geometry.matrix.impl;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.Matrix;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixH;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixNN;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixS;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH.Type;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.VectorHNImpl;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.collection.SubList;
import uk.co.strangeskies.utilities.factory.Factory;
import uk.co.strangeskies.utilities.function.collection.ListTransformOnceView;
import uk.co.strangeskies.utilities.function.collection.ListTransformationFunction;

public abstract class MatrixHImpl<S extends MatrixH<S, V>, V extends Value<V>>
		extends /* @ReadOnly */MatrixImpl<S, V> implements MatrixH<S, V> {
	public MatrixHImpl(int size, Order order, Factory<V> valueFactory) {
		super(size + 1, size, order, valueFactory);

		for (int i = 0; i < getProjectedDimensions(); i++) {
			getElement(i, i).setValue(1);
		}
	}

	public MatrixHImpl(Order order, List<? extends List<? extends V>> values) {
		super(order, resizeColumnsImplementation(values, order));

		Matrix.assertIsSquare(this);
	}

	protected static <V extends Value<V>> List<List<V>> resizeColumnsImplementation(
			List<? extends List<? extends V>> data, Order order) {
		List<List<V>> newData = new ArrayList<>();
		List<V> newElements = null;

		if (order == Order.ColumnMajor) {
			for (List<? extends V> elements : data) {
				newElements = new ArrayList<>(elements);
				newData.add(newElements);

				V element = newElements.get(0).copy().setValue(0);
				newElements.add(element);
			}
		} else {
			for (List<? extends V> elements : data)
				newData.add(new ArrayList<>(elements));

			newElements = new ArrayList<V>();
			for (V element : data.get(0))
				newElements.add(element.copy().setValue(0));

			newData.add(newElements);
		}

		newElements.get(newElements.size() - 1).setValue(0);

		return newData;
	}

	@Override
	public int getDimensions() {
		return getMinorSize();
	}

	@Override
	public int getProjectedDimensions() {
		return getDimensions() - 1;
	}

	@Override
	public MatrixNN<V> getMutableMatrix() {
		List<List<V>> dataView;

		if (getOrder() == Order.ColumnMajor) {
			dataView = new ListTransformOnceView<List<V>, List<V>>(getData2(),
					l -> l.subList(0, getProjectedDimensions()));
		} else {
			dataView = new SubList<>(getData2(), 0, getProjectedDimensions());
		}

		return new MatrixNNImpl<V>(getOrder(), dataView);
	}

	protected List<List<V>> getTransformationData2() {
		return ListTransformationFunction.apply(
				getData2().subList(0, getProjectedDimensions()),
				l -> l.subList(0, getProjectedDimensions()));
	}

	@Override
	public abstract MatrixS<?, V> getTransformationMatrix();

	@Override
	public S translate(Vector<?, ?> translation) {
		getColumnVector(getProjectedDimensions()).translate(translation);

		return getThis();
	}

	@Override
	public S getTranslated(Vector<?, ?> translation) {
		return copy().translate(translation);
	}

	@Override
	public S preTranslate(Vector<?, ?> translation) {
		// TODO implement pre-rotation
		return null;
	}

	@Override
	public S getPreTranslated(Vector<?, ?> translation) {
		return copy().preTranslate(translation);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<? extends VectorH<?, V>> getColumnVectors() {
		return (List<? extends VectorH<?, V>>) super.getColumnVectors();
	}

	@Override
	public VectorH<?, V> getColumnVector(int column) {
		return new VectorHNImpl<V>(column == getDimensions() - 1 ? Type.Absolute
				: Type.Relative, getOrder(), Orientation.Column, getColumnVectorData(
				column).subList(0, getProjectedDimensions()));
	}

	@Override
	public V getDeterminant() {
		return MatrixSImpl.getDeterminant(this);
	}
}
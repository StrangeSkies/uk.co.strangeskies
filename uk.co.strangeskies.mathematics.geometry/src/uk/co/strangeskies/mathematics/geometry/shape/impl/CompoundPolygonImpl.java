package uk.co.strangeskies.mathematics.geometry.shape.impl;

import java.util.List;
import java.util.Set;

import uk.co.strangeskies.mathematics.expression.CompoundExpression;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.shape.ClosedPolyline2;
import uk.co.strangeskies.mathematics.geometry.shape.CompoundPolygon;
import uk.co.strangeskies.mathematics.geometry.shape.Shape;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.mathematics.values.ValueFactory;

/**
 * Complex polygons, self intersecting with holes and multiple parts.
 *
 * @author eli
 *
 * @param <V>
 */
public class CompoundPolygonImpl<V extends Value<V>> extends
		CompoundExpression<CompoundPolygonImpl<V>> implements
		CompoundPolygon<CompoundPolygonImpl<V>, V> {
	public CompoundPolygonImpl(CompoundPolygon<?, ?> polygon,
			ValueFactory<? extends V> valueFactory) {
	}

	public CompoundPolygonImpl(CompoundPolygon<?, ? extends V> polygon) {
	}

	public CompoundPolygonImpl(List<? extends Vector2<?>> polygon,
			ValueFactory<? extends V> valueFactory) {
	}

	public CompoundPolygonImpl(List<? extends Vector2<V>> polygon) {
	}

	@Override
	public Value<?> getArea() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean intersects(Shape<?> shape) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touches(Shape<?> shape) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public CompoundPolygonImpl<V> copy() {
		return new CompoundPolygonImpl<>(this);
	}

	@Override
	public CompoundPolygonImpl<V> set(CompoundPolygonImpl<V> to) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompoundPolygonImpl<V> get() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public uk.co.strangeskies.mathematics.geometry.shape.CompoundPolygon.WindingRule windingRule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ClosedPolyline2<V>> boundaryComponents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CompoundPolygonImpl<V> evaluate() {
		// TODO Auto-generated method stub
		return null;
	}
}
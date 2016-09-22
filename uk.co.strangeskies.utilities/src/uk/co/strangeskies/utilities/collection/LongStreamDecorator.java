package uk.co.strangeskies.utilities.collection;

import java.util.LongSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import uk.co.strangeskies.utilities.BaseStreamDecorator;

/**
 * A decorator for a {@link LongStream} which wraps intermediate and terminal
 * operations such that they can be easily extended.
 * 
 * @author Elias N Vasylenko
 */
public interface LongStreamDecorator extends BaseStreamDecorator<Long, LongStream>, LongStream {
	@Override
	default LongStream parallel() {
		return BaseStreamDecorator.super.parallel();
	}

	@Override
	default LongStream sequential() {
		return BaseStreamDecorator.super.parallel();
	}

	@Override
	default PrimitiveIterator.OfLong iterator() {
		return decorateTerminal(LongStream::iterator);
	}

	@Override
	default Spliterator.OfLong spliterator() {
		return decorateTerminal(LongStream::spliterator);
	}

	@Override
	LongStream component();

	@Override
	default LongStream decorateIntermediate(Function<? super LongStream, LongStream> transformation) {
		return decorateIntermediateLong(transformation);
	}

	@Override
	default LongStream filter(LongPredicate predicate) {
		return decorateIntermediate(s -> s.filter(predicate));
	}

	@Override
	default LongStream map(LongUnaryOperator mapper) {
		return decorateIntermediate(s -> s.map(mapper));
	}

	@Override
	default <U> Stream<U> mapToObj(LongFunction<? extends U> mapper) {
		return decorateIntermediateReference(s -> s.mapToObj(mapper));
	}

	@Override
	default IntStream mapToInt(LongToIntFunction mapper) {
		return decorateIntermediateInt(s -> s.mapToInt(mapper));
	}

	@Override
	default DoubleStream mapToDouble(LongToDoubleFunction mapper) {
		return decorateIntermediateDouble(s -> s.mapToDouble(mapper));
	}

	@Override
	default LongStream flatMap(LongFunction<? extends LongStream> mapper) {
		return decorateIntermediate(s -> s.flatMap(mapper));
	}

	@Override
	default LongStream distinct() {
		return decorateIntermediate(s -> s.distinct());
	}

	@Override
	default LongStream sorted() {
		return decorateIntermediate(s -> s.sorted());
	}

	@Override
	default LongStream peek(LongConsumer action) {
		return decorateIntermediate(s -> s.peek(action));
	}

	@Override
	default LongStream limit(long maxSize) {
		return decorateIntermediate(s -> s.limit(maxSize));
	}

	@Override
	default LongStream skip(long n) {
		return decorateIntermediate(s -> s.skip(n));
	}

	@Override
	default void forEach(LongConsumer action) {
		decorateVoidTerminal(s -> s.forEach(action));
	}

	@Override
	default void forEachOrdered(LongConsumer action) {
		decorateVoidTerminal(s -> s.forEachOrdered(action));
	}

	@Override
	default long[] toArray() {
		return decorateTerminal(s -> s.toArray());
	}

	@Override
	default long reduce(long identity, LongBinaryOperator op) {
		return decorateTerminal(s -> s.reduce(identity, op));
	}

	@Override
	default OptionalLong reduce(LongBinaryOperator op) {
		return decorateTerminal(s -> s.reduce(op));
	}

	@Override
	default <R> R collect(Supplier<R> supplier, ObjLongConsumer<R> accumulator, BiConsumer<R, R> combiner) {
		return decorateTerminal(s -> s.collect(supplier, accumulator, combiner));
	}

	@Override
	default long sum() {
		return decorateTerminal(s -> s.sum());
	}

	@Override
	default OptionalLong min() {
		return decorateTerminal(s -> s.min());
	}

	@Override
	default OptionalLong max() {
		return decorateTerminal(s -> s.max());
	}

	@Override
	default long count() {
		return decorateTerminal(s -> s.count());
	}

	@Override
	default OptionalDouble average() {
		return decorateTerminal(s -> s.average());
	}

	@Override
	default LongSummaryStatistics summaryStatistics() {
		return decorateTerminal(s -> s.summaryStatistics());
	}

	@Override
	default boolean anyMatch(LongPredicate predicate) {
		return decorateTerminal(s -> s.anyMatch(predicate));
	}

	@Override
	default boolean allMatch(LongPredicate predicate) {
		return decorateTerminal(s -> s.allMatch(predicate));
	}

	@Override
	default boolean noneMatch(LongPredicate predicate) {
		return decorateTerminal(s -> s.noneMatch(predicate));
	}

	@Override
	default OptionalLong findFirst() {
		return decorateTerminal(s -> s.findFirst());
	}

	@Override
	default OptionalLong findAny() {
		return decorateTerminal(s -> s.findAny());
	}

	@Override
	default DoubleStream asDoubleStream() {
		return decorateIntermediateDouble(s -> s.asDoubleStream());
	}

	@Override
	default Stream<Long> boxed() {
		return mapToObj(i -> i);
	}
}
/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.reflection.codegen.
 *
 * uk.co.strangeskies.reflection.codegen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.codegen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.codegen.block;

import static java.util.Arrays.asList;
import static uk.co.strangeskies.reflection.token.ExecutableToken.staticMethods;
import static uk.co.strangeskies.reflection.token.MethodMatcher.allMethods;
import static uk.co.strangeskies.reflection.token.OverloadResolver.resolveOverload;

import java.lang.reflect.Type;
import java.util.List;

import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.MethodMatcher;
import uk.co.strangeskies.reflection.token.TypeToken;

public class Expressions {
	@SuppressWarnings("unchecked")
	private static final ExecutableToken<Void, TypeToken<?>> FOR_TYPE_METHOD = (ExecutableToken<Void, TypeToken<?>>) staticMethods(
			TypeToken.class).filter(allMethods().named("forType")).collect(resolveOverload(Type.class));

	private Expressions() {}

	public static <T> ValueExpression<T> nullLiteral() {
		return (ValueExpression<T>) v -> v.visitNull();
	}

	private static <T> ValueExpression<T> literalImpl(T value) {
		return v -> v.visitLiteral(value);
	}

	public static ValueExpression<?> tryLiteral(Object object) {
		if (object instanceof String
				|| object instanceof Integer
				|| object instanceof Float
				|| object instanceof Long
				|| object instanceof Double
				|| object instanceof Byte
				|| object instanceof Character
				|| object instanceof Class<?>) {
			return literalImpl(object);
		}
		throw new IllegalArgumentException();
	}

	public static ValueExpression<String> literal(String value) {
		return literalImpl(value);
	}

	public static ValueExpression<Integer> literal(int value) {
		return literalImpl(value);
	}

	public static ValueExpression<Float> literal(float value) {
		return literalImpl(value);
	}

	public static ValueExpression<Long> literal(long value) {
		return literalImpl(value);
	}

	public static ValueExpression<Double> literal(double value) {
		return literalImpl(value);
	}

	public static ValueExpression<Byte> literal(byte value) {
		return literalImpl(value);
	}

	public static ValueExpression<Character> literal(char value) {
		return literalImpl(value);
	}

	public static <T> ValueExpression<Class<T>> literal(Class<T> value) {
		return literalImpl(value);
	}

	public static <T> ValueExpression<? extends TypeToken<T>> typeTokenExpression(
			TypeToken<T> token) {
		return new CastValueExpression<>(
				token.getThisTypeToken(),
				invokeStatic(
						FOR_TYPE_METHOD,
						new AnnotatedTypeExpression(token.getAnnotatedDeclaration())));
	}

	/**
	 * @see #invokeStatic(MethodMatcher, List)
	 */
	@SuppressWarnings("javadoc")
	public static <T> InvocationExpression<Void, T> invokeStatic(
			MethodMatcher<Void, T> executable,
			ValueExpression<?>... arguments) {
		return invokeStatic(executable, asList(arguments));
	}

	/**
	 * @param <T>
	 *          the type of the result of the execution
	 * @param executable
	 *          the executable to be invoked
	 * @param arguments
	 *          the expressions of the arguments of the invocation
	 * @return an expression describing the invocation of the given static
	 *         executable with the given argument expressions
	 */
	public static <T> InvocationExpression<Void, T> invokeStatic(
			MethodMatcher<Void, T> executable,
			List<ValueExpression<?>> arguments) {
		return new InvocationExpression<>(nullLiteral(), executable, arguments);
	}

	/**
	 * @see #invokeStatic(MethodMatcher, List)
	 */
	@SuppressWarnings("javadoc")
	public static <T> InvocationExpression<Void, T> invokeStatic(
			ExecutableToken<Void, T> executable,
			ValueExpression<?>... arguments) {
		return invokeStatic(executable, asList(arguments));
	}

	/**
	 * @param <T>
	 *          the type of the result of the execution
	 * @param executable
	 *          the executable to be invoked
	 * @param arguments
	 *          the expressions of the arguments of the invocation
	 * @return an expression describing the invocation of the given static
	 *         executable with the given argument expressions
	 */
	public static <T> InvocationExpression<Void, T> invokeStatic(
			ExecutableToken<Void, T> executable,
			List<ValueExpression<?>> arguments) {
		return new InvocationExpression<>(nullLiteral(), executable, arguments);
	}
}

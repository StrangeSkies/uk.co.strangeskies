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
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.token.test;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static uk.co.strangeskies.reflection.token.ExecutableToken.overConstructor;
import static uk.co.strangeskies.reflection.token.ExecutableToken.staticMethods;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeArgument;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;
import uk.co.strangeskies.reflection.token.test.ExecutableTokenTest.Outer.Inner;

@SuppressWarnings("javadoc")
public class ExecutableTokenTest {
	static class Outer<T> {
		class Inner<U extends T> {
			public Inner(U parameter) {}
		}

		public <U> void method() {}
	}

	@Test
	public void methodWithDefaultTypeArguments() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> method = ExecutableToken.overMethod(Outer.class.getMethod("method"));

		assertThat(
				method.getAllTypeArguments().map(TypeArgument::getType).collect(toList()),
				contains(instanceOf(TypeVariable.class)));
	}

	@Test
	public void constructorWithEnclosingTypeTest() {
		ExecutableToken
				.overInnerConstructor(Inner.class.getConstructors()[0])
				.withReceiverType(new TypeToken<Outer<Number>>() {})
				.withTargetType(new TypeToken<Outer<Number>.Inner<Integer>>() {});
	}

	@Test(expected = ReflectionException.class)
	public void constructorWithWrongEnclosingTypeTest() {
		ExecutableToken
				.overInnerConstructor(Inner.class.getConstructors()[0])
				.withReceiverType(new TypeToken<Outer<Number>>() {})
				.withTargetType(new TypeToken<Outer<Integer>.Inner<Integer>>() {});
	}

	@SuppressWarnings({ "rawtypes" })
	@Test
	public void constructorWithRawEnclosingTypeTest() {
		ExecutableToken
				.overConstructor(Inner.class.getConstructors()[0])
				.withReceiverType(new TypeToken<Outer<Number>>() {})
				.withTargetType(new TypeToken<Outer.Inner>() {});
	}

	@Test
	public void constructorWithUninferredEnclosingTypeTest() {
		ExecutableToken<?, ?> constructor = overConstructor(Inner.class.getConstructors()[0])
				.withReceiverType(new TypeToken<Outer<Number>>() {})
				.withTargetType(new @Infer TypeToken<Outer<? super Number>.Inner<Integer>>() {});

		assertThat(constructor.getReturnType().resolve(), equalTo(new TypeToken<Outer<Number>.Inner<Integer>>() {}));
	}

	@Test
	public void emptyVarargsInvocationTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<Void, ?> asList = ExecutableToken
				.overStaticMethod(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		List<?> list = (List<?>) asList.invoke(null);

		assertThat(list, empty());
	}

	@Test
	public void singleVarargsInvocationTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<Void, ?> asList = ExecutableToken
				.overStaticMethod(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		List<?> list = (List<?>) asList.invoke(null, "");

		assertThat(list, contains(""));
	}

	@Test
	public void varargsInvocationTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<Void, ?> asList = ExecutableToken
				.overStaticMethod(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		List<?> list = (List<?>) asList.invoke(null, "A", "B", "C");

		assertThat(list, contains("A", "B", "C"));
	}

	@Test
	public void emptyVarargsResolutionTest() {
		ExecutableToken<Void, ?> asList = staticMethods(Arrays.class).named("asList").resolveOverload();

		assertThat(asList.isVariableArityInvocation(), is(true));
	}

	@Test
	public void singleVarargsResolutionTest() {
		ExecutableToken<Void, ?> asList = staticMethods(Arrays.class).named("asList").resolveOverload(String.class);

		assertThat(asList.isVariableArityInvocation(), is(true));
	}

	@Test
	public void varargsResolutionTest() {
		ExecutableToken<Void, ?> asList = staticMethods(Arrays.class)
				.named("asList")
				.resolveOverload(String.class, String.class, String.class);

		assertThat(asList.isVariableArityInvocation(), is(true));
	}

	@Test
	public void varargsDefinitionTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<Void, ?> asList = ExecutableToken
				.overStaticMethod(Arrays.class.getMethod("asList", Object[].class));

		List<?> list = (List<?>) asList.invoke(null, new Object[] { new Object[] { "A", "B", "C" } });

		assertThat(list, contains("A", "B", "C"));
	}

	@Test
	public void varargsDefinitionResolutionTest() {
		ExecutableToken<Void, ?> asList = staticMethods(Arrays.class).named("asList").resolveOverload(
				new TypeToken<String[]>() {});

		assertThat(asList.isVariableArityInvocation(), is(false));
	}
}
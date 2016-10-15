/*
 * Copyright (C) 2016 ${copyright.holder.name} <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.reflection.codegen.test;

import static uk.co.strangeskies.reflection.codegen.ClassDeclaration.declareClass;

import java.util.Set;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.ExecutableToken;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.codegen.ClassDefinition;
import uk.co.strangeskies.reflection.codegen.MethodDeclaration;
import uk.co.strangeskies.reflection.codegen.VariableExpression;

@SuppressWarnings("javadoc")
public class ClassDefinitionTest {
	private static final String TEST_CLASS_NAME = ClassDefinitionTest.class.getPackage().getName() + ".SelfSet";
	private static final TypeToken<String> STRING_TYPE = new TypeToken<String>() {};

	public interface StringMethod {
		String method(String parameter);

		String method(Object parameter);
	}

	public interface NumberMethod<N> {
		Number method(N parameter);
	}

	public interface TMethod<T> {
		T method(String parameter);
	}

	public interface NumberMethodSubType extends NumberMethod<String> {}

	@Test
	public void runnableClassInvocationTest() {
		ClassDefinition<? extends Runnable> classDefinition = declareClass(TEST_CLASS_NAME).withSuperType(Runnable.class)
				.define();

		classDefinition.declareMethod("run").define();

		Runnable instance = classDefinition.instantiate().cast();

		instance.run();
	}

	@Test
	public void functionClassInvocationTest() {
		ExecutableToken<? super String, String> concatMethod = STRING_TYPE.getMethods().named("concat")
				.resolveOverload(STRING_TYPE).withTargetType(STRING_TYPE);

		ClassDefinition<? extends Function<String, String>> classDefinition = declareClass(TEST_CLASS_NAME)
				.withSuperType(new TypeToken<Function<String, String>>() {}).define();

		MethodDeclaration<? extends Function<String, String>, String> applyMethod = classDefinition.declareMethod("apply")
				.withReturnType(STRING_TYPE);
		VariableExpression<String> parameter = applyMethod.addParameter(STRING_TYPE);
		applyMethod.define().body().addExpression(parameter.assign(parameter.invokeMethod(concatMethod, parameter)))
				.addReturnStatement(parameter);

		Function<String, String> instance = classDefinition.instantiate().cast();

		String result = instance.apply("string");

		Assert.assertEquals("stringstring", result);
	}

	@Test(expected = ReflectionException.class)
	public void invalidSuperTypeTest() {
		ClassDefinition<? extends Set<?>> classDefinition = declareClass(TEST_CLASS_NAME)
				.withSuperType(new TypeToken<Set<String>>() {}, new TypeToken<Set<Number>>() {}).define();
		classDefinition.validate();
	}

	@Test(expected = ReflectionException.class)
	public void inheritedMethodCollisionTest() {
		declareClass(TEST_CLASS_NAME)
				.withSuperType(new TypeToken<StringMethod>() {}, new TypeToken<NumberMethod<String>>() {}).define().validate();
	}

	@Test(expected = ReflectionException.class)
	public void inheritedMethodCollisionAvoidenceTest() {
		declareClass(TEST_CLASS_NAME)
				.withSuperType(new TypeToken<StringMethod>() {}, new TypeToken<NumberMethod<Integer>>() {}).define().validate();
	}

	@Test(expected = ReflectionException.class)
	public void indirectlyInheritedMethodCollisionTest() {
		declareClass(TEST_CLASS_NAME)
				.withSuperType(new TypeToken<StringMethod>() {}, new TypeToken<NumberMethodSubType>() {}).define().validate();
	}
}
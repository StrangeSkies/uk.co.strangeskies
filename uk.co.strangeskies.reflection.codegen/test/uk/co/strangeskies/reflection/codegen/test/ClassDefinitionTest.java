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
package uk.co.strangeskies.reflection.codegen.test;

import static uk.co.strangeskies.reflection.codegen.ClassSignature.classSignature;
import static uk.co.strangeskies.reflection.codegen.Expressions.literal;
import static uk.co.strangeskies.reflection.codegen.MethodSignature.methodSignature;
import static uk.co.strangeskies.reflection.codegen.ParameterSignature.parameterSignature;
import static uk.co.strangeskies.reflection.token.MethodMatcher.matchMethod;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.codegen.Block;
import uk.co.strangeskies.reflection.codegen.ClassDefinition;
import uk.co.strangeskies.reflection.codegen.ClassSignature;
import uk.co.strangeskies.reflection.codegen.ParameterSignature;
import uk.co.strangeskies.reflection.token.TypeToken;

@SuppressWarnings("javadoc")
public class ClassDefinitionTest {
	private interface Func<A, B> {
		B apply(A value);
	}

	private interface Default {
		@SuppressWarnings("unused")
		default void method() {}
	}

	private static final ClassSignature<?> TEST_CLASS_SIGNATURE = classSignature()
			.packageName(ClassDeclarationTest.class.getPackage().getName())
			.simpleName("SelfSet");

	private static final TypeToken<String> STRING_TYPE = new TypeToken<String>() {};

	@Test
	public void defineObject()
			throws InstantiationException, IllegalAccessException {
		Object instance = TEST_CLASS_SIGNATURE
				.defineStandalone()
				.generateClass()
				.newInstance();

		instance.hashCode();
	}

	@Test
	public void runnableClassInvocation()
			throws InstantiationException, IllegalAccessException {
		ClassDefinition<Void, ? extends Runnable> classDefinition = TEST_CLASS_SIGNATURE
				.extending(Runnable.class)
				.defineStandalone()
				.defineMethod(
						matchMethod().named("run"),
						new Block<Void>().withReturnStatement());

		Runnable instance = classDefinition.generateClass().newInstance();

		instance.run();
	}

	@Test
	public void defineWithExplicitMethodDeclaration()
			throws InstantiationException, IllegalAccessException {
		ParameterSignature<String> applyParameter = parameterSignature(
				"value",
				STRING_TYPE);

		/*
		 * A block is something we don't always know the type of until we resolve
		 * the type of it's context, e.g. in method overload resolution a lambda
		 * body must be reinterpreted according to all applicable target types, and
		 * any return statements examined according to that context.
		 * 
		 * Do we achieve this with a new kind of stand-in type? Or do existing
		 * models already fit well (e.g. some sort of ad-hoc type variable)?
		 */

		Func<String, String> instance = TEST_CLASS_SIGNATURE
				.extending(new TypeToken<Func<String, String>>() {})
				.method(
						methodSignature("apply").withReturnType(STRING_TYPE).withParameters(
								applyParameter))
				.defineStandalone()
				.defineMethod(
						matchMethod().named("apply"),
						d -> new Block<String>().withReturnStatement(
								d.getParameter(applyParameter).invokeMethod(
										matchMethod().named("concat").returning(String.class),
										literal("append"))))
				.generateClass()
				.newInstance();

		String result = instance.apply("string");

		Assert.assertEquals("stringappend", result);
	}

	@Test
	public void defineWithInheritedMethodDeclarationBySignature()
			throws InstantiationException, IllegalAccessException {
		ParameterSignature<String> applyParameter = parameterSignature(
				"value",
				STRING_TYPE);

		Func<String, String> instance = TEST_CLASS_SIGNATURE
				.extending(new TypeToken<Func<String, String>>() {})
				.defineStandalone()
				.defineMethod(
						matchMethod().named("apply").returning(String.class),
						d -> new Block<String>().withReturnStatement(
								d.getParameter(applyParameter).invokeMethod(
										matchMethod().named("concat").returning(String.class),
										d.getParameter(parameterSignature("", Integer.class)))))
				.generateClass()
				.newInstance();

		String result = instance.apply("string");

		Assert.assertEquals("stringstring", result);
	}

	@Test
	public void defineWithInheritedMethodDeclaration()
			throws InstantiationException, IllegalAccessException {
		ParameterSignature<String> applyParameter = parameterSignature(
				"value",
				STRING_TYPE);

		Func<String, String> instance = TEST_CLASS_SIGNATURE
				.extending(new TypeToken<Func<String, String>>() {})
				.defineStandalone()
				.defineMethod(
						matchMethod().named("apply").returning(String.class),
						d -> new Block<String>().withReturnStatement(
								d.getParameter(applyParameter).invokeMethod(
										matchMethod().named("concat").returning(String.class),
										d.getParameter(applyParameter))))
				.generateClass()
				.newInstance();

		String result = instance.apply("string");

		Assert.assertEquals("stringstring", result);
	}

	@Test(expected = ReflectionException.class)
	public void defineWithAbstractMethod()
			throws InstantiationException, IllegalAccessException {
		TEST_CLASS_SIGNATURE
				.extending(Runnable.class)
				.defineStandalone()
				.generateClass()
				.newInstance();
	}

	@Test
	public void defineWithDefaultMethod()
			throws InstantiationException, IllegalAccessException {
		TEST_CLASS_SIGNATURE
				.extending(Default.class)
				.defineStandalone()
				.generateClass()
				.newInstance();
	}
}

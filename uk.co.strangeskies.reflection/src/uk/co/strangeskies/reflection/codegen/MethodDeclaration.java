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
package uk.co.strangeskies.reflection.codegen;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.token.TypeToken;

public class MethodDeclaration<C, T> extends ParameterizedDeclaration implements MemberDeclaration<C> {
	private final ClassDefinition<C> classDefinition;
	private final String methodName;

	private final Map<VariableExpressionProxy<?>, AnnotatedType> parameters;
	private AnnotatedType returnType;

	protected MethodDeclaration(ClassDefinition<C> classDefinition, String methodName, TypeToken<T> returnType) {
		this.classDefinition = classDefinition;
		this.methodName = methodName;
		this.returnType = returnType.getAnnotatedDeclaration();

		parameters = new LinkedHashMap<>();
	}

	protected static <C> MethodDeclaration<C, Void> declareMethod(ClassDefinition<C> classDefinition, String methodName) {
		return new MethodDeclaration<>(classDefinition, methodName, TypeToken.overType(void.class));
	}

	@Override
	public ClassDefinition<C> getClassDefinition() {
		return classDefinition;
	}

	public Set<VariableExpressionProxy<?>> getParameters() {
		return parameters.keySet();
	}

	public AnnotatedType getParameterType(VariableExpressionProxy<?> parameter) {
		return parameters.get(parameter);
	}

	@Override
	public String getName() {
		return methodName;
	}

	public AnnotatedType getReturnType() {
		return returnType;
	}

	@Override
	public MethodDefinition<C, T> define() {
		return new InstanceMethodDefinition<>(this);
	}

	protected MethodDefinition<C, T> defineInstance() {
		return new InstanceMethodDefinition<>(this);
	}

	public VariableExpression<?> addParameter(AnnotatedType type) {
		VariableExpressionProxy<?> proxy = new VariableExpressionProxy<>();
		parameters.put(proxy, type);
		return proxy;
	}

	public VariableExpression<?> addParameter(Type type) {
		return addParameter(AnnotatedTypes.over(type));
	}

	@SuppressWarnings("unchecked")
	public <U> VariableExpression<U> addParameter(Class<U> type) {
		return (VariableExpression<U>) addParameter(AnnotatedTypes.over(type));
	}

	@SuppressWarnings("unchecked")
	public <U> VariableExpression<U> addParameter(TypeToken<U> type) {
		return (VariableExpression<U>) addParameter(type.getAnnotatedDeclaration());
	}

	public MethodDeclaration<C, T> withParameters(Collection<AnnotatedType> types) {
		for (AnnotatedType type : types) {
			addParameter(type);
		}
		return this;
	}

	public MethodDeclaration<C, T> withParameters(AnnotatedType... types) {
		return withParameters(Arrays.asList(types));
	}

	public MethodDeclaration<C, T> withParameters(Type... types) {
		return withParameters(AnnotatedTypes.over(types));
	}

	public MethodDeclaration<C, T> withParameters(TypeToken<?>... types) {
		return withParameters(Arrays.stream(types).map(TypeToken::getAnnotatedDeclaration).collect(Collectors.toList()));
	}

	public MethodDeclaration<C, ?> withReturnType(AnnotatedType type) {
		returnType = type;
		return this;
	}

	public MethodDeclaration<C, ?> withReturnType(Type type) {
		return withReturnType(AnnotatedTypes.over(type));
	}

	@SuppressWarnings("unchecked")
	public <U> MethodDeclaration<C, U> withReturnType(Class<U> type) {
		return (MethodDeclaration<C, U>) withReturnType(AnnotatedTypes.over(type));
	}

	@SuppressWarnings("unchecked")
	public <U> MethodDeclaration<C, U> withReturnType(TypeToken<U> type) {
		return (MethodDeclaration<C, U>) withReturnType(type.getAnnotatedDeclaration());
	}
}
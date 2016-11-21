/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.reflection.token;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.LOOSE_COMPATIBILILTY;
import static uk.co.strangeskies.reflection.token.ExecutableTokenStream.executableStream;
import static uk.co.strangeskies.reflection.token.TypeToken.overType;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.ConstraintFormula;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.ParameterizedTypes;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.TypeResolver;
import uk.co.strangeskies.reflection.TypeSubstitution;
import uk.co.strangeskies.reflection.TypeVariableCapture;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.reflection.token.TypeToken.Wildcards;

/**
 * <p>
 * A type safe wrapper around {@link Executable} instances, with proper handling
 * of generic methods, and methods on generic classes.
 * 
 * <p>
 * {@link ExecutableToken executable members} may be created over types which
 * mention inference variables, or even over inference variables themselves.
 * 
 * @author Elias N Vasylenko
 *
 * @param <O>
 *          the receiver type of the executable
 * @param <R>
 *          the return type of the executable
 */
public class ExecutableToken<O, R> implements MemberToken<O> {
	private final BoundSet bounds;

	private final TypeToken<O> receiverType;
	private final TypeToken<R> returnType;
	private final Executable executable;

	private final Map<TypeVariable<?>, Type> typeArguments;
	private final List<Type> methodTypeArguments;

	private final List<ExecutableParameter> parameters;

	private final BiFunction<Object, List<?>, R> invocationFunction;

	private final boolean variableArityInvocation;

	private ExecutableToken(
			BoundSet bounds,
			TypeToken<O> receiverType,
			TypeToken<R> returnType,
			List<Type> methodTypeArguments,
			Executable executable,
			BiFunction<Object, List<?>, R> invocationFunction,
			boolean variableArityInvocation) {
		this.executable = executable;
		this.invocationFunction = invocationFunction;
		this.variableArityInvocation = variableArityInvocation;

		TypeResolver resolver = new TypeResolver(bounds);

		/*
		 * Incorporate relevant type parameters:
		 */
		this.typeArguments = determineContainerTypeArguments(resolver, receiverType, returnType, methodTypeArguments);
		this.methodTypeArguments = stream(executable.getTypeParameters()).map(typeArguments::get).collect(toList());

		TypeSubstitution inferenceVariableSubstitution = new TypeSubstitution(typeArguments);

		/*
		 * Resolve types within context of given Resolver:
		 */
		this.receiverType = determineReceiverType(resolver, inferenceVariableSubstitution, receiverType);
		this.returnType = determineReturnType(resolver, inferenceVariableSubstitution, returnType);
		this.parameters = determineParameterTypes(inferenceVariableSubstitution);

		this.bounds = resolver.getBounds();
	}

	private Map<TypeVariable<?>, Type> determineContainerTypeArguments(
			TypeResolver resolver,
			TypeToken<O> receiverType,
			TypeToken<R> returnType,
			List<Type> methodTypeArguments) {
		TypeToken<?> containerType = isConstructor() ? returnType : receiverType;
		Type containerSuperType;
		if (isConstructor()) {
			containerSuperType = containerType.resolveSupertype(executable.getDeclaringClass()).getType();
		} else if (receiverType.getType() == void.class) {
			containerSuperType = void.class;
		} else if (isStatic()) {
			containerSuperType = containerType.resolveSupertype(executable.getDeclaringClass().getEnclosingClass()).getType();
		} else {
			containerSuperType = containerType.resolveSupertype(executable.getDeclaringClass()).getType();
		}

		if (containerSuperType instanceof Class<?>) {
			if (Types.isGeneric((Class<?>) containerSuperType)) {
				// raw type
				return null;
			} else {
				// non-generic class
				return resolver.inferOverTypeParameters(getMember()).collect(toMap(Entry::getKey, Entry::getValue));
			}
		} else {
			// generic type
			Map<TypeVariable<?>, Type> arguments = ParameterizedTypes
					.getAllTypeArguments((ParameterizedType) containerSuperType)
					.collect(toMap(Entry::getKey, e -> resolver.resolveType(e.getValue())));

			if (!(containerType.getType() instanceof InferenceVariable)
					&& arguments.values().stream().anyMatch(WildcardType.class::isInstance)) {
				/*
				 * TODO this should actually check if there were any inference variables
				 * in the type hierarchy between containerType and containerSuperType,
				 * as it should still pass if the containerType is e.g. an intersection
				 * containing an inference variables with an upper bound on
				 * containerSuperClass.
				 */
				throw new ReflectionException(p -> p.cannotResolveInvocationOnTypeWithWildcardParameters(containerType));
			}

			if (methodTypeArguments != null) {
				for (int i = 0; i < methodTypeArguments.size(); i++) {
					arguments.put(executable.getTypeParameters()[i], methodTypeArguments.get(i));
				}
			}

			resolver.inferOverTypeParameters(getMember(), arguments).forEach(i -> arguments.put(i.getKey(), i.getValue()));

			return arguments;
		}
	}

	private TypeToken<O> determineReceiverType(
			TypeResolver resolver,
			TypeSubstitution inferenceVariables,
			TypeToken<O> receiverType) {
		if (!receiverType.getType().equals(void.class)) {
			Class<?> receiverClass;

			if (executable instanceof Method && !isStatic()) {
				receiverClass = executable.getDeclaringClass();
			} else {
				receiverClass = executable.getDeclaringClass().getEnclosingClass();
			}

			if (!receiverType.getRawType().equals(receiverClass)) {
				receiverType.incorporateInto(resolver);
			}

			receiverType = receiverType.withBounds(resolver.getBounds()).resolve();
		}

		return receiverType.resolve();
	}

	@SuppressWarnings("unchecked")
	private TypeToken<R> determineReturnType(
			TypeResolver resolver,
			TypeSubstitution inferenceVariables,
			TypeToken<R> returnType) {
		if (returnType != null) {
			returnType = returnType.withBounds(resolver.getBounds());
		} else {
			Type genericReturnType = inferenceVariables.resolve(((Method) executable).getGenericReturnType());

			/*
			 * TODO should this always be PRESERVE? Or:
			 * 
			 * InferenceVariable.isProperType(genericReturnType) ? Wildcards.PRESERVE
			 * : Wildcards.INFER
			 */
			returnType = (TypeToken<R>) overType(resolver.getBounds(), genericReturnType, Wildcards.RETAIN);
		}

		return returnType.resolve();
	}

	private List<ExecutableParameter> determineParameterTypes(TypeSubstitution inferenceVariables) {
		Type[] genericParameters;
		if (isRaw()) {
			genericParameters = executable.getParameterTypes();
		} else {
			genericParameters = executable.getGenericParameterTypes();
			for (int i = 0; i < genericParameters.length; i++) {
				genericParameters[i] = inferenceVariables.resolve(genericParameters[i]);
			}
		}

		return IntStream
				.range(0, genericParameters.length)
				.mapToObj(i -> new ExecutableParameter(executable.getParameters()[i].getName(), genericParameters[i]))
				.collect(toList());
	}

	/**
	 * Create a new {@link ExecutableToken} instance from a reference to a
	 * {@link Constructor}.
	 * 
	 * @param <T>
	 *          The type of the given {@link Constructor}.
	 * @param constructor
	 *          The constructor to wrap.
	 * @return An executable member wrapping the given constructor.
	 */
	public static <T> ExecutableToken<Void, T> overConstructor(Constructor<T> constructor) {
		return overConstructor(constructor, TypeToken.overType(constructor.getDeclaringClass()));
	}

	/**
	 * Create a new {@link ExecutableToken} instance from a reference to a
	 * {@link Constructor}.
	 * 
	 * @param <T>
	 *          the exact type of the constructor
	 * @param constructor
	 *          the constructor to wrap
	 * @param instance
	 *          the type of the instantiated instance
	 * @return an executable member wrapping the given constructor
	 */
	@SuppressWarnings("unchecked")
	public static <T> ExecutableToken<Void, T> overConstructor(
			Constructor<? super T> constructor,
			TypeToken<T> instance) {
		return new ExecutableToken<>(instance.getBounds(), TypeToken.overType(void.class), instance, null, constructor,
				(Object r, List<?> a) -> {
					try {
						return (T) constructor.newInstance(a.toArray());
					} catch (Exception e) {
						throw new ReflectionException(p -> p.invalidConstructorArguments(constructor, instance.getType(), a), e);
					}
				}, false);
	}

	/**
	 * Create a new {@link ExecutableToken} instance from a reference to a
	 * {@link Constructor}.
	 * 
	 * @param <T>
	 *          the exact type of the constructor
	 * @param constructor
	 *          the constructor to wrap
	 * @param enclosingInstance
	 *          the receiving type for a constructor on an inner class
	 * @param instance
	 *          the type of the instantiated instance
	 * @return an executable member wrapping the given constructor
	 */
	@SuppressWarnings("unchecked")
	public static <E, T> ExecutableToken<E, T> overConstructor(
			Constructor<? super T> constructor,
			TypeToken<E> enclosingInstance,
			TypeToken<T> instance) {

		enclosingInstance = (TypeToken<E>) instance.getEnclosingType().withConstraintFrom(Kind.SUBTYPE, enclosingInstance);
		instance.withBounds(enclosingInstance.getBounds());

		return new ExecutableToken<>(enclosingInstance.getBounds(), enclosingInstance, instance, null, constructor,
				(Object r, List<?> a) -> {
					try {
						Object[] arguments = new Object[a.size() + 1];
						arguments[0] = r;
						Iterator<?> argumentIterator = a.iterator();
						for (int i = 1; i <= a.size(); i++) {
							arguments[i] = argumentIterator.next();
						}
						return (T) constructor.newInstance(arguments);
					} catch (Exception e) {
						throw new ReflectionException(p -> p.invalidConstructorArguments(constructor, instance.getType(), a), e);
					}
				}, false);
	}

	/**
	 * Create a new {@link ExecutableToken} instance from a reference to a
	 * {@link Method}.
	 * 
	 * @param method
	 *          The method to wrap.
	 * @return An executable member wrapping the given method.
	 */
	public static ExecutableToken<Void, ?> overStaticMethod(Method method) {
		return new ExecutableToken<>(new BoundSet(), TypeToken.overType(void.class), null, null, method,
				(Object r, List<?> a) -> {
					try {
						return method.invoke(null, a.toArray());
					} catch (Exception e) {
						throw new ReflectionException(p -> p.invalidStaticMethodArguments(method, a), e);
					}
				}, false);
	}

	/**
	 * Create a new {@link ExecutableToken} instance from a reference to a
	 * {@link Method}.
	 * 
	 * @param method
	 *          The method to wrap.
	 * @return An executable member wrapping the given method.
	 */
	public static ExecutableToken<?, ?> overMethod(Method method) {
		TypeToken<?> receiver = TypeToken.overType(method.getDeclaringClass());
		return overMethod(method, receiver);
	}

	/**
	 * Create a new {@link ExecutableToken} instance from a reference to a
	 * {@link Constructor}.
	 * 
	 * @param <T>
	 *          The exact type of the method receiver.
	 * @param method
	 *          The method to wrap.
	 * @param receiver
	 *          The target type of the given {@link Method}.
	 * @return An executable member wrapping the given method.
	 */
	public static <T> ExecutableToken<T, ?> overMethod(Method method, TypeToken<T> receiver) {
		return new ExecutableToken<>(receiver.getBounds(), receiver, null, null, method, (Object r, List<?> a) -> {
			try {
				return method.invoke(r, a.toArray());
			} catch (Exception e) {
				throw new ReflectionException(p -> p.invalidMethodArguments(method, receiver.getType(), a), e);
			}
		}, false);
	}

	/**
	 * @return the name of the executable member
	 */
	@Override
	public String getName() {
		return getMember().getName();
	}

	@Override
	public BoundSet getBounds() {
		return bounds;
	}

	@Override
	public String toString() {
		return toString(parameters);
	}

	private String toString(List<ExecutableParameter> parameters) {
		StringBuilder builder = new StringBuilder();

		if (isPrivate())
			builder.append("private ");
		else if (isProtected())
			builder.append("protected ");
		else if (isPublic())
			builder.append("public ");

		if (isNative())
			builder.append("native ");
		if (isStatic())
			builder.append("static ");
		if (isStrict())
			builder.append("strictfp ");
		if (isSynchronized())
			builder.append("synchronized ");

		if (isAbstract())
			builder.append("abstract ");
		else if (isFinal())
			builder.append("final ");

		if (isGeneric()) {
			builder
					.append("<")
					.append(getAllTypeParameters()
							.map(getAllTypeArguments().collect(toMap(Entry::getKey, Entry::getValue))::get)
							.map(Objects::toString)
							.collect(Collectors.joining(", ")))
					.append("> ");
		}

		builder.append(returnType).toString();
		if (executable instanceof Method)
			builder.append(" ").append(receiverType).append(".").append(executable.getName());

		return builder
				.append("(")
				.append(parameters.stream().map(Objects::toString).collect(Collectors.joining(", ")))
				.append(")")
				.toString();
	}

	/**
	 * @return true if the wrapped executable is abstract, false otherwise
	 */
	public boolean isAbstract() {
		return Modifier.isAbstract(executable.getModifiers());
	}

	@Override
	public boolean isFinal() {
		return Modifier.isFinal(executable.getModifiers());
	}

	/**
	 * @return true if the wrapped executable is native, false otherwise
	 */
	public boolean isNative() {
		return Modifier.isNative(executable.getModifiers());
	}

	@Override
	public boolean isPrivate() {
		return Modifier.isPrivate(executable.getModifiers());
	}

	@Override
	public boolean isProtected() {
		return Modifier.isProtected(executable.getModifiers());
	}

	@Override
	public boolean isPublic() {
		return Modifier.isPublic(executable.getModifiers());
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(executable.getModifiers());
	}

	/**
	 * @return true if the executable is a constructor, false otherwise
	 */
	public boolean isConstructor() {
		return executable instanceof Constructor<?>;
	}

	/**
	 * @return true if the executable is a method, false otherwise
	 */
	public boolean isMethod() {
		return executable instanceof Method;
	}

	/**
	 * @return true if the wrapped executable is strict, false otherwise
	 */
	public boolean isStrict() {
		return Modifier.isStrict(executable.getModifiers());
	}

	/**
	 * @return true if the wrapped executable is synchronized, false otherwise
	 */
	public boolean isSynchronized() {
		return Modifier.isSynchronized(executable.getModifiers());
	}

	/**
	 * @return true if the wrapped executable is generic, false otherwise
	 */
	public boolean isGeneric() {
		return executable.getTypeParameters().length > 0;
	}

	/**
	 * @return true if the wrapped executable is variable arity, false otherwise
	 */
	public boolean isVariableArityDefinition() {
		return executable.isVarArgs();
	}

	/**
	 * Check whether the executable is flagged to be invoked with varargs. If an
	 * executable is flagged to be invoked with varargs, then the
	 * {@link #invoke(Object, List) invocation} will be made by putting trailing
	 * arguments into an array as per Java variable arity method invocation rules.
	 * 
	 * @return true if the executable is flagged to be invoked with varargs, false
	 *         otherwise
	 */
	public boolean isVariableArityInvocation() {
		return variableArityInvocation;
	}

	/**
	 * @return copy of the {@link ExecutableToken} flagged to be invoked with
	 *         {@link #isVariableArityInvocation() variable arity}
	 */
	public ExecutableToken<O, R> asVariableArityInvocation() {
		if (isVariableArityInvocation()) {
			return this;
		} else if (!isVariableArityDefinition()) {
			throw new ReflectionException(p -> p.invalidVariableArityInvocation(getMember()));
		} else {
			return new ExecutableToken<>(bounds, receiverType, returnType, methodTypeArguments, executable,
					invocationFunction, true);
		}
	}

	/**
	 * @return The executable represented by this {@link ExecutableToken}.
	 */
	@Override
	public Executable getMember() {
		return executable;
	}

	@Override
	public TypeToken<O> getReceiverType() {
		return receiverType;
	}

	/**
	 * @return The exact return type of this executable member instance. Generic
	 *         type parameters may include inference variables.
	 */
	public TypeToken<R> getReturnType() {
		return returnType;
	}

	/**
	 * @return The exact types of the expected parameters of this executable
	 *         member instance. Inference variables may be mentioned.
	 */
	public Stream<ExecutableParameter> getParameters() {
		return parameters.stream();
	}

	/**
	 * Derive a new {@link ExecutableToken} instance, with the given bounds
	 * incorporated into the bounds of the underlying resolver. The original
	 * {@link ExecutableToken} will remain unmodified.
	 * 
	 * @param bounds
	 *          The new bounds to incorporate.
	 * @return The newly derived {@link ExecutableToken}.
	 */
	@Override
	public ExecutableToken<O, R> withBounds(BoundSet bounds) {
		return withBounds(bounds, bounds.getInferenceVariables().collect(toList()));
	}

	/**
	 * Derive a new {@link ExecutableToken} instance, with the bounds on the given
	 * inference variables, with respect to the given bound set, incorporated into
	 * the bounds of the underlying resolver. The original {@link ExecutableToken}
	 * will remain unmodified.
	 * 
	 * @param bounds
	 *          The new bounds to incorporate.
	 * @param inferenceVariables
	 *          The new inference variables whose bounds are to be incorporated.
	 * @return The newly derived {@link ExecutableToken}.
	 */
	@Override
	public ExecutableToken<O, R> withBounds(BoundSet bounds, Collection<? extends InferenceVariable> inferenceVariables) {
		bounds = getBounds().withIncorporated(bounds, inferenceVariables);

		return new ExecutableToken<>(bounds, receiverType, returnType, methodTypeArguments, executable, invocationFunction,
				variableArityInvocation);
	}

	/**
	 * Derive a new {@link ExecutableToken} instance, with the bounds on the given
	 * type incorporated into the bounds of the underlying resolver. The original
	 * {@link ExecutableToken} will remain unmodified.
	 * 
	 * @param type
	 *          The type whose bounds are to be incorporated.
	 * @return The newly derived {@link ExecutableToken}.
	 */
	@Override
	public ExecutableToken<O, R> withBoundsFrom(TypeToken<?> type) {
		return withBounds(type.getBounds(), type.getInferenceVariablesMentioned().collect(toList()));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U extends O> ExecutableToken<U, ? extends R> withReceiverType(TypeToken<U> type) {
		try {
			BoundSet bounds = type.incorporateInto(getBounds());
			bounds = new ConstraintFormula(Kind.SUBTYPE, type.getType(), receiverType.getType()).reduce(bounds);

			Class<?> mostSpecificOverridingClass = this.executable.getDeclaringClass();
			mostSpecificOverridingClass = type.getRawTypes().reduce(mostSpecificOverridingClass,
					(a, b) -> a.isAssignableFrom(b) ? b : a);

			Executable override = mostSpecificOverridingClass.equals(executable.getDeclaringClass()) ? executable
					: mostSpecificOverridingClass.getMethod(executable.getName(), executable.getParameterTypes());

			return new ExecutableToken<>(bounds, type, null, methodTypeArguments, override, invocationFunction,
					variableArityInvocation);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new ReflectionException(p -> p.cannotResolveOverride(getMember(), type.getType()), e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public ExecutableToken<? extends O, ? extends R> withReceiverType(Type type) {
		return withReceiverType((TypeToken<? extends O>) overType(type));
	}

	/**
	 * Derive a new instance of {@link ExecutableToken} with the given target
	 * type.
	 * 
	 * <p>
	 * The new {@link ExecutableToken} will always have a target type which is as
	 * or more specific than both the current target type <em>and</em> the given
	 * type. This means that the new target will be assignment compatible with the
	 * given type, but if the given type contains wildcards or inference variables
	 * which are less specific that those implied by the <em>current</em> target
	 * type, new type arguments will be inferred in their place, or further bounds
	 * may be added to them.
	 * 
	 * @param <S>
	 *          The derived {@link ExecutableToken} must be assignment compatible
	 *          with this type.
	 * @param target
	 *          The derived {@link ExecutableToken} must be assignment compatible
	 *          with this type.
	 * @return A new {@link ExecutableToken} compatible with the given target
	 *         type.
	 * 
	 *         <p>
	 *         The new target type will not be effectively more specific than the
	 *         intersection type of the current target type and the given type.
	 *         That is, any type which can be assigned to both the given type and
	 *         the current target type, will also be assignable to the new type.
	 */
	public <S extends R> ExecutableToken<O, S> withTargetType(Class<S> target) {
		return withTargetType(TypeToken.overType(target));
	}

	/**
	 * Derive a new instance of {@link ExecutableToken} with the given target
	 * type.
	 * 
	 * <p>
	 * The new {@link ExecutableToken} will always have a target type which is as
	 * or more specific than both the current target type <em>and</em> the given
	 * type. This means that the new target will be assignment compatible with the
	 * given type, but if the given type contains wildcards or inference variables
	 * which are less specific that those implied by the <em>current</em> target
	 * type, new type arguments will be inferred in their place, or further bounds
	 * may be added to them.
	 * 
	 * @param <S>
	 *          The derived {@link ExecutableToken} must be assignment compatible
	 *          with this type.
	 * @param target
	 *          The derived {@link ExecutableToken} must be assignment compatible
	 *          with this type.
	 * @return A new {@link ExecutableToken} compatible with the given target
	 *         type.
	 * 
	 *         <p>
	 *         The new target type will not be effectively more specific than the
	 *         intersection type of the current target type and the given type.
	 *         That is, any type which can be assigned to both the given type and
	 *         the current target type, will also be assignable to the new type.
	 */
	@SuppressWarnings("unchecked")
	public <S> ExecutableToken<O, S> withTargetType(TypeToken<S> target) {
		if (target == null)
			return (ExecutableToken<O, S>) this;

		return (ExecutableToken<O, S>) withBoundsFrom(target).withTargetType(target.getType());
	}

	/**
	 * Derive a new instance of {@link ExecutableToken} with the given target
	 * type.
	 * 
	 * <p>
	 * The new {@link ExecutableToken} will always have a target type which is as
	 * or more specific than both the current target type <em>and</em> the given
	 * type. This means that the new target will be assignment compatible with the
	 * given type, but if the given type contains wildcards or inference variables
	 * which are less specific that those implied by the <em>current</em> target
	 * type, new type arguments will be inferred in their place, or further bounds
	 * may be added to them.
	 * 
	 * @param target
	 *          The derived {@link ExecutableToken} must be assignment compatible
	 *          with this type.
	 * @return A new {@link ExecutableToken} compatible with the given target
	 *         type.
	 * 
	 *         <p>
	 *         The new target type will not be effectively more specific than the
	 *         intersection type of the current target type and the given type.
	 *         That is, any type which can be assigned to both the given type and
	 *         the current target type, will also be assignable to the new type.
	 */
	public ExecutableToken<O, ? extends R> withTargetType(Type target) {
		return withTargetTypeCapture(target);
	}

	@SuppressWarnings("unchecked")
	private <S extends R> ExecutableToken<O, S> withTargetTypeCapture(Type target) {
		if (target == null)
			return (ExecutableToken<O, S>) this;

		BoundSet bounds = new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY, returnType.getType(), target)
				.reduce(getBounds());

		return new ExecutableToken<>(bounds, receiverType, (TypeToken<S>) returnType, methodTypeArguments, executable,
				(BiFunction<Object, List<?>, S>) invocationFunction, variableArityInvocation);
	}

	/**
	 * Derive a new {@link ExecutableToken} fulfilling two conditions.
	 * 
	 * <ul>
	 * <li>Firstly, that the result be assignment compatible with the given target
	 * type.</li>
	 * <li>Secondly, that the arguments are compatible in either a
	 * {@link #withLooseApplicability(List) loose invocation context}, or failing
	 * that, a {@link #withVariableArityApplicability(List) variable arity
	 * invocation context}.</li>
	 * </ul>
	 * 
	 * <p>
	 * This method uses the same type inference algorithm as the Java language,
	 * and so will only fail in those cases where the Java compiler would have
	 * failed to infer a type.
	 * 
	 * @param <U>
	 *          The derived {@link ExecutableToken} must be assignment compatible
	 *          with this type.
	 * @param targetType
	 *          The derived {@link ExecutableToken} must be assignment compatible
	 *          with this type.
	 * @param arguments
	 *          The derived {@link ExecutableToken} must be loose invocation
	 *          compatible, or failing that variable arity compatible, with the
	 *          given arguments.
	 * @return A new {@link ExecutableToken} compatible with the given target type
	 *         and parameters, and which has more specific arguments, type
	 *         arguments, and return type than the receiving
	 *         {@link ExecutableToken}.
	 */
	public <U> ExecutableToken<O, U> withInferredType(TypeToken<U> targetType, TypeToken<?>... arguments) {
		ExecutableToken<O, R> executableMember;
		try {
			executableMember = withLooseApplicability(arguments);
		} catch (Exception e) {
			if (isVariableArityDefinition())
				executableMember = withVariableArityApplicability(arguments);
			else
				throw e;
		}
		return executableMember.withTargetType(targetType);
	}

	/**
	 * Derived a new {@link ExecutableToken} instance with generic method
	 * parameters inferred, and if this is a member of a generic type, with
	 * generic type parameters inferred, too.
	 * 
	 * @return The derived {@link ExecutableToken} with inferred invocation type.
	 */
	@Override
	public ExecutableToken<O, R> infer() {
		TypeResolver resolver = new TypeResolver(getBounds());
		resolver.infer();

		return new ExecutableToken<>(resolver.getBounds(), receiverType, returnType, methodTypeArguments, executable,
				invocationFunction, variableArityInvocation);
	}

	/**
	 * @return A new derived {@link ExecutableToken} instance with generic method
	 *         parameters inferred, and if this is a constructor on a generic
	 *         type, with generic type parameters inferred, also.
	 */
	public ExecutableToken<O, R> inferParameterTypes() {
		TypeResolver resolver = new TypeResolver(getBounds());

		for (ExecutableParameter parameter : parameters)
			resolver.infer(parameter.getType());

		return new ExecutableToken<>(resolver.getBounds(), receiverType, returnType, methodTypeArguments, executable,
				invocationFunction, variableArityInvocation);
	}

	/**
	 * Derive a new {@link ExecutableToken} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a strict invocation
	 * context. Where necessary, the derived {@link ExecutableToken} may infer new
	 * bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableToken}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a strict compatibility invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableToken}.
	 */
	public ExecutableToken<O, R> withStrictApplicability(TypeToken<?>... arguments) {
		return withStrictApplicability(Arrays.asList(arguments));
	}

	/**
	 * Derive a new {@link ExecutableToken} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a strict invocation
	 * context. Where necessary, the derived {@link ExecutableToken} may infer new
	 * bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableToken}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a strict compatibility invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableToken}.
	 */
	public ExecutableToken<O, R> withStrictApplicability(List<? extends TypeToken<?>> arguments) {
		// TODO && make sure no boxing/unboxing occurs!

		return withLooseApplicability(arguments);
	}

	/**
	 * Derive a new {@link ExecutableToken} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a loose invocation
	 * context. Where necessary, the derived {@link ExecutableToken} may infer new
	 * bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableToken}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a loose compatibility invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableToken}.
	 */
	public ExecutableToken<O, R> withLooseApplicability(TypeToken<?>... arguments) {
		return withLooseApplicability(Arrays.asList(arguments));
	}

	/**
	 * Derive a new {@link ExecutableToken} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a loose invocation
	 * context. Where necessary, the derived {@link ExecutableToken} may infer new
	 * bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableToken}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a loose compatibility invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableToken}.
	 */
	public ExecutableToken<O, R> withLooseApplicability(List<? extends TypeToken<?>> arguments) {
		return withLooseApplicability(false, arguments);
	}

	/**
	 * Derive a new {@link ExecutableToken} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a variable arity
	 * invocation context. Where necessary, the derived {@link ExecutableToken}
	 * may infer new bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableToken}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a variable arity invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableToken}.
	 */
	public ExecutableToken<O, R> withVariableArityApplicability(TypeToken<?>... arguments) {
		return withVariableArityApplicability(Arrays.asList(arguments));
	}

	/**
	 * Derive a new {@link ExecutableToken} instance with inferred invocation type
	 * such that it is compatible with the given arguments in a variable arity
	 * invocation context. Where necessary, the derived {@link ExecutableToken}
	 * may infer new bounds or instantiations on type parameters.
	 * 
	 * @param arguments
	 *          The argument types of an invocation of this
	 *          {@link ExecutableToken}.
	 * @return If the given parameters are not compatible with this executable
	 *         member in a variable arity invocation context, we throw an
	 *         exception. Otherwise, we return the derived
	 *         {@link ExecutableToken}.
	 */
	public ExecutableToken<O, R> withVariableArityApplicability(List<? extends TypeToken<?>> arguments) {
		return withLooseApplicability(true, arguments);
	}

	private ExecutableToken<O, R> withLooseApplicability(boolean variableArity, List<? extends TypeToken<?>> arguments) {
		if (variableArity) {
			if (!executable.isVarArgs()) {
				throw new ReflectionException(p -> p.invalidVariableArityInvocation(getMember()));
			}
			if (parameters.size() > arguments.size() + 1) {
				throw new ReflectionException(p -> p.cannotResolveInvocationType(getMember(),
						arguments.stream().map(TypeToken::getType).collect(toList())));
			}
		} else if (parameters.size() != arguments.size()) {
			throw new ReflectionException(p -> p.cannotResolveInvocationType(getMember(),
					arguments.stream().map(TypeToken::getType).collect(toList())));
		}

		TypeResolver resolver = new TypeResolver(getBounds());

		if (!parameters.isEmpty()) {
			Iterator<ExecutableParameter> parameters = this.parameters.iterator();
			Type nextParameter = parameters.next().getType();
			Type parameter = nextParameter;
			for (TypeToken<?> argument : arguments) {
				if (nextParameter != null) {
					parameter = nextParameter;
					if (parameters.hasNext()) {
						nextParameter = parameters.next().getType();
					} else if (variableArity) {
						parameter = Types.getComponentType(parameter);
						nextParameter = null;
					}
				}

				argument.incorporateInto(resolver);
				resolver.reduce(new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY, argument.getType(), parameter));
			}

			// Test resolution is possible.
			resolver.copy().infer();
		}

		return new ExecutableToken<>(resolver.getBounds(), receiverType, returnType, methodTypeArguments, executable,
				invocationFunction, variableArity);
	}

	public boolean isRaw() {
		return typeArguments == null;
	}

	/**
	 * @return All generic type parameters of the wrapped {@link Executable}.
	 */
	public Stream<TypeVariable<?>> getAllTypeParameters() {
		if (isRaw())
			return Stream.empty();
		else
			return typeArguments.keySet().stream();
	}

	/**
	 * @return The generic type parameter instantiations of the wrapped
	 *         {@link Executable}, or their inference variables if not yet
	 *         instantiated.
	 */
	public Stream<TypeVariable<?>> getTypeParameters() {
		return Arrays.stream(executable.getTypeParameters());
	}

	/**
	 * @return All generic type parameter instantiations of the wrapped
	 *         {@link Executable}, or their inference variables if not yet
	 *         instantiated.
	 */
	public Stream<Map.Entry<TypeVariable<?>, Type>> getAllTypeArguments() {
		if (isRaw())
			return Stream.empty();
		else
			return typeArguments.entrySet().stream();
	}

	/**
	 * @return The generic type parameter instantiations of the wrapped
	 *         {@link Executable}, or their inference variables if not yet
	 *         instantiated.
	 */
	public Stream<Map.Entry<TypeVariable<?>, Type>> getTypeArguments() {
		return IntStream.range(0, typeArguments.size()).mapToObj(
				i -> new AbstractMap.SimpleEntry<>(executable.getTypeParameters()[i], methodTypeArguments.get(i)));
	}

	/**
	 * Derive a new {@link ExecutableToken} instance from this, with the given
	 * instantiation substituted for the given {@link TypeVariable}.
	 * 
	 * <p>
	 * The substitution will only succeed if it is compatible with the bounds on
	 * that type variable, and if it is more specific than the current type of the
	 * type variable, whether it is an {@link InferenceVariable}, a
	 * {@link TypeVariableCapture}, or another class of {@link Type}.
	 * 
	 * @param <U>
	 *          the type variable on the generic declaration which is the
	 *          {@link Executable} wrapped by this {@link ExecutableToken}
	 * @param variable
	 *          the type variable on the generic declaration which is the
	 *          {@link Executable} wrapped by this {@link ExecutableToken}
	 * @param instantiation
	 *          the type with which to instantiate the given type variable
	 * @return a new derived {@link ExecutableToken} instance with the given
	 *         instantiation substituted for the given type variable
	 */
	@SuppressWarnings("unchecked")
	public <U> ExecutableToken<O, ? extends R> withTypeArgument(TypeParameter<U> variable, TypeToken<U> instantiation) {
		return withTypeArgument(variable.getType(), instantiation.getType());
	}

	/**
	 * Derive a new {@link ExecutableToken} instance from this, with the given
	 * instantiation substituted for the given {@link TypeVariable}.
	 * 
	 * <p>
	 * The substitution will only succeed if it is compatible with the bounds on
	 * that type variable, and if it is more specific than the current type of the
	 * type variable, whether it is an {@link InferenceVariable}, a
	 * {@link TypeVariableCapture}, or another class of {@link Type}.
	 * 
	 * @param variable
	 *          the type variable on the generic declaration which is the
	 *          {@link Executable} wrapped by this {@link ExecutableToken}
	 * @param instantiation
	 *          the type with which to instantiate the given type variable
	 * @return a new derived {@link ExecutableToken} instance with the given
	 *         instantiation substituted for the given type variable
	 */
	public ExecutableToken<O, ? extends R> withTypeArgument(TypeVariable<?> variable, Type instantiation) {
		if (inferenceVariables.containsKey(variable)) {
			BoundSet bounds = this.bounds.withIncorporated().equality(inferenceVariables.get(variable), instantiation);

			return new ExecutableToken<>(bounds, receiverType, returnType, methodTypeArguments, executable,
					invocationFunction, variableArityInvocation);
		} else {
			return this;
		}
	}

	/**
	 * Derive a new {@link ExecutableToken} instance with the given generic type
	 * argument substitutions, as per the behavior of
	 * {@link #withTypeArgument(TypeVariable, Type)}, but with every argument
	 * provided in order.
	 * 
	 * @param typeArguments
	 *          a list of arguments for each generic type parameter of the
	 *          underlying {@link Executable}
	 * @return a new derived {@link ExecutableToken} instance with the given
	 *         instantiations substituted for each generic type parameter, in
	 *         order
	 */
	public ExecutableToken<O, ? extends R> withTypeArguments(Type... typeArguments) {
		return withTypeArguments(asList(typeArguments));
	}

	/**
	 * Derive a new {@link ExecutableToken} instance with the given generic type
	 * argument substitutions, as per the behavior of
	 * {@link #withTypeArgument(TypeVariable, Type)}, but with every argument
	 * provided in order.
	 * 
	 * @param typeArguments
	 *          a list of arguments for each generic type parameter of the
	 *          underlying {@link Executable}
	 * @return a new derived {@link ExecutableToken} instance with the given
	 *         instantiations substituted for each generic type parameter, in
	 *         order
	 */
	public ExecutableToken<O, ? extends R> withTypeArguments(List<Type> methodTypeArguments) {
		if (methodTypeArguments.size() != executable.getTypeParameters().length) {
			new ReflectionException(e -> e.incorrectTypeArgumentCount(executable, methodTypeArguments));
		}

		return new ExecutableToken<>(bounds, receiverType, returnType, methodTypeArguments, executable, invocationFunction,
				variableArityInvocation);
	}

	/**
	 * Invoke the wrapped {@link Executable} on the given receiver and with the
	 * given parameters. The receiver will be ignored for static methods or
	 * constructors. Variable arity invocation is not attempted.
	 * 
	 * <p>
	 * Due to erasure of the types of the arguments, there is a limit to what type
	 * checking can be performed at runtime. For type safe invocation, wrap
	 * arguments in {@link TypedObject} instances and use an overload of
	 * {@link #invokeSafely(Object, TypedObject...)} instead.
	 * 
	 * 
	 * @param receiver
	 *          the receiving object for the invocation. This parameter will be
	 *          ignored in the case of a constructor invocation or other static
	 *          method invocation
	 * @param arguments
	 *          the argument list for the invocation
	 * @return the result of the invocation
	 */
	public R invoke(O receiver, Object... arguments) {
		return invoke(receiver, Arrays.asList(arguments));
	}

	/**
	 * Invoke the wrapped {@link Executable} on the given receiver and with the
	 * given parameters. The receiver will be ignored for static methods or
	 * constructors. Variable arity invocation is not attempted.
	 * 
	 * <p>
	 * Due to erasure of the types of the arguments, there is a limit to what type
	 * checking can be performed at runtime. For type safe invocation, wrap
	 * arguments in {@link TypedObject} instances and use an overload of
	 * {@link #invokeSafely(Object, TypedObject...)} instead.
	 * 
	 * 
	 * @param receiver
	 *          the receiving object for the invocation. This parameter will be
	 *          ignored in the case of a constructor invocation or other static
	 *          method invocation
	 * @param arguments
	 *          the argument list for the invocation
	 * @return the result of the invocation
	 */
	public R invoke(O receiver, List<? extends Object> arguments) {
		if (variableArityInvocation) {
			List<Object> actualArguments = new ArrayList<>(parameters.size());
			Object[] varargs = (Object[]) Array.newInstance(
					Types.getRawType(parameters.get(parameters.size() - 1).getType()).getComponentType(),
					arguments.size() - parameters.size() + 1);

			for (int i = 0; i < parameters.size() - 1; i++) {
				actualArguments.add(arguments.get(0));
			}

			int j = 0;
			for (int i = parameters.size() - 1; i < arguments.size(); i++) {
				varargs[j++] = arguments.get(i);
			}
			actualArguments.add(varargs);

			return invocationFunction.apply(receiver, actualArguments);
		} else {
			return invocationFunction.apply(receiver, arguments);
		}
	}

	/**
	 * <p>
	 * As {@link #invoke(Object, Object...)}, but with arguments passed with their
	 * exact types, meaning full type checking can be performed at runtime. Also,
	 * here it is possible to determine whether the invocation is intended to be
	 * variable arity, and if so an attempt is made to invoke as such.
	 * 
	 * <p>
	 * If the expected parameter types of this executable member contain inference
	 * variables or type variable captures, an attempt will be made to satisfy
	 * their bounds according to the given argument types.
	 * 
	 * @param receiver
	 *          the receiving object for the invocation. This parameter will be
	 *          ignored in the case of a constructor invocation or other static
	 *          method invocation
	 * @param arguments
	 *          the typed argument list for the invocation
	 * @return the result of the invocation
	 */
	public R invokeSafely(O receiver, TypedObject<?>... arguments) {
		return invokeSafely(receiver, Arrays.asList(arguments));
	}

	/**
	 * <p>
	 * As {@link #invoke(Object, Object...)}, but with arguments passed with their
	 * exact types, meaning full type checking can be performed at runtime. Also,
	 * here it is possible to determine whether the invocation is intended to be
	 * variable arity, and if so an attempt is made to invoke as such.
	 * 
	 * <p>
	 * If the expected parameter types of this executable member contain inference
	 * variables or type variable captures, an attempt will be made to satisfy
	 * their bounds according to the given argument types.
	 * 
	 * @param receiver
	 *          the receiving object for the invocation. This parameter will be
	 *          ignored in the case of a constructor invocation or other static
	 *          method invocation
	 * @param arguments
	 *          the typed argument list for the invocation
	 * @return the result of the invocation
	 */
	public R invokeSafely(O receiver, List<? extends TypedObject<?>> arguments) {
		for (int i = 0; i < arguments.size(); i++)
			if (!arguments.get(i).getTypeToken().satisfiesConstraintTo(LOOSE_COMPATIBILILTY, parameters.get(i).getType())) {
				int finalI = i;
				throw new ReflectionException(p -> p.incompatibleArgument(arguments.get(finalI).getObject(),
						arguments.get(finalI).getTypeToken().getType(), parameters.get(finalI).getType(), finalI, getMember()));
			}
		return invoke(receiver, arguments);
	}

	/**
	 * find which methods can be invoked on this type, whether statically or on
	 * instances
	 * 
	 * @return a list of all {@link Method} objects applicable to this type,
	 *         wrapped in {@link ExecutableToken} instances
	 */
	public static ExecutableTokenStream<ExecutableToken<Void, ?>> getStaticMethods(Class<?> declaringClass) {
		Stream<Method> methodStream = Arrays
				.stream(declaringClass.getMethods())
				.filter(m -> Modifier.isStatic(m.getModifiers()));

		return executableStream(methodStream, m -> (ExecutableToken<Void, ?>) ExecutableToken.overStaticMethod(m));
	}
}

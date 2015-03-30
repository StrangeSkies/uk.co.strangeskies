/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.reflection.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.utilities.tuples.Pair;

public class Invokable<T, R> {
	private final Resolver resolver;

	private final TypeLiteral<T> receiverType;
	private final TypeLiteral<R> returnType;
	private final Executable executable;

	private final List<Type> parameters;

	private final BiFunction<T, List<?>, R> invocationFunction;

	private Invokable(TypeLiteral<T> receiverType, TypeLiteral<R> returnType,
			Executable executable, BiFunction<T, List<?>, R> invocationFunction) {
		this(receiverType.getResolver(), receiverType, returnType, executable,
				invocationFunction);
	}

	@SuppressWarnings("unchecked")
	private Invokable(Resolver resolver, TypeLiteral<T> receiverType,
			TypeLiteral<R> returnType, Executable executable,
			BiFunction<T, List<?>, R> invocationFunction) {
		this.receiverType = receiverType;
		this.executable = executable;
		this.invocationFunction = invocationFunction;

		this.resolver = resolver;
		resolver.capture(getExecutable());

		if (receiverType != returnType)
			returnType = (TypeLiteral<R>) TypeLiteral.from(resolver
					.resolveType(returnType.getType()));

		this.returnType = returnType;

		parameters = Arrays.stream(executable.getGenericParameterTypes())
				.map(resolver::resolveType).collect(Collectors.toList());
	}

	public static <T> Invokable<T, T> from(Constructor<T> constructor) {
		TypeLiteral<T> type = new TypeLiteral<>(constructor.getDeclaringClass());
		return from(constructor, type);
	}

	static <T> Invokable<T, T> from(Constructor<T> constructor,
			TypeLiteral<T> receiver) {
		return new Invokable<>(receiver, receiver, constructor,
				(T r, List<?> a) -> {
					try {
						return constructor.newInstance(a);
					} catch (Exception e) {
						throw new TypeInferenceException("Cannot invoke constructor '"
								+ constructor + "' with arguments '" + a + "'.");
					}
				});
	}

	public static Invokable<?, ?> from(Method method) {
		return from(method, TypeLiteral.from(method.getDeclaringClass()),
				TypeLiteral.from(method.getGenericReturnType()));
	}

	@SuppressWarnings("unchecked")
	static <T, R> Invokable<T, R> from(Method method, TypeLiteral<T> receiver,
			TypeLiteral<R> result) {
		return new Invokable<>(receiver, result, method, (T r, List<?> a) -> {
			try {
				return (R) method.invoke(r, a);
			} catch (Exception e) {
				throw new TypeInferenceException("Cannot invoke method '" + method
						+ "' on receiver '" + r + "' with arguments '" + a + "'.");
			}
		});
	}

	public static Invokable<?, ?> from(Executable executable) {
		if (executable instanceof Method)
			return from((Method) executable);
		else
			return from((Constructor<?>) executable);
	}

	public Resolver getResolver() {
		return new Resolver(getInternalResolver());
	}

	private Resolver getInternalResolver() {
		return resolver;
	}

	public boolean isAbstract() {
		return Modifier.isAbstract(executable.getModifiers());
	}

	public boolean isFinal() {
		return Modifier.isFinal(executable.getModifiers());
	}

	public boolean isNative() {
		return Modifier.isNative(executable.getModifiers());
	}

	public boolean isPrivate() {
		return Modifier.isPrivate(executable.getModifiers());
	}

	public boolean isProtected() {
		return Modifier.isProtected(executable.getModifiers());
	}

	public boolean isPublic() {
		return Modifier.isPublic(executable.getModifiers());
	}

	public boolean isStatic() {
		return Modifier.isStatic(executable.getModifiers());
	}

	public boolean isStrict() {
		return Modifier.isStrict(executable.getModifiers());
	}

	public boolean isSynchronized() {
		return Modifier.isSynchronized(executable.getModifiers());
	}

	public boolean isGeneric() {
		return executable.getTypeParameters().length > 0;
	}

	public boolean isVariableArity() {
		return executable.isVarArgs();
	}

	@Override
	public String toString() {
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

		return builder
				.append(returnType)
				.append(" ")
				.append(receiverType)
				.append(".")
				.append(executable.getName())
				.append("(")
				.append(
						parameters.stream().map(Objects::toString)
								.collect(Collectors.joining(", "))).append(")").toString();
	}

	public Executable getExecutable() {
		return executable;
	}

	public Type getDeclaringType() {
		return receiverType.getType();
	}

	public TypeLiteral<T> getReceiverType() {
		return receiverType;
	}

	public TypeLiteral<R> getReturnType() {
		return returnType;
	}

	public List<Type> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	public <U extends T> Invokable<U, ? extends R> withReceiverType(
			TypeLiteral<U> type) {
		throw new UnsupportedOperationException(); // TODO resolve override
	}

	public <S extends R> Invokable<T, S> withTargetType(Class<S> target) {
		return withTargetType(TypeLiteral.from(target));
	}

	@SuppressWarnings("unchecked")
	public <S extends R> Invokable<T, S> withTargetType(TypeLiteral<S> target) {
		return (Invokable<T, S>) withTargetType(target.getType());
	}

	public Invokable<T, ? extends R> withTargetType(Type target) {
		return withTargetTypeCapture(target);
	}

	@SuppressWarnings("unchecked")
	private <S extends R> Invokable<T, S> withTargetTypeCapture(Type target) {
		Resolver resolver = getResolver();

		new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY, returnType.getType(),
				target).reduceInto(resolver.getBounds());

		Resolver testResolver = new Resolver(resolver);

		testResolver.infer();

		if (!testResolver.validate())
			throw new TypeInferenceException(
					"Cannot resolve generic type parameters for invocation of '" + this
							+ "' with target '" + target + "'.");

		return new Invokable<>(resolver, receiverType,
				(TypeLiteral<S>) TypeLiteral.from(resolver.resolveType(returnType
						.getType())), executable,
				(BiFunction<T, List<?>, S>) invocationFunction);
	}

	public <U extends R> Invokable<T, U> withInferredType(
			TypeLiteral<U> targetType, TypeLiteral<?>... arguments) {
		return withInferredType(targetType, Arrays.asList(arguments));
	}

	@SuppressWarnings("unchecked")
	public <U extends R> Invokable<T, U> withInferredType(
			TypeLiteral<U> targetType, List<? extends TypeLiteral<?>> arguments) {
		return (Invokable<T, U>) withInferredType(targetType.getType(), arguments
				.stream().map(TypeLiteral::getType).collect(Collectors.toList()));
	}

	public Invokable<T, ? extends R> withInferredType(Type targetType,
			Type... arguments) {
		return withInferredType(targetType, Arrays.asList(arguments));
	}

	public Invokable<T, ? extends R> withInferredType(Type targetType,
			List<? extends Type> arguments) {
		Invokable<T, R> invokable;
		try {
			invokable = withLooseApplicability(arguments);
		} catch (Exception e) {
			invokable = withVariableArityApplicability(arguments);
		}
		return invokable.withTargetType(targetType);
	}

	@SuppressWarnings("unchecked")
	public <U extends R> Invokable<T, U> infer() {
		Resolver resolver = getResolver();

		resolver.infer();

		if (!resolver.validate())
			throw new TypeInferenceException(
					"Cannot resolve generic type parameters for invocation of '" + this
							+ "'.");

		return new Invokable<>(resolver, receiverType,
				(TypeLiteral<U>) TypeLiteral.from(resolver.resolveType(returnType
						.getType())), executable,
				(BiFunction<T, List<?>, U>) invocationFunction);
	}

	/*
	 * If the given parameters are not compatible with this invokable in a strict
	 * compatibility invocation context, we return null. Otherwise, we infer a
	 * partial parameterisation where necessary and return the resulting
	 * invokable.
	 */
	public Invokable<T, R> withStrictApplicability(Type... arguments) {
		return withStrictApplicability(Arrays.asList(arguments));
	}

	public Invokable<T, R> withStrictApplicability(List<? extends Type> arguments) {
		// TODO && make sure no boxing/unboxing occurs!

		return withLooseApplicability(arguments);
	}

	/*
	 * If the given parameters are not compatible with this invokable in a loose
	 * compatibility invocation context, we return null. Otherwise, we infer a
	 * partial parameterisation where necessary and return the resulting
	 * invokable.
	 */
	public Invokable<T, R> withLooseApplicability(Type... arguments) {
		return withLooseApplicability(Arrays.asList(arguments));
	}

	public Invokable<T, R> withLooseApplicability(List<? extends Type> arguments) {
		return withLooseApplicability(false, arguments);
	}

	/*
	 * If the given parameters are not compatible with this invokable in a loose
	 * compatibility invocation context, and with variable arity, we return null.
	 * Otherwise, we infer a partial parameterisation where necessary and return
	 * the resulting invokable.
	 */
	public Invokable<T, R> withVariableArityApplicability(Type... arguments) {
		return withVariableArityApplicability(Arrays.asList(arguments));
	}

	public Invokable<T, R> withVariableArityApplicability(
			List<? extends Type> arguments) {
		return withLooseApplicability(true, arguments);
	}

	private Invokable<T, R> withLooseApplicability(boolean variableArity,
			List<? extends Type> arguments) {
		if (variableArity) {
			if (!executable.isVarArgs())
				throw new IllegalArgumentException("Invokable '" + this
						+ "' cannot be invoked in variable arity invocation context.");

			if (parameters.size() > arguments.size())
				return null;
		} else if (parameters.size() != arguments.size())
			return null;

		Resolver resolver = getResolver();

		if (!parameters.isEmpty()) {
			Iterator<Type> parameters = this.parameters.iterator();
			Type nextParameter = parameters.next();
			Type parameter = nextParameter;
			for (Type argument : arguments) {
				if (nextParameter != null) {
					parameter = nextParameter;
					if (parameters.hasNext())
						nextParameter = parameters.next();
					else if (variableArity) {
						parameter = Types.getComponentType(parameter);
						nextParameter = null;
					}
				}
				new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY, argument, parameter)
						.reduceInto(resolver.getBounds());
			}

			Resolver testResolver = new Resolver(resolver);

			if (!testResolver.validate())
				throw new TypeInferenceException(
						"Cannot resolve generic type parameters for invocation of '" + this
								+ "' with arguments '" + arguments + "'.");
		}

		return new Invokable<>(resolver, receiverType, returnType, executable,
				invocationFunction);
	}

	@SuppressWarnings("unchecked")
	public List<TypeVariable<? extends Executable>> getTypeParameters() {
		return Arrays.asList(executable.getTypeParameters()).stream()
				.map(v -> (TypeVariable<? extends Executable>) v)
				.collect(Collectors.toList());
	}

	public List<Type> getTypeArguments() {
		throw new UnsupportedOperationException(); // TODO
	}

	public Invokable<T, ? extends R> withTypeArgument(
			TypeVariable<? extends Executable> variable, Type instantiation) {
		if (Arrays.stream(executable.getTypeParameters())
				.anyMatch(variable::equals)) {
			Resolver resolver = new Resolver(this.resolver);
			resolver.incorporateInstantiation(variable, instantiation);
		}

		throw new UnsupportedOperationException(); // TODO
	}

	public Invokable<T, ? extends R> withTypeArguments(Type... typeArguments) {
		return withTypeArguments(Arrays.asList(typeArguments));
	}

	public Invokable<T, ? extends R> withTypeArguments(List<Type> typeArguments) {
		throw new UnsupportedOperationException(); // TODO
	}

	public R invoke(T receiver, Object... arguments) {
		return invoke(receiver, Arrays.asList(arguments));
	}

	public R invoke(T receiver, List<? extends Object> arguments) {
		return invocationFunction.apply(receiver, arguments);
	}

	public R invokeSafely(T receiver, TypedObject<?>... arguments) {
		return invokeSafely(receiver, Arrays.asList(arguments));
	}

	public R invokeSafely(T receiver, List<? extends TypedObject<?>> arguments) {
		for (int i = 0; i < arguments.size(); i++)
			if (!arguments.get(i).getType().isAssignableTo(parameters.get(i)))
				throw new IllegalArgumentException("Argument '" + arguments.get(i)
						+ "' is not assignable to parameter '" + parameters.get(i)
						+ "' at index '" + i + "'.");
		return invoke(receiver, arguments);
	}

	public static <T, R> Set<? extends Invokable<T, ? extends R>> resolveApplicableCandidates(
			Set<? extends Invokable<T, ? extends R>> candidates,
			List<? extends Type> parameters) {
		Map<Invokable<T, ? extends R>, RuntimeException> failures = new HashMap<>();

		Set<? extends Invokable<T, ? extends R>> compatibleCandidates = filterOverloadCandidates(
				candidates, i -> i.withLooseApplicability(parameters), failures::put);

		if (compatibleCandidates.isEmpty())
			compatibleCandidates = filterOverloadCandidates(candidates,
					i -> i.withVariableArityApplicability(parameters), failures::put);
		else {
			Set<? extends Invokable<T, ? extends R>> oldCompatibleCandidates = compatibleCandidates;
			compatibleCandidates = filterOverloadCandidates(compatibleCandidates,
					i -> i.withStrictApplicability(parameters), failures::put);
			if (compatibleCandidates.isEmpty())
				compatibleCandidates = oldCompatibleCandidates;
		}

		if (compatibleCandidates.isEmpty())
			throw new TypeInferenceException("Parameters '" + parameters
					+ "' are not applicable to invokable candidates '" + candidates
					+ "'.", failures.values().stream().findAny().get());

		return compatibleCandidates;
	}

	private static <T, R> Set<? extends Invokable<T, ? extends R>> filterOverloadCandidates(
			Set<? extends Invokable<T, ? extends R>> candidates,
			Function<? super Invokable<T, ? extends R>, Invokable<T, ? extends R>> applicabilityFunction,
			BiConsumer<Invokable<T, ? extends R>, RuntimeException> failures) {
		return candidates.stream().map(i -> {
			try {
				return applicabilityFunction.apply(i);
			} catch (RuntimeException e) {
				failures.accept(i, e);
				return null;
			}
		}).filter(o -> o != null).collect(Collectors.toSet());
	}

	public static <T, R> Invokable<T, R> resolveMostSpecificCandidate(
			Collection<? extends Invokable<T, R>> candidates) {
		if (candidates.size() == 1)
			return candidates.iterator().next();

		Set<Invokable<T, R>> mostSpecificSoFar = resolveMostSpecificCandidateSet(candidates);

		/*
		 * Find which of the remaining candidates, which should all have identical
		 * parameter types, is declared in the lowermost class.
		 */
		Iterator<Invokable<T, R>> overrideCandidateIterator = mostSpecificSoFar
				.iterator();
		Invokable<T, R> mostSpecific = overrideCandidateIterator.next();
		while (overrideCandidateIterator.hasNext()) {
			Invokable<T, R> candidate = overrideCandidateIterator.next();

			if (!candidate.equals(mostSpecific))
				throw new TypeInferenceException(
						"Cannot resolve method invokation ambiguity between candidate '"
								+ candidate + "' and '" + mostSpecific + "'.");

			mostSpecific = candidate.getExecutable().getDeclaringClass()
					.isAssignableFrom(mostSpecific.getExecutable().getDeclaringClass()) ? candidate
					: mostSpecific;
		}

		return mostSpecific;
	}

	private static <T, R> Set<Invokable<T, R>> resolveMostSpecificCandidateSet(
			Collection<? extends Invokable<T, R>> candidates) {
		List<Invokable<T, R>> remainingCandidates = new ArrayList<>(candidates);

		/*
		 * For each remaining candidate in the list...
		 */
		for (int first = 0; first < remainingCandidates.size(); first++) {
			Invokable<T, R> firstCandidate = remainingCandidates.get(first);

			/*
			 * Compare with each other remaining candidate...
			 */
			for (int second = first + 1; second < remainingCandidates.size(); second++) {
				Invokable<T, R> secondCandidate = remainingCandidates.get(second);

				/*
				 * Determine which of the invokables, if either, are more specific.
				 */
				Pair<Boolean, Boolean> moreSpecific = compareCandidates(firstCandidate,
						secondCandidate);

				if (moreSpecific.getLeft()) {
					if (moreSpecific.getRight()) {
						/*
						 * First and second are equally specific.
						 */
					} else {
						/*
						 * First is strictly more specific.
						 */
						remainingCandidates.remove(second--);
					}
				} else if (moreSpecific.getRight()) {
					/*
					 * Second is strictly more specific.
					 */
					remainingCandidates.remove(first--);

					break;
				} else {
					/*
					 * Neither first nor second are more specific.
					 */
					throw new TypeInferenceException(
							"Cannot resolve method invokation ambiguity between candidate '"
									+ firstCandidate + "' and '" + secondCandidate + "'.");
				}
			}
		}

		return new HashSet<>(remainingCandidates);
	}

	private static Pair<Boolean, Boolean> compareCandidates(
			Invokable<?, ?> firstCandidate, Invokable<?, ?> secondCandidate) {
		boolean firstMoreSpecific = true;
		boolean secondMoreSpecific = true;

		if (firstCandidate.isGeneric())
			secondMoreSpecific = compareGenericCandidate(secondCandidate,
					firstCandidate);
		if (secondCandidate.isGeneric())
			firstMoreSpecific = compareGenericCandidate(firstCandidate,
					secondCandidate);

		if (!firstCandidate.isGeneric() || !secondCandidate.isGeneric()) {
			int i = 0;
			for (Type firstParameter : firstCandidate.getParameters()) {
				Type secondParameter = secondCandidate.getParameters().get(i++);

				if (!secondMoreSpecific && !secondCandidate.isGeneric()) {
					if (!Types.isAssignable(firstParameter, secondParameter)) {
						firstMoreSpecific = false;
						break;
					}
				} else if (!firstMoreSpecific && !firstCandidate.isGeneric()) {
					if (!Types.isAssignable(secondParameter, firstParameter)) {
						secondMoreSpecific = false;
						break;
					}
				} else {
					secondMoreSpecific = Types.isAssignable(secondParameter,
							firstParameter);
					firstMoreSpecific = Types.isAssignable(firstParameter,
							secondParameter);

					if (!(firstMoreSpecific || secondMoreSpecific))
						break;
				}
			}
		}

		return new Pair<Boolean, Boolean>(firstMoreSpecific, secondMoreSpecific);
	}

	private static boolean compareGenericCandidate(
			Invokable<?, ?> firstCandidate, Invokable<?, ?> genericCandidate) {
		Resolver resolver = genericCandidate.getResolver();

		try {
			int parameters = firstCandidate.getParameters().size();
			if (firstCandidate.isVariableArity()) {
				parameters--;

				new ConstraintFormula(Kind.SUBTYPE, firstCandidate.getParameters().get(
						parameters), genericCandidate.getParameters().get(parameters))
						.reduceInto(resolver.getBounds());
			}

			for (int i = 0; i < parameters; i++) {
				new ConstraintFormula(Kind.SUBTYPE, firstCandidate.getParameters().get(
						i), genericCandidate.getParameters().get(i)).reduceInto(resolver
						.getBounds());
			}

			resolver.validate();
		} catch (Exception e) {
			return false;
		}

		return true;
	}
}

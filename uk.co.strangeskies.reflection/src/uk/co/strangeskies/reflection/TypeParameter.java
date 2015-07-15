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

import java.lang.reflect.TypeVariable;

/**
 * A capture of a type variable, with all of the reflective functionality
 * provided by {@link TypeToken}.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type variable we wish to capture.
 */
public class TypeParameter<T> extends TypeToken<T> {
	/**
	 * Capture the type variable provided as an argument to the type parameter of
	 * this constructor. This should only ever be parameterised with an
	 * uninstantiated type variable.
	 */
	protected TypeParameter() {
		if (!(super.getType() instanceof TypeVariable))
			throw new IllegalArgumentException();
	}

	private TypeParameter(TypeVariable<?> type) {
		super(type);
	}

	@Override
	public TypeVariable<?> getType() {
		return (TypeVariable<?>) super.getType();
	}

	/**
	 * Capture the given type variable in a TypeToken.
	 * 
	 * @param type
	 *          The type variable to capture.
	 * @return A type token instance over the given type.
	 */
	public static TypeParameter<?> of(TypeVariable<?> type) {
		return new TypeParameter<>(type);
	}
}
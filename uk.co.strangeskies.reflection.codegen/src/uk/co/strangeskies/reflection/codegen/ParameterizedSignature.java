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
package uk.co.strangeskies.reflection.codegen;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public interface ParameterizedSignature<S extends ParameterizedSignature<S>> extends AnnotatedSignature<S> {
	Stream<? extends TypeVariableSignature> getTypeVariables();

	default S withTypeVariables(String... names) {
		return withTypeVariables(stream(names).map(TypeVariableSignature::typeVariableSignature).collect(toList()));
	}

	default S withTypeVariables(TypeVariableSignature... typeVariables) {
		return withTypeVariables(asList(typeVariables));
	}

	S withTypeVariables(Collection<? extends TypeVariableSignature> typeVariables);

	static void appendTypeParametersTo(ParameterizedSignature<?> signature, StringBuilder builder) {
		builder.append("<").append(signature.getTypeVariables().map(Objects::toString).collect(joining(", "))).append(">");
	}

	static boolean equals(ParameterizedSignature<?> first, ParameterizedSignature<?> second) {
		return first == second || (AnnotatedSignature.equals(first, second)
				&& Objects.equals(first.getTypeVariables().collect(toSet()), second.getTypeVariables().collect(toSet())));
	}

	static int hashCode(ParameterizedSignature<?> signature) {
		return AnnotatedSignature.hashCode(signature)
				^ signature.getTypeVariables().mapToInt(Objects::hashCode).reduce(0, (a, b) -> a ^ b);
	}
}
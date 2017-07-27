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

import java.util.List;

import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.FieldToken;
import uk.co.strangeskies.reflection.token.MethodMatcher;
import uk.co.strangeskies.reflection.token.VariableMatcher;

public interface ValueExpression<T> extends Expression {
  default <R> VariableExpression<R> getField(FieldToken<? super T, R> field) {
    return new FieldExpression<>(this, field);
  }

  default <R> VariableExpression<R> getField(VariableMatcher<? super T, R> field) {
    return new QualifiedVariableExpression<>(this, field);
  }

  default <R> ValueExpression<R> invoke(
      ExecutableToken<? super T, R> invocable,
      ValueExpression<?>... arguments) {
    return invoke(invocable, asList(arguments));
  }

  default <R> ValueExpression<R> invoke(
      ExecutableToken<? super T, R> invocable,
      List<ValueExpression<?>> arguments) {
    return new MethodExpression<>(this, invocable, arguments);
  }

  default <R> ValueExpression<R> invoke(
      MethodMatcher<? super T, R> invocable,
      ValueExpression<?>... arguments) {
    return invoke(invocable, asList(arguments));
  }

  default <R> ValueExpression<R> invoke(
      MethodMatcher<? super T, R> invocable,
      List<ValueExpression<?>> arguments) {
    return new QualifiedInvocationExpression<>(this, invocable, arguments);
  }
}
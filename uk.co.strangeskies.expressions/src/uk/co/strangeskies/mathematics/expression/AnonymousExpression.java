/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.expressions.
 *
 * uk.co.strangeskies.expressions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.expressions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.expressions.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.expression;

/**
 * A simple interface for expressions when the type of the expression object
 * itself is unimportant or unavailable, and we only care about the type of the
 * expressions value. This helps avoid API surface containing types such as
 * {@code Expression<?, T>}, where the omission of the recursive self bound can
 * cause issues.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the value of this expression
 */
public interface AnonymousExpression<T> extends Expression<AnonymousExpression<T>, T> {}
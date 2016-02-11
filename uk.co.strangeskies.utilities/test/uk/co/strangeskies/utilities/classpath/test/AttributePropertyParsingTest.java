/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.classpath.test;

import org.junit.Assert;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import uk.co.strangeskies.utilities.classpath.AttributeProperty;
import uk.co.strangeskies.utilities.classpath.ManifestUtilities;
import uk.co.strangeskies.utilities.classpath.PropertyType;

/**
 * @author Elias N Vasylenko
 */
@SuppressWarnings("javadoc")
@RunWith(Theories.class)
public class AttributePropertyParsingTest {
	public static class AttributePropertyParsingTheory<T> extends AttributeProperty<T> {
		private final String parses;

		private AttributeProperty<?> parsed;

		public AttributePropertyParsingTheory(String parses, String name, PropertyType<T> type, T value) {
			super(name, type, value);

			this.parses = parses;
		}

		public AttributeProperty<?> parse() {
			if (parsed == null) {
				parsed = ManifestUtilities.parseAttributeProperty(parses);
			}
			return parsed;
		}
	}

	private static <T> AttributePropertyParsingTheory<T> newTheory(String parses, String name, PropertyType<T> type,
			T value) {
		return new AttributePropertyParsingTheory<>(parses, name, type, value);
	}

	@DataPoint
	public static final AttributePropertyParsingTheory<String> simpleString = newTheory("simpleString=test",
			"simpleString", PropertyType.STRING, "test");

	@DataPoint
	public static final AttributePropertyParsingTheory<String> typedString = newTheory("typedString:String=test",
			"typedString", PropertyType.STRING, "test");

	@DataPoint
	public static final AttributePropertyParsingTheory<String> quotedString = newTheory("quotedString:String=\"test\"",
			"quotedString", PropertyType.STRING, "test");

	@DataPoint
	public static final AttributePropertyParsingTheory<String> escapedQuoteString = newTheory(
			"escapedQuoteString:String=\"te\\\"st\"", "escapedQuoteString", PropertyType.STRING, "te\"st");

	@Theory
	public void testManifestEntryAttributeParserValid(AttributePropertyParsingTheory<?> theory) {
		Assert.assertNotNull(theory.parse());
	}

	@Theory
	public void testManifestEntryAttributeNameParser(AttributePropertyParsingTheory<?> theory) {
		Assert.assertNotNull(theory.parse().name());
		Assert.assertEquals(theory.name(), theory.parse().name());
	}

	@Theory
	public void testManifestEntryAttributeTypeParser(AttributePropertyParsingTheory<?> theory) {
		Assert.assertNotNull(theory.parse().type());
		Assert.assertEquals(theory.type(), theory.parse().type());
	}

	@Theory
	public void testManifestEntryAttributeValueParser(AttributePropertyParsingTheory<?> theory) {
		Assert.assertNotNull(theory.parse().value());
		Assert.assertEquals(theory.value(), theory.parse().value());
	}
}

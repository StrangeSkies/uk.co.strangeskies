/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.osgi.
 *
 * uk.co.strangeskies.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.osgi.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.osgi;

import java.util.Locale;

import uk.co.strangeskies.utilities.text.LocalizedRuntimeException;
import uk.co.strangeskies.utilities.text.LocalizedString;

/**
 * A localised exception class for dealing with general service wiring and
 * provision issues.
 * 
 * @author Elias N Vasylenko
 */
public class ServiceWiringException extends LocalizedRuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Build with the given developer locale
	 * 
	 * @param message
	 *          the localised message string
	 * @param developerLocale
	 *          the developer's locale
	 * @param cause
	 *          the cause
	 */
	protected ServiceWiringException(LocalizedString message, Locale developerLocale, Throwable cause) {
		super(message, developerLocale, cause);
	}

	/**
	 * Build with the given developer locale
	 * 
	 * @param message
	 *          the localised message string
	 * @param developerLocale
	 *          the developer's locale
	 */
	protected ServiceWiringException(LocalizedString message, Locale developerLocale) {
		super(message, developerLocale);
	}

	/**
	 * Build with an English developer locale
	 * 
	 * @param message
	 *          the localised message string
	 * @param cause
	 *          the cause
	 */
	public ServiceWiringException(LocalizedString message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Build with an English developer locale
	 * 
	 * @param message
	 *          the localised message string
	 */
	public ServiceWiringException(LocalizedString message) {
		super(message);
	}
}
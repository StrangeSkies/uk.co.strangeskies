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
package uk.co.strangeskies.utilities.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * A resource bundle with a configurable locale
 * 
 * @author Elias N Vasylenko
 */
public abstract class LocalizedResourceBundle extends ResourceBundle {
	static final class LocalizingResourceBundleImpl extends LocalizedResourceBundle {
		private final List<LocalizationResource> resources;

		private final List<ResourceBundle> bundles;

		public LocalizingResourceBundleImpl(Locale locale, List<? extends LocalizationResource> resources) {
			super(locale);

			this.resources = new ArrayList<>(resources);

			bundles = new ArrayList<>();

			for (LocalizationResource resource : resources) {
				try {
					bundles.add(ResourceBundle.getBundle(resource.getLocation(), locale, resource.getClassLoader()));
					break;
				} catch (MissingResourceException e) {}
			}

			if (bundles.isEmpty()) {
				throw new MissingResourceException("Cannot find resources for " + locale + " in any of " + resources,
						resources.toString(), "");
			}
		}

		@Override
		public Enumeration<String> getKeys() {
			Set<String> keys = new LinkedHashSet<>();

			return Collections.enumeration(keys);
		}

		@Override
		public LocalizingResourceBundleImpl withLocale(Locale locale) {
			return new LocalizingResourceBundleImpl(locale, resources);
		}

		@Override
		protected String handleGetObject(String key) {
			for (ResourceBundle bundle : bundles) {
				try {
					return bundle.getString(key);
				} catch (MissingResourceException e) {}
			}

			return null;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (obj == this)
				return true;
			if (!(obj instanceof LocalizingResourceBundleImpl))
				return false;

			LocalizingResourceBundleImpl other = (LocalizingResourceBundleImpl) obj;

			return resources.equals(other.resources);
		}

		@Override
		public int hashCode() {
			return resources.hashCode();
		}
	}

	private Locale locale;

	/**
	 * Create a resource bundle with the given initial locale.
	 * 
	 * @param locale
	 *          the initial locale
	 */
	public LocalizedResourceBundle(Locale locale) {}

	@Override
	protected abstract String handleGetObject(String key);

	/**
	 * @param locale
	 *          the new locale
	 * @return the previous locale
	 */
	public abstract LocalizedResourceBundle withLocale(Locale locale);

	@Override
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Create a {@link LocalizedResourceBundle localising resource bundle} over
	 * the given class loader and base names. The created resource bundle will
	 * behave as a delegate to a series of {@link ResourceBundle resource bundles}
	 * , which in turn will behave according to
	 * {@link ResourceBundle#getBundle(String, Locale, ClassLoader)} with the
	 * given class loader, using the given locations as base names, and using the
	 * current locale of the {@link LocalizedResourceBundle}. This locale may
	 * change, and the delegate resource bundles will be updated accordingly.
	 * 
	 * @param locale
	 *          the locale for the resource bundle
	 * @param resources
	 *          the base names of properties files to load, and the class loaders
	 *          they exist in
	 * @return a resource bundle over all resources at each given location
	 */
	public static LocalizedResourceBundle getBundle(Locale locale, List<? extends LocalizationResource> resources) {
		return new LocalizingResourceBundleImpl(locale, resources);
	}
}

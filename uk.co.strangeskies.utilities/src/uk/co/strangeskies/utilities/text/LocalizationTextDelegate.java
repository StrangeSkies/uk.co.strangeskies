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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.ObservableImpl;
import uk.co.strangeskies.utilities.ObservablePropertyImpl;
import uk.co.strangeskies.utilities.text.LocalizerImpl.MethodSignature;

/**
 * Delegate implementation object for proxy instances of LocalizationText
 * classes. This class deals with most method interception from the proxies
 * generated by {@link Localizer}.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the delegating {@link LocalizedText} proxy
 */
public class LocalizationTextDelegate<T extends LocalizedText<T>> extends ObservableImpl<T>
		implements LocalizedText<T> {
	/*
	 * Implementation of localised string
	 */
	class LocalizedStringImpl extends ObservablePropertyImpl<String, String> implements LocalizedString, Consumer<T> {
		private final MethodSignature signature;
		private final String key;
		private final Object[] arguments;

		public LocalizedStringImpl(MethodSignature signature, Object[] arguments) {
			super(Function.identity(), Objects::equals, null);

			this.signature = signature;
			this.key = LocalizerImpl.getKey(signature.method(), arguments);

			if (arguments != null) {
				int argumentCount = 0;

				for (int i = 0; i < arguments.length; i++) {
					if (signature.method().getParameters()[i].getAnnotation(AppendToLocalizationKey.class) == null) {
						arguments[argumentCount++] = arguments[i];
					}
				}

				this.arguments = Arrays.copyOf(arguments, argumentCount);
			} else {
				this.arguments = new Object[0];
			}

			updateText();

			LocalizationTextDelegate.this.addWeakObserver(this);
		}

		private void updateText() {
			String translationText = translations.get(signature);
			if (translationText == null) {
				translationText = loadTranslation(signature.method(), arguments);
				if (translationText != null) {
					translations.put(signature, translationText);
				}
			}

			if (translationText == null) {
				if (signature.method().getDeclaringClass().equals(LocalizerText.class)) {
					try {
						translationText = ((LocalizedString) signature.method().invoke(text, arguments)).toString();
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
				}

				if (translationText == null) {
					translationText = text.translationNotFoundFor(key).toString();
				}
			}

			translationText = String.format(locale(), translationText, arguments);

			set(translationText);
		}

		private String loadTranslation(Method method, Object[] arguments) {
			try {
				return bundle.getString(key);
			} catch (MissingResourceException e) {
				localizer.log(Level.WARN, new LocalizationException(text.translationNotFoundMessage(key), e));
			}

			return null;
		}

		@Override
		public String toString() {
			return get();
		}

		@Override
		public String toString(Locale locale) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void accept(T t) {
			updateText();
		}

		@Override
		public Locale locale() {
			return localizer.getLocale();
		}
	}

	private LocalizerImpl localizer;
	private LocalizedResourceBundle bundle;

	private final Map<MethodSignature, String> translations;

	private final T proxy;
	private final LocalizerText text;

	private final Consumer<Locale> observer;

	LocalizationTextDelegate(LocalizerImpl localizer, T proxy, LocalizedResourceBundle bundle, LocalizerText text) {
		this.localizer = localizer;
		this.proxy = proxy;
		this.bundle = bundle;
		this.text = text;

		translations = new ConcurrentHashMap<>();

		observer = l -> {
			this.bundle = this.bundle.withLocale(l);
			translations.clear();
			fire(this.proxy);
		};
		localizer.locale().addWeakObserver(observer);
	}

	LocalizedString getTranslation(MethodSignature signature, Object[] args) {
		return new LocalizedStringImpl(signature, args);
	}

	@Override
	public Locale getLocale() {
		return localizer.getLocale();
	}

	@Override
	public T copy() {
		return proxy;
	}
}

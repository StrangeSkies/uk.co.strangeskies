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
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.properties;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableSet;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import uk.co.strangeskies.log.Log;
import uk.co.strangeskies.log.Log.Level;
import uk.co.strangeskies.text.properties.PropertyConfiguration.Defaults;
import uk.co.strangeskies.text.properties.PropertyConfiguration.Evaluation;
import uk.co.strangeskies.text.properties.PropertyLoaderImpl.MethodSignature;

/**
 * Delegate implementation object for proxy instances of property accessor
 * interfaces. This class deals with most method interception from the proxies
 * generated by {@link PropertyLoader}.
 * 
 * @author Elias N Vasylenko
 *
 * @param <A>
 *          the type of the delegating proxy
 */
public class PropertyAccessorDelegate<A> {
	private static final Set<MethodSignature> DIRECT_METHODS = getDirectMethods();

	private static Set<MethodSignature> getDirectMethods() {
		Set<MethodSignature> signatures = new HashSet<>();

		for (Method method : Object.class.getDeclaredMethods()) {
			signatures.add(new MethodSignature(method));
		}

		return unmodifiableSet(signatures);
	}

	private static final Constructor<MethodHandles.Lookup> METHOD_HANDLE_CONSTRUCTOR = getMethodHandleConstructor();

	private static Constructor<Lookup> getMethodHandleConstructor() {
		try {
			Constructor<Lookup> constructor = MethodHandles.Lookup.class
					.getDeclaredConstructor(Class.class, int.class);

			if (!constructor.isAccessible()) {
				constructor.setAccessible(true);
			}

			return constructor;
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private final PropertyLoader loader;
	private final Log log;
	private final PropertyAccessorConfiguration<A> source;
	private final A proxy;

	/**
	 * TODO GC of bundle cache if all properties are loaded & none localized!
	 */
	private final Map<PropertyAccessorConfiguration<?>, PropertyResource> bundleCache = new ConcurrentHashMap<>();
	private final Map<MethodSignature, PropertyValueDelegate<A>> valueDelegateCache = new ConcurrentHashMap<>();

	/**
	 * @param loader
	 *          which created the delegate, to call back to
	 * @param log
	 *          the log, or null
	 * @param source
	 *          the property accessor class and configuration
	 */
	public PropertyAccessorDelegate(
			PropertyLoader loader,
			Log log,
			PropertyAccessorConfiguration<A> source) {
		this.loader = loader;
		this.log = log != null ? log : (l, m) -> {};
		this.source = source;

		if (!source.getAccessor().isInterface()) {
			PropertyLoaderException e = new PropertyLoaderException(
					getText().mustBeInterface(source.getAccessor()));
			log.log(Level.ERROR, e);
			throw e;
		}

		proxy = createProxy(source.getAccessor());

		initialize();
	}

	PropertyLoader getLoader() {
		return loader;
	}

	PropertyAccessorConfiguration<A> getSource() {
		return source;
	}

	private PropertyLoaderProperties getText() {
		return loader.getProperties();
	}

	private void initialize() {
		for (Method method : source.getAccessor().getMethods()) {
			MethodSignature signature = new MethodSignature(method);

			if (!DIRECT_METHODS.contains(signature) && !method.isDefault()) {
				PropertyConfiguration methodConfiguration = method
						.getAnnotation(PropertyConfiguration.class);

				Evaluation evaluate = source.getConfiguration().evaluation();
				if (methodConfiguration != null
						&& methodConfiguration.evaluation() != Evaluation.UNSPECIFIED) {
					evaluate = methodConfiguration.evaluation();
				}

				if (evaluate == Evaluation.IMMEDIATE) {
					loadPropertyValueDelegate(signature);
				}
			}
		}
	}

	private PropertyValueDelegate<A> loadPropertyValueDelegate(MethodSignature signature) {
		return valueDelegateCache.computeIfAbsent(signature, s -> new PropertyValueDelegate<>(this, s));
	}

	private Object getInstantiatedPropertyValue(MethodSignature signature, Object... arguments) {
		List<?> argumentList;
		if (arguments == null) {
			argumentList = emptyList();
		} else {
			argumentList = asList(arguments);
		}

		try {
			return loadPropertyValueDelegate(signature).getValue(argumentList);
		} catch (Exception e) {
			/*
			 * Extra layer of protection for internal properties, so things can still
			 * function if there is a problem retrieving them...
			 */
			if (source.getAccessor().equals(PropertyLoaderProperties.class)) {
				try {
					return signature.method().invoke(new DefaultPropertyLoaderProperties(), arguments);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
					throw new RuntimeException(e1);
				}
			} else {
				throw e;
			}
		}
	}

	@SuppressWarnings("unchecked")
	<U> PropertyAccessorConfiguration<U> getPropertiesConfigurationUnsafe(Class<?> returnType) {
		return new PropertyAccessorConfiguration<>((Class<U>) returnType);
	}

	<T> Function<List<?>, T> parseValueString(
			PropertyAccessorConfiguration<A> source,
			AnnotatedType propertyType,
			String key,
			Locale locale) {
		@SuppressWarnings("unchecked")
		PropertyValueProvider<T> provider = (PropertyValueProvider<T>) loader
				.getValueProvider(propertyType)
				.orElseThrow(
						() -> new PropertyLoaderException(
								getText().propertyValueTypeNotSupported(propertyType, key)));

		try {
			String valueString = loadValueString(source, key, locale);

			return arguments -> provider.getParser(arguments).parse(valueString);
		} catch (MissingResourceException e) {
			if (source.getConfiguration().defaults() != Defaults.IGNORE && provider.providesDefault()) {
				return arguments -> provider.getDefault(key, arguments);
			}
			PropertyLoaderException ple = new PropertyLoaderException(
					getText().translationNotFoundMessage(key),
					e);
			log.log(Level.WARN, ple);
			throw ple;
		}
	}

	@SuppressWarnings("unchecked")
	private String loadValueString(
			PropertyAccessorConfiguration<?> configuration,
			String key,
			Locale locale) {
		return bundleCache.computeIfAbsent(configuration, c -> {
			return loader
					.getResourceStrategy(c.getConfiguration().strategy())
					.getPropertyResourceBundle(c.getAccessor(), c.getConfiguration().resource());
		}).getValue(key, locale);
	}

	@SuppressWarnings("unchecked")
	A createProxy(Class<A> accessor) {
		ClassLoader classLoader = new PropertyAccessorClassLoader(accessor.getClassLoader());

		return (A) Proxy.newProxyInstance(
				classLoader,
				new Class<?>[] { accessor },
				(Object p, Method method, Object[] args) -> {
					MethodSignature signature = new MethodSignature(method);

					if (DIRECT_METHODS.contains(signature)) {
						return method.invoke(PropertyAccessorDelegate.this, args);
					}

					if (method.isDefault()) {
						return METHOD_HANDLE_CONSTRUCTOR
								.newInstance(method.getDeclaringClass(), MethodHandles.Lookup.PRIVATE)
								.unreflectSpecial(method, method.getDeclaringClass())
								.bindTo(p)
								.invokeWithArguments(args);
					}

					return getInstantiatedPropertyValue(signature, args);
				});
	}

	class PropertyAccessorClassLoader extends ClassLoader {
		public PropertyAccessorClassLoader(ClassLoader classLoader) {
			super(classLoader);
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			if (name.equals(PropertyAccessorDelegate.class.getName())) {
				return PropertyAccessorDelegate.class;
			} else {
				return super.findClass(name);
			}
		}
	}

	public A getProxy() {
		return proxy;
	}
}

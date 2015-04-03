/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.osgi.
 *
 * uk.co.strangeskies.utilities.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.osgi.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.osgi;

import java.util.Map;

import org.osgi.framework.Constants;

/**
 * 
 * @author Elias N Vasylenko
 * 
 * @param <T>
 *          This is the type of the service this wrapper is designed to be
 *          applied to.
 */
public interface ServiceWrapper<T> {
	/**
	 * <p>
	 * This method should return an implementation of the service class wrapping
	 * the implementation provided as a parameter. It doesn't matter if a
	 * different object is returned by multiple calls providing an identical
	 * 'service' parameter.
	 * </p>
	 * 
	 * <p>
	 * Classes published under this service with higher property values for the
	 * key {@link Constants#SERVICE_RANKING} will be applied first. Multiple
	 * service wrappers on the same service class T will stack in the order this
	 * creates. For a class which registers itself under multiple services, this
	 * order will be maintained across each wrapping service. For ServiceWrapper
	 * services with the same ranking, those with the
	 * {@link ServiceWrapper#HIDE_SERVICES} property set to ALWAYS will be
	 * selected preferentially, then WHEN_WRAPPED, otherwise the ordering is
	 * arbitrary.
	 * </p>
	 * 
	 * <p>
	 * Bear in mind that any method calls on the underlying object by the
	 * registering context will not be wrapped, only calls from the service
	 * consumer context will be wrapped. This means, for example, that calls from
	 * the OSGi framework on declarative service objects on deactivation or on
	 * reference binding will be made directly on the underlying service object.
	 * </p>
	 * 
	 * @param service
	 *          The service implementation to be wrapped.
	 * @return The wrapping implementation.
	 */
	public T wrapService(/*  */T service);

	/**
	 * <p>
	 * This method will be called as a service wrapped using this class is being
	 * unregistered. The parameter passed to this method is the same object as was
	 * originally passed as a parameter to
	 * {@link ServiceWrapper#wrapService(Object)}, rather than the wrapper object
	 * returned from that method.
	 * </p>
	 * 
	 * <p>
	 * Calls to this method will always precede, and correspond 1:1, with calls to
	 * the wrapping method.
	 * </p>
	 * 
	 * @param service
	 *          The service implementation which has been wrapped and is now being
	 *          unregistered.
	 */
	public void unwrapService(T service);

	/**
	 * <p>
	 * This method will be called directly before the wrapper is applied to a
	 * service through {@link ServiceWrapper#wrapService(Object)}, and then once
	 * again each time any of the properties of the wrapped service change.
	 * </p>
	 * 
	 * <p>
	 * The map passed as an argument will contain the properties of the service to
	 * be wrapped. Any changes made to this map will be reflected in the
	 * properties of the wrapping service if it succeeds. Typically no changes
	 * will need to be made, though an example of a sensible change would be to
	 * increase the {@link Constants#SERVICE_RANKING} so the wrapping service
	 * takes precedence over the original.
	 * </p>
	 * 
	 * <p>
	 * Values for the {@link Constants#OBJECTCLASS} and
	 * {@link Constants#SERVICE_ID} keys can not be changed. These values are set
	 * by the Framework when the service is registered in the OSGi environment.
	 * </p>
	 * 
	 * <p>
	 * {@link ServiceWrapper#wrapService(Object)} will only ever be called to wrap
	 * a service if this method returns 'true' for the properties of that service.
	 * This can act as a filter for the service wrapper, such that it is only
	 * applied when certain conditions are met.
	 * </p>
	 * 
	 * @param serviceProperties
	 *          The properties of the service to be wrapped.
	 * @return True if a service can be wrapped with the given properties, false
	 *         otherwise.
	 */
	public boolean wrapServiceProperties(
			Map<String, /*  */Object> serviceProperties);

	/**
	 * This method should return the target class of services to be wrapped.
	 * 
	 * @return The class of the service objects dealt with.
	 */
	public Class<T> getServiceClass();

	/**
	 * <p>
	 * Wrappers which maintain state through some sort of decorator should
	 * normally be careful not set this value to {@link HideServices#NEVER}, as
	 * they won't be able to guarantee they are not subverted, with the wrapped
	 * service being manipulated without their knowledge.
	 * </p>
	 * 
	 * <p>
	 * The default value when none is provided is
	 * {@link HideServices#WHEN_WRAPPED}.
	 * </p>
	 */
	public static final String HIDE_SERVICES = "hide.services";

	/**
	 * <p>
	 * Enumeration of possible values for the {@link ServiceWrapper#HIDE_SERVICES}
	 * property of any {@link ServiceWrapper} services.
	 * </p>
	 * 
	 * @author Elias N Vasylenko
	 * 
	 */
	public enum HideServices {
		/**
		 * <p>
		 * Always remove services applicable by class from the service registry, so
		 * only the wrapping service will be visible. Be careful, as this will act
		 * as a filter, only registering wrapping services for those which are
		 * successfully wrapped, simply removing them otherwise. If no services are
		 * matched by {@link ServiceWrapper#wrapServiceProperties(Map)} then none
		 * will be available to any bundles, no matter what service ranking the
		 * wrapper has.
		 * </p>
		 * 
		 * <p>
		 * The default value when none is provided is
		 * {@link HideServices#WHEN_WRAPPED}.
		 * </p>
		 */
		ALWAYS,
		/**
		 * Never remove wrapped services from the service registry, so both will be
		 * visible.
		 */
		NEVER,
		/**
		 * This value indicates that services will only be hidden in the case that a
		 * valid wrap will be provided instead, which is often safer than
		 * {@link HideServices#ALWAYS}.
		 */
		WHEN_WRAPPED,
		/**
		 * This option behaves in the same way as {@link HideServices#WHEN_WRAPPED},
		 * with the added stipulation that wrapping and unwrapping will be invisible
		 * to bundles which are already using the services being wrapped.
		 */
		SILENTLY;
	}

	/**
	 * <p>
	 * If a property with this key is present on a {@link ServiceWrapper} service
	 * then the value should be of the type {@link Boolean}. This value then
	 * determines whether a wrapper be applied retroactively to services which
	 * already exist, and therefore may already be in use by other bundles.
	 * 
	 * If the {@link HideServices} property is set to {@link HideServices#NEVER},
	 * this value is ignored.
	 * </p>
	 * 
	 * <p>
	 * Wrappers which maintain state should normally not set this value to
	 * {@link Boolean#TRUE}, as they won't generally have any way to determine the
	 * state they should adopt at the point at which they are added, since they
	 * may be added and removed multiple times, through multiple calls to
	 * {@link ServiceWrapper#wrapService(Object)}.
	 * </p>
	 * 
	 * <p>
	 * If this property is set to {@link Boolean#FALSE} then existing services
	 * will not be wrapped, so the wrapper can be sure that
	 * {@link ServiceWrapper#wrapService(Object)} is only ever called once for any
	 * service, at the point when that services is registered.
	 * </p>
	 * 
	 * <p>
	 * If this property is set to {@link Boolean#TRUE} then wrappers may be
	 * removed and reapplied to maintain {@link Constants#SERVICE_RANKING} order.
	 * Otherwise, wrapping services will ignore reorderings unless their raking
	 * drops below that of the service they are wrapping, in which case they will
	 * be unregistered.
	 * </p>
	 * 
	 * <p>
	 * Implementations of {@link ServiceWrapperManager} may choose to not accept
	 * or to ignore wrappers with this property set to true, as it may be
	 * necessary to incur a slight overhead over the entire service-framework in
	 * order to support this feature (all services may need to be proxied
	 * preemptively). Managers which are registered as a service themselves should
	 * advertise whether they support this feature with the
	 * {@link ServiceWrapperManager#SUPPORTS_WRAP_EXISTING_SERVICES} property. It
	 * should also be noted that an implementation of
	 * {@link ServiceWrapperManager} may only be able to wrap services which were
	 * registered after the manager was registered or created itself.
	 * </p>
	 */
	public static final String WRAP_EXISTING_SERVICES = "wrap.existing.services";
}

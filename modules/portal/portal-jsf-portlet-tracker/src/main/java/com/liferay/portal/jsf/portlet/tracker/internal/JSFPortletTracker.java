/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.jsf.portlet.tracker.internal;

import com.liferay.portal.jsf.portlet.tracker.JSFPortlet;
import java.util.Hashtable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import javax.portlet.Portlet;
import java.util.Dictionary;

/**
 * @author Carlos Sierra Andrés
 */
@Component(immediate=true)
public class JSFPortletTracker {

	private ServiceTracker<
		JSFPortlet, ServiceTracker<?, ServiceRegistration<Portlet>>>
			_jsfPortletServiceTrackerServiceTracker;

	@Activate
	protected void activate(final BundleContext bundleContext) {
		_jsfPortletServiceTrackerServiceTracker = new ServiceTracker<>(
			bundleContext, JSFPortlet.class,
			new JSFPortletServiceTrackerServiceTrackerCustomizer(
				bundleContext));

		_jsfPortletServiceTrackerServiceTracker.open();
	}

	@Deactivate
	protected void deactivate() {
		_jsfPortletServiceTrackerServiceTracker.close();
	}

	private static class JSFPortletServiceTrackerServiceTrackerCustomizer
			implements ServiceTrackerCustomizer<
				JSFPortlet, ServiceTracker<?, ServiceRegistration<Portlet>>> {

		private final BundleContext _bundleContext;

		public JSFPortletServiceTrackerServiceTrackerCustomizer(
			BundleContext bundleContext) {

			_bundleContext = bundleContext;
		}

		@Override
		public ServiceTracker<?, ServiceRegistration<Portlet>> addingService(
			final ServiceReference<JSFPortlet> serviceReference) {

			final JSFPortlet jsfPortlet = _bundleContext.getService(
				serviceReference);

			Bundle bundle = serviceReference.getBundle();

			String filterString = "(&(osgi.http.whiteboard.servlet.name=" +
				"Faces Servlet)(service.bundleId=" + bundle.getBundleId() +
					"))";

			Filter filter;

			try {
				filter = _bundleContext.createFilter(filterString);
			}
			catch (InvalidSyntaxException e) {
				throw new RuntimeException(e);
			}

			String[] propertyKeys = serviceReference.getPropertyKeys();

			Dictionary<String, Object> properties = new Hashtable<>();

			for (String propertyKey : propertyKeys) {
				properties.put(
					propertyKey, serviceReference.getProperty(propertyKey));
			}

			ServiceTracker<?, ServiceRegistration<Portlet>> serviceTracker =
				new ServiceTracker<>(
					_bundleContext, filter,
					new ServiceRegistrationServiceTrackerCustomizer(
						jsfPortlet, properties));

			serviceTracker.open();

			return serviceTracker;
		}

		@Override
		public void modifiedService(
			ServiceReference<JSFPortlet> serviceReference,
			ServiceTracker<?, ServiceRegistration<Portlet>> serviceTracker) {

			removedService(serviceReference, serviceTracker);
			addingService(serviceReference);
		}

		@Override
		public void removedService(
			ServiceReference<JSFPortlet> serviceReference,
			ServiceTracker<?, ServiceRegistration<Portlet>> serviceTracker) {

			serviceTracker.close();

			_bundleContext.ungetService(serviceReference);
		}

		private class ServiceRegistrationServiceTrackerCustomizer
			implements ServiceTrackerCustomizer<
				Object, ServiceRegistration<Portlet>> {

			private final JSFPortlet _jsfPortlet;
			private Dictionary<String, Object> _properties;

			public ServiceRegistrationServiceTrackerCustomizer(
				JSFPortlet jsfPortlet, Dictionary<String, Object> properties) {

				_jsfPortlet = jsfPortlet;
				_properties = properties;
			}

			@Override
			public ServiceRegistration<Portlet> addingService(
				ServiceReference<Object> reference) {

				return _bundleContext.registerService(
					Portlet.class, _jsfPortlet, _properties);
			}

			@Override
			public void modifiedService(
				ServiceReference<Object> serviceReference,
				ServiceRegistration<Portlet> serviceRegistration) {

				removedService(serviceReference, serviceRegistration);
				addingService(serviceReference);
			}

			@Override
			public void removedService(
				ServiceReference<Object> serviceReference,
				ServiceRegistration<Portlet> serviceRegistration) {

				serviceRegistration.unregister();
			}
		}
	}
}

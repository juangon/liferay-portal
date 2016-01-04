/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.wab.extender.internal.adapter.listener;

import com.liferay.portal.portlet.tracker.ServletContextWrapper;
import com.liferay.portal.wab.extender.internal.adapter.TCCLUtil;

import java.util.concurrent.Callable;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

/**
 *
 * @author Juan Gonzalez
 */
public class ServletRequestListenerExceptionAdapter
	implements EventListenerExceptionAdapter<ServletRequestListener> {

	public ServletRequestListenerExceptionAdapter(
		ServletRequestListener servletRequestListener) {

		_servletRequestListener = servletRequestListener;
	}

	public Exception getException() {
		return _exception;
	}

	@Override
	public String getListenerClassName() {
		return ServletRequestListener.class.getName();
	}

	@Override
	public ServletRequestListener getListenerInstance() {
		return new ServletRequestListener() {

			@Override
			public void requestDestroyed(
				final ServletRequestEvent servletRequestEvent) {

				try {
					TCCLUtil.wrapTCCL(
						new Callable<Void>() {

							@Override
							public Void call() throws Exception {
								ServletRequestEvent newServletRequestEvent =
									servletRequestEvent;

								Thread thread = Thread.currentThread();

								ClassLoader classLoader =
									thread.getContextClassLoader();

								ServletContext oldServletContext =
									servletRequestEvent.getServletContext();
								ServletContext newServletContext =
									new ServletContextWrapper(
										oldServletContext, classLoader);
								newServletRequestEvent =
										new ServletRequestEvent(
											newServletContext,
											servletRequestEvent.
												getServletRequest());

								_servletRequestListener.requestDestroyed(
									newServletRequestEvent);

								return null;
							}

						});
				}
				catch (Exception e) {
					_exception = e;
				}
			}

			@Override
			public void requestInitialized(
				final ServletRequestEvent servletRequestEvent) {

				try {
					TCCLUtil.wrapTCCL(
						new Callable<Void>() {

							@Override
							public Void call() throws Exception {

								ServletRequestEvent newServletRequestEvent =
									servletRequestEvent;

								Thread thread = Thread.currentThread();

								ClassLoader classLoader =
									thread.getContextClassLoader();

								ServletContext oldServletContext =
									servletRequestEvent.getServletContext();
								ServletContext newServletContext =
									new ServletContextWrapper(
										oldServletContext, classLoader);
								newServletRequestEvent =
										new ServletRequestEvent(
											newServletContext,
											servletRequestEvent.
												getServletRequest());
								_servletRequestListener.requestInitialized(
									newServletRequestEvent);

								return null;
							}

						});
				}
				catch (Exception e) {
					_exception = e;
				}
			}

		};
	}

	private Exception _exception;
	private final ServletRequestListener _servletRequestListener;

}
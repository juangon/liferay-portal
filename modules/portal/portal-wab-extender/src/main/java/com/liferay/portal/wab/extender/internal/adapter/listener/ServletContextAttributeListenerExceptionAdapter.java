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
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;

/**
 *
 * @author Juan Gonzalez
 */
public class ServletContextAttributeListenerExceptionAdapter
	implements EventListenerExceptionAdapter<ServletContextAttributeListener> {

	public ServletContextAttributeListenerExceptionAdapter(
		ServletContextAttributeListener servletContextAttributeListener) {

		_servletContextAttributeListener = servletContextAttributeListener;
	}

	public Exception getException() {
		return _exception;
	}

	@Override
	public String getListenerClassName() {
		return ServletContextAttributeListener.class.getName();
	}

	public ServletContextAttributeListener getListenerInstance() {
		return new ServletContextAttributeListener() {

			@Override
			public void attributeRemoved(final ServletContextAttributeEvent
				servletContextAttributeEvent) {

				try {
					TCCLUtil.wrapTCCL(
						new Callable<Void>() {

							@Override
							public Void call() throws Exception {
								Thread thread = Thread.currentThread();

								ClassLoader classLoader =
									thread.getContextClassLoader();

								ServletContext oldServletContext =
										servletContextAttributeEvent.
										getServletContext();
								ServletContext newServletContext =
										new ServletContextWrapper(
											oldServletContext, classLoader);
								ServletContextAttributeEvent
									newServletContextAttributeEvent =
										new ServletContextAttributeEvent(
												newServletContext,
												servletContextAttributeEvent.
													getName(),
												servletContextAttributeEvent.
													getSource());

								_servletContextAttributeListener.
									attributeRemoved(
										newServletContextAttributeEvent);

								return null;
							}

						});
				}
				catch (Exception e) {
					_exception = e;
				}
			}

			@Override
			public void attributeReplaced(final ServletContextAttributeEvent
				servletContextAttributeEvent) {

				try {
					TCCLUtil.wrapTCCL(
						new Callable<Void>() {

							@Override
							public Void call() throws Exception {

								Thread thread = Thread.currentThread();

								ClassLoader classLoader =
									thread.getContextClassLoader();

								ServletContext oldServletContext =
										servletContextAttributeEvent.
											getServletContext();
								ServletContext newServletContext =
										new ServletContextWrapper(
											oldServletContext, classLoader);
								ServletContextAttributeEvent
									newServletContextAttributeEvent =
										new ServletContextAttributeEvent(
												newServletContext,
												servletContextAttributeEvent.
													getName(),
												servletContextAttributeEvent.
													getSource());
								_servletContextAttributeListener.
									attributeReplaced(
										newServletContextAttributeEvent);

								return null;
							}

						});
				}
				catch (Exception e) {
					_exception = e;
				}
			}

			@Override
			public void attributeAdded(final ServletContextAttributeEvent
				servletContextAttributeEvent) {

				try {
					TCCLUtil.wrapTCCL(
						new Callable<Void>() {

							@Override
							public Void call() throws Exception {

								Thread thread = Thread.currentThread();

								ClassLoader classLoader =
									thread.getContextClassLoader();

								ServletContext oldServletContext =
										servletContextAttributeEvent.
											getServletContext();
								ServletContext newServletContext =
										new ServletContextWrapper(
											oldServletContext, classLoader);
								ServletContextAttributeEvent
									newServletContextAttributeEvent =
										new ServletContextAttributeEvent(
												newServletContext,
												servletContextAttributeEvent.
													getName(),
												servletContextAttributeEvent.
													getSource());

								_servletContextAttributeListener.attributeAdded(
									newServletContextAttributeEvent);

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
	private final ServletContextAttributeListener
		_servletContextAttributeListener;

}
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
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;

/**
 *
 * @author Juan Gonzalez
 */
public class ServletRequestAttributeListenerExceptionAdapter
	implements EventListenerExceptionAdapter<ServletRequestAttributeListener> {

	public ServletRequestAttributeListenerExceptionAdapter(
		ServletRequestAttributeListener servletRequestAttributeListener) {

		_servletRequestAttributeListener = servletRequestAttributeListener;
	}

	public Exception getException() {
		return _exception;
	}

	@Override
	public String getListenerClassName() {
		return ServletRequestAttributeListener.class.getName();
	}

	public ServletRequestAttributeListener getListenerInstance() {
		return new ServletRequestAttributeListener() {

			@Override
			public void attributeRemoved(final ServletRequestAttributeEvent
				servletRequestAttributeEvent) {

				try {
					TCCLUtil.wrapTCCL(
						new Callable<Void>() {

							@Override
							public Void call() throws Exception {
								Thread thread = Thread.currentThread();

								ClassLoader classLoader =
									thread.getContextClassLoader();

								ServletContext oldServletContext =
										servletRequestAttributeEvent.
											getServletContext();
								ServletContext newServletContext =
										new ServletContextWrapper(
											oldServletContext, classLoader);
								ServletRequestAttributeEvent
									newServletRequestAttributeEvent =
										new ServletRequestAttributeEvent(
												newServletContext,
												servletRequestAttributeEvent.
													getServletRequest(),
												servletRequestAttributeEvent.
													getName(),
												servletRequestAttributeEvent.
													getSource());

								_servletRequestAttributeListener.
									attributeRemoved(
										newServletRequestAttributeEvent);

								return null;
							}

						});
				}
				catch (Exception e) {
					_exception = e;
				}
			}

			@Override
			public void attributeReplaced(final ServletRequestAttributeEvent
				servletRequestAttributeEvent) {

				try {
					TCCLUtil.wrapTCCL(
						new Callable<Void>() {

							@Override
							public Void call() throws Exception {

								Thread thread = Thread.currentThread();

								ClassLoader classLoader =
									thread.getContextClassLoader();

								ServletContext oldServletContext =
										servletRequestAttributeEvent.
											getServletContext();
								ServletContext newServletContext =
										new ServletContextWrapper(
											oldServletContext, classLoader);
								ServletRequestAttributeEvent
									newServletRequestAttributeEvent =
										new ServletRequestAttributeEvent(
												newServletContext,
												servletRequestAttributeEvent.
													getServletRequest(),
												servletRequestAttributeEvent.
													getName(),
												servletRequestAttributeEvent.
													getSource());

								_servletRequestAttributeListener.
									attributeReplaced(
										newServletRequestAttributeEvent);

								return null;
							}

						});
				}
				catch (Exception e) {
					_exception = e;
				}
			}

			@Override
			public void attributeAdded(final ServletRequestAttributeEvent
				servletRequestAttributeEvent) {

				try {
					TCCLUtil.wrapTCCL(
						new Callable<Void>() {

							@Override
							public Void call() throws Exception {

								Thread thread = Thread.currentThread();

								ClassLoader classLoader =
									thread.getContextClassLoader();

								ServletContext oldServletContext =
										servletRequestAttributeEvent.
											getServletContext();
								ServletContext newServletContext =
										new ServletContextWrapper(
											oldServletContext, classLoader);
								ServletRequestAttributeEvent
									newServletRequestAttributeEvent =
										new ServletRequestAttributeEvent(
												newServletContext,
												servletRequestAttributeEvent.
													getServletRequest(),
												servletRequestAttributeEvent.
													getName(),
												servletRequestAttributeEvent.
													getSource());

								_servletRequestAttributeListener.attributeAdded(
									newServletRequestAttributeEvent);

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
	private final ServletRequestAttributeListener
		_servletRequestAttributeListener;

}
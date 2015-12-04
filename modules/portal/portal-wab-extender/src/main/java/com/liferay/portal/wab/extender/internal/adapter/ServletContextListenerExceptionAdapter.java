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

package com.liferay.portal.wab.extender.internal.adapter;

import java.util.concurrent.Callable;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Raymond Augé
 */
public class ServletContextListenerExceptionAdapter
	implements ServletContextListener {

	public ServletContextListenerExceptionAdapter(
		ServletContextListener servletContextListener) {

		_servletContextListener = servletContextListener;
	}

	public ServletContext getServletContext() {
		return _servletContext;
	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		try {
			TCCLUtil.wrapTCCL(
					new Callable<Void>() {

						@Override
						public Void call() throws Exception {
								_servletContextListener.contextDestroyed(servletContextEvent);
								
								_servletContext = servletContextEvent.getServletContext();
								
								return null;
						}

					});
		}
		catch (Exception e) {
			_exception = e;
		}
	}

	@Override
	public void contextInitialized(
		final ServletContextEvent servletContextEvent) {

		try {
			TCCLUtil.wrapTCCL(
				new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						_servletContextListener.contextInitialized(
							servletContextEvent);

						_servletContext = servletContextEvent.getServletContext();
						return null;
					}

				});
		}
		catch (Exception e) {
			_exception = e;
		}
	}

	public Exception getException() {
		return _exception;
	}

	private Exception _exception;
	private final ServletContextListener _servletContextListener;
	private ServletContext _servletContext;

}
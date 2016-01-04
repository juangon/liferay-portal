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

import com.liferay.portal.portlet.tracker.ServletContextWrapper;

import java.io.IOException;

import java.util.Enumeration;
import java.util.concurrent.Callable;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author Raymond Augé
 */
public class ServletExceptionAdapter implements Servlet {

	public ServletExceptionAdapter(Servlet servlet) {
		_servlet = servlet;
	}

	@Override
	public void destroy() {
		try {
			TCCLUtil.wrapTCCL(
				new Callable<Void>() {

					@Override
					public Void call() throws Exception {

						_servlet.destroy();

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

	@Override
	public ServletConfig getServletConfig() {
		return _servlet.getServletConfig();
	}

	@Override
	public String getServletInfo() {
		return _servlet.getServletInfo();
	}

	@Override
	public void init(final ServletConfig servletConfig) {
		try {
			TCCLUtil.wrapTCCL(
				new Callable<Void>() {

					@Override
					public Void call() throws Exception {

						Thread thread = Thread.currentThread();

						ClassLoader classLoader =
							thread.getContextClassLoader();

						final ServletContext newServletContext =
								new ServletContextWrapper(
										servletConfig.getServletContext(),
										classLoader);

						ServletConfig newServletConfig = new ServletConfig() {

							@Override
							public String getServletName() {
								return servletConfig.getServletName();
							}

							@Override
							public ServletContext getServletContext() {
								return newServletContext;
							}

							@Override
							public Enumeration<String> getInitParameterNames() {
								return servletConfig.getInitParameterNames();
							}

							@Override
							public String getInitParameter(String name) {
								return servletConfig.getInitParameter(name);
							}

						};

						_servlet.init(newServletConfig);

						return null;
					}

				});
		}
		catch (Exception e) {
			_exception = e;
		}
	}

	@Override
	public void service(
			ServletRequest servletRequest, ServletResponse servletResponse)
		throws IOException, ServletException {

		_servlet.service(servletRequest, servletResponse);
	}

	private Exception _exception;
	private final Servlet _servlet;

}
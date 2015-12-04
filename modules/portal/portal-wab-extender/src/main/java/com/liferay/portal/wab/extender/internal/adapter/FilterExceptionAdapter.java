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

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.Callable;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author Raymond Augé
 */
public class FilterExceptionAdapter implements Filter {

	public FilterExceptionAdapter(Filter filter, ServletContext servletContext) {
		_filter = filter;
		_servletContext = servletContext;
	}

	@Override
	public void destroy() {
		_filter.destroy();
	}

	@Override
	public void doFilter(
			ServletRequest servletRequest, ServletResponse servletResponse,
			FilterChain filterChain)
		throws IOException, ServletException {

		_filter.doFilter(servletRequest, servletResponse, filterChain);
	}

	public Exception getException() {
		return _exception;
	}

	public ServletContext getServletContext() {
		return _servletContext;
	}
	
	@Override
	public void init(final FilterConfig filterConfig) {
		try {
			TCCLUtil.wrapTCCL(
				new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						FilterConfig newFilterConfig = new FilterConfig() {
							
							@Override
							public ServletContext getServletContext() {
								if (_servletContext != null) {
									return _servletContext;
								}else {
									return filterConfig.getServletContext();
								}
							}
							
							@Override
							public Enumeration<String> getInitParameterNames() {
								return filterConfig.getInitParameterNames();
							}
							
							@Override
							public String getInitParameter(String name) {
								return filterConfig.getInitParameter(name);
							}
							
							@Override
							public String getFilterName() {
								return filterConfig.getFilterName();
							}
						};
						
						_filter.init(newFilterConfig);

						_servletContext = newFilterConfig.getServletContext();
						
						return null;
					}

				});
		}
		catch (Exception e) {
			_exception = e;
		}
	}

	private Exception _exception;
	private final Filter _filter;
	private ServletContext _servletContext;

}
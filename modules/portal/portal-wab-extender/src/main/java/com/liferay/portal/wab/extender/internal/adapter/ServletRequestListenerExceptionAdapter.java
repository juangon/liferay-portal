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
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import com.liferay.portal.wab.extender.internal.ServletContextWrapper;

/**
 * @author Juan Gonzalez
 */
public class ServletRequestListenerExceptionAdapter 
	implements ServletRequestListener {

	public ServletRequestListenerExceptionAdapter(
		ServletRequestListener servletRequestListener, ClassLoader classLoader) {

		_servletRequestListener = servletRequestListener;
		_classLoader = classLoader;
	}

	
	public ServletRequestListener getEventListener() {
		return _servletRequestListener;
	}
	
	@Override
	public void requestDestroyed(final ServletRequestEvent servletRequestEvent) {
		try {
			TCCLUtil.wrapTCCL(
				new Callable<Void>() {

						@Override
					public Void call() throws Exception {
							ServletContext oldServletContext = servletRequestEvent.getServletContext();
							ServletContext newServletContext = new ServletContextWrapper(oldServletContext, _classLoader);
							ServletRequestEvent newServletRequesttEvent = new ServletRequestEvent(newServletContext, servletRequestEvent.getServletRequest());
							_servletRequestListener.requestDestroyed(newServletRequesttEvent);
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
						
						ServletContext oldServletContext = servletRequestEvent.getServletContext();
						ServletContext newServletContext = new ServletContextWrapper(oldServletContext, _classLoader);
						ServletRequestEvent newServletRequesttEvent = new ServletRequestEvent(newServletContext, servletRequestEvent.getServletRequest());
						_servletRequestListener.requestInitialized(newServletRequesttEvent);

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
	private ServletRequestListener _servletRequestListener;
	private final ClassLoader _classLoader;
	

}
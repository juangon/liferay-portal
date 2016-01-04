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

import com.liferay.portal.wab.extender.internal.adapter.TCCLUtil;

import java.util.concurrent.Callable;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 *
 * @author Juan Gonzalez
 */
public class SessionListenerExceptionAdapter
	implements EventListenerExceptionAdapter<HttpSessionListener> {

	public SessionListenerExceptionAdapter(
		HttpSessionListener httpSessionListener) {

		_httpSessionListener = httpSessionListener;
	}

	public Exception getException() {
		return _exception;
	}

	@Override
	public String getListenerClassName() {
		return HttpSessionListener.class.getName();
	}

	@Override
	public HttpSessionListener getListenerInstance() {
		return new HttpSessionListener() {

			@Override
			public void sessionDestroyed(
				final HttpSessionEvent httpSessionEvent) {

				try {
					TCCLUtil.wrapTCCL(
						new Callable<Void>() {

							@Override
							public Void call() throws Exception {
								_httpSessionListener.sessionDestroyed(
									httpSessionEvent);

								return null;
							}

						});
				}
				catch (Exception e) {
					_exception = e;
				}
			}

			@Override
			public void sessionCreated(
				final HttpSessionEvent httpSessionEvent) {

				try {
					TCCLUtil.wrapTCCL(
						new Callable<Void>() {

							@Override
							public Void call() throws Exception {

								_httpSessionListener.sessionCreated(
									httpSessionEvent);

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
	private final HttpSessionListener _httpSessionListener;

}
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

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

/**
 *
 * @author Juan Gonzalez
 */
public class SessionAttributeListenerExceptionAdapter
	implements EventListenerExceptionAdapter<HttpSessionAttributeListener> {

	public SessionAttributeListenerExceptionAdapter(
		HttpSessionAttributeListener httpSesssionAttributeListener) {

		_httpSessionAttributeListener = httpSesssionAttributeListener;
	}

	public Exception getException() {
		return _exception;
	}

	@Override
	public String getListenerClassName() {
		return HttpSessionAttributeListener.class.getName();
	}

	@Override
	public HttpSessionAttributeListener getListenerInstance() {
		return new HttpSessionAttributeListener() {

			@Override
			public void attributeRemoved(
				final HttpSessionBindingEvent httpSessionBindingEvent) {

				try {
					TCCLUtil.wrapTCCL(
						new Callable<Void>() {

							@Override
							public Void call() throws Exception {
								_httpSessionAttributeListener.attributeRemoved(
									httpSessionBindingEvent);

								return null;
							}

						});
				}
				catch (Exception e) {
					_exception = e;
				}
			}

			@Override
			public void attributeReplaced(
				final HttpSessionBindingEvent httpSessionBindingEvent) {

				try {
					TCCLUtil.wrapTCCL(
						new Callable<Void>() {

							@Override
							public Void call() throws Exception {

								_httpSessionAttributeListener.attributeReplaced(
									httpSessionBindingEvent);

								return null;
							}

						});
				}
				catch (Exception e) {
					_exception = e;
				}
			}

			@Override
			public void attributeAdded(
				final HttpSessionBindingEvent httpSessionBindingEvent) {

				try {
					TCCLUtil.wrapTCCL(
						new Callable<Void>() {

							@Override
							public Void call() throws Exception {

								_httpSessionAttributeListener.attributeAdded(
									httpSessionBindingEvent);

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
	private final HttpSessionAttributeListener _httpSessionAttributeListener;

}
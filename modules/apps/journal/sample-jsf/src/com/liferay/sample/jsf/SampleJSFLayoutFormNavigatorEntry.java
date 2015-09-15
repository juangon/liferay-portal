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

package com.liferay.sample.jsf;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.taglib.ui.BaseFormNavigatorEntry;
import com.liferay.portal.kernel.servlet.taglib.ui.FormNavigatorConstants;
import com.liferay.portal.kernel.servlet.taglib.ui.FormNavigatorEntry;
import com.liferay.portal.kernel.util.StringPool;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.faces.webapp.FacesServlet;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Locale;

/**
 * @author Julio Camarero
 */
@Component(
	property = {"service.ranking:Integer=10"},
	service = FormNavigatorEntry.class
)
public class SampleJSFLayoutFormNavigatorEntry extends BaseFormNavigatorEntry {

	private ServiceRegistration<Servlet> _servletServiceRegistration;

	@Activate
	protected void activate(BundleContext bundleContext) {
		FacesServlet facesServlet = new FacesServlet();

		Hashtable<String, Object> properties = new Hashtable<>();

		properties.put(
			HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,
			_servletContext.getServletContextName());

		properties.put(
			HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME,
			"Faces Servlet");
		properties.put(
			HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, "*.xhtml");

		_servletServiceRegistration = bundleContext.registerService(
			Servlet.class, facesServlet, properties);
	}

	@Deactivate
	protected void deactivate() {
		_servletServiceRegistration.unregister();
	}

	@Override
	public String getCategoryKey() {
		return StringPool.BLANK;
	}

	@Override
	public String getFormNavigatorId() {
		return FormNavigatorConstants.FORM_NAVIGATOR_ID_LAYOUT;
	}

	@Override
	public String getLabel(Locale locale) {
		return LanguageUtil.get(locale, getKey());
	}

	@Override
	public String getKey() {
		return "sample-jsf";
	}

	@Override
	public void include(
			HttpServletRequest request, HttpServletResponse response)
		throws IOException {

		ServletContext servletContext = getServletContext();

		RequestDispatcher requestDispatcher =
			servletContext.getRequestDispatcher(getJsfPath());

		try {
			requestDispatcher.include(request, response);
		}
		catch (ServletException se) {
			if (_log.isErrorEnabled()) {
				_log.error("Unable to include JSP", se);
			}

			throw new IOException("Unable to include " + getJsfPath(), se);
		}
	}

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.sample.jsf)",
		unbind = "-"
	)
	public void setServletContext(ServletContext servletContext) {
		_servletContext = servletContext;
	}

	public ServletContext getServletContext() {
		return _servletContext;
	}

	protected String getJsfPath() {
		return "/sample.xhtml";
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SampleJSFLayoutFormNavigatorEntry.class);

	protected ServletContext _servletContext;

}

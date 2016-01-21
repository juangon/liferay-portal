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

package com.liferay.portal.portlet.tracker;

import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

/**
 *
 * @author Juan Gonzalez
 *
 */
public class ServletContextWrapper implements ServletContext {

	public ServletContextWrapper(
		ServletContext servletContext, ClassLoader classLoader) {

		_servletContext = servletContext;
		_classLoader = classLoader;
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
		String filterName, Class<? extends Filter> filterClass) {

		return getWrapped().addFilter(filterName, filterClass);
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
		String filterName, Filter filter) {

		return getWrapped().addFilter(filterName, filter);
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
		String filterName, String className) {

		return getWrapped().addFilter(filterName, className);
	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		getWrapped().addListener(listenerClass);
	}

	@Override
	public void addListener(String className) {
		getWrapped().addListener(className);
	}

	@Override
	public <T extends EventListener> void addListener(T t) {
		getWrapped().addListener(t);
	}

	@Override
	public Dynamic addServlet(
		String servletName, Class<? extends Servlet> servletClass) {

		return getWrapped().addServlet(servletName, servletClass);
	}

	@Override
	public Dynamic addServlet(String servletName, Servlet servlet) {
		return getWrapped().addServlet(servletName, servlet);
	}

	@Override
	public Dynamic addServlet(String servletName, String className) {
		return getWrapped().addServlet(servletName, className);
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> clazz)
		throws ServletException {

		return getWrapped().createFilter(clazz);
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> clazz)
		throws ServletException {

		return getWrapped().createListener(clazz);
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> clazz)
		throws ServletException {

		return getWrapped().createServlet(clazz);
	}

	@Override
	public void declareRoles(String... roleNames) {
		getWrapped().declareRoles(roleNames);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (!(obj instanceof ServletContextWrapper)) {
			return super.equals(obj);
		}

		ServletContextWrapper servletContext = (ServletContextWrapper)obj;

		if (getServletContextName().equals(
				servletContext.getServletContextName()) &&
			getClassLoader().equals(servletContext.getClassLoader())) {

			return true;
		}

		return super.equals(obj);
	}

	@Override
	public Object getAttribute(String name) {
		return getWrapped().getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return getWrapped().getAttributeNames();
	}

	@Override
	public ClassLoader getClassLoader() {
		return _classLoader;
	}

	@Override
	public ServletContext getContext(String uripath) {
		return getWrapped().getContext(uripath);
	}

	@Override
	public String getContextPath() {
		return getWrapped().getContextPath();
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		return getWrapped().getDefaultSessionTrackingModes();
	}

	@Override
	public int getEffectiveMajorVersion() {
		return getWrapped().getEffectiveMajorVersion();
	}

	@Override
	public int getEffectiveMinorVersion() {
		return getWrapped().getEffectiveMinorVersion();
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		return getWrapped().getEffectiveSessionTrackingModes();
	}

	@Override
	public FilterRegistration getFilterRegistration(String filterName) {
		return getWrapped().getFilterRegistration(filterName);
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		return getWrapped().getFilterRegistrations();
	}

	@Override
	public String getInitParameter(String name) {
		return getWrapped().getInitParameter(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return getWrapped().getInitParameterNames();
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		return getWrapped().getJspConfigDescriptor();
	}

	@Override
	public int getMajorVersion() {
		return getWrapped().getMajorVersion();
	}

	@Override
	public String getMimeType(String file) {
		return getWrapped().getMimeType(file);
	}

	@Override
	public int getMinorVersion() {
		return getWrapped().getMinorVersion();
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		return getWrapped().getNamedDispatcher(name);
	}

	@Override
	public String getRealPath(String path) {
		return getWrapped().getRealPath(path);
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return getWrapped().getRequestDispatcher(path);
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		return getWrapped().getResource(path);
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		return getWrapped().getResourceAsStream(path);
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		return getWrapped().getResourcePaths(path);
	}

	@Override
	public String getServerInfo() {
		return getWrapped().getServerInfo();
	}

	@Override
	public Servlet getServlet(String name) throws ServletException {
		return getWrapped().getServlet(name);
	}

	@Override
	public String getServletContextName() {
		return getWrapped().getServletContextName();
	}

	@Override
	public Enumeration<String> getServletNames() {
		return getWrapped().getServletNames();
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		return getWrapped().getServletRegistration(servletName);
	}

	@Override
	public Map<String, ? extends ServletRegistration>
		getServletRegistrations() {

		return getWrapped().getServletRegistrations();
	}

	@Override
	public Enumeration<Servlet> getServlets() {
		return getWrapped().getServlets();
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		return getWrapped().getSessionCookieConfig();
	}

	public ServletContext getWrapped() {
		return _servletContext;
	}

	@Override
	public void log(Exception exception, String msg) {
		getWrapped().log(exception, msg);
	}

	@Override
	public void log(String msg) {
		getWrapped().log(msg);
	}

	@Override
	public void log(String message, Throwable throwable) {
		getWrapped().log(message, throwable);
	}

	@Override
	public void removeAttribute(String name) {
		getWrapped().removeAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object object) {
		getWrapped().setAttribute(name, object);
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		return getWrapped().setInitParameter(name, value);
	}

	@Override
	public void setSessionTrackingModes(
		Set<SessionTrackingMode> sessionTrackingModes) {

		getWrapped().setSessionTrackingModes(sessionTrackingModes);
	}

	private final ClassLoader _classLoader;
	private final ServletContext _servletContext;

}
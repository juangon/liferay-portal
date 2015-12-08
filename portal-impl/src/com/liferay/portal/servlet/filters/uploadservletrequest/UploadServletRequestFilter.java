/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

package com.liferay.portal.servlet.filters.uploadservletrequest;

import com.liferay.portal.kernel.portlet.LiferayPortletConfig;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.upload.UploadServletRequest;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.service.PortletLocalServiceUtil;
import com.liferay.portal.servlet.filters.BasePortalFilter;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.theme.ThemeDisplayFactory;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.InvokerPortlet;
import com.liferay.portlet.PortletInstanceFactoryUtil;

import java.util.Locale;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Preston Crary
 */
public class UploadServletRequestFilter extends BasePortalFilter {

	public static final String COPY_MULTIPART_STREAM_TO_FILE =
		UploadServletRequestFilter.class.getName() +
			"#COPY_MULTIPART_STREAM_TO_FILE";

	@Override
	public void processFilter(
			HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain)
		throws Exception {

		UploadServletRequest uploadServletRequest = null;

		String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);

		if ((contentType != null) &&
			contentType.startsWith(ContentTypes.MULTIPART_FORM_DATA)) {

			boolean initThemeDisplay = false;

			String portletId = ParamUtil.getString(request, "p_p_id");

			ThemeDisplay themeDisplay = (ThemeDisplay)
					request.getAttribute(WebKeys.THEME_DISPLAY);

			if (themeDisplay == null) {
				initThemeDisplay = true;
				themeDisplay = ThemeDisplayFactory.create();
			}

			if (Validator.isNotNull(portletId)) {

				Company company = PortalUtil.getCompany(request);

				if (initThemeDisplay) {
					themeDisplay.setCompany(company);
				}

				Portlet portlet = PortletLocalServiceUtil.getPortletById(
					company.getCompanyId(), portletId);

				if (portlet != null) {
					ServletContext servletContext =
						(ServletContext)request.getAttribute(WebKeys.CTX);

					InvokerPortlet invokerPortlet =
						PortletInstanceFactoryUtil.create(
							portlet, servletContext);

					LiferayPortletConfig liferayPortletConfig =
						(LiferayPortletConfig)invokerPortlet.getPortletConfig();

					if (invokerPortlet.isStrutsPortlet() ||
						liferayPortletConfig.isCopyRequestParameters() ||
						!liferayPortletConfig.isWARFile()) {

						request.setAttribute(
							UploadServletRequestFilter.
								COPY_MULTIPART_STREAM_TO_FILE,
							Boolean.FALSE);
					}
				}
			}

			if (initThemeDisplay) {
				Locale locale = PortalUtil.getLocale(request);
				long scopeGroupId = PortalUtil.getScopeGroupId(request);
				String portalURL = PortalUtil.getPortalURL(request);

				themeDisplay.setLocale(locale);
				themeDisplay.setPortalURL(portalURL);
				themeDisplay.setScopeGroupId(scopeGroupId);

				request.setAttribute(WebKeys.THEME_DISPLAY, themeDisplay);
			}

			uploadServletRequest = PortalUtil.getUploadServletRequest(request);
		}

		if (uploadServletRequest == null) {
			processFilter(
				UploadServletRequestFilter.class, request, response,
				filterChain);
		}
		else {
			try {
				processFilter(
					UploadServletRequestFilter.class, uploadServletRequest,
					response, filterChain);
			}
			finally {
				uploadServletRequest.cleanUp();
			}
		}
	}

}
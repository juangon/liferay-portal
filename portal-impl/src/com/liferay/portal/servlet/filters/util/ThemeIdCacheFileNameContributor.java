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

package com.liferay.portal.servlet.filters.util;

import com.liferay.portal.kernel.model.Theme;
import com.liferay.portal.kernel.service.ThemeLocalServiceUtil;
import com.liferay.portal.kernel.spring.osgi.OSGiBeanProperties;
import com.liferay.portal.kernel.util.Function;
import com.liferay.portal.kernel.util.KeyValuePair;
import com.liferay.portal.kernel.util.PortalUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Carlos Sierra Andrés
 */
@OSGiBeanProperties(property = "cache.file.name.contributor=true")
public class ThemeIdCacheFileNameContributor
	implements Function<HttpServletRequest, KeyValuePair> {

	@Override
	public KeyValuePair apply(HttpServletRequest request) {
		long companyId = PortalUtil.getCompanyId(request);
		String themeId = request.getParameter(_PARAMETER_NAME);

		Theme theme = ThemeLocalServiceUtil.fetchTheme(companyId, themeId);

		if (theme != null) {
			return new KeyValuePair(_PARAMETER_NAME, themeId);
		}

		return null;
	}

	private static final String _PARAMETER_NAME = "themeId";

}
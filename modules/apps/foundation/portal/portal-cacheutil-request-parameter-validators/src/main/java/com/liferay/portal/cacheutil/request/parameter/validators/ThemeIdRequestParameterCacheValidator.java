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

package com.liferay.portal.cacheutil.request.parameter.validators;

import com.liferay.portal.kernel.model.Theme;
import com.liferay.portal.kernel.service.ThemeLocalService;
import com.liferay.portal.kernel.util.Function;
import com.liferay.portal.kernel.util.KeyValuePair;
import com.liferay.portal.kernel.util.Portal;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Carlos Sierra Andrés
 */
@Component(immediate = true, property = "cache.file.name.contributor=true")
public class ThemeIdRequestParameterCacheValidator
	implements Function<HttpServletRequest, KeyValuePair> {

	@Override
	public KeyValuePair apply(HttpServletRequest request) {
		long companyId = _portal.getCompanyId(request);
		String themeId = request.getParameter(_PARAMETER_NAME);

		Theme theme = _themeLocalService.fetchTheme(companyId, themeId);

		if (theme != null) {
			return new KeyValuePair(_PARAMETER_NAME, themeId);
		}

		return null;
	}

	private static final String _PARAMETER_NAME = "themeId";

	@Reference
	private Portal _portal;

	@Reference
	private ThemeLocalService _themeLocalService;

}
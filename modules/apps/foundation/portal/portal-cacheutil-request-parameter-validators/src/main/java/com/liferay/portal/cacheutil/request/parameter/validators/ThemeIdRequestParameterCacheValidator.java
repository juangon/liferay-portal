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
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PredicateFilter;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Carlos Sierra Andrés
 */
@Component(immediate = true, property = "filter.request.parameter=themeId")
public class ThemeIdRequestParameterCacheValidator
	implements PredicateFilter<HttpServletRequest> {

	@Override
	public boolean filter(HttpServletRequest request) {
		long companyId = _portal.getCompanyId(request);

		Theme theme = _themeLocalService.fetchTheme(
			companyId, request.getParameter(_parameterName));

		if (theme != null) {
			return true;
		}

		return false;
	}

	@Activate
	protected void activate(Map<String, Object> properties) {
		Object parameterNameObject = properties.get("filter.request.parameter");

		_parameterName = parameterNameObject.toString();
	}

	private String _parameterName;

	@Reference
	private Portal _portal;

	@Reference
	private ThemeLocalService _themeLocalService;

}
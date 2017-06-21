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

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.Function;
import com.liferay.portal.kernel.util.KeyValuePair;
import com.liferay.portal.kernel.util.LocaleUtil;

import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

/**
 * @author Carlos Sierra Andrés
 */
@Component(immediate = true, property = "cache.file.name.contributor=true")
public class LanguageIdCacheFileNameContributor
	implements Function<HttpServletRequest, KeyValuePair> {

	@Override
	public KeyValuePair apply(HttpServletRequest request) {
		String languageId = request.getParameter(_PARAMETER_NAME);

		Set<Locale> availableLocales = LanguageUtil.getAvailableLocales();

		if (availableLocales.contains(LocaleUtil.fromLanguageId(languageId))) {
			return new KeyValuePair(_PARAMETER_NAME, languageId);
		}

		return null;
	}

	private static final String _PARAMETER_NAME = "languageId";

}
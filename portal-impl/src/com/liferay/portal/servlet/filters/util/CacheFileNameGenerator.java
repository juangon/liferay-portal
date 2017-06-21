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

import com.liferay.portal.kernel.cache.key.CacheKeyGenerator;
import com.liferay.portal.kernel.cache.key.CacheKeyGeneratorUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.Digester;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.kernel.util.Function;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.KeyValuePair;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.registry.collections.ServiceTrackerCollections;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.List;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Miguel Pastor
 * @author Carlos Sierra Andrés
 */
public class CacheFileNameGenerator {

	public static String getCacheFileName(
		final HttpServletRequest request, String cacheName, Log log) {

		CacheKeyGenerator cacheKeyGenerator =
			CacheKeyGeneratorUtil.getCacheKeyGenerator(cacheName);

		cacheKeyGenerator.append(HttpUtil.getProtocol(request.isSecure()));
		cacheKeyGenerator.append(StringPool.UNDERLINE);
		cacheKeyGenerator.append(request.getRequestURI());

		StringBundler queryStringSB = new StringBundler(
			_cacheFileNameContributors.size() * 4);

		HttpServletRequest readOnlyRequest = new ReadOnlyHttpServletRequest(
			request);

		for (Function<HttpServletRequest, KeyValuePair>
				cacheFileNameContributor : _cacheFileNameContributors) {

			KeyValuePair keyValuePair = cacheFileNameContributor.apply(
				readOnlyRequest);

			if (keyValuePair == null) {
				continue;
			}

			queryStringSB.append(StringPool.UNDERLINE);
			queryStringSB.append(keyValuePair.getKey());
			queryStringSB.append(StringPool.UNDERLINE);
			queryStringSB.append(keyValuePair.getValue());
		}

		cacheKeyGenerator.append(
			DigesterUtil.digestBase64(
				Digester.SHA_256, queryStringSB.toString()));

		return _sterilizeFileName(String.valueOf(cacheKeyGenerator.finish()));
	}

	private static String _sterilizeFileName(String fileName) {
		return StringUtil.replace(
			fileName,
			new char[] {
				CharPool.SLASH, CharPool.BACK_SLASH, CharPool.PLUS,
				CharPool.EQUAL
			},
			new char[] {
				CharPool.UNDERLINE, CharPool.UNDERLINE, CharPool.DASH,
				CharPool.UNDERLINE
			});
	}

	private static final List<Function<HttpServletRequest, KeyValuePair>>
		_cacheFileNameContributors =
			(List<Function<HttpServletRequest, KeyValuePair>>)(List<?>)
				ServiceTrackerCollections.openList(
					Function.class, "(cache.file.name.contributor=true)");

	private static class ReadOnlyHttpServletRequest
		extends HttpServletRequestWrapper {

		public ReadOnlyHttpServletRequest(HttpServletRequest request) {
			super(request);
		}

		@Override
		public boolean authenticate(HttpServletResponse response)
			throws IOException, ServletException {

			return false;
		}

		@Override
		public void login(String username, String password)
			throws ServletException {
		}

		@Override
		public void logout() throws ServletException {
		}

		@Override
		public void removeAttribute(String name) {
		}

		@Override
		public void setAttribute(String name, Object o) {
		}

		@Override
		public void setCharacterEncoding(String enc)
			throws UnsupportedEncodingException {
		}

		@Override
		public void setRequest(ServletRequest request) {
		}

		@Override
		public AsyncContext startAsync() throws IllegalStateException {
			throw new UnsupportedOperationException();
		}

		@Override
		public AsyncContext startAsync(
				ServletRequest servletRequest, ServletResponse servletResponse)
			throws IllegalStateException {

			throw new UnsupportedOperationException();
		}

	}

}
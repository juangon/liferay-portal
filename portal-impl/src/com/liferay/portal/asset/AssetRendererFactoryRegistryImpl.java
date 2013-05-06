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

package com.liferay.portal.asset;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.security.auth.CompanyThreadLocal;
import com.liferay.portal.service.PortletLocalServiceUtil;
import com.liferay.portlet.asset.AssetRendererFactoryRegistry;
import com.liferay.portlet.asset.model.AssetRendererFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Bruno Farache
 * @author Marcellus Tavares
 */
public class AssetRendererFactoryRegistryImpl
	implements AssetRendererFactoryRegistry {

	public AssetRendererFactory getActivePortletAssetRendererFactoryByClassName(
		long companyId, String className) {

		Map<String, AssetRendererFactory> filteredAssetRendererFactories =
			filterAssetRendererFactories(
				companyId, _assetRenderFactoriesMapByClassName);

		return filteredAssetRendererFactories.get(className);
	}

	public List<AssetRendererFactory> getActivePortletsAssetRendererFactories(
		long companyId) {

		return ListUtil.fromMapValues(
			filterAssetRendererFactories(
				companyId, _assetRenderFactoriesMapByClassName));
	}

	public long[] getActivePortletsClassNameIds(long companyId) {

		return _getClassNameIds(companyId);
	}

	public List<AssetRendererFactory> getAssetRendererFactories() {
		return ListUtil.fromMapValues(_assetRenderFactoriesMapByClassName);
	}

	public AssetRendererFactory getAssetRendererFactoryByClassName(
		String className) {

		return _assetRenderFactoriesMapByClassName.get(className);
	}

	public AssetRendererFactory getAssetRendererFactoryByType(String type) {
		return _assetRenderFactoriesMapByClassType.get(type);
	}

	public long[] getClassNameIds() {
		return _getClassNameIds(CompanyThreadLocal.getCompanyId());
	}

	public void register(AssetRendererFactory assetRendererFactory) {
		_assetRenderFactoriesMapByClassName.put(
			assetRendererFactory.getClassName(), assetRendererFactory);
		_assetRenderFactoriesMapByClassType.put(
			assetRendererFactory.getType(), assetRendererFactory);
	}

	public void unregister(AssetRendererFactory assetRendererFactory) {
		_assetRenderFactoriesMapByClassName.remove(
			assetRendererFactory.getClassName());
		_assetRenderFactoriesMapByClassType.remove(
			assetRendererFactory.getType());
	}

	private long[] _getClassNameIds(long companyId) {
		Map<String, AssetRendererFactory> assetRenderFactories = null;

		if (Validator.isNull(companyId)) {
			assetRenderFactories =_assetRenderFactoriesMapByClassName;
		}
		else {
			assetRenderFactories = filterAssetRendererFactories(
				companyId, _assetRenderFactoriesMapByClassName);
		}

		long[] classNameIds = new long[assetRenderFactories.size()];

		int i = 0;

		for (AssetRendererFactory assetRendererFactory :
				assetRenderFactories.values()) {

			classNameIds[i] = assetRendererFactory.getClassNameId();

			i++;
		}

		return classNameIds;
	}

	private Map<String, AssetRendererFactory> filterAssetRendererFactories(
		long companyId,
		Map<String, AssetRendererFactory> assetRendererFactories) {

		Map<String, AssetRendererFactory> filteredAssetRendererFactories =
			new ConcurrentHashMap<String, AssetRendererFactory>();

		for (String className : assetRendererFactories.keySet()) {
			AssetRendererFactory assetRendererFactory =
				assetRendererFactories.get(className);

			Portlet portlet = null;

			try {
				portlet = PortletLocalServiceUtil.getPortletById(
					companyId, assetRendererFactory.getPortletId());
			}
			catch (SystemException e) {
				portlet = PortletLocalServiceUtil.getPortletById(
					assetRendererFactory.getPortletId());
			}

			if (portlet.isActive()) {
				filteredAssetRendererFactories.put(
					className, assetRendererFactory);
			}
		}

		return filteredAssetRendererFactories;
	}

	private Map<String, AssetRendererFactory>
		_assetRenderFactoriesMapByClassName =
			new ConcurrentHashMap<String, AssetRendererFactory>();
	private Map<String, AssetRendererFactory>
		_assetRenderFactoriesMapByClassType =
			new ConcurrentHashMap<String, AssetRendererFactory>();

}
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

package com.liferay.portlet.assetpublisher.util;

import com.liferay.portal.kernel.search.BaseIndexer;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.BooleanQueryFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerPostProcessor;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.PropsValues;
import com.liferay.portlet.asset.model.AssetCategory;
import com.liferay.portlet.asset.service.AssetCategoryLocalServiceUtil;
import com.liferay.portlet.asset.service.persistence.AssetEntryQuery;
import com.liferay.portlet.asset.util.AssetUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletURL;

/**
 * @author Eudaldo Alonso
 */
public class AssetSearcher extends BaseIndexer {

	public static Indexer getInstance() {
		return new AssetSearcher();
	}

	public AssetSearcher() {
		setFilterSearch(true);
		setPermissionAware(true);
	}

	@Override
	public String[] getClassNames() {
		long[] classNameIds = _assetEntryQuery.getClassNameIds();

		String[] classNames = new String[classNameIds.length];

		for (int i = 0; i < classNames.length; i++) {
			long classNameId = classNameIds[i];

			classNames[i] = PortalUtil.getClassName(classNameId);
		}

		return classNames;
	}

	@Override
	public IndexerPostProcessor[] getIndexerPostProcessors() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPortletId() {
		return null;
	}

	@Override
	public void registerIndexerPostProcessor(
		IndexerPostProcessor indexerPostProcessor) {

		throw new UnsupportedOperationException();
	}

	public void setAssetEntryQuery(AssetEntryQuery assetEntryQuery) {
		_assetEntryQuery = assetEntryQuery;
	}

	protected void addSearchAllCategories(
			BooleanQuery contextQuery, SearchContext searchContext)
		throws Exception {

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		Map<Long, Boolean> allCategoryIds = AssetUtil.viewableCategoryIds(
			permissionChecker, _assetEntryQuery.getAllCategoryIds());

		if (allCategoryIds.size() == 0) {
			return;
		}

		List<Long> viewableCategoryIds = new ArrayList<Long>();
		List<Long> notViewableCategoryIds = new ArrayList<Long>();

		for (long allCategoryId : allCategoryIds.keySet()) {
			Boolean categoryViewable = allCategoryIds.get(allCategoryId);

			if (categoryViewable != null) {
				if (categoryViewable.booleanValue()) {
					viewableCategoryIds.add(allCategoryId);
				}
				else {
					notViewableCategoryIds.add(allCategoryId);
				}
			}
		}

		buildAllCategoriesQuery(
			contextQuery, searchContext, viewableCategoryIds, true);

		buildAllCategoriesQuery(
			contextQuery, searchContext, notViewableCategoryIds, false);
	}

	protected void addSearchAllTags(
			BooleanQuery contextQuery, SearchContext searchContext)
		throws Exception {

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		Map<Long, Boolean> allTagIds = AssetUtil.viewableTagIds(
			permissionChecker, _assetEntryQuery.getAllTagIds());

		if (allTagIds.size() == 0) {
			return;
		}

		List<Long> viewableTagIds = new ArrayList<Long>();
		List<Long> notViewableTagIds = new ArrayList<Long>();

		for (long allTagId : allTagIds.keySet()) {
			Boolean tagViewable = allTagIds.get(allTagId);

			if (tagViewable != null) {
				if (tagViewable.booleanValue()) {
					viewableTagIds.add(allTagId);
				}
				else {
					notViewableTagIds.add(allTagId);
				}
			}
		}

		buildAllTagsQuery(contextQuery, searchContext, viewableTagIds, true);

		buildAllTagsQuery(
			contextQuery, searchContext, notViewableTagIds, false);
	}

	protected void addSearchAnyCategories(
			BooleanQuery contextQuery, SearchContext searchContext)
		throws Exception {

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		Map<Long, Boolean> anyCategoryIds = AssetUtil.viewableCategoryIds(
			permissionChecker, _assetEntryQuery.getAnyCategoryIds());

		if (anyCategoryIds.size() == 0) {
			return;
		}

		List<Long> viewableCategoryIds = new ArrayList<Long>();
		List<Long> notViewableCategoryIds = new ArrayList<Long>();

		for (long anyCategoryId : anyCategoryIds.keySet()) {
			Boolean categoryViewable = anyCategoryIds.get(anyCategoryId);

			if (categoryViewable != null) {
				if (categoryViewable.booleanValue()) {
					viewableCategoryIds.add(anyCategoryId);
				}
				else {
					notViewableCategoryIds.add(anyCategoryId);
				}
			}
		}

		buildAnyCategoriesQuery(
			contextQuery, searchContext, viewableCategoryIds, true);

		if (viewableCategoryIds.isEmpty()) {
			buildAllCategoriesQuery(
				contextQuery, searchContext, notViewableCategoryIds, false);
		}
		else {
			buildAnyCategoriesQuery(
				contextQuery, searchContext, notViewableCategoryIds, false);
		}
	}

	protected void addSearchAnyTags(
			BooleanQuery contextQuery, SearchContext searchContext)
		throws Exception {

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		Map<Long, Boolean> anyTagIds = AssetUtil.viewableTagIds(
			permissionChecker, _assetEntryQuery.getAnyTagIds());

		if (anyTagIds.size() == 0) {
			return;
		}

		List<Long> viewableTagIds = new ArrayList<Long>();
		List<Long> notViewableTagIds = new ArrayList<Long>();

		for (long anyTagId : anyTagIds.keySet()) {
			Boolean tagViewable = anyTagIds.get(anyTagId);

			if (tagViewable != null) {
				if (tagViewable.booleanValue()) {
					viewableTagIds.add(anyTagId);
				}
				else {
					notViewableTagIds.add(anyTagId);
				}
			}
		}

		buildAnyTagsQuery(contextQuery, searchContext, viewableTagIds, true);

		if (viewableTagIds.isEmpty()) {
			buildAllTagsQuery(
				contextQuery, searchContext, notViewableTagIds, false);
		}
		else {
			buildAnyTagsQuery(
				contextQuery, searchContext, notViewableTagIds, false);
		}
	}

	@Override
	protected void addSearchAssetCategoryIds(
			BooleanQuery contextQuery, SearchContext searchContext)
		throws Exception {

		addSearchAllCategories(contextQuery, searchContext);
		addSearchAnyCategories(contextQuery, searchContext);
		addSearchNotAnyCategories(contextQuery, searchContext);
		addSearchNotAllCategories(contextQuery, searchContext);
	}

	@Override
	protected void addSearchAssetTagNames(
			BooleanQuery contextQuery, SearchContext searchContext)
		throws Exception {

		addSearchAllTags(contextQuery, searchContext);
		addSearchAnyTags(contextQuery, searchContext);
		addSearchNotAllTags(contextQuery, searchContext);
		addSearchNotAnyTags(contextQuery, searchContext);
	}

	@Override
	protected void addSearchKeywords(
			BooleanQuery searchQuery, SearchContext searchContext)
		throws Exception {

		String keywords = searchContext.getKeywords();

		if (Validator.isNull(keywords)) {
			return;
		}

		super.addSearchKeywords(searchQuery, searchContext);

		String field = DocumentImpl.getLocalizedName(
			searchContext.getLocale(), "localized_title");

		searchQuery.addTerm(field, keywords, true);
	}

	@Override
	protected void addSearchLayout(
			BooleanQuery contextQuery, SearchContext searchContext)
		throws Exception {

		String layoutUuid = (String)searchContext.getAttribute(
			Field.LAYOUT_UUID);

		if (Validator.isNotNull(layoutUuid)) {
			contextQuery.addRequiredTerm(Field.LAYOUT_UUID, layoutUuid);
		}
	}

	protected void addSearchNotAllCategories(
			BooleanQuery contextQuery, SearchContext searchContext)
		throws Exception {

		long[] notAllCategoryIds = _assetEntryQuery.getNotAllCategoryIds();

		if (notAllCategoryIds.length == 0) {
			return;
		}

		BooleanQuery categoryIdsQuery = BooleanQueryFactoryUtil.create(
			searchContext);

		for (long notAllCategoryId : notAllCategoryIds) {
			AssetCategory assetCategory =
				AssetCategoryLocalServiceUtil.fetchAssetCategory(
					notAllCategoryId);

			if (assetCategory == null) {
				continue;
			}

			List<Long> categoryIds = new ArrayList<Long>();

			if (PropsValues.ASSET_CATEGORIES_SEARCH_HIERARCHICAL) {
				categoryIds = AssetCategoryLocalServiceUtil.getSubcategoryIds(
					notAllCategoryId);
			}

			if (categoryIds.isEmpty()) {
				categoryIds.add(notAllCategoryId);
			}

			BooleanQuery categoryIdQuery = BooleanQueryFactoryUtil.create(
				searchContext);

			for (long categoryId : categoryIds) {
				categoryIdQuery.addTerm(Field.ASSET_CATEGORY_IDS, categoryId);
			}

			categoryIdsQuery.add(categoryIdQuery, BooleanClauseOccur.MUST);
		}

		contextQuery.add(categoryIdsQuery, BooleanClauseOccur.MUST_NOT);
	}

	protected void addSearchNotAllTags(
			BooleanQuery contextQuery, SearchContext searchContext)
		throws Exception {

		long[] notAllTagIds = _assetEntryQuery.getNotAllTagIds();

		if (notAllTagIds.length == 0) {
			return;
		}

		BooleanQuery tagIdsQuery = BooleanQueryFactoryUtil.create(
			searchContext);

		for (long tagId : notAllTagIds) {
			tagIdsQuery.addRequiredTerm(Field.ASSET_TAG_IDS, tagId);
		}

		contextQuery.add(tagIdsQuery, BooleanClauseOccur.MUST_NOT);
	}

	protected void addSearchNotAnyCategories(
			BooleanQuery contextQuery, SearchContext searchContext)
		throws Exception {

		long[] notAnyCategoryIds = _assetEntryQuery.getNotAnyCategoryIds();

		if (notAnyCategoryIds.length == 0) {
			return;
		}

		BooleanQuery categoryIdsQuery = BooleanQueryFactoryUtil.create(
			searchContext);

		for (long notAnyCategoryId : notAnyCategoryIds) {
			AssetCategory assetCategory =
				AssetCategoryLocalServiceUtil.fetchAssetCategory(
					notAnyCategoryId);

			if (assetCategory == null) {
				continue;
			}

			List<Long> categoryIds = new ArrayList<Long>();

			if (PropsValues.ASSET_CATEGORIES_SEARCH_HIERARCHICAL) {
				categoryIds = AssetCategoryLocalServiceUtil.getSubcategoryIds(
					notAnyCategoryId);
			}

			if (categoryIds.isEmpty()) {
				categoryIds.add(notAnyCategoryId);
			}

			for (long categoryId : categoryIds) {
				categoryIdsQuery.addTerm(Field.ASSET_CATEGORY_IDS, categoryId);
			}
		}

		contextQuery.add(categoryIdsQuery, BooleanClauseOccur.MUST_NOT);
	}

	protected void addSearchNotAnyTags(
			BooleanQuery contextQuery, SearchContext searchContext)
		throws Exception {

		long[] notAnyTagIds = _assetEntryQuery.getNotAnyTagIds();

		if (notAnyTagIds.length == 0) {
			return;
		}

		BooleanQuery tagIdsQuery = BooleanQueryFactoryUtil.create(
			searchContext);

		for (long tagId : _assetEntryQuery.getNotAnyTagIds()) {
			tagIdsQuery.addTerm(Field.ASSET_TAG_IDS, tagId);
		}

		contextQuery.add(tagIdsQuery, BooleanClauseOccur.MUST_NOT);
	}

	protected void buildAllCategoriesQuery(
			BooleanQuery contextQuery, SearchContext searchContext,
			List<Long> allCategoryIds, boolean viewable)
		throws Exception {

		BooleanQuery categoryIdsQuery = BooleanQueryFactoryUtil.create(
			searchContext);

		for (long allCategoryId : allCategoryIds) {
			AssetCategory assetCategory =
				AssetCategoryLocalServiceUtil.fetchAssetCategory(allCategoryId);

			if (assetCategory == null) {
				continue;
			}

			List<Long> categoryIds = new ArrayList<Long>();

			if (PropsValues.ASSET_CATEGORIES_SEARCH_HIERARCHICAL && viewable) {
				categoryIds = AssetCategoryLocalServiceUtil.getSubcategoryIds(
					allCategoryId);
			}

			if (categoryIds.isEmpty()) {
				categoryIds.add(allCategoryId);
			}

			BooleanQuery categoryIdQuery = BooleanQueryFactoryUtil.create(
				searchContext);

			for (long categoryId : categoryIds) {
				categoryIdQuery.addTerm(Field.ASSET_CATEGORY_IDS, categoryId);
			}

			BooleanClauseOccur booleanClauseOccur = BooleanClauseOccur.MUST;

			if (!viewable) {
				booleanClauseOccur = BooleanClauseOccur.MUST_NOT;
			}

			categoryIdsQuery.add(categoryIdQuery, booleanClauseOccur);
		}

		if (allCategoryIds.size() > 0) {
			contextQuery.add(categoryIdsQuery, BooleanClauseOccur.MUST);
		}
	}

	protected void buildAllTagsQuery(
			BooleanQuery contextQuery, SearchContext searchContext,
			List<Long> allTagIds, boolean viewable)
		throws Exception {

		BooleanQuery tagIdsQuery = BooleanQueryFactoryUtil.create(
			searchContext);

		for (long tagId : allTagIds) {
			BooleanQuery tagIdQuery = BooleanQueryFactoryUtil.create(
				searchContext);

			tagIdQuery.addTerm(Field.ASSET_TAG_IDS, tagId);

			BooleanClauseOccur booleanClauseOccur = BooleanClauseOccur.MUST;

			if (!viewable) {
				booleanClauseOccur = BooleanClauseOccur.MUST_NOT;
			}

			tagIdsQuery.add(tagIdQuery, booleanClauseOccur);
		}

		if (allTagIds.size() > 0) {
			contextQuery.add(tagIdsQuery, BooleanClauseOccur.MUST);
		}
	}

	protected void buildAnyCategoriesQuery(
			BooleanQuery contextQuery, SearchContext searchContext,
			List<Long> anyCategoryIds, boolean viewable)
		throws Exception {

		BooleanQuery categoryIdsQuery = BooleanQueryFactoryUtil.create(
			searchContext);

		for (long anyCategoryId : anyCategoryIds) {
			AssetCategory assetCategory =
				AssetCategoryLocalServiceUtil.fetchAssetCategory(anyCategoryId);

			if (assetCategory == null) {
				continue;
			}

			List<Long> categoryIds = new ArrayList<Long>();

			if (PropsValues.ASSET_CATEGORIES_SEARCH_HIERARCHICAL && viewable) {
				categoryIds = AssetCategoryLocalServiceUtil.getSubcategoryIds(
					anyCategoryId);
			}

			if (categoryIds.isEmpty()) {
				categoryIds.add(anyCategoryId);
			}

			for (long categoryId : categoryIds) {
				categoryIdsQuery.addTerm(Field.ASSET_CATEGORY_IDS, categoryId);
			}
		}

		BooleanClauseOccur booleanClauseOccur = BooleanClauseOccur.MUST;

		if (!viewable) {
			booleanClauseOccur = BooleanClauseOccur.MUST_NOT;
		}

		if (anyCategoryIds.size() > 0 ) {
			contextQuery.add(categoryIdsQuery, booleanClauseOccur);
		}
	}

	protected void buildAnyTagsQuery(
			BooleanQuery contextQuery, SearchContext searchContext,
			List<Long> anyTagIds, boolean viewable)
		throws Exception {

		BooleanQuery tagIdsQuery = BooleanQueryFactoryUtil.create(
			searchContext);

		for (long tagId : anyTagIds) {
			tagIdsQuery.addTerm(Field.ASSET_TAG_IDS, tagId);
		}

		BooleanClauseOccur booleanClauseOccur = BooleanClauseOccur.MUST;

		if (!viewable) {
			booleanClauseOccur = BooleanClauseOccur.MUST_NOT;
		}

		if (anyTagIds.size() > 0 ) {
			contextQuery.add(tagIdsQuery, booleanClauseOccur);
		}
	}

	@Override
	protected void doDelete(Object obj) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Document doGetDocument(Object obj) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Summary doGetSummary(
			Document document, Locale locale, String snippet,
			PortletURL portletURL)
		throws Exception {

		throw new UnsupportedOperationException();
	}

	@Override
	protected void doReindex(Object obj) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void doReindex(String className, long classPK) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void doReindex(String[] ids) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	protected String getPortletId(SearchContext searchContext) {
		return null;
	}

	private AssetEntryQuery _assetEntryQuery;

}
/**
 * Copyright (c) 2000-2010 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portlet.documentlibrary.util;

import com.liferay.documentlibrary.model.FileModel;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchEngineUtil;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Group;
import com.liferay.portal.search.BaseIndexer;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portlet.asset.service.AssetCategoryLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetTagLocalServiceUtil;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.model.DLFolder;
import com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFolderLocalServiceUtil;

import java.util.Date;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.WindowStateException;

/**
 * <a href="DLIndexer.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 * @author Raymond Augé
 */
public class DLIndexer extends BaseIndexer {

	public static final String[] CLASS_NAMES = {DLFileEntry.class.getName()};

	public static final String PORTLET_ID = PortletKeys.DOCUMENT_LIBRARY;

	public DLIndexer() {
		IndexerRegistryUtil.register(
			new com.liferay.documentlibrary.util.DLIndexer());
	}

	public String[] getClassNames() {
		return CLASS_NAMES;
	}

	public Summary getSummary(
		Document document, String snippet, PortletURL portletURL) {

		LiferayPortletURL liferayPortletURL = (LiferayPortletURL)portletURL;

		liferayPortletURL.setLifecycle(PortletRequest.ACTION_PHASE);

		try {
			liferayPortletURL.setWindowState(LiferayWindowState.EXCLUSIVE);
		}
		catch (WindowStateException wse) {
		}

		String repositoryId = document.get("repositoryId");
		String fileName = document.get("path");

		String title = fileName;

		String content = snippet;

		if (Validator.isNull(snippet)) {
			content = StringUtil.shorten(document.get(Field.CONTENT), 200);
		}

		portletURL.setParameter("struts_action", "/document_library/get_file");
		portletURL.setParameter("folderId", repositoryId);
		portletURL.setParameter("name", fileName);

		return new Summary(title, content, portletURL);
	}

	protected void doDelete(Object obj) throws Exception {
		DLFileEntry fileEntry = (DLFileEntry)obj;

		FileModel fileModel = new FileModel();

		fileModel.setCompanyId(fileEntry.getCompanyId());
		fileModel.setFileName(fileEntry.getName());
		fileModel.setPortletId(PORTLET_ID);
		fileModel.setRepositoryId(fileEntry.getRepositoryId());

		Indexer indexer = IndexerRegistryUtil.getIndexer(FileModel.class);

		indexer.delete(fileModel);
	}

	protected void doReindex(String[] ids) throws Exception {
		long companyId = GetterUtil.getLong(ids[0]);

		reindexFolders(companyId);
		reindexRoot(companyId);
	}

	protected void doReindex(String className, long classPK) throws Exception {
		DLFileEntry fileEntry = DLFileEntryLocalServiceUtil.getFileEntry(
			classPK);

		doReindex(fileEntry);
	}

	protected void doReindex(Object obj) throws Exception {
		DLFileEntry fileEntry = (DLFileEntry)obj;

		Document document = getDocument(fileEntry);

		if (document != null) {
			SearchEngineUtil.updateDocument(
				fileEntry.getCompanyId(), document.get(Field.UID), document);
		}
	}

	protected Document doGetDocument(Object obj) throws Exception {
		DLFileEntry fileEntry = (DLFileEntry)obj;

		DLFolder folder = fileEntry.getFolder();

		long companyId = fileEntry.getCompanyId();
		long groupId = folder.getGroupId();
		long repositoryId = fileEntry.getRepositoryId();
		String fileName = fileEntry.getName();
		long fileEntryId = fileEntry.getFileEntryId();
		String properties = fileEntry.getLuceneProperties();
		Date modifiedDate = fileEntry.getModifiedDate();

		long[] assetCategoryIds = AssetCategoryLocalServiceUtil.getCategoryIds(
			DLFileEntry.class.getName(), fileEntryId);
		String[] assetTagNames = AssetTagLocalServiceUtil.getTagNames(
			DLFileEntry.class.getName(), fileEntryId);

		FileModel fileModel = new FileModel();

		fileModel.setAssetCategoryIds(assetCategoryIds);
		fileModel.setAssetTagNames(assetTagNames);
		fileModel.setCompanyId(companyId);
		fileModel.setFileEntryId(fileEntryId);
		fileModel.setFileName(fileName);
		fileModel.setGroupId(groupId);
		fileModel.setModifiedDate(modifiedDate);
		fileModel.setPortletId(PORTLET_ID);
		fileModel.setProperties(properties);
		fileModel.setRepositoryId(repositoryId);

		Indexer indexer = IndexerRegistryUtil.getIndexer(FileModel.class);

		return indexer.getDocument(fileModel);
	}

	protected String getPortletId(SearchContext searchContext) {
		return PORTLET_ID;
	}

	protected void reindexFolders(long companyId) throws Exception {
		int folderCount = DLFolderLocalServiceUtil.getCompanyFoldersCount(
			companyId);

		int folderPages = folderCount / Indexer.DEFAULT_INTERVAL;

		for (int i = 0; i <= folderPages; i++) {
			int folderStart = (i * Indexer.DEFAULT_INTERVAL);
			int folderEnd = folderStart + Indexer.DEFAULT_INTERVAL;

			reindexFolders(companyId, folderStart, folderEnd);
		}
	}

	protected void reindexFolders(
			long companyId, int folderStart, int folderEnd)
		throws Exception {

		List<DLFolder> folders = DLFolderLocalServiceUtil.getCompanyFolders(
			companyId, folderStart, folderEnd);

		for (DLFolder folder : folders) {
			String portletId = PortletKeys.DOCUMENT_LIBRARY;
			long groupId = folder.getGroupId();
			long folderId = folder.getFolderId();

			String[] newIds = {
				String.valueOf(companyId), portletId,
				String.valueOf(groupId), String.valueOf(folderId)
			};

			Indexer indexer = IndexerRegistryUtil.getIndexer(FileModel.class);

			indexer.reindex(newIds);
		}
	}

	protected void reindexRoot(long companyId) throws Exception {
		int groupCount = GroupLocalServiceUtil.getCompanyGroupsCount(companyId);

		int groupPages = groupCount / Indexer.DEFAULT_INTERVAL;

		for (int i = 0; i <= groupPages; i++) {
			int groupStart = (i * Indexer.DEFAULT_INTERVAL);
			int groupEnd = groupStart + Indexer.DEFAULT_INTERVAL;

			reindexRoot(companyId, groupStart, groupEnd);
		}
	}

	protected void reindexRoot(long companyId, int groupStart, int groupEnd)
		throws Exception {

		List<Group> groups = GroupLocalServiceUtil.getCompanyGroups(
			companyId, groupStart, groupEnd);

		for (Group group : groups) {
			String portletId = PortletKeys.DOCUMENT_LIBRARY;
			long groupId = group.getGroupId();
			long folderId = groupId;

			String[] newIds = {
				String.valueOf(companyId), portletId,
				String.valueOf(groupId), String.valueOf(folderId)
			};

			Indexer indexer = IndexerRegistryUtil.getIndexer(FileModel.class);

			indexer.reindex(newIds);
		}
	}

}
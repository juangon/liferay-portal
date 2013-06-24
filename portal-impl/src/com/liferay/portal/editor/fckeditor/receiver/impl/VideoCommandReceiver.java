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

package com.liferay.portal.editor.fckeditor.receiver.impl;

import com.liferay.portal.editor.fckeditor.command.CommandArgument;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Group;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.documentlibrary.service.DLAppServiceUtil;
import com.liferay.portlet.documentlibrary.util.DLUtil;
import com.liferay.portlet.documentlibrary.util.VideoProcessorUtil;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Juan Gonzalez
 *
 */
public class VideoCommandReceiver extends DocumentCommandReceiver {

	@Override
	protected void getFiles(
			CommandArgument commandArgument, Document document, Node rootNode)
		throws Exception {

		Set<String> videoMimesSet = VideoProcessorUtil.getVideoMimeTypes();
		String[] videoMimeTypes = null;

		if (Validator.isNotNull(videoMimesSet)) {
			videoMimeTypes = ArrayUtil.toStringArray(videoMimesSet.toArray());
		}

		Element filesElement = document.createElement("Files");

		rootNode.appendChild(filesElement);

		if (Validator.isNull(commandArgument.getCurrentGroupName())) {
			return;
		}

		Group group = commandArgument.getCurrentGroup();

		Folder folder = getFolder(
			group.getGroupId(), commandArgument.getCurrentFolder());

		List<FileEntry> fileEntries = null;

		if (Validator.isNotNull(videoMimeTypes)) {
			fileEntries = DLAppServiceUtil.getFileEntries(
				folder.getRepositoryId(), folder.getFolderId(), videoMimeTypes);
		}
		else {
			fileEntries = DLAppServiceUtil.getFileEntries(
				folder.getRepositoryId(), folder.getFolderId());
		}

		for (FileEntry fileEntry : fileEntries) {
			if (VideoProcessorUtil.hasVideo(fileEntry.getFileVersion())) {
				Element fileElement = document.createElement("File");

				filesElement.appendChild(fileElement);

				fileElement.setAttribute("name", fileEntry.getTitle());
				fileElement.setAttribute("desc", fileEntry.getTitle());
				fileElement.setAttribute("size", getSize(fileEntry.getSize()));

				ThemeDisplay themeDisplay = commandArgument.getThemeDisplay();

				String url = DLUtil.getPreviewURL(
					fileEntry, fileEntry.getFileVersion(), themeDisplay,
					StringPool.BLANK, false, false);

				fileElement.setAttribute("url", url);
			}
		}
	}

}
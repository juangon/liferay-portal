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

package com.liferay.portal.editor.fckeditor;

import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Group;
import com.liferay.portal.service.GroupLocalServiceUtil;

/**
 *
 * @author Juan Gonzalez
 *
 */
public class FCKEditorUtil {

	public static String getRootFolderName(
			Group group, long scopeGroupId, String portletId)
		throws Exception {

		return getRootFolderName(group, scopeGroupId, portletId, null);
	}

	public static String getRootFolderName(
			Group group, long scopeGroupId, String portletId,
			Boolean stagedData)
		throws Exception {

		if (group.isLayoutPrototype() || group.isLayoutSetPrototype()) {
			return null;
		}

		if (group.isLayout()) {
			long parentGroupId = group.getParentGroupId();

			if (parentGroupId >0) {
				group = GroupLocalServiceUtil.getGroup(parentGroupId);
			}
		}

		boolean setNameAttribute = false;

		boolean stagedDataPortlet = group.isStagedPortlet(portletId);

		if (Validator.isNotNull(stagedData)) {
			stagedDataPortlet = stagedData.booleanValue();
		}

		String name = null;

		if (group.hasStagingGroup()) {
			Group stagingGroup = group.getStagingGroup();

			if ((stagingGroup.getGroupId() == scopeGroupId) &&
				group.isStagedPortlet(portletId) && !group.isStagedRemotely() &&
				stagedDataPortlet) {

				name =
					stagingGroup.getGroupId() + " - " +
						HtmlUtil.escape(stagingGroup.getDescriptiveName());

				setNameAttribute = true;
			}
		}

		if (!setNameAttribute) {
			name =
				group.getGroupId() + " - " +
					HtmlUtil.escape(group.getDescriptiveName());
		}

		return name;

	}

}
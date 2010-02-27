/**
 * Copyright (c) 2000-2010 Liferay, Inc. All rights reserved.
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

package com.liferay.taglib.portlet;

import com.liferay.portal.kernel.util.Validator;

import javax.portlet.PortletURL;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * <a href="ActionURLTei.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 */
public class ActionURLTei extends TagExtraInfo {

	public VariableInfo[] getVariableInfo(TagData data) {
		String var = data.getAttributeString("var");
		String varImpl = data.getAttributeString("varImpl");

		if (Validator.isNotNull(var)) {
			return new VariableInfo[] {
				new VariableInfo(
					var, String.class.getName(), true, VariableInfo.AT_END),
			};
		}
		else if (Validator.isNotNull(varImpl)) {
			return new VariableInfo[] {
				new VariableInfo(
					varImpl, PortletURL.class.getName(), true,
					VariableInfo.AT_END),
			};
		}
		else {
			return null;
		}
	}

}
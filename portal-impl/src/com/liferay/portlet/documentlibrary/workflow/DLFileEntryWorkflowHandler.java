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

package com.liferay.portlet.documentlibrary.workflow;

import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.workflow.BaseWorkflowHandler;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil;

/**
 * <a href="DLFileEntryWorkflowHandler.java.html"><b><i>View Source</i></b></a>
 *
 * @author Bruno Farache
 */
public class DLFileEntryWorkflowHandler extends BaseWorkflowHandler {

	public static final String CLASS_NAME = DLFileEntry.class.getName();

	public String getClassName() {
		return CLASS_NAME;
	}

	public String getType() {
		return TYPE_DOCUMENT;
	}

	public DLFileEntry updateStatus(
			long companyId, long groupId, long userId, long classPK, int status)
		throws Exception {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setStatus(status);

		return DLFileEntryLocalServiceUtil.updateWorkflowStatus(
			userId, classPK, serviceContext);
	}

}
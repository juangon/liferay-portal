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

package com.liferay.taglib.ui;

import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.taglib.util.ParamAndPropertyAncestorTagImpl;

import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

/**
 * <a href="SectionTag.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 */
public class SectionTag extends ParamAndPropertyAncestorTagImpl {

	public int doStartTag() throws JspException {
		_tabsTag = (TabsTag)findAncestorWithClass(this, TabsTag.class);

		if (_tabsTag == null) {
			throw new JspException();
		}

		try {
			HttpServletRequest request =
				(HttpServletRequest)pageContext.getRequest();

			RenderResponse renderResponse =
				(RenderResponse)request.getAttribute(
					JavaConstants.JAVAX_PORTLET_RESPONSE);

			String namespace = StringPool.BLANK;

			if (renderResponse != null) {
				namespace = renderResponse.getNamespace();
			}

			String sectionParam = _tabsTag.getParam();
			String sectionName = _tabsTag.getSectionName();
			_sectionSelected = Boolean.valueOf(_tabsTag.getSectionSelected());
			String sectionScroll = namespace + sectionParam + "TabsScroll";
			String sectionRedirectParams =
				"&scroll=" + sectionScroll + "&" + sectionParam + "=" +
					sectionName;

			_tabsTag.incrementSection();

			request.setAttribute("liferay-ui:section:param", sectionParam);
			request.setAttribute("liferay-ui:section:name", sectionName);
			request.setAttribute(
				"liferay-ui:section:selected", _sectionSelected);
			request.setAttribute("liferay-ui:section:scroll", sectionScroll);

			pageContext.setAttribute("sectionSelected", _sectionSelected);
			pageContext.setAttribute("sectionParam", sectionParam);
			pageContext.setAttribute("sectionName", sectionName);
			pageContext.setAttribute("sectionScroll", sectionScroll);
			pageContext.setAttribute(
				"sectionRedirectParams", sectionRedirectParams);

			if (!_tabsTag.isRefresh() || _sectionSelected.booleanValue()) {
				if (!_tabsTag.isRefresh()) {
					include(getStartPage());
				}

				return EVAL_BODY_INCLUDE;
			}
			else {
				return EVAL_PAGE;
			}
		}
		catch (Exception e) {
			throw new JspException(e);
		}
	}

	public int doEndTag() throws JspException {
		try {
			if (!_tabsTag.isRefresh() || _sectionSelected.booleanValue()) {
				if (!_tabsTag.isRefresh()) {
					include(getEndPage());
				}

				return EVAL_BODY_INCLUDE;
			}
			else {
				return EVAL_PAGE;
			}
		}
		catch (Exception e) {
			throw new JspException(e);
		}
	}

	public String getStartPage() {
		if (Validator.isNull(_startPage)) {
			return _START_PAGE;
		}
		else {
			return _startPage;
		}
	}

	public void setStartPage(String startPage) {
		_startPage = startPage;
	}

	public String getEndPage() {
		if (Validator.isNull(_endPage)) {
			return _END_PAGE;
		}
		else {
			return _endPage;
		}
	}

	public void setEndPage(String endPage) {
		_endPage = endPage;
	}

	private static final String _START_PAGE =
		"/html/taglib/ui/section/start.jsp";

	private static final String _END_PAGE = "/html/taglib/ui/section/end.jsp";

	private String _startPage;
	private String _endPage;
	private TabsTag _tabsTag = null;
	private Boolean _sectionSelected = Boolean.FALSE;

}
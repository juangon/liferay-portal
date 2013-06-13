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

package com.liferay.portal.kernel.search;

import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.util.PortletKeys;

import java.util.Arrays;
import java.util.Locale;

/**
 * @author Michael C. Han
 */
public class QueryIndexingHitsProcessor implements HitsProcessor {

	public static final float SCORES_THRESHOLD_DEFAULT = 0.5f;

	@Override
	public boolean process(SearchContext searchContext, Hits hits)
		throws SearchException {

		float[] scores = hits.getScores();

		if ((scores != null) && (scores.length != 0)) {
			Arrays.sort(scores);

			if (scores[0] >= _scoresThreshold) {
				addDocument(
					searchContext.getCompanyId(), searchContext.getKeywords(),
					searchContext.getLocale());
			}
		}

		return true;
	}

	public void setDocument(Document document) {
		_document = document;
	}

	public void setScoresThreshold(float scoresThreshold) {
		_scoresThreshold = scoresThreshold;
	}

	protected void addDocument(long companyId, String keywords, Locale locale)
		throws SearchException {

		Document document = (Document)_document.clone();

		document.addKeyword(Field.COMPANY_ID, companyId);
		document.addKeyword(Field.KEYWORD_SEARCH, keywords);
		document.addKeyword(Field.LANGUAGE_ID, LocaleUtil.toLanguageId(locale));
		document.addKeyword(Field.PORTLET_ID, PortletKeys.SEARCH);

		SearchEngineUtil.addDocument(
			SearchEngineUtil.getDefaultSearchEngineId(), companyId, document);
	}

	private Document _document;
	private float _scoresThreshold = SCORES_THRESHOLD_DEFAULT;

}
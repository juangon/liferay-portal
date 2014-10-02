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

package com.liferay.portal.xml;

import com.liferay.portal.kernel.util.OrderedProperties;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;

import java.io.IOException;

import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Juan Gonzalez
 */
public class SAXReaderTest {

	@Before
	public void setUp() throws Exception {
		SAXReaderUtil saxReaderUtil = new SAXReaderUtil();

		saxReaderUtil.setSAXReader(new SAXReaderImpl());
	}

	@Test
	public void testMergeXMLFromProperties()
		throws DocumentException, IOException {

		Properties mergeProperties = getMergeProperties();

		Document destinationDocument = SAXReaderUtil.read(mergeProperties);

		Document originalDocument = getDocument();

		Document expectedDocument = getMergedDocument();

		Element mergedElement =
						SAXReaderUtil.mergeElement(
							originalDocument.getRootElement(),
							destinationDocument.getRootElement());

		Element expectedRootElement = expectedDocument.getRootElement();

		checkElements(mergedElement, expectedRootElement);
	}

	@Test
	public void testReadXMLFromProperties()
		throws DocumentException, IOException {

		Properties properties = getProperties();

		Document createdDocument = SAXReaderUtil.read(properties);

		Document expectedDocument = getDocument();

		Element createdRootElement = createdDocument.getRootElement();
		Element expectedRootElement = expectedDocument.getRootElement();

		checkElements(createdRootElement, expectedRootElement);
	}

	protected void checkElements(
			Element createdElement, Element expectedElement)
		throws DocumentException {

		List<Element> createdElements = createdElement.elements();
		List<Element> expectedElements = expectedElement.elements();

		Assert.assertEquals(expectedElements.size(), createdElements.size());

		int i = 0;

		for (Element currentExpectedElement : expectedElements) {
			Element currentCreatedElement = createdElements.get(i);

			Assert.assertEquals(
				currentExpectedElement.getName(),
				currentCreatedElement.getName());

			int expectedChildsCount = currentExpectedElement.elements().size();
			int createdChildsCount = currentCreatedElement.elements().size();
			Assert.assertEquals(expectedChildsCount, createdChildsCount);

			if (expectedChildsCount >0 && createdChildsCount >0) {
				checkElements(currentCreatedElement, currentExpectedElement);
			}
			else {
				Assert.assertEquals(
					currentCreatedElement.getText(),
					currentExpectedElement.getText());
			}

			i++;
		}
	}

	protected Document getDocument() {
		Document document = SAXReaderUtil.createDocument();
		Element element = document.addElement("root");

		element = element.addElement("liferay");
		Element portletElement = element.addElement("portlet");

		element = portletElement.addElement("struts-path");
		element.setText("MyStrutsPath");

		Element arrayElement = portletElement.addElement("scheduler-entry");
		element = arrayElement.addElement("scheduler-event-listener-class");
		element.setText("com.liferay.portlet.admin.EventListener1");
		element = arrayElement.addElement("trigger");
		Element simpleElement = element.addElement("simple");
		element = simpleElement.addElement("simple-trigger-value");
		element.setText("1");
		element = simpleElement.addElement("time-unit");
		element.setText("day");

		arrayElement = portletElement.addElement("scheduler-entry");
		element = arrayElement.addElement("scheduler-event-listener-class");
		element.setText("com.liferay.portlet.admin.EventListener2");
		element = arrayElement.addElement("trigger");
		simpleElement = element.addElement("simple");
		element = simpleElement.addElement("simple-trigger-value");
		element.setText("2");
		element = simpleElement.addElement("time-unit");
		element.setText("year");

		element = portletElement.addElement("portlet-url-class");
		element.setText("com.liferay.portal.struts.StrutsActionPortletURL");

		return document;
	}

	protected Document getMergedDocument() {
		Document document = SAXReaderUtil.createDocument();
		Element element = document.addElement("root");

		element = element.addElement("liferay");
		Element portletElement = element.addElement("portlet");

		element = portletElement.addElement("struts-path");
		element.setText("MyMergedStrutsPath");

		Element arrayElement = portletElement.addElement("scheduler-entry");
		element = arrayElement.addElement("scheduler-event-listener-class");
		element.setText("com.liferay.portlet.admin.MergedEventListener");
		element = arrayElement.addElement("trigger");
		Element simpleElement = element.addElement("simple");
		element = simpleElement.addElement("simple-trigger-value");
		element.setText("1");
		element = simpleElement.addElement("time-unit");
		element.setText("day");

		return document;
	}

	protected Properties getMergeProperties() throws IOException {
		Properties props = new OrderedProperties();
		props.load(this.getClass().getResourceAsStream("xmlMerge.properties"));

		return props;
	}

	protected Properties getProperties() throws IOException {
		Properties props = new OrderedProperties();
		props.load(this.getClass().getResourceAsStream("xml.properties"));

		return props;
	}
}
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

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.pacl.DoPrivileged;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.PropertiesUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.Attribute;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.Entity;
import com.liferay.portal.kernel.xml.Namespace;
import com.liferay.portal.kernel.xml.Node;
import com.liferay.portal.kernel.xml.ProcessingInstruction;
import com.liferay.portal.kernel.xml.QName;
import com.liferay.portal.kernel.xml.SAXReader;
import com.liferay.portal.kernel.xml.Text;
import com.liferay.portal.kernel.xml.XMLSchema;
import com.liferay.portal.kernel.xml.XPath;
import com.liferay.portal.util.ClassLoaderUtil;
import com.liferay.portal.util.EntityResolver;
import com.liferay.portal.util.PropsValues;
import com.liferay.util.xml.XMLSafeReader;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.xerces.parsers.SAXParser;

import org.dom4j.DocumentFactory;

/**
 * @author Brian Wing Shun Chan
 */
@DoPrivileged
public class SAXReaderImpl implements SAXReader {

	public static SAXReaderImpl getInstance() {
		return _instance;
	}

	public static List<Attribute> toNewAttributes(
		List<org.dom4j.Attribute> oldAttributes) {

		List<Attribute> newAttributes = new ArrayList<Attribute>(
			oldAttributes.size());

		for (org.dom4j.Attribute oldAttribute : oldAttributes) {
			newAttributes.add(new AttributeImpl(oldAttribute));
		}

		return new NodeList<Attribute, org.dom4j.Attribute>(
			newAttributes, oldAttributes);
	}

	public static List<Element> toNewElements(
		List<org.dom4j.Element> oldElements) {

		List<Element> newElements = new ArrayList<Element>(oldElements.size());

		for (org.dom4j.Element oldElement : oldElements) {
			newElements.add(new ElementImpl(oldElement));
		}

		return new NodeList<Element, org.dom4j.Element>(
			newElements, oldElements);
	}

	public static List<Namespace> toNewNamespaces(
		List<org.dom4j.Namespace> oldNamespaces) {

		List<Namespace> newNamespaces = new ArrayList<Namespace>(
			oldNamespaces.size());

		for (org.dom4j.Namespace oldNamespace : oldNamespaces) {
			newNamespaces.add(new NamespaceImpl(oldNamespace));
		}

		return new NodeList<Namespace, org.dom4j.Namespace>(
			newNamespaces, oldNamespaces);
	}

	public static List<Node> toNewNodes(List<org.dom4j.Node> oldNodes) {
		List<Node> newNodes = new ArrayList<Node>(oldNodes.size());

		for (org.dom4j.Node oldNode : oldNodes) {
			if (oldNode instanceof org.dom4j.Element) {
				newNodes.add(new ElementImpl((org.dom4j.Element)oldNode));
			}
			else {
				newNodes.add(new NodeImpl(oldNode));
			}
		}

		return new NodeList<Node, org.dom4j.Node>(newNodes, oldNodes);
	}

	public static List<ProcessingInstruction> toNewProcessingInstructions(
		List<org.dom4j.ProcessingInstruction> oldProcessingInstructions) {

		List<ProcessingInstruction> newProcessingInstructions =
			new ArrayList<ProcessingInstruction>(
				oldProcessingInstructions.size());

		for (org.dom4j.ProcessingInstruction oldProcessingInstruction :
				oldProcessingInstructions) {

			newProcessingInstructions.add(
				new ProcessingInstructionImpl(oldProcessingInstruction));
		}

		return new NodeList
			<ProcessingInstruction, org.dom4j.ProcessingInstruction>(
				newProcessingInstructions, oldProcessingInstructions);
	}

	public static List<org.dom4j.Attribute> toOldAttributes(
		List<Attribute> newAttributes) {

		List<org.dom4j.Attribute> oldAttributes =
			new ArrayList<org.dom4j.Attribute>(newAttributes.size());

		for (Attribute newAttribute : newAttributes) {
			AttributeImpl newAttributeImpl = (AttributeImpl)newAttribute;

			oldAttributes.add(newAttributeImpl.getWrappedAttribute());
		}

		return oldAttributes;
	}

	public static List<org.dom4j.Node> toOldNodes(List<Node> newNodes) {
		List<org.dom4j.Node> oldNodes = new ArrayList<org.dom4j.Node>(
			newNodes.size());

		for (Node newNode : newNodes) {
			NodeImpl newNodeImpl = (NodeImpl)newNode;

			oldNodes.add(newNodeImpl.getWrappedNode());
		}

		return oldNodes;
	}

	public static List<org.dom4j.ProcessingInstruction>
		toOldProcessingInstructions(
			List<ProcessingInstruction> newProcessingInstructions) {

		List<org.dom4j.ProcessingInstruction> oldProcessingInstructions =
			new ArrayList<org.dom4j.ProcessingInstruction>(
				newProcessingInstructions.size());

		for (ProcessingInstruction newProcessingInstruction :
				newProcessingInstructions) {

			ProcessingInstructionImpl newProcessingInstructionImpl =
				(ProcessingInstructionImpl)newProcessingInstruction;

			oldProcessingInstructions.add(
				newProcessingInstructionImpl.getWrappedProcessingInstruction());
		}

		return oldProcessingInstructions;
	}

	@Override
	public Attribute createAttribute(
		Element element, QName qName, String value) {

		ElementImpl elementImpl = (ElementImpl)element;
		QNameImpl qNameImpl = (QNameImpl)qName;

		return new AttributeImpl(
			_documentFactory.createAttribute(
				elementImpl.getWrappedElement(), qNameImpl.getWrappedQName(),
				value));
	}

	@Override
	public Attribute createAttribute(
		Element element, String name, String value) {

		ElementImpl elementImpl = (ElementImpl)element;

		return new AttributeImpl(
			_documentFactory.createAttribute(
				elementImpl.getWrappedElement(), name, value));
	}

	@Override
	public Document createDocument() {
		return new DocumentImpl(_documentFactory.createDocument());
	}

	@Override
	public Document createDocument(Element rootElement) {
		ElementImpl rootElementImpl = (ElementImpl)rootElement;

		return new DocumentImpl(
			_documentFactory.createDocument(
				rootElementImpl.getWrappedElement()));
	}

	@Override
	public Document createDocument(String encoding) {
		return new DocumentImpl(_documentFactory.createDocument(encoding));
	}

	@Override
	public Element createElement(QName qName) {
		QNameImpl qNameImpl = (QNameImpl)qName;

		return new ElementImpl(
			_documentFactory.createElement(qNameImpl.getWrappedQName()));
	}

	@Override
	public Element createElement(String name) {
		return new ElementImpl(_documentFactory.createElement(name));
	}

	@Override
	public Entity createEntity(String name, String text) {
		return new EntityImpl(_documentFactory.createEntity(name, text));
	}

	@Override
	public Namespace createNamespace(String uri) {
		return new NamespaceImpl(org.dom4j.Namespace.get(uri));
	}

	@Override
	public Namespace createNamespace(String prefix, String uri) {
		return new NamespaceImpl(_documentFactory.createNamespace(prefix, uri));
	}

	@Override
	public ProcessingInstruction createProcessingInstruction(
		String target, Map<String, String> data) {

		org.dom4j.ProcessingInstruction processingInstruction =
			_documentFactory.createProcessingInstruction(target, data);

		if (processingInstruction == null) {
			return null;
		}
		else {
			return new ProcessingInstructionImpl(processingInstruction);
		}
	}

	@Override
	public ProcessingInstruction createProcessingInstruction(
		String target, String data) {

		org.dom4j.ProcessingInstruction processingInstruction =
			_documentFactory.createProcessingInstruction(target, data);

		if (processingInstruction == null) {
			return null;
		}
		else {
			return new ProcessingInstructionImpl(processingInstruction);
		}
	}

	@Override
	public QName createQName(String localName) {
		return new QNameImpl(_documentFactory.createQName(localName));
	}

	@Override
	public QName createQName(String localName, Namespace namespace) {
		NamespaceImpl namespaceImpl = (NamespaceImpl)namespace;

		return new QNameImpl(
			_documentFactory.createQName(
				localName, namespaceImpl.getWrappedNamespace()));
	}

	@Override
	public Text createText(String text) {
		return new TextImpl(_documentFactory.createText(text));
	}

	@Override
	public XPath createXPath(String xPathExpression) {
		return createXPath(xPathExpression, null);
	}

	@Override
	public XPath createXPath(
		String xPathExpression, Map<String, String> namespaceContextMap) {

		return new XPathImpl(
			_documentFactory.createXPath(xPathExpression), namespaceContextMap);
	}

	@Override
	public XPath createXPath(
		String xPathExpression, String prefix, String namespace) {

		Map<String, String> namespaceContextMap = new HashMap<String, String>();

		namespaceContextMap.put(prefix, namespace);

		return createXPath(xPathExpression, namespaceContextMap);
	}

	@Override
	public Element mergeElement(
		Element originalElement, Element changedElement) {

		Element mergedElement = originalElement.createCopy();

		if (mergedElement.getName().equals(changedElement.getName())) {
			mergedElement.setText(changedElement.getText());
		}

		List<Element> sourceElements = mergedElement.elements();

		for (Element sourceElement : sourceElements) {
			List<Element> sourceElementsName = mergedElement.elements(
							sourceElement.getName());

			List<Element> destElementsName = changedElement.elements(
							sourceElement.getName());

			if (sourceElementsName.size()>1 && destElementsName.size() >0) {
				for (Element srcEL : sourceElementsName) {
					mergedElement.remove(srcEL);
				}
			}
		}

		sourceElements = mergedElement.elements();

		for (Element sourceElement : sourceElements) {
			Element destElementName = changedElement.element(
							sourceElement.getName());

			if (destElementName != null) {
				Element tempMergedElement = mergeElement(
					sourceElement, destElementName);

				mergedElement.remove(sourceElement);

				if (!_elementEmpty(tempMergedElement)) {
					mergedElement.add(tempMergedElement.detach());
				}
			}
		}

		List<Element> changedElements = changedElement.elements();

		for (Element tmpItemEl : changedElements) {
			Element sourceEl = mergedElement.element(tmpItemEl.getName());

			if (sourceEl == null) {
				List<Element> arrayElements = changedElement.elements(
								tmpItemEl.getName());

				for (Element arrayElement : arrayElements) {
					if (!_elementEmpty(arrayElement)) {
						mergedElement.add(arrayElement.detach());
					}
				}
			}
		}

		return mergedElement;
	}

	@Override
	public Document read(File file) throws DocumentException {
		return read(file, false);
	}

	@Override
	public Document read(File file, boolean validate) throws DocumentException {
		ClassLoader classLoader = getClass().getClassLoader();

		ClassLoader contextClassLoader =
			ClassLoaderUtil.getContextClassLoader();

		try {
			if (contextClassLoader != classLoader) {
				ClassLoaderUtil.setContextClassLoader(classLoader);
			}

			org.dom4j.io.SAXReader saxReader = getSAXReader(validate);

			return new DocumentImpl(saxReader.read(file));
		}
		catch (org.dom4j.DocumentException de) {
			throw new DocumentException(de.getMessage(), de);
		}
		finally {
			if (contextClassLoader != classLoader) {
				ClassLoaderUtil.setContextClassLoader(contextClassLoader);
			}
		}
	}

	@Override
	public Document read(InputStream is) throws DocumentException {
		return read(is, false);
	}

	@Override
	public Document read(InputStream is, boolean validate)
		throws DocumentException {

		ClassLoader classLoader = getClass().getClassLoader();

		ClassLoader contextClassLoader =
			ClassLoaderUtil.getContextClassLoader();

		try {
			if (contextClassLoader != classLoader) {
				ClassLoaderUtil.setContextClassLoader(classLoader);
			}

			org.dom4j.io.SAXReader saxReader = getSAXReader(validate);

			return new DocumentImpl(saxReader.read(is));
		}
		catch (org.dom4j.DocumentException de) {
			throw new DocumentException(de.getMessage(), de);
		}
		finally {
			if (contextClassLoader != classLoader) {
				ClassLoaderUtil.setContextClassLoader(contextClassLoader);
			}
		}
	}

	@Override
	public Document read(Properties props) throws DocumentException {
		return read(props, null);
	}

	@Override
	public Document read(Properties props, String prefix)
		throws DocumentException {

		List<String> processedKeys = new ArrayList<String>();

		return _read (props, prefix, processedKeys);
	}

	@Override
	public Document read(Reader reader) throws DocumentException {
		return read(reader, false);
	}

	@Override
	public Document read(Reader reader, boolean validate)
		throws DocumentException {

		ClassLoader classLoader = getClass().getClassLoader();

		ClassLoader contextClassLoader =
			ClassLoaderUtil.getContextClassLoader();

		try {
			if (contextClassLoader != classLoader) {
				ClassLoaderUtil.setContextClassLoader(classLoader);
			}

			org.dom4j.io.SAXReader saxReader = getSAXReader(validate);

			return new DocumentImpl(saxReader.read(reader));
		}
		catch (org.dom4j.DocumentException de) {
			throw new DocumentException(de.getMessage(), de);
		}
		finally {
			if (contextClassLoader != classLoader) {
				ClassLoaderUtil.setContextClassLoader(contextClassLoader);
			}
		}
	}

	@Override
	public Document read(String xml) throws DocumentException {
		return read(new XMLSafeReader(xml));
	}

	@Override
	public Document read(String xml, boolean validate)
		throws DocumentException {

		return read(new XMLSafeReader(xml), validate);
	}

	@Override
	public Document read(String xml, XMLSchema xmlSchema)
		throws DocumentException {

		ClassLoader classLoader = getClass().getClassLoader();

		ClassLoader contextClassLoader =
			ClassLoaderUtil.getContextClassLoader();

		try {
			if (contextClassLoader != classLoader) {
				ClassLoaderUtil.setContextClassLoader(classLoader);
			}

			org.dom4j.io.SAXReader saxReader = getSAXReader(xmlSchema);

			Reader reader = new XMLSafeReader(xml);

			return new DocumentImpl(saxReader.read(reader));
		}
		catch (org.dom4j.DocumentException de) {
			throw new DocumentException(de.getMessage(), de);
		}
		finally {
			if (contextClassLoader != classLoader) {
				ClassLoaderUtil.setContextClassLoader(contextClassLoader);
			}
		}
	}

	@Override
	public Document read(URL url) throws DocumentException {
		return read(url, false);
	}

	@Override
	public Document read(URL url, boolean validate) throws DocumentException {
		ClassLoader classLoader = getClass().getClassLoader();

		ClassLoader contextClassLoader =
			ClassLoaderUtil.getContextClassLoader();

		try {
			if (contextClassLoader != classLoader) {
				ClassLoaderUtil.setContextClassLoader(classLoader);
			}

			org.dom4j.io.SAXReader saxReader = getSAXReader(validate);

			return new DocumentImpl(saxReader.read(url));
		}
		catch (org.dom4j.DocumentException de) {
			throw new DocumentException(de.getMessage(), de);
		}
		finally {
			if (contextClassLoader != classLoader) {
				ClassLoaderUtil.setContextClassLoader(contextClassLoader);
			}
		}
	}

	@Override
	public Document readURL(String url)
		throws DocumentException, MalformedURLException {

		return read(new URL(url), false);
	}

	@Override
	public Document readURL(String url, boolean validate)
		throws DocumentException, MalformedURLException {

		return read(new URL(url), validate);
	}

	@Override
	public List<Node> selectNodes(
		String xPathFilterExpression, List<Node> nodes) {

		org.dom4j.XPath xPath = _documentFactory.createXPath(
			xPathFilterExpression);

		return toNewNodes(xPath.selectNodes(toOldNodes(nodes)));
	}

	@Override
	public List<Node> selectNodes(String xPathFilterExpression, Node node) {
		NodeImpl nodeImpl = (NodeImpl)node;

		org.dom4j.XPath xPath = _documentFactory.createXPath(
			xPathFilterExpression);

		return toNewNodes(xPath.selectNodes(nodeImpl.getWrappedNode()));
	}

	@Override
	public void sort(List<Node> nodes, String xPathExpression) {
		org.dom4j.XPath xPath = _documentFactory.createXPath(xPathExpression);

		xPath.sort(toOldNodes(nodes));
	}

	@Override
	public void sort(
		List<Node> nodes, String xPathExpression, boolean distinct) {

		org.dom4j.XPath xPath = _documentFactory.createXPath(xPathExpression);

		xPath.sort(toOldNodes(nodes), distinct);
	}

	protected org.dom4j.io.SAXReader getSAXReader(boolean validate) {
		org.dom4j.io.SAXReader reader = null;

		if (!PropsValues.XML_VALIDATION_ENABLED) {
			validate = false;
		}

		try {
			reader = new org.dom4j.io.SAXReader(new SAXParser(), validate);

			reader.setEntityResolver(new EntityResolver());

			reader.setFeature(_FEATURES_DYNAMIC, validate);
			reader.setFeature(_FEATURES_EXTERNAL_GENERAL_ENTITIES, validate);
			reader.setFeature(_FEATURES_LOAD_DTD_GRAMMAR, validate);
			reader.setFeature(_FEATURES_LOAD_EXTERNAL_DTD, validate);
			reader.setFeature(_FEATURES_VALIDATION, validate);
			reader.setFeature(_FEATURES_VALIDATION_SCHEMA, validate);
			reader.setFeature(
				_FEATURES_VALIDATION_SCHEMA_FULL_CHECKING, validate);
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"XSD validation is disabled because " + e.getMessage());
			}

			reader = new org.dom4j.io.SAXReader(false);

			reader.setEntityResolver(new EntityResolver());
		}

		return reader;
	}

	protected org.dom4j.io.SAXReader getSAXReader(XMLSchema xmlSchema) {
		boolean validate = true;

		if (!PropsValues.XML_VALIDATION_ENABLED) {
			validate = false;
		}

		org.dom4j.io.SAXReader saxReader = getSAXReader(validate);

		if ((xmlSchema == null) || (validate == false)) {
			return saxReader;
		}

		try {
			saxReader.setProperty(
				_PROPERTY_SCHEMA_LANGUAGE, xmlSchema.getSchemaLanguage());
			saxReader.setProperty(
				_PROPERTY_SCHEMA_SOURCE, xmlSchema.getSchemaSource());
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"XSD validation is disabled because " + e.getMessage());
			}
		}

		return saxReader;
	}

	private void _copyInRoot(
		String elementName, Document source, Document destination,
		boolean copyNoRootElements) {

		Element sourceRootElement = source.getRootElement();
		List<Element> elements = sourceRootElement.elements();

		Element destinationRootElement = destination.getRootElement();

		if ((elements.size() > 0) || copyNoRootElements) {
			Element subElement = destinationRootElement.addElement(elementName);

			for (Element el : elements) {
				subElement.add(el.detach());
			}

			subElement.setText(sourceRootElement.getText());
		}
	}

	private boolean _elementEmpty(Element element) {
		int sizeElements = element.elements().size();
		String text = element.getText();

		if (((sizeElements == 0) && Validator.isNotNull(text)) ||
			(sizeElements >0)) {

			return false;
		}

		return true;
	}

	private Document _read(
		Properties props, String prefix, List<String> processedKeys) {

		return _read (props, prefix, processedKeys, false);
	}

	private Document _read(
		Properties props, String prefix, List<String> processedKeys,
		boolean allowEmptyElement) {

		Document document = createDocument();

		document.addElement("root");

		Properties properties = props;

		if (prefix != null) {
			properties = PropertiesUtil.getProperties(
				properties, prefix, false);
		}

		Iterator<Object> keyIterator = properties.keySet().iterator();

		while (keyIterator.hasNext()) {
			String key = (String)keyIterator.next();

			if (processedKeys.contains(key)) {
				continue;
			}

			String nextPrefix = key;

			int posPrefix = 0;

			if (prefix != null) {
				posPrefix = prefix.length();
			}

			String keyNoPrefix = nextPrefix.substring(posPrefix);

			if (keyNoPrefix.startsWith(StringPool.PERIOD)) {
				posPrefix += 1;
			}

			keyNoPrefix = nextPrefix.substring(posPrefix);

			int posFirstPeriod = keyNoPrefix.indexOf(CharPool.PERIOD);

			if (posFirstPeriod == -1) {
				posFirstPeriod = keyNoPrefix.length();
			}

			String elementName = keyNoPrefix.substring(0, posFirstPeriod);

			if (prefix != null) {
				posFirstPeriod = posFirstPeriod + posPrefix;
			}

			if (posFirstPeriod == -1) {
				posFirstPeriod = nextPrefix.length();
			}

			nextPrefix = nextPrefix.substring(0, posFirstPeriod);

			Properties portletSubProperties =
							PropertiesUtil.getProperties(
								properties, nextPrefix, false);

			posFirstPeriod = keyNoPrefix.indexOf(CharPool.PERIOD);
			int posLastPeriod = keyNoPrefix.lastIndexOf(CharPool.PERIOD);

			if (posFirstPeriod !=-1 && (posFirstPeriod <= posLastPeriod) &&
				(portletSubProperties.size() > 0)) {

				Document tempDocument = null;

				Properties portletSubPropertiesArray =
								PropertiesUtil.getProperties(
									portletSubProperties, nextPrefix+".0",
									false);

				if (portletSubPropertiesArray.size()>0) {
					for (int i = 0; i< portletSubProperties.size(); i++) {
						String arrayKey = nextPrefix + "."+ i;
						Properties portletSubPropertiesItem =
										PropertiesUtil.getProperties(
											portletSubProperties, arrayKey,
											false);

						if (portletSubPropertiesItem.size() > 0) {
							tempDocument = _read(
								portletSubProperties, arrayKey, processedKeys,
								true);
							_copyInRoot(
								elementName, tempDocument, document, true);
						}
					}
				}

				tempDocument = _read(
					portletSubProperties, nextPrefix, processedKeys);

				_copyInRoot(elementName, tempDocument, document, false);
			}
			else {
				String value = properties.getProperty(key);

				Element element = null;

				if (Validator.isNotNull(elementName)) {
					element = createElement(elementName);
					element.setText(value);
					document.getRootElement().add(element.detach());
				}
				else if (allowEmptyElement) {
					element = document.getRootElement();
					element.setText(value);
				}
			}

			processedKeys.add(key);
		}

		return document;
	}

	private static final String _FEATURES_DYNAMIC =
		"http://apache.org/xml/features/validation/dynamic";

	private static final String _FEATURES_EXTERNAL_GENERAL_ENTITIES =
		"http://xml.org/sax/features/external-general-entities";

	private static final String _FEATURES_LOAD_DTD_GRAMMAR =
		"http://apache.org/xml/features/nonvalidating/load-dtd-grammar";

	private static final String _FEATURES_LOAD_EXTERNAL_DTD =
		"http://apache.org/xml/features/nonvalidating/load-external-dtd";

	private static final String _FEATURES_VALIDATION =
		"http://xml.org/sax/features/validation";

	private static final String _FEATURES_VALIDATION_SCHEMA =
		"http://apache.org/xml/features/validation/schema";

	private static final String _FEATURES_VALIDATION_SCHEMA_FULL_CHECKING =
		"http://apache.org/xml/features/validation/schema-full-checking";

	private static final String _PROPERTY_SCHEMA_LANGUAGE =
		"http://java.sun.com/xml/jaxp/properties/schemaLanguage";

	private static final String _PROPERTY_SCHEMA_SOURCE =
		"http://java.sun.com/xml/jaxp/properties/schemaSource";

	private static final Log _log = LogFactoryUtil.getLog(SAXReaderImpl.class);

	private static final SAXReaderImpl _instance = new SAXReaderImpl();

	private final DocumentFactory _documentFactory =
		DocumentFactory.getInstance();

}
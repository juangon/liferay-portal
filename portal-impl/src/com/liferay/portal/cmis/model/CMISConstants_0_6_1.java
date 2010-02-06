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

package com.liferay.portal.cmis.model;

import javax.xml.namespace.QName;

/**
 * <a href="CMISConstants_0_6_1.java.html"><b><i>View Source</i></b></a>
 *
 * @author Alexander Chow
 */
public class CMISConstants_0_6_1 extends CMISConstants {

	public static CMISConstants getInstance() {
		return _instance;
	}

	private CMISConstants_0_6_1() {
		CMIS_NS = "http://docs.oasis-open.org/ns/cmis/core/200901";

		CMIS_PREFIX = "cmis";

		VERSION = "0.61";

		// Repository

		REPOSITORY_DESCRIPTION = new QName(
			CMIS_NS, "repositoryDescription", CMIS_PREFIX);

		REPOSITORY_ID = new QName(CMIS_NS, "repositoryId", CMIS_PREFIX);

		REPOSITORY_INFO = new QName(CMIS_NS, "repositoryInfo", CMIS_PREFIX);

		REPOSITORY_NAME = new QName(CMIS_NS, "repositoryName", CMIS_PREFIX);

		REPOSITORY_PRODUCT_NAME = new QName(
			CMIS_NS, "productName", CMIS_PREFIX);

		REPOSITORY_PRODUCT_VERSION = new QName(
			CMIS_NS, "productVersion", CMIS_PREFIX);

		REPOSITORY_RELATIONSHIP = new QName(
			CMIS_NS, "repositoryRelationship", CMIS_PREFIX);

		REPOSITORY_ROOT_FOLDER_ID = new QName(
			CMIS_NS, "rootFolderId", CMIS_PREFIX);

		REPOSITORY_SPECIFIC_INFO = new QName(
			CMIS_NS, "repositorySpecificInformation", CMIS_PREFIX);

		REPOSITORY_VENDOR_NAME = new QName(CMIS_NS, "vendorName", CMIS_PREFIX);

		REPOSITORY_VERSION_SUPPORTED = new QName(
			CMIS_NS, "cmisVersionSupported", CMIS_PREFIX);

		// Collection

		COLLECTION_TYPE = new QName(CMIS_NS, "collectionType", CMIS_PREFIX);

		COLLECTION_CHECKEDOUT = "checkedout";

		COLLECTION_QUERY = "query";

		COLLECTION_ROOT = "rootchildren";

		COLLECTION_ROOT_DESCENDANTS = "rootdescendants";

		COLLECTION_TYPES_CHILDREN = "typeschildren";

		COLLECTION_TYPES_DESCENDANTS = "typesdescendants";

		COLLECTION_UNFILED = "unfiled";

		// Object

		OBJECT = new QName(CMIS_NS, "object", CMIS_PREFIX);

		BASE_TYPE_DOCUMENT = "document";

		BASE_TYPE_FOLDER = "folder";

		LINK_ALL_VERSIONS = "allversions";

		LINK_ALLOWABLE_ACTIONS = "allowableactions";

		LINK_CHILDREN = "children";

		LINK_DESCENDANTS = "descendants";

		LINK_LATEST_VERSION = "latestversion";

		LINK_PARENT = "parent";

		LINK_PARENTS = "parents";

		LINK_POLICIES = "policies";

		LINK_RELATIONSHIPS = "relationships";

		LINK_REPOSITORY = "repository";

		LINK_SOURCE = "source";

		LINK_STREAM = "stream";

		LINK_TARGET = "target";

		LINK_TYPE = "type";

		PROPERTIES = new QName(CMIS_NS, "properties", CMIS_PREFIX);

		PROPERTY_NAME = new QName(CMIS_NS, "name", CMIS_PREFIX);

		PROPERTY_TYPE_STRING = new QName(
			CMIS_NS, "propertyString", CMIS_PREFIX);

		PROPERTY_TYPE_DECIMAL = new QName(
			CMIS_NS, "propertyDecimal", CMIS_PREFIX);

		PROPERTY_TYPE_INTEGER = new QName(
			CMIS_NS, "propertyInteger", CMIS_PREFIX);

		PROPERTY_TYPE_BOOLEAN = new QName(
			CMIS_NS, "propertyBoolean", CMIS_PREFIX);

		PROPERTY_TYPE_DATETIME = new QName(
			CMIS_NS, "propertyDateTime", CMIS_PREFIX);

		PROPERTY_TYPE_URI = new QName(CMIS_NS, "propertyUri", CMIS_PREFIX);

		PROPERTY_TYPE_ID = new QName(CMIS_NS, "propertyId", CMIS_PREFIX);

		PROPERTY_TYPE_XML = new QName(CMIS_NS, "propertyXml", CMIS_PREFIX);

		PROPERTY_TYPE_HTML = new QName(CMIS_NS, "propertyHtml", CMIS_PREFIX);

		PROPERTY_VALUE = new QName(CMIS_NS, "value", CMIS_PREFIX);

		PROPERTY_NAME_BASETYPE = "BaseType";

		PROPERTY_NAME_CHECKIN_COMMENT = "CheckinComment";

		PROPERTY_NAME_CONTENT_STREAM_FILENAME = "ContentStreamFilename";

		PROPERTY_NAME_CONTENT_STREAM_LENGTH = "ContentStreamLength";

		PROPERTY_NAME_CONTENT_STREAM_MIMETYPE = "ContentStreamMimetype";

		PROPERTY_NAME_CONTENT_STREAM_URI = "ContentStreamUri";

		PROPERTY_NAME_CREATED_BY = "CreatedBy";

		PROPERTY_NAME_CREATION_DATE = "CreationDate";

		PROPERTY_NAME_IS_IMMUTABLE = "IsImmutable";

		PROPERTY_NAME_IS_LATEST_MAJOR_VERSION = "IsLatestMajorVersion";

		PROPERTY_NAME_IS_LATEST_VERSION = "IsLatestVersion";

		PROPERTY_NAME_IS_MAJOR_VERSION = "IsMajorVersion";

		PROPERTY_NAME_IS_VERSION_SERIES_CHECKED_OUT =
			"IsVersionSeriesCheckedOut";

		PROPERTY_NAME_LAST_MODIFIED_BY = "LastModifiedBy";

		PROPERTY_NAME_LAST_MODIFICATION_DATE = "LastModificationDate";

		PROPERTY_NAME_NAME = "Name";

		PROPERTY_NAME_OBJECT_ID  = "ObjectId";

		PROPERTY_NAME_OBJECT_TYPE_ID = "ObjectTypeId";

		PROPERTY_NAME_SOURCE_ID = "SourceId";

		PROPERTY_NAME_TARGET_ID = "TargetId";

		PROPERTY_NAME_VERSION_LABEL = "VersionLabel";

		PROPERTY_NAME_VERSION_SERIES_CHECKED_OUT_BY =
			"VersionSeriesCheckedOutBy";

		PROPERTY_NAME_VERSION_SERIES_CHECKED_OUT_ID =
			"VersionSeriesCheckedOutId";

		PROPERTY_NAME_VERSION_SERIES_ID = "VersionSeriesId";
	}

	private static CMISConstants _instance = new CMISConstants_0_6_1();

}
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

package com.liferay.portal.kernel.lar;

import com.liferay.portal.NoSuchModelException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.trash.TrashHandler;
import com.liferay.portal.kernel.trash.TrashHandlerRegistryUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.StagedModel;
import com.liferay.portal.model.TrashedModel;
import com.liferay.portal.model.WorkflowedModel;
import com.liferay.portlet.sitesadmin.lar.StagedGroup;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mate Thurzo
 * @author Daniel Kocsis
 * @author Zsolt Berentey
 */
public abstract class BaseStagedModelDataHandler<T extends StagedModel>
	implements StagedModelDataHandler<T> {

	@Override
	public abstract void deleteStagedModel(
			String uuid, long groupId, String className, String extraData)
		throws PortalException, SystemException;

	@Override
	public void exportStagedModel(
			PortletDataContext portletDataContext, T stagedModel)
		throws PortletDataException {

		validateExport(portletDataContext, stagedModel);

		String path = ExportImportPathUtil.getModelPath(stagedModel);

		if (portletDataContext.isPathExportedInScope(path)) {
			return;
		}

		try {
			ManifestSummary manifestSummary =
				portletDataContext.getManifestSummary();

			PortletDataHandlerStatusMessageSenderUtil.sendStatusMessage(
				"stagedModel", stagedModel, manifestSummary);

			doExportStagedModel(portletDataContext, (T)stagedModel.clone());

			if (countStagedModel(portletDataContext, stagedModel)) {
				manifestSummary.incrementModelAdditionCount(
					stagedModel.getStagedModelType());
			}
		}
		catch (PortletDataException pde) {
			throw pde;
		}
		catch (Exception e) {
			PortletDataException pde = new PortletDataException(e);

			if (e instanceof NoSuchModelException) {
				pde.setStagedModel(stagedModel);
				pde.setType(PortletDataException.MISSING_DEPENDENCY);
			}

			throw pde;
		}
	}

	@Override
	public abstract String[] getClassNames();

	@Override
	public String getDisplayName(T stagedModel) {
		return stagedModel.getUuid();
	}

	@Override
	public int[] getExportableStatuses() {
		return new int[] {WorkflowConstants.STATUS_APPROVED};
	}

	@Override
	public Map<String, String> getReferenceAttributes(
		PortletDataContext portletDataContext, T stagedModel) {

		return new HashMap<String, String>();
	}

	/**
	 * @deprecated As of 7.0.0, replaced by {@link
	 *             #importMissingReference(PortletDataContext, Element)}
	 */
	@Deprecated
	@Override
	public void importCompanyStagedModel(
			PortletDataContext portletDataContext, Element referenceElement)
		throws PortletDataException {

		importMissingReference(portletDataContext, referenceElement);
	}

	/**
	 * @deprecated As of 7.0.0, replaced by {@link
	 *             #importMissingReference(PortletDataContext, String, long,
	 *             long)}
	 */
	@Deprecated
	@Override
	public void importCompanyStagedModel(
			PortletDataContext portletDataContext, String uuid, long classPK)
		throws PortletDataException {

		importMissingReference(
			portletDataContext, uuid, portletDataContext.getCompanyGroupId(),
			classPK);
	}

	@Override
	public void importMissingReference(
			PortletDataContext portletDataContext, Element referenceElement)
		throws PortletDataException {

		importMissingGroupReference(portletDataContext, referenceElement);

		String uuid = referenceElement.attributeValue("uuid");

		long liveGroupId = GetterUtil.getLong(
			referenceElement.attributeValue("live-group-id"));

		Map<Long, Long> groupIds =
			(Map<Long, Long>)portletDataContext.getNewPrimaryKeysMap(
				Group.class);

		liveGroupId = MapUtil.getLong(groupIds, liveGroupId, liveGroupId);

		long classPK = GetterUtil.getLong(
			referenceElement.attributeValue("class-pk"));

		importMissingReference(portletDataContext, uuid, liveGroupId, classPK);
	}

	@Override
	public void importMissingReference(
			PortletDataContext portletDataContext, String uuid, long groupId,
			long classPK)
		throws PortletDataException {

		try {
			doImportMissingReference(
				portletDataContext, uuid, groupId, classPK);
		}
		catch (PortletDataException pde) {
			throw pde;
		}
		catch (Exception e) {
			throw new PortletDataException(e);
		}
	}

	@Override
	public void importStagedModel(
			PortletDataContext portletDataContext, T stagedModel)
		throws PortletDataException {

		String path = ExportImportPathUtil.getModelPath(stagedModel);

		if (portletDataContext.isPathProcessed(path)) {
			return;
		}

		try {
			ManifestSummary manifestSummary =
				portletDataContext.getManifestSummary();

			PortletDataHandlerStatusMessageSenderUtil.sendStatusMessage(
				"stagedModel", stagedModel, manifestSummary);

			if (stagedModel instanceof TrashedModel) {
				restoreStagedModel(portletDataContext, stagedModel);
			}

			doImportStagedModel(portletDataContext, stagedModel);

			manifestSummary.incrementModelAdditionCount(
				stagedModel.getStagedModelType());
		}
		catch (PortletDataException pde) {
			throw pde;
		}
		catch (Exception e) {
			throw new PortletDataException(e);
		}
	}

	@Override
	public void restoreStagedModel(
			PortletDataContext portletDataContext, T stagedModel)
		throws PortletDataException {

		try {
			doRestoreStagedModel(portletDataContext, stagedModel);
		}
		catch (PortletDataException pde) {
			throw pde;
		}
		catch (Exception e) {
			throw new PortletDataException(e);
		}
	}

	@Override
	public boolean validateReference(
		PortletDataContext portletDataContext, Element referenceElement) {

		String uuid = referenceElement.attributeValue("uuid");

		if (!validateMissingGroupReference(
				portletDataContext, referenceElement)) {

			return false;
		}

		long liveGroupId = GetterUtil.getLong(
			referenceElement.attributeValue("live-group-id"));

		Map<Long, Long> groupIds =
			(Map<Long, Long>)portletDataContext.getNewPrimaryKeysMap(
				Group.class);

		liveGroupId = MapUtil.getLong(groupIds, liveGroupId, liveGroupId);

		try {
			return validateMissingReference(
				uuid, portletDataContext.getCompanyId(), liveGroupId);
		}
		catch (Exception e) {
			return false;
		}
	}

	protected boolean countStagedModel(
		PortletDataContext portletDataContext, T stagedModel) {

		return !portletDataContext.isStagedModelCounted(stagedModel);
	}

	protected abstract void doExportStagedModel(
			PortletDataContext portletDataContext, T stagedModel)
		throws Exception;

	protected void doImportMissingReference(
			PortletDataContext portletDataContext, String uuid, long groupId,
			long classPK)
		throws Exception {

		throw new UnsupportedOperationException();
	}

	protected abstract void doImportStagedModel(
			PortletDataContext portletDataContext, T stagedModel)
		throws Exception;

	protected void doRestoreStagedModel(
			PortletDataContext portletDataContext, T stagedModel)
		throws Exception {

		throw new UnsupportedOperationException();
	}


	protected void importMissingGroupReference(
			PortletDataContext portletDataContext, Element referenceElement)
		throws PortletDataException {

		StagedModelDataHandler<StagedGroup> stagedModelDataHandler =
			(StagedModelDataHandler<StagedGroup>)
				StagedModelDataHandlerRegistryUtil.getStagedModelDataHandler(
					StagedGroup.class.getName());

		stagedModelDataHandler.importMissingReference(
			portletDataContext, referenceElement);
	}

	protected void validateExport(
			PortletDataContext portletDataContext, T stagedModel)
		throws PortletDataException {

		if (stagedModel instanceof WorkflowedModel) {
			WorkflowedModel workflowedModel = (WorkflowedModel)stagedModel;

			if (!ArrayUtil.contains(
					getExportableStatuses(), workflowedModel.getStatus())) {

				PortletDataException pde = new PortletDataException(
					PortletDataException.STATUS_UNAVAILABLE);

				pde.setStagedModel(stagedModel);

				throw pde;
			}
		}

		StagedModelType stagedModelType = stagedModel.getStagedModelType();

		TrashHandler trashHandler = TrashHandlerRegistryUtil.getTrashHandler(
			stagedModelType.getClassName());

		if (trashHandler != null) {
			try {
				long classPK = (Long)stagedModel.getPrimaryKeyObj();

				if (trashHandler.isInTrash(classPK)) {
					PortletDataException pde = new PortletDataException(
						PortletDataException.STATUS_IN_TRASH);

					pde.setStagedModel(stagedModel);

					throw pde;
				}
			}
			catch (PortletDataException pde) {
				throw pde;
			}
			catch (Exception e) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to check trash status for " +
							stagedModel.getModelClassName());
				}
			}
		}
	}

	protected boolean validateMissingGroupReference(
		PortletDataContext portletDataContext, Element referenceElement) {

		StagedModelDataHandler<StagedGroup> stagedModelDataHandler =
			(StagedModelDataHandler<StagedGroup>)
				StagedModelDataHandlerRegistryUtil.getStagedModelDataHandler(
					StagedGroup.class.getName());

		return stagedModelDataHandler.validateReference(
			portletDataContext, referenceElement);
	}

	protected boolean validateMissingReference(
			String uuid, long companyId, long groupId)
		throws Exception {

		return true;
	}

	private static Log _log = LogFactoryUtil.getLog(
		BaseStagedModelDataHandler.class);

}

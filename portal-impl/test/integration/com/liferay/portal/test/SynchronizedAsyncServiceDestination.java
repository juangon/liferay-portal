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

package com.liferay.portal.test;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusException;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import com.liferay.portal.kernel.messaging.SynchronousDestination;
import com.liferay.portal.kernel.messaging.sender.DefaultSynchronousMessageSender;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;

/**
 * @author Zsolt Berentey
 */
public class SynchronizedAsyncServiceDestination
	extends SynchronousDestination {

	public SynchronizedAsyncServiceDestination(String destinationName) {
		_destinationName = destinationName;

		_defaultSynchronousMessageSender.setMessageBus(
			MessageBusUtil.getMessageBus());
		_defaultSynchronousMessageSender.setPortalUUID(
			PortalUUIDUtil.getPortalUUID());
		_defaultSynchronousMessageSender.setTimeout(Time.SECOND * 10);
	}

	@Override
	public void send(Message message) {
		try {
			_defaultSynchronousMessageSender.send(_destinationName, message);
		}
		catch (MessageBusException mbe) {
			_log.error("Unable to process message " + message, mbe);
		}
	}

	private static Log _log = LogFactoryUtil.getLog(
		SynchronizedAsyncServiceDestination.class);

	private DefaultSynchronousMessageSender _defaultSynchronousMessageSender =
		new DefaultSynchronousMessageSender();
	private String _destinationName;

}
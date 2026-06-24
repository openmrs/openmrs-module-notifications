/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.notifications.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.notifications.Notification;
import org.openmrs.module.notifications.api.DeliveryService;
import org.springframework.stereotype.Component;

/**
 * Default in-app delivery. The notification is already persisted by the service layer; the O3
 * Notification Center polls the REST endpoint to surface it via the bell icon. This implementation
 * therefore simply logs delivery, providing a seam for future push channels (SMS, email).
 */
@Component("notifications.InAppDeliveryService")
public class InAppDeliveryServiceImpl implements DeliveryService {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void deliver(Notification notification) {
		if (notification == null) {
			return;
		}
		if (log.isDebugEnabled()) {
			log.debug("Delivering in-app notification " + notification.getUuid() + " to recipient "
			        + (notification.getRecipient() != null ? notification.getRecipient().getUuid() : "<none>"));
		}
	}
}

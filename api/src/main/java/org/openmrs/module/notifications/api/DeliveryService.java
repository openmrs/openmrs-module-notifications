/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.notifications.api;

import org.openmrs.module.notifications.Notification;

/**
 * Delivers persisted notifications to their recipients. The default in-app channel relies on the
 * notification being stored and subsequently fetched by the O3 frontend via REST. Additional
 * channels (e.g. SMS, email) can be layered on by extending this interface.
 */
public interface DeliveryService {

	/**
	 * Delivers a notification that has already been persisted.
	 *
	 * @param notification the notification to deliver
	 */
	void deliver(Notification notification);
}

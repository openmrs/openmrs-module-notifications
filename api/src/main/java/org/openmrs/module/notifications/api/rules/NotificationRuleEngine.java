/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.notifications.api.rules;

import org.openmrs.module.notifications.Notification;
import org.springframework.stereotype.Component;

/**
 * Determines the priority of a notification and whether it should be sent. This implements the
 * notification decision tree described in O3-5751:
 *
 * <pre>
 *   Lab Result Received
 *     ├── Sample rejected?               → SEND (HIGH)
 *     ├── STAT order?                    → SEND (HIGH)
 *     ├── Critical value?                → SEND (HIGH)
 *     ├── Abnormal beyond threshold?     → SEND (MEDIUM)
 *     └── otherwise                      → SILENT_UPDATE
 * </pre>
 */
@Component("notifications.NotificationRuleEngine")
public class NotificationRuleEngine {

	/**
	 * Evaluates the given lab result context and returns a decision.
	 *
	 * @param context the inputs to evaluate; must not be null
	 * @return the resulting {@link NotificationDecision}
	 */
	public NotificationDecision evaluate(LabResultContext context) {
		if (context == null) {
			return NotificationDecision.silent();
		}

		// A rejected sample always requires clinician attention.
		if (context.isSampleRejected()) {
			return NotificationDecision.send(Notification.Priority.HIGH);
		}

		// STAT orders are urgent by definition.
		if (context.getOrderPriority() == LabResultContext.OrderPriority.STAT) {
			return NotificationDecision.send(Notification.Priority.HIGH);
		}

		// Critical lab values are urgent regardless of order priority.
		if (context.getLabValueClassification() == LabResultContext.LabValueClassification.CRITICAL) {
			return NotificationDecision.send(Notification.Priority.HIGH);
		}

		// Abnormal values beyond the configured threshold warrant a medium-priority alert.
		if (context.getLabValueClassification() == LabResultContext.LabValueClassification.ABNORMAL
		        && context.isAbnormalBeyondThreshold()) {
			return NotificationDecision.send(Notification.Priority.MEDIUM);
		}

		// Everything else is a silent chart update.
		return NotificationDecision.silent();
	}
}

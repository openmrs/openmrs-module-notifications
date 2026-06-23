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

/**
 * The result produced by the {@link NotificationRuleEngine}. It indicates whether a
 * notification should be sent to a recipient or whether the chart should be updated silently,
 * and, when a notification is warranted, the priority it should carry.
 */
public class NotificationDecision {

	public enum Action {
		SEND_NOTIFICATION, SILENT_UPDATE
	}

	private final Action action;

	private final Notification.Priority priority;

	private NotificationDecision(Action action, Notification.Priority priority) {
		this.action = action;
		this.priority = priority;
	}

	public static NotificationDecision send(Notification.Priority priority) {
		return new NotificationDecision(Action.SEND_NOTIFICATION, priority);
	}

	public static NotificationDecision silent() {
		return new NotificationDecision(Action.SILENT_UPDATE, null);
	}

	public Action getAction() {
		return action;
	}

	public Notification.Priority getPriority() {
		return priority;
	}

	public boolean shouldSend() {
		return action == Action.SEND_NOTIFICATION;
	}
}

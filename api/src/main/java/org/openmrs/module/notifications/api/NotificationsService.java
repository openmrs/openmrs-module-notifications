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

import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.notifications.Notification;
import org.openmrs.module.notifications.NotificationsConfig;

import java.util.List;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 */
public interface NotificationsService extends OpenmrsService {
	
	// ── Notification methods ──────────────────────────────────────────────────
	
	/**
	 * Creates (or updates) a notification record. Newly created notifications default to
	 * {@link Notification.Status#UNREAD}.
	 * 
	 * @param notification the notification to persist
	 * @return the saved notification
	 * @throws APIException
	 */
	@Authorized(NotificationsConfig.MODULE_PRIVILEGE)
	Notification createNotification(Notification notification) throws APIException;
	
	/**
	 * Returns a notification by its UUID.
	 * 
	 * @param uuid the notification UUID
	 * @return the matching notification, or null
	 * @throws APIException
	 */
	@Authorized()
	Notification getNotificationByUuid(String uuid) throws APIException;
	
	/**
	 * Returns all non-voided notifications for a given recipient user, optionally filtered by
	 * status.
	 * 
	 * @param recipient the recipient user (required)
	 * @param status optional status filter; if null, all statuses are returned
	 * @return list of notifications
	 * @throws APIException
	 */
	@Authorized()
	List<Notification> getNotificationsByRecipient(User recipient, Notification.Status status) throws APIException;
	
	/**
	 * Returns all non-voided notifications for a given patient.
	 * 
	 * @param patient the patient
	 * @return list of notifications
	 * @throws APIException
	 */
	@Authorized()
	List<Notification> getNotificationsByPatient(Patient patient) throws APIException;
	
	/**
	 * Returns all non-voided notifications matching the given status.
	 * 
	 * @param status the status to filter by
	 * @return list of notifications
	 * @throws APIException
	 */
	@Authorized()
	List<Notification> getNotificationsByStatus(Notification.Status status) throws APIException;
	
	/**
	 * Marks a notification as READ and stamps {@code readAt}.
	 * 
	 * @param notification the notification to mark as read
	 * @return the updated notification
	 * @throws APIException
	 */
	@Authorized(NotificationsConfig.MODULE_PRIVILEGE)
	Notification markAsRead(Notification notification) throws APIException;
	
	/**
	 * Marks a notification as REVIEWED by the given user and stamps {@code reviewedAt}.
	 * 
	 * @param notification the notification to mark as reviewed
	 * @param reviewer the user who reviewed the notification
	 * @return the updated notification
	 * @throws APIException
	 */
	@Authorized(NotificationsConfig.MODULE_PRIVILEGE)
	Notification markAsReviewed(Notification notification, User reviewer) throws APIException;
	
	/**
	 * Archives a notification (sets status to {@link Notification.Status#ARCHIVED}).
	 * 
	 * @param notification the notification to archive
	 * @return the updated notification
	 * @throws APIException
	 */
	@Authorized(NotificationsConfig.MODULE_PRIVILEGE)
	Notification archiveNotification(Notification notification) throws APIException;
	
	/**
	 * Voids a notification (soft delete).
	 * 
	 * @param notification the notification to void
	 * @param reason the reason for voiding
	 * @throws APIException
	 */
	@Authorized(NotificationsConfig.MODULE_PRIVILEGE)
	void voidNotification(Notification notification, String reason) throws APIException;
}

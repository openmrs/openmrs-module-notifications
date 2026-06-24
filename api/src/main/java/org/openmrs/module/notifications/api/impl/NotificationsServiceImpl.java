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

import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.notifications.Notification;
import org.openmrs.module.notifications.api.NotificationsService;
import org.openmrs.module.notifications.api.dao.NotificationsDao;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public class NotificationsServiceImpl extends BaseOpenmrsService implements NotificationsService {
	
	NotificationsDao dao;
	
	UserService userService;
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setDao(NotificationsDao dao) {
		this.dao = dao;
	}
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	// ── Notification methods ──────────────────────────────────────────────────
	
	@Override
	@Transactional
	public Notification createNotification(Notification notification) throws APIException {
		if (notification.getStatus() == null) {
			notification.setStatus(Notification.Status.UNREAD);
		}
		return dao.saveNotification(notification);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Notification getNotificationByUuid(String uuid) throws APIException {
		return dao.getNotificationByUuid(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Notification> getNotificationsByRecipient(User recipient, Notification.Status status) throws APIException {
		return dao.getNotificationsByRecipient(recipient, status);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Notification> getNotificationsByPatient(Patient patient) throws APIException {
		return dao.getNotificationsByPatient(patient);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Notification> getNotificationsByStatus(Notification.Status status) throws APIException {
		return dao.getNotificationsByStatus(status);
	}
	
	@Override
	@Transactional
	public Notification markAsRead(Notification notification) throws APIException {
		notification.setStatus(Notification.Status.READ);
		notification.setReadAt(new Date());
		return dao.saveNotification(notification);
	}
	
	@Override
	@Transactional
	public Notification markAsReviewed(Notification notification, User reviewer) throws APIException {
		notification.setStatus(Notification.Status.REVIEWED);
		notification.setReviewedBy(reviewer != null ? reviewer : Context.getAuthenticatedUser());
		notification.setReviewedAt(new Date());
		return dao.saveNotification(notification);
	}
	
	@Override
	@Transactional
	public Notification archiveNotification(Notification notification) throws APIException {
		notification.setStatus(Notification.Status.ARCHIVED);
		return dao.saveNotification(notification);
	}
	
	@Override
	@Transactional
	public void voidNotification(Notification notification, String reason) throws APIException {
		notification.setVoided(true);
		notification.setVoidReason(reason);
		notification.setVoidedBy(userService.getUser(1));
		dao.saveNotification(notification);
	}
}

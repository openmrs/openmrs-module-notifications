/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.notifications.api.dao;

import org.junit.Test;
import org.junit.Ignore;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.notifications.Notification;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * It is an integration test (extends BaseModuleContextSensitiveTest), which verifies DAO methods
 * against the in-memory H2 database. The database is initially loaded with data from
 * standardTestDataset.xml in openmrs-api. All test methods are executed in transactions, which are
 * rolled back by the end of each test method.
 */
public class NotificationsDaoTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	NotificationsDao dao;
	
	@Autowired
	UserService userService;
	
	@Test
	public void saveNotification_shouldPersistAndQueryByStatus() {
		//Given
		User recipient = userService.getUser(1);
		Notification notification = new Notification();
		notification.setRecipient(recipient);
		notification.setType(Notification.Type.LAB_RESULT);
		notification.setPriority(Notification.Priority.HIGH);
		notification.setMessage("Critical potassium value detected");
		notification.setStatus(Notification.Status.UNREAD);
		
		//When
		dao.saveNotification(notification);
		Context.flushSession();
		Context.clearSession();
		
		//Then
		Notification saved = dao.getNotificationByUuid(notification.getUuid());
		assertNotNull(saved);
		assertThat(saved, hasProperty("message", is("Critical potassium value detected")));
		assertThat(saved, hasProperty("priority", is(Notification.Priority.HIGH)));
		assertThat(saved, hasProperty("status", is(Notification.Status.UNREAD)));
		
	}
}

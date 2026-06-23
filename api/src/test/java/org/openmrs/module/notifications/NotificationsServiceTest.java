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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.module.notifications.Item;
import org.openmrs.module.notifications.Notification;
import org.openmrs.module.notifications.api.dao.NotificationsDao;
import org.openmrs.module.notifications.api.impl.NotificationsServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationsServiceImpl. Does not use an in-memory DB or Spring context.
 */
public class NotificationsServiceTest {

	@InjectMocks
	NotificationsServiceImpl service;

	@Mock
	NotificationsDao dao;

	@Mock
	UserService userService;

	@Before
	public void setupMocks() {
		MockitoAnnotations.initMocks(this);
	}

	// ── Item tests ────────────────────────────────────────────────────────────

	@Test
	public void saveItem_shouldSetOwnerIfNotSet() {
		Item item = new Item();
		item.setDescription("some description");
		when(dao.saveItem(item)).thenReturn(item);
		User user = new User();
		when(userService.getUser(1)).thenReturn(user);

		service.saveItem(item);

		assertThat(item, hasProperty("owner", is(user)));
	}

	@Test
	public void saveItem_shouldNotOverwriteExistingOwner() {
		User owner = new User(2);
		Item item = new Item();
		item.setOwner(owner);
		when(dao.saveItem(item)).thenReturn(item);

		service.saveItem(item);

		assertThat(item.getOwner(), is(owner));
		verify(userService, never()).getUser(anyInt());
	}

	// ── Notification tests ────────────────────────────────────────────────────

	@Test
	public void createNotification_shouldDefaultStatusToUnreadAndDelegateToDao() {
		Notification notification = buildNotification("Critical potassium value detected");
		notification.setStatus(null);
		when(dao.saveNotification(notification)).thenReturn(notification);

		Notification result = service.createNotification(notification);

		assertThat(result.getStatus(), is(Notification.Status.UNREAD));
		verify(dao).saveNotification(notification);
	}

	@Test
	public void getNotificationByUuid_shouldReturnNotificationFromDao() {
		String uuid = UUID.randomUUID().toString();
		Notification notification = buildNotification("Alert");
		notification.setUuid(uuid);
		when(dao.getNotificationByUuid(uuid)).thenReturn(notification);

		Notification result = service.getNotificationByUuid(uuid);

		assertThat(result, is(notification));
		assertThat(result.getUuid(), is(uuid));
	}

	@Test
	public void getNotificationByUuid_shouldReturnNullForUnknownUuid() {
		when(dao.getNotificationByUuid("unknown")).thenReturn(null);

		assertNull(service.getNotificationByUuid("unknown"));
	}

	@Test
	public void getNotificationsByPatient_shouldReturnListFromDao() {
		Patient patient = new Patient(1);
		List<Notification> list = Arrays.asList(buildNotification("A"), buildNotification("B"));
		when(dao.getNotificationsByPatient(patient)).thenReturn(list);

		List<Notification> result = service.getNotificationsByPatient(patient);

		assertThat(result, hasSize(2));
		verify(dao).getNotificationsByPatient(patient);
	}

	@Test
	public void getNotificationsByRecipient_shouldDelegateWithStatus() {
		User user = new User(3);
		List<Notification> list = Arrays.asList(buildNotification("C"));
		when(dao.getNotificationsByRecipient(user, Notification.Status.UNREAD)).thenReturn(list);

		List<Notification> result = service.getNotificationsByRecipient(user, Notification.Status.UNREAD);

		assertThat(result, hasSize(1));
		verify(dao).getNotificationsByRecipient(user, Notification.Status.UNREAD);
	}

	@Test
	public void getNotificationsByStatus_shouldDelegateToDao() {
		List<Notification> list = Arrays.asList(buildNotification("D"));
		when(dao.getNotificationsByStatus(Notification.Status.UNREAD)).thenReturn(list);

		List<Notification> result = service.getNotificationsByStatus(Notification.Status.UNREAD);

		assertThat(result, hasSize(1));
		verify(dao).getNotificationsByStatus(Notification.Status.UNREAD);
	}

	@Test
	public void markAsRead_shouldSetStatusAndReadAt() {
		Notification notification = buildNotification("Unread alert");
		when(dao.saveNotification(notification)).thenReturn(notification);

		Notification result = service.markAsRead(notification);

		assertThat(result.getStatus(), is(Notification.Status.READ));
		assertNotNull(result.getReadAt());
		verify(dao).saveNotification(notification);
	}

	@Test
	public void markAsReviewed_shouldSetStatusReviewerAndReviewedAt() {
		Notification notification = buildNotification("Review me");
		User reviewer = new User(5);
		when(dao.saveNotification(notification)).thenReturn(notification);

		Notification result = service.markAsReviewed(notification, reviewer);

		assertThat(result.getStatus(), is(Notification.Status.REVIEWED));
		assertThat(result.getReviewedBy(), is(reviewer));
		assertNotNull(result.getReviewedAt());
		verify(dao).saveNotification(notification);
	}

	@Test
	public void archiveNotification_shouldSetStatusToArchived() {
		Notification notification = buildNotification("Archive me");
		when(dao.saveNotification(notification)).thenReturn(notification);

		Notification result = service.archiveNotification(notification);

		assertThat(result.getStatus(), is(Notification.Status.ARCHIVED));
		verify(dao).saveNotification(notification);
	}

	@Test
	public void voidNotification_shouldSetVoidedTrueAndSave() {
		Notification notification = buildNotification("Void me");
		User superuser = new User(1);
		when(userService.getUser(1)).thenReturn(superuser);
		when(dao.saveNotification(notification)).thenReturn(notification);

		service.voidNotification(notification, "test void");

		assertTrue(notification.getVoided());
		assertThat(notification.getVoidReason(), is("test void"));
		assertThat(notification.getVoidedBy(), is(superuser));
		verify(dao).saveNotification(notification);
	}

	@Test
	public void newNotification_shouldDefaultToGeneralTypeLowPriorityUnreadStatus() {
		Notification notification = new Notification();

		assertThat(notification.getType(), is(Notification.Type.GENERAL));
		assertThat(notification.getPriority(), is(Notification.Priority.LOW));
		assertThat(notification.getStatus(), is(Notification.Status.UNREAD));
	}

	// ── helpers ───────────────────────────────────────────────────────────────

	private Notification buildNotification(String message) {
		Notification n = new Notification();
		n.setMessage(message);
		n.setType(Notification.Type.LAB_RESULT);
		n.setPriority(Notification.Priority.HIGH);
		n.setStatus(Notification.Status.UNREAD);
		return n;
	}
}

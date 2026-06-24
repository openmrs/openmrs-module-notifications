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

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.notifications.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("notifications.NotificationsDao")
public class NotificationsDao {
	
	@Autowired
	DbSessionFactory sessionFactory;
	
	private DbSession getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	// ── Notification methods ──────────────────────────────────────────────────
	
	public Notification saveNotification(Notification notification) {
		getSession().saveOrUpdate(notification);
		return notification;
	}
	
	public Notification getNotificationByUuid(String uuid) {
		return (Notification) getSession().createCriteria(Notification.class).add(Restrictions.eq("uuid", uuid))
		        .add(Restrictions.eq("voided", false)).uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<Notification> getNotificationsByRecipient(User recipient, Notification.Status status) {
		Criteria criteria = getSession().createCriteria(Notification.class).add(Restrictions.eq("recipient", recipient))
		        .add(Restrictions.eq("voided", false));
		if (status != null) {
			criteria.add(Restrictions.eq("status", status));
		}
		return criteria.addOrder(Order.desc("dateCreated")).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<Notification> getNotificationsByPatient(Patient patient) {
		return getSession().createCriteria(Notification.class).add(Restrictions.eq("patient", patient))
		        .add(Restrictions.eq("voided", false)).addOrder(Order.desc("dateCreated")).list();
	}
	
	@SuppressWarnings("unchecked")
	public List<Notification> getNotificationsByStatus(Notification.Status status) {
		return getSession().createCriteria(Notification.class).add(Restrictions.eq("status", status))
		        .add(Restrictions.eq("voided", false)).addOrder(Order.desc("dateCreated")).list();
	}
}

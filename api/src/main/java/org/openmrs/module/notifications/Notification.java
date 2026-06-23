/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.notifications;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Patient;
import org.openmrs.User;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Represents a notification raised by the system (e.g. when a lab result becomes available)
 * and routed to a recipient user about a given patient. Maps to the
 * {@code notifications_notification} database table.
 *
 * @see org.openmrs.module.notifications.api.NotificationsService
 */
@Entity(name = "notifications.Notification")
@Table(name = "notifications_notification")
public class Notification extends BaseOpenmrsData {

	/**
	 * The category of clinical event that triggered the notification.
	 */
	public enum Type {
		LAB_RESULT, PRESCRIPTION, SAMPLE_REJECTED, GENERAL
	}

	/**
	 * The urgency of the notification, derived by the rules engine.
	 */
	public enum Priority {
		LOW, MEDIUM, HIGH
	}

	/**
	 * The lifecycle state of the notification.
	 */
	public enum Status {
		UNREAD, READ, REVIEWED, ARCHIVED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@ManyToOne
	@JoinColumn(name = "recipient_user_id")
	private User recipient;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", length = 50, nullable = false)
	private Type type = Type.GENERAL;

	@Enumerated(EnumType.STRING)
	@Column(name = "priority", length = 20, nullable = false)
	private Priority priority = Priority.LOW;

	@Basic
	@Column(name = "message", length = 2000, nullable = false)
	private String message;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20, nullable = false)
	private Status status = Status.UNREAD;

	@Basic
	@Column(name = "read_at")
	private Date readAt;

	@ManyToOne
	@JoinColumn(name = "reviewed_by")
	private User reviewedBy;

	@Basic
	@Column(name = "reviewed_at")
	private Date reviewedAt;

	@Basic
	@Column(name = "metadata", length = 4000)
	private String metadata;

	public Notification() {
	}

	public Notification(Patient patient, User recipient, Type type, Priority priority, String message) {
		this.patient = patient;
		this.recipient = recipient;
		this.type = type;
		this.priority = priority;
		this.message = message;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public User getRecipient() {
		return recipient;
	}

	public void setRecipient(User recipient) {
		this.recipient = recipient;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Date getReadAt() {
		return readAt;
	}

	public void setReadAt(Date readAt) {
		this.readAt = readAt;
	}

	public User getReviewedBy() {
		return reviewedBy;
	}

	public void setReviewedBy(User reviewedBy) {
		this.reviewedBy = reviewedBy;
	}

	public Date getReviewedAt() {
		return reviewedAt;
	}

	public void setReviewedAt(Date reviewedAt) {
		this.reviewedAt = reviewedAt;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
}

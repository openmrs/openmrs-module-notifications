/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.notifications.web.resource;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.notifications.Notification;
import org.openmrs.module.notifications.api.NotificationsService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.List;

/**
 * REST resource for {@link Notification}. Exposed at {@code /ws/rest/v1/notification}.
 * <p>
 * Supports:
 * </p>
 * <ul>
 * <li>{@code POST /ws/rest/v1/notification} — create a notification</li>
 * <li>{@code GET  /ws/rest/v1/notification?status=UNREAD} — fetch by status</li>
 * <li>{@code GET  /ws/rest/v1/notification?recipient=<uuid>} — fetch by recipient</li>
 * <li>{@code POST /ws/rest/v1/notification/ uuid} with {@code status} — mark read/reviewed/archived
 * </li>
 * </ul>
 */
@Resource(name = RestConstants.VERSION_1 + "/notification", supportedClass = Notification.class, supportedOpenmrsVersions = { "2.0 - 9.*" })
public class NotificationResource extends DataDelegatingCrudResource<Notification> {
	
	private NotificationsService getService() {
		return Context.getService(NotificationsService.class);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("uuid");
		description.addProperty("display");
		description.addSelfLink();
		if (rep instanceof DefaultRepresentation) {
			description.addProperty("type");
			description.addProperty("priority");
			description.addProperty("message");
			description.addProperty("status");
			description.addProperty("patient", Representation.REF);
			description.addProperty("recipient", Representation.REF);
			description.addProperty("readAt");
			description.addProperty("reviewedAt");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
		} else if (rep instanceof FullRepresentation) {
			description.addProperty("type");
			description.addProperty("priority");
			description.addProperty("message");
			description.addProperty("status");
			description.addProperty("patient", Representation.DEFAULT);
			description.addProperty("recipient", Representation.DEFAULT);
			description.addProperty("readAt");
			description.addProperty("reviewedBy", Representation.REF);
			description.addProperty("reviewedAt");
			description.addProperty("metadata");
			description.addProperty("voided");
			description.addProperty("auditInfo");
		}
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("patient");
		description.addProperty("recipient");
		description.addProperty("type");
		description.addProperty("priority");
		description.addRequiredProperty("message");
		description.addProperty("status");
		description.addProperty("metadata");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("status");
		description.addProperty("priority");
		description.addProperty("message");
		description.addProperty("metadata");
		return description;
	}
	
	/**
	 * Allows clients to transition a notification's lifecycle by setting {@code status}. Setting
	 * READ, REVIEWED, or ARCHIVED routes through the corresponding service action so audit fields
	 * (readAt/reviewedAt/reviewedBy) are populated.
	 */
	@PropertySetter("status")
	public void setStatus(Notification notification, Object value) {
		Notification.Status status = Notification.Status.valueOf(value.toString().toUpperCase());
		NotificationsService service = getService();
		switch (status) {
			case READ:
				service.markAsRead(notification);
				break;
			case REVIEWED:
				service.markAsReviewed(notification, Context.getAuthenticatedUser());
				break;
			case ARCHIVED:
				service.archiveNotification(notification);
				break;
			default:
				notification.setStatus(status);
		}
	}
	
	public String getDisplayString(Notification notification) {
		return notification.getMessage() + " [" + notification.getType() + "/" + notification.getPriority() + "]";
	}
	
	@Override
	public Notification newDelegate() {
		return new Notification();
	}
	
	@Override
	public Notification save(Notification notification) {
		return getService().createNotification(notification);
	}
	
	@Override
	public Notification getByUniqueId(String uuid) {
		return getService().getNotificationByUuid(uuid);
	}
	
	@Override
	protected void delete(Notification notification, String reason, RequestContext context) throws ResponseException {
		getService().voidNotification(notification, reason);
	}
	
	@Override
	public void purge(Notification notification, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("Purge is not supported for notifications.");
	}
	
	/**
	 * Supports filtering by {@code status} and/or {@code recipient} (user uuid).
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String statusParam = context.getParameter("status");
		String recipientUuid = context.getParameter("recipient");
		
		Notification.Status status = statusParam != null ? Notification.Status.valueOf(statusParam.toUpperCase()) : null;
		
		List<Notification> notifications;
		if (recipientUuid != null) {
			User recipient = Context.getUserService().getUserByUuid(recipientUuid);
			notifications = getService().getNotificationsByRecipient(recipient, status);
		} else if (status != null) {
			notifications = getService().getNotificationsByStatus(status);
		} else {
			notifications = getService().getNotificationsByRecipient(Context.getAuthenticatedUser(), null);
		}
		return new NeedsPaging<Notification>(notifications, context);
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.notifications.api.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.event.EventListener;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.notifications.Notification;
import org.openmrs.module.notifications.api.DeliveryService;
import org.openmrs.module.notifications.api.NotificationsService;
import org.openmrs.module.notifications.api.RecipientResolver;
import org.openmrs.module.notifications.api.rules.LabResultContext;
import org.openmrs.module.notifications.api.rules.NotificationDecision;
import org.openmrs.module.notifications.api.rules.NotificationRuleEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.MapMessage;
import javax.jms.Message;
import java.util.Set;

/**
 * Listens for lab-result {@link Obs} events fired by the OpenMRS Event module. When an Obs is
 * created or updated, the listener reconstructs a {@link LabResultContext}, asks the
 * {@link NotificationRuleEngine} whether to notify, and, if so, persists and delivers a
 * notification to the resolved recipients.
 * <p>
 * The handling runs inside a {@link Daemon} thread because event callbacks execute outside of an
 * authenticated OpenMRS context and Hibernate session.
 * </p>
 */
@Component("notifications.LabResultEventListener")
public class LabResultEventListener implements EventListener {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	private final NotificationRuleEngine ruleEngine;
	
	private final RecipientResolver recipientResolver;
	
	private DaemonToken daemonToken;
	
	@Autowired
	public LabResultEventListener(NotificationRuleEngine ruleEngine, RecipientResolver recipientResolver) {
		this.ruleEngine = ruleEngine;
		this.recipientResolver = recipientResolver;
	}
	
	public void setDaemonToken(DaemonToken daemonToken) {
		this.daemonToken = daemonToken;
	}
	
	@Override
	public void onMessage(final Message message) {
		try {
			final String uuid = ((MapMessage) message).getString("uuid");
			if (daemonToken != null) {
				Daemon.runInDaemonThread(new Runnable() {
					
					@Override
					public void run() {
						handle(uuid);
					}
				}, daemonToken);
			} else {
				handle(uuid);
			}
		}
		catch (Exception e) {
			log.error("Failed to process lab result event", e);
		}
	}
	
	/**
	 * Processes a single Obs by uuid: classifies it, evaluates the rules, and creates a
	 * notification when warranted. Package-visible for unit testing.
	 * 
	 * @param obsUuid the uuid of the Obs carried by the event message
	 */
	void handle(String obsUuid) {
		Obs obs = Context.getObsService().getObsByUuid(obsUuid);
		if (obs == null) {
			return;
		}
		
		LabResultContext context = buildContext(obs);
		NotificationDecision decision = ruleEngine.evaluate(context);
		if (!decision.shouldSend()) {
			return;
		}
		
		Patient patient = obs.getPerson() != null && obs.getPerson().isPatient() ? (Patient) obs.getPerson() : null;
		Order order = obs.getOrder();
		Set<User> recipients = order != null ? recipientResolver.resolveForOrder(order) : recipientResolver
		        .resolveForPatient(patient);
		
		NotificationsService service = Context.getService(NotificationsService.class);
		DeliveryService deliveryService = Context.getRegisteredComponent("notifications.InAppDeliveryService",
		    DeliveryService.class);
		
		for (User recipient : recipients) {
			Notification notification = new Notification(patient, recipient, Notification.Type.LAB_RESULT,
			        decision.getPriority(), buildMessage(obs));
			notification.setMetadata("{\"obsUuid\":\"" + obsUuid + "\"}");
			Notification saved = service.createNotification(notification);
			deliveryService.deliver(saved);
		}
	}
	
	private LabResultContext buildContext(Obs obs) {
		LabResultContext.OrderPriority orderPriority = LabResultContext.OrderPriority.ROUTINE;
		if (obs.getOrder() != null && obs.getOrder().getUrgency() == Order.Urgency.STAT) {
			orderPriority = LabResultContext.OrderPriority.STAT;
		}
		// Classification of the actual value is delegated to concept ranges; default to NORMAL here.
		return LabResultContext.builder().orderPriority(orderPriority).labValueClassification(classify(obs))
		        .abnormalBeyondThreshold(false).sampleRejected(false).build();
	}
	
	private LabResultContext.LabValueClassification classify(Obs obs) {
		if (obs.getConcept() == null || obs.getValueNumeric() == null) {
			return LabResultContext.LabValueClassification.NORMAL;
		}
		// Concept numeric ranges (hi/low critical and normal) drive classification when present.
		Double value = obs.getValueNumeric();
		try {
			org.openmrs.ConceptNumeric cn = Context.getConceptService().getConceptNumeric(obs.getConcept().getId());
			if (cn != null) {
				if ((cn.getHiCritical() != null && value >= cn.getHiCritical())
				        || (cn.getLowCritical() != null && value <= cn.getLowCritical())) {
					return LabResultContext.LabValueClassification.CRITICAL;
				}
				if ((cn.getHiNormal() != null && value > cn.getHiNormal())
				        || (cn.getLowNormal() != null && value < cn.getLowNormal())) {
					return LabResultContext.LabValueClassification.ABNORMAL;
				}
			}
		}
		catch (Exception e) {
			log.warn("Could not classify obs value for concept " + obs.getConcept().getId(), e);
		}
		return LabResultContext.LabValueClassification.NORMAL;
	}
	
	private String buildMessage(Obs obs) {
		String conceptName = obs.getConcept() != null && obs.getConcept().getName() != null ? obs.getConcept().getName()
		        .getName() : "Lab";
		return "New " + conceptName + " result is available.";
	}
}

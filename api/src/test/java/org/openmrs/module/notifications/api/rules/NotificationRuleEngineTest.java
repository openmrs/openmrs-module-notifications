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

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.notifications.Notification;
import org.openmrs.module.notifications.api.rules.LabResultContext.LabValueClassification;
import org.openmrs.module.notifications.api.rules.LabResultContext.OrderPriority;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the {@link NotificationRuleEngine} decision tree described in O3-5751.
 */
public class NotificationRuleEngineTest {
	
	private NotificationRuleEngine engine;
	
	@Before
	public void setup() {
		engine = new NotificationRuleEngine();
	}
	
	@Test
	public void evaluate_shouldSendHighWhenSampleRejected() {
		LabResultContext context = LabResultContext.builder().sampleRejected(true).build();
		
		NotificationDecision decision = engine.evaluate(context);
		
		assertThat(decision.shouldSend(), is(true));
		assertThat(decision.getPriority(), is(Notification.Priority.HIGH));
	}
	
	@Test
	public void evaluate_shouldSendHighForStatOrder() {
		LabResultContext context = LabResultContext.builder().orderPriority(OrderPriority.STAT)
		        .labValueClassification(LabValueClassification.NORMAL).build();
		
		NotificationDecision decision = engine.evaluate(context);
		
		assertThat(decision.shouldSend(), is(true));
		assertThat(decision.getPriority(), is(Notification.Priority.HIGH));
	}
	
	@Test
	public void evaluate_shouldSendHighForCriticalValue() {
		LabResultContext context = LabResultContext.builder().orderPriority(OrderPriority.ROUTINE)
		        .labValueClassification(LabValueClassification.CRITICAL).build();
		
		NotificationDecision decision = engine.evaluate(context);
		
		assertThat(decision.shouldSend(), is(true));
		assertThat(decision.getPriority(), is(Notification.Priority.HIGH));
	}
	
	@Test
	public void evaluate_shouldSendMediumForAbnormalBeyondThreshold() {
		LabResultContext context = LabResultContext.builder().orderPriority(OrderPriority.ROUTINE)
		        .labValueClassification(LabValueClassification.ABNORMAL).abnormalBeyondThreshold(true).build();
		
		NotificationDecision decision = engine.evaluate(context);
		
		assertThat(decision.shouldSend(), is(true));
		assertThat(decision.getPriority(), is(Notification.Priority.MEDIUM));
	}
	
	@Test
	public void evaluate_shouldSilentUpdateForAbnormalWithinThreshold() {
		LabResultContext context = LabResultContext.builder().orderPriority(OrderPriority.ROUTINE)
		        .labValueClassification(LabValueClassification.ABNORMAL).abnormalBeyondThreshold(false).build();
		
		NotificationDecision decision = engine.evaluate(context);
		
		assertThat(decision.shouldSend(), is(false));
		assertThat(decision.getAction(), is(NotificationDecision.Action.SILENT_UPDATE));
	}
	
	@Test
	public void evaluate_shouldSilentUpdateForRoutineNormalResult() {
		LabResultContext context = LabResultContext.builder().orderPriority(OrderPriority.ROUTINE)
		        .labValueClassification(LabValueClassification.NORMAL).build();
		
		NotificationDecision decision = engine.evaluate(context);
		
		assertThat(decision.shouldSend(), is(false));
	}
	
	@Test
	public void evaluate_shouldSilentUpdateForNullContext() {
		NotificationDecision decision = engine.evaluate(null);
		
		assertThat(decision.shouldSend(), is(false));
	}
}

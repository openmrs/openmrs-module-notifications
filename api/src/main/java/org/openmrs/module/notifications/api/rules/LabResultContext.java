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

/**
 * Immutable set of inputs the {@link NotificationRuleEngine} evaluates to decide whether and how to
 * notify a clinician about a lab result. Build instances with {@link Builder}.
 */
public class LabResultContext {
	
	public enum OrderPriority {
		STAT,
		ROUTINE
	}
	
	public enum LabValueClassification {
		CRITICAL,
		ABNORMAL,
		NORMAL
	}
	
	private final OrderPriority orderPriority;
	
	private final LabValueClassification labValueClassification;
	
	private final boolean abnormalBeyondThreshold;
	
	private final boolean sampleRejected;
	
	private LabResultContext(Builder builder) {
		this.orderPriority = builder.orderPriority;
		this.labValueClassification = builder.labValueClassification;
		this.abnormalBeyondThreshold = builder.abnormalBeyondThreshold;
		this.sampleRejected = builder.sampleRejected;
	}
	
	public OrderPriority getOrderPriority() {
		return orderPriority;
	}
	
	public LabValueClassification getLabValueClassification() {
		return labValueClassification;
	}
	
	public boolean isAbnormalBeyondThreshold() {
		return abnormalBeyondThreshold;
	}
	
	public boolean isSampleRejected() {
		return sampleRejected;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private OrderPriority orderPriority = OrderPriority.ROUTINE;
		
		private LabValueClassification labValueClassification = LabValueClassification.NORMAL;
		
		private boolean abnormalBeyondThreshold = false;
		
		private boolean sampleRejected = false;
		
		public Builder orderPriority(OrderPriority orderPriority) {
			this.orderPriority = orderPriority;
			return this;
		}
		
		public Builder labValueClassification(LabValueClassification labValueClassification) {
			this.labValueClassification = labValueClassification;
			return this;
		}
		
		public Builder abnormalBeyondThreshold(boolean abnormalBeyondThreshold) {
			this.abnormalBeyondThreshold = abnormalBeyondThreshold;
			return this;
		}
		
		public Builder sampleRejected(boolean sampleRejected) {
			this.sampleRejected = sampleRejected;
			return this;
		}
		
		public LabResultContext build() {
			return new LabResultContext(this);
		}
	}
}

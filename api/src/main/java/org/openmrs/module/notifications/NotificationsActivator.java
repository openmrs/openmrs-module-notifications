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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.event.Event;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.notifications.api.listener.LabResultEventListener;

/**
 * This class contains the logic that is run every time this module is either started or shutdown.
 * It receives a {@link DaemonToken} so that the lab result event listener can run privileged
 * handling in a daemon thread, and it subscribes/unsubscribes that listener to Obs events.
 */
public class NotificationsActivator extends BaseModuleActivator implements DaemonTokenAware {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private DaemonToken daemonToken;
	
	@Override
	public void setDaemonToken(DaemonToken daemonToken) {
		this.daemonToken = daemonToken;
	}
	
	/**
	 * @see #started()
	 */
	@Override
	public void started() {
		LabResultEventListener listener = getListener();
		if (listener != null) {
			listener.setDaemonToken(daemonToken);
			Event.subscribe(Obs.class, Event.Action.CREATED.name(), listener);
			Event.subscribe(Obs.class, Event.Action.UPDATED.name(), listener);
			log.info("Subscribed LabResultEventListener to Obs CREATED/UPDATED events");
		}
		log.info("Started Notifications");
	}
	
	/**
	 * @see #shutdown()
	 */
	public void shutdown() {
		LabResultEventListener listener = getListener();
		if (listener != null) {
			Event.unsubscribe(Obs.class, Event.Action.CREATED, listener);
			Event.unsubscribe(Obs.class, Event.Action.UPDATED, listener);
		}
		log.info("Shutdown Notifications");
	}
	
	private LabResultEventListener getListener() {
		try {
			return Context.getRegisteredComponent("notifications.LabResultEventListener", LabResultEventListener.class);
		}
		catch (Exception e) {
			log.warn("LabResultEventListener component not available", e);
			return null;
		}
	}
}

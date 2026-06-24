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

import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Resolves the set of {@link User}s who should receive a notification for a given clinical event.
 * The default strategy routes notifications to the clinician who placed the order.
 */
@Component("notifications.RecipientResolver")
public class RecipientResolver {
	
	/**
	 * Resolves recipients for a notification triggered by the given order. Falls back to an empty
	 * set if no recipient can be determined.
	 * 
	 * @param order the order that triggered the notification (may be null)
	 * @return an ordered set of recipient users (never null)
	 */
	public Set<User> resolveForOrder(Order order) {
		if (order == null || order.getOrderer() == null) {
			return Collections.emptySet();
		}
		Set<User> recipients = new LinkedHashSet<User>();
		User ordererUser = getUserForProvider(order.getOrderer());
		if (ordererUser != null) {
			recipients.add(ordererUser);
		}
		return recipients;
	}
	
	/**
	 * Resolves recipients for a notification about a given patient when no order is available.
	 * 
	 * @param patient the patient (may be null)
	 * @return an ordered set of recipient users (never null)
	 */
	public Set<User> resolveForPatient(Patient patient) {
		// Without an order to trace the ordering clinician, there is no default recipient.
		// Implementations may override this to route to a care team or location-based role.
		return Collections.emptySet();
	}
	
	private User getUserForProvider(Provider provider) {
		if (provider == null || provider.getPerson() == null) {
			return null;
		}
		for (User user : Context.getUserService().getUsersByPerson(provider.getPerson(), false)) {
			return user;
		}
		return null;
	}
}

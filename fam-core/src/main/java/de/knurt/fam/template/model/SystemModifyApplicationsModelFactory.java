/*
 * Copyright 2009-2012 by KNURT Systeme (http://www.knurt.de)
 *
 * Licensed under the Creative Commons License Attribution-NonCommercial-ShareAlike 3.0 Unported;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://creativecommons.org/licenses/by-nc-sa/3.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.knurt.fam.template.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.util.booking.ApplicationConflicts;

/**
 * controller for view and cancel bookings.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090810 (08/10/2009)
 */
public class SystemModifyApplicationsModelFactory {
	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		List<Booking> bookings = new ArrayList<Booking>();
		if (templateResource.getAuthUser() != null && templateResource.getAuthUser().hasAdminTasks()) {
			List<Booking> candidates;
			if (templateResource.getAuthUser().isAdmin()) {
				candidates = FamDaoProxy.bookingDao().getAll();
			} else {
				List<Facility> facilities = FamDaoProxy.facilityDao().getBookableFacilitiesUserIsResponsibleFor(templateResource.getAuthUser());
				candidates = FamDaoProxy.bookingDao().getAll(facilities);
			}
			for (Booking candidate : candidates) {
				if (!candidate.isCanceled() && !candidate.sessionAlreadyMade() && candidate.isApplication()) {
					bookings.add(candidate);
				}
			}
		}
		result.put("has_conficts", this.hasConflicts(bookings));
		result.put("bookings", bookings);
		return result;
	}

	private boolean hasConflicts(List<Booking> bookings) {
		boolean result = false;
		List<FacilityBookable> toCheckFacilities = new ArrayList<FacilityBookable>();
		for (Booking booking : bookings) {
			if (!toCheckFacilities.contains(booking.getFacility()) && booking.isTimeBased()) {
				toCheckFacilities.add(booking.getFacility());
			}
		}
		for (FacilityBookable toCheckFacility : toCheckFacilities) {
			if (ApplicationConflicts.getInstance().isOverapplied(toCheckFacility)) {
				result = true;
				break;
			}
		}
		return result;
	}
}

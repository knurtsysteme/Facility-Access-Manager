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
package de.knurt.fam.template.controller.image.availability;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.knurt.fam.core.config.FamCalendarConfiguration;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * factory to produce the availabilities of facilities for images. calendar views
 * contains images using facilityavailabilities produced here.
 * 
 * @see FacilityAvailability
 * @author Daniel Oltmanns
 * @since 0.20090702 (07/02/2009)
 */
public class FacilityAvailability4ImagesGetter {

	private TimeFrame day2show;
	private TimeBookingRequest br;
	private Integer requestedCapacityUnits;

	/**
	 * construct a image getter.
	 * 
	 * @param br
	 *            a booking request for a facility that must be booked with a time
	 *            rule.
	 * @param requestedCapacityUnits
	 */
	public FacilityAvailability4ImagesGetter(TimeBookingRequest br, Integer requestedCapacityUnits) {
		this.day2show = SimpleTimeFrame.getDay(br.getRequestedTimeFrame().getCalendarStart());
		this.br = br;
		this.requestedCapacityUnits = requestedCapacityUnits;
	}

	private List<FacilityAvailability> getAvailabilitiesWithoutBookingSituation() {
		return FamDaoProxy.facilityDao().getFacilityAvailabilitiesMergedByFacilities(this.day2show, this.br.getFacility().getKey());
	}

	/**
	 * return a list of {@link FacilityAvailability}s and add dynamicly the
	 * booking situtation. the list contains all not availabilities (not
	 * available in general, sudden failures and maintenances) and creates and
	 * adds new facility availabilities, if there are bookings or applications
	 * already. all completely availables are not shown here.
	 * 
	 * if requestedCapacityUnits is <code>null</code>, show orange on partly
	 * availables. if a positive Integer, assume user wants this capacity units.
	 * decide between red and green then (on 5 availables, requested 4, 2 are
	 * booked = red etc.).
	 * 
	 * @return a list of {@link FacilityAvailability}s and add dynamicly the
	 *         booking situtation.
	 */
	public List<FacilityAvailability> getAvailabilitiesWithBookingSituation() {
		List<FacilityAvailability> result = new ArrayList<FacilityAvailability>();
		if (this.br.getFacility().isUnknown()) {
			result.addAll(this.getAvailabilitiesWithoutBookingSituation());
		} else {
			int minuteSteps = FamCalendarConfiguration.smallestMinuteStep();
			Calendar start = this.day2show.getCalendarStart();
			Calendar end = this.day2show.getCalendarStart();
			end.add(Calendar.MINUTE, minuteSteps);
			TimeFrame pointer = new SimpleTimeFrame(start, end);
			// watch booking situation
			List<Booking> bookingsOfEntireDay = FamDaoProxy.bookingDao().getUncanceledBookingsAndApplicationsIn(this.br.getFacility(), this.day2show);
			int counterMaybeAvailable = 0;
			int counterNotAvailable = 0;
			String facilityKey = this.br.getFacility().getKey();
			int capacityUnitsOfFacility = this.br.getFacility().getCapacityUnits();
			List<FacilityAvailability> availabilitiesWithoutBookingSituation = this.getAvailabilitiesWithoutBookingSituation();
			while (this.isRelevant(pointer)) {
				boolean generalRulePresent = false;
				for (FacilityAvailability da : availabilitiesWithoutBookingSituation) {
					if (!da.isCompletelyAvailable() && da.overlaps(pointer)) {
						generalRulePresent = true;
					}
				}
				if (!generalRulePresent) {
					FacilityAvailability step = new FacilityAvailability(facilityKey, pointer);
					// count statuses
					counterMaybeAvailable = 0;
					counterNotAvailable = 0;
					for (Booking b : bookingsOfEntireDay) {
						if (b.overlaps(pointer)) {
							if (b.isApplication()) {
								counterMaybeAvailable += b.getCapacityUnits();
							} else if (b.isBooked()) {
								counterNotAvailable += b.getCapacityUnits();
							}
						}
					}
					if (this.requestedCapacityUnits == null || this.requestedCapacityUnits.intValue() <= 0) {
						if (counterNotAvailable >= capacityUnitsOfFacility) {
							// ↖ there is a not available and it shall be
							// ↖ colored with one time or it is booked up
							step.setNotAvailableBecauseOfBooking();
						} else if (counterMaybeAvailable > 0 || counterNotAvailable > 0) {
							// ↖ there is an application or it is not booked up
							// ↖ completely
							step.setMaybeAvailable();
						}
					} else {
						if (capacityUnitsOfFacility - counterNotAvailable < this.requestedCapacityUnits.intValue()) {
							step.setNotAvailableBecauseOfBooking();
						}
					}
					if (step.isUnset() == false) {
						result.add(step);
					}
				}
				pointer.add(Calendar.MINUTE, minuteSteps);
			}
			result.addAll(availabilitiesWithoutBookingSituation);
		}
		return result;
	}

	private boolean isRelevant(TimeFrame pointer) {
		return this.isRelevant(pointer.getCalendarStart());
	}

	private boolean isRelevant(Calendar pointer) {
		return pointer.before(this.day2show.getCalendarEnd());
	}
}

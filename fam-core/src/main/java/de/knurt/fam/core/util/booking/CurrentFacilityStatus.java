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
package de.knurt.fam.core.util.booking;

import java.util.Calendar;
import java.util.List;

import de.knurt.fam.core.model.config.BookingStrategy;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.persistence.dao.FacilityDao;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.util.time.FacilityAvailabilityMerger;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * class to answer questions like
 * "what is the status of this facility <strong>now</strong>". this is mainly used
 * to resolve emergencies on a facility.
 * 
 * @see FacilityDao
 * @see FacilityAvailability
 * @author Daniel Oltmanns
 * @since 0.20090811
 */
public class CurrentFacilityStatus {

	private Facility facility;

	/**
	 * construct the current status for the given facility. current means the status
	 * of a facility <strong>now</strong>
	 * 
	 * @param facility
	 *            the current status for the given facility.
	 */
	public CurrentFacilityStatus(Facility facility) {
		this.facility = facility;
	}

	/**
	 * return true, if a {@link FacilityAvailability} is set <strong>for
	 * now</strong>.
	 * 
	 * @return true, if a {@link FacilityAvailability} is set <strong>for
	 *         now</strong>.
	 */
	public FacilityAvailability getFacilityAvailability() {
		// prepare result
		FacilityAvailability result = new FacilityAvailability();
		result.setAvailable(FacilityAvailability.COMPLETE_AVAILABLE); // thinking
		// positive

		// prepare vars
		TimeFrame today = SimpleTimeFrame.getToday();
		TimeFrame now = new SimpleTimeFrame();
		now.addEnd(Calendar.MINUTE, 1);

		// get general availability
		List<FacilityAvailability> das = FamDaoProxy.facilityDao().getFacilityAvailabilitiesFollowingParents(today, this.facility);
		List<FacilityAvailability> dasMerged = FacilityAvailabilityMerger.getMergedByTimeStampSet(das, today);
		for (FacilityAvailability da : dasMerged) {
			if (da.overlaps(now)) {
				result = da;
			}
		}

		// found a not availability?
		if (result.isCompletelyAvailable()) { // no
			if (this.facility.isBookable() && FacilityConfigDao.bookingRule(this.facility.getKey()).getBookingStrategy() == BookingStrategy.TIME_BASED) {
				// ↖ is bookable and time based booking
				// check, if there is a booking
				List<Booking> bookings = FamDaoProxy.bookingDao().getAllUncanceledBookingsAndApplicationsOfToday((FacilityBookable) this.facility);
				for (Booking booking : bookings) {
					if (booking.overlaps(now)) {
						result.setNotAvailableBecauseOfBooking();
						result.setBasePeriodOfTime(booking.getSessionTimeFrame());
					}
				}
			}
		}
		return result;
	}

	/**
	 * return true, if a {@link FacilityAvailability} or a uncancelled
	 * {@link Booking} (or application) is set <strong>for now</strong>.
	 * 
	 * @return true, if a {@link FacilityAvailability} or a uncancelled
	 *         {@link Booking} (or application) is set <strong>for now</strong>.
	 */
	public boolean facilityAvailabilityIsSet() {
		boolean result = false;
		// prepare vars
		TimeFrame today = SimpleTimeFrame.getToday();
		TimeFrame now = new SimpleTimeFrame();
		now.addEnd(Calendar.MINUTE, 1);

		// get general availability
		List<FacilityAvailability> das = FamDaoProxy.facilityDao().getFacilityAvailabilitiesFollowingParents(today, this.facility);
		List<FacilityAvailability> dasMerged = FacilityAvailabilityMerger.getMergedByTimeStampSet(das, today);
		for (FacilityAvailability da : dasMerged) {
			if (da.overlaps(now)) {
				result = true;
				break;
			}
		}

		// found a not availability?
		if (!result) { // no
			// is facility bookable?
			if (this.facility.isBookable() && FacilityConfigDao.bookingRule(this.facility.getKey()).getBookingStrategy() != BookingStrategy.QUEUE_BASED) {
				// check, if there is a booking
				List<Booking> bookings = FamDaoProxy.bookingDao().getAllUncanceledBookingsAndApplicationsOfToday((FacilityBookable) this.facility);
				for (Booking booking : bookings) {
					if (booking.overlaps(now)) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}
}

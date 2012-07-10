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

import java.util.ArrayList;
import java.util.List;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;

/**
 * booking requests may have one or more conflicts. (like two people booked the
 * same). this is a helper class for that cases
 * 
 * @author Daniel Oltmanns
 * @since 0.20091111
 */
public class ApplicationConflicts {

	/** one and only instance of me */
	private volatile static ApplicationConflicts me;

	/** construct me */
	private ApplicationConflicts() {
	}

	/**
	 * return the one and only instance of me
	 * 
	 * @return the one and only instance of me
	 */
	public static ApplicationConflicts getInstance() {
		if (me == null) { // no instance so far
			synchronized (ApplicationConflicts.class) {
				if (me == null) { // still no instance so far
					me = new ApplicationConflicts(); // the one and only
				}
			}
		}
		return me;
	}

	/**
	 * return true, if there are more booking requests for the given facility at
	 * one time than the facility has capacity units.
	 * 
	 * @param bookableFacility
	 *            asked for
	 * @return true, if there are more booking requests for the given facility at
	 *         one time than the facility has capacity units.
	 */
	public boolean isOverapplied(FacilityBookable bookableFacility) {
		boolean result = false;
		List<Booking> bookingRequests = FamDaoProxy.bookingDao().getAllUncanceledBookingsAndApplicationsNotBegunYet(bookableFacility);
		int capacityInUse = BookingUtil.getSumOfMaxCapacityUsedAtOneTime(bookingRequests);
		if (capacityInUse > bookableFacility.getCapacityUnits()) { // no capacity
																	// left
			result = true;
		}
		return result;
	}

	/**
	 * return all bookings overlapping with the given time booking and that are
	 * responsible for an overapplication of the facility the given booking is
	 * for. <br />
	 * return empty list if no conflict was found.
	 * 
	 * @param booking
	 *            the conflicts are returned of
	 * @return all bookings overlapping with the given time booking and that are
	 *         responsible for an overapplication of the facility the given
	 *         booking is for.
	 */
	public List<Booking> getConflicts(TimeBooking booking) {
		List<Booking> result = new ArrayList<Booking>();
		List<Booking> bookingRequests = FamDaoProxy.bookingDao().getAllUncanceledOverlapping(booking);
		int capacityInUse = BookingUtil.getSumOfMaxCapacityUsedAtOneTime(bookingRequests);
		if (capacityInUse > booking.getFacility().getCapacityUnits()) { // overbooked
			result = bookingRequests;
		}
		return result;
	}
}

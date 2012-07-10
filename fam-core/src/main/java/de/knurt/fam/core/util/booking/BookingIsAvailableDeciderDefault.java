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

import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * decide, if a {@link Booking} is available or not - default fam behavior.
 * 
 * @author Daniel Oltmanns
 * @since 1.4.3 (07/22/2011)
 */
public class BookingIsAvailableDeciderDefault implements BookingIsAvailableDecider {

	@Override
	public boolean isAvailableForInsertion(TimeBooking bookingRequest) {
		return this.isAvailableForInsertion(bookingRequest, bookingRequest.getSessionTimeFrame());
	}

	@Override
	public boolean isAvailableForInsertion(QueueBooking bookingRequest) {
		int maxLength = bookingRequest.getQueueBasedBookingRule().getMaxQueueLength();
		boolean result = true;
		if (maxLength <= 0 || bookingRequest.getActualQueuePosition() == null) {
			result = true;
		} else {
			result = bookingRequest.getActualQueuePosition() <= bookingRequest.getQueueBasedBookingRule().getActualQueueLength();
		}
		return result;
	}

	@Override
	public boolean isAvailableForInsertion(TimeBooking bookingRequest, TimeFrame supposedTimeFrame) {
		boolean result = true;

		// time frame is past
		if (supposedTimeFrame.startsInPast()) {
			result = false;
		}

		// facility available in general
		if (result == true && bookingRequest.isApplicableToANotAvailableFacilityAvailability(supposedTimeFrame)) {
			result = false;
		}

		// are there bookings already
		if (result == true) {
			List<Booking> bookings = FamDaoProxy.bookingDao().getUncanceledBookingsWithoutApplicationsIn(bookingRequest.getFacility(), supposedTimeFrame);
			if (bookings.size() > 0) {
				int capacityInUse = BookingUtil.getSumOfMaxCapacityUsedAtOneTime(bookings);
				if (capacityInUse + bookingRequest.getCapacityUnits() > bookingRequest.getFacility().getCapacityUnits()) { // no
					// capacity
					// left
					result = false;
				}

			}
		}

		if (result == true) {
			// ↘ user has to wait some time
			int time2waitOnTheFacility = FamAuth.getEarliestPossibilityToBookFromNow(bookingRequest.getUser(), bookingRequest.getFacility());
			if (time2waitOnTheFacility > 0) {
				Calendar earliestStartTime = Calendar.getInstance();
				earliestStartTime.add(Calendar.MINUTE, time2waitOnTheFacility);
				// ↘ user requested a time before
				if (supposedTimeFrame.getCalendarStart().before(earliestStartTime)) {
					result = false;
				}
			}
		}
		return result;
	}

}

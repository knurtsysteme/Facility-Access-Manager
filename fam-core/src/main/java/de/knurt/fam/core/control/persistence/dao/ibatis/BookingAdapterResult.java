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
package de.knurt.fam.core.control.persistence.dao.ibatis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.BookingStrategy;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.BookingStatus;
import de.knurt.fam.core.model.persist.booking.Cancelation;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * adapt database to a booking
 * 
 * @author Daniel Oltmanns
 * @since 0.20090701
 */
public class BookingAdapterResult {

	/**
	 * return a list of bookings represented by given booking adapters
	 * 
	 * @param sadapts
	 *            adapters representing bookings.
	 * @return a list of bookings represented by given booking adapters
	 */
	protected static List<Booking> getBookings(List<BookingAdapterResult> sadapts) {
		List<Booking> result = new ArrayList<Booking>();
		for (BookingAdapterResult sadapt : sadapts) {
			result.add(sadapt.getBooking());
		}
		return result;
	}

	/**
	 * return a list of queue bookings represented by given booking adapters
	 * 
	 * @param sadapts
	 *            adapters representing queue bookings.
	 * @return a list of queue bookings represented by given booking adapters
	 */
	protected static List<QueueBooking> getQueueBookings(List<BookingAdapterResult> sadapts) {
		List<QueueBooking> result = new ArrayList<QueueBooking>();
		for (BookingAdapterResult sadapt : sadapts) {
			result.add((QueueBooking) sadapt.getBooking());
		}
		return result;
	}

	/**
	 * return the gime frame, if <code>time_start</code> and
	 * <code>time_end</code> is set.
	 * 
	 * @return the gime frame, if <code>time_start</code> and
	 *         <code>time_end</code> is set.
	 */
	protected TimeFrame getTimeFrame() {
		if (this.time_end == null || this.time_start == null) {
			return null;
		} else {
			return new SimpleTimeFrame(this.time_start.getTime(), this.time_end.getTime());
		}
	}

	private String username, facilityKey, cancelation_username, cancelation_reason, notice;
	private Date seton, status_seton, time_end, time_start, cancelation_seton;
	private int status_id, capacityUnits, id, idBookedInBookingStrategy;
	private boolean processed;

	/**
	 * construct an empty adapter. this must be used for select operations.
	 */
	public BookingAdapterResult() {
	}

	/**
	 * return a {@link Booking} from the set elements. must be invoke after all
	 * attributes are set.
	 * 
	 * @return a {@link Booking} from the set elements.
	 */
	public Booking getBooking() {
		Booking result;
		if (this.idBookedInBookingStrategy == BookingStrategy.QUEUE_BASED) {
			if (this.time_start == null) { // not started now
				result = QueueBooking.getBooking4Query();
			} else { // real session started
				if (this.time_end == null) { // real session does not end
					result = QueueBooking.getBooking4Query(this.time_start.getTime(), this.time_start.getTime());
				} else { // real session is finished
					result = QueueBooking.getBooking4Query(this.time_start.getTime(), this.time_end.getTime());
				}
			}
		} else { // this.idBookedInBookingStrategy == BookingStrategy.TIME_BASED
			result = TimeBooking.getBooking4Query(this.time_start.getTime(), this.time_end.getTime());
		}
		result.setProcessed(this.processed);
		result.setUsername(this.username);
		result.setFacilityKey(this.facilityKey);
		result.setBookingRule(FacilityConfigDao.bookingRule(this.facilityKey));
		if (this.cancelation_reason != null) {
			Cancelation c = Cancelation.getCancelationForMapping(this.cancelation_username, this.cancelation_reason, this.cancelation_seton);
			result.setCancelation(c);
		}
		result.setSeton(this.seton);
		result.setBookingStatus(new BookingStatus(this.status_id, this.status_seton));
		result.setId(this.id);
		result.setCapacityUnits(this.capacityUnits);
		result.setNotice(this.notice);
		result.setLastInvoiced(this.last_invoiced);
		result.setIdBookedInBookingStrategy(this.idBookedInBookingStrategy);
		return result;
	}

	/**
	 * set username
	 * 
	 * @see Booking#setUsername(java.lang.String)
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * set id of booking strategy booked the adapted booking
	 * 
	 * @param idBookedInBookingStrategy
	 *            id of booking strategy booked the adapted booking
	 */
	public void setIdBookedInBookingStrategy(int idBookedInBookingStrategy) {
		this.idBookedInBookingStrategy = idBookedInBookingStrategy;
	}

	/**
	 * set notice
	 * 
	 * @see Booking#setNotice(java.lang.String)
	 * @param notice
	 *            to set
	 */
	public void setNotice(String notice) {
		this.notice = notice;
	}

	/**
	 * set processed
	 * 
	 * @see Booking#setProcessed(boolean)
	 * @param processed
	 *            to set
	 */
	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	/**
	 * @param facilityKey
	 *            the facilityKey to set
	 */
	public void setFacilityKey(String facilityKey) {
		this.facilityKey = facilityKey;
	}

	/**
	 * @param cancelation_username
	 *            the cancelation_username to set
	 */
	public void setCancelation_username(String cancelation_username) {
		this.cancelation_username = cancelation_username == null || cancelation_username.isEmpty() ? null : cancelation_username;
	}

	/**
	 * @param cancelation_reason
	 *            the cancelation_reason to set
	 */
	public void setCancelation_reason(String cancelation_reason) {
		this.cancelation_reason = cancelation_reason == null || cancelation_reason.isEmpty() ? null : cancelation_reason;
	}

	/**
	 * @param seton
	 *            the seton to set
	 */
	public void setSeton(Date seton) {
		this.seton = seton;
	}

	/**
	 * @param status_seton
	 *            the status_seton to set
	 */
	public void setStatus_seton(Date status_seton) {
		this.status_seton = status_seton;
	}

	/**
	 * @param time_end
	 *            the time_end to set
	 */
	public void setTime_end(Date time_end) {
		this.time_end = time_end;
	}

	/**
	 * @param time_start
	 *            the time_start to set
	 */
	public void setTime_start(Date time_start) {
		this.time_start = time_start;
	}

	/**
	 * @param cancelation_seton
	 *            the cancelation_seton to set
	 */
	public void setCancelation_seton(Date cancelation_seton) {
		this.cancelation_seton = cancelation_seton;
	}

	/**
	 * @param status_id
	 *            the status_id to set
	 */
	public void setStatus_id(int status_id) {
		this.status_id = status_id;
	}

	/**
	 * @param capacityUnits
	 *            the capacityUnits to set
	 */
	public void setCapacityUnits(int capacityUnits) {
		this.capacityUnits = capacityUnits;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	private Date last_invoiced;

	public void setLast_invoiced(Date lastInvoiced) {
		last_invoiced = lastInvoiced;
	}

	public Date getLast_invoiced() {
		return last_invoiced;
	}

	/**
	 * return the id of the status of the booking being adapted
	 * 
	 * @return the id of the status of the booking being adapted
	 */
	protected int getStatusId() {
		return status_id;
	}
}

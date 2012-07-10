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

import java.util.Date;

import de.knurt.fam.core.model.persist.booking.Booking;

/**
 * adapt a booking to update, insert or delete it in database.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090701
 */
public class BookingAdapterParameter {

	private Booking booking;

	/**
	 * construct a adapter with a booking. this must be used for insert, update
	 * and delete operations.
	 * 
	 * @param booking
	 *            to adapt
	 */
	public BookingAdapterParameter(Booking booking) {
		this.booking = booking;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return this.booking.getUsername();
	}

	/**
	 * return {@link Booking#getIdBookedInBookingStrategy()}
	 * 
	 * @return {@link Booking#getIdBookedInBookingStrategy()}
	 */
	public int getIdBookedInBookingStrategy() {
		return this.booking.getIdBookedInBookingStrategy();
	}

	/**
	 * return {@link Booking#getNotice()}
	 * 
	 * @return {@link Booking#getNotice()}
	 */
	public String getNotice() {
		return this.booking.getNotice();
	}

	/**
	 * return {@link Booking#getId()}
	 * 
	 * @return {@link Booking#getId()}
	 */
	public Integer getId() {
		return this.booking.getId();
	}

	/**
	 * must be the only writable operation for update
	 * 
	 * @param id
	 *            to set
	 */
	public void setId(Integer id) {
		this.booking.setId(id);
	}

	/**
	 * @return the facilityKey
	 */
	public String getFacilityKey() {
		return this.booking.getFacilityKey();
	}

	/**
	 * @return the cancelation_username
	 */
	public String getCancelation_username() {
		return this.booking.isCanceled() ? this.booking.getCancelation().getUsername() : null;
	}

	/**
	 * @return the cancelation_reason
	 */
	public String getCancelation_reason() {
		return this.booking.isCanceled() ? this.booking.getCancelation().getReason() : null;
	}

	/**
	 * @return the seton
	 */
	public Date getSeton() {
		return this.booking.getSeton();
	}

	/**
	 * @return the status_seton
	 */
	public Date getStatus_seton() {
		return this.booking.getBookingStatus() == null ? null : this.booking.getBookingStatus().getSeton();
	}

	/**
	 * return {@link Booking#isProcessed()}
	 * 
	 * @return {@link Booking#isProcessed()}
	 */
	public boolean isProcessed() {
		return this.booking.isProcessed();
	}

	/**
	 * @return the time_end
	 */
	public Date getTime_end() {
		Date result = null;
		if (this.booking.getSessionTimeFrame() != null) {
			result = this.booking.getSessionTimeFrame().getDateEnd();
		}
		return result;
	}

	public Date getLastInvoiced() {
		return this.booking.getLastInvoiced();
	}

	public Date getLast_invoiced() {
		return this.getLastInvoiced();
	}

	public void setLast_invoiced(Date lastInvoiced) {
		this.booking.setLastInvoiced(lastInvoiced);
	}

	/**
	 * @return the time_start
	 */
	public Date getTime_start() {
		Date result = null;
		if (this.booking.getSessionTimeFrame() != null) {
			result = this.booking.getSessionTimeFrame().getDateStart();
		}
		return result;
	}

	/**
	 * @return the cancelation_seton
	 */
	public Date getCancelation_seton() {
		return this.booking.isCanceled() ? this.booking.getCancelation().getDateCanceled() : null;
	}

	/**
	 * @return the status_id
	 */
	public Integer getStatus_id() {
		return this.booking.getBookingStatus() == null ? null : this.booking.getBookingStatus().getStatus();
	}

	/**
	 * @return the capacityUnits
	 */
	public Integer getCapacityUnits() {
		return this.booking.getCapacityUnits();
	}
}

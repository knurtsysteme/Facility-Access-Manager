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
package de.knurt.fam.core.model.persist.booking;

import java.util.Date;

/**
 * a data holder for the status of a booking and some factory methods.
 * 
 * these questions are answered here:<br />
 * is a booking a application or a final booking? if it is an application, is
 * there an positive or negative answer given? yes? from who and when?
 * 
 * @author Daniel Oltmanns
 * @since 0.20090615
 */
public class BookingStatus {

	/**
	 * not status set for this booking. this status must not be persist in
	 * bookings.
	 */
	public final static int STATUS_UNSET = 0;
	/**
	 * this booking is just an application by now.
	 */
	public final static int STATUS_APPLIED = 1;
	/**
	 * this booking is proofed.
	 */
	public final static int STATUS_BOOKED = 2;
	private int status = STATUS_UNSET;
	private Date seton;

	/**
	 * construct a booking status with given status set on now.
	 * 
	 * @param status
	 *            set as status
	 */
	public BookingStatus(int status) {
		this(status, new Date());
	}

	/**
	 * construct a booking status with given status set on seton.
	 * 
	 * @param status
	 *            set as status
	 * @param seton
	 *            date status has been set on
	 */
	public BookingStatus(int status, Date seton) {
		this.status = status;
		this.seton = seton;
	}

	/**
	 * return true, if status of booking is unset
	 * 
	 * @return true, if status of booking is unset
	 */
	public boolean isUnset() {
		return status == STATUS_UNSET;
	}

	/**
	 * return true, if status of booking is applied
	 * 
	 * @return true, if status of booking is applied
	 */
	public boolean isApplied() {
		return this.getStatus() == STATUS_APPLIED;
	}

	/**
	 * return true, if status of booking is booked
	 * 
	 * @return true, if status of booking is booked
	 */
	public boolean isBooked() {
		return this.getStatus() == STATUS_BOOKED;
	}

	/**
	 * return date, status set on
	 * 
	 * @return the date, status set on
	 */
	public Date getSeton() {
		return seton;
	}

	/**
	 * return status of booking
	 * 
	 * @return the status of booking
	 */
	public int getStatus() {
		return status;
	}
}

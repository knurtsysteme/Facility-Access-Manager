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
import java.util.List;

import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.config.BookingStrategy;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.Availability;
import de.knurt.fam.core.model.persist.Storeable;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.booking.ApplicationConflicts;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.heinzelmann.util.adapter.ViewableObject;
import de.knurt.heinzelmann.util.query.Identificable;
import de.knurt.heinzelmann.util.shopping.Purchasable;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * data holders for bookings at a facility.
 * 
 * every booking made has a session, a user, details for the booked conditions.
 * a booking can be fixed (it is a booking) or the booking has an application
 * status (must confirmed before it is a real booking).
 * 
 * furthermore every booking has been made by specific rules.
 * 
 * @see BookingRule
 * @see TimeBookingRequest
 * @see BookingStatus
 * @author Daniel Oltmanns
 * @since 0.20090615
 */
public interface Booking extends Cloneable, Storeable, Availability, Identificable, ViewableObject, Purchasable {

	/**
	 * cancel the booking with the given cancelation. the cancelation contains
	 * meta data to get more transparency in cancelations.
	 * 
	 * @param cancelation
	 *            used to cancel the booking.
	 */
	public void cancel(Cancelation cancelation);
	
	/**
	 * return the booking conflicting with this.
	 * conflicts exist on time booking applications and other bookings are classicaly other applications.
	 * @see ApplicationConflicts
	 * @return the booking applications that are conflicting with <code>to</code>
	 */
	public List<Booking> getConflicts();

	/**
	 * return the {@link FacilityBookable} the booking is for.
	 * 
	 * @return the {@link FacilityBookable} the booking is for.
	 */
	public FacilityBookable getFacility();

	/**
	 * return the user requested this booking.
	 * 
	 * @return the user requested this booking.
	 */
	public User getUser();

	/**
	 * return the date of the last invoice. a session is invoiced, if the
	 * invoice is generated as pdf and send via email. a session must not be
	 * invoiced. return null in that case.
	 * 
	 * @return the date of the last invoice.
	 */
	public Date getLastInvoiced();

	/**
	 * do processing operations like put something into the db or just send a
	 * mail. usualy invoke setProcessed
	 */
	public void processSession();

	/**
	 * set true, if the session has been processed (by an operator).
	 * 
	 * @param processed
	 *            true, if the session has been processed (by an operator).
	 */
	public void setProcessed(boolean processed);

	/**
	 * return true, if the session has been processed (by an operator).
	 * 
	 * @return true, if the session has been processed (by an operator).
	 */
	public boolean isProcessed();

	/**
	 * return true, if the session is already made.
	 * 
	 * @return true, if the session is already made.
	 */
	public boolean sessionAlreadyMade();

	/**
	 * return a message after booking has been purchased. a booking is purchased
	 * when someone request a booking. when purchasing a booking, this ends in a
	 * failure or in a success.
	 * 
	 * @return a message after booking has been purchased.
	 */
	public String getMessageAfterTryingToPurchase();

	/**
	 * return strategy of booking as booked by the user. mostly the id of the
	 * used {@link BookingRule}, but might be different from that after changing
	 * strategy in configuration.
	 * 
	 * @see BookingRule#getBookingStrategy()
	 * @return strategy of booking as booked by the user.
	 */
	public int getIdBookedInBookingStrategy();

	/**
	 * set the booking strategy this booking has been made. a booking is booked
	 * with a booking strategy. this strategy might be "queue based" or
	 * "time based". the default strategy of the booking might be different from
	 * that, if the application is reconfigured. if a facility is booked with
	 * strategy a, then the system sets the strategy b for the same facility, set
	 * a here!
	 * 
	 * @see BookingStrategy
	 * @param idBookedInBookingStrategy
	 *            the booking strategy this booking has been made
	 */
	public void setIdBookedInBookingStrategy(int idBookedInBookingStrategy);

	/**
	 * return a notice for this booking processable by all.
	 * 
	 * @return a notice for this booking processable by all.
	 */
	public String getNotice();

	/**
	 * set a notice for this booking processable by all.
	 * 
	 * @param notice
	 *            for this booking processable by all.
	 */
	public void setNotice(String notice);

	/**
	 * return true, if the start of the session is in past.
	 * 
	 * @return true, if the start of the session is in past.
	 */
	public boolean sessionAlreadyBegun();

	/**
	 * return true, if this overlaps the other booking.
	 * 
	 * @see TimeFrame#overlaps(de.knurt.heinzelmann.util.time.TimeFrame)
	 * @param otherBooking
	 *            to check
	 * @return true, if this overlaps the other booking.
	 */
	public boolean overlaps(Booking otherBooking);

	/**
	 * get the application the bonus. there is an optional mail message to send.
	 * 
	 * @param mailMessage
	 *            optional mail message to send.
	 */
	public void confirmApplication(String mailMessage);

	/**
	 * return the time frame when the session has been done or surely comes.
	 * return <code>null</code> if the time frame is not known yet.
	 * 
	 * @return the time frame when the session has been done or surely comes.
	 */
	public TimeFrame getSessionTimeFrame();

	/**
	 * return the capacity units booked.
	 * 
	 * @see BookingRule#getMaxBookableCapacityUnits()
	 * @see FacilityBookable#getCapacityUnits()
	 * @return the capacity units booked.
	 */
	public Integer getCapacityUnits();

	/**
	 * return true, if the booking request is an application that must be
	 * confirmed. not all users have the right to book a facility directly. some
	 * users must apply for a session on some facilities.
	 * 
	 * @return true, if the booking request is an application that must be
	 *         confirmed.
	 */
	public boolean isApplication();

	/**
	 * return true, if this booking is available and can be insert into db. it
	 * cannot be used for objects, that are already in the database.
	 * 
	 * @return true, if this booking is available and can be insert into db.
	 */
	public boolean isAvailableForInsertion();

	/**
	 * return true, if the booking has been cancelled.
	 * 
	 * @return true, if the booking has been cancelled.
	 */
	public boolean isCanceled();

	/**
	 * set status "this is booked". after setting this, it is not a application
	 * anymore and it can only be canceled for special reasons (like sudden
	 * failures).
	 * 
	 * @see BookingStatus#STATUS_BOOKED
	 */
	public void setBooked();

	/**
	 * set status unset
	 * 
	 * @see BookingStatus#STATUS_UNSET
	 */
	public void setUnset();

	/**
	 * set "this is an application"
	 * 
	 * @see BookingStatus#STATUS_APPLIED
	 */
	public void setApplied();

	/**
	 * return the username of user requested this booking.
	 * 
	 * @return the username of user requested this booking.
	 */
	public String getUsername();

	/**
	 * set the username of user requested this booking.
	 * 
	 * @param username
	 *            of user requested this booking.
	 */
	public void setUsername(String username);

	/**
	 * return the key represeting the facility this booking is for.
	 * 
	 * @return the key represeting the facility this booking is for.
	 */
	public String getFacilityKey();

	/**
	 * set the key represeting the facility this booking is for.
	 * 
	 * @param facilityKey
	 *            key represeting the facility this booking is for.
	 */
	public void setFacilityKey(String facilityKey);

	/**
	 * return the status of the booking.
	 * 
	 * @return the status of the booking.
	 */
	public BookingStatus getBookingStatus();

	/**
	 * set the status of the booking.
	 * 
	 * @param bookingStatus
	 *            status of the booking.
	 */
	public void setBookingStatus(BookingStatus bookingStatus);

	/**
	 * return the booking rule used and to use for this booking. when creating a
	 * booking, a default booking rule is used to book a facility. anyway, there
	 * might be bookings in the database where another booking rule has been
	 * used for. in this case, use that booking rule.
	 * 
	 * to change the configuration this way is not tested and might result in
	 * strange behaviour.
	 * 
	 * @return the booking rule used and to use for this booking.
	 */
	public BookingRule getBookingRule();

	/**
	 * set the rules this booking is made with.
	 * 
	 * @param bookingRule
	 *            the booking rule used and to use for this booking.
	 */
	public void setBookingRule(BookingRule bookingRule);

	/**
	 * set the capacity units requested with this booking
	 * 
	 * @param capacityUnits
	 *            the capacity units requested with this booking
	 */
	public void setCapacityUnits(Integer capacityUnits);

	/**
	 * return the date this booking was requested on.
	 * 
	 * @return the date this booking was requested on.
	 */
	public Date getSeton();

	/**
	 * set date this booking was requested on.
	 * 
	 * @param seton
	 *            the date this booking was requested on.
	 */
	public void setSeton(Date seton);

	/**
	 * return the cancelation for this booking or <code>null</code> if the
	 * booking is uncancelled.
	 * 
	 * @return the cancelation for this booking or <code>null</code> if the
	 *         booking is uncancelled.
	 */
	public Cancelation getCancelation();

	/**
	 * set the cancelation for this booking or <code>null</code> if the booking
	 * is uncancelled.
	 * 
	 * @param cancelation
	 *            for this booking or <code>null</code> if the booking is
	 *            uncancelled.
	 */
	public void setCancelation(Cancelation cancelation);

	/**
	 * return true, if the booking status is booked.
	 * 
	 * @see BookingStatus#STATUS_BOOKED
	 * @return true, if the booking status is booked.
	 */
	public boolean isBooked();

	/**
	 * return true, if the session time frame overlaps the given timeFrame.
	 * 
	 * @see #getSessionTimeFrame()
	 * @param timeFrame
	 *            to check
	 * @return true, if the session time frame overlaps the given timeFrame.
	 */
	public boolean overlaps(TimeFrame timeFrame);

	/**
	 * return true, if {@link #getIdBookedInBookingStrategy()} is queue based.
	 * 
	 * @return true, if {@link #getIdBookedInBookingStrategy()} is queue based.
	 */
	public boolean isQueueBased();

	/**
	 * return true, if {@link #getIdBookedInBookingStrategy()} is time based.
	 * 
	 * @return true, if {@link #getIdBookedInBookingStrategy()} is time based.
	 */
	public boolean isTimeBased();

	public void setLastInvoiced(Date lastInvoiced);

	/**
	 * set the booking invoiced and update. typicaly implementation is:
	 * 
	 * <pre>
	 * this.setLastInvoiced(new Date());
	 * this.update();
	 * </pre>
	 */
	public void invoice();

	/**
	 * delete a booking physically from the database.
	 * please only use for system purposes (e.g. a rollback).
	 * for all user interaction use {@link #cancel(Cancelation)} instead!
	 * return true if deleting has been successful
	 */
	public boolean delete();
}

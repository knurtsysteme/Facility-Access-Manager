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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.content.text.FamText;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.booking.ApplicationConflicts;
import de.knurt.fam.core.util.booking.BookingIsAvailableDeciderProxy;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.fam.core.util.mail.OutgoingUserMailBox;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * a data holder for booking a facility at a specific time.
 * 
 * the booking is made from a user for a specific facility and time frame. it is
 * not a definitive booking but has a booking status.
 * 
 * @see BookingStatus
 * @author Daniel Oltmanns
 * @since 0.20090615
 */
public final class TimeBooking extends AbstractBooking implements TimeFrame {

	/** {@inheritDoc} */
	@Override
	public TimeFrame clone() {
		return (TimeFrame) super.clone();
	}

	private TimeFrame timeFrame;

	/**
	 * return true, if the session of this booking is already made. this session
	 * is made, if the requested time frame ends. it does not matter, if the
	 * booking is an application or it is cancelled.
	 * 
	 * @return true, if the session of this booking is already made.
	 */
	@Override
	public boolean sessionAlreadyMade() {
		TimeFrame tmp = this.getSessionTimeFrame();
		return tmp != null && tmp.endsInPast();
	}

	/**
	 * return true, if the session started in past.
	 * 
	 * @return true, if the session started in past.
	 */
	@Override
	public boolean sessionAlreadyBegun() {
		TimeFrame tmp = this.getSessionTimeFrame();
		return tmp != null && tmp.startsInPast();
	}

	/**
	 * return a new {@link TimeBooking} from a {@link TimeBookingRequest} with
	 * an unset {@link BookingStatus}.
	 * 
	 * this means, all {@link BookingStatus} objects of Bookings must be set
	 * afterwards, when {@link TimeBooking} shall be insert.
	 * 
	 * if given request is invalid, return null.
	 * 
	 * @see BookingStatus#STATUS_UNSET
	 * @param bookingRequest
	 *            a new booking is created from
	 * @return a new {@link TimeBooking} from a {@link TimeBookingRequest} with
	 *         an unset {@link BookingStatus}.
	 */
	public static TimeBooking getNewBooking(TimeBookingRequest bookingRequest) {
		if (bookingRequest.isValidRequest()) {
			FacilityBookable facility = bookingRequest.getFacility();
			User user = bookingRequest.getUser();
			TimeFrame tf = bookingRequest.getRequestedTimeFrame();
			BookingStatus bs = new BookingStatus(BookingStatus.STATUS_UNSET);
			Integer capacityUnits = bookingRequest.getRequestedCapacityUnits();
			BookingRule bookingRule = bookingRequest.getBookingRule();
			return new TimeBooking(facility, user, tf, bs, capacityUnits, bookingRule);
		} else {
			return null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getArticleNumber() {
		return this.getUsername() + this.getCapacityUnits() + this.getFacilityKey() + this.getStart() + this.getEnd();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAvailableForInsertion() {
		return BookingIsAvailableDeciderProxy.me().getDeciderFactory().get(this.getFacility()).isAvailableForInsertion(this);
	}

	/**
	 * cancel the booking with given cancelation.
	 * 
	 * @param cancelation
	 *            used to cancel the booking.
	 */
	@Override
	public void cancel(Cancelation cancelation) {
		this.setCancelation(cancelation);
		this.update();
		OutgoingUserMailBox.insert_BookingCancelation(this);
	}

	/**
	 * return true, if this booking overlaps with a unavailability set. it
	 * overlaps it with the session time frame.
	 * 
	 * @see #getSessionTimeFrame()
	 * @return true, if this booking overlaps with a unavailability set.
	 */
	public boolean isApplicableToANotAvailableFacilityAvailability() {
		return this.isApplicableToANotAvailableFacilityAvailability(this.getSessionTimeFrame());
	}

	public boolean isApplicableToANotAvailableFacilityAvailability(TimeFrame supposedTimeFrame) {
		boolean result = false;
		List<FacilityAvailability> availabilities = FamDaoProxy.facilityDao().getFacilityAvailabilitiesMergedByFacilities(supposedTimeFrame, this.getFacilityKey());
		for (FacilityAvailability availability : availabilities) {
			boolean isNotBookable = availability.isNotAvailableInGeneral() || availability.isNotAvailableBecauseOfMaintenance() || availability.isNotAvailableBecauseOfSuddenFailure() || availability.mustNotStartHere();
			if (isNotBookable) {
				if (availability.applicableTo(supposedTimeFrame)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * return true, if this booking is available on given time frame. it is not,
	 * if ...
	 * <ul>
	 * <li>starting time of given time frame is in past</li>
	 * <li>it is booked out</li>
	 * <li>the facility is generaly not available</li>
	 * <li>the user has to wait for the facility some time</li>
	 * </ul>
	 * assume, that you are not booked and not in the database. or answer the
	 * question "am i available to insert me into the db?"
	 * 
	 * @param supposedTimeFrame
	 *            to check
	 * @return true, if this booking is available, if it has the given time
	 *         frame.
	 */
	public boolean isAvailableForInsertion(TimeFrame supposedTimeFrame) {
		return BookingIsAvailableDeciderProxy.me().getDeciderFactory().get(this.getFacility()).isAvailableForInsertion(this, supposedTimeFrame);
	}

	/**
	 * return TimeBooking for querying by example.
	 * 
	 * @return TimeBooking for querying by example.
	 */
	public static TimeBooking getEmptyExampleBooking() {
		return new TimeBooking();
	}

	/**
	 * return a {@link TimeBooking} for queries. this is not for persistent use
	 * but to get some out of the db.
	 * 
	 * @param start
	 *            time of booking starts
	 * @param end
	 *            time of booking ends
	 * @see TimeFrame#setEnd(long)
	 * @see TimeFrame#setStart(long)
	 * @return a {@link TimeBooking} for queries.
	 */
	public static TimeBooking getBooking4Query(long start, long end) {
		TimeBooking result = new TimeBooking();
		result.timeFrame = new SimpleTimeFrame(start, end);
		return result;
	}

	/**
	 * construct a {@link TimeBooking}.
	 * 
	 * @see BookingRule#smallestBookableCapacityUnit
	 * @param facility
	 *            the booking is for.
	 * @param user
	 *            interested the facility
	 * @param tf
	 *            the user is interested in
	 * @param bs
	 *            status of booking.
	 * @param capacityUnits
	 *            booked
	 * @param bookingRule
	 *            used for booking
	 */
	private TimeBooking(FacilityBookable facility, User user, TimeFrame tf, BookingStatus bs, int capacityUnits, BookingRule bookingRule) {
		this(facility.getKey(), user.getUsername(), tf.getStart(), tf.getEnd(), bs, capacityUnits, bookingRule);
	}

	private TimeBooking(String facilityKey, String username, long start, long end, BookingStatus bookingStatus, int capacityUnits, BookingRule bookingRule) {
		super(facilityKey, username, bookingStatus, capacityUnits, bookingRule);
		this.timeFrame = new SimpleTimeFrame(start, end);
	}

	/**
	 * construct empty booking (for querying reason)
	 */
	protected TimeBooking() {
	}

	/** {@inheritDoc} */
	@Override
	public synchronized boolean insert() throws DataIntegrityViolationException {
		boolean result = FamDaoProxy.bookingDao().insert(this);
		if (this.isUncanceledBooking()) {
			// send mail to booker
			OutgoingUserMailBox.insert_BookingMade(this);

			// cancel applications for same slot
			Cancelation cancelation = new Cancelation(this.getUser(), Cancelation.REASON_BOOKED_BY_ANOTHER);
			this.cancelApplicationsForSameSlotIfNotAvailableAnymore(cancelation);
		}
		return result;

	}

	private synchronized boolean update(boolean cancelUncanceledBookingsForSameSlot) throws DataIntegrityViolationException {
		boolean result = FamDaoProxy.bookingDao().update(this);
		if (cancelUncanceledBookingsForSameSlot) {
			// cancel applications for same slot
			Cancelation cancelation = new Cancelation(this.getUser(), Cancelation.REASON_BOOKED_BY_ANOTHER);
			this.cancelApplicationsForSameSlotIfNotAvailableAnymore(cancelation);
		}
		return result;

	}

	/** {@inheritDoc} */
	@Override
	public boolean update() throws DataIntegrityViolationException {
		return this.update(this.isUncanceledBooking());
	}

	private boolean isUncanceledBooking() {
		return this.getBookingStatus().isBooked() && this.isCanceled() == false;
	}

	private void cancelApplicationsForSameSlotIfNotAvailableAnymore(Cancelation cancelation) {
		List<Booking> bookings = FamDaoProxy.bookingDao().getUncanceledBookingsAndApplicationsIn(this.getFacility(), this);
		for (Booking booking : bookings) {
			if (booking.getId().intValue() == this.getId().intValue()) { // hello
				// me!
				continue; // do not cancel me
			}

			if (booking.isApplication() && !booking.isAvailableForInsertion()) { // is
				// not
				// available
				// anymore
				booking.cancel(cancelation);
				booking.update();
			}

		}
	}

	/**
	 * return true if the session time frame requested contains the given date.
	 * 
	 * @param date
	 *            to check
	 * @return true if the session time frame requested contains the given date.
	 */
	@Override
	public boolean contains(Date date) {
		return this.timeFrame.contains(date);
	}

	/**
	 * add the given time to requested time frame's start.
	 * 
	 * @see TimeFrame#add(int, int)
	 * @param field
	 *            the amount is added to
	 * @param amount
	 *            to be added to the given time frame
	 */
	@Override
	public void add(int field, int amount) {
		this.timeFrame.add(field, amount);
	}

	/**
	 * forward {@link TimeFrame#setCalendarStart(int, int)} of requested
	 * session.
	 * 
	 * @param field
	 *            the amount is added to
	 * @param amount
	 *            to be added to the given time frame
	 */
	@Override
	public void setCalendarStart(int field, int amount) {
		this.timeFrame.setCalendarStart(field, amount);
	}

	/**
	 * forward {@link TimeFrame#setCalendarEnd(int, int)} of requested session.
	 * 
	 * @param field
	 *            the amount is added to
	 * @param amount
	 *            to be added to the given time frame
	 */
	@Override
	public void setCalendarEnd(int field, int amount) {
		this.timeFrame.setCalendarEnd(field, amount);
	}

	/**
	 * return true, if {@link TimeFrame#startsInPast()} of requested session.
	 * 
	 * @return true, if {@link TimeFrame#startsInPast()} of requested session.
	 */
	@Override
	public boolean startsInPast() {
		return this.timeFrame.startsInPast();
	}

	/**
	 * return true, if
	 * {@link TimeFrame#overlaps(de.knurt.heinzelmann.util.time.TimeFrame)} of
	 * requested session.
	 * 
	 * @param timeFrame
	 *            to check
	 * @return true, if
	 *         {@link TimeFrame#overlaps(de.knurt.heinzelmann.util.time.TimeFrame)}
	 *         of requested session.
	 */
	@Override
	public boolean overlaps(TimeFrame timeFrame) {
		return this.timeFrame.overlaps(timeFrame);
	}

	/**
	 * return {@link TimeFrame#getStart()} of requested session.
	 * 
	 * @return {@link TimeFrame#getStart()} of requested session.
	 */
	@Override
	public long getStart() {
		return this.timeFrame.getStart();
	}

	/**
	 * return {@link TimeFrame#getDateStart()} of requested session.
	 * 
	 * @return {@link TimeFrame#getDateStart()} of requested session.
	 */
	@Override
	public Date getDateStart() {
		return this.timeFrame.getDateStart();
	}

	/**
	 * return {@link TimeFrame#getCalendarStart()} of requested session.
	 * 
	 * @return {@link TimeFrame#getCalendarStart()} of requested session.
	 */
	@Override
	public Calendar getCalendarStart() {
		return this.timeFrame.getCalendarStart();
	}

	/**
	 * return {@link TimeFrame#getCalendarEnd()} of requested session.
	 * 
	 * @return {@link TimeFrame#getCalendarEnd()} of requested session.
	 */
	@Override
	public Calendar getCalendarEnd() {
		return this.timeFrame.getCalendarEnd();
	}

	/**
	 * return {@link TimeFrame#getDateEnd()} of requested session.
	 * 
	 * @return {@link TimeFrame#getDateEnd()} of requested session.
	 */
	@Override
	public Date getDateEnd() {
		return this.timeFrame.getDateEnd();
	}

	/**
	 * set {@link TimeFrame#setStartEnd(long, long)} of requested session.
	 * 
	 * @param start
	 *            of {@link TimeFrame#setStartEnd(long, long)}
	 * @param end
	 *            of {@link TimeFrame#setStartEnd(long, long)}
	 */
	@Override
	public void setStartEnd(long start, long end) {
		this.timeFrame.setStartEnd(start, end);
	}

	/**
	 * set
	 * {@link TimeFrame#setStartEnd(de.knurt.heinzelmann.util.time.TimeFrame)}
	 * of requested session.
	 * 
	 * @param timeFrame
	 *            of
	 *            {@link TimeFrame#setStartEnd(de.knurt.heinzelmann.util.time.TimeFrame)}
	 */
	@Override
	public void setStartEnd(TimeFrame timeFrame) {
		this.timeFrame.setStartEnd(timeFrame);
	}

	/**
	 * return {@link TimeFrame#getDateEnd()} of requested session.
	 * 
	 * @return {@link TimeFrame#getDateEnd()} of requested session.
	 */
	@Override
	public long getEnd() {
		return this.timeFrame.getEnd();
	}

	/**
	 * set {@link TimeFrame#setEnd(long)} of requested session.
	 * 
	 * @param end
	 *            of {@link TimeFrame#setEnd(long)}
	 */
	@Override
	public void setEnd(long end) {
		this.timeFrame.setEnd(end);
	}

	/**
	 * set {@link TimeFrame#setStart(long)} of requested session.
	 * 
	 * @param start
	 *            of {@link TimeFrame#setStart(long)}
	 */
	@Override
	public void setStart(long start) {
		this.timeFrame.setStart(start);
	}

	/**
	 * return {@link TimeFrame#getDuration()} of requested session.
	 * 
	 * @return {@link TimeFrame#getDuration()} of requested session.
	 */
	@Override
	public long getDuration() {
		return this.timeFrame.getDuration();
	}

	/**
	 * return {@link TimeFrame#endsInPast()} of requested session.
	 * 
	 * @return {@link TimeFrame#endsInPast()} of requested session.
	 */
	@Override
	public boolean endsInPast() {
		return this.timeFrame.endsInPast();
	}

	/**
	 * return
	 * {@link TimeFrame#compareTo(de.knurt.heinzelmann.util.time.TimeFrame)} of
	 * requested session.
	 * 
	 * @param o
	 *            to compare
	 * @return {@link TimeFrame#compareTo(de.knurt.heinzelmann.util.time.TimeFrame)}
	 *         of requested session.
	 */
	@Override
	public int compareTo(TimeFrame o) {
		return this.timeFrame.compareTo(o);
	}

	/**
	 * forward {@link TimeFrame#addEnd(int, int)} of requested session.
	 * 
	 * @param field
	 *            for {@link TimeFrame#addEnd(int, int)}
	 * @param amount
	 *            for {@link TimeFrame#addEnd(int, int)}
	 */
	@Override
	public void addEnd(int field, int amount) {
		this.timeFrame.addEnd(field, amount);
	}

	/**
	 * forward {@link TimeFrame#addStart(int, int)} of requested session.
	 * 
	 * @param field
	 *            for {@link TimeFrame#addStart(int, int)}
	 * @param amount
	 *            for {@link TimeFrame#addStart(int, int)}
	 */
	@Override
	public void addStart(int field, int amount) {
		this.timeFrame.addStart(field, amount);
	}

	/**
	 * set {@link TimeFrame#setStart(java.util.Calendar)} of requested session.
	 * 
	 * @param calendar
	 *            of {@link TimeFrame#setStart(java.util.Calendar)}
	 */
	@Override
	public void setStart(Calendar calendar) {
		this.timeFrame.setStart(calendar);
	}

	/**
	 * set {@link TimeFrame#setEnd(java.util.Calendar)} of requested session.
	 * 
	 * @param calendar
	 *            of {@link TimeFrame#setEnd(java.util.Calendar)}
	 */
	@Override
	public void setEnd(Calendar calendar) {
		this.timeFrame.setEnd(calendar);
	}

	/**
	 * set {@link TimeFrame#setStart(Date)} of requested session.
	 * 
	 * @param start
	 *            of {@link TimeFrame#setStart(Date)}
	 */
	@Override
	public void setStart(Date start) {
		this.timeFrame.setStart(start);
	}

	/**
	 * set {@link TimeFrame#setEnd(Date)} of requested session.
	 * 
	 * @param calendar
	 *            of {@link TimeFrame#setEnd(Date)}
	 */
	@Override
	public void setEnd(Date end) {
		this.timeFrame.setEnd(end);
	}

	/**
	 * return the time frame requested with this time booking.
	 * 
	 * @return the time frame requested with this time booking.
	 */
	@Override
	public TimeFrame getSessionTimeFrame() {
		return this.timeFrame;
	}

	/**
	 * after purchasing this booking (which means, it is booked), return a
	 * message for it.
	 * 
	 * @return return a message after purchasing this booking
	 */
	@Override
	public String getMessageAfterTryingToPurchase() {
		return this.messageAfterTryingToPurchase;
	}

	private String messageAfterTryingToPurchase;

	/**
	 * return true, if this booking has been purchased (which means set as
	 * booked or applied). before purchasing it, check, if it is still available
	 * and generate the message for it.
	 * 
	 * @return true, if this booking has been purchased (which means set as
	 *         booked or applied).
	 */
	@Override
	public boolean purchase() {
		boolean result = false;
		this.messageAfterTryingToPurchase = "";
		if (!this.startsInPast() && this.isCompletelyAvailable()) {
			if (FamAuth.hasRight(this.getUser(), FamAuth.DIRECT_BOOKING, this.getFacility())) {
				this.setBooked();
			} else {
				this.setApplied();
			}

			try {
				this.insert();
				String format = "We got your %s for %s on<br />%s"; // INTLANG
				this.messageAfterTryingToPurchase = String.format(format, this.isApplication() ? "Application" : "Booking", FamText.facilityNameWithCapacityUnits(this), FamDateFormat.getDateFormattedWithTime(this, true)); // INTLANG
				result = true;
			} catch (DataIntegrityViolationException ex) {
			}
		} else if (!this.isCompletelyAvailable()) {
			this.messageAfterTryingToPurchase = "The time slot, you are queried for, has just been booked by someone else"; // INTLANG
		} else { // wait to long
			this.messageAfterTryingToPurchase = "The time slot, you are queried for, is past in meanwhile."; // INTLANG
		}

		return result;
	}

	/**
	 * set it processed and send a mail to the user.
	 */
	@Override
	public void processSession() {
		this.setProcessed();
		this.update(false);
		OutgoingUserMailBox.insert_BookingProcessed(this);
	}

	/** {@inheritDoc} */
	@Override
	public List<Booking> getConflicts() {
		return ApplicationConflicts.getInstance().getConflicts(this);
	}
}

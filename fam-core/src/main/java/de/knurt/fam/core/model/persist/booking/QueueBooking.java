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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.config.QueueBasedBookingRule;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.BookingDao;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.util.booking.BookingIsAvailableDeciderProxy;
import de.knurt.fam.core.util.mail.OutgoingUserMailBox;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * a facility where you have to join a queue for, is booked with this. this
 * {@link Booking} made by rules of a {@link QueueBasedBookingRule}.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090924
 */
public class QueueBooking extends AbstractBooking implements Comparable<QueueBooking> {

	/**
	 * return an empty {@link QueueBooking}. this must only be used for db
	 * queries by example.
	 * 
	 * @return an empty {@link QueueBooking}.
	 */
	public static QueueBooking getBooking4Query() {
		return new QueueBooking();
	}

	/** {@inheritDoc} */
	@Override
	public String getArticleNumber() {
		return this.getUsername() + this.getCapacityUnits() + this.getFacilityKey();
	}

	/**
	 * return an empty {@link QueueBooking}. this must only be used for db
	 * queries by example.
	 * 
	 * @param sessionStart
	 *            real session time frame start
	 * @param sessionEnd
	 *            real session time frame end
	 * @return an empty {@link QueueBooking}.
	 */
	public static QueueBooking getBooking4Query(long sessionStart, long sessionEnd) {
		QueueBooking result = getBooking4Query();
		result.setRealSessionTimeFrame(sessionStart, sessionEnd);
		return result;
	}

	private QueueBooking() {
	}

	/**
	 * construct it with given user for given facility. - status is set to
	 * booked, if user has right for direct booking. otherwise to applied. -
	 * capacity units of booking is set to min bookable.
	 * 
	 * @see BookingStatus#STATUS_UNSET
	 * @see BookingRule#getMinBookableCapacityUnits()
	 * @see FamAuth#DIRECT_BOOKING
	 * @param user
	 * @param facility
	 */
	public QueueBooking(User user, FacilityBookable facility) {
		this.setUsername(user.getUsername());
		this.setFacilityKey(facility.getKey());
		if (user.hasRight(FamAuth.DIRECT_BOOKING, facility)) {
			this.setBooked();
		} else {
			this.setApplied();
		}
		this.setBookingRule(facility.getBookingRule());
		this.setCapacityUnits(facility.getBookingRule().getMinBookableCapacityUnits(user));
	}

	/**
	 * return true, if it is possible to queue. it is not possible, if the max
	 * length of the queue is reached
	 * 
	 * @return true, if it is possible to queue.
	 */
	@Override
	public boolean isAvailableForInsertion() {
		return BookingIsAvailableDeciderProxy.me().getDeciderFactory().get(this.getFacility()).isAvailableForInsertion(this);
	}

	/**
	 * return the real made session time frame (always past)
	 * 
	 * @return the real made session time frame (always past)
	 */
	@Override
	public TimeFrame getSessionTimeFrame() {
		return this.realSessionTimeFrame;
	}

	/**
	 * set the session time frame of the queue booking. set <code>null</code> if
	 * session is not made yet.
	 * 
	 * @param tf
	 *            time frame of the session
	 */
	public void setSessionTimeFrame(TimeFrame tf) {
		this.realSessionTimeFrame = tf;
	}

	private TimeFrame realSessionTimeFrame = null;

	private void setRealSessionTimeFrame(long msStart, long msEnd) {
		this.realSessionTimeFrame = new SimpleTimeFrame(msStart, msEnd);
	}

	/** {@inheritDoc} */
	@Override
	public void cancel(Cancelation cancelation) {
		this.setCancelation(cancelation);
		this.update();
		OutgoingUserMailBox.insert_BookingCancelation(this);
		this.getQueueBasedBookingRule().reduceQueue();
	}

	/** {@inheritDoc} */
	@Override
	public boolean sessionAlreadyMade() {
		return this.sessionAlreadyBegun() && this.realSessionTimeFrame.getDuration() != 0;
	}

	/** {@inheritDoc} */
	@Override
	public boolean sessionAlreadyBegun() {
		return this.realSessionTimeFrame != null;
	}

	/**
	 * return the time frame when the session is expected or a now-pointer, if
	 * this is not part of the queue. in other words, return
	 * <code>new SimpleTimeFrame(this.getExpectedSessionStart(), this.getExpectedSessionEnd())</code>
	 * 
	 * @see #getExpectedSessionEnd()
	 * @see #getExpectedSessionStart()
	 * @see SimpleTimeFrame
	 * @return the time frame when the session is expected or a now-pointer, if
	 *         this is not part of the queue.
	 */
	public TimeFrame getExpectedSessionTimeFrame() {
		return new SimpleTimeFrame(this.getExpectedSessionStart(), this.getExpectedSessionEnd());
	}

	/**
	 * insert into db
	 * 
	 * @throws DataIntegrityViolationException
	 *             if it violates db rules
	 */
	@Override
	public boolean insert() throws DataIntegrityViolationException {
		FamDaoProxy.bookingDao().insert(this);
		this.getQueueBasedBookingRule().incrementQueue();
		return OutgoingUserMailBox.insert_BookingMade(this);
	}

	public QueueBasedBookingRule getQueueBasedBookingRule() {
		return (QueueBasedBookingRule) this.getBookingRule();
	}

	/**
	 * return the expected start of the session. if the session already made or
	 * the session is canceled, return null.
	 * 
	 * @return the expeted start of the session
	 */
	public Calendar getExpectedSessionStart() {
		Calendar result = null;
		Integer aqp = this.getCurrentQueuePosition();
		if (aqp != null) {
			result = Calendar.getInstance();
			if (aqp > 0) {
				aqp--; // -1: 1st position is next
				result.add(Calendar.MINUTE, aqp * 60 / this.getQueueBasedBookingRule().getUnitsPerHourProcessed());
			}
		}
		return result;
	}

	/**
	 * return the expected end of the session if the session already made or the
	 * session is canceled, return null.
	 * 
	 * @return the expeted end of the session
	 */
	public Calendar getExpectedSessionEnd() {
		Calendar result = this.getExpectedSessionStart();
		if (result != null) {
			result.add(Calendar.MINUTE, 60 / this.getQueueBasedBookingRule().getUnitsPerHourProcessed());
		}
		return result;
	}

	/**
	 * updateAnswers in db
	 * 
	 * @throws DataIntegrityViolationException
	 *             if it violates db rules
	 */
	@Override
	public boolean update() throws DataIntegrityViolationException {
		return FamDaoProxy.bookingDao().update(this);
	}

	/**
	 * return success message, because never fail
	 * 
	 * @return success message, because never fail
	 */
	@Override
	public String getMessageAfterTryingToPurchase() {
		return "You have just queued up for " + this.getFacility().getLabel() + "."; // INTLANG
	}

	/**
	 * never fail, even if it is not available anymore.
	 * 
	 * @return true, because never fail.
	 */
	@Override
	public boolean purchase() {
		this.insert();
		return true;
	}

	/**
	 * return the position in the queue of this booking. if this is not a part
	 * of the current queue return queue length.
	 * 
	 * @see BookingDao#getCurrentPositionInQueue(de.knurt.fam.core.model.persist.booking.QueueBooking)
	 * @return the position in the queue of this booking.
	 */
	public Integer getCurrentQueuePosition() {
		return FamDaoProxy.bookingDao().getCurrentPositionInQueue(this);
	}

	/**
	 * stop the session of the booking now. update session after starting.
	 */
	public void stopSession() {
		long start = this.realSessionTimeFrame.getStart();
		long end = new Date().getTime();
		this.setRealSessionTimeFrame(start, end);
		this.update();
		this.getQueueBasedBookingRule().reduceQueue();
		OutgoingUserMailBox.insert_BookingProcessed(this);
	}

	/**
	 * start the session of the booking now. update session after starting.
	 */
	public void startSession() {
		long startAndEnd = new Date().getTime();
		this.setRealSessionTimeFrame(startAndEnd, startAndEnd);
		this.update();
	}

	/**
	 * process the session. means set and update real session time frame. if
	 * session has been started, take this start point. otherwise take
	 * {@link QueueBasedBookingRule#getUnitsPerHourProcessed()} for the duration
	 * and go back in time from now.
	 */
	@Override
	public void processSession() {
		Long start = null;
		if (this.sessionAlreadyBegun()) {
			start = this.realSessionTimeFrame.getStart();
		} else {
			Integer uphp = this.getQueueBasedBookingRule().getUnitsPerHourProcessed();
			uphp = uphp == null ? 1 : uphp; // nothing set? take 1 hour
			Calendar startc = Calendar.getInstance();

			startc.add(Calendar.MINUTE, 60 / uphp * -1);
			start = startc.getTimeInMillis();
		}
		long end = new Date().getTime();
		this.setRealSessionTimeFrame(start.longValue(), end);
		this.setProcessed();
		this.update();
		this.getQueueBasedBookingRule().reduceQueue();
		OutgoingUserMailBox.insert_BookingProcessed(this);
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(QueueBooking o) {
		return this.getSeton().compareTo(o.getSeton());
	}

	/**
	 * queue bookings never conflicting
	 */
	@Override
	public List<Booking> getConflicts() {
		return new ArrayList<Booking>();
	}
}

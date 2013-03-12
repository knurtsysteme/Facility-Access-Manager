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
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.config.TimeBasedBookingRule;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.BookingStatus;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.heinzelmann.util.shopping.Purchasable;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * before booking a time slot, any user must request a booking with this object.
 * this class has a double function. on the one hand, it searches for free time
 * slots and, as a next step, it is the base for a concrete booking. in other
 * words use this class for this purposes:
 * <ul>
 * <li>user requested a facility for a specific time</li>
 * <li>answer question: what is, when user would request a facility at a
 * specific time?</li>
 * <li>what are requests resulting in positive answers?</li>
 * </ul>
 * 
 * @see TimeBooking#getNewBooking(de.knurt.fam.core.util.booking.TimeBookingRequest)
 * @author Daniel Oltmanns
 * @since 0.20090624
 */
public class TimeBookingRequest implements Purchasable, Cloneable {

	private BookingRule bookingRule;
	private User user;
	private int requestedCapacityUnits;
	private int requestedTimeUnits;
	private Calendar requestedStartTime;

	/**
	 * clone it (without exception) and return
	 * 
	 * @return the clone
	 */
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error("implements Cloneable!");
		}
	}

	/**
	 * construct a time booking request. set all facts for this requests.
	 * 
	 * @param bookingRule
	 *            used to book the facility
	 * @param user
	 *            requesting the booking
	 * @param capacityUnitsInterestedIn
	 *            capacity units requested
	 * @param timeUnitsInterestedIn
	 *            time units requested
	 * @param startTimeInterestedIn
	 *            the start time requested
	 */
	public TimeBookingRequest(BookingRule bookingRule, User user, int capacityUnitsInterestedIn, int timeUnitsInterestedIn, Calendar startTimeInterestedIn) {
		this.bookingRule = bookingRule;
		this.user = user;
		this.requestedCapacityUnits = capacityUnitsInterestedIn;
		this.requestedTimeUnits = timeUnitsInterestedIn;
		startTimeInterestedIn.set(Calendar.MILLISECOND, 0);
		startTimeInterestedIn.set(Calendar.SECOND, 0);
		this.requestedStartTime = startTimeInterestedIn;
	}

	/**
	 * return true, if the request is valid and available.
	 * 
	 * @see TimeBooking#isAvailableForInsertion()
	 * @return true, if the request is valid and available
	 */
	public boolean isAvailable() {
		return this.isValidRequest() && TimeBooking.getNewBooking(this).isAvailableForInsertion();
	}

	/**
	 * return free time slots for the requested day. if there are less hits then
	 * needed, search next day. if there are more hits then allowed, cut result
	 * set.
	 * 
	 * @param minTimeSlots
	 *            min time slots needed
	 * @param maxTimeSlots
	 *            max time slots allowed
	 * @return free time slots for the requested day.
	 */
	public List<TimeBookingRequest> getFreeBookingRequestsOfDay(int minTimeSlots, int maxTimeSlots) {
		List<TimeBookingRequest> freeTimeSlots = this.getFreeBookingRequestsStartingSameDay();

		if (freeTimeSlots.size() < minTimeSlots) {
			int nextDayCount = 1;
			List<TimeBookingRequest> tmp = this.getFreeBookingRequestsOfNextDay(nextDayCount);
			while (freeTimeSlots.size() < minTimeSlots) {
				freeTimeSlots.addAll(tmp);
				tmp = this.getFreeBookingRequestsOfNextDay(++nextDayCount);
			}
			freeTimeSlots.subList(minTimeSlots - 1, freeTimeSlots.size() - 1).clear(); // cut
			// to
			// min
		} else if (freeTimeSlots.size() > maxTimeSlots) { // to many time slots
			freeTimeSlots.subList(maxTimeSlots - 1, freeTimeSlots.size() - 1).clear(); // cut
			// to
			// max
		}
		return freeTimeSlots;
	}

	/**
	 * return true, if the requested time frame of <code>other</code> overlaps
	 * with this requested time frame.
	 * 
	 * @see #getRequestedTimeFrame()
	 * @param other
	 *            to check
	 * @return true, if the requested time frame of <code>other</code> overlaps
	 *         with this requested time frame.
	 */
	public boolean overlaps(TimeBookingRequest other) {
		return this.getRequestedTimeFrame().overlaps(other.getRequestedTimeFrame());
	}

	/**
	 * return true, if the requested start time is on the same day of year on
	 * <code>this</code> and <code>given</code>. the year does not matter, so
	 * this is true on requested start times: 3.5.2008 and 3.5.1981
	 * 
	 * @param given
	 *            compared to
	 * @return true, if the requested start time is on the same day of year on
	 *         <code>this</code> and <code>given</code>.
	 */
	public boolean startsOnSameDayAs(TimeBookingRequest given) {
		return this.getRequestedStartTime().get(Calendar.DAY_OF_YEAR) == given.getRequestedStartTime().get(Calendar.DAY_OF_YEAR);
	}

	private TimeBookingRequest getConcreteBookingRequest(TimeFrame fromTo) {
		return new TimeBookingRequest(bookingRule, user, requestedCapacityUnits, requestedTimeUnits, fromTo.getCalendarStart());
	}

	/**
	 * return the facility, this request is for.
	 * 
	 * @return the facility, this request is for.
	 */
	public FacilityBookable getFacility() {
		return this.bookingRule.getFacility();
	}

	/**
	 * return a time frame starting at {@link #requestedStartTime} and ending
	 * after {@link #requestedTimeUnits}
	 * 
	 * @return a time frame starting at {@link #requestedStartTime} and ending
	 *         after {@link #requestedTimeUnits}
	 */
	public TimeFrame getRequestedTimeFrame() {
		Calendar end = (Calendar) this.getRequestedStartTime().clone();
		end.add(Calendar.MINUTE, this.getRequestedDurationInMinutes());
		return new SimpleTimeFrame(this.getRequestedStartTime(), end);
	}

	/**
	 * return the entire day as requested
	 * 
	 * @return the entire day as requested
	 */
	private Calendar getCalendarOfRequestedDay() {
		Calendar day = (Calendar) this.getRequestedStartTime().clone();
		day.set(Calendar.HOUR_OF_DAY, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		day.set(Calendar.MILLISECOND, 0);
		return day;
	}

	/**
	 * return all free time slots of the day, the user is interested in.
	 * 
	 * it is a free time slot if:<br />
	 * <ul>
	 * <li>{@link FacilityAvailability} is
	 * {@link FacilityAvailability#COMPLETE_AVAILABLE}</li>
	 * <li>the {@link TimeBooking} IS NOT {@link BookingStatus#STATUS_APPLIED}
	 * or {@link BookingStatus#STATUS_BOOKED}</li>
	 * <li>the {@link TimeBooking} IS {@link BookingStatus#STATUS_APPLIED} or
	 * {@link BookingStatus#STATUS_BOOKED} BUT the {@link BookingRule} has
	 * enough capacity units ({@link BookingRule#getMaxBookableCapacityUnits()})
	 * left</li>
	 * </ul>
	 * 
	 * return <code>null</code> on in invalid requests (asking for too many or
	 * less units).
	 * 
	 * @return all free time slots of the day, the user is interested in.
	 */
	public List<TimeFrame> getFreeTimeSlotsOfDay() {
		ArrayList<TimeFrame> result = null;
		if (this.isValidRequest()) {

			// result is not null but empty
			result = new ArrayList<TimeFrame>();

			// get candidates
			ArrayList<TimeFrame> candidates = this.getCandidatesForFreeTimeSlotsOfDay();
			TimeBooking bookingTry = TimeBooking.getNewBooking(this);

			// check every single candidate
			for (TimeFrame candidate : candidates) {
				if (bookingTry.isAvailableForInsertion(candidate)) {
					result.add(candidate);
				}
			}
		}
		return result;
	}

	/**
	 * return BookingRequests instead of TimeFrames and do the same as
	 * {@link TimeBookingRequest#getFreeTimeSlotsOfDay()}.
	 * 
	 * @return BookingRequests instead of TimeFrames and do the same as
	 *         {@link TimeBookingRequest#getFreeTimeSlotsOfDay()}.
	 */
	public List<TimeBookingRequest> getFreeBookingRequestsStartingSameDay() {
		List<TimeBookingRequest> result = new ArrayList<TimeBookingRequest>();
		List<TimeFrame> tfs = this.getFreeTimeSlotsOfDay();
		assert tfs != null;
		for (TimeFrame tf : tfs) {
			result.add(this.getConcreteBookingRequest(tf));
		}
		return result;
	}

	/**
	 * return all free time slots candidates starting on requested day.
	 * 
	 * the free time slot candidates are simply the requested day sliced in
	 * pieces as given in the requested time units.
	 * 
	 * steps are used as defined in {@link #getDefaultMinutesOfOneSteps()) or,
	 * if it is smaller, in {@link BookingRule#getSmallestMinutesBookable}
	 * 
	 * the duration between the slices is as defined in the booking rule.
	 * 
	 * @see BookingRule#smallestMinutesBookable
	 * @return all free time slots candidates for the requested day.
	 */
	private ArrayList<TimeFrame> getCandidatesForFreeTimeSlotsOfDay() {
		ArrayList<TimeFrame> candidates = new ArrayList<TimeFrame>();
		int minuteSteps = getMinutesOfOneStep(this.bookingRule);
		if (Calendar.getInstance().before(this.getCalendarOfRequestedDay()) || this.isRequest42day()) {
			Calendar start = this.getCalendarOfRequestedDay();
			if (this.getBookingRule().getMustStartAt() != null) {
				start.set(Calendar.MINUTE, this.getBookingRule().getMustStartAt().intValue());
			}

			Calendar end = (Calendar) start.clone();
			int requestedMinutes = this.getRequestedDurationInMinutes();
			end.add(Calendar.MINUTE, requestedMinutes);
			TimeFrame candidate = new SimpleTimeFrame(start, end);

			start.add(Calendar.DAY_OF_YEAR, 1);
			long stopOn = start.getTimeInMillis();
			start.add(Calendar.DAY_OF_YEAR, -1);

			if (this.isRequest42day()) {
				while (candidate.getCalendarStart().before(Calendar.getInstance())) {
					candidate.add(Calendar.MINUTE, minuteSteps);
				}
			}

			while (candidate.getStart() < stopOn) {
				candidates.add(candidate.clone());
				candidate.add(Calendar.MINUTE, minuteSteps);
			}
		}
		return candidates;
	}

	/**
	 * return the minutes of one step. use
	 * {@link TimeBasedBookingRule#getSmallestMinutesBookable()} for that to
	 * avoid bad "booking parking".
	 * 
	 * @param br
	 *            rules using to book a facility.
	 * @return the minutes of one step.
	 */
	public static int getMinutesOfOneStep(BookingRule br) {
		return br.getSmallestMinutesBookable();
	}

	private List<TimeBookingRequest> getFreeBookingRequestsOfNextDay(int days) {
		Calendar nextDay = (Calendar) this.getCalendarOfRequestedDay().clone();
		if (this.getBookingRule().getMustStartAt() != null) {
			nextDay.set(Calendar.MINUTE, this.getBookingRule().getMustStartAt().intValue());
		}
		nextDay.add(Calendar.DAY_OF_YEAR, days);
		TimeBookingRequest br4nextDay = new TimeBookingRequest(this.bookingRule, this.user, this.requestedCapacityUnits, this.requestedTimeUnits, nextDay);
		return br4nextDay.getFreeBookingRequestsStartingSameDay();
	}

	private int getRequestedDurationInMinutes() {
		return this.getRequestedTimeUnits() * this.bookingRule.getSmallestMinutesBookable(); // minutes
	}

	/**
	 * return the booking rule used for this request.
	 * 
	 * @return the booking rule used for this request.
	 */
	public BookingRule getBookingRule() {
		return bookingRule;
	}

	/**
	 * set the booking rule used for this request.
	 * 
	 * @param bookingRule
	 *            the booking rule used for this request.
	 */
	public void setBookingRule(BookingRule bookingRule) {
		this.bookingRule = bookingRule;
	}

	/**
	 * return user requests a time booking.
	 * 
	 * @return the user requests a time booking.
	 */
	public User getUser() {
		return user;
	}

	/**
	 * set user requests a time booking.
	 * 
	 * @param user
	 *            requests a time booking
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * return capacity units requested.
	 * 
	 * @return capacity units requested.
	 */
	public int getRequestedCapacityUnits() {
		return requestedCapacityUnits;
	}

	/**
	 * set capacity units requested.
	 * 
	 * @param requestedCapacityUnits
	 *            capacity units requested.
	 */
	public void setRequestedCapacityUnits(int requestedCapacityUnits) {
		this.requestedCapacityUnits = requestedCapacityUnits;
	}

	/**
	 * return time units requested
	 * 
	 * @return the time units requested
	 */
	public int getRequestedTimeUnits() {
		return requestedTimeUnits;
	}

	/**
	 * set time units requested
	 * 
	 * @param requestedTimeUnits
	 *            time units requested
	 */
	public void setRequestedTimeUnits(int requestedTimeUnits) {
		this.requestedTimeUnits = requestedTimeUnits;
	}

	/**
	 * return true, if it is a valid request
	 * 
	 * @return true, if it is a valid request
	 */
	public boolean isValidRequest() {
		return this.hasValidTimeUnits() && this.hasValidCapacityUnits();
	}

	/**
	 * return true, if the requested time units are allowed to book. this does
	 * not check the real existing time units, that might be smaller because of
	 * bookings or crashes.
	 * 
	 * @return true, if the requested time units are allowed to book.
	 */
	public boolean hasValidTimeUnits() {
		assert this.getUser() != null;
		return this.getRequestedTimeUnits() >= this.bookingRule.getMinBookableTimeUnits(this.getUser()) && this.getRequestedTimeUnits() <= this.bookingRule.getMaxBookableTimeUnits(this.getUser());
	}

	/**
	 * return start of time requested.
	 * 
	 * @return the start of time requested
	 */
	public Calendar getRequestedStartTime() {
		return requestedStartTime;
	}

	/**
	 * start of time requested.
	 * 
	 * @param requestedStartTime
	 *            the start of time requested.
	 */
	public void setRequestedStartTime(Calendar requestedStartTime) {
		requestedStartTime.set(Calendar.MILLISECOND, 0);
		requestedStartTime.set(Calendar.SECOND, 0);
		this.requestedStartTime = requestedStartTime;
	}

	/**
	 * return true, if the requested capacity units is allowed to book in
	 * general. this does not check the current capacity units, that might differ
	 * because of bookings or crashes.
	 * 
	 * @return true, if the requested capacity units is allowed to book.
	 */
	public boolean hasValidCapacityUnits() {
		assert this.getUser() != null;
		return this.getRequestedCapacityUnits() >= this.bookingRule.getMinBookableCapacityUnits(this.getUser()) && this.getRequestedCapacityUnits() <= this.bookingRule.getMaxBookableCapacityUnits(this.getUser());

	}

	/**
	 * return true, if the request is for today (servertime)
	 * 
	 * @return true, if the request is for today (servertime)
	 */
	public boolean isRequest42day() {
		return Calendar.getInstance().get(Calendar.DAY_OF_YEAR) == this.getRequestedStartTime().get(Calendar.DAY_OF_YEAR);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		Properties p = new Properties();
		p.put("start", this.getRequestedStartTime().getTime().toString());
		p.put("time units", this.getRequestedTimeUnits());
		p.put("cap units", this.getRequestedCapacityUnits());
		p.put("user", this.getUser().getUsername());
		p.put("booking rule", this.getBookingRule().toString());
		return p.toString();
	}

	/**
	 * return true, if it is a request for yesterdays but not today!
	 * 
	 * @return true, if it is a request for yesterdays but not today!
	 */
	public boolean isRequest4Yesterdays() {
		return this.getRequestedStartTime().before(Calendar.getInstance()) && !this.isRequest42day();
	}

	/**
	 * return the article number as it were an booking
	 * 
	 * @return the article number as it were an booking
	 */
	@Override
  public String getArticleNumber() {
		return this.getBooking().getArticleNumber();
	}

	private TimeBooking getBooking() {
		return TimeBooking.getNewBooking(this);
	}

	@Override
  public boolean purchase() {
		return this.getBooking().purchase();
	}
}

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
package de.knurt.fam.core.model.persist;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.booking.Cancelation;
import de.knurt.fam.core.persistence.dao.FacilityDao;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.heinzelmann.util.adapter.ComparableInDifferentWays;
import de.knurt.heinzelmann.util.adapter.ViewableObject;
import de.knurt.heinzelmann.util.query.Identificable;
import de.knurt.heinzelmann.util.time.IntervalTimeFrame;
import de.knurt.heinzelmann.util.time.SimpleIntervalTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * a facility is generaly available or not. this class describes a time frame
 * that is full, free or maybe available.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090424
 */
public class FacilityAvailability implements ComparableInDifferentWays, Cloneable, IntervalTimeFrame, Availability, ViewableObject, Storeable, Deletable, Comparable<FacilityAvailability>, Identificable {

	/**
	 * flag for unrestricted availability.
	 */
	public static final int COMPLETE_AVAILABLE = 0;
	/**
	 * flag for not availability in general. flag in case of facility is outside
	 * opening hours or special events.
	 */
	public static final int GENERAL_NOT_AVAILABLE = 1;
	/**
	 * this is to mark a facility that is limited available in general. this is
	 * NOT the case if any bookings are there with applications and there is no
	 * possibility to put this in by now but temporary (without persistence).
	 * 
	 * @see FacilityDao#isDataIntegrityViolation(de.knurt.fam.core.model.persist.FacilityAvailability)
	 */
	public static final int MAYBE_AVAILABLE = 2;
	/**
	 * flag for not availability because of bookings. flag in case of facility
	 * is booked up.
	 */
	public static final int BOOKED_NOT_AVAILABLE = 3;
	/**
	 * flag for not availability because of maintenance. flag in case of
	 * facility is maintained.
	 */
	public static final int MAINTENANCE_NOT_AVAILABLE = 4;
	public static final int SUDDEN_FAILURE_NOT_AVAILABLE = 5;
	/**
	 * flag for "booking must not start here".
	 */
	public static final int BOOKING_MUST_NOT_START_HERE = 6;
	private String facilityKey;
	private Date timeStampSet;
	private String usernameSetThis;
	private Integer id;
	/**
	 * set this to compare the availability by the base time frame. this is the
	 * default comparing modus.
	 * 
	 * @see #getTimeStampSet()
	 * @see #getComparingModus()
	 * @see #compareTo(de.knurt.fam.core.model.persist.FacilityAvailability)
	 */
	public static final int COMPARE_BY_BASE_PERIOD_OF_TIME = 0;
	/**
	 * set this to compare the availability by the date it was set.
	 * 
	 * @see #getTimeStampSet()
	 * @see #getComparingModus()
	 * @see #compareTo(de.knurt.fam.core.model.persist.FacilityAvailability)
	 */
	public static final int COMPARE_BY_DATE_SET = 1;
	private int comparingModus = COMPARE_BY_BASE_PERIOD_OF_TIME;

	/**
	 * construct availability for given facility and time frame. after
	 * constructing the availability is set to <code>null</code>.
	 * 
	 * @param facilityKey
	 *            representing the facility the availability is set for.
	 * @param timeFrame
	 *            base period of time the availability is set for.
	 */
	public FacilityAvailability(String facilityKey, TimeFrame timeFrame) {
		this(facilityKey, timeFrame.getCalendarStart(), timeFrame.getCalendarEnd());
	}

	/**
	 * return the date when the base period of time starts.
	 * 
	 * @see IntervalTimeFrame#getBasePeriodOfTime()
	 * @see #getBasePeriodOfTime()
	 * @return the date when the base period of time starts.
	 */
	public Date getStartOfBasePeriodOfTime() {
		return this.getBasePeriodOfTime() == null ? null : this.getBasePeriodOfTime().getDateStart();
	}

	/**
	 * set the date when the base period of time starts.
	 * 
	 * @see IntervalTimeFrame#getBasePeriodOfTime()
	 * @see #getBasePeriodOfTime()
	 * @param start
	 *            the date when the base period of time starts.
	 */
	public void setStartOfBasePeriodOfTime(Date start) {
		this.createIntervalTimeFrameNotSet();
		this.getBasePeriodOfTime().setStart(start.getTime());
	}

	/**
	 * set the date when the base period of time ends.
	 * 
	 * @see IntervalTimeFrame#getBasePeriodOfTime()
	 * @see #getBasePeriodOfTime()
	 * @param end
	 *            the date when the base period of time ends.
	 */
	public void setEndOfBasePeriodOfTime(Date end) {
		this.createIntervalTimeFrameNotSet();
		this.getBasePeriodOfTime().setEnd(end.getTime());
	}

	/**
	 * return the date when the base period of time ends.
	 * 
	 * @see IntervalTimeFrame#getBasePeriodOfTime()
	 * @see #getBasePeriodOfTime()
	 * @return the date when the base period of time ends.
	 */
	public Date getEndOfBasePeriodOfTime() {
		return this.getBasePeriodOfTime() == null ? null : this.getBasePeriodOfTime().getDateEnd();
	}

	/**
	 * @return the userSetThis
	 */
	public User getUserSetThis() {
		return FamDaoProxy.userDao().getUserFromUsername(this.usernameSetThis);
	}

	/**
	 * set the user that is responsible for this availability. it can be set
	 * directly (if an operator set a maintenance) or indirectly (if a user
	 * overbook another booking).
	 * 
	 * @param user
	 *            the user that is responsible for this availability.
	 */
	public void setUserSetThis(User user) {
		this.usernameSetThis = user.getUsername();
	}

	/**
	 * set username of user that set the availability for the facility.
	 * 
	 * @param usernameSetThis
	 *            username of user that set the availability for the facility.
	 */
	public void setUsernameSetThis(String usernameSetThis) {
		this.usernameSetThis = usernameSetThis;
	}

	/**
	 * return the user that is responsible for this availability. it can be set
	 * directly (if an operator set a maintenance) or indirectly (if a user
	 * overbook another booking).
	 * 
	 * @return the user that is responsible for this availability.
	 */
	public String getUsernameSetThis() {
		return this.usernameSetThis;
	}

	/**
	 * return true, if the given integer is one of the class constants
	 * 
	 * @param availability
	 *            to check
	 * @return true, if the given integer is one of static constants
	 */
	public static boolean isValidAvailability(int availability) {
		return availability >= 0 && availability <= 6;
	}

	private Integer available;
	private String notice;
	private IntervalTimeFrame intervalTimeFrame;

	/**
	 * constructur mainly to construct examples for queries. call super default
	 * constructor. after constructing the availability is set to
	 * <code>null</code>.
	 */
	public FacilityAvailability() {
		super();
	}

	/**
	 * return true, if a notice exist. notice must not be null or empty.
	 * 
	 * @return true, if a notice exist.
	 */
	public boolean hasNotice() {
		return this.isNotice(this.notice);
	}

	/**
	 * construct availability for given facility and time frame. after
	 * constructing the availability is set to <code>null</code>.
	 * 
	 * @param facilityKey
	 *            representing the facility the availability is set for.
	 * @param start
	 *            of base period of time.
	 * @param end
	 *            of base period of time.
	 */
	public FacilityAvailability(String facilityKey, Calendar start, Calendar end) {
		this.intervalTimeFrame = new SimpleIntervalTimeFrame(start, end);
		this.facilityKey = facilityKey;
		this.timeStampSet = new Date();
	}

	/** {@inheritDoc} */
	@Override
	public boolean insert() throws DataIntegrityViolationException {
		boolean result = FamDaoProxy.facilityDao().insert(this);
		Cancelation c = new Cancelation(this.getUserSetThis(), Cancelation.REASON_NOT_AVAILABLE_IN_GENERAL);
		FamDaoProxy.bookingDao().cancelOverlappingBookings(this.getFacility(), c);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean delete() throws DataIntegrityViolationException {
		boolean result = FamDaoProxy.facilityDao().delete(this);
		Cancelation c = new Cancelation(this.getUserSetThis(), Cancelation.REASON_NOT_AVAILABLE_IN_GENERAL);
		FamDaoProxy.bookingDao().cancelOverlappingBookings(this.getFacility(), c);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean update() throws DataIntegrityViolationException {
		boolean result = FamDaoProxy.facilityDao().update(this);
		Cancelation c = new Cancelation(this.getUserSetThis(), Cancelation.REASON_NOT_AVAILABLE_IN_GENERAL);
		FamDaoProxy.bookingDao().cancelOverlappingBookings(this.getFacility(), c);
		return result;
	}

	/**
	 * @return the facilityKey
	 */
	public String getFacilityKey() {
		return facilityKey;
	}

	/**
	 * @return the facilityAvailabilityId
	 */
	public long getFacilityAvailabilityId() {
		return this.id.longValue();
	}

	/**
	 * @param facilityAvailabilityId
	 *            the facilityAvailabilityId to set
	 */
	public void setFacilityAvailabilityId(long facilityAvailabilityId) {
		this.id = (int) facilityAvailabilityId;
	}

	/**
	 * @return the timeStampSet
	 */
	public Date getTimeStampSet() {
		return timeStampSet;
	}

	/**
	 * @param timeStampSet
	 *            the timeStampSet to set
	 */
	public void setTimeStampSet(Date timeStampSet) {
		this.timeStampSet = timeStampSet;
	}

	/**
	 * @param facilityKey
	 *            the facilityKey to set
	 */
	public void setFacilityKey(String facilityKey) {
		this.facilityKey = facilityKey;
	}

	/**
	 * @return the available
	 */
	public Integer getAvailable() {
		return available;
	}

	/**
	 * return true, if it is maybe available
	 * 
	 * @see FacilityAvailability#MAYBE_AVAILABLE
	 * @return true, if it is maybe available
	 */
	public boolean isMaybeAvailable() {
		return this.availableIs(MAYBE_AVAILABLE);
	}

	private boolean availableIs(int availability) {
		return this.available != null && this.available == availability;
	}

	/**
	 * return true, if the booking cannot start here.
	 * 
	 * @return true, if the booking cannot start here.
	 */
	public boolean mustNotStartHere() {
		return this.availableIs(BOOKING_MUST_NOT_START_HERE);
	}

	/**
	 * @param available
	 *            the available to set
	 */
	public void setAvailable(Integer available) {
		this.available = available;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isCompletelyAvailable() {
		return this.availableIs(COMPLETE_AVAILABLE);
	}

	/**
	 * return true, if it is not available in general
	 * 
	 * @see FacilityAvailability#GENERAL_NOT_AVAILABLE
	 * @return true, if it is not available in general
	 */
	public boolean isNotAvailableInGeneral() {
		return this.availableIs(GENERAL_NOT_AVAILABLE);
	}

	/**
	 * return true, if it is not available because of a booking
	 * 
	 * @see FacilityAvailability#BOOKED_NOT_AVAILABLE
	 * @return true, if it is not available because of a booking
	 */
	public boolean isNotAvailableBecauseOfBooking() {
		return this.availableIs(BOOKED_NOT_AVAILABLE);
	}

	/**
	 * return true, if it is not available because of a maintenance
	 * 
	 * @see FacilityAvailability#MAINTENANCE_NOT_AVAILABLE
	 * @return true, if it is not available because of a maintenance
	 */
	public boolean isNotAvailableBecauseOfMaintenance() {
		return this.availableIs(MAINTENANCE_NOT_AVAILABLE);
	}

	/**
	 * set not available because of booking
	 * 
	 * @see FacilityAvailability#BOOKED_NOT_AVAILABLE
	 */
	public void setNotAvailableBecauseOfBooking() {
		this.available = BOOKED_NOT_AVAILABLE;
	}

	/**
	 * set not available in general
	 * 
	 * @see FacilityAvailability#GENERAL_NOT_AVAILABLE
	 */
	public void setNotAvailableInGeneral() {
		this.available = GENERAL_NOT_AVAILABLE;
	}

	/**
	 * set maybe available
	 * 
	 * @see FacilityAvailability#MAYBE_AVAILABLE
	 */
	public void setMaybeAvailable() {
		this.available = MAYBE_AVAILABLE;
	}

	/**
	 * set not available because of a maintenance
	 * 
	 * @see FacilityAvailability#MAINTENANCE_NOT_AVAILABLE
	 */
	public void setNotAvailableBecauseOfMaintenance() {
		this.available = MAINTENANCE_NOT_AVAILABLE;
	}

	/**
	 * @return the notice
	 */
	public String getNotice() {
		return this.notice;
	}

	/**
	 * @param notice
	 *            the notice to set
	 */
	public void setNotice(String notice) {
		this.notice = this.isNotice(notice) ? notice.trim() : null;
	}

	/**
	 * return the facility the availability is set for.
	 * 
	 * @return the facility the availability is set for
	 */
	public Facility getFacility() {
		return FacilityConfigDao.facility(this.getFacilityKey());
	}

	private boolean isNotice(String notize) {
		return notize != null && !notize.trim().isEmpty();
	}

	/**
	 * return same as {@link #isNotAvailableBecauseOfSuddenFailure()}
	 * 
	 * @return same as {@link #isNotAvailableBecauseOfSuddenFailure()}
	 */
	public boolean isFailure() {
		return this.isNotAvailableBecauseOfSuddenFailure();
	}

	/**
	 * set not available because of a sudden failure
	 * 
	 * @see FacilityAvailability#SUDDEN_FAILURE_NOT_AVAILABLE
	 */
	public void setNotAvailableBecauseOfSuddenFailure() {
		this.available = SUDDEN_FAILURE_NOT_AVAILABLE;
	}

	/**
	 * return true, if the availability stands for a sudden failure
	 * 
	 * @return true, if the availability stands for a sudden failure
	 */
	public boolean isNotAvailableBecauseOfSuddenFailure() {
		return this.availableIs(SUDDEN_FAILURE_NOT_AVAILABLE);
	}

	/**
	 * set completely available
	 * 
	 * @see FacilityAvailability#COMPLETE_AVAILABLE
	 */
	public void setCompletelyAvailable() {
		this.available = COMPLETE_AVAILABLE;
	}

	/** {@inheritDoc} */
	@Override
	public List<IntervalTimeFrame> getIntervalTimeFramesWithNoIteration(TimeFrame fromTo) {
		return this.getIntervalTimeFrame() == null ? new ArrayList<IntervalTimeFrame>() : this.getIntervalTimeFrame().getIntervalTimeFramesWithNoIteration(fromTo);
	}

	/**
	 * return the intervals of the availabilities in single not looped
	 * intervals. shrink the set to the availabilities overlapping the given
	 * time frame.
	 * 
	 * @see IntervalTimeFrame#getIntervalTimeFramesWithNoIteration(de.knurt.heinzelmann.util.time.TimeFrame)
	 * @param fromTo
	 *            shrink the set to the availabilities overlapping this
	 * @return the intervals of the availabilities in single not looped
	 *         intervals.
	 */
	public List<FacilityAvailability> getFacilityAvailabilitiesWithNoIteration(TimeFrame fromTo) {
		List<FacilityAvailability> result = new ArrayList<FacilityAvailability>();
		if (this.getIntervalTimeFrame() != null) {
			List<IntervalTimeFrame> itfs = this.getIntervalTimeFrame().getIntervalTimeFramesWithNoIteration(fromTo);
			for (IntervalTimeFrame itf : itfs) {
				FacilityAvailability da = this.clone();
				da.setIntervalTimeFrame(itf);
				result.add(da);
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public List<TimeFrame> getSingleSimpleTimeFrames(TimeFrame fromTo) {
		return this.getIntervalTimeFrame() == null ? new ArrayList<TimeFrame>() : this.getIntervalTimeFrame().getSingleSimpleTimeFrames(fromTo);
	}

	/** {@inheritDoc} */
	@Override
	public void setHourly() {
		this.getIntervalTimeFrame().setHourly();
	}

	/** {@inheritDoc} */
	@Override
	public void setWeekly() {
		this.getIntervalTimeFrame().setWeekly();
	}

	/**
	 * if given time frame overlaps this time frame.
	 * 
	 * @see IntervalTimeFrame#overlaps(de.knurt.heinzelmann.util.time.TimeFrame)
	 * @param timeframe
	 *            to check
	 * @return true, if the availability overlaps with given time frame.
	 */

	@Override
	public boolean overlaps(TimeFrame timeframe) {
		return this.getIntervalTimeFrame().overlaps(timeframe);
	}

	/**
	 * return true, if this facility availability is applicable to the given
	 * time frame. at most, this is, if the given time frame overlaps with this
	 * facility availability. if availability is
	 * {@link #BOOKING_MUST_NOT_START_HERE}, return true, if given time frame
	 * starts on interval time frame. Otherwise return true,
	 * 
	 * @param timeframe
	 *            to check
	 * @return true, if this facility availability is applicable to the given
	 *         time frame.
	 */
	public boolean applicableTo(TimeFrame timeframe) {
		if (this.mustNotStartHere()) {
			boolean result = false;
			for (TimeFrame overlapping : this.getSingleSimpleTimeFrames(timeframe)) {
				if (overlapping.contains(timeframe.getCalendarStart().getTime())) {
					result = true;
					break;
				}
			}
			return result;
		} else {
			return this.overlaps(timeframe);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setInterval(Integer interval) {
		this.createIntervalTimeFrameNotSet();
		this.getIntervalTimeFrame().setInterval(interval);
	}

	/** {@inheritDoc} */
	@Override
	public void setMonthly() {
		this.getIntervalTimeFrame().setMonthly();
	}

	/** {@inheritDoc} */
	@Override
	public void setYearly() {
		this.getIntervalTimeFrame().setYearly();
	}

	/** {@inheritDoc} */
	@Override
	public TimeFrame getBasePeriodOfTime() {
		return this.getIntervalTimeFrame() == null ? null : this.getIntervalTimeFrame().getBasePeriodOfTime();
	}

	/** {@inheritDoc} */
	@Override
	public void setBasePeriodOfTime(TimeFrame basePeriodOfTime) {
		if (this.getIntervalTimeFrame() == null) {
			this.setIntervalTimeFrame(new SimpleIntervalTimeFrame(basePeriodOfTime));
		} else {
			this.getIntervalTimeFrame().setBasePeriodOfTime(basePeriodOfTime);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean isOneTime() {
		return this.getIntervalTimeFrame().isOneTime();
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(TimeFrame o) {
		return this.getIntervalTimeFrame().compareTo(o);
	}

	/** {@inheritDoc} */
	@Override
	public void setCalendarStart(int field, int amount) {
		this.getIntervalTimeFrame().setCalendarStart(field, amount);
	}

	/** {@inheritDoc} */
	@Override
	public Integer getInterval() {
		return this.getIntervalTimeFrame() == null ? null : this.getIntervalTimeFrame().getInterval();
	}

	/** {@inheritDoc} */
	@Override
	public void setDaily() {
		this.getIntervalTimeFrame().setDaily();
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(FacilityAvailability o) {
		if (this.comparingModus == COMPARE_BY_DATE_SET) {
			return this.getTimeStampSet().before(o.getTimeStampSet()) ? -1 : 1;
		} else {
			return this.getBasePeriodOfTime().getDateStart().before(o.getBasePeriodOfTime().getDateStart()) ? -1 : 1;
		}
	}

	/**
	 * return a clone
	 * 
	 * @return a clone
	 */
	public IntervalTimeFrame getClone() {
		IntervalTimeFrame clone = new FacilityAvailability(this.facilityKey, this.getBasePeriodOfTime().getCalendarStart(), this.getBasePeriodOfTime().getCalendarEnd());
		clone.setInterval(this.getInterval());
		return clone;
	}

	/** {@inheritDoc} */
	@Override
	public FacilityAvailability clone() {
		try {
			FacilityAvailability result = (FacilityAvailability) super.clone();
			IntervalTimeFrame cloneditf = this.intervalTimeFrame.clone();
			cloneditf.setBasePeriodOfTime(cloneditf.getBasePeriodOfTime().clone());
			result.setIntervalTimeFrame(cloneditf);
			return result;
		} catch (CloneNotSupportedException e) {
			throw new Error("implements Cloneable!");
		}
	}

	/**
	 * @return the intervalTimeFrame
	 */
	private IntervalTimeFrame getIntervalTimeFrame() {
		return intervalTimeFrame;
	}

	/**
	 * @param intervalTimeFrame
	 *            the intervalTimeFrame to set
	 */
	private void setIntervalTimeFrame(IntervalTimeFrame intervalTimeFrame) {
		this.intervalTimeFrame = intervalTimeFrame;
	}

	private void createIntervalTimeFrameNotSet() {
		if (this.getIntervalTimeFrame() == null) {
			this.intervalTimeFrame = new SimpleIntervalTimeFrame();
			TimeFrame tf = this.intervalTimeFrame.getBasePeriodOfTime();
			tf.add(Calendar.YEAR, -1000);
			this.intervalTimeFrame.setBasePeriodOfTime(tf);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Integer getId() {
		return id;
	}

	/** {@inheritDoc} */
	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * set the time stamp set this to given unix timestamp.
	 * 
	 * @param milis
	 *            unix time stamp.
	 */
	public void setTimeStampSet(long milis) {
		this.setTimeStampSet(new Date(milis));
	}

	/**
	 * return true, if availability is not set yet.
	 * 
	 * @return true, if availability is not set yet.
	 */
	public boolean isUnset() {
		return this.available == null;
	}

	/**
	 * set booking cannot start here. if a facility must be booked for at least
	 * 2 hours, it is not available at 5pm, the facility is available at 4pm -
	 * but a booking cannot start at 4pm. this is only for temporary use of the
	 * class.
	 */
	public void setBookingMustNotStartHere() {
		this.available = BOOKING_MUST_NOT_START_HERE;
	}

	/** {@inheritDoc} */
	@Override
	public void setComparingModus(int comparingModus) {
		this.comparingModus = comparingModus;
	}

	/** {@inheritDoc} */
	@Override
	public int getComparingModus() {
		return this.comparingModus;
	}

	/**
	 * short for {@link #setBookingMustNotStartHere()}
	 */
	public void setMustNotStartHere() {
		this.setBookingMustNotStartHere();
	}
}

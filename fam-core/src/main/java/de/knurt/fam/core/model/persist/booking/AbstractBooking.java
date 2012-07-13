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

import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.config.BookingStrategy;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.util.mail.OutgoingUserMailBox;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * a booking resolving things that are the same in all bookings of the access
 * system.<br />
 * be sure, never have an User or Facility object directly as attribute here! it
 * results in saving inconsistent data.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090827
 */
public abstract class AbstractBooking implements Booking {
	private Date lastInvoiced = null;

	/** {@inheritDoc} */
	@Override
	public Date getLastInvoiced() {
		return this.lastInvoiced;
	}

	/** {@inheritDoc} */
	@Override
	public void invoice() {
		this.setLastInvoiced(new Date());
		this.update();
	}

	/** {@inheritDoc} */
	@Override
	public void setLastInvoiced(Date lastInvoiced) {
		this.lastInvoiced = lastInvoiced;
	}

	/**
	 * construct a booking for a user, a facility, with a status and capacity
	 * units and booked by rules.
	 * 
	 * @param facilityKey
	 *            representing the facility this booking is for
	 * @param username
	 *            of user booked this
	 * @param bookingStatus
	 *            the status of booking ({@link BookingStatus})
	 * @param capacityUnits
	 *            units booked
	 * @param bookingRule
	 *            rules used for this booking
	 */
	public AbstractBooking(String facilityKey, String username, BookingStatus bookingStatus, int capacityUnits, BookingRule bookingRule) {
		this.username = username;
		this.bookingStatus = bookingStatus;
		this.facilityKey = facilityKey;
		if (capacityUnits < 1 || capacityUnits > this.getFacility().getCapacityUnits()) {
			throw new DataIntegrityViolationException("invalid capacity units [200909300830]");
		}
		this.capacityUnits = capacityUnits;
		this.bookingRule = bookingRule;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isQueueBased() {
		return this.getIdBookedInBookingStrategy() == BookingStrategy.QUEUE_BASED;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isTimeBased() {
		return this.getIdBookedInBookingStrategy() == BookingStrategy.TIME_BASED;
	}

	/**
	 * send the applicant, that requested this booking, a confirmation and give
	 * him the bonus for it.
	 * 
	 * @param mailMessage
	 *            additional message to this confirmation
	 */
	@Override
	public void confirmApplication(String mailMessage) {
		this.setBooked();
		this.update();
		OutgoingUserMailBox.insert_ApplicationConfirmation(this.getUser(), this, mailMessage);
	}

	/** {@inheritDoc} */
	@Override
	public int getIdBookedInBookingStrategy() {
		if (this.idBookedInBookingStrategy == null) {
			this.idBookedInBookingStrategy = this.getBookingRule().getBookingStrategy();
		}
		return this.idBookedInBookingStrategy.intValue();
	}

	private Integer idBookedInBookingStrategy = null;

	/** {@inheritDoc} */
	@Override
	public void setIdBookedInBookingStrategy(int idBookedInBookingStrategy) {
		this.idBookedInBookingStrategy = idBookedInBookingStrategy;
	}

	/**
	 * empty constructor
	 */
	protected AbstractBooking() {
	}

	/** {@inheritDoc} */
	@Override
	public boolean overlaps(Booking otherBooking) {
		return this.overlaps(otherBooking.getSessionTimeFrame());
	}

	/** {@inheritDoc} */
	@Override
	public boolean overlaps(TimeFrame timeFrame) {
		return this.getSessionTimeFrame().overlaps(timeFrame);
	}

	/** {@inheritDoc} */
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error("implements Cloneable!");
		}
	}

	private String username, notice;
	private String facilityKey;
	private BookingStatus bookingStatus;
	private BookingRule bookingRule;
	private Integer capacityUnits;
	private Integer id;
	private Date seton;
	private Cancelation cancelation;
	private FacilityBookable tmpFacility;
	private boolean processed = false;

	/**
	 * set this booking as processed.
	 * 
	 * @see #setProcessed(boolean)
	 */
	public void setProcessed() {
		this.setProcessed(true);
	}

	/**
	 * return the facility, this booking is for.
	 * 
	 * @return the facility, this booking is for.
	 */
	@Override
	public FacilityBookable getFacility() {
		if (this.tmpFacility == null) {
			this.tmpFacility = FacilityConfigDao.bookableFacility(this.getFacilityKey());
		}
		return this.tmpFacility;
	}

	/**
	 * return -1, if this booking starts before the other booking. otherwise
	 * return 1.
	 * 
	 * @param otherBooking
	 *            other booking
	 * @return -1, if this booking starts before the other booking. otherwise
	 *         return 1.
	 */
	public int compareTo(Booking otherBooking) {
		return this.getSessionTimeFrame().getStart() < otherBooking.getSessionTimeFrame().getStart() ? -1 : 1;
	}

	/**
	 * return the user made in this booking.
	 * 
	 * @return the user made in this booking.
	 */
	@Override
	public User getUser() {
		return FamDaoProxy.userDao().getUserFromUsername(this.getUsername());
	}

	/** {@inheritDoc} */
	@Override
	public Integer getCapacityUnits() {
		return capacityUnits;
	}

	/**
	 * return true, if this booking is an application. same as
	 * <code>this.getBookingStatus().isApplied()</code>.
	 * 
	 * @see BookingStatus#isApplied()
	 * @return true, if this booking is an application.
	 */
	@Override
	public boolean isApplication() {
		return this.getBookingStatus().isApplied();
	}

	/**
	 * return true, if this booking is available
	 * 
	 * @return true, if this booking is available
	 */
	@Override
	public abstract boolean isAvailableForInsertion();

	/** {@inheritDoc} */
	@Override
	public boolean isCanceled() {
		return this.cancelation != null;
	}

	/** {@inheritDoc} */
	@Override
	public void setBooked() {
		this.bookingStatus = new BookingStatus(BookingStatus.STATUS_BOOKED);
	}

	/** {@inheritDoc} */
	@Override
	public void setUnset() {
		this.bookingStatus = new BookingStatus(BookingStatus.STATUS_UNSET);
	}

	/** {@inheritDoc} */
	@Override
	public void setApplied() {
		this.bookingStatus = new BookingStatus(BookingStatus.STATUS_APPLIED);
	}

	/** {@inheritDoc} */
	@Override
	public String getUsername() {
		return username;
	}

	/** {@inheritDoc} */
	@Override
	public void setUsername(String username) {
		this.username = username;
	}

	/** {@inheritDoc} */
	@Override
	public String getFacilityKey() {
		return facilityKey;
	}

	/** {@inheritDoc} */
	@Override
	public void setFacilityKey(String facilityKey) {
		this.facilityKey = facilityKey;
	}

	/** {@inheritDoc} */
	@Override
	public BookingStatus getBookingStatus() {
		return bookingStatus;
	}

	/** {@inheritDoc} */
	@Override
	public void setBookingStatus(BookingStatus bookingStatus) {
		this.bookingStatus = bookingStatus;
	}

	/** {@inheritDoc} */
	@Override
	public BookingRule getBookingRule() {
		return bookingRule;
	}

	/** {@inheritDoc} */
	@Override
	public void setBookingRule(BookingRule bookingRule) {
		this.bookingRule = bookingRule;
	}

	/** {@inheritDoc} */
	@Override
	public void setCapacityUnits(Integer capacityUnits) {
		this.capacityUnits = capacityUnits;
	}

	/**
	 * return true, if the booking is complete available. do the same as
	 * {@link #isAvailableForInsertion()}
	 * 
	 * @return true, if the booking is complete available.
	 */
	@Override
	public boolean isCompletelyAvailable() {
		return this.isAvailableForInsertion();
	}

	/** {@inheritDoc} */
	@Override
	public Integer getId() {
		return this.id;
	}

	/** {@inheritDoc} */
	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	/** {@inheritDoc} */
	@Override
	public Date getSeton() {
		return seton;
	}

	/** {@inheritDoc} */
	@Override
	public void setSeton(Date seton) {
		this.seton = seton;
	}

	/** {@inheritDoc} */
	@Override
	public Cancelation getCancelation() {
		return cancelation;
	}

	/** {@inheritDoc} */
	@Override
	public void setCancelation(Cancelation cancelation) {
		this.cancelation = cancelation;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isBooked() {
		return this.getBookingStatus().isBooked();
	}

	/** {@inheritDoc} */
	@Override
	public String getNotice() {
		return notice;
	}

	/** {@inheritDoc} */
	@Override
	public void setNotice(String notice) {
		this.notice = notice == null ? null : notice.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}

	/** {@inheritDoc} */
	@Override
	public boolean isProcessed() {
		return processed;
	}

	/** {@inheritDoc} */
	@Override
	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	/** {@inheritDoc} */
	@Override
	public boolean delete() {
		return FamDaoProxy.bookingDao().delete(this);
	}
}

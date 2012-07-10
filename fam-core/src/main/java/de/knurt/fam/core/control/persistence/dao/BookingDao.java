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
package de.knurt.fam.core.control.persistence.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.BookingStrategy;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.BookingStatus;
import de.knurt.fam.core.model.persist.booking.Cancelation;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * A dao accessing stored {@link TimeBooking}s
 * 
 * @author Daniel Oltmanns
 * @since 0.20090625
 */
public abstract class BookingDao extends AbstractFamDao<Booking> {

	/**
	 * return a list with uncancelled bookings and application for a given
	 * facility on a specific day.
	 * 
	 * @param facility
	 *            given
	 * @param c
	 *            calendar representing the day
	 * @return a list with uncancelled bookings and application for a given
	 *         facility on a specific day.
	 */
	public List<Booking> getAllUncanceledBookingsAndApplicationsOfDay(FacilityBookable facility, Calendar c) {
		Calendar start = (Calendar) c.clone();
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		Calendar end = (Calendar) start.clone();
		end.add(Calendar.DAY_OF_YEAR, 1);
		return this.getUncanceledBookingsAndApplicationsIn(facility, new SimpleTimeFrame(start, end));
	}

	/**
	 * return all bookings for the given facility that are overlapping the given
	 * timeframe. that are all bookings, where the date AND the end of booking
	 * IS NOT before OR after timeFrame. do not sort out bookings that are only
	 * applications.
	 * 
	 * @param facility
	 *            asking for
	 * @param timeFrame
	 *            asking for
	 * @return all bookings for the given facility that are overlapping the
	 *         given timeframe.
	 */
	public abstract List<Booking> getUncanceledBookingsAndApplicationsIn(FacilityBookable facility, TimeFrame timeFrame);

	/**
	 * return all bookings for the given facility that are overlapping the given
	 * timeframe. do not sort out bookings that are only applications.
	 * 
	 * @param facility
	 *            asking for
	 * @param timeFrame
	 *            booking shall be overlapping with
	 * @return all bookings for the given facility that are overlapping the
	 *         given timeframe.
	 */
	public abstract List<Booking> getUncanceledBookingsAndApplicationsIn(FacilityBookable facility, FacilityAvailability timeFrame);

	/**
	 * return all bookings for the given facility that are overlapping the given
	 * timeframe and starts in future. do not sort out bookings that are only
	 * applications.
	 * 
	 * @see java.util.Date
	 * @see de.knurt.heinzelmann.util.time.IntervalTimeFrame
	 * @see BookingDao#getUncanceledBookingsAndApplicationsIn(de.knurt.fam.core.model.config.FacilityBookable,
	 *      de.knurt.fam.core.model.persist.FacilityAvailability)
	 * @param facility
	 *            asking for
	 * @param timeFrame
	 *            booking shall be overlapping with
	 * @return all bookings for the given facility that are overlapping the
	 *         given timeframe and starts in future.
	 */
	public abstract List<Booking> getUncanceledBookingsAndApplicationsStartingInFutureOverlapping(FacilityBookable facility, FacilityAvailability timeFrame);

	/**
	 * return all bookings for the given facility that are overlapping the given
	 * timeframe. that are all bookings, where the date AND the end of booking
	 * IS NOT before OR after timeFrame. sort out bookings that are only
	 * applications.
	 * 
	 * @param facility
	 *            asking for
	 * @param timeFrame
	 *            asking for
	 * @return all bookings for the given facility that are overlapping the
	 *         given timeframe.
	 */
	public abstract List<Booking> getUncanceledBookingsWithoutApplicationsIn(FacilityBookable facility, TimeFrame timeFrame);

	/**
	 * return all bookings and applications made by given user
	 * 
	 * @param user
	 *            given
	 * @return all bookings and applications made by given user
	 */
	public abstract List<Booking> getAllBookingsOfUser(User user);

	/**
	 * simply return all + 1. this does not guerentee any unique ids!!!
	 */
	@Override
	protected void setIdToNextId(Booking dataholder) {
		dataholder.setId(this.getAll().size() + 1);
	}

	private String dataIntegrityMessage = null;

	/** {@inheritDoc} */
	@Override
	public synchronized boolean insert(Booking dataholder) throws DataIntegrityViolationException {
		dataholder.setSeton(new Date());
		this.setIdToNextId(dataholder);
		return super.insert(dataholder);
	}

	/**
	 * return true, if the given entry is not valid in saving.
	 * 
	 * this is if:
	 * <ul>
	 * <li>{@link BookingStatus} is unset</li>
	 * <li>{@link TimeBooking} has invalid units (of time or capacity)</li>
	 * <li>{@link TimeBooking} is not free anymore (in case of client server
	 * thread problems)</li>
	 * </ul>
	 * 
	 * updating none availables is valid. inserting not. this is why a boolean
	 * flag parameter is needed.
	 * 
	 * @param booking
	 *            to check
	 * @param onInsert
	 *            true, if entry shall be inserted
	 * @return true, if the given entry is not valid in saving.
	 */
	@Override
	protected boolean isDataIntegrityViolation(Booking booking, boolean onInsert) {
		boolean result = false;
		if (booking.getBookingStatus().isUnset()) {
			result = true;
			this.dataIntegrityMessage = "booking is unset";
		}
		if (result == false) {
			if (booking.getCapacityUnits() < booking.getBookingRule().getMinBookableCapacityUnits(booking.getUser()) || booking.getCapacityUnits() > booking.getBookingRule().getMaxBookableCapacityUnits(booking.getUser())) {
				result = true;
				this.dataIntegrityMessage = "invalid units";
			}
		}
		if (onInsert && result == false && booking.isAvailableForInsertion() == false && booking.isCanceled() == false) {
			result = true;
			this.dataIntegrityMessage = "not available anymore (" + booking.isAvailableForInsertion() + "/" + booking.isCanceled() + ")";
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	protected void logAndThrowDataIntegrityViolationException(Booking entry) throws DataIntegrityViolationException {
		String mess = "insert entry fail on booking " + entry + ". reason: " + dataIntegrityMessage + ".";
		DataIntegrityViolationException ex = new DataIntegrityViolationException(mess);
		FamLog.logException(BookingDao.class, ex, mess, 200906251009l);
		throw ex;
	}

	/** {@inheritDoc} */
	@Override
	protected void logInsert(Booking entry) {
		FamLog.info(String.format("insert booking id: %s; an: %s", entry.getId(), entry.getArticleNumber()), 200906251010l);
	}

	/** {@inheritDoc} */
	@Override
	protected void logUpdate(Booking entry) {
		FamLog.info(String.format("update booking id: %s; an: %s", entry.getId(), entry.getArticleNumber()), 200906251011l);
	}

	/**
	 * return all bookings and applications for the given facility that are not
	 * cancelled and overlapping with today.
	 * 
	 * @param facility
	 *            given
	 * @return all bookings and applications for the given facility that are not
	 *         cancelled and overlapping with today.
	 */
	public List<Booking> getAllUncanceledBookingsAndApplicationsOfToday(FacilityBookable facility) {
		return this.getAllUncanceledBookingsAndApplicationsOfDay(facility, Calendar.getInstance());
	}

	/**
	 * return all bookings that are for one of the given facility. ignore all
	 * none-bookable facilities in given facilities. this means, if there is a
	 * booking for a meta-resource, return the bookings for it twice. e.g. peter
	 * booked a coffee machine of a mensa of a university. if "coffee machine",
	 * "mensa" and "university" is given in this method, return peters booking 3
	 * times!
	 * 
	 * @param facilities
	 *            given
	 * @return all bookings that are for one of the given facility.
	 */
	public List<Booking> getAll(List<Facility> facilities) {
		List<FacilityBookable> bookableFacilities = new ArrayList<FacilityBookable>();
		for (Facility facility : facilities) {
			bookableFacilities.addAll(FacilityConfigDao.getInstance().getBookableChildrenFacilities(facility.getKey()));
			if (facility.isBookable()) {
				bookableFacilities.add((FacilityBookable) facility);
			}
		}
		return this.getAllIntern(bookableFacilities);
	}

	/**
	 * return all bookings that are for one of the given facility
	 * 
	 * @param bookableFacilities
	 *            given
	 * @return all bookings that are for one of the given facility
	 */
	public abstract List<Booking> getAllIntern(List<FacilityBookable> bookableFacilities);

	/**
	 * return all bookings and applications that are not cancelled.
	 * 
	 * @return all bookings and applications that are not cancelled.
	 */
	public abstract List<Booking> getAllUncanceled();

	/**
	 * get all applications where the session starts in future.
	 * 
	 * @return all applications where the session starts in future.
	 */
	public List<Booking> getAllUncanceledApplicationsNotMade() {
		List<Booking> candidates = this.getAllUncanceled();
		List<Booking> result = new ArrayList<Booking>();
		for (Booking candidate : candidates) {
			if (candidate.isApplication() && !candidate.sessionAlreadyMade()) {
				result.add(candidate);
			}
		}
		return result;
	}

	/**
	 * return the actual length of the queue on a facility.
	 * 
	 * @param facility
	 *            the queue length is checked for
	 * @return the actual length of the queue on a facility.
	 */
	public int getActualQueueLength(FacilityBookable facility) {
		return this.getActualQueue(facility).size();
	}

	/**
	 * return the actual position of the given booking in the queue. if the
	 * given queue is canceled are the session is already made, return null. if
	 * the session is running now, return 0. if the given booking is unknown
	 * (not saved yet), return queue length
	 * 
	 * @param queueBasedBooking
	 *            the position is given back.
	 * @return the actual position of the given booking in the queue.
	 * @throws DataIntegrityViolationException
	 */
	public Integer getActualPositionInQueue(QueueBooking queueBasedBooking) throws DataIntegrityViolationException {
		Integer result = null;
		if (!queueBasedBooking.isCanceled() && !queueBasedBooking.sessionAlreadyMade() && queueBasedBooking.getSeton() != null) {
			result = 0;
			if (!queueBasedBooking.sessionAlreadyBegun()) {
				List<QueueBooking> queue = this.getActualQueue(queueBasedBooking.getFacility());
				try {
					while (queue.get(result).getSeton().before(queueBasedBooking.getSeton()) || queue.get(result).getSeton().equals(queueBasedBooking.getSeton())) {
						result++;
					}
				} catch (IndexOutOfBoundsException e) {
					result = this.getActualQueueLength(queueBasedBooking.getFacility());
				}
			}
		} else if (queueBasedBooking.getSeton() == null) { // unknown booking by
			// now
			result = this.getActualQueueLength(queueBasedBooking.getFacility());
		}
		return result;
	}

	/**
	 * return all bookings for the given facility as it is booked queue based.
	 * the result list must be sorted by date set on and DOES NOT CONTAIN
	 * canceled bookings. it contains the actual session, that has already begun
	 * but not ended by now.
	 * 
	 * @param facility
	 *            bookings are for
	 * @return all bookings for the given facility as it is booked queue based.
	 */
	public abstract List<QueueBooking> getActualQueue(FacilityBookable facility);

	/**
	 * check the availability situation for the given facility and cancel all
	 * overlapping bookings.
	 * 
	 * @param facility
	 *            to check
	 * @param cancelation
	 *            to set in case of cancelations
	 */
	public void cancelOverlappingBookings(Facility facility, Cancelation cancelation) {

		// IDEA v2 if the slot gets free again, the canceled bookings may be
		// restored again
		// else { // isCompletely free again
		// free canceled bookings
		// }

		// cancel overlapping bookings
		List<Booking> cancelBookingCandidates = new ArrayList<Booking>();
		List<FacilityBookable> bookableFacilities = FacilityConfigDao.getInstance().getBookableChildrenFacilities(facility.getKey());

		// get all depending bookable facilities
		if (facility.isBookable()) {
			bookableFacilities.add((FacilityBookable) facility);
		}
		// get all bookings of all bookable facilities
		for (FacilityBookable bookableFacility : bookableFacilities) {
			List<Booking> comingBookings = FamDaoProxy.bookingDao().getAllUncanceledBookingsAndApplicationsNotBegunYet(bookableFacility);
			cancelBookingCandidates.addAll(comingBookings);
		}
		// cancel all bookings
		for (Booking cancelBookingCandidate : cancelBookingCandidates) {
			if (cancelBookingCandidate.getIdBookedInBookingStrategy() == BookingStrategy.TIME_BASED) {
				if (((TimeBooking) cancelBookingCandidate).isApplicableToANotAvailableFacilityAvailability()) {
					cancelBookingCandidate.cancel(cancelation);
					cancelBookingCandidate.update();
				}
			}
		}
	}

	/**
	 * return all booking requests that:
	 * <ul>
	 * <li>are for the given facility</li>
	 * <li>are not canceled</li>
	 * <li>not begun yet</li>
	 * </ul>
	 * 
	 * @param bookableFacility
	 *            bookings are for
	 * @return all booking requests that:
	 *         <ul>
	 *         <li>are for the given facility</li>
	 *         <li>are not canceled</li>
	 *         <li>not begun yet</li>
	 *         </ul>
	 */
	public List<Booking> getAllUncanceledBookingsAndApplicationsNotBegunYet(FacilityBookable bookableFacility) {
		List<Booking> candidates = this.getAllUncanceled(bookableFacility);
		List<Booking> result = new ArrayList<Booking>();
		for (Booking candidate : candidates) {
			if (candidate.sessionAlreadyBegun() == false) {
				result.add(candidate);
			}
		}
		return result;
	}

	/**
	 * return all booking requests that:
	 * <ul>
	 * <li>are for the given facility</li>
	 * <li>are not canceled</li>
	 * <li>does not have complete the session</li>
	 * </ul>
	 * 
	 * @param bookableFacility
	 *            bookings are for
	 * @return all booking requests that:
	 *         <ul>
	 *         <li>are for the given facility</li>
	 *         <li>are not canceled</li>
	 *         <li>does not have complete the session</li>
	 *         </ul>
	 */
	public List<Booking> getAllUncanceledBookingsAndApplicationsNotMadeYet(FacilityBookable bookableFacility) {
		List<Booking> candidates = this.getAllUncanceled(bookableFacility);
		List<Booking> result = new ArrayList<Booking>();
		for (Booking candidate : candidates) {
			if (candidate.sessionAlreadyMade() == false) {
				result.add(candidate);
			}
		}
		return result;
	}

	/**
	 * return all booking requests that:
	 * <ul>
	 * <li>are for the given facility</li>
	 * <li>are not canceled</li>
	 * </ul>
	 * 
	 * @param bookableFacility
	 *            bookings are for
	 * @return all booking requests that:
	 *         <ul>
	 *         <li>are for the given facility</li>
	 *         <li>are not canceled</li>
	 *         </ul>
	 */
	public List<Booking> getAllUncanceled(FacilityBookable bookableFacility) {
		List<Booking> result = new ArrayList<Booking>();
		List<Booking> candidates = this.getAll(bookableFacility);
		for (Booking candidate : candidates) {
			if (candidate.isCanceled() == false) {
				result.add(candidate);
			}
		}
		return result;
	}

	/**
	 * return all booking requests for the given facility.
	 * 
	 * @param bookableFacility
	 *            bookings are for
	 * @return all booking requests for the given facility.
	 */
	public List<Booking> getAll(FacilityBookable bookableFacility) {
		List<Facility> tmp = new ArrayList<Facility>();
		tmp.add(bookableFacility);
		return this.getAll(tmp);
	}

	/**
	 * return all booking requests that:
	 * <ul>
	 * <li>are for the same facility as the given booking</li>
	 * <li>are not canceled</li>
	 * <li>are on the same session time frame as the given booking</li>
	 * </ul>
	 * <br />
	 * if the given booking is part of the database, it is included. otherwise
	 * not.
	 * 
	 * @param booking
	 *            to check
	 * @return all booking requests that:
	 *         <ul>
	 *         <li>are for the same facility as the given booking</li>
	 *         <li>are not canceled</li>
	 *         <li>are on the same session time frame as the given booking</li>
	 *         </ul>
	 */
	public List<Booking> getAllUncanceledOverlapping(TimeBooking booking) {
		return this.getUncanceledBookingsAndApplicationsIn(booking.getFacility(), booking.getSessionTimeFrame());
	}

	/**
	 * return the booking with the given id or null if no booking exists with
	 * this id.
	 * 
	 * @param id
	 *            to query for
	 * @return the booking with the given id or null if no booking exists with
	 *         this id.
	 */
	public abstract Booking getBookingWithId(int id);

	/**
	 * return a list with bookings of the user that are not cancelled and
	 * processed. if there are no bookings return an empty list.
	 * 
	 * @param user
	 *            the owner of the booking returned
	 * @return a list with bookings of the user that are not cancelled and
	 *         processed.
	 */
	public abstract List<Booking> getAllUncanceledAndProcessed(User user);
}

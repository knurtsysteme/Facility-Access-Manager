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
package de.knurt.fam.core.persistence.dao.ibatis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.BookingStatus;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.persistence.dao.BookingDao;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * dao for {@link Booking}s stored in sql
 * 
 * @author Daniel Oltmanns
 * @since 0.20090828
 */
public class BookingDao4ibatis extends BookingDao {

	/** {@inheritDoc} */
	@Override
	protected synchronized boolean internInsert(Booking dataholder) {
		boolean result = false;
		try {
			BookingAdapterParameter adapter = new BookingAdapterParameter(dataholder);
			FamSqlMapClientDaoSupport.sqlMap().insert("Booking.insert", adapter);
			FamLog.info("insert a booking", 201102221431l);
			result = true;
		} catch (Exception e) {
			FamLog.exception(e, 201205071123l);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	protected synchronized boolean internUpdate(Booking dataholder) {
		boolean result = false;
		try {
			BookingAdapterParameter adapter = new BookingAdapterParameter(dataholder);
			FamSqlMapClientDaoSupport.sqlMap().update("Booking.update", adapter);
			result = true;
		} catch (Exception e) {
			FamLog.exception(e, 201205071124l);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized boolean delete(Booking booking) {
		boolean result = false;
		if (booking.getId() != null) {
			try {
				FamSqlMapClientDaoSupport.sqlMap().delete("Booking.delete", booking, 1);
				result = true;
			} catch (Exception e) {
				FamLog.exception(e, 201204231018l);
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public List<Booking> getAllBookingsOfUser(User user) {
		List<BookingAdapterResult> facilityBookings = FamSqlMapClientDaoSupport.sqlMap().queryForList("Booking.select.allFromUser", user);
		return BookingAdapterResult.getBookings(facilityBookings);
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public List<Booking> getObjectsLike(Booking example) {
		BookingAdapterParameter adapter = new BookingAdapterParameter(example);
		List<BookingAdapterResult> sadapts = FamSqlMapClientDaoSupport.sqlMap().queryForList("Booking.select.where", adapter);
		return BookingAdapterResult.getBookings(sadapts);
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public List<Booking> getUncanceledBookingsAndApplicationsIn(FacilityBookable facility, TimeFrame timeFrame) {
		List<BookingAdapterResult> facilityBookings = FamSqlMapClientDaoSupport.sqlMap().queryForList("Booking.select.uncanceledTimeBookingsAndApplicationsOf", facility);
		List<Booking> result = new ArrayList<Booking>();
		for (BookingAdapterResult candidate : facilityBookings) {
			if (timeFrame.overlaps(candidate.getTimeFrame())) {
				result.add(candidate.getBooking());
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public List<Booking> getUncanceledBookingsWithoutApplicationsIn(FacilityBookable facility, TimeFrame timeFrame) {
		List<BookingAdapterResult> facilityBookings = FamSqlMapClientDaoSupport.sqlMap().queryForList("Booking.select.uncanceledTimeBookingsAndApplicationsOf", facility);
		List<Booking> result = new ArrayList<Booking>();
		for (BookingAdapterResult candidate : facilityBookings) {
			if (candidate.getStatusId() != BookingStatus.STATUS_APPLIED && candidate.getTimeFrame() != null && candidate.getTimeFrame().overlaps(timeFrame)) {
				result.add(candidate.getBooking());
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public List<Booking> getUncanceledBookingsAndApplicationsIn(FacilityBookable facility, FacilityAvailability timeFrame) {
		List<BookingAdapterResult> facilityBookings = FamSqlMapClientDaoSupport.sqlMap().queryForList("Booking.select.uncanceledTimeBookingsAndApplicationsOf", facility);
		List<Booking> result = new ArrayList<Booking>();
		for (BookingAdapterResult candidate : facilityBookings) {
			if (timeFrame.applicableTo(candidate.getTimeFrame())) {
				result.add(candidate.getBooking());
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public List<Booking> getUncanceledBookingsAndApplicationsStartingInFutureOverlapping(FacilityBookable facility, FacilityAvailability timeFrame) {
		List<BookingAdapterResult> facilityBookings = FamSqlMapClientDaoSupport.sqlMap().queryForList("Booking.select.uncanceledFutureTimeBookingsAndApplicationsOf", facility);
		List<Booking> result = new ArrayList<Booking>();
		for (BookingAdapterResult candidate : facilityBookings) {
			if (timeFrame.applicableTo(candidate.getTimeFrame())) {
				result.add(candidate.getBooking());
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public List<Booking> getAllIntern(List<FacilityBookable> bookableFacilities) {
		List<Booking> result = new ArrayList<Booking>();
		for (FacilityBookable facility : bookableFacilities) {
			Booking b = TimeBooking.getEmptyExampleBooking();
			b.setFacilityKey(facility.getKey());
			result.addAll(this.getObjectsLike(b));
		}
		return result;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public List<Booking> getAll() {
		List<BookingAdapterResult> sadapts = FamSqlMapClientDaoSupport.sqlMap().queryForList("Booking.select.all");
		return BookingAdapterResult.getBookings(sadapts);
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public List<Booking> getAllUncanceled() {
		List<BookingAdapterResult> uncanceledBookings = FamSqlMapClientDaoSupport.sqlMap().queryForList("Booking.select.allUncanceled");
		return BookingAdapterResult.getBookings(uncanceledBookings);
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public List<QueueBooking> getCurrentQueue(FacilityBookable facility) {
		List<BookingAdapterResult> sadapts = FamSqlMapClientDaoSupport.sqlMap().queryForList("Booking.select.currentQueue", facility);
		return BookingAdapterResult.getQueueBookings(sadapts);
	}

	/**
	 * return bookings of "where" opening the door for SQL injection.
	 * 
	 * @param where
	 *            used as sql where
	 * @return bookings <code>where</code> statement is true
	 */
	@SuppressWarnings("unchecked")
	private final List<Booking> getWhere(String where) {
		List<BookingAdapterResult> sadapts = FamSqlMapClientDaoSupport.sqlMap().queryForList("Booking.select.sqlwhere", where);
		return BookingAdapterResult.getBookings(sadapts);
	}

	/** {@inheritDoc} */
	@Override
	public Booking getBookingWithId(int id) {
		String where = String.format("id = '%s'", id);
		List<Booking> result = this.getWhere(where);
		if (result != null && result.size() == 1) {
			return result.get(0);
		} else {
			return null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public List<Booking> getAllUncanceledAndProcessed(User user) {
		String where = String.format("processed = true AND cancelation_seton IS NULL AND username = '%s'", user.getUsername());
		return this.getWhere(where);
	}

	/** {@inheritDoc} */
	@Override
	public List<TimeBooking> getCurrentSessions(User user) {
		List<TimeBooking> result = new ArrayList<TimeBooking>();
		String whereToFormat = "time_start <= '%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS' AND time_end > '%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS' AND cancelation_seton IS NULL AND username = '%2$s'";
		String where = String.format(whereToFormat, new Date(), user.getUsername());
		List<Booking> cands = this.getWhere(where);
		for (Booking cand : cands) {
			if (cand.isTimeBased()) {
				result.add((TimeBooking) cand);
			}
		}
		return result;
	}

}

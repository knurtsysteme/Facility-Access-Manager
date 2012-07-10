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

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.control.persistence.dao.couchdb.CouchDBDao4Jobs;
import de.knurt.fam.core.control.persistence.dao.couchdb.CouchDBDao4Soa;
import de.knurt.fam.core.control.persistence.dao.couchdb.FamCouchDBDao;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.Logbook;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;

/**
 * a (singleton) data holder for all defined roles
 * 
 * @author Daniel Oltmanns
 * @since 0.20090324
 */
public class FamDaoProxy {

	private volatile static FamDaoProxy me;

	/**
	 * return the configured database access object resolving {@link User}s.
	 * 
	 * @return the configured database access object resolving {@link User}s.
	 */
	public static UserDao userDao() {
		return getInstance().getUserDao();
	}

	private UserDao userDao;
	private LogbookEntryDao logbookEntryDao;
	private FacilityDao facilityDao;
	private BookingDao bookingDao;
	private KeyValueDao keyValueDao;

	/**
	 * return the one and only instance of me
	 * 
	 * @return the one and only instance of me
	 */
	public static FamDaoProxy getInstance() {
		if (me == null) { // no instance so far
			synchronized (FamDaoProxy.class) {
				if (me == null) { // still no instance so far
					me = new FamDaoProxy(); // the one and only
				}
			}
		}
		return me;
	}

	/**
	 * return the configured database access object resolving {@link Facility}
	 * concerns.
	 * 
	 * @return the configured database access object resolving {@link Facility}
	 *         concerns.
	 */
	public FacilityDao getFacilityDao() {
		return this.facilityDao;
	}

	/**
	 * return the configured database access object resolving {@link Booking}s.
	 * 
	 * @return the configured database access object resolving {@link Booking}s.
	 */
	public BookingDao getBookingDao() {
		return this.bookingDao;
	}

	public KeyValueDao getKeyValueDao() {
		return this.keyValueDao;
	}

	/**
	 * return the configured database access object resolving {@link Booking}s.
	 * 
	 * @return the configured database access object resolving {@link Booking}s.
	 */
	public static BookingDao bookingDao() {
		return getInstance().getBookingDao();
	}

	public static KeyValueDao keyValueDao() {
		return getInstance().getKeyValueDao();
	}

	/**
	 * return the configured database access object resolving {@link Logbook}s.
	 * 
	 * @return the configured database access object resolving {@link Logbook}s.
	 */
	public LogbookEntryDao getLogbookEntryDao() {
		return this.logbookEntryDao;
	}

	/**
	 * short form of <code>FamDaoProxy.getInstance().getLogbookEntryDao()</code>
	 * 
	 * @return the {@link LogbookEntryDao}
	 */
	public static LogbookEntryDao logbookEntryDao() {
		return getInstance().getLogbookEntryDao();
	}

	/**
	 * short form of <code>FamDaoProxy.getInstance().getLogbookEntryDao()</code>
	 * 
	 * @return the {@link LogbookEntryDao}
	 */
	public static FacilityDao facilityDao() {
		return getInstance().getFacilityDao();
	}

	/**
	 * return the dao managing {@link User}
	 * 
	 * @return the userdao managing {@link User}
	 */
	public UserDao getUserDao() {
		return userDao;
	}

	/**
	 * set the dao managing {@link User}
	 * 
	 * @param userDao
	 *            the userdao to set
	 */
	@Required
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	/**
	 * set the configured database access object resolving {@link Booking}s.
	 * 
	 * @param bookingDao
	 *            database access object resolving {@link Booking}s.
	 */
	@Required
	public void setBookingDao(BookingDao bookingDao) {
		this.bookingDao = bookingDao;
	}

	/**
	 * set the configured database access object resolving {@link Logbook}s.
	 * 
	 * @param logbookEntryDao
	 *            database access object resolving {@link Logbook}s.
	 */
	@Required
	public void setLogbookEntryDao(LogbookEntryDao logbookEntryDao) {
		this.logbookEntryDao = logbookEntryDao;
	}

	/**
	 * @param facilityDao
	 *            the dao for facility concerns to set
	 */
	@Required
	public void setFacilityDao(FacilityDao facilityDao) {
		this.facilityDao = facilityDao;
	}

	@Required
	public void setKeyValueDao(KeyValueDao keyValueDao) {
		this.keyValueDao = keyValueDao;
	}

	public static FamJobsDao jobsDao() {
		return CouchDBDao4Jobs.getInstance();
	}

	public static FamSoaDao soaDao() {
		return CouchDBDao4Soa.getInstance();
	}

	public static FamDocumentDao docDao() {
		return FamCouchDBDao.getInstance();
	}

}

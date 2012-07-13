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
package de.knurt.fam.core.aspects.security.auth;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.model.config.BookingStrategy;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.config.TimeBasedBookingRule;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.config.RoleConfigDao;
import de.knurt.fam.core.util.time.FamCalendar;
import de.knurt.heinzelmann.util.auth.UserAuthentication;

/**
 * Central point to authentificate user in the dao system
 * 
 * @author Daniel Oltmanns
 * @since 0.20090326 (03/26/2009)
 */
public class FamAuth {

	/** flag for "having administration rights" */
	public final static int ADMIN = 0;
	/** flag for "allowed to make bookings without any application" */
	public final static int DIRECT_BOOKING = 1;
	/**
	 * flag for
	 * "allowed to make direct bookings even on time slots with applications"
	 */
	public final static int OVERRIDE_APPLICATIONS = 2;
	/**
	 * flag for
	 * "allowed to confirm and cancel applications for their facilities"
	 */
	public final static int CONFIRM_AND_CANCEL_APPLICATIONS = 3;
	/** flag for "allowed to exclude users" */
	public final static int EXCLUDE_USERS = 4;
	/** flag for "allowed to cancel bookings for their facilities" */
	public final static int CANCEL_BOOKINGS = 5;
	/** flag for "allowed to set maintenance times for their facilities" */
	public final static int SET_MAINTENANCE = 6;
	/** flag for "allowed to book" */
	public final static int BOOKING = 7;
	/** flag for "allowed to view personal information" */
	public final static int VIEW_PERSONAL_INFORMATION = 8;
	/** flag for "allowed to view statistics" */
	public final static int VIEW_STATISTICS = 9;
	/**
	 * flag for "allowed to book without a time barrier". some users, mostly
	 * that have to apply, shall not apply the next xy hours. You need this
	 * right to book earlier.
	 */
	public final static int BOOK_WITHOUT_TIME_BARRIER = 10;
	/** Book without to sign terms of use agreements */
	public final static int BOOK_WITHOUT_ACCEPTING_SOA = 11;
	/** flag for "allowed to delete a" */
	public final static int DELETE_USER = 12;
	/** flag for "allowed to anonymize a" */
	public final static int ANONYMIZE_USER = 13;
	/** flag for "allowed to delete users data" */
	public final static int DELETE_USERS_DATA = 14;

	/**
	 * return true, if the given user has the right <code>forwhat</code>.
	 * <code>forwhat</code> is one of the class constants.
	 * 
	 * <img src="./doc-files/classdiagram_auth.png" />
	 * 
	 * if the user is the admin, return true in any case.
	 * 
	 * @param user
	 *            to check
	 * @param forwhat
	 *            asking for what right? one of the public class constants
	 * @param onFacility
	 *            some rights depends on a facility. this facility is given
	 *            here.
	 * @return true, if the given user has the right <code>forwhat</code>.
	 */
	public final static boolean hasRight(User user, int forwhat, Facility onFacility) {
		return isAuth(user, forwhat, onFacility);
	}

	/**
	 * return true, if the given user has the right <code>forwhat</code>.
	 * <code>forwhat</code> is one of the class constants.
	 * 
	 * <img src="./doc-files/classdiagram_auth.png" />
	 * 
	 * if the user is the admin, return true in any case.
	 * 
	 * @param user
	 *            to check
	 * @param forwhat
	 *            asking for what right? one of the public class constants
	 * @return true, if the given user has the right <code>forwhat</code>.
	 */
	private final static boolean isAuth(User user, int forwhat, Facility onFacility) {
		boolean result = RoleConfigDao.getInstance().isAdmin(user);
		if (result == false) {
			result = RoleConfigDao.getInstance().hasRight(user, forwhat, onFacility);
		}
		return result;
	}

	/** one and only instance of FamAuth */
	private volatile static FamAuth me;

	/** construct FamAuth */
	private FamAuth() {
	}

	/**
	 * return the one and only instance of FamAuth
	 * 
	 * @return the one and only instance of FamAuth
	 */
	public static FamAuth getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (FamAuth.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new FamAuth();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of FamAuth
	 */
	public static FamAuth me() {
		return getInstance();
	}

	private UserAuthentication userAuthentication = null;

	@Required
	public void setUserAuthentication(UserAuthentication userAuthentication) {
		assert userAuthentication != null;
		this.userAuthentication = userAuthentication;
	}

	public UserAuthentication getUserAuthentication() {
		return userAuthentication;
	}

	/**
	 * return true, if the given user has general access to the system
	 * 
	 * @param user
	 *            to check
	 * @param cleanPass
	 *            the none encoded password of the user
	 * @return true, if the given user has general access to the system
	 */
	public final static boolean isAuth(User user, String cleanPass) {
		return me().isAuthIntern(user, cleanPass);
	}

	private final boolean isAuthIntern(User user, String cleanPass) {
		boolean result = false;
		if (this.userAuthentication.isAuth(user, cleanPass)) {
			result = true;
		}
		return result;
	}

	/**
	 * return when the given user is allowed to book the given facility from now
	 * on in minutes.
	 * 
	 * @param user
	 *            to check
	 * @param bd
	 *            bookable facility asking for.
	 * @return when the given user is allowed to book the given facility from
	 *         now on in minutes.
	 */
	public static int getEarliestPossibilityToBookFromNow(User user, FacilityBookable bd) {
		int result = 0;
		if (hasRight(user, BOOK_WITHOUT_TIME_BARRIER, bd) == false) {
			if (bd.getBookingStrategy() == BookingStrategy.TIME_BASED) {
				result = ((TimeBasedBookingRule) bd.getBookingRule()).getEarliestPossibilityToBookFromNow();
			}
		}
		return result;
	}

	public static Calendar getEarliestCalendarToBookFromNow(User user, FacilityBookable bd) {
		Calendar result = FamCalendar.getInstance();
		result.add(Calendar.MINUTE, getEarliestPossibilityToBookFromNow(user, bd));
		return result;
	}

	/**
	 * short form for checking more then one right.
	 * 
	 * @see #hasRight(User, int, Facility)
	 * @param user
	 *            to check
	 * @param rights
	 *            asking for what rights? use public class constants
	 * @param onFacility
	 *            some rights depends on a facility. this facility is given
	 *            here. set null if the right is independent of a facility.
	 * @return true, if the given user has all the given <code>rights</code>.
	 */
	public static boolean hasAllRights(User user, Integer[] rights, Facility onFacility) {
		boolean result = true;
		for (Integer right : rights) {
			result = hasRight(user, right, onFacility);
			if (!result) {
				break;
			}
		}
		return result;
	}
}

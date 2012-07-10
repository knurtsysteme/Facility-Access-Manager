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
package de.knurt.fam.core.content.text;

import java.io.File;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.config.FamRequestContainer;
import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.Cancelation;
import de.knurt.heinzelmann.util.IntegerNumeralFormat;
import de.knurt.heinzelmann.util.text.DurationAdapter;
import de.knurt.heinzelmann.util.text.DurationAdapter.SupportedLanguage;

/**
 * base class to access the properties text file. this is to provide a central
 * point for internationalisation. however, some things are used as an image as
 * well - but does nothing as representing a text (with a title). these
 * image-hints can be found here as well.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090410 (04/10/2009)
 */
public class FamText {
	
	private static DurationAdapter durationAdapter = new DurationAdapter(SupportedLanguage.ENGLISH);


	/**
	 * return the given number as ordinal (1st, 2nd, 3rd ...)
	 * 
	 * @see IntegerNumeralFormat#format(int)
	 * @param number
	 * @return
	 */
	public static String getOrdinal(int number) {
		return new IntegerNumeralFormat(FamRequestContainer.locale()).format(number);
	}

	/**
	 * return the capacity units of the booking as text.
	 * 
	 * @see BookingRule#getCapacityUnitName(int)
	 * @param booking
	 *            the capacity units are taken from.
	 * @return the capacity units of the booking as text.
	 */
	public static String facilityNameWithCapacityUnits(Booking booking) {
		int cu = booking.getCapacityUnits();
		return cu + " " + booking.getBookingRule().getCapacityUnitName(cu);
	}

	/**
	 * return the capacity units of the facility as text. a <code>We have</code>
	 * is leading that.
	 * 
	 * @see FacilityBookable#getCapacityUnits()
	 * @param bd
	 *            the capacity units is returned as text.
	 * @return the capacity units of the facility as text
	 */
	public static String capacityUnitsOfAvailables(FacilityBookable bd) {
		int cu = bd.getCapacityUnits();
		String result = "";
		if (cu > 1) {
			result = "Total: " + capacityUnits(bd); // INTLANG
		}
		return result;
	}

	/**
	 * return the capacity units of the facility as text.
	 * 
	 * @see BookingRule#getCapacityUnitName(int)
	 * @param bd
	 *            the capacity units are taken from.
	 * @return the capacity units of the facility as text.
	 */
	public static String capacityUnits(FacilityBookable bd) {
		int cu = bd.getCapacityUnits();
		return cu + " " + bd.getBookingRule().getCapacityUnitName(cu);
	}

	private static boolean isBookingOfUser(User actual, Booking booking) {
		return actual.getUsername().equals(booking.getUsername());
	}

	/**
	 * return the text used as title for the status image icon.
	 * 
	 * @see #statusOfBookingAsImg(de.knurt.fam.core.model.persist.User,
	 *      de.knurt.fam.core.model.persist.booking.Booking)
	 * @param actual
	 *            user requesting the status
	 * @param booking
	 *            the status is requested of
	 * @return the status of the given booking for the actual user
	 */
	public static String statusOfBookingAsText(User actual, Booking booking) {
		boolean usersBooking = isBookingOfUser(actual, booking);
		String alt = "unknown"; // INTLANG
		if (booking.sessionAlreadyMade()) {
			if (booking.getBookingStatus().isBooked()) {
				alt = "session did come into being"; // INTLANG
			} else {
				alt = "session did not come into being"; // INTLANG
			}
		} else {
			if (booking.isCanceled()) {
				String reason = booking.getCancelation().getReason();
				if (usersBooking && reason.equals(Cancelation.REASON_FREE_BY_USER)) {
					reason = "You canceled it";
				}
				alt = booking.isApplication() ? "Booking has not been approved." : "Booking is canceled.";
				alt += " Reason: " + reason; // INTLANG
			} else {
				if (booking.getBookingStatus().isApplied()) {
					alt = usersBooking ? "We got your application. You get a decision soon." : "User has applied for booking and is waiting for a decision."; // INTLANG
				} else if (booking.getBookingStatus().isBooked()) {
					alt = String.format("This is set aside for %s.", usersBooking ? "you" : "the user"); // INTLANG
				}
			}
		}
		return alt;
	}

	/**
	 * return the facility availability as text.
	 * 
	 * @see #facilityAvailability(de.knurt.fam.core.model.persist.FacilityAvailability)
	 * @see FacilityAvailability#getAvailable()
	 * @param daAvailability
	 *            id for availability
	 * @return the facility availability as text.
	 */
	public static String facilityAvailability(int daAvailability) {
		FacilityAvailability da = new FacilityAvailability();
		da.setAvailable(daAvailability);
		return facilityAvailability(da);
	}

	/**
	 * return the given string or alt if the given string is null or empty
	 * 
	 * @param val
	 *            the given string to check
	 * @param alt
	 *            the alternative for given string
	 * @return the given string or a message "no input" if the given value is
	 *         null or empty
	 */
	public static String valueOrAlt(String val, String alt) {
		return val != null && !val.trim().isEmpty() ? val : alt;
	}

	/**
	 * return the facility availability as text.
	 * 
	 * @see FacilityAvailability#getAvailable()
	 * @param da
	 *            for availability used
	 * @return the facility availability as text.
	 */
	public static String facilityAvailability(FacilityAvailability da) {
		String result = "";
		if (da.isCompletelyAvailable()) {
			result = "completely available"; // INTLANG
		} else if (da.isMaybeAvailable()) {
			result = "maybe available"; // INTLANG
		} else if (da.mustNotStartHere()) {
			result = "must not start here"; // INTLANG
		} else if (da.isNotAvailableBecauseOfBooking()) {
			result = "not available because of a booking"; // INTLANG
		} else if (da.isNotAvailableBecauseOfSuddenFailure()) {
			result = "not available because of a sudden failure"; // INTLANG
		} else if (da.isNotAvailableBecauseOfMaintenance()) {
			result = "not available because of a maintenance"; // INTLANG
		} else if (da.isNotAvailableInGeneral()) {
			result = "not available in general"; // INTLANG
		}
		return result;
	}

	/**
	 * return the facility availability as text in short.
	 * 
	 * @see #facilityAvailability(de.knurt.fam.core.model.persist.FacilityAvailability)
	 * @see FacilityAvailability#getAvailable()
	 * @param daAvailability
	 *            id for availability
	 * @return the facility availability as text.
	 */
	public static String facilityAvailabilityShort(int daAvailability) {
		FacilityAvailability da = new FacilityAvailability();
		da.setAvailable(daAvailability);
		return facilityAvailabilityShort(da);
	}

	private static String facilityAvailabilityShort(FacilityAvailability da) {
		String result = "";
		if (da.isCompletelyAvailable()) {
			result = "yes"; // INTLANG
		} else if (da.isMaybeAvailable()) {
			result = "maybe"; // INTLANG
		} else if (da.isNotAvailableBecauseOfBooking()) {
			result = "booked"; // INTLANG
		} else if (da.mustNotStartHere()) {
			result = "must not start here"; // INTLANG
		} else if (da.isNotAvailableBecauseOfMaintenance()) {
			result = "maintenance"; // INTLANG
		} else if (da.isNotAvailableInGeneral()) {
			result = "no in general"; // INTLANG
		}
		return result;
	}

	/**
	 * compute and return the given minutes in x day(s), y hour(s), z minute(s).
	 * 
	 * @param minutes
	 *            to use
	 * @param html
	 *            if true, <code>&lt;br /&gt;</code> is used as separator.
	 *            Otherwise <code>", "</code>
	 * @return the given minutes in x day(s), y hour(s), z minute(s).
	 */
	public static String getTimeInput(int minutes) {
		return durationAdapter.getText(minutes);
	}

	private ReloadableResourceBundleMessageSource rbms;
	/** one and only instance of me */
	private volatile static FamText me;

	/** construct me */
	private FamText() {
		rbms = new ReloadableResourceBundleMessageSource();
		if (new File(FamConnector.getConfigDirectory() + "config" + System.getProperty("file.separator") + "lang.properties").exists()) {
			String basename = "file:" + FamConnector.getConfigDirectory() + "config" + System.getProperty("file.separator") + "lang";
			rbms.setBasename(basename);
			rbms.setDefaultEncoding("UTF-8");
			rbms.setUseCodeAsDefaultMessage(true);
		} else {
			FamLog.error("no language file defined at : " + FamConnector.getConfigDirectory() + "config" + System.getProperty("file.separator") + "lang.properties", 201204181117l);
		}
	}

	/**
	 * return the one and only instance of FamText. set lang properties of the
	 * config directory - if exists there. otherwise use the default language in
	 * classpath.
	 * 
	 * @see FamConnector#getConfigDirectory()
	 * 
	 * @return the one and only instance of FamText
	 */
	public static FamText getInstance() {
		if (me == null) { // no instance so far
			synchronized (FamText.class) {
				if (me == null) { // still no instance so far
					me = new FamText(); // the one and only
				}
			}
		}
		return me;
	}

	/**
	 * return a message from the message source replacing placeholders with
	 * args.
	 * 
	 * @param string
	 *            key of the property
	 * @param args
	 *            objects put into the placeholders
	 * @see ResourceBundleMessageSource#getMessage(java.lang.String,
	 *      java.lang.Object[], java.util.Locale)
	 * @return a message from the message source replacing placeholders with
	 *         args.
	 */
	public String getMessage(String string, Object[] args) {
		return this.rbms.getMessage(string, args, FamRequestContainer.locale());
	}

	/**
	 * return true, if a message for the given key exists. this assumes, that
	 * the key not equals its value!
	 * 
	 * @param key
	 *            to check
	 * @return true, if the key exists in message source.
	 */
	public boolean messageExists(String key) {
		return !this.getMessage(key).equals(key);
	}

	/**
	 * return a message from the message source without any replacing.
	 * 
	 * @param string
	 *            key of the property
	 * @return a message from the message source without any replacing.
	 */
	public String getMessage(String string) {
		return this.getMessage(string, null);
	}

	/**
	 * return message as short for <code>getInstance().getMessage(key)</code>
	 * 
	 * @see #getMessage(java.lang.String)
	 * @param key
	 *            of the message
	 * @return message as short for <code>getInstance().getMessage(key)</code>
	 */
	public static String message(String key) {
		return getInstance().getMessage(key);
	}

	/**
	 * return message as short for
	 * <code>getInstance().getMessage(key, args)</code>
	 * 
	 * @see #getMessage(java.lang.String, java.lang.Object[])
	 * @param key
	 *            of the message
	 * @param args
	 *            for message
	 * @return message as short for <code>getInstance().getMessage(key)</code>
	 */
	public static String message(String key, Object[] args) {
		return getInstance().getMessage(key, args);
	}

	/**
	 * return a word for the user status.
	 * <ul>
	 * <li>unverified</li>
	 * <li>barred</li>
	 * <li>active</li>
	 * </ul>
	 * 
	 * @param user
	 *            the word is for
	 * @return a word for the user status.
	 */
	public static String getUserStatus(User user) {
		String result;
		if (!user.hasVarifiedAccount()) {
			result = "unverified"; // INTLANG
		} else {
			result = user.isExcluded() ? "barred" : "active"; // INTLANG
		}
		return result;
	}
}

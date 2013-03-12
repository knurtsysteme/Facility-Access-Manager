/*
 * Copyright 2002-2008 the original author or authors.  *  * Licensed under the Creative Commons License Attribution-NonCommercial-ShareAlike 3.0 Unported;  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *      http://creativecommons.org/licenses/by-nc-sa/3.0/  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.
 */
package de.knurt.fam.test.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;

import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.config.SetOfRulesForARole;
import de.knurt.fam.core.model.config.SimpleSetOfRulesForARoleBean;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Cancelation;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.model.persist.document.JobDataProcessing;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.persistence.dao.config.RoleConfigDao;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.core.util.booking.TimeBookingRequest;

/**
 * get simple beans for unit testing
 * 
 * @author Daniel Oltmanns
 * @since 0.20090326
 */
public class TeztBeanSimpleFactory {

	/**
	 * 
	 * @return
	 */
	public static Facility getFacility1() {
		return FacilityConfigDao.getInstance().getConfiguredInstance(KEY_FACILITY_BOOKABLE);
	}

	/**
	 * 
	 * @return
	 */
	public static FacilityBookable getFacilityBookable() {
	  return getFacilityBookable(KEY_FACILITY_BOOKABLE);
	}

	/**
	 * 
	 * @return
	 */
	public static Cancelation getNewCancelation() {
		User user = getNewValidUser();
		user.insert();
		return new Cancelation(user, Cancelation.REASON_FREE_BY_USER);
	}

	/**
	 * 
	 * @return
	 */
	public static TimeBooking getNewValidBooking4TomorrowSameTimeAsNow() {
		return getNewValidBooking(getBookingRequest());
	}

	/**
	 * 
	 * @param br
	 * @return
	 */
	public static TimeBooking getNewValidBooking(TimeBookingRequest br) {
		return TimeBooking.getNewBooking(br);
	}

	/**
	 * 
	 * @return
	 */
	public static User getNewValidUser() {
		User user = UserFactory.me().blank();
		user.setMail("user@foo.bar");
		user.setFname("Peter");
		user.setSname("Meier");
		user.setPassword("foobar12345");
		user.setDepartmentKey(USER_DEPARTMENT);
		user.setRoleId("extern");
		user.setStandardUser();
		user.setUniqueUsernameForInsertion();
		user.setExcluded(Boolean.FALSE);

		return user;
	}

	/**
	 * 
	 * @param salt
	 * @return
	 */
	public static User getNewUniqueValidUser(String salt) {
		User user = UserFactory.me().blank();
		user.setMail(getRandomString());
		user.setFname(getRandomString());
		user.setSname(getRandomString());
		user.setPassword(getRandomString());
		user.setDepartmentKey(USER_DEPARTMENT);
		user.setStandardUser();
		user.setRoleId("intern");
		user.setExcluded(Boolean.FALSE);
		user.setUniqueUsernameForInsertion();
		return user;
	}

	private static final String USER_DEPARTMENT = "Department for Foo";
	public static final String LOGBOOK_ID = "bus1Logbook";
	public static final String KEY_FACILITY_BOOKABLE = "bus1";
	public static final String KEY_FACILITY_BOOKABLE_PARENT = "vehicles";
	public static final String KEY_FACILITY_BOOKABLE_2 = "schoolbus";
	public static final String KEY_FACILITY_BOOKABLE_QUEUE = "carRepairShop";
	public static final String LOGBOOK_ID1 = LOGBOOK_ID;
	public static final String LOGBOOK_ID2 = "bus3Logbook";
	public static final String LOGBOOK_ID3 = "bus4Logbook";
	public static final String LOGBOOK_ID4 = "bus5Logbook";
	public static int mySalt = 1;
	private static MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder("SHA");

	/**
	 * 
	 * @return
	 */
	public static FacilityAvailability getValidFacilityAvailability() {
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		end.add(Calendar.MONTH, 1);
		FacilityAvailability result = new FacilityAvailability(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE, start, end);
		User user = getNewUniqueValidUser(end.hashCode() + "");
		user.insert();
		result.setUserSetThis(user);
		result.setAvailable(FacilityAvailability.COMPLETE_AVAILABLE);
		return result;

	}

	/**
	 * 
	 * @return
	 */
	public static Calendar getTomorrow() {
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.add(Calendar.DAY_OF_YEAR, 1);
		return tomorrow;
	}

	/**
	 * 
	 * @param salt
	 * @return
	 */
	public static FacilityAvailability getValidFacilityAvailability(int salt) {
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		end.set(Calendar.MONTH, salt);
		FacilityAvailability result = new FacilityAvailability(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE, start, end);
		User user = getNewUniqueValidUser(salt + "");
		user.insert();
		result.setUserSetThis(user);
		result.setAvailable(FacilityAvailability.COMPLETE_AVAILABLE);
		return result;
	}

	/**
	 * 
	 * @return
	 */
	public static TimeBookingRequest getBookingRequest(FacilityBookable facility) {
    BookingRule bookingRule = facility.getBookingRule();
    SetOfRulesForARole defaultSet = new SimpleSetOfRulesForARoleBean();
    defaultSet.setMaxBookableCapacityUnits(1);
    defaultSet.setMinBookableCapacityUnits(1);
    defaultSet.setMaxBookableTimeUnits(1);
    defaultSet.setMinBookableTimeUnits(1);
    defaultSet.setReminderMailMinutesBeforeStarting(1440);
    bookingRule.setDefaultSetOfRulesForARole(defaultSet);
    bookingRule.setSmallestMinutesBookable(15);
    bookingRule.setMustStartAt(null);
    User user = getNewUniqueValidUser(bookingRule.hashCode() + "");
    user.insert();
    int capacityUnitsInterestedIn = 1;
    int timeUnitsInterestedIn = 1;
    Calendar userInterestedIn = getTomorrow();
    return new TimeBookingRequest(bookingRule, user, capacityUnitsInterestedIn, timeUnitsInterestedIn, userInterestedIn);
	}
  public static TimeBookingRequest getBookingRequest() {
    return getBookingRequest(getFacilityBookable());
	}

	/**
	 * 
	 * @return
	 */
	public static TimeBooking getNewValidBooking() {
		return getNewValidBooking(getBookingRequest());
	}

	private static String getRandomString() {
		mySalt++;
		return encoder.encodePassword(new Date().toString() + mySalt, mySalt);
	}

	/**
	 * 
	 * @return
	 */
	public static LogbookEntry getNewValidLogbookEntry() {
		LogbookEntry le = new LogbookEntry();
		le.setContent(getRandomString());
		le.setHeadline(getRandomString());
		le.setLogbookId(LOGBOOK_ID);
		ArrayList<String> tags = new ArrayList<String>();
		Random rand = new Random();
		String[] randtags = { "notice", "problem", "request", "info" };
		tags.add(randtags[rand.nextInt(randtags.length)]);
		String[] randtags2 = { "zurwt", "sedc", "sdfvve", "klji", "asdfw", "hukla", "asdfae" };
		tags.add(randtags2[rand.nextInt(randtags2.length)]);
		tags.add(randtags2[rand.nextInt(randtags2.length)]);
		tags.add(randtags2[rand.nextInt(randtags2.length)]);
		le.setTags(tags);
		Calendar c = Calendar.getInstance();
		c.set(2008 - Math.abs(rand.nextInt(10)), Math.abs(rand.nextInt(11)), Math.abs(rand.nextInt(31)), Math.abs(rand.nextInt(24)), Math.abs(rand.nextInt(60)), Math.abs(rand.nextInt(60)));
		le.setDate(c.getTime());
		le.setLanguage(Locale.ENGLISH);
		User u = getNewUniqueValidUser(getRandomString());
		u.insert();
		le.setOfUserName(u.getUsername());
		return le;
	}

	/**
	 * 
	 * @return
	 */
	public static User getAdmin() {
		User result = getNewValidUser();
		result.setUsername(RoleConfigDao.getInstance().getUsernamesOfAdmins()[0]);
		result.setRoleId(RoleConfigDao.getInstance().getAdminId());
		return result;
	}

	/**
	 * 
	 * @return
	 */
	public static FacilityBookable getBookableQueueFacility() {
		return (FacilityBookable) FacilityConfigDao.getInstance().getConfiguredInstance(KEY_FACILITY_BOOKABLE_QUEUE);
	}

	/**
	 * 
	 * @param user
	 * @param facility
	 * @return
	 */
	public static QueueBooking getNewValidQueueBooking(User user, FacilityBookable facility) {
		return new QueueBooking(user, facility);
	}

	/**
	 * 
	 * @return
	 */
	public static QueueBooking getNewValidQueueBooking() {
		User user = getNewUniqueValidUser("" + mySalt++);
		user.insert();
		return getNewValidQueueBooking(user, getBookableQueueFacility());
	}

	/**
	 * 
	 * @return
	 */
	public static QueueBooking getNewValidAndBookedQueueBooking() {
		QueueBooking result = getNewValidQueueBooking();
		result.setBooked();
		return result;
	}

	private TeztBeanSimpleFactory() {
	}

	public static JobDataProcessing getNewValidJobDataProcessing() {
		JobDataProcessing result = new JobDataProcessing();
		result.setFacilityKey(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE);
		String username = "user_" + new Date().getTime();
		result.setUsername(username);
		ArrayList<Map<String, Object>> templates = new ArrayList<Map<String, Object>>();
		result.setTemplates(templates);
		result.setCreated(new Date().getTime());
		return result;
	}

  public static FacilityBookable getFacilityBookable(String keyFacility) {
    return (FacilityBookable) FacilityConfigDao.getInstance().getConfiguredInstance(keyFacility);
  }

}

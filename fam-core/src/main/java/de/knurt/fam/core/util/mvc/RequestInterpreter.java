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
package de.knurt.fam.core.util.mvc;

import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_DEFAULT_VALUE_COUNT_OF_ENTRIES_PER_PAGE;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_COUNT_OF_ENTRIES_PER_PAGE;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_DAY;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_DELETE;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_FACILITY;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_FROM;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_HOUR_OF_DAY;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_MINUTE_OF_HOUR;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_MONTH;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_ROLE;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_SECRET;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_TO;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_YEAR;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

import de.knurt.fam.core.aspects.security.auth.DirectPageAccess;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.config.FamCalendarConfiguration;
import de.knurt.fam.core.config.FamRequestContainer;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.config.statistics.FamStatistic;
import de.knurt.fam.core.model.config.statistics.FamStatisticContainer;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.fam.core.util.time.FamCalendar;
import de.knurt.fam.template.controller.json.DirectBookingRequestController;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * interpret given requests as it is build by {@link QueryStringBuilder}.
 * keys are the matching key to the given value in {@link QueryKeys}
 * 
 * @author Daniel Oltmanns
 * @since 0.20090916 (09/16/2009)
 */
public class RequestInterpreter {

	/**
	 * return the bookable facility out of the request or null if no key for it is
	 * given or the given key is not a bookable facility.
	 * 
	 * @param rq
	 *            of user
	 * @see FacilityConfigDao#bookableFacility(java.lang.String)
	 * @return the bookable facility out of the request or null if no key for it
	 *         given or the given key is not a bookable facility.
	 */
	public static FacilityBookable getBookableFacility(HttpServletRequest rq) {
		FacilityBookable bd = null;
		if (rq.getParameter(QUERY_KEY_FACILITY) != null) {
			if (FacilityConfigDao.isKey(rq.getParameter(QUERY_KEY_FACILITY)) && FacilityConfigDao.bookable(rq.getParameter(QUERY_KEY_FACILITY))) {
				bd = FacilityConfigDao.bookableFacility(rq.getParameter(QUERY_KEY_FACILITY));
			}
		}
		return bd;
	}

	/**
	 * return true, if the request has the parameter send by the link
	 * "request booking" in a calender view
	 * 
	 * @param rq
	 *            given
	 * @return true, if the request has the parameter send by the link
	 *         "request booking" in a calender view
	 */
	public static boolean isAjaxRequestFromRequestBookingLink(HttpServletRequest rq) {
		return getCapacityUnits(rq) != null && getAjaxFrom(rq) != null && getAjaxTo(rq) != null;
	}

	private static Calendar getAjaxFrom(HttpServletRequest rq) {
		return getAjaxCalendar(rq, QueryKeys.JS_KEY_FROM_DATE, QueryKeys.JS_KEY_FROM_TIME);
	}

	/**
	 * return the start of a time frame as given in <code>date</code>. format of
	 * date is the encoding of a {@link Calendar}.
	 * 
	 * @see QueryKeys#getEncodeStringForTimeFrame(java.util.Calendar,
	 *      java.util.Calendar)
	 * @param date
	 *            used to create the calendar
	 * @return the start of a time frame as given in <code>date</code>.
	 */
	public static Calendar getCalendarStart(String date) {
		Calendar result = null;
		String[] vals = date.split("\\.");
		if (vals.length == 10) {
			result = getCalendar(vals[0], vals[1], vals[2], vals[3], vals[4]);
		}
		return result;
	}

	/**
	 * return the end of a time frame as given in <code>date</code>. format of
	 * date is the encoding of a {@link Calendar}.
	 * 
	 * @see QueryKeys#getEncodeStringForTimeFrame
	 * @param date
	 *            used to create the calendar
	 * @return the end of a time frame as given in <code>date</code>.
	 */
	public static Calendar getCalendarEnd(String date) {
		Calendar result = null;
		String[] vals = date.split("\\.");
		if (vals.length == 10) {
			result = getCalendar(vals[5], vals[6], vals[7], vals[8], vals[9]);
		}
		return result;
	}

	private static Calendar getCalendar(String date) {
		Calendar result = null;
		String[] vals = date.split("\\.");
		if (vals.length == 5) {
			result = getCalendar(vals[0], vals[1], vals[2], vals[3], vals[4]);
		}
		return result;
	}

	/**
	 * return a calendar from given single values. if null use value of today.
	 * 
	 * @param yearQ
	 *            year of calendar
	 * @param monthQ
	 *            month of calendar
	 * @param dayOfMonthQ
	 *            day of month of calendar
	 * @param hourOfDayQ
	 *            hour of day of calendar
	 * @param minuteOfHourQ
	 *            minute of hour of calendar
	 * @return a calendar from given single values.
	 */
	public static Calendar getCalendar(String yearQ, String monthQ, String dayOfMonthQ, String hourOfDayQ, String minuteOfHourQ) {
		Calendar result = null;
		Integer year = getAsIntegerOrNull(yearQ);
		Integer month = getAsIntegerOrNull(monthQ);
		Integer dayOfMonth = getAsIntegerOrNull(dayOfMonthQ);
		Integer hourOfDay = getAsIntegerOrNull(hourOfDayQ);
		Integer minuteOfHour = getAsIntegerOrNull(minuteOfHourQ);
		if (year != null && month != null && dayOfMonth != null && hourOfDay != null && minuteOfHour != null) {
			result = getCalendar(year, month, dayOfMonth, hourOfDay, minuteOfHour);
		}
		return result;
	}

	private static Calendar getCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minuteOfHour) {
		Calendar result = FamCalendar.getInstance();
		result.set(Calendar.YEAR, year);
		result.set(Calendar.MONTH, month);
		result.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		result.set(Calendar.HOUR_OF_DAY, hourOfDay);
		result.set(Calendar.MINUTE, minuteOfHour);
		result.set(Calendar.SECOND, 0);
		result.set(Calendar.MILLISECOND, 0);
		return result;
	}

	/**
	 * return given string parsed as int or <code>null</code> on
	 * {@link NumberFormatException}.
	 * 
	 * @see Integer#parseInt(java.lang.String)
	 * @param integerCandidate
	 *            parsed to int
	 * @return given string parsed as int or <code>null</code> on
	 *         {@link NumberFormatException}.
	 */
	public static Integer getAsIntegerOrNull(String integerCandidate) {
		Integer result = null;
		try {
			result = Integer.parseInt(integerCandidate);
		} catch (NumberFormatException e) {
		}
		return result;
	}

	private static Calendar getAjaxCalendar(HttpServletRequest rq, String keyDate, String baseKeyTime) {
		Calendar result = null;
		String valueHour = rq.getParameter(baseKeyTime + QueryKeys.QUERY_KEY_HOUR_OF_DAY);
		String valueMinuteOfHour = rq.getParameter(baseKeyTime + QueryKeys.QUERY_KEY_MINUTE_OF_HOUR);
		if (rq.getParameter(keyDate) != null && valueMinuteOfHour != null && valueHour != null) {
			result = getCalendar(rq.getParameter(keyDate) + "." + valueHour + "." + valueMinuteOfHour);
		}
		return result;
	}

	private static Calendar getAjaxTo(HttpServletRequest rq) {
		return getAjaxCalendar(rq, QueryKeys.JS_KEY_TO_DATE, QueryKeys.JS_KEY_TO_TIME);
	}

	/**
	 * return a {@link TimeBooking} wish from given request. the booking wish
	 * starts from calendar and the duration is one year. user is always the
	 * user auth.
	 * 
	 * @see SessionAuth#user(javax.servlet.http.HttpServletRequest)
	 * @param cal
	 *            marking the start of the booking wish
	 * @param facilityKey
	 *            for the facility of the booking wish
	 * @param request
	 *            got
	 * @return a {@link TimeBooking} wish from given request.
	 */
	public static TimeBookingRequest getBookingWishFromRequest(Calendar cal, String facilityKey, HttpServletRequest request, User sessionAuthUser) {
		if (!FacilityConfigDao.bookable(facilityKey)) {
			facilityKey = FacilityConfigDao.getUnknownBookableFacility().getKey();
		}
		BookingRule br = FacilityConfigDao.bookingRule(facilityKey);
		int capacityUnits = getCapacityUnits(facilityKey, request, sessionAuthUser);
		int timeUnits = getTimeUnits(facilityKey, request, sessionAuthUser);
		return new TimeBookingRequest(br, sessionAuthUser, capacityUnits, timeUnits, cal);
	}

	/**
	 * return the booking wish from the given request. assume that a calendar
	 * and a facility key is in the request as well.
	 * 
	 * @see #getBookingWishFromRequest(java.util.Calendar, java.lang.String,
	 *      javax.servlet.http.HttpServletRequest)
	 * @see #getCalendar(javax.servlet.http.HttpServletRequest)
	 * @param request
	 *            got
	 * @return the booking wish from the given request.
	 */
	public static TimeBookingRequest getBookingWishFromRequest(HttpServletRequest request, User sessionAuthUser) {
		return getBookingWishFromRequest(getCalendar(request), request.getParameter(QUERY_KEY_FACILITY), request, sessionAuthUser);
	}

	/**
	 * return the value of {@link QueryKeys#QUERY_KEY_DELETE}
	 * 
	 * @param rq
	 *            got
	 * @return the value of {@link QueryKeys#QUERY_KEY_DELETE}
	 */
	public static String getDelete(HttpServletRequest rq) {
		return rq.getParameter(QueryKeys.QUERY_KEY_DELETE);
	}

	/**
	 * return the value of {@link QueryKeys#QUERY_KEY_UNITS_CAPACITY}. if not
	 * set, return the min bookable capacity units that is the default of the
	 * given facility for the user in the request. if there is no user in the
	 * request, get the capacity units that is the default on the facility for
	 * undifined user roles.
	 * 
	 * @see BookingRule#getSetOfRulesForARole(User)
	 * @see BookingRule#getMinBookableCapacityUnits()
	 * @param rq
	 *            got
	 * @return the value of {@link QueryKeys#QUERY_KEY_UNITS_CAPACITY}
	 */
	private static int getCapacityUnits(String facilityKey, HttpServletRequest rq, User sessionAuthUser) {
		String result = rq.getParameter(QueryKeys.QUERY_KEY_UNITS_CAPACITY);
		if (result != null) {
			return Integer.parseInt(result);
		} else {
			return FacilityConfigDao.bookingRule(facilityKey).getMinBookableCapacityUnits(sessionAuthUser);
		}
	}

	/**
	 * return the value of
	 * {@link QueryKeys#QUERY_KEY_COUNT_OF_ENTRIES_PER_PAGE}. if not set,
	 * return {@link QueryKeys#QUERY_DEFAULT_VALUE_COUNT_OF_ENTRIES_PER_PAGE}
	 * 
	 * @param rq
	 *            got
	 * @return the value of
	 *         {@link QueryKeys#QUERY_KEY_COUNT_OF_ENTRIES_PER_PAGE}
	 */
	public static int getCountOfEntriesPerPage(HttpServletRequest rq) {
		String result = rq.getParameter(QUERY_KEY_COUNT_OF_ENTRIES_PER_PAGE);
		if (result == null) {
			result = QUERY_DEFAULT_VALUE_COUNT_OF_ENTRIES_PER_PAGE;
		}
		return Integer.parseInt(result);
	}

	/**
	 * return the value of {@link QueryKeys#QUERY_KEY_FROM}.
	 * 
	 * @param rq
	 *            got
	 * @return the value of {@link QueryKeys#QUERY_KEY_FROM}
	 */
	public static Integer getFrom(HttpServletRequest rq) {
		return getAsIntegerOrNull(rq.getParameter(QUERY_KEY_FROM));
	}

	/**
	 * return the value of {@link QueryKeys#QUERY_KEY_FROM}. if not set,
	 * return the given value.
	 * 
	 * @param rq
	 *            got
	 * @param valueOnNull
	 *            used if nothing found in request
	 * @return the value of {@link QueryKeys#QUERY_KEY_FROM}
	 */
	public static int getFrom(HttpServletRequest rq, int valueOnNull) {
		Integer result = getFrom(rq);
		if (result == null) {
			result = valueOnNull;
		}
		return result;
	}

	/**
	 * return the value of {@link QueryKeys#QUERY_KEY_TO}.
	 * 
	 * @param rq
	 *            got
	 * @return the value of {@link QueryKeys#QUERY_KEY_TO}
	 */
	public static Integer getTo(HttpServletRequest rq) {
		return getAsIntegerOrNull(rq.getParameter(QUERY_KEY_TO));
	}

	/**
	 * return the value of {@link QueryKeys#QUERY_KEY_TO}. if not set, return
	 * the given value.
	 * 
	 * @param rq
	 *            got
	 * @param valueOnNull
	 *            used if nothing found in request
	 * @return the value of {@link QueryKeys#QUERY_KEY_TO}
	 */
	public static int getTo(HttpServletRequest rq, String valueOnNull) {
		Integer result = getTo(rq);
		if (result == null) {
			result = Integer.parseInt(valueOnNull);
		}
		return result;
	}

	private static int getTimeUnits(String facilityKey, HttpServletRequest rq, User sessionAuthUser) {
		String got = rq.getParameter(QueryKeys.QUERY_KEY_UNITS_TIME);
		int result = -1;
		if (got != null) {
			result = Integer.parseInt(got);
			if (FacilityConfigDao.bookingRule(facilityKey).getMinBookableTimeUnits(sessionAuthUser) > result) {
				result = FacilityConfigDao.bookingRule(facilityKey).getMinBookableTimeUnits(sessionAuthUser);
			} else if (result > FacilityConfigDao.bookingRule(facilityKey).getMaxBookableTimeUnits(sessionAuthUser)) {
				result = FacilityConfigDao.bookingRule(facilityKey).getMaxBookableTimeUnits(sessionAuthUser);
			}
		} else {
			result = FacilityConfigDao.bookableFacility(facilityKey).getBookingRule().getMinBookableTimeUnits(sessionAuthUser);
		}
		return result;
	}

	/**
	 * return the {@link FacilityAvailability} queried in the request got
	 * 
	 * @param request
	 *            got
	 * @return the {@link FacilityAvailability} queried in the request got
	 */
	public static FacilityAvailability getFacilityAvailabilityOfConfiguredDayForDisplaying(HttpServletRequest request) {
		Calendar start = getCalendar(request);
		start.set(Calendar.HOUR_OF_DAY, FamCalendarConfiguration.hourStart());
		start.set(Calendar.MINUTE, 0);
		Calendar end = (Calendar) start.clone();
		start.set(Calendar.HOUR_OF_DAY, FamCalendarConfiguration.hourStop());
		return new FacilityAvailability(request.getParameter(QUERY_KEY_FACILITY), start, end);
	}

	/**
	 * return a {@link FacilityAvailability} from request. return null, if request
	 * is not there or invalid. this is when querying with an invalid facilityKey
	 * or an invalid timeframe format. <br />
	 * this assumes, that there can be only send one {@link FacilityAvailability} per
	 * request!!!! <br />
	 * this must have set all single calendar components (year, month, day of
	 * year, hour of day, minute of hour) to not return <code>null</code>. <br />
	 * if value "from" or "to" is not set, the current date is taken! <br />
	 * if "to" is before "from", it is not switched, but it is pushed to future
	 * in 1 step of <code>interval</code>. if "to" is still before "from" then,
	 * return <code>null</code> (because of this input shall not be possible).
	 * this is very important to insure inputs like
	 * "yearly from dec 30 to jan 2".
	 * 
	 * @see FacilityAvailability#getFacilityKey()
	 * @see QueryKeys#getEncodeStringForTimeFrame(java.util.Calendar,
	 *      java.util.Calendar)
	 * @param request
	 *            got
	 * @return return a {@link FacilityAvailability} from request.
	 */
	public static FacilityAvailability getCompleteFacilityAvailabilityForInsertion(HttpServletRequest request, User sessionAuthUser) {
		FacilityAvailability result = null;
		Facility d = getFacility(request);
		if (d != null && FacilityConfigDao.isKey(d.getKey())) {
			String facilityKey = d.getKey();
			Integer availability = getAvailability(request);
			if (availability != null) {
				Integer interval = getInterval(request);
				if (interval != null) {
					String notice = getNotice(request);
					if (notice != null) {
						User user = sessionAuthUser;
						if (user != null) {
							String encodedString = getEncodedString4TimeFrame(request);
							if (encodedString != null) {
								Calendar start = getCalendarStart(encodedString);
								Calendar end = getCalendarEnd(encodedString);
								if (end.before(start)) {
									end.add(interval, 1);
								}
								if (!end.before(start)) {
									TimeFrame tf = new SimpleTimeFrame(start, end);
									result = new FacilityAvailability(facilityKey, tf);
									result.setAvailable(availability);
									result.setInterval(interval);
									result.setNotice(notice);
									result.setUserSetThis(user);
								}
							}
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * return value of interval in request or null if not set.
	 * 
	 * @see QueryKeys#QUERY_KEY_ITERATION
	 * @param request
	 *            got
	 * @return value of interval in request or null if not set.
	 */
	public static Integer getInterval(HttpServletRequest request) {
		return RequestInterpreter.getAsIntegerOrNull(request.getParameter(QueryKeys.QUERY_KEY_ITERATION));
	}

	/**
	 * return the availability in the request. return null, if nothing given or
	 * given value is invalid.
	 * 
	 * @see FacilityAvailability#getAvailable()
	 * @see QueryKeys.QUERY_KEY_AVAILABLILITY
	 * @param request
	 *            got
	 * @return the availability in the request.
	 */
	public static Integer getAvailability(HttpServletRequest request) {
		Integer result = null;
		try {
			result = Integer.parseInt(request.getParameter(QueryKeys.QUERY_KEY_AVAILABLILITY));
		} catch (NumberFormatException e) {
			result = null;
		}
		if (result != null && !FacilityAvailability.isValidAvailability(result)) {
			result = null;
		}
		return result;
	}

	/**
	 * return the calendar of the given request. assuming parameters for each
	 * field. if a field is not set, use the current time on this field. use the
	 * locale as given in request.
	 * 
	 * @see FamRequestContainer#locale()
	 * @see QueryKeys#QUERY_KEY_YEAR
	 * @see QueryKeys#QUERY_KEY_MONTH
	 * @see QueryKeys#QUERY_KEY_DAY
	 * @see QueryKeys#QUERY_KEY_HOUR_OF_DAY
	 * @see QueryKeys#QUERY_KEY_MINUTE_OF_HOUR
	 * @param rq
	 *            got
	 * @return the calendar of the given request.
	 */
	public static Calendar getCalendar(HttpServletRequest rq) {
		Calendar c = Calendar.getInstance(FamRequestContainer.locale());
		try {
			c.set(Calendar.YEAR, Integer.parseInt(rq.getParameter(QUERY_KEY_YEAR)));
		} catch (NumberFormatException e) {
		} // so what
		try {
			c.set(Calendar.MONTH, Integer.parseInt(rq.getParameter(QUERY_KEY_MONTH)));
		} catch (NumberFormatException e) {
		} // so what
		try {
			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(rq.getParameter(QUERY_KEY_DAY)));
		} catch (NumberFormatException e) {
		} // so what
		try {
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(rq.getParameter(QUERY_KEY_HOUR_OF_DAY)));
		} catch (NumberFormatException e) {
		}
		try {
			c.set(Calendar.MINUTE, Integer.parseInt(rq.getParameter(QUERY_KEY_MINUTE_OF_HOUR)));
		} catch (NumberFormatException e) {
		} // so what
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}

	/**
	 * return a {@link TimeFrame} out of the from and to value or null.
	 * 
	 * @see #getFrom(javax.servlet.http.HttpServletRequest)
	 * @see #getTo(javax.servlet.http.HttpServletRequest)
	 * @param rq
	 *            of user
	 * @return a {@link TimeFrame} out of the from and to value or null.
	 */
	public static TimeFrame getTimeFrame(HttpServletRequest rq) {
		TimeFrame result = null;
		int from = getFrom(rq, 0);
		int to = getTo(rq, QueryKeys.QUERY_DEFAULT_VALUE_COUNT_OF_ENTRIES_PER_PAGE);
		if (from > 0 && to > 0) {
			result = new SimpleTimeFrame(from, to);
		}
		return result;
	}

	/**
	 * return a {@link TimeFrame} of a entire week where a day could be found in
	 * the request. if no day found, return null.
	 * 
	 * @param rq
	 *            of user
	 * @return a {@link TimeFrame} of a entire week
	 */
	public static TimeFrame getTimeFrameOfWeek(HttpServletRequest rq) {
		TimeFrame result = null;
		Calendar start = getCalendar(rq);
		if (start != null) {
			while (start.get(Calendar.DAY_OF_WEEK) != start.getFirstDayOfWeek()) {
				start.add(Calendar.DAY_OF_YEAR, -1);
			}
			start.set(Calendar.HOUR_OF_DAY, 0);
			start.set(Calendar.MINUTE, 0);
			start.set(Calendar.SECOND, 0);
			start.set(Calendar.MILLISECOND, 0);
			Calendar end = (Calendar) start.clone();
			end.add(Calendar.DAY_OF_YEAR, 7);
			result = new SimpleTimeFrame(start, end);
		}
		return result;
	}

	/**
	 * this is an out of the box function creating a time frame for a ajax
	 * request. it takes the given year, set the given dayOfYear (given as id
	 * node of td element) and given times ("tfstart", "tfend" given in minutes
	 * of day).
	 * 
	 * @see DirectBookingRequestController
	 * @see "calendar.js"
	 * @param rq
	 *            of user
	 * @return time frame for given request
	 */
	public static TimeFrame getTimeFrameWithDayOfYearAndMinutesStartEnd(HttpServletRequest rq) {
		TimeFrame result = null;
		if (rq.getParameter("tdid") != null && rq.getParameter("tfstart") != null && rq.getParameter("tfend") != null) {
			String tdid = rq.getParameter("tdid");
			try {
				Integer tfstart = Integer.parseInt(rq.getParameter("tfstart"));
				Integer tfend = Integer.parseInt(rq.getParameter("tfend"));
				Integer dayOfYear = Integer.parseInt(tdid.substring(tdid.indexOf("_") + 1));
				if (dayOfYear != null && tfstart != null && tfend != null) {
					Calendar start = getCalendar(rq);
					if (start != null) {
						start.set(Calendar.SECOND, 0);
						start.set(Calendar.MILLISECOND, 0);
						start.set(Calendar.DAY_OF_YEAR, dayOfYear);
						start.set(Calendar.HOUR_OF_DAY, (int) Math.floor(tfstart / 60));
						start.set(Calendar.MINUTE, (int) Math.floor(tfstart % 60));
						Calendar end = (Calendar) start.clone();
						end.set(Calendar.HOUR_OF_DAY, (int) Math.floor(tfend / 60));
						end.set(Calendar.MINUTE, (int) Math.floor(tfend % 60));
						result = new SimpleTimeFrame(start, end);
					}
				}
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}

	/**
	 * return the value of {@link QueryKeys#QUERY_KEY_UNITS_CAPACITY}.
	 * 
	 * @param rq
	 *            got
	 * @return the value of {@link QueryKeys#QUERY_KEY_UNITS_CAPACITY}
	 */
	public static Integer getCapacityUnits(HttpServletRequest rq) {
		Integer result = null;
		try {
			result = Integer.parseInt(rq.getParameter(QueryKeys.QUERY_KEY_UNITS_CAPACITY));
		} catch (NumberFormatException e) {
		}
		return result;
	}

	/**
	 * return a time frame as queried by javascript.
	 * 
	 * @see QueryKeys#JS_KEY_FROM_DATE
	 * @see QueryKeys#JS_KEY_FROM_TIME
	 * @see QueryKeys#JS_KEY_TO_DATE
	 * @see QueryKeys#JS_KEY_TO_TIME
	 * @param rq
	 *            got
	 * @return a time frame as queried by javascript.
	 */
	public static TimeFrame getTimeFrameFromAjaxRequestBookingLink(HttpServletRequest rq) {
		TimeFrame result = null;
		Calendar start = getAjaxFrom(rq);
		Calendar end = getAjaxTo(rq);
		if (start != null && end != null) {
			result = new SimpleTimeFrame(start, end);
		}
		return result;
	}

	/**
	 * return the booking as given from {@link QueryKeys#QUERY_KEY_BOOKING}.
	 * contact the database for that.
	 * 
	 * @param rq
	 *            got
	 * @return the value of {@link QueryKeys#QUERY_KEY_BOOKING}
	 */
	public static Booking getBooking(HttpServletRequest rq) {
		Booking result = null;
		if (rq.getParameter(QueryKeys.QUERY_KEY_BOOKING) != null) {
			try {
				Integer id = Integer.parseInt(rq.getParameter(QueryKeys.QUERY_KEY_BOOKING));
				Booking example = TimeBooking.getEmptyExampleBooking();
				example.setId(id);
				result = FamDaoProxy.bookingDao().getOneLike(example);
			} catch (NumberFormatException e) { // no valid id
			}
		}
		return result;
	}

	/**
	 * return the notice as given from {@link QueryKeys#QUERY_KEY_TEXT_NOTICE}
	 * .
	 * 
	 * @param rq
	 *            got
	 * @return the value of {@link QueryKeys#QUERY_KEY_TEXT_NOTICE}
	 */
	public static String getNotice(HttpServletRequest rq) {
		String result = null;
		if (rq.getParameter(QueryKeys.QUERY_KEY_TEXT_NOTICE) != null) {
			result = rq.getParameter(QueryKeys.QUERY_KEY_TEXT_NOTICE);
		}
		return result;
	}

	/**
	 * return true, if {@link QueryKeys#QUERY_KEY_POST_REQUEST_SUCCEEDED}
	 * equals "1"
	 * 
	 * @param rq
	 *            got
	 * @return true, if {@link QueryKeys#QUERY_KEY_POST_REQUEST_SUCCEEDED}
	 *         equals "1"
	 */
	public static boolean hasSentFlag(HttpServletRequest rq) {
		return rq.getParameter(QueryKeys.QUERY_KEY_POST_REQUEST_SUCCEEDED) != null && rq.getParameter(QueryKeys.QUERY_KEY_POST_REQUEST_SUCCEEDED).equals("1");
	}

	/**
	 * return true, if {@link QueryKeys#JS_AJAX_FLAG} is
	 * {@link QueryKeys#YES}
	 * 
	 * @param rq
	 *            request searched in
	 * @return true, if {@link QueryKeys#JS_AJAX_FLAG} is
	 *         {@link QueryKeys#YES}
	 */
	public static boolean hasAjaxFlag(HttpServletRequest rq) {
		return rq.getParameter(QueryKeys.JS_AJAX_FLAG) != null && rq.getParameter(QueryKeys.JS_AJAX_FLAG).equals(QueryKeys.YES);
	}

	/**
	 * return true, if the yes no flag is set to yes. if nothing set or flag is
	 * set to no, return false.
	 * 
	 * @see QueryKeys#YES
	 * @see QueryKeys#NO
	 * @see QueryKeys#QUERY_KEY_YES_NO
	 * @param rq
	 *            request got
	 * @return true, if the yes no flag is set to yes.
	 */
	public static boolean isYes(HttpServletRequest rq) {
		return rq.getParameter(QueryKeys.QUERY_KEY_YES_NO) != null && rq.getParameter(QueryKeys.QUERY_KEY_YES_NO).equals(QueryKeys.YES);
	}

	/**
	 * return true, if the yes no flag is set to no. if nothing set or flag is
	 * set to yes, return false.
	 * 
	 * @see QueryKeys#YES
	 * @see QueryKeys#NO
	 * @see QueryKeys#QUERY_KEY_YES_NO
	 * @param rq
	 *            request got
	 * @return true, if the yes no flag is set to no.
	 */
	public static boolean isNo(HttpServletRequest rq) {
		return rq.getParameter(QueryKeys.QUERY_KEY_YES_NO) != null && rq.getParameter(QueryKeys.QUERY_KEY_YES_NO).equals(QueryKeys.NO);
	}


	/**
	 * return true, if the request has a parameter for a facility
	 * 
	 * @param rq
	 *            given
	 * @return true, if the request has a parameter for a facility
	 */
	public static boolean hasFacility(HttpServletRequest rq) {
		return rq.getParameter(QueryKeys.QUERY_KEY_FACILITY) != null;
	}

	/**
	 * return the facility in the request or null if nothing there.
	 * 
	 * @param rq
	 *            got
	 * @return the facility in the request or null if nothing there.
	 */
	public static Facility getFacility(HttpServletRequest rq) {
		Facility bd = null;
		if (rq.getParameter(QUERY_KEY_FACILITY) != null) {
			bd = FacilityConfigDao.facility(rq.getParameter(QUERY_KEY_FACILITY));
		}
		return bd;
	}

	/**
	 * return true, if the request have a delete flag. this is name "u" and
	 * value "true".
	 * 
	 * @see QueryKeys#QUERY_KEY_DELETE
	 * @see QueryStringBuilder#getDeleteQueryString()
	 * @param rq
	 *            got
	 * @return true, if the request have a delete flag.
	 */
	public static boolean hasDeleteFlag(HttpServletRequest rq) {
		boolean result = false;
		if (rq.getParameter(QUERY_KEY_DELETE) != null && rq.getParameter(QUERY_KEY_DELETE).equals("true")) {
			result = true;
		}
		return result;
	}

	/**
	 * return true, if details shall be shown. otherwise and if no information
	 * is given, return false.
	 * 
	 * @see QueryStringBuilder#getShowDetails(boolean)
	 * @param rq
	 *            got
	 * @return true, if details shall be shown
	 */
	public static boolean showDetails(HttpServletRequest rq) {
		return rq.getParameter(QueryKeys.QUERY_KEY_SHOW_DETAILS) != null && rq.getParameter(QueryKeys.QUERY_KEY_SHOW_DETAILS).equals(QueryKeys.YES);
	}

	/**
	 * return value of {@link QueryKeys#QUERY_KEY_OF} or null.
	 * 
	 * @param rq
	 *            got
	 * @return value of {@link QueryKeys#QUERY_KEY_OF} or null.
	 */
	public static String getOf(HttpServletRequest rq) {
		return rq.getParameter(QueryKeys.QUERY_KEY_OF);
	}

	/**
	 * return the job step id in the request. if no value there, return
	 * {@link JobStep#USER_INPUT}
	 * 
	 * @param rq
	 *            got
	 * @return the job step id in the request.
	 */
	public static Integer getJobStepId(HttpServletRequest rq) {
		Integer result = null;
		String tmp = getOf(rq);
		if (tmp != null) {
			try {
				int tmpi = Integer.parseInt(tmp);
				result = tmpi; // no exception here
			} catch (NumberFormatException e) {
			} // so what
		}
		return result;
	}

	/**
	 * return day in query. that is
	 * <code>request.getParameter(AsQueryKeys.QUERY_KEY_DAY)</code>
	 * 
	 * @param request
	 *            got
	 * @return day in query.
	 */
	public static String getPureDay(HttpServletRequest request) {
		return request.getParameter(QueryKeys.QUERY_KEY_DAY);
	}

	/**
	 * return the statistic ask for or null, if no statistic is given or given
	 * statistic is not configured.
	 * 
	 * @param rq
	 *            got
	 * @return the statistic ask for or null, if no statistic is given or given
	 *         statistic is not configured.
	 */
	public static FamStatistic getStatistic(HttpServletRequest rq) {
		FamStatistic result = null;
		String sid = RequestInterpreter.getOf(rq);
		if (sid != null) {
			int id = Integer.parseInt(sid);
			result = FamStatisticContainer.getInstance().getStatistic(id);
		}
		return result;
	}

	/**
	 * return the user represented in the request.
	 * 
	 * @see QueryKeys#QUERY_KEY_USER
	 * @param rq
	 *            got
	 * @return the user represented in the request.
	 */
	public static User getUser(HttpServletRequest rq) {
		User result = null;
		String username = rq.getParameter(QueryKeys.QUERY_KEY_USER);
		if (username != null) {
			result = FamDaoProxy.userDao().getUserFromUsername(username);
		}
		return result;
	}

	/**
	 * return the given time units or null if nothing is given.
	 * 
	 * @see QueryKeys#QUERY_KEY_UNITS_TIME
	 * @param request
	 *            got
	 * @return the given time units or null if nothing is given.
	 */
	public static Integer getTimeUnits(HttpServletRequest request) {
		Integer result = null;
		if (request.getParameter(QueryKeys.QUERY_KEY_UNITS_TIME) != null) {
			result = getAsIntegerOrNull(request.getParameter(QueryKeys.QUERY_KEY_UNITS_TIME));
		}
		return result;
	}

	/**
	 * try to find an encoded string in the request and build a time frame out
	 * of it.
	 * 
	 * @see QueryKeys#getEncodeStringForTimeFrame(java.util.Calendar,
	 *      java.util.Calendar)
	 * @param request
	 *            got
	 * @return time frame found or null, if nothing is found
	 */
	public static TimeFrame getTimeFrameFromEncodedString(HttpServletRequest request) {
		TimeFrame result = null;
		String encodedString = getEncodedString4TimeFrame(request);
		if (encodedString != null) {
			result = getTimeFrameFromEncodedString(encodedString);
		}
		return result;
	}

	private static String getEncodedString4TimeFrame(HttpServletRequest request) {
		String result = request.getParameter(QueryKeys.QUERY_KEY_FROM);
		if (result == null) {
			result = request.getParameter(QueryKeys.QUERY_KEY_FROM + QueryKeys.QUERY_KEY_DAY);
			if (result != null) {
				String tmp = request.getParameter(QueryKeys.QUERY_KEY_FROM + QueryKeys.QUERY_KEY_HOUR_OF_DAY);
				if (tmp != null) {
					result += "." + tmp;
					tmp = request.getParameter(QueryKeys.QUERY_KEY_TO + QueryKeys.QUERY_KEY_DAY);
					if (tmp != null) {
						result += "." + tmp;
						tmp = request.getParameter(QueryKeys.QUERY_KEY_TO + QueryKeys.QUERY_KEY_HOUR_OF_DAY);
						if (tmp != null) {
							result += "." + tmp;
						}
					}
				}
			}
		} else {
			result += "." + request.getParameter(QueryKeys.QUERY_KEY_TO);
		}
		return result;
	}

	private static TimeFrame getTimeFrameFromEncodedString(String encodedString) {
		TimeFrame result = null;
		if (encodedString != null) {
			result = new SimpleTimeFrame(getCalendarStart(encodedString), getCalendarEnd(encodedString));
		}
		return result;
	}

	/**
	 * return the existing facility availability with the id given in the request. <br />
	 * the id is given in {@link QueryKeys#QUERY_KEY_AVAILABLILITY}. if no id
	 * given or no availability with given id exists, return <code>null</code>.
	 * 
	 * @see QueryKeys#QUERY_KEY_AVAILABLILITY
	 * @param rq
	 *            request got / to use
	 * @return the existing facility availability with the id given in the
	 *         request.
	 */
	public static FacilityAvailability getExistingFacilityAvailabilityWithId(HttpServletRequest rq) {
		Integer idFacilityAvailability = getAsIntegerOrNull(rq.getParameter(QueryKeys.QUERY_KEY_AVAILABLILITY));
		FacilityAvailability result = null;
		if (idFacilityAvailability != null) {
			FacilityAvailability example = new FacilityAvailability();
			example.setId(idFacilityAvailability.intValue());
			result = FamDaoProxy.facilityDao().getOneLike(example);
		}
		return result;
	}


	/**
	 * return true, if the request contains a direct access for the given view
	 * name.
	 * 
	 * @see DirectPageAccess
	 * @param viewName
	 *            to check
	 * @param request
	 *            to check
	 * @return true, if the request contains a direct access for the given view
	 *         name.
	 */
	public static boolean containsDirectAccess(String viewName, HttpServletRequest request) {
		boolean result = false;
		if (request.getParameter(QueryKeys.QUERY_KEY_USER) != null && request.getParameter(QueryKeys.QUERY_KEY_SECRET) != null) {
			result = DirectPageAccess.getInstance().isAuth(viewName, request.getParameter(QueryKeys.QUERY_KEY_USER), request.getParameter(QueryKeys.QUERY_KEY_SECRET));
		}
		return result;
	}

	private RequestInterpreter() {
	}

	public static Integer getPageNo(HttpServletRequest rq) {
		Integer result = null;
		try {
			result = Integer.parseInt(rq.getParameter(QueryKeys.QUERY_KEY_PAGENO));
		} catch (Exception e) {
			// number format, null pointer exception etc. stay null ...
		}
		return result;
	}

	public static boolean hasPageNo(HttpServletRequest rq) {
		return getPageNo(rq) != null;
	}

	public static String getSecret(HttpServletRequest request) {
		return request.getParameter(QUERY_KEY_SECRET);
	}

	public static String getToAsString(HttpServletRequest request) {
		return request.getParameter(QUERY_KEY_TO);
	}

	public static String getRole(HttpServletRequest request) {
		return request.getParameter(QUERY_KEY_ROLE);
	}
}

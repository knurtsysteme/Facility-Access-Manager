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

import static de.knurt.fam.core.util.mvc.QueryKeys.JS_AJAX_FLAG;
import static de.knurt.fam.core.util.mvc.QueryKeys.MONTH;
import static de.knurt.fam.core.util.mvc.QueryKeys.OVERVIEW;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_DEFAULT_VALUE_COUNT_OF_ENTRIES_PER_PAGE;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_DEFAULT_VALUE_POST_REQUEST_SUCCEEDED;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_BOOKING;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_CALENDAR_VIEW;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_COUNT_OF_ENTRIES_PER_PAGE;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_DAY;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_DELETE;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_FACILITY;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_HOUR_OF_DAY;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_LOGBOOK;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_MINUTE_OF_HOUR;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_MONTH;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_PAGENO;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_POST_REQUEST_SUCCEEDED;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_YEAR;
import static de.knurt.fam.core.util.mvc.QueryKeys.WEEK;

import java.util.Calendar;

import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.Logbook;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.util.time.CalendarViewResolver;
import de.knurt.heinzelmann.util.query.QueryString;
import de.knurt.heinzelmann.util.shopping.Purchasable;

/**
 * static methods to add key values to a {@link QueryString}. keys are the
 * matching key to the given value in {@link QueryKeys}
 * 
 * @author Daniel Oltmanns
 * @since 0.20090818 (08/18/2009)
 */
public class QueryStringBuilder {
	/**
	 * default page number to show.
	 */
	public final static String QUERY_LOGBOOK_DEFAULT_VALUE_PAGENO = "1";

	/**
	 * return query string with delete = true;
	 * 
	 * @return query string with delete = true;
	 */
	public static QueryString getDeleteQueryString() {
		QueryString qs = new QueryString();
		qs.put(QUERY_KEY_DELETE, "true");
		return qs;
	}
	/**
	 * return a standard {@link QueryString} for the logbook of given key. this
	 * will be the choice of the first time entering a logbook.
	 * 
	 * @param key
	 *            of logbook this {@link QueryString} is for.
	 * @return a standard {@link QueryString} for the logbook of given key
	 */
	public final static QueryString getLogbookQueryString(String key) {
		return getLogbookQueryString(key, QUERY_LOGBOOK_DEFAULT_VALUE_PAGENO, QUERY_DEFAULT_VALUE_COUNT_OF_ENTRIES_PER_PAGE);
	}

	/**
	 * this is a special query string that shows the successful posting
	 * 
	 * @param key
	 *            of the logbook
	 * @return query string for showing posting success
	 */
	public final static QueryString getLogbookSuccessQueryString(String key) {
		QueryString qs = getLogbookQueryString(key, QUERY_LOGBOOK_DEFAULT_VALUE_PAGENO, QUERY_DEFAULT_VALUE_COUNT_OF_ENTRIES_PER_PAGE);
		qs.put(QUERY_KEY_POST_REQUEST_SUCCEEDED, QUERY_DEFAULT_VALUE_POST_REQUEST_SUCCEEDED);
		return qs;
	}


	public final static QueryString getLogbookQueryString(String key, String pageno, String countofentriesperpage) {
		QueryString qs = new QueryString();
		qs.put(QUERY_KEY_LOGBOOK, key);
		qs.put(QUERY_KEY_PAGENO, pageno);
		qs.put(QUERY_KEY_COUNT_OF_ENTRIES_PER_PAGE, countofentriesperpage);
		return qs;
	}
	/**
	 * add a {@link TimeBooking} to the given queryString.
	 * 
	 * @param queryString
	 *            {@link TimeBooking} added to
	 * @param booking
	 *            being added to queryString
	 */
	public static void add(QueryString queryString, Booking booking) {
		queryString.put(QUERY_KEY_BOOKING, booking.getId());
	}

	/**
	 * return a new query string representing the given facility.
	 * 
	 * @param facility
	 *            to put into a {@link QueryString}
	 * @return a new query string representing the given facility.
	 */
	public static QueryString getQueryString(Facility facility) {
		QueryString result = new QueryString();
		result.put(QueryKeys.QUERY_KEY_FACILITY, facility.getKey());
		return result;
	}

	/**
	 * add a value representing the given facility to given {@link QueryString}.
	 * 
	 * @param queryString
	 *            given
	 * @param facility
	 *            to put into given {@link QueryString}
	 */
	public static void add(QueryString queryString, Facility facility) {
		queryString.put(QUERY_KEY_FACILITY, facility.getKey());
	}

	/**
	 * add a value representing the given logbook to given {@link QueryString}.
	 * 
	 * @param queryString
	 *            given
	 * @param logbook
	 *            to put into given {@link QueryString}
	 */
	public static void add(QueryString queryString, Logbook logbook) {
		queryString.put(QUERY_KEY_LOGBOOK, logbook.getKey());
	}

	/**
	 * return a query string for getting a calendar.
	 * 
	 * @param cal
	 *            calendar the query string is for
	 * @return a query string for getting a calendar.
	 */
	public static QueryString getQueryString(Calendar cal) {
		QueryString qs = new QueryString();
		qs.put(QUERY_KEY_YEAR, cal.get(Calendar.YEAR) + "");
		qs.put(QUERY_KEY_MONTH, cal.get(Calendar.MONTH) + "");
		qs.put(QUERY_KEY_DAY, cal.get(Calendar.DAY_OF_MONTH) + "");
		qs.put(QUERY_KEY_HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + "");
		qs.put(QUERY_KEY_MINUTE_OF_HOUR, cal.get(Calendar.MINUTE) + "");
		return qs;
	}

	/**
	 * return the query string for a booking
	 * 
	 * @param booking
	 *            the query string is produced from
	 * @return the query string for a booking
	 */
	public static QueryString getQueryString(Booking booking) {
		QueryString result = new QueryString();
		add(result, booking);
		return result;
	}

	/**
	 * return a new query string representing the given purchasable object.
	 * 
	 * @param purchasable
	 *            to put into a {@link QueryString}
	 * @return a new query string representing the given purchasable object.
	 */
	public static QueryString getArticleNumber(Purchasable purchasable) {
		QueryString result = new QueryString();
		result.put(QueryKeys.QUERY_KEY_BOOKING, purchasable.getArticleNumber());
		return result;
	}

	/**
	 * return a QueryString to the calendar overview
	 * 
	 * @param facilityKey
	 *            of facility of the calendar to show
	 * @param c
	 *            calendar of the day and time to show
	 * @return a QueryString to the calendar overview
	 */
	public static QueryString getNextAvailableTimeSlotsFrom(String facilityKey, Calendar c) {
		return getBigCalendarQueryString(facilityKey, c, OVERVIEW);
	}

	/**
	 * add two flags to the given query that it has been sent: one to "true" for
	 * signing, that it has been sent. another to "false" to sign, it is an ajax
	 * request. the second parameter is used for given different answers (as a
	 * save signal) in ajax requests and must be set to true via javascript.
	 * true and falst is set with 1 and 0 here.
	 * 
	 * @param queryString
	 */
	public static void addSentFlags(QueryString queryString) {
		queryString.put(QUERY_KEY_POST_REQUEST_SUCCEEDED, QueryKeys.YES);
		queryString.put(JS_AJAX_FLAG, QueryKeys.NO);
	}

	/**
	 * add a value representing a start / stop of the given booking to given
	 * {@link QueryString}. if the given booking has already begun, add a stop
	 * flag - otherwise a start flag.
	 * 
	 * @see QueryKeys#START
	 * @see QueryKeys#STOP
	 * @see QueryKeys#QUERY_KEY_START_STOP
	 * @see QueueBooking#sessionAlreadyBegun()
	 * @param queryString
	 *            given
	 * @param booking
	 *            a value representing a start / stop of the given booking is
	 *            put into given {@link QueryString} of
	 */
	public static void addStartStop(QueryString queryString, QueueBooking booking) {
		if (booking.sessionAlreadyBegun()) {
			queryString.put(QueryKeys.QUERY_KEY_START_STOP, QueryKeys.STOP);
		} else {
			queryString.put(QueryKeys.QUERY_KEY_START_STOP, QueryKeys.START);
		}
	}

	/**
	 * return a new query string representing the given true false option for
	 * details flag.
	 * 
	 * @see QueryKeys#YES
	 * @see QueryKeys#NO
	 * @see QueryKeys#QUERY_KEY_SHOW_DETAILS
	 * @param show
	 *            value set for details flag
	 * @return a new query string representing the given true false option for
	 *         details flag.
	 */
	public static QueryString getShowDetails(boolean show) {
		QueryString result = new QueryString();
		result.put(QueryKeys.QUERY_KEY_SHOW_DETAILS, show ? QueryKeys.YES : QueryKeys.NO);
		return result;
	}

	/**
	 * return query string with given flag for ajax request. this should only be
	 * used with flag false, because this is not a ajax request natuarly. you
	 * can set flag to true anyway, if you want to simulate an ajax request (on
	 * no script form parts) or if you build a form that must be sent via ajax.
	 * 
	 * @param setFlagOn
	 *            true, if ajax is used. otherwise false.
	 * @return query string with given flag for ajax request.
	 */
	public static QueryString getAjaxFlag(boolean setFlagOn) {
		QueryString result = new QueryString();
		if (setFlagOn) {
			result.put(JS_AJAX_FLAG, QueryKeys.YES);
		} else {
			result.put(JS_AJAX_FLAG, QueryKeys.NO);
		}
		return result;
	}

	/**
	 * return a new query string representing the given booking and a second
	 * of-id. set the id of booking for "booking query key" and
	 * <code>ofId</code> as value for "of query key"
	 * 
	 * @see QueryKeys#QUERY_KEY_BOOKING
	 * @see QueryKeys#QUERY_KEY_OF
	 * @see Booking#getId()
	 * @param booking
	 *            value set for booking
	 * @param ofId
	 *            id standing for something "of"
	 * @return a new query string representing the given booking and a second
	 *         of-id.
	 */
	public static QueryString getQueryString(Booking booking, int ofId) {
		QueryString result = new QueryString();
		result.put(QueryKeys.QUERY_KEY_BOOKING, booking.getId());
		result.put(QueryKeys.QUERY_KEY_OF, ofId + "");
		return result;
	}

	private QueryStringBuilder() {
	}
	
    /**
     * return the query string to enter a calendar for the given facility at the given time with in given view.
     * @param facilityKey key representing the facility to be viewed
     * @param cal time visitied with this query string.
     * @param calendarView like month or week to show
     * @return the query string to enter a calendar for the given facility at the given time with in given view.
     */
    public final static QueryString getBigCalendarQueryString(String facilityKey, Calendar cal, String calendarView) {
        QueryString qs = QueryStringBuilder.getQueryString(cal);
        qs.put(QUERY_KEY_FACILITY, facilityKey);
        if (isValidCalendarView(calendarView)) {
            qs.put(QUERY_KEY_CALENDAR_VIEW, calendarView);
        } else {
            qs.put(QUERY_KEY_CALENDAR_VIEW, CalendarViewResolver.getInstance().getDefaultCalendarView(FacilityConfigDao.facility(facilityKey)));
        }
        return qs;
    }
    /**
     * return the query string to enter a calendar for the given facility at the given time in the standard view of the facility.
     * @see CalendarViewResolver#getDefaultCalendarView(de.knurt.fam.core.model.config.Facility)
     * @param facilityKey key representing the facility to be viewed
     * @param cal time visitied with this query string.
     * @return the query string to enter a calendar for the given facility at the given time in the standard view of the facility.
     */
    public final static QueryString getBigCalendarQueryString(String facilityKey, Calendar cal) {
        return getBigCalendarQueryString(facilityKey, cal, CalendarViewResolver.getInstance().getDefaultCalendarView(FacilityConfigDao.facility(facilityKey)));
    }
    
    public static boolean isValidCalendarView(String calendarView) {
        boolean result = false;
        for (String valid : configuredCalendarview) {
            if (valid.equals(calendarView)) {
                result = true;
                break;
            }
        }
        return result;
    }
    
    private static final String[] configuredCalendarview = {MONTH, WEEK, OVERVIEW};
}

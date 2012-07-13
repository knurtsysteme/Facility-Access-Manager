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

import java.util.Calendar;

import de.knurt.fam.core.model.config.Role;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.persistence.dao.config.LogbookConfigDao;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.heinzelmann.util.time.AbstractTimeFrame;

/**
 * the central point for queries.
 * 
 * for uris with long queries support shortcuts.
 * support shortcuts for some values like TRUE or FALSE.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090425 (04/25/2009)
 */
public class QueryKeys {

	// XXX do we need all the get[A-Z]() stuff?

	/*
	 * ############# VALUES ##############
	 */
	/**
	 * value representing <code>false</code>
	 */
	public final static String FALSE = "0";
	/**
	 * value representing <code>true</code>
	 */
	public final static String TRUE = "1";
	/**
	 * value representing yes. same as {@link #TRUE}
	 * 
	 * @see #TRUE
	 */
	public final static String YES = TRUE;
	/**
	 * value representing no. same as {@link #FALSE}
	 * 
	 * @see #FALSE
	 */
	public final static String NO = FALSE;
	/**
	 * value representing start. same as {@link #TRUE}
	 * 
	 * @see #TRUE
	 */
	public final static String START = TRUE;
	/**
	 * value representing stop. same as {@link #FALSE}
	 * 
	 * @see #FALSE
	 */
	public final static String STOP = FALSE;
	/**
	 * value representing week
	 */
	public final static String WEEK = "2";
	/**
	 * value representing month
	 */
	public final static String MONTH = "3";
	/**
	 * value representing overview
	 */
	public final static String OVERVIEW = "4";

	/*
	 * ############# QUERY KEYS ##############
	 */
	/**
	 * key for a facility
	 */
	public static final String QUERY_KEY_FACILITY = "a";
	/**
	 * key for "year"
	 */
	public static final String QUERY_KEY_YEAR = "b";
	/**
	 * key for "month"
	 */
	public static final String QUERY_KEY_MONTH = "c";
	/**
	 * key for "day"
	 */
	public static final String QUERY_KEY_DAY = "d";
	/**
	 * key for quering a logbook
	 */
	public final static String QUERY_KEY_LOGBOOK = "e";
	/**
	 * key for quering a page number
	 */
	public final static String QUERY_KEY_PAGENO = "f";
	/**
	 * key for quering an entry number. this is the number of entries shown.
	 */
	public final static String QUERY_KEY_COUNT_OF_ENTRIES_PER_PAGE = "g";
	/**
	 * flag to show successfull posting
	 */
	public final static String QUERY_KEY_POST_REQUEST_SUCCEEDED = "h";
	/**
	 * key for "capacity units"
	 * 
	 * @see TimeBookingRequest#getRequestedCapacityUnits()
	 */
	public final static String QUERY_KEY_UNITS_CAPACITY = "i";
	/**
	 * key for "time units"
	 * 
	 * @see TimeBookingRequest#getRequestedTimeUnits()
	 */
	public final static String QUERY_KEY_UNITS_TIME = "j";
	/**
	 * key for "show details"
	 */
	public final static String QUERY_KEY_SHOW_DETAILS = "k";
	/**
	 * key for "hour of day"
	 */
	public final static String QUERY_KEY_HOUR_OF_DAY = "l";
	/**
	 * key for "minute of hour"
	 */
	public final static String QUERY_KEY_MINUTE_OF_HOUR = "m";
	/**
	 * a view in general. might be "week view" or "month view" etc.
	 */
	public final static String QUERY_KEY_CALENDAR_VIEW = "n";
	/**
	 * key for choosing week
	 */
	public final static String QUERY_KEY_WEEK = "o";
	/**
	 * key for overview yes / no
	 */
	public final static String QUERY_KEY_OVERVIEW = "p";
	/**
	 * key for availability yes / no
	 */
	public final static String QUERY_KEY_AVAILABLILITY = "q";
	/**
	 * key for "from"
	 */
	public final static String QUERY_KEY_FROM = "r";
	/**
	 * key for "to"
	 */
	public final static String QUERY_KEY_TO = "s";
	/**
	 * key for "iteration"
	 */
	public final static String QUERY_KEY_ITERATION = "t";
	/**
	 * key for booking something
	 */
	public final static String QUERY_KEY_DELETE = "u";
	/**
	 * key for "booking"
	 * 
	 * @see Booking#getId()
	 */
	public final static String QUERY_KEY_BOOKING = "v";
	/**
	 * key for a text input in general. this does only make sense if there is
	 * exactly one textinput in the form.
	 */
	public final static String QUERY_KEY_TEXT_NOTICE = "w";
	/**
	 * key for "role"
	 * 
	 * @see Role#getKey()
	 */
	public final static String QUERY_KEY_ROLE = "x";
	/**
	 * key for "true / false"
	 * 
	 * @see #TRUE
	 * @see #FALSE
	 */
	public final static String QUERY_KEY_TRUE_FALSE = "y";
	/**
	 * key for "yes / no" same as {@link #QUERY_KEY_TRUE_FALSE}
	 * 
	 * @see #QUERY_KEY_TRUE_FALSE
	 * @see #YES
	 * @see #NO
	 */
	public final static String QUERY_KEY_YES_NO = QUERY_KEY_TRUE_FALSE;
	/**
	 * key for "start / stop" same as {@link #QUERY_KEY_TRUE_FALSE}
	 * 
	 * @see #QUERY_KEY_TRUE_FALSE
	 * @see #START
	 * @see #STOP
	 */
	public final static String QUERY_KEY_START_STOP = QUERY_KEY_TRUE_FALSE;
	/**
	 * key for "user"
	 * 
	 * @see User#getId()
	 */
	public final static String QUERY_KEY_USER = "z";
	/**
	 * general key for the "of" of queries like "get something of".
	 */
	public final static String QUERY_KEY_OF = "aa";
	/**
	 * general key for a secret.
	 */
	public final static String QUERY_KEY_SECRET = "ab";

	/*
	 * ############# QUERY KEYS DEFAULT VALUES ##############
	 */
	/**
	 * default count of entries shown on one page
	 */
	public final static String QUERY_DEFAULT_VALUE_COUNT_OF_ENTRIES_PER_PAGE = "500";
	/**
	 * abstract flag to check if it succeeded or not
	 */
	public final static String QUERY_DEFAULT_VALUE_POST_REQUEST_SUCCEEDED = "1";
	/**
	 * key for sending a date from via ajax
	 */
	public final static String JS_KEY_FROM_DATE = "js_from_date";
	/**
	 * key for sending a time from via ajax
	 */
	public final static String JS_KEY_FROM_TIME = "js_from_time";
	/**
	 * key for sending a date to via ajax
	 */
	public final static String JS_KEY_TO_DATE = "js_to_date";
	/**
	 * key for sending a time to via ajax
	 */
	public final static String JS_KEY_TO_TIME = "js_to_time";
	/**
	 * key for setting a ajax flag
	 */
	public final static String JS_AJAX_FLAG = "ajax";

	/**
	 * return a string for querying time frames. the time frame is exactly one
	 * day here. given hour and minute is set to 00:00 h for start and end.
	 * 
	 * @param entireDay
	 *            representing the day
	 * @return a string for querying time frames.
	 */
	public static String getEncodeStringForOneDay(Calendar entireDay) {
		entireDay.set(Calendar.HOUR_OF_DAY, 0);
		entireDay.set(Calendar.MINUTE, 0);
		Calendar end = (Calendar) entireDay.clone();
		end.add(Calendar.DATE, 1);
		return getEncodeStringForTimeFrame(entireDay, end);
	}

	/**
	 * return encoded time frame string. string has the form of: <code>
     * <startYear>.<startMonth>.<startDayOfYear>.<startHourOfDay>.<startMinuteOfHour>.<endYear>.<endMonth>.<endDayOfYear>.<endHourOfDay>.<endMinuteOfHour>
     * </code>
	 * 
	 * @param start
	 *            starting time frame
	 * @see AbstractTimeFrame#start
	 * @see AbstractTimeFrame#end
	 * @param stop
	 *            stoping time frame
	 * @return encoded time frame string.
	 */
	public static String getEncodeStringForTimeFrame(Calendar start, Calendar stop) {
		return getEncodeString(start) + "." + getEncodeString(stop);
	}

	/**
	 * return a string representing the year, month and day of the given
	 * calendar. string has the format y.m.D. ignore time smaller then day of
	 * given calendar.
	 * 
	 * @param cal
	 *            calendar given
	 * @return a string representing the year, month and day of the given
	 *         calendar.
	 */
	public static String getEncodeStringOfDate(Calendar cal) {
		String format = "%s.%s.%s";
		return String.format(format, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
	}

	private static String getEncodeString(Calendar cal) {
		return getEncodeStringOfDate(cal) + "." + getEncodeStringOfTime(cal);
	}

	/**
	 * like {@link #getEncodeStringOfDate(java.util.Calendar)}, return it for
	 * the given hour and minute.
	 * 
	 * @param cal
	 *            hour of day and minute is taken from
	 * @return it for the given hour and minute like
	 *         {@link #getEncodeStringOfDate(java.util.Calendar)} it does for
	 *         the date.
	 */
	public static String getEncodeStringOfTime(Calendar cal) {
		String format = "%s.%s";
		return String.format(format, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
	}

	private String a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, rm, sm, rl, sl, rb, sb, rc, sc, rd, sd;

	/**
	 * @see #QUERY_KEY_FACILITY
	 * @return the a
	 */
	public String getA() {
		return a;
	}

	/**
	 * return value of {@link #getTrueFalse()}
	 * 
	 * @return value of {@link #getTrueFalse()}
	 */
	public Boolean isYes() {
		return this.getTrueFalse();
	}

	/**
	 * return true if {@link #QUERY_KEY_TRUE_FALSE} equals {@link #TRUE}, false
	 * if {@link #QUERY_KEY_TRUE_FALSE} equals {@link #FALSE} and null if
	 * {@link #QUERY_KEY_TRUE_FALSE} is not set.
	 * 
	 * @return value of {@link #QUERY_KEY_TRUE_FALSE}
	 */
	public Boolean getTrueFalse() {
		Boolean result = null;
		if (y != null) {
			if (y.equals(TRUE)) {
				result = true;
			} else {
				result = false;
			}
		}
		return result;
	}

	/**
	 * return value of {@link #QUERY_KEY_UNITS_TIME}. if key is not set or
	 * cannot be parsed to integer, return null.
	 * 
	 * @return value of {@link #QUERY_KEY_UNITS_TIME}.
	 */
	public Integer getTime() {
		Integer result = null;
		try {
			result = Integer.parseInt(this.getJ());
		} catch (NumberFormatException nfe) {
		}
		return result;
	}

	/**
	 * empty constructor
	 */
	public QueryKeys() {
	}

	/**
	 * return the requested result key if and only if it does exist.
	 * 
	 * @return the requested result key if and only if it does exist.
	 */
	public String getFacilityKey() {
		String result = null;
		if (FacilityConfigDao.getInstance().keyExists(getA())) {
			result = getA();
		}
		return result;
	}

	/**
	 * @see #QUERY_KEY_FACILITY
	 * @param a
	 *            the a to set
	 */
	public void setA(String a) {
		this.a = a;
	}

	/**
	 * @see #QUERY_KEY_YEAR
	 * @return the b
	 */
	public String getB() {
		return b;
	}

	/**
	 * return year as {@link Integer} or null on FormatException.
	 * 
	 * @return year as {@link Integer} or null on FormatException.
	 */
	public Integer getYear() {
		return RequestInterpreter.getAsIntegerOrNull(getB());
	}

	/**
	 * @see #QUERY_KEY_YEAR
	 * @param b
	 *            the b to set
	 */
	public void setB(String b) {
		this.b = b;
	}

	/**
	 * @see #QUERY_KEY_MONTH
	 * @return the c
	 */
	public String getC() {
		return c;
	}

	/**
	 * return month as {@link Integer} or null on FormatException.
	 * 
	 * @return month as {@link Integer} or null on FormatException.
	 */
	public Integer getMonth() {
		return RequestInterpreter.getAsIntegerOrNull(getC());
	}

	/**
	 * @see #QUERY_KEY_MONTH
	 * @param c
	 *            the c to set
	 */
	public void setC(String c) {
		this.c = c;
	}

	/**
	 * @see #QUERY_KEY_DAY
	 * @return the d
	 */
	public String getD() {
		return d;
	}

	/**
	 * return day as {@link Integer} or null on FormatException.
	 * 
	 * @return day as {@link Integer} or null on FormatException.
	 */
	public Integer getDay() {
		return RequestInterpreter.getAsIntegerOrNull(getD());
	}

	/**
	 * @see #QUERY_KEY_DAY
	 * @param d
	 *            the d to set
	 */
	public void setD(String d) {
		this.d = d;
	}

	/**
	 * @see #QUERY_KEY_LOGBOOK
	 * @return the e
	 */
	public String getE() {
		return e;
	}

	/**
	 * @see #QUERY_KEY_LOGBOOK
	 * @param e
	 *            the e to set
	 */
	public void setE(String e) {
		this.e = e;
	}

	/**
	 * return key a logbook as queried
	 * 
	 * @return key a logbook as queried
	 */
	public String getLogbookKey() {
		String result = null;
		if (LogbookConfigDao.getInstance().keyExists(getE())) {
			result = getE();
		}
		return result;
	}

	/**
	 * @see #QUERY_KEY_PAGENO
	 * @return the QUERY_KEY_PAGENO
	 */
	public String getF() {
		return f;
	}

	/**
	 * return page number as {@link Integer} or null on FormatException.
	 * 
	 * @return page number as {@link Integer} or null on FormatException.
	 */
	public Integer getPageno() {
		return RequestInterpreter.getAsIntegerOrNull(getF());
	}

	/**
	 * @see #QUERY_KEY_PAGENO
	 * @param f
	 *            the f to set
	 */
	public void setF(String f) {
		this.f = f;
	}

	/**
	 * @see #QUERY_KEY_COUNT_OF_ENTRIES_PER_PAGE
	 * @return the g
	 */
	public String getG() {
		return g;
	}

	/**
	 * return entries per page as {@link Integer} or null on FormatException.
	 * 
	 * @return entries per page as {@link Integer} or null on FormatException.
	 */
	public Integer getEntriesPerPage() {
		return RequestInterpreter.getAsIntegerOrNull(getG());
	}

	/**
	 * @see #QUERY_KEY_COUNT_OF_ENTRIES_PER_PAGE
	 * @param g
	 *            the g to set
	 */
	public void setG(String g) {
		this.g = g;
	}

	/**
	 * @see #QUERY_KEY_POST_REQUEST_SUCCEEDED
	 * @return the h
	 */
	public String getH() {
		return h;
	}

	/**
	 * return true, if and only if h is the default value.
	 * 
	 * @see #QUERY_KEY_POST_REQUEST_SUCCEEDED
	 * @see #QUERY_DEFAULT_VALUE_POST_REQUEST_SUCCEEDED
	 * @return true, if and only if h is the default value.
	 */
	public boolean getPostRequestSucceeded() {
		return getH().equals("1");
	}

	/**
	 * @see #QUERY_KEY_POST_REQUEST_SUCCEEDED
	 * @param h
	 *            the h to set
	 */
	public void setH(String h) {
		this.h = h;
	}

	/**
	 * @see #QUERY_KEY_SHOW_DETAILS
	 * @return the k
	 */
	public String getK() {
		return k;
	}

	/**
	 * @see #QUERY_KEY_SHOW_DETAILS
	 * @param k
	 *            the k to set
	 */
	public void setK(String k) {
		this.k = k;
	}

	/**
	 * @see #QUERY_KEY_HOUR_OF_DAY
	 * @return the l
	 */
	public String getL() {
		return l;
	}

	/**
	 * @see #QUERY_KEY_HOUR_OF_DAY
	 * @param l
	 *            the l to set
	 */
	public void setL(String l) {
		this.l = l;
	}

	/**
	 * @see #QUERY_KEY_MINUTE_OF_HOUR
	 * @return the m
	 */
	public String getM() {
		return m;
	}

	/**
	 * @see #QUERY_KEY_MINUTE_OF_HOUR
	 * @param m
	 *            the m to set
	 */
	public void setM(String m) {
		this.m = m;
	}

	/**
	 * @return the n
	 */
	public String getN() {
		return n;
	}

	/**
	 * @param n
	 *            the n to set
	 */
	public void setN(String n) {
		this.n = n;
	}

	/**
	 * @return the o
	 */
	public String getO() {
		return o;
	}

	/**
	 * @param o
	 *            the o to set
	 */
	public void setO(String o) {
		this.o = o;
	}

	/**
	 * @return the p
	 */
	public String getP() {
		return p;
	}

	/**
	 * @param p
	 *            the p to set
	 */
	public void setP(String p) {
		this.p = p;
	}

	/**
	 * @return the i
	 */
	public String getI() {
		return i;
	}

	/**
	 * @param i
	 *            the i to set
	 */
	public void setI(String i) {
		this.i = i;
	}

	/**
	 * @return the j
	 */
	public String getJ() {
		return j;
	}

	/**
	 * @param j
	 *            the j to set
	 */
	public void setJ(String j) {
		this.j = j;
	}

	/**
	 * @return the rm
	 */
	public String getRm() {
		return rm;
	}

	/**
	 * @param rm
	 *            the rm to set
	 */
	public void setRm(String rm) {
		this.rm = rm;
	}

	/**
	 * @return the sm
	 */
	public String getSm() {
		return sm;
	}

	/**
	 * @param sm
	 *            the sm to set
	 */
	public void setSm(String sm) {
		this.sm = sm;
	}

	/**
	 * @return the rl
	 */
	public String getRl() {
		return rl;
	}

	/**
	 * @param rl
	 *            the rl to set
	 */
	public void setRl(String rl) {
		this.rl = rl;
	}

	/**
	 * @return the sl
	 */
	public String getSl() {
		return sl;
	}

	/**
	 * @param sl
	 *            the sl to set
	 */
	public void setSl(String sl) {
		this.sl = sl;
	}

	/**
	 * @return the q
	 */
	public String getQ() {
		return q;
	}

	/**
	 * @param q
	 *            the q to set
	 */
	public void setQ(String q) {
		this.q = q;
	}

	/**
	 * @return the r
	 */
	public String getR() {
		return r;
	}

	/**
	 * @param r
	 *            the r to set
	 */
	public void setR(String r) {
		this.r = r;
	}

	/**
	 * @return the s
	 */
	public String getS() {
		return s;
	}

	/**
	 * @param s
	 *            the s to set
	 */
	public void setS(String s) {
		this.s = s;
	}

	/**
	 * @return the t
	 */
	public String getT() {
		return t;
	}

	/**
	 * @param t
	 *            the t to set
	 */
	public void setT(String t) {
		this.t = t;
	}

	/**
	 * @return the rb
	 */
	public String getRb() {
		return rb;
	}

	/**
	 * @param rb
	 *            the rb to set
	 */
	public void setRb(String rb) {
		this.rb = rb;
	}

	/**
	 * @return the sb
	 */
	public String getSb() {
		return sb;
	}

	/**
	 * @param sb
	 *            the sb to set
	 */
	public void setSb(String sb) {
		this.sb = sb;
	}

	/**
	 * @return the rc
	 */
	public String getRc() {
		return rc;
	}

	/**
	 * @param rc
	 *            the rc to set
	 */
	public void setRc(String rc) {
		this.rc = rc;
	}

	/**
	 * @return the sc
	 */
	public String getSc() {
		return sc;
	}

	/**
	 * @param sc
	 *            the sc to set
	 */
	public void setSc(String sc) {
		this.sc = sc;
	}

	/**
	 * @return the rd
	 */
	public String getRd() {
		return rd;
	}

	/**
	 * @param rd
	 *            the rd to set
	 */
	public void setRd(String rd) {
		this.rd = rd;
	}

	/**
	 * @return the sd
	 */
	public String getSd() {
		return sd;
	}

	/**
	 * @param sd
	 *            the sd to set
	 */
	public void setSd(String sd) {
		this.sd = sd;
	}

	/**
	 * @return the u
	 */
	private String getU() {
		return u;
	}

	/**
	 * return true, if value of {@link #QUERY_KEY_DELETE} is not null. this does
	 * not automaticly mean, that something is requested to delete but that the
	 * flag is set.
	 * 
	 * @return true, if value of {@link #QUERY_KEY_DELETE} is not null.
	 */
	public boolean hasDeletedInfo() {
		return this.getDeletedInfo() != null;
	}

	/**
	 * return value of {@link #QUERY_KEY_DELETE}
	 * 
	 * @return value of {@link #QUERY_KEY_DELETE}
	 */
	public String getDeletedInfo() {
		return this.getU();
	}

	/**
	 * @param u
	 *            the u to set
	 */
	public void setU(String u) {
		this.u = u;
	}

	/**
	 * @return the v
	 */
	public String getV() {
		return v;
	}

	/**
	 * return the value set for the booking key.
	 * 
	 * @return the value set for the booking key.
	 */
	public String getBookingKey() {
		return this.getV();
	}

	/**
	 * @param v
	 *            the v to set
	 */
	public void setV(String v) {
		this.v = v;
	}

	/**
	 * @return the w
	 */
	public String getW() {
		return w;
	}

	/**
	 * @param w
	 *            the w to set
	 */
	public void setW(String w) {
		this.w = w;
	}

	/**
	 * @return the x
	 */
	public String getX() {
		return x;
	}

	/**
	 * @param x
	 *            the x to set
	 */
	public void setX(String x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public String getY() {
		return y;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	public void setY(String y) {
		this.y = y;
	}

	/**
	 * @return the z
	 */
	public String getZ() {
		return z;
	}

	/**
	 * return value of query key representing the user.
	 * 
	 * @return value of query key representing the user.
	 */
	public User getUser() {
		return FamDaoProxy.userDao().getUserFromUsername(getZ());
	}

	/**
	 * @param z
	 *            the z to set
	 */
	public void setZ(String z) {
		this.z = z;
	}

	/**
	 * return the value set for the role key.
	 * 
	 * @see #QUERY_KEY_ROLE
	 * @return the value set for the role key.
	 */
	public String getRoleKey() {
		return x;
	}

	/**
	 * return true, if a value is set for the role key.
	 * 
	 * @see #QUERY_KEY_ROLE
	 * @return true, if a value is set for the role key.
	 */
	public boolean containsRoleKey() {
		return this.getRoleKey() != null;
	}
}

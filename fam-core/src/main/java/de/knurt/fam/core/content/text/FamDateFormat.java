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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.knurt.fam.core.config.FamRequestContainer;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.heinzelmann.util.IntegerNumeralFormat;
import de.knurt.heinzelmann.util.time.TimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrameFactory;

/**
 * base class to format dates in the "das". therefor, the given location is
 * used.
 * 
 * @see FamRequestContainer#locale
 * @author Daniel Oltmanns
 * @since 0.20090413 (04/13/2009)
 */
public class FamDateFormat {

	/**
	 * return a date as string in the language of the user
	 * 
	 * @param date
	 *            to convert to string
	 * @return a date as string in the language of the user
	 */
	public static String getDateFormattedWithTime(Date date) {
		return date == null ? "unknown" : DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, FamRequestContainer.locale()).format(date);
	}

	/**
	 * return the interval of the given availability formatted to a string. if
	 * the interval is not set, return "unknown". if it is weeklay, return
	 * "monday". if it is daily, return "from 3am to 4pm" etc.
	 * 
	 * @param da
	 *            the time is returned in subject to the interval.
	 * @param html
	 *            true, if the output shall be include html elements.
	 * @return the interval of the given availability formatted to a string.
	 */
	public static String getIntervalFormatted(FacilityAvailability da, boolean html) {
		String result = "";
		switch (da.getInterval()) {
		case FacilityAvailability.ONE_TIME:
			result = getDateFormattedWithTime(da.getBasePeriodOfTime(), "From %s to %s");
			break;
		case FacilityAvailability.EACH_DAY:
		case FacilityAvailability.EACH_HOUR:
			result = getTimeFormatted(da.getBasePeriodOfTime(), "From %s to %s");
			break;
		case FacilityAvailability.EACH_MONTH:
			TimeFrame emTf = da.getBasePeriodOfTime();
			String em2Format = "Every %s of the month from %s to %s of the month %s"; // INTLANG
			Calendar emCStart = emTf.getCalendarStart();
			Calendar emCEnd = emTf.getCalendarEnd();
			String emStartDay = INF.format(emCStart.get(Calendar.DAY_OF_MONTH));
			String emEndDay = INF.format(emCEnd.get(Calendar.DAY_OF_MONTH));
			String emStartHour = getTimeFormatted(emCStart.getTime());
			String emEndHour = getTimeFormatted(emCEnd.getTime());
			result = String.format(em2Format, emStartDay, emStartHour, emEndDay, emEndHour);
			break;
		case FacilityAvailability.EACH_WEEK:
			TimeFrame ewTf = da.getBasePeriodOfTime();
			String ew2Format = "Every week from %s %s to %s %s"; // INTLANG
			result = String.format(ew2Format, ewTf.getCalendarStart().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, FamRequestContainer.locale()), getTimeFormatted(ewTf.getCalendarStart()),
					ewTf.getCalendarEnd().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, FamRequestContainer.locale()), getTimeFormatted(ewTf.getCalendarEnd()));
			break;
		case FacilityAvailability.EACH_YEAR:
			TimeFrame eyTf = da.getBasePeriodOfTime();
			result = String.format("From %s %s to %s %s", getDateFormattedWithoutTime(eyTf.getCalendarStart()), getTimeFormatted(eyTf.getDateStart()), getDateFormattedWithoutTime(eyTf.getCalendarEnd()), getTimeFormatted(eyTf.getDateEnd()));
			break;
		}
		return result;
	}

	/**
	 * format the given date and return.
	 * 
	 * @param date
	 *            to format
	 * @return given date formatted
	 */
	public static String getDateFormatted(Date date) {
		return date == null ? "unknown" : DateFormat.getDateInstance(DateFormat.LONG, FamRequestContainer.locale()).format(date); // INTLANG
	}

	/**
	 * return the date formatted without showing the year.
	 * 
	 * @param day
	 *            to format
	 * @return the date formatted without showing the year.
	 */
	public static String getDateFormattedWithoutYear(Calendar day) {
		return day.getDisplayName(Calendar.MONTH, Calendar.LONG, FamRequestContainer.locale()) + " " + INF.format(day.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * central integer numeral format instance of the system.
	 */
	private static final IntegerNumeralFormat INF = new IntegerNumeralFormat(FamRequestContainer.locale());

	/**
	 * return {@link IntegerNumeralFormat#format(int)} in current application
	 * language.
	 * 
	 * @param value
	 *            to format
	 * @return {@link IntegerNumeralFormat#format(int)} in current application
	 *         language.
	 */
	public static String getNumeralFormat(int value) {
		return INF.format(value);
	}

	/**
	 * format the given date and return.
	 * 
	 * @param calendar
	 *            to format
	 * @return given date formatted
	 */
	public static Object getDateFormattedWithWeekday(Calendar calendar) {
		return getDateFormattedWithTime(calendar) + " (" + calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, FamRequestContainer.locale()) + ")";
	}

	/**
	 * format the given date and return.
	 * 
	 * @param calendar
	 *            to format
	 * @return given date formatted
	 */
	public static String getDateFormattedWithoutTime(Calendar calendar) {
		return getDateFormatted(calendar.getTime());
	}

	/**
	 * return the given date as used to put into input fields.
	 * 
	 * @param date
	 *            to convert
	 * @return the given date as used to put into input fields.
	 */
	public static String getLangIndependantShortDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return getLangIndependantShortDate(calendar);
	}

	/**
	 * return the given date as used to put into input fields.
	 * 
	 * @param calendar
	 *            to convert
	 * @return the given date as used to put into input fields.
	 */
	public static String getLangIndependantShortDate(Calendar calendar) {
		String day = (calendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" : "") + calendar.get(Calendar.DAY_OF_MONTH);
		String month = (calendar.get(Calendar.MONTH) + 1 < 10 ? "0" : "") + (calendar.get(Calendar.MONTH) + 1);
		return day + "." + month + "." + calendar.get(Calendar.YEAR);
	}

	/**
	 * return the given calendar in locale specific short string
	 * 
	 * @param calendar
	 *            used to generate the date
	 * @return the given calendar in locale specific short string
	 */
	public static String getShortDate(Calendar calendar) {
		return DateFormat.getDateInstance(DateFormat.SHORT, FamRequestContainer.locale()).format(calendar.getTime());
	}

	/**
	 * return a date as string in the language of the user
	 * 
	 * @param calendar
	 *            to convert to string
	 * @return a date as string in the language of the user
	 */
	public static String getDateFormattedWithTime(Calendar calendar) {
		return getDateFormattedWithTime(calendar.getTime());
	}

	/**
	 * return string representation of the start and end time of the given time
	 * frame.
	 * 
	 * @param timeFrame
	 *            get dates of
	 * @param html
	 *            true, if html schall be used for the output
	 * @return string representation of the start and end time of the given time
	 *         frame.
	 */
	public static String getDateFormattedWithTime(TimeFrame timeFrame, boolean html) {
		String format = "";
		if (html) {
			format = "<table class=\"justtext\"><tr><td>from</td><td>%s</td></tr><tr><td>to</td><td>%s</td></tr></table>"; // INTLANG
		} else {
			format = "from %s to %s"; // INTLANG
		}
		return String.format(format, getDateFormattedWithTime(timeFrame.getDateStart()), getDateFormattedWithTime(timeFrame.getDateEnd()));
	}

	/**
	 * return string representation of the start and end time of the given time
	 * frame.
	 * 
	 * @param timeFrame
	 *            get dates of
	 * @param format
	 *            string to formated with start and end time
	 * @return string representation of the start and end time of the given time
	 *         frame.
	 */
	public static String getDateFormattedWithTime(TimeFrame timeFrame, String format) {
		return String.format(format, getDateFormattedWithTime(timeFrame.getDateStart()), getDateFormattedWithTime(timeFrame.getDateEnd()));
	}

	/**
	 * return a time frame as string in the language of the user. time frame is
	 * shown as start and end with delimiter.
	 * 
	 * @param timeFrame
	 *            to show
	 * @param delemiter
	 *            shown between start and end
	 * @see FamDateFormat#getDateFormattedWithTime(de.knurt.heinzelmann.util.time.TimeFrame,
	 *      boolean)
	 * @return a time frame as string in the language of the user
	 */
	public static String getDateFormatted(TimeFrame timeFrame, String delemiter) {
		return getDateFormattedWithTime(timeFrame.getCalendarStart()) + delemiter + getDateFormattedWithTime(timeFrame.getCalendarEnd());
	}

	/**
	 * return <code>[date start] ([time start]) - [date end] ([time end])</code>
	 * 
	 * @param timeFrame
	 *            to format
	 * @return
	 *         <code>[date start] ([time start]) - [date end] ([time end])</code>
	 */
	public static String getShortDateFormattedWithTime(TimeFrame timeFrame) {
		return String.format("%s (%s) - %s (%s)", getShortDate(timeFrame.getCalendarStart()), getTimeFormatted(timeFrame.getCalendarStart()), getShortDate(timeFrame.getCalendarEnd()), getTimeFormatted(timeFrame.getCalendarEnd()));
	}

	/**
	 * return <code>[date start] ([time start]) - [date end] ([time end])</code>
	 * 
	 * @param timeFrame
	 *            to format
	 * @return
	 *         <code>[date start] ([time start]) - [date end] ([time end])</code>
	 */
	public static String getShortDateFormattedWithoutTime(TimeFrame timeFrame) {
		return String.format("%s - %s", getShortDate(timeFrame.getCalendarStart()), getShortDate(timeFrame.getCalendarEnd()));
	}

	/**
	 * return a date as string in the language of the user.
	 * 
	 * @param date
	 *            to show
	 * @return a date as string in the language of the user
	 */
	public static String getTimeFormatted(Date date) {
		return DateFormat.getTimeInstance(DateFormat.SHORT, FamRequestContainer.locale()).format(date);
	}

	/**
	 * return a date as string in the language of the user.
	 * 
	 * @param calendar
	 *            to show
	 * @return a date as string in the language of the user
	 */
	public static String getTimeFormatted(Calendar calendar) {
		return getTimeFormatted(calendar.getTime());
	}

	/**
	 * format the week of the given calendar in default locale.
	 * 
	 * @param calendar
	 *            given
	 * @param style
	 *            used for the formatting
	 * @see Calendar#getDisplayName(int, int, java.util.Locale)
	 * @return the week of the given calendar
	 */
	public static String getWeekFormatted(Calendar calendar, int style) {
		return calendar.getDisplayName(Calendar.DAY_OF_WEEK, style, FamRequestContainer.locale());
	}

	/**
	 * return given time frame as from - to value. if the end of the given
	 * timeframe is at 12:00 AM, this will set the end in the output -1 second
	 * to avoid looking silly (e.g. entire day: show "12:00 AM - 11:59 PM"
	 * instead of "12:00 AM - 12:00 AM").
	 * 
	 * @param timeFrame
	 *            to format
	 * @return given time frame as from - to value.
	 */
	public static String getTimeFormatted(TimeFrame timeFrame) {
		String format = "%s - %s";
		return getTimeFormatted(timeFrame, format);
	}

	/**
	 * return given time frame as formatted value. use given format. if the end
	 * of the given timeframe is at 12:00 AM, this will set the end in the
	 * output -1 second to avoid looking silly (e.g. entire day: show
	 * "12:00 AM - 11:59 PM" instead of "12:00 AM - 12:00 AM").
	 * 
	 * @param timeFrame
	 *            to format
	 * @param toFormat
	 *            needs two "%s" in it
	 * @see String#format(java.lang.String, java.lang.Object[])
	 * @return given time frame as from - to value.
	 */
	public static String getTimeFormatted(TimeFrame timeFrame, String toFormat) {
		Calendar end = timeFrame.getCalendarEnd();
		if (end.get(Calendar.HOUR_OF_DAY) == 0 && end.get(Calendar.MINUTE) == 0) {
			end = (Calendar) end.clone();
			end.add(Calendar.MILLISECOND, -1);
		}
		return String.format(toFormat, getTimeFormatted(timeFrame.getCalendarStart()), getTimeFormatted(end));
	}

	/**
	 * format a full hour to a time string. this may 23 -> 11 p.m..
	 * 
	 * @param fullhour
	 *            to format
	 * @return given full hour formatted
	 */
	public static String getShortTimeFormatted(int fullhour) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, fullhour);
		String result = getTimeFormatted(c);
		return result.substring(0, result.indexOf(":")) + result.substring(result.indexOf(" "));
	}

	public static String getDateAndTimeShort(Date date) {
		return getCustomDate(date, "MM/dd/yyyy HH:mm");
	}
	public static String getDateShort(Date date) {
		return getCustomDate(date, "MM/dd/yyyy");
	}
	public static String getTimeShort(Date date) {
		return getCustomDate(date, "HH:mm");
	}
	public static String getTimeFrameShort(TimeFrame timeFrame) {
		return String.format("%s - %s", getDateAndTimeShort(timeFrame.getDateStart()), getDateAndTimeShort(timeFrame.getDateEnd()));
	}
	public static String getDateAndTimeShort() {
		return getDateAndTimeShort(new Date());
	}
/**
 * return a custom date as given in the pattern.
 * do not use for printing out dates to user. use {@link #getDateShort(Date)}, {@link #getDateAndTimeShort()} or equal for that!
 * @param date
 * @param pattern
 * @return
 */
	public static String getCustomDate(Date date, String pattern) {
		if (date == null)
			date = new Date();
		return new SimpleDateFormat(pattern).format(date);
	}

	public static String getCustomDate(String pattern) {
		return getCustomDate(new Date(), pattern);
	}

	private FamDateFormat() {
	}

	public static String getCustomTimeFrame(TimeFrame timeFrame, String patternStart, String patternEnd, String delimeter) {
		if (timeFrame == null)
			timeFrame = new TimeFrameFactory().getDay();
		if (delimeter == null)
			delimeter = " – ";
		String start = new SimpleDateFormat(patternStart).format(timeFrame.getDateStart());
		String end = new SimpleDateFormat(patternEnd).format(timeFrame.getDateEnd());
		return start + delimeter + end;
	}

	public static String getLangIndependantShortDate() {
		return getLangIndependantShortDate(new Date());
	}
}

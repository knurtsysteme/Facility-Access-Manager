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
package de.knurt.fam.core.view.html.calendar;

import java.util.Calendar;

import de.knurt.fam.core.view.html.calendar.factory.FamCalendarHtmlFactory;

/**
 * do it like your superclass, but use another image.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090702 (07/02/2009)
 */
@Deprecated
public class FamWeekAvailabilityBookingsHtml extends FamWeekAvailabilityHtml {

	/**
	 * a week shown in the browser containing the bookings of a specific facility.
	 * 
	 * @param cal
	 *            use date of this calendar to decide which week to show
	 * @param facilityKey
	 *            representing the facility shown in the calendar.
	 * @param dasCalendarHtmlFactory
	 * @param showRedGreenOnly
	 *            if true, availability is only separeted in availably and not
	 *            available. this is used, when the calendar is checked for
	 *            specific units. general not availabilities are still shown.
	 * @param noScriptOnOverview
	 *            if true, a noscript tag is added to the calendar prefix as
	 *            explained in
	 *            {@link FamCalendarHtmlFactory#getCalendarPrefixHtmlNavi(java.util.Calendar, de.knurt.fam.core.view.html.calendar.CalendarView, boolean, boolean)}
	 */
	public FamWeekAvailabilityBookingsHtml(Calendar cal, String facilityKey, FamCalendarHtmlFactory dasCalendarHtmlFactory, boolean showRedGreenOnly, boolean noScriptOnOverview) {
		super(cal, facilityKey, dasCalendarHtmlFactory, showRedGreenOnly, noScriptOnOverview);
	}

	/**
	 * return the key for loading the image showing a day of the week in the
	 * availability-calendar. this is used to load the right image as a
	 * background-image style.
	 * 
	 * @return the key for loading the image showing a day of the week in the
	 *         availability-calendar.
	 */
	@Override
	protected String getDayContentBackgroundImageKey() {
		return "oneweekdaybookings";
	}

	/**
	 * return the content of a day in the week view of availabilities. the day
	 * is the content of a td-element in the calendar.
	 * 
	 * @param c
	 *            representing the day.
	 * @return the content of a day in the week view of availabilities.
	 */
	@Override
	protected String getTdContentDayIntern(Calendar c) {
		return "";
	}
}

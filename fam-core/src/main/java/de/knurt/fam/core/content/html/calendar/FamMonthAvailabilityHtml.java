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
package de.knurt.fam.core.content.html.calendar;

import java.util.Calendar;

import de.knurt.fam.core.content.html.FamImageHtmlGetter;
import de.knurt.fam.core.content.html.calendar.factory.FamCalendarHtmlFactory;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * generate the html for the month overview of facility availability. including
 * buttons to set it.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090423 (04/23/2009)
 */
@SuppressWarnings("deprecation") // TODO #11 kill uses of deprecations
public class FamMonthAvailabilityHtml extends FamMonthHtml {

	private String imageBaseName;
	private boolean showRedGreenOnly;

	/**
	 * create calendar html contents for the calendar showing the general
	 * availablity of a facility.
	 * 
	 * @param cal
	 *            to create the content for
	 * @param dasCalendarHtmlFactory
	 *            the factory used to create common things
	 * @param imageBaseName
	 *            value of status bar as control of creating images
	 * @param facility
	 *            the calendar is for
	 * @param showRedGreenOnly
	 *            if true, availability is only separeted in availably and not
	 *            available. this is used, when the calendar is checked for
	 *            specific units. general not availabilities are still shown.
	 * @param noScriptOnOverview
	 *            if true, a noscript tag is added to the calendar prefix as
	 *            explained in
	 *            {@link FamCalendarHtmlFactory#getCalendarPrefixHtmlNavi(java.util.Calendar, de.knurt.fam.core.content.html.calendar.CalendarView, boolean, boolean)}
	 * @see FamImageHtmlGetter#get(java.lang.String, java.lang.String,
	 *      de.knurt.heinzelmann.util.query.QueryString)
	 */
	public FamMonthAvailabilityHtml(Calendar cal, FamCalendarHtmlFactory dasCalendarHtmlFactory, String imageBaseName, Facility facility, boolean showRedGreenOnly, boolean noScriptOnOverview) {
		super(cal, dasCalendarHtmlFactory, noScriptOnOverview);
		this.imageBaseName = imageBaseName;
		this.showRedGreenOnly = showRedGreenOnly;
	}

	/**
	 * create calendar html contents for the calendar showing the general
	 * availablity of a facility.
	 * 
	 * @param cal
	 *            to create the content for
	 * @param dasCalendarHtmlFactory
	 *            the factory used to create common things
	 * @param imageBaseName
	 *            value of status bar as control of creating images
	 * @param facilityKey
	 *            key representing a facility the calendar is for
	 * @param showRedGreenOnly
	 *            if true, availability is only separeted in availably and not
	 *            available. this is used, when the calendar is checked for
	 *            specific units. general not availabilities are still shown.
	 * @param noScriptOnOverview
	 *            if true, a noscript tag is added to the calendar prefix as
	 *            explained in
	 *            {@link FamCalendarHtmlFactory#getCalendarPrefixHtmlNavi(java.util.Calendar, de.knurt.fam.core.content.html.calendar.CalendarView, boolean, boolean)}
	 */
	public FamMonthAvailabilityHtml(Calendar cal, FamCalendarHtmlFactory dasCalendarHtmlFactory, String imageBaseName, String facilityKey, boolean showRedGreenOnly, boolean noScriptOnOverview) {
		this(cal, dasCalendarHtmlFactory, imageBaseName, FacilityConfigDao.facility(facilityKey), showRedGreenOnly, noScriptOnOverview);
	}

	/**
	 * return the status of given calendar. return it as an image using
	 * {@link FamImageHtmlGetter#getBackgroundImage(java.lang.String, de.knurt.heinzelmann.util.query.QueryString)}
	 * 
	 * @param c
	 *            date is used for the status is shown for
	 * @return the status of given calendar.
	 */
	@Override
	public String getStatus(Calendar c) {
		QueryString qs = this.getQueryString(c);
		if (this.showRedGreenOnly) {
			qs.putAll(QueryStringBuilder.getAjaxFlag(true));
		}
		return FamImageHtmlGetter.getBackgroundImage(this.imageBaseName, qs).toString();
	}

	/**
	 * return the content for the given calendar.
	 * 
	 * @param c
	 *            use date of this calendar to generate the content
	 * @see FamCalendarHtmlFactory#getCellContent(java.util.Calendar,
	 *      java.lang.String, de.knurt.fam.core.model.config.Facility)
	 * @return the content for the given calendar.
	 */
	@Override
	public String getContent(Calendar c) {
		return "";
	}
}

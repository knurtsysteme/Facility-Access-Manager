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

import de.knurt.fam.core.content.html.calendar.factory.FamCalendarHtmlFactory;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.Facility;

/**
 * generate the html for the week overview of facility availability.
 * including buttons to set it.
 * @author Daniel Oltmanns
 * @since 0.20090514 (05/14/2009)
 */
@SuppressWarnings("deprecation") // TODO #361 kill uses of deprecations
public class FamWeekAvailabilityHtml extends FamWeekHtml {

    private Facility facility;

    /**
     * a week shown in the browser representing the availability of a specific facility.
     * @param cal use date of this calendar to decide which week to show
     * @param facilityKey representing the specific facility shown in the calendar
     * @param famCalendarHtmlFactory factory used to generate specific elements.
     * @param showRedGreenOnly if true, availability is only separeted in availably and not available.
     *  this is used, when the calendar is checked for specific units.
     *  general not availabilities are still shown.
     * @param noScriptOnOverview if true, a noscript tag is added to the calendar prefix
     *  as explained in {@link FamCalendarHtmlFactory#getCalendarPrefixHtmlNavi(java.util.Calendar, de.knurt.fam.core.content.html.calendar.CalendarView, boolean, boolean)}
     */
    public FamWeekAvailabilityHtml(Calendar cal, String facilityKey, FamCalendarHtmlFactory famCalendarHtmlFactory, boolean showRedGreenOnly, boolean noScriptOnOverview) {
        super(cal, famCalendarHtmlFactory, showRedGreenOnly, noScriptOnOverview);
        this.facility = FacilityConfigDao.facility(facilityKey);
    }

    /**
     * @return the facilityKey
     */
    public String getFacilityKey() {
        return facility.getKey();
    }

    /**
     * return the content of a day in the week view of availabilities.
     * the day is the content of a td-element in the calendar.
     * @param c representing the day.
     * @return the content of a day in the week view of availabilities.
     */
    @Override
    protected String getTdContentDayIntern(Calendar c) {
        return "";
    }

    /**
     * return empty string.
     * the week view does not have a leading cell.
     * @param c representing the day
     * @return empty string
     */
    @Override
    protected String getTdContentLeadingCellIntern(Calendar c) {
        return "";
    }
}

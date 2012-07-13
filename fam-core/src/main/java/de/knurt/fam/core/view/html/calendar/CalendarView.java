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

import de.knurt.fam.core.view.html.calendar.factory.FamCalendarHtmlFactory;

/**
 * interface for a calendar view and calendar factories
 * @see FamMonthHtml
 * @see FamWeekHtml
 * @see FamCalendarHtmlFactory
 * @author Daniel Oltmanns
 * @since 0.20090516 (05/16/2009)
 */
@SuppressWarnings("deprecation") // TODO #11 kill uses of deprecations
public interface CalendarView extends Selectable {

    /**
     * return inner view name of the calendar
     * @return inner view name of the calendar
     */
    public String getCalendarViewName();

    /**
     * return the factory used to generate many contents.
     * every calendar has equal areas (like the upper navigation).
     * this is all generated in a factory returned here.
     * @return the factory used to generate many contents.
     */
    public FamCalendarHtmlFactory getDasCalendarHtmlGetter();
}

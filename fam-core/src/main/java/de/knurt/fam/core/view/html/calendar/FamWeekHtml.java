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

import static de.knurt.fam.core.config.style.FamCalendarStyle.calendarHeight;

import java.util.Calendar;

import de.knurt.fam.core.config.FamRequestContainer;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.core.view.html.FamImageHtmlGetter;
import de.knurt.fam.core.view.html.calendar.factory.FamCalendarHtmlFactory;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.ui.html.calendar.CalendarOneWeekHtml;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * the "das"-version of {@link CalendarOneWeekHtml}
 * @author Daniel Oltmanns
 * @since 0.20090514 (05/14/2009)
 */
@Deprecated
public abstract class FamWeekHtml extends CalendarOneWeekHtml implements CalendarView {

    private FamCalendarHtmlFactory dasCalendarHtmlGetter;
    private boolean showRedGreenOnly = false;
    private boolean noscriptTagOnOverview = false;

    /**
     * a week shown in the browser.
     * @param cal use date of this calendar to decide which week to show
     * @param dasCalendarHtmlGetter use this factory to generate elements.
     * @param showRedGreenOnly if true, availability is only separeted in availably and not available.
     *  this is used, when the calendar is checked for specific units.
     *  general not availabilities are still shown.
     * @param noscriptTagOnOverview if true, a noscript tag is added to the calendar prefix
     *  as explained in {@link FamCalendarHtmlFactory#getCalendarPrefixHtmlNavi(java.util.Calendar, de.knurt.fam.core.view.html.calendar.CalendarView, boolean, boolean)}
     */
    public FamWeekHtml(Calendar cal, FamCalendarHtmlFactory dasCalendarHtmlGetter, boolean showRedGreenOnly, boolean noscriptTagOnOverview) {
        super(cal, FamRequestContainer.locale());
        this.dasCalendarHtmlGetter = dasCalendarHtmlGetter;
        this.showRedGreenOnly = showRedGreenOnly;
        this.noscriptTagOnOverview = noscriptTagOnOverview;
    }

    /**
     * return the html content of a day.
     * for showing a day, a td-element is used.
     * @param c representing the day
     * @return the html content of a day.
     */
    protected abstract String getTdContentDayIntern(Calendar c);

    /**
     * return week as view name of the calendar
     * @see QueryKeys#WEEK
     * @return week as view name of the calendar
     */
    @Override
    public String getCalendarViewName() {
        return QueryKeys.WEEK;
    }

    /**
     * return the prefix for the week view.
     * this is where the user can select the week be viewed.
     * @param c representing the week.
     * @return the prefix for the week view.
     */
    @Override
    protected String getPrefixHtml(Calendar c) {
        String format = "<div class=\"dasCalendarNavigation\"><div>%s</div><div>%s</div></div>";
        String prefixHtml_weekChoice = this.getPrefixHtml_weekChoice((Calendar) c.clone());
        String prefixNavi = this.getDasCalendarHtmlGetter().getCalendarPrefixHtmlNavi(c, this, false, this.noscriptTagOnOverview).toString();
        return String.format(format, prefixHtml_weekChoice, prefixNavi);
    }

    private String getContentAnchor(Calendar c, int format) {
        if (format == Calendar.SHORT) {
            return c.get(Calendar.WEEK_OF_YEAR) + "";
        } else {
            return c.get(Calendar.WEEK_OF_YEAR) + "/" + c.get(Calendar.YEAR);
        }
    }

    /**
     * this is only the non-javascript bar
     * @param c
     * @return
     */
    private String getPrefixHtml_weekChoice(Calendar c) {
        String format = "<a href=\"%s\" class=\"lastYear\" title=\"%s\">%s</a>" +
                "<a href=\"%s\" class=\"xBefore\" title=\"%s\">%s</a>" +
                "<a href=\"%s\" class=\"xBefore\" title=\"%s\">%s</a>" +
                "<a href=\"%s\" class=\"xBefore\" title=\"%s\">%s</a>" +
                "<a href=\"%s\" class=\"lastX\" title=\"%s\">%s</a>" +
                "<a href=\"%s\" class=\"thisX\" title=\"%s\">%s</a>" +
                "<a href=\"%s\" class=\"nextX\" title=\"%s\">%s</a>" +
                "<a href=\"%s\" class=\"xAfter\" title=\"%s\">%s</a>" +
                "<a href=\"%s\" class=\"xAfter\" title=\"%s\">%s</a>" +
                "<a href=\"%s\" class=\"xAfter\" title=\"%s\">%s</a>" +
                "<a href=\"%s\" class=\"nextYear\" title=\"%s\">%s</a>";

        c.add(Calendar.MONTH, -12);
        String hrefLastYear = this.getQueryString(c).getAsHtmlLinkHref();
        String titleLastYear = this.getContentAnchor(c, Calendar.LONG);
        String valLastYear = this.getContentAnchor(c, Calendar.LONG);

        // -4, -3, -2 month
        String[] hrefx = new String[6];
        String[] titlex = new String[6];
        String[] valx = new String[6];
        c.add(Calendar.MONTH, 12);
        c.add(Calendar.WEEK_OF_YEAR, -4);
        int i = 0;
        while (i < 3) {
            hrefx[i] = this.getQueryString(c).getAsHtmlLinkHref();
            titlex[i] = this.getContentAnchor(c, Calendar.LONG);
            valx[i] = this.getContentAnchor(c, Calendar.SHORT);
            c.add(Calendar.WEEK_OF_YEAR, 1);
            i++;
        }

        // -1 month
        String hrefLastMonth = this.getQueryString(c).getAsHtmlLinkHref();
        String titleLastMonth = this.getContentAnchor(c, Calendar.LONG);
        String valLastMonth = this.getContentAnchor(c, Calendar.SHORT);

        // anchor to current and next of this WEEK_OF_YEAR
        c.add(Calendar.WEEK_OF_YEAR, 1);
        String hrefThisMonth = this.getQueryString(c).getAsHtmlLinkHref();
        String titleThisMonth = "show totday's week"; // INTLANG
        String valThisMonth = this.getContentAnchor(c, Calendar.LONG);

        // +1 WEEK_OF_YEAR
        c.add(Calendar.WEEK_OF_YEAR, 1);
        String hrefNextMonth = this.getQueryString(c).getAsHtmlLinkHref();
        String titleNextMonth = this.getContentAnchor(c, Calendar.LONG);
        String valNextMonth = this.getContentAnchor(c, Calendar.SHORT);

        // +2, +3, +4 WEEK_OF_YEAR
        while (i < 6) {
            c.add(Calendar.WEEK_OF_YEAR, 1);
            hrefx[i] = this.getQueryString(c).getAsHtmlLinkHref();
            titlex[i] = this.getContentAnchor(c, Calendar.LONG);
            valx[i] = this.getContentAnchor(c, Calendar.SHORT);
            i++;
        }

        // +1 year
        c.add(Calendar.WEEK_OF_YEAR, -5);
        c.add(Calendar.MONTH, 12);
        String hrefNextYear = this.getQueryString(c).getAsHtmlLinkHref();
        String titleNextYear = this.getContentAnchor(c, Calendar.LONG);
        String valNextYear = this.getContentAnchor(c, Calendar.LONG);

        // set back to this WEEK_OF_YEAR
        c.add(Calendar.MONTH, -12);

        return String.format(format,
                hrefLastYear, titleLastYear, valLastYear,
                hrefx[0], titlex[0], valx[0],
                hrefx[1], titlex[1], valx[1],
                hrefx[2], titlex[2], valx[2],
                hrefLastMonth, titleLastMonth, valLastMonth,
                hrefThisMonth, titleThisMonth, valThisMonth,
                hrefNextMonth, titleNextMonth, valNextMonth,
                hrefx[3], titlex[3], valx[3],
                hrefx[4], titlex[4], valx[4],
                hrefx[5], titlex[5], valx[5],
                hrefNextYear, titleNextYear, valNextYear);
    }

    /**
     * return the content of a day.
     * a day is a td-element in the calendar.
     * @param c representing the day
     * @return the content of a day.
     */
    @Override
    protected String getTdContentDay(Calendar c) {
        return this.wrapTd(this.getDayContentBackgroundImageKey(), this.getTdContentDayIntern(c), c).toString();
    }

    /**
     * return the key for loading the background-image.
     * this is used to load the image into the background.
     * @see FamImageHtmlGetter#getBackgroundImage(java.lang.String, de.knurt.heinzelmann.util.query.QueryString) 
     * @return the key for loading the background-image.
     */
    protected String getDayContentBackgroundImageKey() {
        return "oneweekday";
    }

    private HtmlElement wrapTd(String backgroundImageKey, String content, Calendar c) {
        QueryString qs = this.getQueryString(c);
        if (this.showRedGreenOnly) {
            qs.putAll(QueryStringBuilder.getAjaxFlag(true));
        }
        HtmlElement result = HtmlFactory.get("div");
        String style = FamImageHtmlGetter.getBackgroundImage(backgroundImageKey, qs);
        style += "height:" + calendarHeight() + "px;";
        result.att("style", style).add(content);
        return result;
    }

    /**
     * return the content of a cell in the leading row of the calendar.
     * @param c representing the week shown.
     * @return the content of a cell in the leading row of the calendar.
     */
    protected abstract String getTdContentLeadingCellIntern(Calendar c);

    /**
     * return the content of a cell in the leading row of the calendar.
     * wraps the td and delete it to {@link #getTdContentLeadingCell(java.util.Calendar)}
     * @param c representing the week shown.
     * @return the content of a cell in the leading row of the calendar.
     */
    @Override
    protected String getTdContentLeadingCell(Calendar c) {
        HtmlElement result = this.wrapTd("timelineofday", this.getTdContentLeadingCellIntern(c), c);
        String style = result.getAttributes().getProperty("style");
        result.att("style", style);
        return result.toString();
    }

    /**
     * wrap the output of <code>super.toString</code> into a html element with class name <code>weekHtml</code>.
     * @see FamCalendarHtmlFactory#toStringWrapper(java.lang.String, java.lang.String[])
     * @return output of <code>super.toString</code> into a html element with class name <code>weekHtml</code>.
     */
    @Override
    public String toString() {
        String[] classNames = {"weekHtml"};
        return FamCalendarHtmlFactory.toStringWrapper(super.toString(), classNames);
    }

    /**
     * return the query string needed to visit the given week in the week view.
     * @param c representing the week
     * @return the query string needed to visit the given week in the week view.
     */
    @Override
    public QueryString getQueryString(Calendar c) {
        return this.getDasCalendarHtmlGetter().getQueryString(c, this);
    }

    /**
     * return the factory generating html-elements.
     * @return the factory generating html-elements.
     */
    @Override
    public FamCalendarHtmlFactory getDasCalendarHtmlGetter() {
        return dasCalendarHtmlGetter;
    }
}

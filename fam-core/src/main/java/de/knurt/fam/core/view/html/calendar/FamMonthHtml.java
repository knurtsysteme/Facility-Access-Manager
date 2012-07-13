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
import java.util.Locale;

import de.knurt.fam.core.config.FamRequestContainer;
import de.knurt.fam.core.util.graphics.Images;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.view.html.calendar.factory.FamCalendarHtmlFactory;
import de.knurt.fam.core.view.text.FamDateFormat;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.calendar.CalendarOneMonthHtml;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * the "das"-version of {@link CalendarOneMonthHtml}
 * @author Daniel Oltmanns
 * @since 0.20090423 (04/23/2009)
 */
@Deprecated
public abstract class FamMonthHtml extends CalendarOneMonthHtml implements CalendarView {

    private FamCalendarHtmlFactory dasCalendarHtmlGetter;
    private boolean noscriptTagOnOverview;

    /**
     * a month shown in the browser.
     * @param cal use date of this calendar to decide which month to show
     * @param dasCalendarHtmlGetter use this factory to generate elements.
     * @param noscriptTagOnOverview if true, a noscript tag is added to the calendar prefix
     *  as explained in {@link FamCalendarHtmlFactory#getCalendarPrefixHtmlNavi(java.util.Calendar, de.knurt.fam.core.view.html.calendar.CalendarView, boolean, boolean)}
     */
    public FamMonthHtml(Calendar cal, FamCalendarHtmlFactory dasCalendarHtmlGetter, boolean noscriptTagOnOverview) {
        super(cal, FamRequestContainer.locale());
        this.dasCalendarHtmlGetter = dasCalendarHtmlGetter;
        this.noscriptTagOnOverview = noscriptTagOnOverview;
    }

    /**
     * return month as view name of the calendar.
     * @see QueryKeys#MONTH
     * @return month as view name of the calendar.
     */
    public String getCalendarViewName() {
        return QueryKeys.MONTH;
    }

    /**
     * return the week of day in short
     * @see FamDateFormat#getWeekFormatted(java.util.Calendar, int)
     * @param c day to return
     * @return the week of day in short
     */
    @Override
    protected String getTableHeadCellContent(Calendar c) {
        return FamDateFormat.getWeekFormatted(c, Calendar.SHORT);
    }

    /**
     * add a style attribute to result of superclass, showing showing the day as background-image and return.
     * @return super <code>getTdDay</code> a month-image into the background added
     */
    @Override
    protected HtmlElement getTdDay() {
        HtmlElement result = super.getTdDay();
        result.setAttribute("style", String.format("%s; height: %spx; margin:0; padding:0;", this.getStatus(this.getCal()), Images.ONE_MONTH_DAY_IMAGE_HEIGHT));
        return result;
    }

    /**
     * return <code>&lt;div&gt;</code>s with the day of the month and the content.
     * @see #getContent(java.util.Calendar)
     * @param c representing the day
     * @return <code>&lt;div&gt;</code>s with the day of the month and the content.
     */
    @Override
    protected String getTdContentDay(Calendar c) {
        String format = "<div class=\"daynumber\">%s</div>" +
                "<div class=\"calContent\">%s</div>";
        String daynumber = c.get(Calendar.DAY_OF_MONTH) + "";
        String content = this.getContent(c);
        return String.format(format, daynumber, content);
    }

    /**
     * return the status for the given day.
     * @param c representing the day
     * @return the status for the given day.
     */
    protected abstract String getStatus(Calendar c);

    /**
     * return the content for the given day.
     * @param c representing the day
     * @return the content for the given day.
     */
    protected abstract String getContent(Calendar c);

    /**
     * wrap the output of <code>super.toString</code> into a html element with class name <code>monthHtml</code>.
     * @see FamCalendarHtmlFactory#toStringWrapper(java.lang.String, java.lang.String[]) 
     * @return output of <code>super.toString</code> into a html element with class name <code>monthHtml</code>.
     */
    @Override
    public String toString() {
        String[] classNames = {"monthHtml"};
        return FamCalendarHtmlFactory.toStringWrapper(super.toString(), classNames);
    }

    /**
     * return the head of the month calendar.
     * this is where the user can select the month.
     * @param c representing the month
     * @return the head of the month calendar.
     */
    @Override
    protected String getPrefixHtml(Calendar c) {
        String format = "<div class=\"dasCalendarNavigation\"><div>%s</div><div>%s</div></div>";
        return String.format(format, this.getPrefixHtml_monthChoice(c), this.getDasCalendarHtmlGetter().getCalendarPrefixHtmlNavi(c, this, false, this.noscriptTagOnOverview).toString());
    }

    private String getPrefixHtml_monthChoice(Calendar c) {
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
        String titleLastYear = this.getContentAnchor(c, TEXT_LONG);
        String valLastYear = this.getContentAnchor(c, MONTH_MIN_12);

        // -4, -3, -2 month
        String[] hrefx = new String[6];
        String[] titlex = new String[6];
        String[] valx = new String[6];
        c.add(Calendar.MONTH, 12 - 4);
        int i = 0;
        while (i < 3) {
            hrefx[i] = this.getQueryString(c).getAsHtmlLinkHref();
            titlex[i] = this.getContentAnchor(c, TEXT_LONG);
            valx[i] = this.getContentAnchor(c, TEXT_SHORT);
            c.add(Calendar.MONTH, 1);
            i++;
        }

        // -1 month
        String hrefLastMonth = this.getQueryString(c).getAsHtmlLinkHref();
        String titleLastMonth = this.getContentAnchor(c, TEXT_LONG);
        String valLastMonth = this.getContentAnchor(c, MONTH_MIN_1);

        // anchor to current and next of this month
        c.add(Calendar.MONTH, 1);
        String hrefThisMonth = this.getQueryString(Calendar.getInstance()).getAsHtmlLinkHref();
        String titleThisMonth = "show totday's month"; // INTLANG
        String valThisMonth = this.getContentAnchor(c, TEXT_LONG);

        // +1 month
        c.add(Calendar.MONTH, 1);
        String hrefNextMonth = this.getQueryString(c).getAsHtmlLinkHref();
        String titleNextMonth = this.getContentAnchor(c, TEXT_LONG);
        String valNextMonth = this.getContentAnchor(c, MONTH_PLU_1);

        // +2, +3, +4 month
        while (i < 6) {
            c.add(Calendar.MONTH, 1);
            hrefx[i] = this.getQueryString(c).getAsHtmlLinkHref();
            titlex[i] = this.getContentAnchor(c, TEXT_LONG);
            valx[i] = this.getContentAnchor(c, TEXT_SHORT);
            i++;
        }

        // +1 year
        c.add(Calendar.MONTH, 12 - 4);
        String hrefNextYear = this.getQueryString(c).getAsHtmlLinkHref();
        String titleNextYear = this.getContentAnchor(c, TEXT_LONG);
        String valNextYear = this.getContentAnchor(c, MONTH_PLU_12);

        // set back to this month
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
    private static final int MONTH_PLU_1 = 1;
    private static final int TEXT_LONG = 100;
    private static final int TEXT_SHORT = 50;
    private static final int MONTH_MIN_1 = -1;
    private static final int MONTH_PLU_12 = 12;
    private static final int MONTH_MIN_12 = -12;

    private String getContentAnchor(Calendar c, int id) {
        Locale l = FamRequestContainer.locale();
        String result = "";
        switch (id) {
            case MONTH_PLU_1:
            case MONTH_MIN_1:
                result = c.getDisplayName(Calendar.MONTH, Calendar.SHORT, l);
                break;
            case MONTH_MIN_12:
            case MONTH_PLU_12:
                result = c.getDisplayName(Calendar.MONTH, Calendar.SHORT, l) + "&nbsp;" + c.get(Calendar.YEAR);
                break;
            case TEXT_SHORT:
                result = c.getDisplayName(Calendar.MONTH, Calendar.SHORT, l);
                break;
            case TEXT_LONG:
                result = c.getDisplayName(Calendar.MONTH, Calendar.LONG, l) + "&nbsp;" + c.get(Calendar.YEAR);
                break;
        }
        return result;
    }

    /**
     * return the query string needed to visit the given month in the month view.
     * @param c representing the month
     * @return the query string needed to visit the given month in the month view.
     */
    public QueryString getQueryString(Calendar c) {
        return this.getDasCalendarHtmlGetter().getQueryString(c, this);
    }

    /**
     * return the factory used to generate html for month view.
     * @return the factory used to generate html for month view.
     */
    public FamCalendarHtmlFactory getDasCalendarHtmlGetter() {
        return dasCalendarHtmlGetter;
    }
}

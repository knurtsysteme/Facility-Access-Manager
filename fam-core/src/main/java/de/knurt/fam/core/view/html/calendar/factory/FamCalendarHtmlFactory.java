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
package de.knurt.fam.core.view.html.calendar.factory;

import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import de.knurt.fam.core.config.FamCalendarConfiguration;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.core.view.html.calendar.CalendarView;
import de.knurt.fam.core.view.text.FamDateFormat;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.ui.html.StrictHtmlFactory;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * html generator for all calendar views. in all areas, there are different
 * calendar views (like month view, week view, year view etc.) that have some
 * html elements in common. these elements are generated here.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090516 (05/16/2009)
 */
@Deprecated
public abstract class FamCalendarHtmlFactory {

	/**
	 * return a drop down position where a time can be put in from 0:00 to
	 * 24:59.
	 * 
	 * @param prefix
	 *            is the prefix of the name attributes. set name of an
	 *            select-input: <code>
     *  prefix + [QueryKeys.QUERY_KEY_HOUR_OF_DAY | QueryKeys.QUERY_KEY_MINUTE_OF_HOUR]
     * </code> set id of an select-input: <code>
     *  prefix + [QueryKeys.QUERY_KEY_HOUR_OF_DAY | QueryKeys.QUERY_KEY_MINUTE_OF_HOUR] + "_id"
     * </code>
	 * @param selectFullHour
	 *            the full hour, that is selected. it is always the minute 0
	 *            selected.
	 * @return a drop down position where a time can be put in from 0:00 to
	 *         24:59.
	 */
	public static HtmlElement getTimeSelect(String prefix, int selectFullHour) {
		HtmlElement span = HtmlFactory.get("span");
		// add hour select
		Properties hourAtts = new Properties();
		hourAtts.setProperty("name", prefix + QueryKeys.QUERY_KEY_HOUR_OF_DAY);
		hourAtts.setProperty("id", prefix + QueryKeys.QUERY_KEY_HOUR_OF_DAY + "_id");

		HtmlElement selectEl = HtmlFactory.get_select(hourAtts);
		// optionsselection for an hour
		String hourSelectOptions = "";
		int i = 0;
		while (i <= 24) {
			HtmlElement option = HtmlFactory.get_option(i + "", (i < 10 ? "0" : "") + i, false);
			if (i == selectFullHour) {
				option.setAttribute("selected", "selected");
			}
			hourSelectOptions += option.toString();
			i++;
		}
		selectEl.add(hourSelectOptions);
		span.add(selectEl);

		span.add(":");

		// add minute select
		Properties minuteAtts = new Properties();
		minuteAtts.setProperty("name", prefix + QueryKeys.QUERY_KEY_MINUTE_OF_HOUR);
		minuteAtts.setProperty("id", prefix + QueryKeys.QUERY_KEY_MINUTE_OF_HOUR + "_id");

		selectEl = HtmlFactory.get_select(minuteAtts);
		// optionsselection for an hour
		String minuteSelectOptions = "";
		i = 0;
		while (i <= 59) {
			HtmlElement option = HtmlFactory.get_option(i + "", (i < 10 ? "0" : "") + i, false);
			minuteSelectOptions += option.toString();
			i += FamCalendarConfiguration.smallestMinuteStep();
		}
		selectEl.add(minuteSelectOptions);
		span.add(selectEl);
		return span;
	}

	/**
	 * return a select for a compact time format.
	 * 
	 * @param name
	 *            of the select
	 * @param selected
	 *            the day entry that is selected.
	 * @return a select with date information.
	 */
	public static HtmlElement getTimeSelectCompact(String name, int selected) {
		HtmlElement select = HtmlFactory.get("select");
		select.att("name", name);
		Calendar pointer = Calendar.getInstance();
		pointer.set(Calendar.HOUR_OF_DAY, 0);
		pointer.set(Calendar.MINUTE, 0);
		int day = pointer.get(Calendar.DAY_OF_YEAR);
		boolean selectHour = false;
		while (day == pointer.get(Calendar.DAY_OF_YEAR)) {
			HtmlElement option = HtmlFactory.get("option").att("value", QueryKeys.getEncodeStringOfTime(pointer));
			option.add(FamDateFormat.getTimeFormatted(pointer));
			if (selectHour == false && pointer.get(Calendar.HOUR_OF_DAY) == selected) {
				option.att("selected");
				selectHour = true;
			}
			select.add(option);
			pointer.add(Calendar.MINUTE, FamCalendarConfiguration.smallestMinuteStep());
		}
		return select;
	}

	/**
	 * return a select with date information. the select starts at the given
	 * start and ends in steps given as size of option texts. text of the option
	 * is given by <code>optiontexts</code>. value of options is
	 * {@link QueryKeys#getEncodeString(java.util.Calendar)}.
	 * 
	 * @param start
	 *            of the first option
	 * @param name
	 *            of the select
	 * @param optiontexts
	 *            texts for the options
	 * @param selected
	 *            the day entry that is selected.
	 * @return a select with date information.
	 */
	public static HtmlElement getDateSelect(Calendar start, String name, int selected, List<String> optiontexts) {
		HtmlElement select = HtmlFactory.get("select");
		select.att("name", name);
		int pointer = 0;
		while (pointer < optiontexts.size()) {
			HtmlElement option = HtmlFactory.get("option").att("value", QueryKeys.getEncodeStringOfDate(start));
			option.add(optiontexts.get(pointer));
			if (pointer == selected) {
				option.att("selected");
			}
			select.add(option);
			pointer++;
			start.add(Calendar.DAY_OF_YEAR, 1);
		}
		start.add(Calendar.DAY_OF_YEAR, -optiontexts.size());
		return select;
	}

	/**
	 * return a select with date information. the select starts at the given
	 * start and ends in <code>days</code> steps. text of the option is
	 * {@link FamDateFormat#getDateFormatted(java.util.Date)} or
	 * {@link FamDateFormat#getDateFormattedWithoutYear(java.util.Calendar)}
	 * depending on <code>showYear</code>. value of options is
	 * {@link QueryKeys#getEncodeString(java.util.Calendar)}.
	 * 
	 * @param start
	 *            of the first option
	 * @param days
	 *            to show
	 * @param name
	 *            of the select
	 * @param selected
	 *            the day entry that is selected.
	 * @param showYear
	 *            true, if the option text shall contain the year
	 * @return a select with date information.
	 */
	public static HtmlElement getDateSelect(Calendar start, int days, String name, int selected, boolean showYear) {
		HtmlElement select = HtmlFactory.get("select");
		select.att("name", name);
		int pointer = 0;
		while (pointer < days) {
			HtmlElement option = HtmlFactory.get("option").att("value", QueryKeys.getEncodeStringOfDate(start));
			if (showYear) {
				option.add(FamDateFormat.getDateFormatted(start.getTime()));
			} else {
				option.add(FamDateFormat.getDateFormattedWithoutYear(start));
			}
			if (pointer == selected) {
				option.att("selected");
			}
			select.add(option);
			pointer++;
			start.add(Calendar.DAY_OF_YEAR, 1);
		}
		start.add(Calendar.DAY_OF_YEAR, -days);
		return select;
	}

	/**
	 * return the given <code>toString</code> wrapped into
	 * <code>&lt;div class="[classNames]"&gt;toString&lt;div&gt;</code>.
	 * 
	 * @param toString
	 *            to wrap string
	 * @param classNames
	 *            set in the div-element
	 * @return the given <code>toString</code> wrapped into
	 *         <code>&lt;div class="[classNames]"&gt;toString&lt;div&gt;</code>.
	 */
	public static String toStringWrapper(String toString, String[] classNames) {
		String result = "<div class=\"dasCalendar";
		for (String className : classNames) {
			result += " " + className;
		}
		result += "\">" + toString + "</div>";
		return result;
	}

	/**
	 * return the head of the calendar containing the control panel. the panel
	 * contains a link for "show today" and "go to date". There is a possibility
	 * to switch the view from "week" to "month" (and other way around).
	 * 
	 * @param c
	 *            the date is used to show the right calendar time.
	 * @param currentCalVName
	 *            view name of the current calendar. this is used to show the
	 *            view as link or not. e.g. if {@link QueryKeys#WEEK}, the
	 *            "week view" is <strong>not</strong> shown as a link, but month
	 *            view does.
	 * @param timeWrapNavigationOnOverview
	 *            if false, does not return the output for switching time.
	 * @param noscriptTagOnOverview
	 *            if true, the entire output is wrapped into a
	 *            <code>&lt;noscript&gt;</code>
	 * @return the head of the calendar containing the control panel.
	 */
	public HtmlElement getCalendarPrefixHtmlNavi(Calendar c, String currentCalVName, boolean timeWrapNavigationOnOverview, boolean noscriptTagOnOverview) {
		HtmlElement result = HtmlFactory.get("div");
		Properties atts = new Properties();
		String href = "";
		String content = "";
		HtmlElement element = null;
		if (timeWrapNavigationOnOverview == true || currentCalVName.equals(QueryKeys.OVERVIEW) == false) {
			HtmlElement timeWrapDiv = HtmlFactory.get("div");
			timeWrapDiv.addClassName("timeWrapDiv");
			href = getQueryString(Calendar.getInstance(), currentCalVName).getAsHtmlLinkHref();
			content = "show today"; // INTLANG
			atts.put("title", content);
			element = StrictHtmlFactory.get().get_a(href, content, atts);
			timeWrapDiv.add(element);

			timeWrapDiv.add("&nbsp;");

			content = "go to date"; // INTLANG
			atts.put("title", content);
			atts.put("style", "display: none;");
			element = StrictHtmlFactory.get().get_a("#", content, atts);
			atts.put("style", "");
			String id = "js_select_date";
			element.setAttribute("id", id);
			element.setAttribute("name", id);
			timeWrapDiv.add(element);
			timeWrapDiv.add("<input type=\"text\" id=\"hidden_pseudo_id\" name=\"hidden_pseudo_id\" style=\"display: none;\" />"); // calendar
																																	// container
																																	// needed
																																	// for
																																	// js
			result.add(timeWrapDiv);
		}

		content = "week view"; // INTLANG
		if (currentCalVName.equals(QueryKeys.WEEK)) {
			element = HtmlFactory.get("span");
			element.add(content);
		} else {
			href = getQueryString(c, QueryKeys.WEEK).getAsHtmlLinkHref();
			atts.put("title", content);
			element = StrictHtmlFactory.get().get_a(href, content, atts);
		}
		result.add(element);

		result.add("&nbsp;");

		content = "month view"; // INTLANG
		if (currentCalVName.equals(QueryKeys.MONTH)) {
			element = HtmlFactory.get("span");
			element.add(content);
		} else {
			href = getQueryString(c, QueryKeys.MONTH).getAsHtmlLinkHref();
			atts.put("title", content);
			element = StrictHtmlFactory.get().get_a(href, content, atts);
		}
		result.add(element);

		result.add("&nbsp;");

		content = "overview"; // INTLANG
		if (currentCalVName.equals(QueryKeys.OVERVIEW)) {
			element = HtmlFactory.get("span");
			element.add(content);
		} else {
			href = getQueryString(c, QueryKeys.OVERVIEW).getAsHtmlLinkHref();
			atts.put("title", content);
			element = StrictHtmlFactory.get().get_a(href, content, atts);
		}
		if (noscriptTagOnOverview) {
			result.add(new HtmlElement("noscript").add(element));
		} else {
			result.add(element);
		}
		return result;
	}

	/**
	 * return the head of the calendar containing the control panel. the panel
	 * contains a link for "show today" and "go to date". There is a possibility
	 * to switch the view from "week" to "month" (and other way around).
	 * 
	 * @param c
	 *            the date is used to show the right calendar time.
	 * @param currentCalV
	 *            view of the current calendar. this is used to show the view as
	 *            link or not. e.g. if {@link QueryKeys#WEEK}, the "week view"
	 *            is <strong>not</strong> shown as a link, but month view does.
	 * @param timeWrapNavigationOnOverview
	 *            if false, does not return the output for switching time.
	 * @param noscriptTagOnOverview
	 *            if true, a noscript tag is added to the calendar prefix as
	 *            explained in
	 *            {@link FamCalendarHtmlFactory#getCalendarPrefixHtmlNavi(java.util.Calendar, de.knurt.fam.core.view.html.calendar.CalendarView, boolean, boolean)}
	 * @return the head of the calendar containing the control panel.
	 */
	public HtmlElement getCalendarPrefixHtmlNavi(Calendar c, CalendarView currentCalV, boolean timeWrapNavigationOnOverview, boolean noscriptTagOnOverview) {
		return this.getCalendarPrefixHtmlNavi(c, currentCalV.getCalendarViewName(), timeWrapNavigationOnOverview, noscriptTagOnOverview);
	}

	/**
	 * return the {@link QueryString} for reaching the date in the given
	 * calendar with the given calendar view.
	 * 
	 * @param c
	 *            date is used in query string
	 * @param calendarView
	 *            that is used in query string
	 * @return {@link QueryString} for reaching the date in the given calendar
	 *         with the given calendar view.
	 */
	public QueryString getQueryString(Calendar c, CalendarView calendarView) {
		return this.getQueryString(c, calendarView.getCalendarViewName());
	}

	/**
	 * return the {@link QueryString} for reaching the date in the given
	 * calendar with the given calendar view.
	 * 
	 * @param c
	 *            date is used in query string
	 * @param calendarViewName
	 *            that is used in query string
	 * @return {@link QueryString} for reaching the date in the given calendar
	 *         with the given calendar view.
	 */
	public QueryString getQueryString(Calendar c, String calendarViewName) {
		QueryString qs = QueryStringBuilder.getQueryString(c);
		qs.put(QueryKeys.QUERY_KEY_CALENDAR_VIEW, calendarViewName);
		return qs;
	}

	/**
	 * return the action-attribute-value of a form in the calendar, when it is
	 * sent.
	 * 
	 * @param c
	 *            calendar the form is for
	 * @param calendarViewName
	 *            the view name of the calendar
	 * @return the action-attribute-value of a form in the calendar, when it is
	 *         sent.
	 */
	protected abstract String getFormAction(Calendar c, String calendarViewName);

	/**
	 * return the hidden input of the form in the head. is is used to sent
	 * background information, the user must not put in.
	 * 
	 * @param c
	 *            the calendar of selected date
	 * @param calendarViewName
	 *            view name of selected calendar view
	 * @return the hidden input of the form in the head.
	 */
	protected String getHiddenInput(Calendar c, String calendarViewName) {
		QueryString query = QueryStringBuilder.getQueryString(c);
		query.put(QueryKeys.QUERY_KEY_CALENDAR_VIEW, calendarViewName);
		return query.getAsHtmlInputsTypeHidden();
	}

	/**
	 * return the input form executer of the form shown in the table cell. that
	 * might be a button sending a form or a simple link to another page.
	 * 
	 * @param c
	 *            calendar for the current cell
	 * @return html of input form executing the form shown in the table's cell.
	 */
	protected Object getInputFormSend(Calendar c) {
		HtmlElement result = HtmlFactory.get("button", "submit");
		result.setAttribute("type", "submit");
		return result;
	}
}

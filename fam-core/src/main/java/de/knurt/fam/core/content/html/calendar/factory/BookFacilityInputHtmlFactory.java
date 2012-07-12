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
package de.knurt.fam.core.content.html.calendar.factory;

import java.util.Calendar;
import java.util.Properties;

import de.knurt.fam.core.content.html.factory.FamSubmitButtonFactory;
import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.ui.html.StrictHtmlFactory;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * factory to create html needed to put a booking into the system.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090613 (06/13/2009)
 */
@Deprecated
public class BookFacilityInputHtmlFactory extends AvailabilityInputHtmlFactory {

	private BookingRule bookingRule;
	private int capacityUnits;
	private int timeUnits;
	private TimeBookingRequest bookingRequest;

	/**
	 * construct the factory for a given booking request.
	 * 
	 * @param bookingRequest
	 */
	public BookFacilityInputHtmlFactory(TimeBookingRequest bookingRequest) {
		super(bookingRequest.getFacility().getKey(), bookingRequest.getRequestedStartTime());
		this.bookingRule = bookingRequest.getBookingRule();
		this.capacityUnits = bookingRequest.getRequestedCapacityUnits();
		this.timeUnits = bookingRequest.getRequestedTimeUnits();
		this.bookingRequest = bookingRequest;
	}

	/**
	 * return the input for a direct booking request
	 * 
	 * @param c
	 *            of choosen day
	 * @param currentCalVName
	 *            name of the calendar view (week, month etc.)
	 * @return the input for a direct booking request
	 */
	public HtmlElement getJavascriptBookingNavi_input(Calendar c, String currentCalVName) {
		HtmlElement result = new HtmlElement("div");
		HtmlElement form = HtmlFactory.get("form");

		String format3tdString = "<td style=\"text-align: left;\">%s</td><td class=\"date\">%s</td><td class=\"time\">%s</td>";
		String format1tdString = "<td style=\"text-align: left;\" colspan=\"3\">%s</td>";
		HtmlElement table = HtmlFactory.get("table");
		table.addClassName("justtext");

		result.setId("js_booking_request_panel");
		result.setAttribute("style", "display: none;");

		HtmlElement tr = HtmlFactory.get("tr", String.format(format1tdString, "I'd like to book")); // INTLANG
		table.add(tr);

		tr = HtmlFactory.get("tr", String.format(format1tdString, this.getUnitField()));
		table.add(tr);

		if (this.bookingRule.getMinBookableCapacityUnits(this.bookingRequest.getUser()) < this.bookingRule.getMaxBookableCapacityUnits(this.bookingRequest.getUser())) {
			String content = "show free time for these units"; // INTLANG
			Properties p = new Properties();
			p.put("title", content);
			HtmlElement anchorShowFreeSlota = StrictHtmlFactory.get().get_a("#", content, p).setId("js_booking_request_panel_select_anchor");
			tr = HtmlFactory.get("tr", String.format(format1tdString, anchorShowFreeSlota));
			table.add(tr);
		}

		form.add(QueryStringBuilder.getQueryString(this.bookingRequest.getFacility()).getAsHtmlInputsTypeHidden());

		HtmlElement calendarSelectDate = new HtmlElement("select");
		calendarSelectDate.setAttribute("name", QueryKeys.JS_KEY_FROM_DATE);
		Calendar pointer = Calendar.getInstance();
		int day = 0;
		while (day < 100) { // show 100 days
			HtmlElement option = new HtmlElement("option");
			option.setAttribute("value", QueryKeys.getEncodeStringOfDate(pointer));
			if (day == 0) {
				option.setAttribute("selected", "selected");
			}
			option.add(FamDateFormat.getLangIndependantShortDate(pointer));
			calendarSelectDate.add(option);
			pointer.add(Calendar.DAY_OF_YEAR, 1);
			day++;
		}

		int hourNow = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		tr = HtmlFactory.get("tr", String.format(format3tdString, "from", calendarSelectDate, FamCalendarHtmlFactory.getTimeSelect(QueryKeys.JS_KEY_FROM_TIME, hourNow + 1))); // INTLANG
		table.add(tr);

		calendarSelectDate.setAttribute("name", QueryKeys.JS_KEY_TO_DATE);
		tr = HtmlFactory.get("tr", String.format(format3tdString, "to", calendarSelectDate, FamCalendarHtmlFactory.getTimeSelect(QueryKeys.JS_KEY_TO_TIME, hourNow + 2))); // INTLANG
		table.add(tr);

		HtmlElement submitButton = FamSubmitButtonFactory.getButton("check!");
		tr = HtmlFactory.get("tr", "<td style=\"text-align: right;\" colspan=\"3\">" + submitButton + "</td>"); // INTLANG
		table.add(tr);

		form.add(table);
		result.add(form);

		return result;
	}

	private HtmlElement getJavascriptBookingNavi(Calendar c, String currentCalVName, boolean timeWrapNavigationOnOverview) {
		HtmlElement result = HtmlFactory.get("div");
		result.setAttribute("style", "display: none;");
		String id = "js_booking_request";
		result.setAttribute("id", id);
		result.setAttribute("name", id);

		String content = "request booking";
		Properties p = new Properties();
		p.put("title", content);
		HtmlElement a = StrictHtmlFactory.get().get_a("#", content, p);
		a.addTitleAttribute(content);
		result.add(a);
		result.add(this.getJavascriptBookingNavi_input(c, currentCalVName));
		return result;
	}

	private HtmlElement getNoscriptBookingNavi(Calendar c, String currentCalVName, boolean timeWrapNavigationOnOverview) {
		HtmlElement result = HtmlFactory.get("noscript");

		HtmlElement noscript;
		// get unit and time fields (as text or form)
		String unitField = this.getUnitField().toString();
		String timeField = this.getTimeField();
		String formatMe = "<p>Make your booking for %s for %s</p>"; // INTLANG

		if (this.bookingRule.getMinBookableTimeUnits(this.bookingRequest.getUser()) < this.bookingRule.getMaxBookableTimeUnits(this.bookingRequest.getUser())
				|| this.bookingRule.getMinBookableCapacityUnits(this.bookingRequest.getUser()) < this.bookingRule.getMaxBookableCapacityUnits(this.bookingRequest.getUser())) { // has
			// form
			// elements
			noscript = HtmlFactory.get("form");
			noscript.setAttribute("action", TemplateHtml.href("book2"));
			noscript.setAttribute("method", "get");
			QueryString ajax = QueryStringBuilder.getAjaxFlag(true);
			noscript.add("<p>" + this.getHiddenInput(c, currentCalVName) + ajax.getAsHtmlInputsTypeHidden() + "</p>");

			HtmlElement changeButton = FamSubmitButtonFactory.getNextButton("change"); // INTLANG
			changeButton.addClassName("js_hide");

			// add to navigation
			noscript.add(String.format(formatMe, unitField, timeField));
			noscript.add("<p>" + changeButton + "</p>");
		} else {
			noscript = HtmlFactory.get("div");
			noscript.add(String.format(formatMe, unitField, timeField));
		}
		result.add(noscript);
		return result;
	}

	@Override
	public HtmlElement getCalendarPrefixHtmlNavi(Calendar c, String currentCalVName, boolean timeWrapNavigationOnOverview, boolean noScriptOnOverview) {
		HtmlElement result = HtmlFactory.get("div");
		result.add(super.getCalendarPrefixHtmlNavi(c, currentCalVName, timeWrapNavigationOnOverview, true));
		result.add(this.getNoscriptBookingNavi(c, currentCalVName, timeWrapNavigationOnOverview));
		result.add(this.getJavascriptBookingNavi(c, currentCalVName, timeWrapNavigationOnOverview));
		return result;
	}

	@Override
	protected String getFormAction(Calendar c, String calendarViewName) {
		return TemplateHtml.href("book2");
	}

	@Override
	public QueryString getQueryString(Calendar c, String calendarViewName) {
		QueryString result = super.getQueryString(c, calendarViewName);
		result.put(QueryKeys.QUERY_KEY_UNITS_CAPACITY, this.capacityUnits);
		result.put(QueryKeys.QUERY_KEY_UNITS_TIME, this.timeUnits);
		return result;
	}

	@Override
	protected Object getInputFormSend(Calendar c) {
		return "";
	}

	private String getTimeField() {
		String result = "";
		// create drop down for selecting time or info, if there is only one
		int availableTimeUnit = this.bookingRule.getMinBookableTimeUnits(this.bookingRequest.getUser());
		if (availableTimeUnit < this.bookingRule.getMaxBookableTimeUnits(this.bookingRequest.getUser())) {
			HtmlElement timeSelect = new HtmlElement("select");
			timeSelect.setAttribute("name", QueryKeys.QUERY_KEY_UNITS_TIME);
			while (availableTimeUnit <= this.bookingRule.getMaxBookableTimeUnits(this.bookingRequest.getUser())) {
				HtmlElement option = new HtmlElement("option");
				option.setAttribute("value", availableTimeUnit);
				if (availableTimeUnit == this.timeUnits) {
					option.setAttribute("selected", "selected");
				}
				option.add(this.bookingRule.getTimeLabel(availableTimeUnit));
				timeSelect.add(option);
				availableTimeUnit++;
			}
			result = timeSelect.toString();
		} else { // there is only one unit to book
			result = this.bookingRule.getTimeLabel(availableTimeUnit);
		}
		return result;
	}

	private HtmlElement getUnitField() {
		HtmlElement result = null;
		// create drop down for selecting capacity or info, if there is only one
		int availableCapacityUnit = this.bookingRule.getMinBookableCapacityUnits(this.bookingRequest.getUser());
		if (availableCapacityUnit < this.bookingRule.getMaxBookableCapacityUnits(this.bookingRequest.getUser())) {
			HtmlElement capacitySelect = new HtmlElement("select");
			capacitySelect.setAttribute("name", QueryKeys.QUERY_KEY_UNITS_CAPACITY);
			while (availableCapacityUnit <= this.bookingRule.getMaxBookableCapacityUnits(this.bookingRequest.getUser())) {
				HtmlElement option = new HtmlElement("option");
				option.setAttribute("value", availableCapacityUnit);
				if (availableCapacityUnit == this.capacityUnits) {
					option.setAttribute("selected", "selected");
				}
				option.add(availableCapacityUnit + " " + this.bookingRule.getCapacityUnitName(availableCapacityUnit));
				capacitySelect.add(option).setId("js_booking_request_panel_select");
				availableCapacityUnit++;
			}
			result = capacitySelect;
		} else { // there is only one unit to book
			result = HtmlFactory.get("p").add(availableCapacityUnit + " " + this.bookingRule.getCapacityUnitName(availableCapacityUnit));
			QueryString qs = new QueryString();
			qs.put(QueryKeys.QUERY_KEY_UNITS_CAPACITY, availableCapacityUnit);
			result.add(qs.getAsHtmlInputsTypeHidden());
		}
		return result;
	}
}

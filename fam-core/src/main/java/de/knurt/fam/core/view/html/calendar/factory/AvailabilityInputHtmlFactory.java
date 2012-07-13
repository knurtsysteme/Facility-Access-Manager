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

import java.util.ArrayList;
import java.util.Calendar;

import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.core.view.html.factory.FamSubmitButtonFactory;
import de.knurt.fam.core.view.text.FamText;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.util.query.QueryString;
import de.knurt.heinzelmann.util.time.AbstractIntervalTimeFrame;
@Deprecated
public class AvailabilityInputHtmlFactory extends FamCalendarHtmlFactory {
	private String facilityKey;
	private String iterationsSelectOptions;
	private String yesNoSelectOptions;

	/** {@inheritDoc} */
	@Override
	protected String getHiddenInput(Calendar c, String calendarViewName) {
		String result = super.getHiddenInput(c, calendarViewName);
		QueryString query = new QueryString();
		query.put(QueryKeys.QUERY_KEY_FACILITY, this.facilityKey);
		result += query.getAsHtmlInputsTypeHidden();
		return result;
	}

	private Calendar cgot;

	/**
	 * construct the factory.
	 * 
	 * @param facilityKey
	 *            key representing facility elements are generated for.
	 * @param c
	 *            used to indicate the time to show
	 */
	public AvailabilityInputHtmlFactory(String facilityKey, Calendar c) {
		this.facilityKey = facilityKey;
		this.cgot = (Calendar) c.clone();
		// to avoid performace problems, some iterations (that must be done for
		// every day of the calendar) are done here.
		this.setIterationsSelectOptions(c);
		this.setYesNotSelectOptions();
	}

	/**
	 * return the query string to enter the calendar at the given time with in
	 * given view.
	 * 
	 * @see QueryStringBuilder#getBigCalendarQueryString
	 * @param c
	 *            time visitied with this query string.
	 * @param calendarViewName
	 *            like month or week to show
	 * @return the query string to enter the calendar at the given time with in
	 *         given view.
	 */
	@Override
	public QueryString getQueryString(Calendar c, String calendarViewName) {
		return QueryStringBuilder.getBigCalendarQueryString(this.facilityKey, c, calendarViewName);
	}

	/**
	 * return <code>systemfacilityavailability.html</code> as form action.
	 * 
	 * @see QueryStringBuilder#getBigCalendarQueryString
	 * @return <code>systemfacilityavailability.html</code> as form action.
	 */
	@Override
	protected String getFormAction(Calendar c, String calendarViewName) {
		return TemplateHtml.href("systemfacilityavailability") + this.getQueryString(c, calendarViewName);
	}

	private boolean isDayOfCalGot(Calendar c) {
		return c.get(Calendar.DAY_OF_YEAR) == this.cgot.get(Calendar.DAY_OF_YEAR) && c.get(Calendar.YEAR) == this.cgot.get(Calendar.YEAR);
	}

	/**
	 * return submit button labeled with "choose day!", if the day is not
	 * selected, or labeled with "set!" if the day is selected (and a form is
	 * shown). this assumes showing a form with inputs on selected days and
	 * nothing else but a "choose day!" button, to select the day.
	 * 
	 * @param c
	 *            representing the day.
	 * @return submit button labeled with "choose day!" or "set!"
	 */
	@Override
	protected Object getInputFormSend(Calendar c) {
		String label = "";
		if (this.isDayOfCalGot(c)) {
			label = "set!";
		} else {
			label = "choose day!";
		}

		return FamSubmitButtonFactory.getNextButton(label);
	}

	private Calendar getStartOfToday() {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		return now;
	}

	/**
	 * iterations: - no (one time) - every day - every week - every month -
	 * every year
	 */
	private void setIterationsSelectOptions(Calendar c) {
		this.iterationsSelectOptions = "";
		ArrayList<Integer> iterations = new ArrayList<Integer>();
		if (c.getTimeInMillis() >= this.getStartOfToday().getTimeInMillis()) {
			iterations.add(AbstractIntervalTimeFrame.ONE_TIME);
		}
		iterations.add(AbstractIntervalTimeFrame.EACH_HOUR);
		iterations.add(AbstractIntervalTimeFrame.EACH_DAY);
		iterations.add(AbstractIntervalTimeFrame.EACH_WEEK);
		iterations.add(AbstractIntervalTimeFrame.EACH_MONTH);
		iterations.add(AbstractIntervalTimeFrame.EACH_YEAR);
		for (int iteration : iterations) {
			HtmlElement option = HtmlFactory.get_option(iteration + "", FamText.message("calendar.iteration." + iteration), false);
			this.iterationsSelectOptions += option.toString();
		}
	}

	private void setYesNotSelectOptions() {
		this.yesNoSelectOptions = "";

		int daAvailability = FacilityAvailability.COMPLETE_AVAILABLE;
		HtmlElement option = HtmlFactory.get_option(daAvailability, FamText.facilityAvailabilityShort(daAvailability), false); // INTLANG
		this.yesNoSelectOptions += option.toString();

		daAvailability = FacilityAvailability.GENERAL_NOT_AVAILABLE;
		option = HtmlFactory.get_option(daAvailability, FamText.facilityAvailabilityShort(daAvailability), false); // INTLANG
		this.yesNoSelectOptions += option.toString();

		daAvailability = FacilityAvailability.MAINTENANCE_NOT_AVAILABLE;
		option = HtmlFactory.get_option(daAvailability, FamText.facilityAvailabilityShort(daAvailability), false); // INTLANG
		option.setAttribute("selected", "selected");
		this.yesNoSelectOptions += option.toString();

	}
}
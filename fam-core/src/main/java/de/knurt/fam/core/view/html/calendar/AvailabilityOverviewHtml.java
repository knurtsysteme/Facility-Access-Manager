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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.core.view.html.factory.FamFormFactory;
import de.knurt.fam.core.view.html.factory.FamSubmitButtonFactory;
import de.knurt.fam.core.view.text.FamDateFormat;
import de.knurt.fam.core.view.text.FamText;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * calendar overview html for the availability of a specific facility. shows a
 * table with the given facility and all its parents as head. table content is the
 * availability of the facility with all details of the availability set.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090527 (05/27/2009)
 */
@Deprecated
public class AvailabilityOverviewHtml extends FamOverviewHtml {

	private String headline = null;
	private String baseFacilityKey = null;
	private Calendar baseCal = null;

	/**
	 * construct overview for the calendar showing the availability of a facility.
	 * 
	 * @param cal
	 *            the date of the calendar is included at the overview.
	 * @param facilityKey
	 *            key representing the facility the calendar shows.
	 * @param das
	 *            availabilities to show in overview
	 * @param auth
	 *            current user being auth
	 */
	public AvailabilityOverviewHtml(Calendar cal, String facilityKey, List<FacilityAvailability> das, User auth) {
		super("table");
		
		List<FacilityAvailability> facilitiesToShow = new ArrayList<FacilityAvailability>();
		for (FacilityAvailability devava : das) {
			if (!this.doNotShow(devava)) {
				facilitiesToShow.add(devava);
			}
		}
		
		this.addClassName("standard");
		this.baseFacilityKey = facilityKey;
		this.baseCal = cal;
		if(facilitiesToShow.size() > 0) {
			this.addNavi();
		}

		for (FacilityAvailability devava : facilitiesToShow) {
			this.mayAddHeadline(devava);
			this.addFacilityInfoRow(devava, auth);
		}
	}

	private void addFacilityInfoRow(FacilityAvailability da, User auth) {
		HtmlElement tr = this.getTr();

		HtmlElement td = HtmlFactory.get("td");
		String status = "";
		if (da.isCompletelyAvailable()) {
			status = "available";
		} else if (da.isNotAvailableBecauseOfMaintenance()) {
			status = "maintenance";
		} else if (da.isNotAvailableBecauseOfSuddenFailure()) {
			status = "failure";
		} else if (da.isNotAvailableInGeneral()) {
			status = "generalnot";
		} else if (da.mustNotStartHere()) {
			status = "mustnotstarthere";
		}

		String text = FamText.facilityAvailability(da);
		HtmlElement img = HtmlFactory.get("img").att("src", "availabilitylegendimage.img?status=" + status).att("title", text).att("style", "border: 1px solid black;");
		td.add(img).add(HtmlFactory.get("br")).add(text);
		tr.add(td);

		td = HtmlFactory.get("td");
		td.add(FamDateFormat.getIntervalFormatted(da, true));
		tr.add(td);

		td = HtmlFactory.get("td");
		td.add(FamText.message("calendar.iteration." + da.getInterval()));
		tr.add(td);

		td = HtmlFactory.get("td");
		td.add(FamDateFormat.getDateFormattedWithTime(da.getTimeStampSet()));
		tr.add(td);

		td = HtmlFactory.get("td");
		td.add(da.getUserSetThis() != null ? da.getUserSetThis().getFullName() : "unknown"); // INTLANG
		tr.add(td);

		td = HtmlFactory.get("td");
		td.add(da.hasNotice() ? da.getNotice() : "-");
		tr.add(td);

		td = HtmlFactory.get("td");

		// query string for availability
		QueryString queryString = QueryStringBuilder.getBigCalendarQueryString(this.baseFacilityKey, this.baseCal, QueryKeys.OVERVIEW);
		queryString.put(QueryKeys.QUERY_KEY_DELETE, da.getFacilityAvailabilityId() + "");
		queryString.put(QueryKeys.QUERY_KEY_AVAILABLILITY, da.getFacilityAvailabilityId() + "");

		// edit button
		if (da.getUsernameSetThis().equals(auth.getUsername())) {
			HtmlElement editForm = FamFormFactory.getForm(queryString, FamSubmitButtonFactory.getEditButton()).setAttribute("action", TemplateHtml.href("editfacilityavailability")).setAttribute("method", "get");
			td.add(editForm).add(HtmlFactory.get("br"));
		}

		// create and add delete button
		HtmlElement delete = FamSubmitButtonFactory.getDeleteButton(); // INTLANG
		delete.addClassName("important");
		HtmlElement form = HtmlFactory.get_form("post", TemplateHtml.href("systemfacilityavailability"));
		form = FamSubmitButtonFactory.getButtonAsForm(queryString, form, delete);
		td.add(form);

		tr.add(td);

		this.add(tr);
	}

	private void addHeadline() {
		HtmlElement tr = HtmlFactory.get("tr");
		HtmlElement th = HtmlFactory.get("th");
		th.setAttribute("colspan", "7");
		th.add(this.headline);
		tr.add(th);
		this.add(tr);
		this.addTableHead();
	}

	private void addTableHead() {
		HtmlElement tr = HtmlFactory.get("tr");

		HtmlElement th = HtmlFactory.get("th");
		th.add("Available"); // INTLANG
		tr.add(th);

		th = HtmlFactory.get("th");
		th.add("Base-Time"); // INTLANG
		tr.add(th);

		th = HtmlFactory.get("th");
		th.add("Interval"); // INTLANG
		tr.add(th);

		th = HtmlFactory.get("th");
		th.add("Set on"); // INTLANG
		tr.add(th);

		th = HtmlFactory.get("th");
		th.add("Set by"); // INTLANG
		tr.add(th);

		th = HtmlFactory.get("th");
		th.add("Notice"); // INTLANG
		tr.add(th);

		th = HtmlFactory.get("th");
		th.add("Operations"); // INTLANG
		tr.add(th);

		this.add(tr);
	}

	private String getFacilityHeadline(FacilityAvailability devava) {
		return this.getFacilityHeadline(devava.getFacilityKey());
	}

	private void mayAddHeadline(FacilityAvailability devava) {
		if (this.headline == null || this.getFacilityHeadline(devava).equals(this.headline) == false) {
			this.headline = this.getFacilityHeadline(devava);
			this.addHeadline();
		}
	}

	/**
	 * add a navigation to the overview. this is the top area, where you can do
	 * different interactions.
	 */
	@Override
	protected void addNavi() {
		this.add("<h1>Existing rules</h1>"); // INTLANG
	}

	private boolean doNotShow(FacilityAvailability devava) {
		return (devava.isOneTime() || devava.isNotAvailableBecauseOfSuddenFailure()) && devava.getBasePeriodOfTime().endsInPast();
	}
}

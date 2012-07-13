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
package de.knurt.fam.template.controller.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.FamShoppingCart;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.util.booking.BookingFinder;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.core.view.html.factory.FamSubmitButtonFactory;
import de.knurt.fam.core.view.text.FamDateFormat;
import de.knurt.fam.core.view.text.FamText;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.util.query.QueryString;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * controller for ajax request to book a facility.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090917 (09/17/2009)
 */
public class DirectBookingRequestController extends JSONController {

	private static final int NOT_POSSIBLE_THIS_WAY = -2;
	private static final int BOOKED_UP = -1;
	private static final int BOOKING_AVAILABLE = 1;

	/**
	 * return an available possible booking.
	 * 
	 * there are two different places where this is called: 1. the
	 * "request booking" link in the navigation of the calendar 2. the drag and
	 * drop action in same calendar
	 * 
	 * the response depends on which place causes the request.
	 * 
	 * @param rq
	 *            request
	 * @param rs
	 *            response
	 * @return an available possible booking.
	 */
	@Override
	public JSONObject getJSONObject(HttpServletRequest rq, HttpServletResponse rs) {
		JSONObject result = new JSONObject();
		User authUser = SessionAuth.user(rq);
		TimeFrame tf = null;
		List<TimeBookingRequest> tbrs = null;
		FacilityBookable bd = RequestInterpreter.getBookableFacility(rq);
		Integer requestedCapacityUnits = null;
		boolean justEqualBookingIsAvailable = false;
		TimeBookingRequest perfectlyCandidate = null;
		if (authUser != null) {
			User user = SessionAuth.user(rq);
			int statusid = 0;
			if (bd != null && user != null) {
				BookingRule brule = bd.getBookingRule();
				if (RequestInterpreter.isAjaxRequestFromRequestBookingLink(rq)) {
					tf = RequestInterpreter.getTimeFrameFromAjaxRequestBookingLink(rq);
					requestedCapacityUnits = RequestInterpreter.getCapacityUnits(rq);
				} else { // it's the drag and drop action
					if (bd.getBookingRule().getSmallestMinutesBookable() * bd.getBookingRule().getMinBookableTimeUnits(authUser) > 1440) {
						statusid = NOT_POSSIBLE_THIS_WAY;
					} else {
						tf = RequestInterpreter.getTimeFrameWithDayOfYearAndMinutesStartEnd(rq);
					}
					requestedCapacityUnits = 1;
				}
				if (tf != null && statusid != NOT_POSSIBLE_THIS_WAY) {
					perfectlyCandidate = BookingFinder.getValidFrom(brule, user, tf, requestedCapacityUnits);
					if (perfectlyCandidate.isAvailable()) {
						tbrs = new ArrayList<TimeBookingRequest>();
						tbrs.add(perfectlyCandidate);
						statusid = BOOKING_AVAILABLE;
						TimeFrame now = perfectlyCandidate.getRequestedTimeFrame();
						if (perfectlyCandidate.getRequestedCapacityUnits() != requestedCapacityUnits || !now.toString().equals(tf.toString())) {
							justEqualBookingIsAvailable = true;
						}
					} else {
						tbrs = BookingFinder.getBookingRequestNextTo(perfectlyCandidate);
						if (tbrs == null || tbrs.size() == 0) {
							statusid = BOOKED_UP;
						} else {
							justEqualBookingIsAvailable = true;
							statusid = BOOKING_AVAILABLE;
						}
					}
				} else if (statusid == 0) { // no time frame could be generated
					// from request
					statusid = BOOKED_UP;
				}
			}
			try {
				result.put("statusid", statusid);
				switch (statusid) {
				case NOT_POSSIBLE_THIS_WAY:
					String message = String.format("<div>It's not possible to book \"%s\" this way.</div>", bd.getLabel()); // INTLANG
					result.put("statusmessageShort", message);
					result.put("statusmessage_long", message);
					break;
				case BOOKED_UP:
					result.put("statusmessageShort", "<div>could not find a free slot</div>"); // INTLANG
					result.put("statusmessage_long", "<div>could not find a free slot</div>"); // INTLANG
					break;
				case BOOKING_AVAILABLE: // return all possibilities in an array
					FamShoppingCart sc = user.getShoppingCart();
					JSONArray availableBookings = new JSONArray();
					boolean isFirstIteration = true;
					for (TimeBookingRequest tbr : tbrs) {
						JSONObject tmpresult = new JSONObject();
						Booking booking = TimeBooking.getNewBooking(tbr);
						sc.addArticle(booking);

						// build the form
						QueryString queryString = QueryStringBuilder.getArticleNumber(booking);
						HtmlElement form = HtmlFactory.get_form("post", TemplateHtml.me().getRelativeHref("bookfacilitiesdone"));
						HtmlElement button = FamSubmitButtonFactory.getButton(user.hasRight(FamAuth.DIRECT_BOOKING, tbr.getFacility()) ? "book!" : "apply!"); // INTLANG
						form = FamSubmitButtonFactory.getButtonAsForm(queryString, form, button);

						HtmlElement statusmessageShort = new HtmlElement("div");
						String tmp = "";
						if (RequestInterpreter.isAjaxRequestFromRequestBookingLink(rq) && justEqualBookingIsAvailable) {
							if (isFirstIteration) {
								tmp = "<strong>Not available!</strong><br />Available " + ((tbr.getRequestedCapacityUnits() > 1) ? "are" : "is");// INTLANG
								isFirstIteration = false;
							} else {
								tmp = "or";// INTLANG
							}
							tmp += " " + FamText.facilityNameWithCapacityUnits(booking);
						} else { // its available
							tmp = "available"; // INTLANG
						}
						String infoTimeFromTo = "";
						if (perfectlyCandidate.getRequestedTimeFrame().overlaps(tbr.getRequestedTimeFrame())) {
							infoTimeFromTo = FamDateFormat.getTimeFormatted(tbr.getRequestedTimeFrame(), "this day from %s<br />to %s");
						} else {
							infoTimeFromTo = FamDateFormat.getDateFormattedWithTime(tbr.getRequestedTimeFrame(), true);
						}
						statusmessageShort.add("<p>" + tmp + ":<br />" + infoTimeFromTo + "</p>"); // INTLANG
						statusmessageShort.add(form);
						tmpresult.put("statusmessageShort", statusmessageShort); // INTLANG
						HtmlElement statusmessageLong = new HtmlElement("div");
						statusmessageLong.add("<p>" + tmp + ":</p>" + FamDateFormat.getDateFormattedWithTime(tbr.getRequestedTimeFrame(), true)); // INTLANG
						statusmessageLong.add(form);
						tmpresult.put("statusmessage_long", statusmessageLong); // INTLANG
						Calendar start = tbr.getRequestedTimeFrame().getCalendarStart();
						Calendar end = tbr.getRequestedTimeFrame().getCalendarEnd();
						tmpresult.put("newMinutesY1", start.get(Calendar.HOUR_OF_DAY) * 60 + start.get(Calendar.MINUTE)); // INTLANG
						tmpresult.put("newMinutesY2", end.get(Calendar.HOUR_OF_DAY) * 60 + end.get(Calendar.MINUTE)); // INTLANG
						availableBookings.put(tmpresult);
						if (!RequestInterpreter.isAjaxRequestFromRequestBookingLink(rq)) {
							// on possibility for drag and drop action
							break;
						}
					}
					result.put("availableBookings", availableBookings);
					break;
				}
			} catch (JSONException ex) {
				this.onException(ex);
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		this.onException(ex);
	}

	private void onException(Exception ex) {
		FamLog.logException(this.getClass(), ex, "creating json failed", 201011271618l);
	}
}

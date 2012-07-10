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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.BookingStrategy;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.FamShoppingCart;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.util.booking.BookingFinder;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.heinzelmann.util.nebc.bu.JSONObjectFromRequest;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * controller for ajax request to book a facility on the new calendar (book2).
 * 
 * @author Daniel Oltmanns
 * @since 1.6.0 (09/28/2011)
 */
public class DirectBookingRequest2Controller extends JSONController {
	private final String DATE_FORMAT = "MM/dd/yyyy HH:mm";

	/**
	 * return an available possible booking like requested or alternatives next
	 * to requested booking. format json.
	 * 
	 * @param rq
	 *            request
	 * @param rs
	 *            response
	 * @return possible bookings.
	 */
	@Override
	public JSONObject getJSONObject(HttpServletRequest rq, HttpServletResponse rs) {
		JSONObject result = new JSONObject();
		User authUser = SessionAuth.user(rq);
		if (authUser != null) {
			try {
				result.put("succ", false);
				// find possibilities to book
				JSONObject request = new JSONObjectFromRequest().process(rq);
				int requestedCapacityUnits = Integer.parseInt(request.getJSONObject("request").getString("capacity_units"));
				Date start = new SimpleDateFormat(DATE_FORMAT).parse(request.getJSONObject("request").getString("start"));
				Date end = new SimpleDateFormat(DATE_FORMAT).parse(request.getJSONObject("request").getString("end"));
				TimeFrame requestedTimeFrame = new SimpleTimeFrame(start.getTime(), end.getTime());
				FacilityBookable bd = FacilityConfigDao.bookableFacility(request.getJSONObject("facility").getString("key"));
				List<TimeBookingRequest> possibilities = new ArrayList<TimeBookingRequest>();
				if (bd != null && requestedTimeFrame != null && bd.isBookable() && bd.getBookingStrategy() == BookingStrategy.TIME_BASED) {
					TimeBookingRequest perfectlyCandidate = BookingFinder.getValidFrom(bd.getBookingRule(), authUser, requestedTimeFrame, requestedCapacityUnits);
					if (perfectlyCandidate.isAvailable() && perfectlyCandidate.getRequestedCapacityUnits() == requestedCapacityUnits && perfectlyCandidate.getRequestedTimeFrame().toString().equals(requestedTimeFrame.toString())) {
						// ↖ the exactly time requested is available
						possibilities.add(perfectlyCandidate);
					} else {
						possibilities = BookingFinder.getBookingRequestNextTo(perfectlyCandidate);
						if (possibilities == null || possibilities.size() == 0) {
							possibilities = new ArrayList<TimeBookingRequest>();
						}
					}
				} else {
					result.put("message", "invalid request");
					FamLog.info("invalid request", 201110111401l);
				}
				// convert possibilities to json
				JSONArray possibilities_json = new JSONArray();
				FamShoppingCart sc = authUser.getShoppingCart();
				for (TimeBookingRequest possibility : possibilities) {
					Booking booking = TimeBooking.getNewBooking(possibility);
					sc.addArticle(booking);
					JSONObject pjson = new JSONObject();
					pjson.put("capacity_units", possibility.getRequestedCapacityUnits());
					pjson.put("start", FamDateFormat.getCustomDate(possibility.getRequestedTimeFrame().getDateStart(), DATE_FORMAT));
					pjson.put("end", FamDateFormat.getCustomDate(possibility.getRequestedTimeFrame().getDateEnd(), DATE_FORMAT));
					pjson.put("article_number", booking.getArticleNumber());
					possibilities_json.put(pjson);
				}
				result.put("possibilities", possibilities_json);
				result.put("succ", true);
			} catch (JSONException e) {
				FamLog.info("invalid request", 201110111347l);
			} catch (ParseException e) {
				FamLog.info("invalid request", 201110111355l);
			}
		} else {
			try {
				result.put("succ", false);
				result.put("message", "your session is timed out. please sign in again."); // INTLANG
			} catch (JSONException e) {
				FamLog.info("json error", 201110120950l);
			}

		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		this.onException(ex);
	}
}

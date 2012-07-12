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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.content.text.FamText;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.config.BookingStrategy;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.config.UsersUnitsQueueBasedBookingRule;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.heinzelmann.util.nebc.bu.JSONObjectFromRequest;

/**
 * return details for a given facility.
 * 
 * @author Daniel Oltmanns
 * @since 1.3.1 (04/07/2011)
 * @version 1.3.1 (04/07/2011)
 */
public class GetFacilityDetailsController extends JSONController {

	public GetFacilityDetailsController(User user) {
		this.user = user;
	}

	private User user = null;

	/**
	 * return an available possible booking.
	 * 
	 * there are two different places where this is called: 1. the
	 * "request booking" link in the navigation of the calendar 2. the drag and
	 * drop action in same calender.
	 * 
	 * if the booking strategy is queue based, put the booking directly into
	 * users shopping cart. if it is time based, we do not know the booking at
	 * this point - so do not put it into the shopping cart.
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
		boolean succ = true;

		// get data
		JSONObject data = null;
		try {
			data = new JSONObjectFromRequest().process(rq);
		} catch (Exception e) {
			succ = false;
			FamLog.info("data not found", 201104071157l);
		}

		// get facility from request
		Facility facility = null;
		BookingRule br = null;
		if (succ) {
			try {
				String facilityKey = data.getString("key");
				if (facilityKey != null && FacilityConfigDao.isKey(facilityKey)) {
					facility = FacilityConfigDao.getInstance().getConfiguredInstance(facilityKey);
					if (facility.isBookable()) {
						br = FacilityConfigDao.bookingRule(facility.getKey());
					}
				} else {
					succ = false;
				}
			} catch (JSONException e) {
				succ = false;
				FamLog.info("facility not found", 201104071258l);
			}
		}
		boolean user_is_allowed_to_access = user.hasRight(FamAuth.BOOKING, facility);
		if (succ && user_is_allowed_to_access) { // => d != null
			// set facility details
			try {
				JSONObject jsonBr = new JSONObject();
				JSONObject jsonD = new JSONObject();
				jsonD.put("label", facility.getLabel());
				jsonD.put("key", facility.getKey());
				jsonD.put("bookable", facility.isBookable());
				if (facility.isBookable()) { // => br != null
					jsonBr.put("strategy", br.getBookingStrategy());
					jsonBr.put("capacity_label_singular", FamText.message(String.format("label.capacity.%s.singular", facility.getKey())));
					jsonBr.put("capacity_label_plural", FamText.message(String.format("label.capacity.%s.plural", facility.getKey())));
					jsonBr.put("must_apply", user.hasRight(FamAuth.DIRECT_BOOKING, facility) ? false : true);
					jsonBr.put("max_bookable_capacity_units", br.getMaxBookableCapacityUnits(user));
					jsonBr.put("min_bookable_capacity_units", br.getMinBookableCapacityUnits(user));
					if (br.getBookingStrategy() == BookingStrategy.TIME_BASED) {
						jsonBr.put("max_bookable_time_units", br.getMaxBookableTimeUnits(user));
						jsonBr.put("min_bookable_time_units", br.getMinBookableTimeUnits(user));
						jsonBr.put("smallest_minutes_bookable", br.getSmallestMinutesBookable());
					} else { // queue based
						UsersUnitsQueueBasedBookingRule qbbr = (UsersUnitsQueueBasedBookingRule) br;
						QueueBooking qb = new QueueBooking(user, (FacilityBookable) facility);
						SessionAuth.addToUsersShoppingCart(rq, qb);
						result.put("current_queue_length", qbbr.getCurrentQueueLength());
						result.put("article_number", qb.getArticleNumber());
					}
					result.put("br", jsonBr);
				} else {
					result.put("br", "null");
				}
				result.put("facility", jsonD);
			} catch (Exception e) {
				FamLog.exception(e, 201104121033l);
			}
		}

		// set succ
		try {
			result.put("succ", succ);
			result.put("user_is_allowed_to_access", user_is_allowed_to_access);
		} catch (Exception e) {
			FamLog.exception(e, 201104121033l);
		}

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		FamLog.exception(ex, 201104071259l);
	}
}

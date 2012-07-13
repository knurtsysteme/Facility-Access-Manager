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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.UserDao;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.core.view.adapter.json.JSONAdapterBooking;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * controller for ajax request to get username. print json with the key
 * "username" and unique username as value. request must contain
 * "supposedUsername", created via javascript.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090916 (09/16/2009)
 */
public class GetBookingsController extends JSONController {

	/**
	 * return username as key and unique username as value
	 * 
	 * @see UserDao#getUniqueUsername(de.knurt.fam.core.model.persist.User)
	 * @param rq
	 *            request
	 * @param rs
	 *            response
	 * @return username as key and unique username as value
	 */
	@Override
	public JSONObject getJSONObject(HttpServletRequest rq, HttpServletResponse rs) {
		JSONObject result = new JSONObject();
		User authUser = SessionAuth.user(rq);
		if (authUser != null) {
			FacilityBookable bd = RequestInterpreter.getBookableFacility(rq);
			if (bd != null) {
				TimeFrame tf = null;
				if (true) { // XXX may later be a check of calendar type (week,
					// month, year etc.)
					tf = RequestInterpreter.getTimeFrameOfWeek(rq);
				}
				if (tf != null) {
					List<Booking> bookings = FamDaoProxy.bookingDao().getUncanceledBookingsAndApplicationsIn(bd, tf);
					try {
						result.put("bookings", new JSONAdapterBooking(authUser).getAsJSONArray(bookings));
					} catch (JSONException ex) {
						FamLog.exception(ex, 201204141014l);
					}
				}
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		FamLog.logException(this.getClass(), ex, "creating json fails", 200909160828l);
	}
}

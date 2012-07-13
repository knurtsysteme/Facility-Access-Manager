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
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.view.adapter.json.JSONAdapterBooking;
import de.knurt.heinzelmann.util.nebc.bu.JSONObjectFromRequest;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * control request for booking information of a specific timeframe.
 * 
 * @author Daniel Oltmanns
 * @since 1.6.1 (01/09/2012)
 */
public class GetDetailsOfBookingController extends JSONController {
	private User user = null;

	public GetDetailsOfBookingController(User user) {
		this.user = user;
	}

	private TimeFrame getTimeFrame(JSONObject request) throws ParseException, JSONException {
		final String DATE_FORMAT = "MM/dd/yyyy HH:mm";
		Date start = new SimpleDateFormat(DATE_FORMAT).parse(request.getString("start"));
		Date end = new SimpleDateFormat(DATE_FORMAT).parse(request.getString("end"));
		return new SimpleTimeFrame(start.getTime(), end.getTime());
	}

	/** {@inheritDoc} */
	@Override
	public JSONObject getJSONObject(HttpServletRequest rq, HttpServletResponse rs) {
		JSONObject result = new JSONObject();
		try {
			result.put("succ", false);

			boolean anonymize_personal_info = !this.user.hasRight(FamAuth.VIEW_PERSONAL_INFORMATION, null);
			result.put("anonymize_personal_info", anonymize_personal_info);

			JSONObject request = new JSONObjectFromRequest().process(rq);
			TimeFrame timeFrame = this.getTimeFrame(request);
			FacilityBookable facility = FacilityConfigDao.bookableFacility(request.getJSONObject("facility").getString("key"));
			List<Booking> bookings = FamDaoProxy.bookingDao().getUncanceledBookingsAndApplicationsIn(facility, timeFrame);

			result.put("details", new JSONAdapterBooking(this.user).getAsJSONArray(bookings));

			result.put("succ", true);
		} catch (NullPointerException e) {
			FamLog.exception(e, 201201090919l);
		} catch (NumberFormatException e) {
			FamLog.exception(e, 201201090918l);
		} catch (JSONException e) {
			FamLog.exception(e, 201201090917l);
		} catch (ParseException e) {
			FamLog.exception(e, 201201090936l);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		FamLog.exception(ex, 201201090920l);
	}
}

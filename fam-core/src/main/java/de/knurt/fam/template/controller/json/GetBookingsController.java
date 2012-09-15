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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.core.view.adapter.json.JSONAdapterBooking;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * controller to get bookings as json.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090916 (09/16/2009)
 */
public class GetBookingsController extends JSONController {

	private User authUser;

	public GetBookingsController(User authUser) {
		this.authUser = authUser;
	}

	private enum KnownFlag {
		DEFAULT, ALL_OWN
	};

	/**
	 * return bookings as json depending on flags (
	 * <code>rq.getParameter("flag")</code>).
	 * 
	 * if flag is not set, return uncanceled bookings and applications of the user
	 * auth and of the given timeframe. if timeframe is not given, return
	 * <code>{}</code>.
	 * 
	 * @see RequestInterpreter#getTimeFrame(HttpServletRequest)
	 * @see JSONAdapterBooking#getAsJSONArray(List)
	 */
	@Override
	public JSONObject getJSONObject(HttpServletRequest rq, HttpServletResponse rs) {
		JSONObject result = new JSONObject();
		List<Booking> bookings = new ArrayList<Booking>();
		if (authUser != null) {
			switch (this.getFlag(rq)) {
			case ALL_OWN:
				bookings = this.getBookingsAllOwn(rq);
				break;
			default:
				bookings = this.getBookingsOfTimeFrame(rq);
				break;
			}
			try {
				result.put("bookings", new JSONAdapterBooking(authUser).getAsJSONArray(bookings));
			} catch (JSONException ex) {
				FamLog.exception(ex, 201204141014l);
			}
		}
		return result;
	}

	private List<Booking> getBookingsAllOwn(HttpServletRequest rq) {
		return FamDaoProxy.bookingDao().getAllBookingsOfUser(authUser);
	}

	private List<Booking> getBookingsOfTimeFrame(HttpServletRequest rq) {
		List<Booking> bookings = new ArrayList<Booking>();
		FacilityBookable bd = RequestInterpreter.getBookableFacility(rq);
		if (bd != null) {
			TimeFrame tf = RequestInterpreter.getTimeFrameOfWeek(rq);
			if (tf != null) {
				bookings = FamDaoProxy.bookingDao().getUncanceledBookingsAndApplicationsIn(bd, tf);
			}
		}
		return bookings;
	}

	private KnownFlag getFlag(HttpServletRequest rq) {
		KnownFlag result = KnownFlag.DEFAULT;
		String flag = rq.getParameter("flag");
		if (flag != null) {
			if (flag.equalsIgnoreCase("all-own")) {
				result = KnownFlag.ALL_OWN;
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

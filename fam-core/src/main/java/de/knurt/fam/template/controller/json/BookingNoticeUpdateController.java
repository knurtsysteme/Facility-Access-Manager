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
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.util.mvc.RequestInterpreter;

/**
 * controller for ajax request to update the notice for a booking.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090923 (09/23/2009)
 */
public class BookingNoticeUpdateController extends JSONController {

	/** {@inheritDoc} */
	@Override
	public JSONObject getJSONObject(HttpServletRequest rq, HttpServletResponse rs) {
		JSONObject result = new JSONObject();
		String message = "";
		String newNotice = RequestInterpreter.getNotice(rq);
		if (newNotice != null && !newNotice.isEmpty()) {
			Booking booking = RequestInterpreter.getBooking(rq);
			if (booking != null) {
				booking.setNotice(newNotice);
				booking.update();
				message = "notice has been updated"; // INTLANG
			}
		}
		if (message.isEmpty()) {
			message = "updating notice failed"; // INTLANG
		}
		try {
			result.put("message", message);
		} catch (JSONException ex) {
			FamLog.exception(ex, 201204141015l);
		}
		return result;
	}

	/**
	 * log {@link IOException}
	 * 
	 * @param ex
	 *            thrown
	 */
	@Override
	public void onException(IOException ex) {
		FamLog.logException(this.getClass(), ex, "creating json fails", 200909231630l);
	}
}

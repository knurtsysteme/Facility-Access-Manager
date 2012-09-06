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
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.heinzelmann.util.nebc.bu.JSONObjectFromRequest;

/**
 * end a session controller
 * 
 * @author Daniel Oltmanns
 * @since 1.9.0 (03/09/2012)
 */
public class EndSessionPostController extends JSONController2 {

	private boolean succ = false;
	private String errormessage = null;
	private static final String SUCCMESSAGE = "Thank you for end your current session!"; // INTLANG
	private User user = null;

	public EndSessionPostController(HttpServletRequest request) {
		user = SessionAuth.user(request);
		if (user == null) {
			errormessage = "Your session expired. Please reload page and log in again."; // INTLANG
		} else {
			try {
				JSONObject json = new JSONObjectFromRequest().process(request);
				int id = json.getInt("id");
				this.updateBooking(id);
			} catch (Exception e) {
				// json parse e, npe, nfe ...
				errormessage = "Bad request. Please reload page."; // INTLANG
			}
		}
	}

	private void updateBooking(int id) {
		Booking booking = FamDaoProxy.bookingDao().getBookingWithId(id);
		if (booking.getUser().is(user)) {
			if (booking.isTimeBased()) {
				if (booking.getSessionTimeFrame().getEnd() > new Date().getTime()) {
					booking.getSessionTimeFrame().setEnd(new Date());
					succ = booking.update();
					if (!succ) {
						FamLog.error("could not update booking with id " + id, 201207031231l);
						errormessage = "I/O error!";
					}
				} else {
					// the session already ends up (e.g. user loaded the browser
					// long before submitting the form).
					// do not confuse the user with a message - just say "ok"
					// with not doing anything.
					succ = true;
				}
			} else {
				FamLog.info("url rewrite" + id + user, 201207031226l);
				errormessage = "Bad request. Please reload page."; // INTLANG
			}
		} else {
			FamLog.info("url rewrite" + id + user, 201207031227l);
			errormessage = "You are not the owner of the session."; // INTLANG
		}
	}

	/** {@inheritDoc} */
	@Override
	public JSONObject getJSONObject() {
		JSONObject result = new JSONObject();
		try {
			result.put("succ", succ);
			if (succ) {
				result.put("succmessage", SUCCMESSAGE);
			} else {
				result.put("errormessage", errormessage);
			}
		} catch (JSONException e) {
			FamLog.exception(e, 201209031219l);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void onIOException(IOException ex) {
		FamLog.exception(ex, 201209031218l);
	}

}

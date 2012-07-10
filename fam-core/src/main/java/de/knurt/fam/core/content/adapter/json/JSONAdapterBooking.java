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
package de.knurt.fam.core.content.adapter.json;

import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.content.text.FamText;
import de.knurt.fam.core.model.persist.ContactDetail;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.template.util.HtmlAdapterAddress;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.util.adapter.JSONAdapter;
import de.knurt.heinzelmann.util.adapter.StringAdapter;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * adapter to adapt a booking into a json format. this is used via ajax at e.g.
 * calendar.js, to resolve interactive booking requests.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090916 (09/16/2009)
 */
@SuppressWarnings("deprecation") // TODO #361 kill uses of deprecations
public class JSONAdapterBooking implements StringAdapter<Booking>, JSONAdapter<Booking> {

	private User authUser;

	/**
	 * an adapter for bookings for the auth user
	 * 
	 * @param authUser
	 *            the auth user
	 * @see SessionAuth#user(javax.servlet.http.HttpServletRequest)
	 */
	public JSONAdapterBooking(User authUser) {
		this.authUser = authUser;
	}

	/**
	 * return the json object as a string.
	 * 
	 * @see #getAsJSONObject(de.knurt.fam.core.model.persist.booking.Booking)
	 * @param booking
	 * @return the json object as a string.
	 */
	public String getAsString(Booking booking) {
		return this.getAsJSONObject(booking).toString();
	}

	/**
	 * return an array of given bookings with subentry array of all days. key of
	 * subentry is day of year of the start time of the booking. this means, the
	 * method DOES NOT ALLOW bookings of differnt years! <br />
	 * here is an example of what to give back: <code>
     * {"258":[{"username":"daoltma1"},{"username":"daoltma1"},{"username":"daoltma1"},{"username":"daoltma1"},{"username":"daoltma1"},{"username":"daoltma1"},{"username":"daoltma1"},{"username":"daoltma1"},{"username":"daoltma1"},{"username":"daoltma1"},{"username":"daoltma1"},{"username":"daoltma1"},{"username":"daoltma1"},{"username":"daoltma1"}]},{"257":[{"username":"daoltma1"},{"username":"daoltma1"},{"username":"daoltma1"}]},{"261":[{"username":"daoltman"}]}
     * </code>
	 * 
	 * @see Booking#getSessionTimeFrame()
	 * @see #getAsString(de.knurt.fam.core.model.persist.booking.Booking)
	 * @param bookings
	 * @return an array of given bookings with subentry array of all days.
	 */
	public String getAsString(List<Booking> bookings) {
		return this.getAsJSONArray(bookings).toString();
	}

	private Integer getDayOfYear(TimeFrame timeFrame) {
		return timeFrame.getCalendarStart().get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * return an array of given bookings with subentry array of all days. key of
	 * subentry is day of year of the start time of the booking. this means, the
	 * method DOES NOT ALLOW bookings of differnt years! <br />
	 * here is an example of what to give back: <code>
     * {"bookings":[{"id":261,"username":"daoltman"},{"id":257,"username":"daoltma1"},{"id":257,"username":"daoltma1"},{"id":257,"username":"daoltma1"},{"id":258,"username":"daoltma1"},{"id":258,"username":"daoltma1"},{"id":258,"username":"daoltma1"},{"id":258,"username":"daoltma1"},{"id":258,"username":"daoltma1"},{"id":258,"username":"daoltma1"},{"id":258,"username":"daoltma1"},{"id":258,"username":"daoltma1"},{"id":258,"username":"daoltma1"},{"id":258,"username":"daoltma1"},{"id":258,"username":"daoltma1"},{"id":258,"username":"daoltma1"},{"id":258,"username":"daoltma1"},{"id":258,"username":"daoltma1"}]}
     * </code>
	 * 
	 * @see Booking#getSessionTimeFrame()
	 * @see #getAsString(de.knurt.fam.core.model.persist.booking.Booking)
	 * @param bookings
	 * @return an array of given bookings with subentry array of all days.
	 */
	public JSONArray getAsJSONArray(List<Booking> bookings) {
		JSONArray result = new JSONArray();
		for (Booking booking : bookings) {
			result.put(this.getAsJSONObject(booking));
		}
		return result;
	}

	/**
	 * return the given booking as a json object. the json object has attributes
	 * like
	 * <ul>
	 * <li>id</li>
	 * <li>dayOfYear</li>
	 * <li>username</li>
	 * <li>status</li>
	 * <li>statustext</li>
	 * <li>fullname</li>
	 * <li>... and many more ...</li>
	 * </ul>
	 * 
	 * @param booking
	 *            being adapted to a json object
	 * @return the booking adapted to a json object.
	 */
	public JSONObject getAsJSONObject(Booking booking) {
		JSONObject json = new JSONObject();
		try {
			TimeFrame sessionTimeFrame = booking.getSessionTimeFrame();
			User booker = booking.getUser();
			json.put("id", booking.getId());
			json.put("dayOfYear", this.getDayOfYear(sessionTimeFrame));
			json.put("username", booking.getUsername());
			json.put("statustext", FamText.statusOfBookingAsText(this.authUser, booking));
			json.put("fullname", booker.getFullName());
			json.put("department", booker.getDepartmentLabel());
			Calendar tmp = sessionTimeFrame.getCalendarStart();
			json.put("start_minutes", tmp.get(Calendar.MINUTE) + (tmp.get(Calendar.HOUR_OF_DAY) * 60));
			tmp = sessionTimeFrame.getCalendarEnd();
			json.put("end_minutes", tmp.get(Calendar.MINUTE) + (tmp.get(Calendar.HOUR_OF_DAY) * 60));
			json.put("start_time", FamDateFormat.getTimeFormatted(sessionTimeFrame.getDateStart()));
			json.put("end_time", FamDateFormat.getTimeFormatted(sessionTimeFrame.getDateEnd()));
			json.put("start_date_and_time", FamDateFormat.getDateFormattedWithTime(sessionTimeFrame.getDateStart()));
			json.put("end_date_and_time", FamDateFormat.getDateFormattedWithTime(sessionTimeFrame.getDateEnd()));
			json.put("start_date_and_time_short", FamDateFormat.getDateAndTimeShort(sessionTimeFrame.getDateStart()));
			json.put("end_date_and_time_short", FamDateFormat.getDateAndTimeShort(sessionTimeFrame.getDateEnd()));
			if (this.authUser.hasRight(FamAuth.VIEW_PERSONAL_INFORMATION, null)) {
				json.put("phone1", booker.getPhone1());
				json.put("phone2", booker.getPhone2());
				json.put("mail", booker.getMail());
				json.put("main_address", new JSONAdapterAddress().getAsJSONObject(booker.getMainAddress()));
				HtmlElement address = new HtmlAdapterAddress(booker.getMainAddress()).getFullAsHtml();
				json.put("address", address == null ? "" : address.toString());
				JSONArray cds = new JSONArray();
				for (ContactDetail cd : booker.getContactDetails()) {
					JSONObject cdjson = new JSONObject();
					cdjson.put("title", cd.getTitle());
					cdjson.put("detail", cd.getDetail());
					cds.put(cdjson);
				}
				json.put("cds", cds);
			} else {
				json.put("phone1", "");
				json.put("phone2", "");
				json.put("mail", "");
				json.put("address", "");
				json.put("cds", new JSONArray());
			}
			String notice = booking.getNotice();
			json.put("sessionAlreadyBegun", booking.sessionAlreadyBegun());
			json.put("notice", notice == null ? "" : notice);
			json.put("capacityUnits", FamText.facilityNameWithCapacityUnits(booking));
		} catch (JSONException ex) {
		}
		return json;
	}
}

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
package de.knurt.fam.core.content.adapter.html;

import java.util.ArrayList;
import java.util.List;

import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.config.style.FamColors;
import de.knurt.fam.core.content.html.factory.FamFormFactory;
import de.knurt.fam.core.content.html.factory.FamSubmitButtonFactory;
import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.content.text.FamText;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.util.booking.ApplicationConflicts;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.heinzelmann.ui.CssStyle;
import de.knurt.heinzelmann.ui.CssStyleFactory;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * adapt bookings. the Facility Access Manager knows different types of booking
 * booked in different strategies (like {@link QueueBooking} or
 * {@link TimeBooking}. this is the super class for all adapter that apapts one
 * kind of these bookings.
 * 
 * @param <E>
 *            a {@link Booking} being adapted.
 * @author Daniel Oltmanns
 * @since 0.20090929 (09/29/2009)
 */
@Deprecated
public abstract class HtmlAdapterAbstractBooking<E extends Booking> extends FamHtmlAdapter<Booking> implements HtmlAdapterBooking {

	private User currentUser;
	private Booking booking;

	/**
	 * constructer for all adapters of bookings
	 * 
	 * @param current
	 *            user logged in
	 * @param booking
	 *            being adapted
	 */
	protected HtmlAdapterAbstractBooking(User current, Booking booking) {
		super(booking);
		this.currentUser = current;
		this.booking = booking;
	}

	/**
	 * if the session of booking is startable and stopable, return form.
	 * 
	 * @return return form if the session of booking is startable and stopable
	 */
	public HtmlElement getProcessedForm(String formAction) {
		QueryString hiddenInputs = QueryStringBuilder.getQueryString(this.booking);
		QueryStringBuilder.add(hiddenInputs, this.booking.getFacility());
		QueryStringBuilder.addSentFlags(hiddenInputs);
		String content = "Processed"; // INTLANG
		HtmlElement button = FamSubmitButtonFactory.getButton(content);
		return FamSubmitButtonFactory.getButtonAsForm(hiddenInputs, HtmlFactory.get_form("post", formAction), button);
	}

	/**
	 * return a red or green field with information about the conflicting
	 * bookings.
	 * 
	 * @return a red or green field with information about the conflicting
	 *         bookings.
	 */
	public HtmlElement getConflicts() {
		HtmlElement result = HtmlFactory.get("div");
		CssStyleFactory styleFactory = CssStyleFactory.getInstance();
		CssStyle style = styleFactory.get();
		style.add("width", "100%");
		style.add("text-align", "center");
		style.add("font-weight", "bolder");
		style.add("padding", "3px");
		style.add("color", "white");
		List<Booking> conflictingBookings = new ArrayList<Booking>();
		if (this.booking.isTimeBased()) { // booking is time based
			conflictingBookings = ApplicationConflicts.getInstance().getConflicts((TimeBooking) this.booking);
		}
		if (conflictingBookings.size() > 0) {
			style.add("background-color", styleFactory.getColor(FamColors.FULL));
			result.add(conflictingBookings.size());
		} else {
			style.add("background-color", styleFactory.getColor(FamColors.FREE));
			result.add("no"); // INTLANG
		}
		result.setCssStyle(style);
		return result;
	}

	/**
	 * return a short summary of most important things of a booking. this simply
	 * is "Mr. Daniel Oltmanns booked 4 Holiday-Weekends"
	 * 
	 * @return a short summary of a booking.
	 */
	public HtmlElement getShortSummary() {
		return HtmlFactory.get("div").add(this.getFullUserAsHtmlWithoutUsername()).add(" booked<br />").add(this.getCapacityUnitsAsText()); // INTLANG
	}

	/**
	 * return true, if the session for the booking came already into being.
	 * 
	 * @return true, if the session for the booking came already into being.
	 */
	protected abstract boolean isOldBooking();

	/**
	 * return a delete button. bookings can be deleted in different situations.
	 * return a form with nothing else but a delete button in that case. if a
	 * deletion is not possible (because the booking is already deleted or the
	 * session for the booking is already made), return a message without a
	 * form.
	 * 
	 * @return return a delete button.
	 */
	public String getDeleteButton() {
		String result = "";
		if (this.disableDelete()) {
			if (this.isOldBooking()) {
				result = "<p>Old bookings cannot be canceled</p>"; // INTLANG
			} else if (booking.isCanceled()) {
				result = "<p>Booking is canceled already</p>"; // INTLANG
			}
		} else {
			HtmlElement button = FamFormFactory.getDeleteButtonAsForm(this.getBaseQueryString(), 25, this.disableDelete());
			result = button.toString();
		}
		return result;
	}

	/**
	 * return true, if the booking cannot be deleted by logged in user.
	 * 
	 * @return true, if the booking cannot be deleted by logged in user.
	 */
	protected abstract boolean disableDelete();

	private QueryString getBaseQueryString() {
		QueryString qs = new QueryString();
		qs.put(QueryKeys.QUERY_KEY_BOOKING, booking.getId());
		return qs;
	}

	/**
	 * return the capacityUnits as text
	 * 
	 * @return the capacityUnits as text
	 */

	public String getCapacityUnitsAsText() {
		return FamText.facilityNameWithCapacityUnits(this.booking);
	}

	/**
	 * return the label of the booked facility.
	 * 
	 * @return the label of the booked facility.
	 */

	public String getFacilityLabel() {
		return FacilityConfigDao.getInstance().getLabel(booking.getFacilityKey());
	}

	/**
	 * return the date the booking has been set on looking nicely.
	 * 
	 * @return the date the booking has been set on looking nicely.
	 */

	public String getSeton() {
		return FamDateFormat.getDateFormattedWithTime(booking.getSeton());
	}

	/**
	 * return the username of the user made the booking request.
	 * 
	 * @return the username of the user made the booking request.
	 */

	public String getUsername() {
		return booking.getUsername();
	}

	/**
	 * return the current user
	 * 
	 * @see SessionAuth#user(javax.servlet.http.HttpServletRequest)
	 * @return the current user
	 */
	public User getCurrentUser() {
		return currentUser;
	}

	/**
	 * return the id of the booking. center it.
	 * 
	 * @return the id of the booking.
	 */

	public String getId() {
		return this.centerIt(booking.getId() + "");
	}

	/**
	 * return the job id of the booking. center it.
	 * 
	 * @return the job id of the booking.
	 */
	public String getJobId() {
		return this.getId();
	}

	/**
	 * return the full name of the user and the username in brackets. set the
	 * entire string into a mailto anchor.
	 * 
	 * @return the full name of the user and the username in brackets.
	 */

	public HtmlElement getFullUserAsHtmlWithUsername() {
		// SMELLS this is the user being adapted, not the booking
		User user = booking.getUser();
		if (user != null) {
			String recipient = String.format("%s (%s)", user.getFullName(), user.getUsername());
			return HtmlFactory.get_a_mailto(user.getMail(), recipient);
		} else {
			return HtmlFactory.get("div").add(booking.getUsername());
		}
	}

	/**
	 * return the full name of the user. set the entire string into a mailto
	 * anchor.
	 * 
	 * @return the full name of the user.
	 */

	public HtmlElement getFullUserAsHtmlWithoutUsername() {
		// SMELLS because this is the user being adapted, not the booking
		User user = booking.getUser();
		if (user != null) {
			return HtmlFactory.get_a_mailto(user.getMail(), user.getFullName());
		} else {
			return HtmlFactory.get("div", "unknown user"); // INTLANG
		}
	}

}

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
package de.knurt.fam.core.view.adapter.html;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.core.view.text.FamDateFormat;
import de.knurt.fam.core.view.text.FamText;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * adapt bookings for facility, you have to queue for, as it is nice looking in
 * html.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090710 (07/10/2009)
 */
@Deprecated
public class HtmlAdapterQueueBooking extends HtmlAdapterAbstractBooking<QueueBooking> {

	private QueueBooking booking;

	/**
	 * construct me
	 * 
	 * @param current
	 *            user being authenticated
	 * @param booking
	 *            object being adapted
	 */
	public HtmlAdapterQueueBooking(User current, QueueBooking booking) {
		super(current, booking);
		this.booking = booking;
	}

	/**
	 * return the position in a queue.
	 * 
	 * @return the position in a queue
	 */
	public HtmlElement getPositionInQueue() {
		return HtmlFactory.get("div").add(this.booking.getCurrentQueuePosition() + 1);
	}

	/**
	 * @return the timeframe
	 */
	public String getTimeframe() {
		String result = "";
		String resultformat = "<span style=\"display:none;\">%s</span>%s";
		if (this.booking.isCanceled()) {
			result = String.format(resultformat, 99999999999999l, "Session has been canceled"); // INTLANG
		} else if (this.booking.getCurrentQueuePosition() != null && this.booking.getCurrentQueuePosition() == 0) {
			result = String.format(resultformat, 00000000000001l, "<strong>comes next</strong>"); // INTLANG
		} else if (this.booking.sessionAlreadyBegun() && !this.booking.sessionAlreadyMade()) {
			result = String.format(resultformat, 00000000000000l, "Session is now"); // INTLANG
		} else {
			if (this.booking.sessionAlreadyMade()) {
				String message = "Session was:<br />" + FamDateFormat.getDateFormattedWithTime(this.booking.getSessionTimeFrame(), true); // INTLANG
				String sortation = sortationdate.format(this.booking.getSessionTimeFrame().getDateStart());
				result = String.format(resultformat, sortation, message);
			} else {
				String message = "Session is expected to be:<br />" + FamDateFormat.getDateFormattedWithTime(this.booking.getExpectedSessionTimeFrame(), true); // INTLANG
				String sortation = sortationdate.format(this.booking.getExpectedSessionTimeFrame().getDateStart());
				result = String.format(resultformat, sortation, message);
			}
		}
		return result;
	}
	
	private DateFormat sortationdate = new SimpleDateFormat("yyyyMMddHHmmss");

	/**
	 * return the status of booking. if the booking is not canceled, return
	 * "comes next" or the position in the queue as well.
	 * 
	 * @see FamText#statusOfBookingAsImg(de.knurt.fam.core.model.persist.User,
	 *      de.knurt.fam.core.model.persist.booking.Booking)
	 * @return the status of booking.
	 */

	public String getBookingStatus() {
		String result = FamText.statusOfBookingAsText(this.getCurrentUser(), booking);
		if (!this.booking.isCanceled() && !this.booking.sessionAlreadyMade()) {
			Integer pos = this.booking.getCurrentQueuePosition();
			if (pos == 0 || pos == 1) {
				result = String.format("<p>%s</p><p><strong>comes next</strong></p>", result); // INTLANG
			} else {
				result = String.format("<p>%s</p><p>Position <strong>%s</strong></p>", result, pos); // INTLANG
			}
		}
		return this.centerIt(result);
	}

	@Override
	protected boolean disableDelete() {
		return booking.isCanceled() || booking.sessionAlreadyBegun();
	}

	/**
	 * return the time of the time frame with a info prefix. if session is
	 * running return "now", if session is already made, return the session time
	 * frame. otherwise return the expected time frame.
	 * 
	 * @return the time of the time frame with a info prefix.
	 */

	public String getTimeframetime() {
		String result = "";
		if (this.booking.sessionAlreadyBegun() && !this.booking.sessionAlreadyMade()) {
			result = "Session is now"; // INTLANG
		} else {
			TimeFrame tf;
			if (this.booking.sessionAlreadyMade()) {
				result = "Session was:<br />"; // INTLANG
				tf = this.booking.getSessionTimeFrame();
			} else {
				result = "Session is expected to be:<br />"; // INTLANG
				tf = this.booking.getExpectedSessionTimeFrame();
			}
			result += FamDateFormat.getTimeFormatted(tf);
		}
		return result;
	}

	@Override
	protected boolean isOldBooking() {
		return this.booking.sessionAlreadyBegun();
	}
}

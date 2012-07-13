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

import java.util.Calendar;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.view.text.FamDateFormat;
import de.knurt.fam.core.view.text.FamText;
import de.knurt.fam.template.util.HtmlTableSortationUtil;

/**
 * adapt bookings for a specific time for nice looking html.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090710 (07/10/2009)
 */
@Deprecated
public class HtmlAdapterTimeBooking extends HtmlAdapterAbstractBooking<TimeBooking> {

	private TimeBooking booking;

	/**
	 * construct me
	 * 
	 * @param current
	 *            user being authenticated
	 * @param booking
	 *            being adapted
	 */
	public HtmlAdapterTimeBooking(User current, TimeBooking booking) {
		super(current, booking);
		this.booking = booking;
	}

	/**
	 * @return the timeframe
	 */
	public String getTimeframe() {
		return HtmlTableSortationUtil.me().span(booking.getDateStart()).toString() + FamDateFormat.getDateFormattedWithTime(booking, true);
	}

	/**
	 * return the status of booking.
	 * 
	 * @see FamText#statusOfBookingAsImg(de.knurt.fam.core.model.persist.User,
	 *      de.knurt.fam.core.model.persist.booking.Booking)
	 * @return the status of booking.
	 */
	public String getBookingStatus() {
		return this.centerIt(FamText.statusOfBookingAsText(this.getCurrentUser(), booking));
	}

	@Override
	protected boolean disableDelete() {
		return booking.isCanceled() || booking.endsInPast();
	}

	/**
	 * return only the time of the booking if the booking is on the same day.
	 * otherwise return the full date.
	 * 
	 * @return only the time of the booking if the booking is on the same day.
	 *         otherwise return the full date.
	 */
	public String getTimeframetime() {
		String suffix = "";
		if (booking.getCalendarStart().get(Calendar.DAY_OF_YEAR) == booking.getCalendarEnd().get(Calendar.DAY_OF_YEAR)) {
			suffix = FamDateFormat.getTimeFormatted(booking);
		} else {
			suffix = this.getTimeframe();
		}
		return HtmlTableSortationUtil.me().span(booking.getDateStart()) + suffix;
	}

	@Override
	protected boolean isOldBooking() {
		return this.booking.startsInPast();
	}
}

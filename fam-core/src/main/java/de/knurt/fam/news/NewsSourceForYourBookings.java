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
package de.knurt.fam.news;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.core.util.time.CalendarUtil;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * report news about new users
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.5.0 (07/29/2011)
 * 
 */
public class NewsSourceForYourBookings implements NewsSource {

	/** {@inheritDoc} */
	@Override
	public List<NewsItem> getNews(TimeFrame from, User to) {
		List<NewsItem> result = new ArrayList<NewsItem>();
		result.addAll(this.getNewsOfYourBookings(from, to));
		return result;
	}

	private List<NewsItem> getNewsOfYourBookings(TimeFrame from, User to) {
		List<NewsItem> result = new ArrayList<NewsItem>();
		List<Booking> bookings = to.getBookings();
		for (Booking booking : bookings) {
			NewsItem candidate = this.getNewsItemOf(booking, from);
			if (candidate != null) {
				result.add(candidate);
			}
		}
		return result;
	}

	private NewsItem getNewsItemOf(Booking booking, TimeFrame from) {
		NewsItem result = null;
		if ((booking.isCanceled() && from.contains(booking.getCancelation().getDateCanceled())) || (!booking.isCanceled() && !booking.sessionAlreadyMade())) {
			result = new NewsItemDefault();
			String facilityLabel = booking.getFacility().getLabel();
			String bOrA = booking.isApplication() ? "application" : "booking"; // INTLANG
			String desc = "";
			if (booking.isCanceled()) {
				String reason = booking.getCancelation().getReason();
				desc = String.format("Your %s for %s was canceled.", bOrA, facilityLabel); // INTLANG
				if (reason != null && !reason.isEmpty()) {
					desc += " Reason: " + reason;
				}
				result.setEventStarts(booking.getCancelation().getDateCanceled());
			} else { // uncanceled booking
				if (booking.isQueueBased() && !booking.sessionAlreadyMade()) {
					// ↖ booking in queue and not made
					desc = String.format("Expected start of your session on %s. Your position in queue: %s.", facilityLabel, ((QueueBooking) booking).getActualQueuePosition()); // INTLANG
					result.setEventStarts(((QueueBooking) booking).getExpectedSessionStart().getTime());
					if (booking.sessionAlreadyBegun()) {
						result.setLinkToFurtherInformation(TemplateHtml.href("viewrequest") + QueryStringBuilder.getQueryString(booking));
					} else {
						result.setLinkToFurtherInformation(TemplateHtml.href("editrequest") + QueryStringBuilder.getQueryString(booking));
					}
				} else { // booking has session start time
					Calendar today = Calendar.getInstance();
					if (DateUtils.isSameDay(today.getTime(), booking.getSessionTimeFrame().getDateStart())) {
						// session is today
						if (DateUtils.isSameDay(today.getTime(), booking.getSessionTimeFrame().getDateEnd())) {
							desc = String.format("Your session on %s.", facilityLabel); // INTLANG
							result.setEventStarts(booking.getSessionTimeFrame().getDateStart());
							result.setEventEnds(booking.getSessionTimeFrame().getDateEnd());
						} else {
							desc = String.format("Your session on %s (ends on %s).", facilityLabel, FamDateFormat.getDateAndTimeShort(booking.getSessionTimeFrame().getDateEnd())); // INTLANG
							result.setEventStarts(booking.getSessionTimeFrame().getDateStart());
						}
						if (booking.sessionAlreadyBegun()) {
							result.setLinkToFurtherInformation(TemplateHtml.href("viewrequest") + QueryStringBuilder.getQueryString(booking));
						} else {
							result.setLinkToFurtherInformation(TemplateHtml.href("editrequest") + QueryStringBuilder.getQueryString(booking));
						}
					} else { // session starts in future
						Calendar booking_c = booking.getSessionTimeFrame().getCalendarStart();
						if (booking_c.before(today)) {
							desc = String.format("your session on %s.", facilityLabel); // INTLANG
						} else {
							long days = CalendarUtil.me().daysBetween(today, booking_c);
							desc = String.format("%s %s to your session on %s.", days, days == 1 ? "day" : "days", facilityLabel); // INTLANG
						}
						result.setEventStarts(booking_c.getTime());
						if (booking.getSessionTimeFrame().getCalendarEnd() != null) {
							result.setEventEnds(booking.getSessionTimeFrame().getCalendarEnd().getTime());
						}
						result.setLinkToFurtherInformation(TemplateHtml.href("editrequest") + QueryStringBuilder.getQueryString(booking));
					}
				}
				if (booking.getNotice() != null && !booking.getNotice().isEmpty()) {
					desc += " Notice: " + booking.getNotice();
				}
			}
			if (result != null && desc != null) {
				result.setDescription(desc);
			}
		}
		return result;
	}

}
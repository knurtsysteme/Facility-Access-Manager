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
package de.knurt.fam.template.model;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import de.knurt.fam.core.content.adapter.html.HtmlAdapterBookingFactory;
import de.knurt.fam.core.content.adapter.html.HtmlAdapterFacilityAvailabilityFactory;
import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.util.mvc.RequestInterpreter;

/**
 * controller for overview of all bookings made
 * 
 * @author Daniel Oltmanns
 * @since 0.20090710 (07/10/2009)
 */
@SuppressWarnings("deprecation") // TODO #361 kill uses of deprecations
public class EventsOfDayModelFactory {

	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		Calendar c = RequestInterpreter.getCalendar(templateResource.getRequest());
		FacilityBookable bdGot = RequestInterpreter.getBookableFacility(templateResource.getRequest());
		if (bdGot != null && FacilityConfigDao.isKey(bdGot.getKey())) {
			List<Booking> bookingsToShow = FamDaoProxy.bookingDao().getAllUncanceledBookingsAndApplicationsOfDay(bdGot, c);
			List<FacilityAvailability> facilityAvailabilitiesToShow = FamDaoProxy.facilityDao().getFacilityAvailabilitiesOfDay(c, bdGot);
			Collections.sort(facilityAvailabilitiesToShow);
			User authUser = templateResource.getAuthUser();
			int bookingssize = bookingsToShow.size();
			int availssize = facilityAvailabilitiesToShow.size();
			int eventssize = bookingssize + availssize;
			result.put("eventssize", eventssize);
			result.put("availssize", availssize);
			result.put("bookingssize", bookingssize);

			String eventssizetext = "";
			if (eventssize == 0) {
				eventssizetext = "No events"; // INTLANG
			} else if (eventssize == 1) {
				eventssizetext = "1 event"; // INTLANG
			} else {
				eventssizetext = eventssizetext + " events"; // INTLANG
			}
			result.put("eventssizetext", eventssizetext);

			String bookingstext = "";
			if (bookingssize == 0) {
				bookingstext = "No bookings"; // INTLANG
			} else if (bookingssize == 1) {
				bookingstext = "1 booking"; // INTLANG
			} else {
				bookingstext = bookingssize + " bookings"; // INTLANG
			}
			result.put("bookingstext", bookingstext);

			String availstext = "";
			if (availssize == 0) {
				availstext = "No rest times"; // INTLANG
			} else if (availssize == 1) {
				availstext = "1 rest time entry"; // INTLANG
			} else {
				availstext = availssize + " rest time entries"; // INTLANG
			}
			result.put("availstext", availstext);

			result.put("bookings", new HtmlAdapterBookingFactory().getInstances(authUser, bookingsToShow));
			result.put("availabilities", new HtmlAdapterFacilityAvailabilityFactory().getInstances(authUser, facilityAvailabilitiesToShow));

			result.put("day", FamDateFormat.getDateFormatted(c.getTime()));
			result.put("facility", FacilityConfigDao.label(bdGot));
		}
		return result;
	}

}

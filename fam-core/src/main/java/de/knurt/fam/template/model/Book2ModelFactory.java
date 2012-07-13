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

import java.util.List;
import java.util.Properties;

import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.view.html.FacilityOverviewHtml;
import de.knurt.fam.template.util.TemplateHtml;

/**
 * produce model for book2.html
 * 
 * @see SimpleSession
 * @author Daniel Oltmanns
 * @since 1..0 (01/19/2012)
 */
public class Book2ModelFactory {

	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		String rootKey = FacilityConfigDao.getInstance().getRootKey();
		String baseUrl = TemplateHtml.href("book2");
		result.put("facilitieslist", FacilityOverviewHtml.getTree(FacilityConfigDao.facility(rootKey), baseUrl, TemplateModelFactory.getFacilitiesUserCanBook(templateResource.getAuthUser())));
		result.put("jsonfacilities", TemplateModelFactory.getJSONFacilities(rootKey));
		result.put("jsonvar", String.format("var FacilityOverviewTreeUrlBase = '%s'", baseUrl));
		result.put("first_time_booking", this.isFirstTimeBooking(templateResource));
		return result;
	}

	private boolean isFirstTimeBooking(TemplateResource templateResource) {
		boolean result = true;
		List<Booking> bookings = templateResource.getAuthUser().getBookings();
		for (Booking booking : bookings) {
			if (booking.isTimeBased()) {
				result = false;
				break;
			}
		}
		return result;
	}

}

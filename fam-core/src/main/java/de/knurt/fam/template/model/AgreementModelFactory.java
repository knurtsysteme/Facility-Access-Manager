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

import java.util.Properties;

import de.knurt.fam.core.control.boardunits.JobSurveyFromJobs;
import de.knurt.fam.core.control.persistence.dao.couchdb.CouchDBDao4Jobs;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.heinzelmann.util.text.DurationAdapter;
import de.knurt.heinzelmann.util.text.DurationAdapter.SupportedLanguage;

/**
 * model for agreements of a specific order.
 * 
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/27/2010)
 */
public class AgreementModelFactory {

	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		Booking booking = RequestInterpreter.getBooking(templateResource.getRequest());
		if (booking != null && booking.getUsername().equals(templateResource.getAuthUser().getUsername())) {
			result.put("booking", booking);
			result.put("job", new JobSurveyFromJobs().process(CouchDBDao4Jobs.me().getJobs(booking)));
			String ms = booking.isTimeBased() ? ((TimeBooking) booking).getDuration() + "" : "";
			SupportedLanguage lang = null;
			if (templateResource.getName().equals("agreementde")) {
				lang = SupportedLanguage.GERMAN;
			} else {
				lang = SupportedLanguage.ENGLISH;
			}
			result.put("booking_length_of_time", this.getBookingLengthOfTime(ms, lang));
		}
		return result;
	}

	private String getBookingLengthOfTime(String ms, SupportedLanguage lang) {
		String result = "";
		if (ms != null && !ms.equals("")) {
			try {
				int minutes = (int) (Long.parseLong(ms) / 1000 / 60);
				DurationAdapter da = new DurationAdapter(lang);

				da.setLanguage(lang);
				result = da.getText(minutes);
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}
}

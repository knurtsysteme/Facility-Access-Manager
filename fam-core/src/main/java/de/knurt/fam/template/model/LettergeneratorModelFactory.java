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

import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.util.bu.PdfLetterFromBooking;
import de.knurt.fam.core.util.mvc.RequestInterpreter;

/**
 * produce a letter with {@link PdfLetterFromBooking}
 * 
 * @author Daniel Oltmanns
 * @since 1.4.0 (06/12/2011)
 */
public class LettergeneratorModelFactory {

	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		Booking booking = RequestInterpreter.getBooking(templateResource.getRequest());
		if (booking != null) {
			result.put("booking", booking);
			JSONObject tmp = new PdfLetterFromBooking(templateResource.getAuthUser()).process(booking);
			String[] keys = JSONObject.getNames(tmp).clone();
			for (String key : keys) {
				try {
					result.put(key, tmp.get(key));
				} catch (JSONException e) {
					FamLog.exception("key: " + key, e, 201106121717l);
				}
			}
		}
		return result;
	}

}

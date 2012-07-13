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

import de.knurt.fam.connector.RedirectTarget;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.util.mvc.RedirectResolver;

/**
 * produce the model to show the "we got your booking" page. redirect to home if
 * this is a invalid request. it is an invalid request if no jobId is given or
 * the jobId is invalid (no booking found with this jobId or it's not the user's
 * job).
 * 
 * @author Daniel Oltmanns
 * @since 1.7.0 (03/08/2012)
 */
public class BookFacilitiesDoneModelFactory {
	private Booking booking = null;

	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		if (this.isValidRequest(templateResource)) {
			result.put("booking", booking);
		} else {
			RedirectResolver.redirectClient(RedirectTarget.PROTECTED_HOME, templateResource);
		}
		return result;
	}

	private boolean isValidRequest(TemplateResource templateResource) {
		boolean result = templateResource.hasAuthUser();
		if (result) {
			try {
				int jobId = Integer.parseInt(templateResource.getRequest().getParameter("jobId"));
				booking = FamDaoProxy.bookingDao().getBookingWithId(jobId);
				result = booking != null && booking.getUsername().equals(templateResource.getAuthUser().getUsername());
			} catch (Exception e) {
				result = false;
			}
		}
		return result;
	}

}

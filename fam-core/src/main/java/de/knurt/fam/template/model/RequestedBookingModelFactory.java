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
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.util.mvc.RedirectResolver;
import de.knurt.fam.core.util.mvc.RequestInterpreter;

/**
 * create the booking requested and set it as <code>booking</code>. set
 * <code>user_owned_booking</code> to <code>true</code> if the auth user is the
 * owner of the booking. set <code>user_owned_booking</code> to
 * <code>false</code> if the auth user is not the owner of the booking but is
 * the operator of the facility booked. redirect to protected home if user is
 * neither first nor second.
 * 
 * @see RequestInterpreter#getBooking(javax.servlet.http.HttpServletRequest)
 * @see User#hasResponsibility4Facility(de.knurt.fam.core.model.config.Facility)
 * @see Booking#getFacility()
 * @author Daniel Oltmanns
 * @since 1.7.0 (03/30/2012)
 */
public class RequestedBookingModelFactory {

	/**
	 * return the requested booking as model
	 * @see RequestedBookingModelFactory
	 * @param templateResource the {@link TemplateResource} of the request
	 * @return the requested booking as model
	 */
	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		Booking booking = RequestInterpreter.getBooking(templateResource.getRequest());
		if (booking != null && templateResource.getAuthUser() != null) {
			Boolean userOwnedBooking = null;
			if (booking.getUser().is(templateResource.getAuthUser())) {
				userOwnedBooking = true;
			} else if (templateResource.getAuthUser().hasResponsibility4Facility(booking.getFacility())) {
				userOwnedBooking = false;
			}
			if (userOwnedBooking == null) {
				// user is not allowed to see session
				RedirectResolver.redirectClient(RedirectTarget.PROTECTED_HOME, templateResource);
				FamLog.info("user " + templateResource.getAuthUser().getUsername() + " not allowed to see " + booking.getArticleNumber() + " on " + templateResource.getName() + " (url rewrite?!)", 201203300856l);
			} else {
				result.put("booking", booking);
				result.put("user_owned_booking", userOwnedBooking);
			}
		} else {
			RedirectResolver.redirectClient(RedirectTarget.PROTECTED_HOME, templateResource);
			FamLog.info("no facility given on session info (url rewrite?!)", 201010271215l);
		}
		return result;
	}
}

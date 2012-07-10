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
package de.knurt.fam.template.controller.image.availability;

import java.util.List;

import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.fam.core.util.mvc.RequestInterpreter;

/**
 * controller for ajax request to get username. print json with the key
 * "username" and unique username as value. request must contain
 * "supposedUsername", created via javascript.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090311 (03/11/2009)
 */
public class OneMonthDayBookingsImageController extends OneMonthDayImageController {

	/** {@inheritDoc} */
	@Override
	public List<FacilityAvailability> getFacilityAvailabilitiesToShow() {
		TimeBookingRequest br = RequestInterpreter.getBookingWishFromRequest(this.getRequest(), SessionAuth.user(this.getRequest()));
		// request only capacity units on ajax flag
		Integer requestedCapacityUnits = null;
		if (RequestInterpreter.hasAjaxFlag(this.getRequest())) {
			requestedCapacityUnits = br.getRequestedCapacityUnits();
		}
		return new FacilityAvailability4ImagesGetter(br, requestedCapacityUnits).getAvailabilitiesWithBookingSituation();
	}
}

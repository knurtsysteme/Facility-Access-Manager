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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.core.model.persist.FacilityAvailability;

/**
 * control generating of a legend explaining the availability of facilities. the
 * result is an image, that presents a specific availability. every availability
 * must contain one of the following a keywords:
 * <ul>
 * <li>booked</li>
 * <li>generalnot</li>
 * <li>maintenance</li>
 * <li>failure</li>
 * <li>cannotstarthere</li>
 * </ul>
 * 
 * @see FacilityAvailability
 * @author Daniel Oltmanns
 * @since 0.20090807 (08/07/2009)
 */
public class AvailabilityLegendImageController extends OneMonthDayImageController {

	private FacilityAvailability da;

	/** {@inheritDoc} */
	@Override
	protected int getImageHeight() {
		return 10;
	}

	/** {@inheritDoc} */
	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String s = request.getParameter("status");

		Calendar start = Calendar.getInstance();
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);

		Calendar end = (Calendar) start.clone();
		end.add(Calendar.DAY_OF_YEAR, 1);

		this.da = new FacilityAvailability("not important", start, end);
		this.da.setAvailable(FacilityAvailability.COMPLETE_AVAILABLE);

		if (s.equals("booked")) {
			this.da.setNotAvailableBecauseOfBooking();
		} else if (s.equals("applied")) {
			this.da.setMaybeAvailable();
		} else if (s.equals("generalnot")) {
			this.da.setNotAvailableInGeneral();
		} else if (s.equals("maintenance")) {
			this.da.setNotAvailableBecauseOfMaintenance();
		} else if (s.equals("failure")) {
			this.da.setNotAvailableBecauseOfSuddenFailure();
		} else if (s.equals("mustnotstarthere")) {
			this.da.setBookingMustNotStartHere();
		}
		return super.handleRequest(request, response);
	}

	/** {@inheritDoc} */
	@Override
	protected int getImageWidth() {
		return 50;
	}

	/** {@inheritDoc} */
	@Override
	public List<FacilityAvailability> getFacilityAvailabilitiesToShow() {
		List<FacilityAvailability> das = new ArrayList<FacilityAvailability>();
		das.add(this.da);
		return das;
	}
}

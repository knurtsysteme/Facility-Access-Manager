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

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.config.style.FamColors;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.fam.core.util.graphics.Images;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.template.controller.image.availability.printer.FacilityAvailabilityPrinter;
import de.knurt.fam.template.controller.image.availability.printer.PrintableBar;

/**
 * controller for ajax request to get username. print json with the key
 * "username" and unique username as value. request must contain
 * "supposedUsername", created via javascript.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090311 (03/11/2009)
 */
public class OneMonthDayImageController extends AbstractDasFacilityAvailabilityImageController implements PrintableBar, FacilityAvailabilityImageController {

	/** {@inheritDoc} */
	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return super.handleRequest(request, response);
	}

	/**
	 * return the image height in pixel.
	 * 
	 * @return the image height in pixel.
	 */
	@Override
	protected int getImageWidth() {
		return 150;
	}

	/** {@inheritDoc} */
	@Override
	protected int getImageHeight() {
		return Images.ONE_MONTH_DAY_IMAGE_HEIGHT;
	}

	/** {@inheritDoc} */
	@Override
	protected void createImage(Graphics2D g2d) {
		FacilityAvailability inda = RequestInterpreter.getFacilityAvailabilityOfConfiguredDayForDisplaying(this.getRequest());
		FacilityAvailabilityPrinter printer = new FacilityAvailabilityPrinter(this.getFacilityAvailabilitiesToShow(), this.getFacilityAvailabilityArtist(inda.getBasePeriodOfTime(), this, g2d), inda.getBasePeriodOfTime());
		printer.out();
	}

	/** {@inheritDoc} */
	@Override
	protected Color getBackgroundColor() {
		return FamColors.CAL_BG;
	}

	/** {@inheritDoc} */
	@Override
	public int getBarX() {
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public int getBarY() {
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public int getBarWidth() {
		return this.getImageWidth();
	}

	/** {@inheritDoc} */
	@Override
	public int getBarHeight() {
		return this.getImageHeight();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isHorizontal() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public List<FacilityAvailability> getFacilityAvailabilitiesToShow() {
		TimeBookingRequest br = RequestInterpreter.getBookingWishFromRequest(this.getRequest(), SessionAuth.user(this.getRequest()));
		Integer requestedCapacityUnits = br.getFacility().getCapacityUnits();
		// requesting all, because want red on only one booking
		return new FacilityAvailability4ImagesGetter(br, requestedCapacityUnits).getAvailabilitiesWithBookingSituation();
	}
}
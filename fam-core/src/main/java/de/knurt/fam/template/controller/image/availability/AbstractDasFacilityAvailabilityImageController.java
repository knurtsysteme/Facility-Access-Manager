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

import java.awt.Graphics2D;

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.template.controller.image.availability.printer.FacilityAvailabilityArtist;
import de.knurt.fam.template.controller.image.availability.printer.FullSizeColoredFacilityAvailabilityArtist;
import de.knurt.fam.template.controller.image.availability.printer.PrintableBar;
import de.knurt.fam.template.controller.image.availability.printer.SmallBarFacilityAvailabilityArtist;
import de.knurt.fam.template.controller.image.statistics.SimplePngImageController;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * control creating an image of availabilities in a time frame with a printable
 * bar and an artist.
 * 
 * @see PrintableBar
 * @see TimeFrame
 * @see FacilityAvailabilityArtist
 * @see FacilityAvailability
 * @author Daniel Oltmanns
 * @since 0.20090807 (08/07/2009)
 */
abstract class AbstractDasFacilityAvailabilityImageController extends SimplePngImageController {

	/**
	 * construct it with given time frame, bar and graphics
	 * 
	 * @param timeFrame
	 *            shown in the image
	 * @param bar
	 *            used to create the image
	 * @param g2d
	 *            used to create the image
	 * @return it with given time frame, bar and graphics
	 */
	protected FacilityAvailabilityArtist getFacilityAvailabilityArtist(TimeFrame timeFrame, PrintableBar bar, Graphics2D g2d) {
		FacilityAvailabilityArtist daa;
		if (this.getArtistKey() == 1) {
			daa = new SmallBarFacilityAvailabilityArtist(timeFrame, bar, g2d);
		} else {
			daa = new FullSizeColoredFacilityAvailabilityArtist(timeFrame, bar, g2d);
		}
		return daa;
	}

	private int artistKey = 0;

	/**
	 * set the key for the used artist to print the image.
	 * 
	 * @param artistKey
	 *            key for the used artist to print the image.
	 * @see FacilityAvailabilityArtist
	 */
	@Required
	public void setArtistKey(int artistKey) {
		this.artistKey = artistKey;
	}

	/**
	 * return the key for the used artist to print the image.
	 * 
	 * @return the key for the used artist to print the image.
	 * @see FacilityAvailabilityArtist
	 */
	protected int getArtistKey() {
		return this.artistKey;
	}
}

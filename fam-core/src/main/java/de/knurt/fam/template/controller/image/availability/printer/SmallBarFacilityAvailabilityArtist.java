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
package de.knurt.fam.template.controller.image.availability.printer;

import java.awt.Graphics2D;

import de.knurt.fam.core.config.style.FamColors;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * class to decide, how to print out a small bar showing the availability.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090423 (04/23/2009)
 */
public class SmallBarFacilityAvailabilityArtist implements FacilityAvailabilityArtist {

	private FacilityAvailabilityPrintingMetrics metrics = null;

	/**
	 * construct artist
	 * 
	 * @param timeFrame
	 *            time frame of shown time in picture
	 * @param printableBar
	 *            used to draw the picture
	 * @param g2d
	 *            used to draw the picture
	 */
	public SmallBarFacilityAvailabilityArtist(TimeFrame timeFrame, PrintableBar printableBar, Graphics2D g2d) {
		metrics = new FacilityAvailabilityPrintingMetrics(timeFrame, printableBar, g2d);
	}

	public void outNotAvailableBooking(FacilityAvailability da) {
		metrics.getG2d().setColor(FamColors.FULL);
		this.out(da);
	}

	private void out(FacilityAvailability da) {
		int x = (int) metrics.getX(da);
		int y = (int) metrics.getY(da);
		int width = metrics.getWidth(da);
		int height = metrics.getHeight(da);
		metrics.getG2d().fillRect(x, y, width, height);
	}

	public void outMaybeAvailable(FacilityAvailability da) {
		metrics.getG2d().setColor(FamColors.PARTLY);
		this.out(da);

		// print line through
		metrics.getG2d().setColor(FamColors.CAL_BG);
		this.printLineThrough(2, da);
	}

	public void outNotAvailableInGeneral(FacilityAvailability da) {
		metrics.getG2d().setColor(FamColors.CAL_BG);
		this.out(da);
		metrics.getG2d().setColor(FamColors.FULL);
		this.printLineThrough(2, da);
	}

	public void outNotAvailableMaintenance(FacilityAvailability da) {
		metrics.getG2d().setColor(FamColors.CAL_LINE_BG_DARK);
		this.out(da);
		metrics.getG2d().setColor(FamColors.FULL);
		this.printLineThrough(2, da);
	}

	public void outNotAvailableFailure(FacilityAvailability da) {
		metrics.getG2d().setColor(FamColors.FULL);
		this.out(da);
		metrics.getG2d().setColor(FamColors.CAL_LINE_BG_DARK);
		this.printLineThrough(Math.min(metrics.getHeight(da), metrics.getWidth(da)) - 2, da);
		metrics.getG2d().setColor(FamColors.FULL);
		this.printLineThrough(2, da);
	}

	public void outCompletelyAvailable(FacilityAvailability da) {
		metrics.getG2d().setColor(FamColors.FREE);
		this.out(da);

		// print line through
		metrics.getG2d().setColor(FamColors.CAL_BG);
		this.printLineThrough(4, da);
	}

	private void printLineThrough(int strength, FacilityAvailability da) {
		int x = (int) metrics.getX(da);
		int y = (int) metrics.getY(da);
		int width = metrics.getWidth(da);
		int height = metrics.getHeight(da);
		if (metrics.getPrintableBar().isHorizontal()) {
			y = metrics.getPrintableBar().getBarHeight() / 2 - (strength / 2);
			height = strength;
		} else { // vertical
			x = metrics.getPrintableBar().getBarWidth() / 2 - (strength / 2);
			width = strength;
		}

		metrics.getG2d().fillRect(x, y, width, height);
	}

	public void outMustNotStartHere(FacilityAvailability da) {
		metrics.getG2d().setColor(FamColors.MUST_NOT_START_HERE);
		this.out(da);

		// print line through
		metrics.getG2d().setColor(FamColors.CAL_BG);
		this.printLineThrough(4, da);
	}
}

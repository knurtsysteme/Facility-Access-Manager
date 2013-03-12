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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import de.knurt.fam.core.config.style.FamColors;
import de.knurt.fam.core.config.style.FamFonts;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.heinzelmann.util.TextImprinting;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * artist printing the availability of facilities as a main small bar and a
 * transparent striped larger panel right of it.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090423 (04/23/2009)
 */
public class FullSizeColoredFacilityAvailabilityArtist implements FacilityAvailabilityArtist {

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
	public FullSizeColoredFacilityAvailabilityArtist(TimeFrame timeFrame, PrintableBar printableBar, Graphics2D g2d) {
		metrics = new FacilityAvailabilityPrintingMetrics(timeFrame, printableBar, g2d);
	}

	private void out(FacilityAvailability da) {
		int x = (int) metrics.getX(da);
		int y = (int) metrics.getY(da);
		int leftWidth = metrics.getWidth(da);
		int height = metrics.getHeight(da);
		metrics.getG2d().fillRect(x, y, leftWidth, height);
		float opacity = 0.5f;
		metrics.getG2d().setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
		int rightWidth = 200;
		metrics.getG2d().fillRect(x, y, rightWidth, height);
		metrics.getG2d().setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		this.labelIt(da, leftWidth + 2, y, 100, height);
	}

	@Override
  public void outNotAvailableBooking(FacilityAvailability da) {
		metrics.getG2d().setColor(FamColors.FULL);
		this.out(da);
	}

	@Override
  public void outMaybeAvailable(FacilityAvailability da) {
		metrics.getG2d().setColor(FamColors.PARTLY);
		this.out(da);
	}

	@Override
  public void outNotAvailableInGeneral(FacilityAvailability da) {
		metrics.getG2d().setColor(FamColors.CAL_LINE_BG_BRIGHT);
		this.out(da);
	}

	@Override
  public void outNotAvailableMaintenance(FacilityAvailability da) {
		metrics.getG2d().setColor(FamColors.CAL_LINE_BG_DARK);
		this.out(da);
	}

	@Override
  public void outNotAvailableFailure(FacilityAvailability da) {
		metrics.getG2d().setColor(Color.BLUE);
		this.out(da);
	}

	@Override
  public void outCompletelyAvailable(FacilityAvailability da) {
		metrics.getG2d().setColor(FamColors.FREE);
		this.out(da);
	}

	private void labelIt(FacilityAvailability da, int x, int y, int width, int height) {
		String notice = da.getNotice();
		this.metrics.getG2d().setColor(FamColors.FONT);
		TextImprinting.getInstance().imprint(FamFonts.getTextSplitterOnWidth(width).split(notice), this.metrics.getG2d(), x, y, height, FamFonts.getLineHeight());
	}

	@Override
  public void outMustNotStartHere(FacilityAvailability da) {
		metrics.getG2d().setColor(FamColors.MUST_NOT_START_HERE);
		this.out(da);
	}
}

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

import java.awt.Font;
import java.awt.Graphics2D;

import de.knurt.fam.core.config.style.FamFonts;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * the availability can be shown in horizontal and vertical bars.
 * this is the class containing the main metrics and printing tools.
 * assumes a picture that shows a time line and it is printed from time start to time end.
 * the direction of printing goes always the long side, which means print from top to bottom on vertical images and
 * from left to right on horizontal images.
 * on all availabllities it is assumed, that it is <strong>not</strong> iterated,
 * means {@link FacilityAvailability#isOneTime()} must be <code>true</code> on all given availablities.
 * @author Daniel Oltmanns
 */
public class FacilityAvailabilityPrintingMetrics {

    private TimeFrame fromTo;
    private Graphics2D g2d;
    private PrintableBar printableBar;

    /**
     * construct the metrics for given time frame, printable bar and graphic.
     * @param fromTo time frame shown in the image
     * @param printableBar used to show the image
     * @param g2d used to create the image
     */
    public FacilityAvailabilityPrintingMetrics(TimeFrame fromTo, PrintableBar printableBar, Graphics2D g2d) {
        this.printableBar = printableBar;
        this.g2d = g2d;
        Font f = FamFonts.getFont();
        g2d.setFont(f);
        this.fromTo = fromTo;
    }

    /**
     * return the height of the availability.
     * 5 px at minimum.
     * @param da to get the height from.
     *  {@link FacilityAvailability#isOneTime()} must be <code>true</code>
     * @return height of the facility availability to print
     */
    protected int getHeight(FacilityAvailability da) {
        int result = this.getPrintableBar().getBarHeight();
        if (!this.printableBar.isHorizontal()) {
            result *= this.getPercentageDuration(da);
        }
        result = result < 5 ? 5 : result;
        return result;
    }

    /**
     * return the width of the availability.
     * 5 px at minimum.
     * @param da to get the height from.
     *  {@link FacilityAvailability#isOneTime()} must be <code>true</code>
     * @return width of the facility availability to print
     */
    protected int getWidth(FacilityAvailability da) {
        int result = this.getPrintableBar().getBarWidth();
        if (this.printableBar.isHorizontal()) {
            result *= this.getPercentageDuration(da);
        }
        result = result < 5 ? 5 : result;
        return result;
    }

    /**
     * return the x position of the image in pixel.
     * if the image is vertical, simply use the x position as defined in the
     * printable bar. if the image is horizontal, compute position from percentage
     * position of given availability and the width of the bar.
     * @see PrintableBar#getBarX()
     * @see PrintableBar#getBarWidth()
     * @see #getPercentagePos(de.knurt.fam.core.model.persist.FacilityAvailability)
     * @param da the start and end time is used to find the x position
     *  in case of a horizontal bar.
     *  {@link FacilityAvailability#isOneTime()} must be <code>true</code>
     * @return the x position of the image in pixel.
     */
    protected double getX(FacilityAvailability da) {
        double result = this.getPrintableBar().getBarX();
        if (this.printableBar.isHorizontal()) {
            result = this.getPrintableBar().getBarWidth() * this.getPercentagePos(da);
        }
        return result;
    }

    /**
     * return the y position of the image in pixel.
     * if the image is horizontal, simply use the y position as defined in the
     * printable bar. if the image is vertical, compute position from percentage
     * position of given availability and the height of the entire bar.
     * @see PrintableBar#getBarY()
     * @see PrintableBar#getBarHeight()
     * @see #getPercentagePos(de.knurt.fam.core.model.persist.FacilityAvailability)
     * @param da the start and end time is used to find the y position
     *  in case of a vertical bar.
     *  {@link FacilityAvailability#isOneTime()} must be <code>true</code>
     * @return the y position of the image in pixel.
     */
    protected double getY(FacilityAvailability da) {
        double result = this.getPrintableBar().getBarY();
        if (!this.printableBar.isHorizontal()) {
            result = this.getPrintableBar().getBarHeight() * this.getPercentagePos(da);
        }
        return result;
    }

    /**
     * return the percentage duration of the given availability in relation to
     * the duration shown in the image.
     * this is the same as the percentage length in pixel in relation to
     * the length of the longer side of the image in pixel.
     * @param da the duration of the base period of time is taken from.
     *  {@link FacilityAvailability#isOneTime()} must be <code>true</code>
     * @return the percentage duration of the given availability
     */
    protected double getPercentageDuration(FacilityAvailability da) {
        return da.getBasePeriodOfTime().getDuration() / (double) this.getFromTo().getDuration();
    }

    /**
     * return the percentage position of the start of the given availability
     * in relation to the longest side of the image.
     * @param da the position, where it has to start is returned of in percent.
     *  {@link FacilityAvailability#isOneTime()} must be <code>true</code>
     * @return the percentage position of the start of the given availability.
     */
    protected double getPercentagePos(FacilityAvailability da) {
        double fromToLength = this.getFromTo().getDuration();
        double fromToStart2daStart = da.getBasePeriodOfTime().getCalendarStart().getTimeInMillis() - this.getFromTo().getStart();
        return fromToStart2daStart / fromToLength;
    }

    /**
     * return the time frame the image shows
     * @return the time frame the image shows
     */
    protected TimeFrame getFromTo() {
        return fromTo;
    }

    /**
     * return the graphics used to print the image
     * @return the graphics used to print the image
     */
    protected Graphics2D getG2d() {
        return g2d;
    }

    /**
     * return the printable bar used to print the image
     * @return the printable bar used to print the image
     */
    protected PrintableBar getPrintableBar() {
        return printableBar;
    }
}

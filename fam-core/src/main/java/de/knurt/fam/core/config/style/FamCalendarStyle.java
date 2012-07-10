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
package de.knurt.fam.core.config.style;

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.config.FamCalendarConfiguration;

/**
 * the style of the calendar in terms of background images used.
 * the style is the heigt, units, the lines etc.
 * @author Daniel Oltmanns
 * @since 0.20090516 (05/16/2009)
 */
public class FamCalendarStyle {

    private int calendarHeight;
    private int oneUnitHeight;
    private int pixelPerHour;
    /** one and only instance of me */
    private volatile static FamCalendarStyle me;

    /** construct me */
    private FamCalendarStyle() {
    }

    /**
     * return the one and only instance of FamCalendarStyle
     * @return the one and only instance of FamCalendarStyle
     */
    public static FamCalendarStyle getInstance() {
        if (me == null) { // no instance so far
            synchronized (FamCalendarStyle.class) {
                if (me == null) { // still no instance so far
                    me = new FamCalendarStyle(); // the one and only
                }
            }
        }
        return me;
    }

    /**
     * return the height of the calendar in pixel.
     * the height is defined by {@link FamCalendarConfiguration}.
     * @see FamCalendarConfiguration
     * @return the height of the calendar in pixel.
     */
    public static int calendarHeight() {
        return getInstance().getCalendarHeight();
    }

    /**
     * return true, if the given pixel per hour is exactly one full hour.
     * @param pixelPos to check
     * @return true, if the given pixel per hour is exactly one full hour.
     */
    public static boolean isFullHour(int pixelPos) {
        return (pixelPos % pixelPerHour()) == 0;
    }

    private int getCalendarHeight() {
        return this.calendarHeight;
    }

    /**
     * return height of one unit in the calendar view.
     * one unit is calculated from {@link FamCalendarStyle#getPixelPerHour()} and {@link FamCalendarConfiguration#getSmallestMinuteStep()}
     * @return height of one unit in the calendar view.
     */
    public static int oneUnitHeight() {
        return getInstance().getOneUnitHeight();
    }

    /**
     * @return the oneUnitHeight
     */
    private int getOneUnitHeight() {
        return oneUnitHeight;
    }

    /**
     * @return the pixelPerHour
     */
    private int getPixelPerHour() {
        return pixelPerHour;
    }

    /**
     * return the configured pixel per hour.
     * this is injected and describes heights of one hour in the calendar.
     * @return the configured pixel per hour.
     */
    public static int pixelPerHour() {
        return getInstance().getPixelPerHour();
    }

    /**
     * set the pixel per hour and calculate and set all other attributes here.
     * @param pixelPerHour the pixelPerHour to set
     */
    @Required
    public void setPixelPerHour(int pixelPerHour) {
        this.pixelPerHour = pixelPerHour;
        this.calendarHeight = (FamCalendarConfiguration.hourStop() - FamCalendarConfiguration.hourStart()) * pixelPerHour;
        this.oneUnitHeight = pixelPerHour * FamCalendarConfiguration.smallestMinuteStep() / 60;
    }
}

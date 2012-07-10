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

import java.awt.Color;

/**
 * container for colors used by the system
 * @author Daniel Oltmanns
 * @since 0.20090423 (04/23/2009)
 */
public class FamColors {

    /**
     * green for totaly free things
     */
    public static final Color FREE = new Color(50, 200, 50);
    /**
     * color for "must not start here"
     */
    public static final Color MUST_NOT_START_HERE = new Color(200, 255, 50);
    /**
     * color for "must not start here" in bright
     */
    public static Color MUST_NOT_START_HERE_BRIGHT = new Color(220, 255, 200);
    /**
     * color for a free something
     */
    public static final Color FREE_BRIGHT = new Color(215, 245, 215);
    /**
     * gray color for the system in bright
     */
    public static final Color GRAY_BRIGHT = new Color(225, 225, 225);
    /**
     * gray color for the system in brighter
     */
    public static final Color GRAY_BRIGHTER = new Color(240, 240, 240);
    /**
     * if something is partly, means neither complete nor nothing, this is the color for it in bright
     */
    public static final Color PARTLY_BRIGHT = new Color(255, 235, 215);
    /**
     * if something is full, this is the color for it in bright.
     */
    public static final Color FULL_BRIGHT = new Color(250, 205, 215);
    /**
     * if something is partly, means neither complete nor nothing, this is the color for it
     */
    public static final Color PARTLY = new Color(250, 150, 50);
    /**
     * dark red for things that are not available anymore
     */
    public static final Color FULL = new Color(250, 0, 50);
    /**
     * font color in general.
     */
    public static final Color FONT = Color.BLACK;
    /**
     * background color of the calendar.
     */
    public static final Color CAL_BG = Color.WHITE;
    /**
     * background color of the calendar's line in dark.
     * used for a full hour.
     */
    public static final Color CAL_LINE_BG_DARK = new Color(190, 190, 190);
    /**
     * background color of the calendar's line in bright.
     * used for a non full hour.
     */
    public static final Color CAL_LINE_BG_BRIGHT = new Color(225, 225, 225);
    /**
     * blue color in the system
     */
    public static final Color BLUE = new Color(0, 0, 255);
    /**
     * blue color in bright in the system
     */
    public static final Color BLUE_BRIGHT = new Color(215, 215, 255);
    /**
     * color for dark lines in the caledar
     */
    public static final Color GRAY = CAL_LINE_BG_DARK;
    /**
     * coporate identity color red
     */
    public static final Color CI_RED = new Color(155, 15, 15);
    private static final Color[] colorRevolver = {
        new Color(0, 0, 150),
        new Color(200, 100, 0),
        new Color(0, 128, 0),
        new Color(192, 0, 0),
        new Color(150, 150, 0),
        new Color(128, 0, 128),
        new Color(0, 0, 0),
        new Color(211, 159, 159),
        new Color(0, 250, 250),
        new Color(130, 130, 130),
        new Color(0, 100, 200),
        new Color(30, 100, 0)
    };
    private static int colorRevolverPointer = 0;

    /**
     * return some colors in a loop.
     * this is useful for e.g. showing statistics.
     * @return some colors in a loop.
     */
    public static final Color getNext() {
        colorRevolverPointer++;
        if (colorRevolver.length <= colorRevolverPointer) {
            colorRevolverPointer = 0;
        }
        return colorRevolver[colorRevolverPointer];
    }

    private FamColors() {
    }
}

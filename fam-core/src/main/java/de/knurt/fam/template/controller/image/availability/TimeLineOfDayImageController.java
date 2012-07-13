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

import static de.knurt.fam.core.config.FamCalendarConfiguration.hourStart;
import static de.knurt.fam.core.config.FamCalendarConfiguration.hourStop;
import static de.knurt.fam.core.config.style.FamCalendarStyle.pixelPerHour;

import java.awt.Font;
import java.awt.Graphics2D;

import de.knurt.fam.core.config.style.FamColors;
import de.knurt.fam.core.config.style.FamFonts;
import de.knurt.fam.core.view.text.FamDateFormat;

/**
 * image for label the vertical col with a time line.
 * @author Daniel Oltmanns
 * @since 0.20090516 (05/16/2009)
 */
public class TimeLineOfDayImageController extends TimeStripedDayAbstractImageController {

    @Override
    protected int getImageWidth() {
        return 40;
    }

    /**
     * label the vertical col with a time line.
     * @param g2d use this as your pencil
     */
    @Override
    protected void createImage(Graphics2D g2d) {
        super.createImage(g2d);
        int hour = hourStart() + 1;
        int offset = 5;
        int y = offset + pixelPerHour();
        Font f = FamFonts.getFont();
        g2d.setFont(f);
        while (hour < hourStop()) {

            // draw gray line white
            g2d.setColor(FamColors.CAL_BG);
            g2d.drawLine(0, y - offset, this.getImageWidth() / 2, y - offset);

            // draw hour (in 0-24 h format)
            g2d.setColor(FamColors.FONT);
            g2d.drawString(FamDateFormat.getShortTimeFormatted(hour) + "", 0, y);

            // increment
            hour++;
            y += pixelPerHour();
        }
    }
}

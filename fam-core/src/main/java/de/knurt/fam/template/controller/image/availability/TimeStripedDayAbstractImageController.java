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

import static de.knurt.fam.core.config.style.FamCalendarStyle.calendarHeight;
import static de.knurt.fam.core.config.style.FamCalendarStyle.isFullHour;
import static de.knurt.fam.core.config.style.FamCalendarStyle.oneUnitHeight;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.core.config.FamCalendarConfiguration;
import de.knurt.fam.core.config.style.FamCalendarStyle;
import de.knurt.fam.core.config.style.FamColors;

/**
 * control to create a image from configured start of day to configured end of day.
 * use the configured style.
 * @see FamCalendarStyle
 * @see FamCalendarConfiguration
 * @author Daniel Oltmanns
 * @since 0.20090516 (05/16/2009)
 */
public abstract class TimeStripedDayAbstractImageController extends AbstractDasFacilityAvailabilityImageController {

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return super.handleRequest(request, response);
    }

    @Override
    protected Color getBackgroundColor() {
        return FamColors.CAL_BG;
    }

    @Override
    protected int getImageHeight() {
        return calendarHeight();
    }

    @Override
    protected void createImage(Graphics2D g2d) {
        int i = oneUnitHeight();
        while (i < this.getImageHeight()) {
            if (isFullHour(i)) {
                g2d.setColor(FamColors.CAL_LINE_BG_DARK);
            } else {
                g2d.setColor(FamColors.CAL_LINE_BG_BRIGHT);
            }
            g2d.drawLine(0, i, this.getImageWidth(), i);
            i += oneUnitHeight();
        }
    }
}

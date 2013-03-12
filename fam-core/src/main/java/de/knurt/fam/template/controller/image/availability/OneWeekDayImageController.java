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
import java.util.List;

import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.template.controller.image.availability.printer.FacilityAvailabilityPrinter;
import de.knurt.fam.template.controller.image.availability.printer.PrintableBar;

/**
 * controller for creation of an image showing one day in a week.
 * in the Facility Access Manager, it is the background image of the table showing the week.
 * @author Daniel Oltmanns
 * @since 0.20090516 (05/16/2009)
 */
public class OneWeekDayImageController extends TimeStripedDayAbstractImageController implements PrintableBar, FacilityAvailabilityImageController {

    @Override
    public boolean isHorizontal() {
        return false;
    }

    @Override
    protected int getImageWidth() {
        return 200;
    }

    @Override
    protected void createImage(Graphics2D g2d) {
        super.createImage(g2d);
        FacilityAvailability inda = RequestInterpreter.getFacilityAvailabilityOfConfiguredDayForDisplaying(this.getRequest());
        FacilityAvailabilityPrinter printer = new FacilityAvailabilityPrinter(this.getFacilityAvailabilitiesToShow(), this.getFacilityAvailabilityArtist(inda.getBasePeriodOfTime(), this, g2d), inda.getBasePeriodOfTime());
        printer.out();
    }

    @Override
    public int getBarX() {
        return 0;
    }

    @Override
    public int getBarY() {
        return 0;
    }

    @Override
    public int getBarWidth() {
        return 10;
    }

    @Override
    public int getBarHeight() {
        return this.getImageHeight();
    }

    @Override
    public List<FacilityAvailability> getFacilityAvailabilitiesToShow() {
        TimeBookingRequest br = RequestInterpreter.getBookingWishFromRequest(this.getRequest(), SessionAuth.user(this.getRequest()));
        Integer requestedCapacityUnits = br.getFacility().getCapacityUnits(); // requesting all, because want red on only one booking
        return new FacilityAvailability4ImagesGetter(br, requestedCapacityUnits).getAvailabilitiesWithBookingSituation();
    }
}

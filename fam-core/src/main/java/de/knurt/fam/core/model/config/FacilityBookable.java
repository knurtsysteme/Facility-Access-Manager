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
package de.knurt.fam.core.model.config;

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.content.text.FamText;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;

/**
 * a facility in general.
 * this uses a properties file to get all the message values like description and
 * label etc. this is resolved via {@link FamText} and {@link FacilityConfigDao} and
 * has nothing to do with this class!
 * @author Daniel Oltmanns
 * @since 0.20090303
 */
public class FacilityBookable extends Facility {

    private BookingRule bookingRule;
    private int capacityUnits = 1;

    /**
     * return default booking rule to use for booking this facility.
     * @return the bookingRule default booking rule to use for booking this facility.
     */
    public BookingRule getBookingRule() {
        return bookingRule;
    }

    /**
     * set the default booking rule that has to be used when requesting this facility.
     * @param bookingRule the default booking rule that has to be used when requesting this facility.
     */
    @Required
    public void setBookingRule(BookingRule bookingRule) {
        this.bookingRule = bookingRule;
        this.bookingRule.setFacility(this);
    }

    /**
     * return total capacity units of this facility.
     * @return the total capacity units of this facility.
     */
    public int getCapacityUnits() {
        return capacityUnits;
    }

    /**
     * set total capacity units of this facility.
     * @param capacityUnits the total capacity units of this facility.
     */
    public void setCapacityUnits(int capacityUnits) {
        this.capacityUnits = capacityUnits;
    }

    /**
     * return the strategy, this facility must be booked with.
     * @see BookingStrategy
     * @return the strategy, this facility must be booked with.
     */
    public int getBookingStrategy() {
        return this.getBookingRule().getBookingStrategy();
    }
}


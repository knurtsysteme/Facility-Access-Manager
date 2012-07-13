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
package de.knurt.fam.core.view.text;

import java.util.Date;
import java.util.Properties;

import de.knurt.fam.core.model.persist.Address;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.view.adapter.plaintext.PlaintextAdapterAddress;

/**
 * adapts pojos to {@link Properties} for use with
 * {@link TemplateQueryStringFactory} classes
 * 
 * @author Daniel Oltmanns
 * @since 2.00-SNAPSHOT (07/22/2010)
 */
public class QueryStringAdapter4Templates {

    /**
     * add information of booking and session into the given properties as they are used
     * by the templates.
     *
     * use following keys for simple values: user_username, user_mainaddress, user_fname,
     * user_sname, user_mail, user_male, user_phone1, user_phone2, user_title,
     * booking_jobid, facility_key, booking_capacityunits, booking_seton
     *
     * for the session values add key to format <code>session_answer_%s_%s</code> where
     * the first replacement is the number of the question (beginning with 0) and the
     * second replacement are the given answers to the question (beginning with 0).
     *
     * the values are served as plain text! by now, there is no way to change
     * this behaviour!
     *
     * if a value is <code>null</code>, an empty string is inserted.
     *
     * @param booking
     *            used for getting the values
     * @param properties
     *            values are put in
     */
    public static void add(Booking booking, Properties properties) {
        assert (booking != null);
        User user = booking.getUser();
        assert (user != null);
        properties.put("user_username", getNoNullValue(user.getUsername()));
        Address address = user.getMainAddress();
        properties.put("user_mainaddress", address == null ? "" : new PlaintextAdapterAddress(address).getInOneRow());
        properties.put("user_fname", getNoNullValue(user.getFname()));
        properties.put("user_sname", getNoNullValue(user.getSname()));
        properties.put("user_intendedResearch", getNoNullValue(user.getIntendedResearch()));
        properties.put("user_mail", getNoNullValue(user.getMail()));
        properties.put("user_company", getNoNullValue(user.getCompany()));
        properties.put("user_male", getNoNullValue(user.getMale()));
        properties.put("user_phone1", getNoNullValue(user.getPhone1()));
        properties.put("user_phone2", getNoNullValue(user.getPhone2()));
        properties.put("user_title", getNoNullValue(user.getTitle()));
        properties.put("booking_jobid", getNoNullValue(booking.getId()));
        properties.put("facility_key", getNoNullValue(booking.getFacilityKey()));
        properties.put("booking_capacityunits", getNoNullValue(booking.getCapacityUnits()));
        String duration = booking.isTimeBased() ? ((TimeBooking) booking).getDuration() + "" : "";
        properties.put("booking_duration", duration);
        Date seton = booking.getSeton();
        properties.put("booking_seton", seton == null ? "" : FamDateFormat.getLangIndependantShortDate(seton));
    }

    private static String getNoNullValue(Object object) {
        return object == null ? "" : object.toString();
    }

}

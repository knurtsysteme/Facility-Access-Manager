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
package de.knurt.fam.core.view.adapter.html;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.view.text.FamText;
import de.knurt.heinzelmann.ui.html.HtmlElement;

/**
 * adapt bookings for nice looking html.
 * @author Daniel Oltmanns
 * @since 0.20090926 (09/26/2009)
 */
@Deprecated
public interface HtmlAdapterBooking {

    /**
     * return the label of the facility.
     * @return the facility
     */
    public String getFacilityLabel();

    /**
     * return user's full name and mail address as html but do not name the username.
     * @return user's full name and mail address as html but do not name the username.
     */
    public HtmlElement getFullUserAsHtmlWithoutUsername();

    /**
     * return the booked units as text.
     * @return the booked units as text.
     */
    public String getCapacityUnitsAsText();

    /**
     * return the time frame for the session.
     * @return the time frame for the session.
     */
    public String getTimeframe();

    /**
     * return the status of booking.
     * @see FamText#statusOfBookingAsImg(de.knurt.fam.core.model.persist.User, de.knurt.fam.core.model.persist.booking.Booking)
     * @see FamText#statusOfBookingAsText(de.knurt.fam.core.model.persist.User, de.knurt.fam.core.model.persist.booking.Booking)
     * @return the status of booking.
     */
    public String getBookingStatus();

    /**
     * return the button canceling this booking.
     * @return the button canceling this booking.
     */
    public String getDeleteButton();

    /**
     * return the date the booking request set on.
     * @return the date the booking request set on.
     */
    public String getSeton();

    /**
     * return the username of the booker.
     * @see User#getUsername()
     * @return the username of the booker
     */
    public String getUsername();

    /**
     * return id of the booking.
     * @see Booking#getId()
     * @return id of the booking.
     */
    public String getId();

    /**
     * return a user containing all information available as html.
     * @return a user containing all information available as html
     */
    public HtmlElement getFullUserAsHtmlWithUsername();

    /**
     * return the time of the timeframe (and not the date).
     * @see #getTimeframe()
     * @return the time of the timeframe (and not the date).
     */
    public String getTimeframetime();

    /**
     * return a form button to do processed action
     * @see Booking#processSession
     * @param formAction url to send to
     * @return a form button to do processed action
     */
    public HtmlElement getProcessedForm(String formAction);
}

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
package de.knurt.fam.core.util.booking;

import java.util.List;

import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.BookingStatus;

/**
 * answer questions about bookings made.
 * @author Daniel Oltmanns
 * @since 0.20090702
 */
public class BookingUtil {

    /** one and only instance of me */
    private volatile static BookingUtil me;

    /** construct me */
    private BookingUtil() {
    }

    /**
     * return the one and only instance of BookingUtil
     * @return the one and only instance of BookingUtil
     */
    public static BookingUtil getInstance() {
        if (me == null) { // no instance so far
            synchronized (BookingUtil.class) {
                if (me == null) { // still no instance so far
                    me = new BookingUtil(); // the one and only
                }
            }
        }
        return me;
    }

    /**
     * return size of capacity units at its maximum use in given bookings.
     * does NOT check the {@link BookingStatus} of given bookings.
     * <br />
     * do not use it with many bookings, because:<br />
     * <code>complexity = bookings.size()^2 + m</code>
     * @param bookings to check
     * @return size of capacity units used maximum at once in given bookings.
     */
    public static int getSumOfMaxCapacityUsedAtOneTime(List<Booking> bookings) {
        int max = 0;
        for (Booking booking1 : bookings) {
            int tmpmax = 0;
            for (Booking booking2 : bookings) {
                if (booking1.overlaps(booking2)) { // booking 1 and 2 are identical or overlapping
                    tmpmax += booking2.getCapacityUnits();
                }
            }
            if (tmpmax > max) {
                max = tmpmax;
            }
        }
        return max;

    }
}

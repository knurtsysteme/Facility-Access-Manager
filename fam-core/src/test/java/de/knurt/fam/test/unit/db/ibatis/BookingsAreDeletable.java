/*
 * Copyright 2002-2008 the original author or authors.  *  * Licensed under the Creative Commons License Attribution-NonCommercial-ShareAlike 3.0 Unported;  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *      http://creativecommons.org/licenses/by-nc-sa/3.0/  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.
 */
package de.knurt.fam.test.unit.db.ibatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * @author Daniel Oltmanns
 * @since 0.20090828
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class BookingsAreDeletable extends FamIBatisTezt {

	/**
     * insert a booking and delete it afterwards
     */
	@Test
	public void isDeleted() {
		this.clearDatabase();
		int bookingsBeforeInsertion = FamDaoProxy.bookingDao().getAll().size();
		Booking booking = TeztBeanSimpleFactory.getNewValidBooking();
		booking.setBooked();
		assertTrue(booking.isAvailableForInsertion());
		booking.insert();
		assertFalse(booking.isAvailableForInsertion());

		int bookingsAfterInsertion = FamDaoProxy.bookingDao().getAll().size();
		assertEquals(bookingsBeforeInsertion + 1, bookingsAfterInsertion);

		assertTrue(booking.delete());
		int bookingsAfterDeleting = FamDaoProxy.bookingDao().getAll().size();
		assertEquals(bookingsBeforeInsertion, bookingsAfterDeleting);
	}
	/**
     * insert a booking and delete it afterwards
     */
	@Test
	public void deleteOnlyThisBooking() {
		this.clearDatabase();
		int bookingsBeforeInsertion = FamDaoProxy.bookingDao().getAll().size();

		Booking booking_1 = TeztBeanSimpleFactory.getNewValidBooking();
		booking_1.setBooked();
		assertTrue(booking_1.isAvailableForInsertion());
		booking_1.insert();
		assertFalse(booking_1.isAvailableForInsertion());

		Booking booking_2 = TeztBeanSimpleFactory.getNewValidBooking();
		booking_2.getSessionTimeFrame().add(Calendar.YEAR, 1);
		booking_2.setBooked();
		assertTrue(booking_2.isAvailableForInsertion());
		booking_2.insert();
		assertFalse(booking_2.isAvailableForInsertion());

		assertTrue(booking_1.delete());
		int bookingsAfterDeleting = FamDaoProxy.bookingDao().getAll().size();
		assertEquals(bookingsBeforeInsertion + 1, bookingsAfterDeleting);
	}
	/**
     * error on no id
     */
	@Test
	public void isNotDeletable() {
		this.clearDatabase();
		Booking booking = TeztBeanSimpleFactory.getNewValidBooking();
		assertFalse(booking.delete());
	}
}

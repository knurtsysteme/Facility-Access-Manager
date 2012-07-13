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
package de.knurt.fam.test.unit.bookbehaviour;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.BookingStatus;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class BookingTest extends FamIBatisTezt {

	@Test
	public void invoiced() {
		this.clearDatabase();
		Booking b = TeztBeanSimpleFactory.getNewValidBooking();
		b.setBooked();
		b.insert();
		Booking back = FamDaoProxy.bookingDao().getAll().get(0);
		assertNull(back.getLastInvoiced());
		back.invoice();
		back = FamDaoProxy.bookingDao().getAll().get(0);
		assertNotNull(back.getLastInvoiced());
	}
	/**
     *
     */
	@Test
	public void queueHasRightSortation() {
		this.clearDatabase();
		int i = 3;
		while (i-- > 0) {
			try {
				this.insertQueueBooking();
				Thread.sleep(1001);
			} catch (InterruptedException ex) {
				fail("thread defect");
			}
		}
		List<Booking> bs = FamDaoProxy.bookingDao().getAll();
		i = 0; // !
		while (i < 2) {
			assertTrue(bs.get(i).getSeton().before(bs.get(i + 1).getSeton()));
			i++;
		}
	}

	private void insertQueueBooking() {
		QueueBooking b = TeztBeanSimpleFactory.getNewValidAndBookedQueueBooking();
		b.insert();
	}

	/**
     *
     */
	@Test
	public void createBookingStatus() {
		BookingStatus bsa = new BookingStatus(BookingStatus.STATUS_APPLIED);
		assertEquals(BookingStatus.class, bsa.getClass());
		BookingStatus bsb = new BookingStatus(BookingStatus.STATUS_BOOKED);
		assertEquals(BookingStatus.class, bsb.getClass());
		assertFalse(bsa.equals(bsb));
	}

	/**
     *
     */
	@Test
	public void createBooking() {
		this.clearDatabase();

		// create TimeBooking
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		TimeBooking b = TeztBeanSimpleFactory.getNewValidBooking(br);
		assertEquals(TimeBooking.class, b.getClass());

		// is time frame
		try {
			@SuppressWarnings("unused")
			TimeFrame tmp = (TimeFrame) b;
			assertTrue("Booking is a TimeFrame", true);
		} catch (Exception e) {
			fail("must not thrown");
		}

		// has a user
		assertEquals(FacilityBookable.class, b.getFacility().getClass());
		assertTrue(b.getCapacityUnits().intValue() > 0);
		assertEquals(br.getBookingRule().getKey(), b.getFacilityKey());
	}

	/**
     *
     */
	@Test
	public void bookingStatusToGetAndSet() {
		this.clearDatabase();

		// create TimeBooking
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		TimeBooking b = TimeBooking.getNewBooking(br);
		assertTrue(b.getBookingStatus().isUnset());
	}

	/**
     *
     */
	@Test
	public synchronized void notAvailableAnymoreBookTwoOnOne_A() {
		this.clearDatabase();

		// create TimeBooking
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		TimeBooking b = TimeBooking.getNewBooking(br);
		b.getFacility().setCapacityUnits(1);
		assertEquals(1, b.getFacility().getCapacityUnits());
		b.setBooked();

		// insert booking
		assertTrue(b.isAvailableForInsertion());
		b.insert();
		assertFalse(b.isAvailableForInsertion());

		// create equal TimeBooking with other time units
		// take it one time unit back
		// ---- try to book
		// -- existing booking
		br = TeztBeanSimpleFactory.getBookingRequest();
		br.setRequestedTimeUnits(2);
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableTimeUnits(2);
		assertTrue(br.isValidRequest());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertFalse(b.isAvailableForInsertion());

		// take it one time unit back
		// ---- try to book
		// -- existing booking
		br.getRequestedStartTime().add(Calendar.MINUTE, br.getBookingRule().getSmallestMinutesBookable());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertTrue(b.isAvailableForInsertion());
	}

	/**
     *
     */
	@Test
	public void notAvailableAnymoreBookTwoOnTwo() {
		this.clearDatabase();

		// ---- insert
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		TimeBooking b = TimeBooking.getNewBooking(br);
		b.getFacility().setCapacityUnits(1);
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableTimeUnits(2);
		br.setRequestedTimeUnits(2);
		assertTrue(br.isValidRequest());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertTrue(b.isAvailableForInsertion());
		b.setBooked();
		b.insert();
		assertTrue(b.isTimeBased());
		assertFalse(b.isCanceled());
		assertFalse(b.sessionAlreadyBegun());
		assertFalse(b.sessionAlreadyMade());
		assertEquals(1, FamDaoProxy.bookingDao().getAll().size());
		assertFalse(b.isApplication());
		assertTrue(b.isBooked());
		assertFalse(b.getSessionTimeFrame().startsInPast());
		assertEquals(1, br.getBookingRule().getSetOfRulesForARole(br.getUser()).getMaxBookableCapacityUnits());
		assertFalse(b.isAvailableForInsertion());

		// ---- try to book
		// ---- existing booking
		br.getRequestedStartTime().add(Calendar.MINUTE, -2 * br.getBookingRule().getSmallestMinutesBookable());
		TimeBooking b2 = TimeBooking.getNewBooking(br);
		assertNotNull(b2);
		assertEquals(0, FamDaoProxy.bookingDao().getUncanceledBookingsWithoutApplicationsIn(b2.getFacility(), b2.getSessionTimeFrame()).size());
		assertTrue(b2.isAvailableForInsertion());

		// ---- try to book
		// ---- existing booking
		br.getRequestedStartTime().add(Calendar.MINUTE, br.getBookingRule().getSmallestMinutesBookable());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertFalse(b.isAvailableForInsertion());

		// ---- try to book
		// ---- existing booking
		br.getRequestedStartTime().add(Calendar.MINUTE, br.getBookingRule().getSmallestMinutesBookable());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertFalse(b.isAvailableForInsertion());

		// ---- try to book
		// ---- existing booking
		br.getRequestedStartTime().add(Calendar.MINUTE, br.getBookingRule().getSmallestMinutesBookable());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertFalse(b.isAvailableForInsertion());

		// ---- try to book
		// ---- existing booking
		br.getRequestedStartTime().add(Calendar.MINUTE, br.getBookingRule().getSmallestMinutesBookable());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertTrue(b.isAvailableForInsertion());
	}

	/**
     *
     */
	@Test
	public void notAvailableAnymoreBookTwoOnOne_B() {
		this.clearDatabase();

		// -- insert
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		TimeBooking b = TimeBooking.getNewBooking(br);
		b.getFacility().setCapacityUnits(1);
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableTimeUnits(1);
		br.setRequestedTimeUnits(1);
		assertTrue(br.isValidRequest());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertTrue(b.isAvailableForInsertion());
		b.setBooked();
		b.insert();
		assertFalse(b.isAvailableForInsertion());

		// ---- try to book
		// -- existing booking
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableTimeUnits(2);
		br.setRequestedTimeUnits(2);
		br.getRequestedStartTime().add(Calendar.MINUTE, -2 * br.getBookingRule().getSmallestMinutesBookable());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertTrue(b.isAvailableForInsertion());

		// ---- try to book
		// -- existing booking
		br.getRequestedStartTime().add(Calendar.MINUTE, br.getBookingRule().getSmallestMinutesBookable());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertFalse(b.isAvailableForInsertion());

		// ---- try to book
		// -- existing booking
		br.getRequestedStartTime().add(Calendar.MINUTE, br.getBookingRule().getSmallestMinutesBookable());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertFalse(b.isAvailableForInsertion());

		// ---- try to book
		// -- existing booking
		br.getRequestedStartTime().add(Calendar.MINUTE, br.getBookingRule().getSmallestMinutesBookable());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertTrue(b.isAvailableForInsertion());
	}

	/**
     *
     */
	@Test
	public void notAvailableAnymoreBookOneOnOne() {
		this.clearDatabase();

		// -- insert
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		TimeBooking b = TimeBooking.getNewBooking(br);
		b.getFacility().setCapacityUnits(1);
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableTimeUnits(1);
		br.setRequestedTimeUnits(1);
		assertTrue(br.isValidRequest());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertTrue(b.isAvailableForInsertion());
		b.setBooked();
		b.insert();
		assertFalse(b.isAvailableForInsertion());

		// -- try to book
		// -- existing booking
		br.getRequestedStartTime().add(Calendar.MINUTE, -2 * br.getBookingRule().getSmallestMinutesBookable());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertTrue(b.isAvailableForInsertion());

		// -- try to book
		// -- existing booking
		br.getRequestedStartTime().add(Calendar.MINUTE, br.getBookingRule().getSmallestMinutesBookable());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertTrue(b.isAvailableForInsertion());

		// -- try to book
		// -- existing booking
		br.getRequestedStartTime().add(Calendar.MINUTE, br.getBookingRule().getSmallestMinutesBookable());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertFalse(b.isAvailableForInsertion());

		// -- try to book
		// -- existing booking
		br.getRequestedStartTime().add(Calendar.MINUTE, br.getBookingRule().getSmallestMinutesBookable());
		b = TimeBooking.getNewBooking(br);
		assertNotNull(b);
		assertTrue(b.isAvailableForInsertion());
	}
}

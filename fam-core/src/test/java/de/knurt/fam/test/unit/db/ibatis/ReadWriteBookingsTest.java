/*
 * Copyright 2002-2008 the original author or authors.  *  * Licensed under the Creative Commons License Attribution-NonCommercial-ShareAlike 3.0 Unported;  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *      http://creativecommons.org/licenses/by-nc-sa/3.0/  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.
 */
package de.knurt.fam.test.unit.db.ibatis;

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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.BookingDao;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.ibatis.BookingDao4ibatis;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.fam.test.utils.AssertSomehowEquals;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * @author Daniel Oltmanns
 * @since 0.20090828
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class ReadWriteBookingsTest extends FamIBatisTezt {

	/**
     *
     */
	@Test
	public void stillAvailable() {
		this.clearDatabase();
		Booking booking = TeztBeanSimpleFactory.getNewValidBooking();
		booking.setBooked();
		assertTrue(booking.isAvailableForInsertion());
		booking.insert();
		assertFalse(booking.isAvailableForInsertion());
	}

	@Test
	public void ticket_348() {
		this.clearDatabase();
		Booking booking = TeztBeanSimpleFactory.getNewValidBooking();
		booking.setBooked();
		booking.insert();
		assertTrue(booking.getId() > 0);
		Booking back = FamDaoProxy.bookingDao().getBookingWithId(booking.getId());
		assertNotNull(back);
		assertFalse(back.isCanceled());
	}

	/**
     *
     */
	@Test
	public void constructDao() {
		BookingDao bdao = FamDaoProxy.bookingDao();
		assertEquals(BookingDao4ibatis.class, bdao.getClass());
	}

	/**
     *
     */
	@Test
	public void cancelOverlappingBookings_notAllCapacities() {
		this.clearDatabase();

		FacilityBookable bd = TeztBeanSimpleFactory.getFacilityBookable();

		Booking b_a = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		b_a.setApplied();
		b_a.insert();

		List<Booking> bookings = FamDaoProxy.bookingDao().getUncanceledBookingsAndApplicationsIn(bd, b_a.getSessionTimeFrame());
		assertEquals(1, bookings.size());

		Booking b_b = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		b_b.setBooked();
		b_b.insert();

		assertEquals(1, b_b.getFacility().getCapacityUnits());

		bookings = FamDaoProxy.bookingDao().getUncanceledBookingsAndApplicationsIn(bd, b_a.getSessionTimeFrame());
		assertEquals(1, bookings.size());
		AssertSomehowEquals.test(b_b, bookings.get(0));
	}

	/**
     *
     */
	@Test
	public void id() {
		this.clearDatabase();
		Booking b_a = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		assertNull(b_a.getId());
		b_a.setBooked();
		b_a.insert();
		assertNotNull(b_a.getId());
		assertTrue(b_a.getId() > 0);
		int a = b_a.getId();

		Booking b_b = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		b_b.getSessionTimeFrame().add(Calendar.YEAR, 1);
		b_b.setBooked();
		b_b.insert();
		int b = b_b.getId();
		assertTrue(b + " > " + a, b > a);
	}

	/**
     *
     */
	@Test
	public void aDebugTest_1() {
		this.clearDatabase();
		Booking b = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		b.setBooked();
		b.insert();
		assertFalse(b.isAvailableForInsertion());

		TimeFrame tf = b.getSessionTimeFrame().clone();
		assertFalse(b.isAvailableForInsertion());

		tf.setCalendarEnd(Calendar.DAY_OF_YEAR, tf.getCalendarEnd().get(Calendar.DAY_OF_YEAR) + 2);
		assertFalse(b.isAvailableForInsertion());
	}

	/**
    *
    */
	@Test
	public void userHasToWait() {
		this.clearDatabase();

		// asserts
		Integer verySafeId = 54654;
		String veryLalaUsername = "lala";

		// prepare
		User user = TeztBeanSimpleFactory.getNewValidUser();
		user.setRoleId("extern");
		user.setUsername(veryLalaUsername);
		user.insert();
		Booking booking = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		booking.setUsername(user.getUsername());
		booking.setBooked();
		booking.getBookingRule().getSetOfRulesForARole(booking.getUser()).setMaxBookableCapacityUnits(verySafeId);
		booking.setCapacityUnits(verySafeId);
		try {
			booking.insert();
			fail("should have been thrown exception because user has to wait 24 h");
		} catch (DataIntegrityViolationException e) {
			assertTrue(true); // right exception because user has to wait 24 h
		}

		// get
		List<Booking> bs = user.getBookings();

		// test
		assertEquals(0, bs.size());
	}

	/**
   *
   */
	@Test
	public void userHasNOTToWait() {
		this.clearDatabase();

		// asserts
		Integer verySafeId = 54654;
		String veryLalaUsername = "lala";

		// prepare
		User user = TeztBeanSimpleFactory.getNewValidUser();
		user.setRoleId("intern");
		user.setUsername(veryLalaUsername);
		user.insert();
		Booking booking = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		booking.setUsername(user.getUsername());
		booking.setBooked();
		booking.getBookingRule().getSetOfRulesForARole(booking.getUser()).setMaxBookableCapacityUnits(verySafeId);
		booking.setCapacityUnits(verySafeId);
		try {
			booking.insert();
			assertTrue(true);
		} catch (DataIntegrityViolationException e) {
			fail("should not thrown an exception");
		}

		// get
		List<Booking> bs = user.getBookings();

		// test
		assertEquals(1, bs.size());
		assertEquals(veryLalaUsername, bs.get(0).getUsername());
		assertEquals(verySafeId, bs.get(0).getCapacityUnits());
	}

	/**
     *
     */
	@Test
	public void getBookingsAndApplicationsIn() {
		this.clearDatabase();
		TimeFrame tf = new SimpleTimeFrame();
		FacilityBookable bd = TeztBeanSimpleFactory.getFacilityBookable();
		List<Booking> bookings = FamDaoProxy.bookingDao().getUncanceledBookingsAndApplicationsIn(bd, tf);
		assertEquals(0, bookings.size());

		tf.add(Calendar.DAY_OF_YEAR, 1);
		tf.setEnd(tf.getStart() + 5000);
		Booking booking = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		booking.setBooked();
		booking.getSessionTimeFrame().setEnd(tf.getEnd());
		booking.getSessionTimeFrame().setStart(tf.getStart());
		booking.insert();

		tf.add(Calendar.SECOND, 2);
		bookings = FamDaoProxy.bookingDao().getUncanceledBookingsAndApplicationsIn(bd, tf);
		assertEquals(1, bookings.size());
	}

	/**
     *
     */
	@Test
	public void getBookingsWithoutApplicationsIn_withException() {
		this.clearDatabase();

		TimeFrame tf = new SimpleTimeFrame();
		FacilityBookable bd = TeztBeanSimpleFactory.getFacilityBookable();
		List<Booking> bookings = FamDaoProxy.bookingDao().getUncanceledBookingsWithoutApplicationsIn(bd, tf);
		assertEquals(0, bookings.size());

		tf.add(Calendar.DAY_OF_YEAR, 1);
		tf.setEnd(tf.getStart() + 5000);
		Booking booking = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		booking.setBooked();
		booking.getSessionTimeFrame().setEnd(tf.getEnd());
		booking.getSessionTimeFrame().setStart(tf.getStart());
		booking.insert();

		tf.add(Calendar.SECOND, 2);
		bookings = FamDaoProxy.bookingDao().getUncanceledBookingsWithoutApplicationsIn(bd, tf);
		assertEquals(1, bookings.size());

		booking.setApplied();
		try {
			booking.insert();
			fail("should have thrown dataintegrityexception!");
		} catch (Exception e) {
			assertTrue(true);
		}
		try {
			booking.update();
			assertTrue(true);
		} catch (Exception e) {
			fail("should not have thrown dataintegrityexception!");
		}

		bookings = FamDaoProxy.bookingDao().getUncanceledBookingsWithoutApplicationsIn(bd, tf);
		assertEquals(1, bookings.size());
	}

	/**
     *
     */
	@Test
	public void getBookingsWithoutApplicationsIn() {
		this.clearDatabase();

		TimeFrame tf = new SimpleTimeFrame();
		FacilityBookable bd = TeztBeanSimpleFactory.getFacilityBookable();
		List<Booking> bookings = FamDaoProxy.bookingDao().getUncanceledBookingsWithoutApplicationsIn(bd, tf);
		assertEquals(0, bookings.size());

		tf.add(Calendar.DAY_OF_YEAR, 1);
		tf.setEnd(tf.getStart() + 5000);
		Booking application = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		application.setApplied();
		application.getSessionTimeFrame().setEnd(tf.getEnd());
		application.getSessionTimeFrame().setStart(tf.getStart());
		application.insert();

		tf.add(Calendar.SECOND, 2);
		bookings = FamDaoProxy.bookingDao().getUncanceledBookingsWithoutApplicationsIn(bd, tf);
		assertEquals(0, bookings.size());
		tf.add(Calendar.SECOND, -2);

		Booking booking = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		booking.setBooked();
		booking.getSessionTimeFrame().setEnd(tf.getEnd());
		booking.getSessionTimeFrame().setStart(tf.getStart());
		booking.insert();

		tf.add(Calendar.SECOND, 2);
		bookings = FamDaoProxy.bookingDao().getUncanceledBookingsWithoutApplicationsIn(bd, tf);
		assertEquals(1, bookings.size());
		AssertSomehowEquals.test(booking, bookings.get(0));
	}

	/**
     *
     */
	@Test
	public void getExample() {
		this.clearDatabase();

		// insert
		Booking booking = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		booking.setBooked();
		booking.insert();

		// selftest
		assertEquals(1, FamDaoProxy.bookingDao().getAll().size());

		// example
		Booking example = TimeBooking.getEmptyExampleBooking();

		// test
		assertEquals(1, FamDaoProxy.bookingDao().getObjectsLike(example).size());
	}

	/**
     *
     */
	@Test
	public void saveAndGetBackSaved() {
		this.clearDatabase();
		assertEquals(0, FamDaoProxy.bookingDao().getAll().size());
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		Booking booking = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		booking.setBooked();
		booking.insert();
		List<Booking> bookings = FamDaoProxy.bookingDao().getAll();
		assertEquals(1, bookings.size());

		Booking backBooking = bookings.get(0);
		assertEquals(TimeBooking.class, backBooking.getClass());
		assertEquals(FacilityBookable.class, backBooking.getFacility().getClass());
		assertTrue(backBooking.getCapacityUnits() > 0);
		assertEquals(br.getBookingRule().getKey(), backBooking.getFacilityKey());
	}

	/**
     *
     */
	@Test
	public void dataIntegrityOnInsert() {
		this.clearDatabase();

		// insert a valid first
		Booking booking = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		booking.setBooked();
		booking.insert();
		List<Booking> bookings = FamDaoProxy.bookingDao().getAll();
		assertEquals(1, bookings.size());

		this.clearDatabase();
		// change to wrong booking status
		booking = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		booking.setUnset();
		try {
			booking.insert();
			fail("must throw an exception");
		} catch (Exception e) {
			assertTrue("right exception", true);
		}
		booking.setBooked();

		booking.setCapacityUnits(9999999);
		try {
			booking.insert();
			fail("must throw an exception");
		} catch (Exception e) {
			assertTrue("right exception", true);
		}
		booking.setCapacityUnits(1);

		// be threadsafe
		booking.insert();
		Booking booking2 = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		booking2.setBooked();
		try {
			booking2.insert();
			fail("must throw an exception");
		} catch (Exception e) {
			assertTrue("right exception", true);
		}
	}
}

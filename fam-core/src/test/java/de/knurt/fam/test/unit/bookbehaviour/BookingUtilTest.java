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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.util.booking.BookingUtil;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class BookingUtilTest extends FamIBatisTezt {

	/**
     *
     */
	@Test
	public void specialChars() {
		this.clearDatabase();
		User test = TeztBeanSimpleFactory.getNewUniqueValidUser("1");
		test.setFname("Ötze");
		test.setSname("Müller");
		test.setUsername(null);
		test.setUniqueUsernameForInsertion();
		test.insert();

		User got = FamDaoProxy.userDao().getAll().get(0);
		assertEquals("Ötze", got.getFname());
		assertEquals("Müller", got.getSname());
		assertEquals("oemuelle", got.getUsername());
	}

	/**
	 * Test 1: 1 = 1 Test 2: 1 2 = 3
	 */
	@Test
	public void testBookingUtil_getSumOfMaxCapacityUsedAtOneTime_simple() {
		this.clearDatabase(); // all time availabile
		// prepare 1
		TimeBooking b1 = TeztBeanSimpleFactory.getNewValidBooking();
		b1.setBooked();
		b1.setCapacityUnits(1);
		List<Booking> bookings = new ArrayList<Booking>();
		bookings.add(b1);

		// test
		assertEquals(1, BookingUtil.getSumOfMaxCapacityUsedAtOneTime(bookings));

		// prepare 2
		TimeBooking b2 = TeztBeanSimpleFactory.getNewValidBooking();
		b2.setBooked();
		b2.setCapacityUnits(2);
		bookings.add(b2);

		// test
		assertEquals(3, BookingUtil.getSumOfMaxCapacityUsedAtOneTime(bookings));
	}

	/**
	 * Test: 1 3 = 3
	 */
	@Test
	public void testBookingUtil_getSumOfMaxCapacityUsedAtOneTime_ugly() {
		this.clearDatabase(); // all time availabile
		// prepare 1
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableCapacityUnits(4);

		TimeBooking b1 = TeztBeanSimpleFactory.getNewValidBooking(br);
		b1.setBooked();
		b1.setCapacityUnits(1);

		TimeBooking b2 = TeztBeanSimpleFactory.getNewValidBooking(br);
		b2.setBooked();
		b2.add(Calendar.MINUTE, 2 * br.getBookingRule().getSmallestMinutesBookable());
		b2.setCapacityUnits(3);

		ArrayList<Booking> bookings = new ArrayList<Booking>();
		bookings.add(b1);
		bookings.add(b2);

		// test
		assertEquals(3, BookingUtil.getSumOfMaxCapacityUsedAtOneTime(bookings));
	}

	/**
	 * Test: 1 3 = 4 3
	 */
	@Test
	public void testBookingUtil_getSumOfMaxCapacityUsedAtOneTime_veryUgly() {
		this.clearDatabase(); // all time availabile
		// prepare 1
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableCapacityUnits(4);

		TimeBooking b1 = TeztBeanSimpleFactory.getNewValidBooking(br);
		b1.setBooked();
		b1.setCapacityUnits(1);

		TimeBooking b2 = TeztBeanSimpleFactory.getNewValidBooking(br);
		b2.setBooked();
		b2.add(Calendar.MINUTE, 2 * br.getBookingRule().getSmallestMinutesBookable());
		b2.setCapacityUnits(3);

		TimeBooking b3 = TeztBeanSimpleFactory.getNewValidBooking(br);
		b3.setBooked();
		b3.setCapacityUnits(3);

		ArrayList<Booking> bookings = new ArrayList<Booking>();
		bookings.add(b1);
		bookings.add(b2);
		bookings.add(b3);

		// test
		assertEquals(4, BookingUtil.getSumOfMaxCapacityUsedAtOneTime(bookings));
	}

}
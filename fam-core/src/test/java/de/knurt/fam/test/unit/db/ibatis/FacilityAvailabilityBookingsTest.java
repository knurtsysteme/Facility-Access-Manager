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
package de.knurt.fam.test.unit.db.ibatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class FacilityAvailabilityBookingsTest extends FamIBatisTezt {

	/**
     *
     */
	@Test
	public void facilityAvailabilityNotice() {
		this.clearDatabase();
		String testNotice = "foo";

		FacilityAvailability da = TeztBeanSimpleFactory.getValidFacilityAvailability();

		da.setNotice(testNotice);
		assertEquals(testNotice, da.getNotice());

		da.insert();
		FacilityAvailability daBack = FamDaoProxy.facilityDao().getAll().get(0);
		assertEquals(testNotice, daBack.getNotice());
	}

	/**
     *
     */
	@Test
	public void facilityAvailabilityHasNotice() {
		FacilityAvailability da = TeztBeanSimpleFactory.getValidFacilityAvailability();
		assertFalse(da.hasNotice());

		da.setNotice("   	");
		assertFalse(da.hasNotice());

		da.setNotice(" foo bar!");
		assertTrue(da.hasNotice());
	}

	/**
     *
     */
	@Test
	public void insertGetAndDelete() {
		this.clearDatabase();

		// insert
		FacilityAvailability da = TeztBeanSimpleFactory.getValidFacilityAvailability();
		da.insert();
		long testid = da.getFacilityAvailabilityId();

		// selftest
		List<FacilityAvailability> das = FamDaoProxy.facilityDao().getAll();
		assertNotNull(das);
		assertEquals(1, das.size());
		assertEquals(testid, das.get(0).getFacilityAvailabilityId());

		// get example
		FacilityAvailability example = new FacilityAvailability();
		example.setFacilityAvailabilityId(testid);
		FacilityAvailability got = FamDaoProxy.facilityDao().getOneLike(example);

		// selftest
		assertEquals(example.getFacilityAvailabilityId(), got.getFacilityAvailabilityId());

		// delete
		got.delete();

		// nothing there anymoe
		das = FamDaoProxy.facilityDao().getAll();
		assertEquals(0, das.size());
	}

	@Test
	public void cancelOverlappingBookings() {
		this.clearDatabase();

		FacilityBookable bd = TeztBeanSimpleFactory.getFacilityBookable();

		TimeBooking b_a = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		b_a.setApplied();
		b_a.insert();

		List<Booking> bookings = FamDaoProxy.bookingDao().getUncanceledBookingsAndApplicationsIn(bd, b_a);
		assertEquals(1, bookings.size());

		FacilityAvailability da = TeztBeanSimpleFactory.getValidFacilityAvailability();
		da.setBasePeriodOfTime(b_a);
		da.setFacilityKey(bd.getKey());
		da.setNotAvailableBecauseOfMaintenance();
		da.insert(); // assert this kills the application above

		bookings = FamDaoProxy.bookingDao().getUncanceledBookingsAndApplicationsIn(bd, b_a);
		assertEquals(0, bookings.size());
	}

	@Test
	public void mustNotStartHere_daEqualsTimeStart() {
		this.clearDatabase();

		FacilityBookable bd = TeztBeanSimpleFactory.getFacilityBookable();

		TimeBooking b_a = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();

		FacilityAvailability da = TeztBeanSimpleFactory.getValidFacilityAvailability();
		da.setBasePeriodOfTime(b_a);
		da.setFacilityKey(bd.getKey());
		da.setMustNotStartHere();
		da.insert();

		assertEquals(b_a.getCalendarStart(), da.getBasePeriodOfTime().getCalendarStart());

		b_a.setBooked();
		assertFalse(b_a.isAvailableForInsertion());

	}

	@Test
	public void mustNotStartHere_daBeforeTimeStart() {
		this.clearDatabase();

		FacilityBookable bd = TeztBeanSimpleFactory.getFacilityBookable();

		TimeBooking b_a = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();

		TimeFrame minutesBefore = new SimpleTimeFrame(b_a.getCalendarStart(), b_a.getCalendarEnd());
		minutesBefore.add(Calendar.MINUTE, -2);
		assertTrue(minutesBefore.getDuration() > 2 * 60 * 1000);
		assertTrue(b_a.getDuration() > 2 * 60 * 1000);
		assertTrue(minutesBefore.getCalendarStart().before(b_a.getCalendarStart()));

		FacilityAvailability da = TeztBeanSimpleFactory.getValidFacilityAvailability();
		da.setBasePeriodOfTime(minutesBefore);
		da.setFacilityKey(bd.getKey());
		da.setMustNotStartHere();
		da.insert();

		b_a.setBooked();
		assertFalse(b_a.isAvailableForInsertion());
	}

	@Test
	public void mustNotStartHere_daAfterTimeStart() {
		this.clearDatabase();

		FacilityBookable bd = TeztBeanSimpleFactory.getFacilityBookable();

		TimeBooking b_a = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();

		TimeFrame minutesAfter = new SimpleTimeFrame(b_a.getCalendarStart(), b_a.getCalendarEnd());
		minutesAfter.add(Calendar.MINUTE, 2);
		assertTrue(minutesAfter.getDuration() > 2 * 60 * 1000);
		assertTrue(b_a.getDuration() > 2 * 60 * 1000);
		assertTrue(minutesAfter.getCalendarStart().after(b_a.getCalendarStart()));

		FacilityAvailability da = TeztBeanSimpleFactory.getValidFacilityAvailability();
		da.setBasePeriodOfTime(minutesAfter);
		da.setFacilityKey(bd.getKey());
		da.setMustNotStartHere();
		da.insert();

		b_a.setBooked();
		assertTrue(b_a.isAvailableForInsertion());
	}
}

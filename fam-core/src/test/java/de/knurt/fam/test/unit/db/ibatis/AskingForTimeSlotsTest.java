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
import static org.junit.Assert.assertNull;
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
import de.knurt.fam.core.model.persist.booking.Cancelation;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;
import de.knurt.heinzelmann.util.time.IntervalTimeFrame;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class AskingForTimeSlotsTest extends FamIBatisTezt {

	/**
     *
     */
	@Test
	public void constructBookingRequest() {
		this.clearDatabase(); // all time availabile
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		assertEquals(TimeBookingRequest.class, br.getClass());
	}

	/**
     *
     */
	@Test
	public void askingWithMinMax() {
		this.clearDatabase(); // all time availabile
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		br.getBookingRule().setSmallestMinutesBookable(1); // every minute
		// available
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMinBookableTimeUnits(1); // every
																							// minute
		// available
		br.setRequestedCapacityUnits(1);
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableCapacityUnits(1);
		List<TimeBookingRequest> timeSlots = br.getFreeBookingRequestsStartingSameDay();

		// selftest
		assertEquals(1440, timeSlots.size()); // the entire day

		timeSlots = br.getFreeBookingRequestsOfDay(10, 500);
		assertEquals(500, timeSlots.size()); // the entire day
		assertFalse(timeSlots.get(499).getRequestedStartTime().getTimeInMillis() == timeSlots.get(498).getRequestedStartTime().getTimeInMillis());

		timeSlots = br.getFreeBookingRequestsOfDay(1500, 2000);
		assertEquals(1500, timeSlots.size()); // the entire day
		assertFalse(timeSlots.get(1499).getRequestedStartTime().getTimeInMillis() == timeSlots.get(1498).getRequestedStartTime().getTimeInMillis());

		// every minute available
		br.getBookingRule().setSmallestMinutesBookable(15);
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMinBookableTimeUnits(1440 / 15);
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableTimeUnits(1440 / 15);
		br.setRequestedTimeUnits(1440 / 15);

		// selftest
		timeSlots = br.getFreeBookingRequestsStartingSameDay();
		assertEquals(96, timeSlots.size()); // the entire day
		timeSlots = br.getFreeBookingRequestsOfDay(3, 4);
		assertEquals(4, timeSlots.size()); // the entire day
		assertFalse(timeSlots.get(1).getRequestedStartTime().getTimeInMillis() == timeSlots.get(2).getRequestedStartTime().getTimeInMillis());
	}

	/**
     *
     */
	@Test
	public void getFreeTimeSlotsOfDay_withCancelations() {
		this.clearDatabase(); // all time availabile
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		br.getBookingRule().setSmallestMinutesBookable(1); // every minute
		// available
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMinBookableTimeUnits(1); // every
																							// minute
		// available
		br.setRequestedCapacityUnits(1);

		int freeTimeSlotsBefore = br.getFreeTimeSlotsOfDay().size();

		TimeBooking canceled = TimeBooking.getNewBooking(br);
		canceled.setBooked();
		canceled.cancel(new Cancelation(TeztBeanSimpleFactory.getNewValidUser(), Cancelation.REASON_FREE_BY_USER));
		int freeTimeSlotsAfter = br.getFreeTimeSlotsOfDay().size();
		assertEquals(freeTimeSlotsBefore, freeTimeSlotsAfter);
	}

	/**
     *
     */
	@Test
	public void getFreeTimeSlotsOfDay_onNoBookingsDay_timeUnits() {
		this.clearDatabase(); // all time availabile
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		br.getBookingRule().setSmallestMinutesBookable(1); // every minute
		// available
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMinBookableTimeUnits(1); // every
																							// minute
		// available
		br.setRequestedCapacityUnits(1);

		List<TimeFrame> timeSlots = br.getFreeTimeSlotsOfDay();
		assertTrue(timeSlots.size() > 0);

		br.getBookingRule().setSmallestMinutesBookable(1); // every minute
		// available
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMinBookableCapacityUnits(1);
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableCapacityUnits(1);
		timeSlots = br.getFreeTimeSlotsOfDay();
		assertEquals(1440, timeSlots.size()); // the entire day
		// duration of time slots is one minute
		assertEquals(60000, timeSlots.get(0).getDuration());
		assertEquals(60000, timeSlots.get(13).getDuration());
		assertEquals(60000, timeSlots.get(1240).getDuration());
		assertEquals(60000, timeSlots.get(1440 - 1).getDuration());

		br.getBookingRule().setSmallestMinutesBookable(15); // every 15 minutes
		// available
		timeSlots = br.getFreeTimeSlotsOfDay();
		assertEquals(timeSlots.size(), 1440 / 15); // the entire day

		// every 15 minutes available
		br.getBookingRule().setSmallestMinutesBookable(15);
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMinBookableTimeUnits(1440 / 15);
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableTimeUnits(1440 / 15);
		br.setRequestedTimeUnits(1440 / 15);

		timeSlots = br.getFreeTimeSlotsOfDay();
		assertEquals(96, timeSlots.size()); // the entire day

		// test with min and max units changed
		br.getBookingRule().setSmallestMinutesBookable(1); // every minute
		// available
		br.setRequestedTimeUnits(2);
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMinBookableTimeUnits(2);
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableTimeUnits(2);
		timeSlots = br.getFreeTimeSlotsOfDay();
		assertEquals(1440, timeSlots.size()); // still the entire day (next da
		// is free to 0:01)
		// duration of time slots is one minute
		assertEquals(2 * 60000, timeSlots.get(0).getDuration());
		assertEquals(2 * 60000, timeSlots.get(13).getDuration());
		assertEquals(2 * 60000, timeSlots.get(1440 / 2 - 1).getDuration());

		// test with min and max units changed
		br.getBookingRule().setSmallestMinutesBookable(15); // every minute
		// available
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMinBookableTimeUnits(2);
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableTimeUnits(2);
		timeSlots = br.getFreeTimeSlotsOfDay();
		assertEquals(1440 / 15, timeSlots.size()); // the entire day
		// duration of time slots is one minute
		assertEquals(timeSlots.get(0).getDuration(), 30 * 60000);
		assertEquals(timeSlots.get(2).getDuration(), 30 * 60000);
		assertEquals(timeSlots.get(1440 / 30).getDuration(), 30 * 60000);

	}

	/**
     *
     */
	@Test
	public void getFreeTimeSlotsOfDay_mustStartAt() {
		this.clearDatabase(); // all time availabile
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();

		br.getBookingRule().setMustStartAt(0);
		assertNotNull(br.getFreeTimeSlotsOfDay());
		TimeFrame timeSlot = br.getFreeTimeSlotsOfDay().get(0);
		assertEquals(0, timeSlot.getCalendarStart().get(Calendar.MINUTE));
		assertEquals(0, timeSlot.getCalendarStart().get(Calendar.HOUR_OF_DAY));

		br.getBookingRule().setMustStartAt(20);
		timeSlot = br.getFreeTimeSlotsOfDay().get(0);
		assertEquals(20, timeSlot.getCalendarStart().get(Calendar.MINUTE));
		assertEquals(0, timeSlot.getCalendarStart().get(Calendar.HOUR_OF_DAY));

		br.getBookingRule().setMustStartAt(123);
		timeSlot = br.getFreeTimeSlotsOfDay().get(0);
		assertEquals(3, timeSlot.getCalendarStart().get(Calendar.MINUTE));
		assertEquals(2, timeSlot.getCalendarStart().get(Calendar.HOUR_OF_DAY));
	}

	/**
     *
     */
	@Test
	public void assertTooLessTimeUnits() {
		this.clearDatabase(); // all time availabile
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();

		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableTimeUnits(1); // every
																							// minute
		// available
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMinBookableTimeUnits(1); // every
																							// minute
		// available
		br.setRequestedTimeUnits(1); // one minute requested
		List<TimeFrame> timeSlots = br.getFreeTimeSlotsOfDay();
		assertTrue(timeSlots.size() > 0); // the entire day

		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMinBookableTimeUnits(2); // every
																							// minute
		// available
		br.setRequestedTimeUnits(1); // one minute requested
		timeSlots = br.getFreeTimeSlotsOfDay();
		assertNull(timeSlots); // the entire day
	}

	/**
     *
     */
	@Test
	public void assertTooManyTimeUnits() {
		this.clearDatabase(); // all time availabile
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();

		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableTimeUnits(1); // every
																							// minute
		// available
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMinBookableTimeUnits(1); // every
																							// minute
		// available
		br.setRequestedTimeUnits(1); // one minute requested
		List<TimeFrame> timeSlots = br.getFreeTimeSlotsOfDay();
		assertTrue(timeSlots.size() > 0); // the entire day

		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableTimeUnits(2); // every
																							// minute
		// available
		br.setRequestedTimeUnits(3); // one minute requested
		timeSlots = br.getFreeTimeSlotsOfDay();
		assertNull(timeSlots); // the entire day
	}

	/**
     *
     */
	@Test
	public void assertTooManyCapacityUnits() {
		this.clearDatabase(); // all time availabile
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();

		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMinBookableCapacityUnits(1); // every
																								// minute
		// available
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableCapacityUnits(1); // every
																								// minute
		// available
		br.setRequestedCapacityUnits(1); // one minute requested
		List<TimeFrame> timeSlots = br.getFreeTimeSlotsOfDay();
		assertTrue(timeSlots.size() > 0); // the entire day

		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableCapacityUnits(2); // every
																								// minute
		// available
		br.setRequestedCapacityUnits(3); // one minute requested
		timeSlots = br.getFreeTimeSlotsOfDay();
		assertNull(timeSlots); // the entire day
	}

	/**
     *
     */
	@Test
	public void assertTooLessCapacityUnits() {
		this.clearDatabase(); // all time availabile
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();

		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableCapacityUnits(1); // every
																								// minute
		// available
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMinBookableCapacityUnits(1); // every
																								// minute
		// available
		br.setRequestedCapacityUnits(1); // one minute requested
		List<TimeFrame> timeSlots = br.getFreeTimeSlotsOfDay();
		assertTrue(timeSlots.size() > 0); // the entire day

		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMinBookableCapacityUnits(2); // every
																								// minute
		// available
		br.setRequestedCapacityUnits(1); // one minute requested
		timeSlots = br.getFreeTimeSlotsOfDay();
		assertNull(timeSlots); // the entire day
	}

	/**
     *
     */
	@Test
	public void getFreeTimeSlotsOfDay_onNotAvaiablePartsDay() {
		this.clearDatabase();

		Calendar day = TeztBeanSimpleFactory.getTomorrow();
		day.set(Calendar.HOUR_OF_DAY, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		day.set(Calendar.MILLISECOND, 0);

		// set not available from 0 to 6 o clock and insert
		FacilityAvailability da = TeztBeanSimpleFactory.getValidFacilityAvailability();
		Calendar dayEnd = (Calendar) day.clone();
		dayEnd.add(Calendar.HOUR_OF_DAY, 6);
		TimeFrame basePeriod = new SimpleTimeFrame(day, dayEnd);
		da.setAvailable(FacilityAvailability.GENERAL_NOT_AVAILABLE);
		da.setInterval(IntervalTimeFrame.ONE_TIME);
		da.setBasePeriodOfTime(basePeriod);
		da.insert();

		// get BookingRequest for same day
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		br.setRequestedStartTime(day);

		// selftest
		assertTrue(da.overlaps(basePeriod));
		assertEquals(da.getBasePeriodOfTime().getCalendarStart(), br.getRequestedStartTime());
		assertTrue(da.isNotAvailableInGeneral());

		// 15 minutes steps from 6 to 24 o clock
		List<TimeFrame> timeSlots = br.getFreeTimeSlotsOfDay();
		assertEquals(24 * 4 * 3 / 4, timeSlots.size());

		// enter a second not availability from 18-24 o clock
		FacilityAvailability da2 = TeztBeanSimpleFactory.getValidFacilityAvailability();
		Calendar day2 = (Calendar) day.clone();
		day2.add(Calendar.HOUR_OF_DAY, 18);
		Calendar dayEnd2 = (Calendar) day2.clone();
		dayEnd2.add(Calendar.HOUR_OF_DAY, 6);
		TimeFrame basePeriod2 = new SimpleTimeFrame(day2, dayEnd2);
		da2.setAvailable(FacilityAvailability.GENERAL_NOT_AVAILABLE);
		da2.setInterval(FacilityAvailability.ONE_TIME);
		da2.setBasePeriodOfTime(basePeriod2);
		da2.insert();
		timeSlots = br.getFreeTimeSlotsOfDay();
		assertEquals(24 * 4 * 1 / 2, timeSlots.size()); // 15 min. steps from 6
		// to 18 o clock

		// confirmApplication same as available
		da2.setAvailable(FacilityAvailability.COMPLETE_AVAILABLE);
		da2.update();
		timeSlots = br.getFreeTimeSlotsOfDay();
		assertEquals(24 * 4 * 3 / 4, timeSlots.size());
	}

	/**
     *
     */
	@Test
	public synchronized void getFreeTimeSlotsOfDay_onDayWithFullBookingsAlready() {
		this.clearDatabase();
		TimeBooking bookingMade_1 = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		bookingMade_1.setBooked();

		// get free time slots on a free day
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		List<TimeFrame> timeSlots = br.getFreeTimeSlotsOfDay();
		assertEquals(1, br.getRequestedCapacityUnits());
		assertEquals(1, br.getBookingRule().getMinBookableCapacityUnits(br.getUser()));
		assertEquals(1, br.getBookingRule().getMaxBookableCapacityUnits(br.getUser()));
		assertEquals(96, timeSlots.size()); // the entire day

		// add one of this free time slot as booking
		TimeFrame ts3bm_1 = timeSlots.get(10);
		bookingMade_1.setStart(ts3bm_1.getStart());
		bookingMade_1.setEnd(ts3bm_1.getEnd());
		bookingMade_1.insert();
		List<TimeFrame> timeSlots2 = br.getFreeTimeSlotsOfDay();
		assertEquals(1, FamDaoProxy.bookingDao().getAll().size());
		Booking bBack = FamDaoProxy.bookingDao().getAll().get(0);
		assertFalse(bBack.isCanceled());
		assertNotNull(bBack.getSessionTimeFrame());
		assertEquals(ts3bm_1 + "", bBack.getSessionTimeFrame() + "");
		assertEquals(1, FamDaoProxy.bookingDao().getUncanceledBookingsAndApplicationsIn(bookingMade_1.getFacility(), bookingMade_1.getSessionTimeFrame()).size());
		assertEquals(1, bookingMade_1.getCapacityUnits().intValue());
		assertEquals(timeSlots2.get(0).getCalendarStart(), timeSlots.get(0).getCalendarStart());
		assertEquals(95, timeSlots2.size()); // the entire day

		// add one of this free time slot as booking
		TimeBooking bookingMade_2 = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		bookingMade_2.setBooked();
		bookingMade_2.setStart(timeSlots.get(20).getStart());
		bookingMade_2.setEnd(timeSlots.get(20).getEnd());
		assertTrue(bookingMade_2.isAvailableForInsertion());
		bookingMade_2.insert();
		timeSlots = br.getFreeTimeSlotsOfDay();
		assertEquals(94, timeSlots.size()); // the entire day
	}

	/**
	 * test booking on a day, where a booking for the same time exists but there
	 * is enough capacity left for the booking anyway.
	 */
	@Test
	public void getFreeTimeSlotsOfDay_onDayWithPartBookingsAlready() {
		this.clearDatabase();

		// get free time slots on a free day
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableCapacityUnits(10);
		FacilityBookable d = br.getFacility();
		int tmpCU = d.getCapacityUnits();
		d.setCapacityUnits(10);
		br.setRequestedCapacityUnits(5);
		assertTrue(TimeBooking.getNewBooking(br).isAvailableForInsertion());

		TimeBooking booking = TimeBooking.getNewBooking(br);
		booking.setBooked();
		booking.insert();

		assertTrue(TimeBooking.getNewBooking(br).isAvailableForInsertion());
		br.setRequestedCapacityUnits(6);
		assertFalse(TimeBooking.getNewBooking(br).isAvailableForInsertion());
		d.setCapacityUnits(tmpCU);
	}

	/**
     *
     */
	@Test
	public void getFreeTimeSlotsForManyDays() {
		this.clearDatabase();

		// get free time slot on a free calendar
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMinBookableTimeUnits(1);
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableTimeUnits(3);
		br.getBookingRule().setSmallestMinutesBookable(24 * 60); // 1 day
		br.setRequestedTimeUnits(2);

		assertTrue(TimeBooking.getNewBooking(br).isAvailableForInsertion());
		assertEquals(1, br.getFreeTimeSlotsOfDay().size());
		assertEquals(2 * 24 * 60 * 60 * 1000, br.getFreeTimeSlotsOfDay().get(0).getDuration());
	}

	/**
     *
     */
	@Test
	public void getFreeTimeSlotsForSmallestMinuteBiggerThenDay() {
		this.clearDatabase();

		// get free time slot on a free calendar
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMinBookableTimeUnits(1);
		br.getBookingRule().getSetOfRulesForARole(br.getUser()).setMaxBookableTimeUnits(3);
		br.getBookingRule().setSmallestMinutesBookable(24 * 60 + 1); // > 1 day
		br.setRequestedTimeUnits(1);
		assertNotNull(TimeBooking.getNewBooking(br));
		assertTrue(TimeBooking.getNewBooking(br).isAvailableForInsertion());
		assertEquals(1, br.getFreeTimeSlotsOfDay().size());
	}
}

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.config.UsersUnitsQueueBasedBookingRule;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.BookingStatus;
import de.knurt.fam.core.model.persist.booking.Cancelation;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;
import de.knurt.heinzelmann.util.time.TimeFrame;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class QueueBookingTest extends FamIBatisTezt {

	/**
     *
     */
	@After
	public void setLengthTo0() {
		UsersUnitsQueueBasedBookingRule br = this.getBookingRule();
		while (br.getActualQueueLength() > 0) {
			br.reduceQueue();
		}
	}

	/**
     *
     */
	@Test
	public void selftest() {
		FacilityBookable bd = TeztBeanSimpleFactory.getBookableQueueFacility();
		BookingRule br = bd.getBookingRule();
		assertEquals(UsersUnitsQueueBasedBookingRule.class, br.getClass());
	}

	private FacilityBookable getFacility() {
		return TeztBeanSimpleFactory.getBookableQueueFacility();
	}

	private UsersUnitsQueueBasedBookingRule getBookingRule() {
		FacilityBookable bd = TeztBeanSimpleFactory.getBookableQueueFacility();
		return (UsersUnitsQueueBasedBookingRule) bd.getBookingRule();
	}

	/**
     *
     */
	@Test
	public void queueLength_noInteraction() {
		this.clearDatabase();
		UsersUnitsQueueBasedBookingRule br = this.getBookingRule();
		assertEquals(0, br.getActualQueueLength());
	}

	/**
     *
     */
	@Test
	public void queueLength_addABooking1() {
		this.clearDatabase();
		UsersUnitsQueueBasedBookingRule br = this.getBookingRule();
		assertEquals(0, br.getActualQueueLength());
		QueueBooking b = TeztBeanSimpleFactory.getNewValidAndBookedQueueBooking();
		b.insert();
		assertEquals(1, br.getActualQueueLength());
	}

	/**
     *
     */
	@Test
	public void queueLength_addABooking2() {
		this.clearDatabase();
		QueueBooking b = TeztBeanSimpleFactory.getNewValidAndBookedQueueBooking();
		b.insert();

		UsersUnitsQueueBasedBookingRule br = this.getBookingRule();
		assertEquals(1, br.getActualQueueLength());
	}

	private void insertQueueBooking() {
		QueueBooking b = TeztBeanSimpleFactory.getNewValidAndBookedQueueBooking();
		b.insert();
	}

	/**
     *
     */
	@Test
	public void testInsertQueueBooking() {
		this.clearDatabase();
		FacilityBookable bd = this.getFacility();
		QueueBooking b = TeztBeanSimpleFactory.getNewValidAndBookedQueueBooking();
		String userNameBefore = b.getUsername();
		List<Booking> bookings = FamDaoProxy.bookingDao().getAll();
		assertEquals(0, bookings.size());
		b.insert();
		bookings = FamDaoProxy.bookingDao().getAll();
		assertEquals(1, bookings.size());
		Booking backbook = bookings.get(0);
		assertEquals(this.getBookingRule().hashCode(), backbook.getBookingRule().hashCode());
		assertEquals(BookingStatus.STATUS_BOOKED, backbook.getBookingStatus().getStatus());
		assertNull(backbook.getCancelation());
		assertEquals(new Integer(1), backbook.getCapacityUnits());
		assertEquals(bd.getKey(), backbook.getFacilityKey());
		assertNotNull(backbook.getId());
		assertTrue(backbook.getId() > 0);
		assertNull(backbook.getNotice());
		assertNotNull(backbook.getSeton());
		assertEquals(userNameBefore, backbook.getUsername());
		assertTrue(backbook.isBooked());
	}

	/**
     *
     */
	@Test
	public void behaviourOfQueueBooking_expectedSessionTimeFrameAndQueuePosition() {
		this.clearDatabase();

		// insert 1st
		QueueBooking b1 = TeztBeanSimpleFactory.getNewValidAndBookedQueueBooking();
		b1.insert();

		// get it back
		QueueBooking backbook_1 = (QueueBooking) FamDaoProxy.bookingDao().getAll().get(0);
		assertNull(backbook_1.getSessionTimeFrame());
		assertNotNull(backbook_1.getExpectedSessionStart());
		assertNotNull(backbook_1.getExpectedSessionEnd());
		assertNotNull(backbook_1.getExpectedSessionTimeFrame());

		// get time frame back
		TimeFrame backtf_1 = backbook_1.getExpectedSessionTimeFrame();
		Integer actualQueuPosition_1 = backbook_1.getActualQueuePosition();
		assertNotNull(actualQueuPosition_1);
		assertEquals(1, actualQueuPosition_1.intValue());

		// compute assert
		Integer uphp = this.getBookingRule().getUnitsPerHourProcessed();
		assertNotNull(uphp);
		Calendar assertStart_1 = Calendar.getInstance();
		Calendar assertEnd_1 = Calendar.getInstance();
		assertEnd_1.add(Calendar.MINUTE, 60 / uphp);
		assertEquals(assertStart_1.getTimeInMillis() / 1000, backtf_1.getStart() / 1000);
		assertEquals(assertEnd_1.getTimeInMillis() / 1000, backtf_1.getEnd() / 1000);

		// insert 2nd
		QueueBooking b2 = TeztBeanSimpleFactory.getNewValidAndBookedQueueBooking();
		this.waitASecond();
		b2.insert();

		// get it back
		QueueBooking backbook_2 = (QueueBooking) FamDaoProxy.bookingDao().getAll().get(1);
		assertNotSame(backbook_1.getId(), backbook_2.getId());
		assertNull(backbook_2.getSessionTimeFrame());
		assertNotNull(backbook_2.getExpectedSessionTimeFrame());

		// get time frame back
		TimeFrame backtf_2 = backbook_2.getExpectedSessionTimeFrame();
		Integer actualQueuePosiotion_2 = backbook_2.getActualQueuePosition();
		assertNotNull(actualQueuePosiotion_2);
		assertEquals(2, actualQueuePosiotion_2.intValue());

		// compute assert
		assertNotNull(uphp);
		Calendar assertStart_2 = Calendar.getInstance();
		Calendar assertEnd_2 = Calendar.getInstance();
		assertStart_2.add(Calendar.MINUTE, (60 / uphp) * 1);
		assertEnd_2.add(Calendar.MINUTE, (60 / uphp) * 2);
		assertEquals(assertStart_2.getTimeInMillis() / 1000, backtf_2.getStart() / 1000);
		assertEquals(assertEnd_2.getTimeInMillis() / 1000, backtf_2.getEnd() / 1000);

		// check 1st one again
		actualQueuPosition_1 = backbook_1.getActualQueuePosition();
		assertEquals(1, actualQueuPosition_1.intValue());
		assertEquals(assertStart_1.getTimeInMillis() / 1000, backtf_1.getStart() / 1000);
		assertEquals(assertEnd_1.getTimeInMillis() / 1000, backtf_1.getEnd() / 1000);

		// insert 3rd
		QueueBooking b3 = TeztBeanSimpleFactory.getNewValidAndBookedQueueBooking();
		this.waitASecond();
		b3.insert();

		// get it back
		QueueBooking backbook_3 = (QueueBooking) FamDaoProxy.bookingDao().getAll().get(2);
		assertNotSame(backbook_1.getId(), backbook_3.getId());
		assertNotSame(backbook_2.getId(), backbook_3.getId());
		assertNull(backbook_3.getSessionTimeFrame());
		assertNotNull(backbook_3.getExpectedSessionTimeFrame());

		// get time frame back
		TimeFrame backtf_3 = backbook_3.getExpectedSessionTimeFrame();
		Integer actualQueuePosiotion_3 = backbook_3.getActualQueuePosition();
		assertNotNull(actualQueuePosiotion_3);
		assertEquals(3, actualQueuePosiotion_3.intValue());

		// compute assert
		assertNotNull(uphp);
		Calendar assertStart_3 = Calendar.getInstance();
		Calendar assertEnd_3 = Calendar.getInstance();
		assertStart_3.add(Calendar.MINUTE, (60 / uphp) * 2);
		assertEnd_3.add(Calendar.MINUTE, (60 / uphp) * 3);
		assertEquals(assertStart_3.getTimeInMillis() / 5000, backtf_3.getStart() / 5000);
		assertEquals(assertEnd_3.getTimeInMillis() / 5000, backtf_3.getEnd() / 5000);

		// check 1st one again
		actualQueuPosition_1 = backbook_1.getActualQueuePosition();
		assertEquals(1, actualQueuPosition_1.intValue());
		assertEquals(assertStart_1.getTimeInMillis() / 1000, backtf_1.getStart() / 1000);
		assertEquals(assertEnd_1.getTimeInMillis() / 1000, backtf_1.getEnd() / 1000);

		// check 2nd one again
		actualQueuePosiotion_2 = backbook_2.getActualQueuePosition();
		backtf_2 = backbook_2.getExpectedSessionTimeFrame();
		assertEquals(2, actualQueuePosiotion_2.intValue());
		// ↘ use 10 seconds here because of waitASecond funtions
		assertEquals(assertStart_2.getTimeInMillis() / 1000000, backtf_2.getStart() / 1000000); 
		assertEquals(assertEnd_2.getTimeInMillis() / 1000000, backtf_2.getEnd() / 1000000);
	}

	/**
     *
     */
	@Test
	public void cancelBooking() {
		this.clearDatabase();

		// insert three bookings
		this.insertQueueBooking();
		this.waitASecond();
		this.insertQueueBooking();
		this.waitASecond();
		this.insertQueueBooking();

		QueueBooking qb_0 = (QueueBooking) FamDaoProxy.bookingDao().getAll().get(0);
		QueueBooking qb_1 = (QueueBooking) FamDaoProxy.bookingDao().getAll().get(1);
		QueueBooking qb_2 = (QueueBooking) FamDaoProxy.bookingDao().getAll().get(2);

		assertEquals(1, qb_0.getActualQueuePosition().intValue());
		assertEquals(2, qb_1.getActualQueuePosition().intValue());
		assertEquals(3, qb_2.getActualQueuePosition().intValue());
		assertEquals(3, this.getQueueLength());

		// now cancel the middle
		qb_1.cancel(new Cancelation(qb_1.getUser(), "foo"));

		// assert last one 1 stop forward
		assertEquals(1, qb_0.getActualQueuePosition().intValue());
		assertNull(qb_1.getActualQueuePosition());
		assertEquals(2, qb_2.getActualQueuePosition().intValue());

		// assert queue length
		assertEquals(2, this.getQueueLength());
	}

	/**
     *
     */
	@Test
	public void assertMailSent() {
		this.clearDatabase();

		assertEquals(0, FamDaoProxy.userDao().getAllUserMails().size());

		// insert three bookings
		this.insertQueueBooking();
		this.insertQueueBooking();
		this.insertQueueBooking();

		assertEquals(3, FamDaoProxy.userDao().getAllUserMails().size());

		QueueBooking qb = (QueueBooking) FamDaoProxy.bookingDao().getAll().get(0);

		qb.cancel(new Cancelation(qb.getUser(), Cancelation.REASON_BOOKED_BY_ANOTHER));

		assertEquals(4, FamDaoProxy.userDao().getAllUserMails().size());
	}

	private int getQueueLength() {
		return this.getBookingRule().getActualQueueLength();
	}

	private void waitASecond() {
		try {
			Thread.sleep(1001);
		} catch (InterruptedException ex) {
			fail("thread kaputt");
		}
	}
}

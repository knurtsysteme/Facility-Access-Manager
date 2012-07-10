package de.knurt.fam.test.unit.bookbehaviour;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.util.booking.BookingFinder;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class BookingFinderTest {

	@Test
	public void getNextTo_onMaintenance_1hour_validUnits() {
		TimeBookingRequest tbr = TeztBeanSimpleFactory.getBookingRequest();

		// insert da for same facility
		FacilityAvailability da = TeztBeanSimpleFactory.getValidFacilityAvailability();
		da.setFacilityKey(tbr.getFacility().getKey());
		da.setNotAvailableBecauseOfMaintenance();
		Calendar start = Calendar.getInstance();
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		start.add(Calendar.DAY_OF_YEAR, 10);
		Calendar end = (Calendar) start.clone();
		end.add(Calendar.HOUR_OF_DAY, 1);
		TimeFrame tfMaintenance = new SimpleTimeFrame(start, end);
		da.setBasePeriodOfTime(tfMaintenance);
		da.insert();

		// create tbr
		tbr.getFacility().setCapacityUnits(30);
		tbr.getFacility().getBookingRule().getSetOfRulesForARole(tbr.getUser()).setMinBookableTimeUnits(3 * 96);
		tbr.getFacility().getBookingRule().getSetOfRulesForARole(tbr.getUser()).setMaxBookableTimeUnits(30 * 96);
		tbr.getFacility().getBookingRule().getSetOfRulesForARole(tbr.getUser()).setMinBookableCapacityUnits(3);
		tbr.getFacility().getBookingRule().getSetOfRulesForARole(tbr.getUser()).setMaxBookableCapacityUnits(30);
		tbr.getFacility().getBookingRule().setSmallestMinutesBookable(15);

		// on same time as maintenance with valid capacity and time units
		tbr.setRequestedCapacityUnits(3);
		tbr.setRequestedStartTime(start);
		tbr.setRequestedTimeUnits(3 * 96);
		assertFalse(tbr.isAvailable());

		List<TimeBookingRequest> tbrsBack = BookingFinder.getBookingRequestNextTo(tbr);
		assertEquals(2, tbrsBack.size());
		TimeBookingRequest gotBack = tbrsBack.get(0);
		assertTrue(gotBack.isAvailable());
		assertEquals(gotBack.getRequestedStartTime().getTimeInMillis(), end.getTimeInMillis());
		assertEquals(gotBack.getRequestedCapacityUnits(), tbr.getRequestedCapacityUnits());
		assertEquals(gotBack.getRequestedTimeUnits(), tbr.getRequestedTimeUnits());
	}

	@Test
	public void tbr_clone1() {
		TimeBookingRequest tbr = TeztBeanSimpleFactory.getBookingRequest();
		tbr.getBookingRule().getSetOfRulesForARole(tbr.getUser()).setMaxBookableCapacityUnits(10);
		TimeBookingRequest clone = (TimeBookingRequest) tbr.clone();
		assertEquals(tbr.getArticleNumber(), clone.getArticleNumber());
		tbr.setRequestedCapacityUnits(5);
		assertEquals(5, tbr.getRequestedCapacityUnits());
		assertEquals(1, clone.getRequestedCapacityUnits());
		assertNotSame(tbr.getArticleNumber(), clone.getArticleNumber());
		assertFalse(tbr.getArticleNumber().equals(clone.getArticleNumber()));
	}

	@Test
	public void tbr_clone2() {
		TimeBookingRequest tbr = TeztBeanSimpleFactory.getBookingRequest();
		tbr.getBookingRule().getSetOfRulesForARole(tbr.getUser()).setMaxBookableTimeUnits(10);
		TimeBookingRequest clone = (TimeBookingRequest) tbr.clone();
		assertEquals(tbr.getArticleNumber(), clone.getArticleNumber());
		tbr.setRequestedTimeUnits(5);
		assertEquals(5, tbr.getRequestedTimeUnits());
		assertEquals(1, clone.getRequestedTimeUnits());
		assertNotSame(tbr.getArticleNumber(), clone.getArticleNumber());
		assertFalse(tbr.getArticleNumber().equals(clone.getArticleNumber()));
	}

	@Test
	public void getNextTo_onMaintenance_1day_validUnits() {
		TimeBookingRequest tbr = TeztBeanSimpleFactory.getBookingRequest();

		// insert da for same facility
		FacilityAvailability da = TeztBeanSimpleFactory.getValidFacilityAvailability();
		da.setFacilityKey(tbr.getFacility().getKey());
		da.setNotAvailableBecauseOfMaintenance();
		Calendar start = Calendar.getInstance();
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		start.add(Calendar.DAY_OF_YEAR, 10);
		Calendar end = (Calendar) start.clone();
		end.add(Calendar.DAY_OF_YEAR, 1);
		TimeFrame tfMaintenance = new SimpleTimeFrame(start, end);
		da.setBasePeriodOfTime(tfMaintenance);
		da.insert();

		// create tbr
		tbr.getFacility().setCapacityUnits(30);
		tbr.getFacility().getBookingRule().getSetOfRulesForARole(tbr.getUser()).setMinBookableTimeUnits(3 * 96);
		tbr.getFacility().getBookingRule().getSetOfRulesForARole(tbr.getUser()).setMaxBookableTimeUnits(30 * 96);
		tbr.getFacility().getBookingRule().getSetOfRulesForARole(tbr.getUser()).setMinBookableCapacityUnits(3);
		tbr.getFacility().getBookingRule().getSetOfRulesForARole(tbr.getUser()).setMaxBookableCapacityUnits(30);
		tbr.getFacility().getBookingRule().setSmallestMinutesBookable(15);

		// on same time as maintenance with valid capacity and time units
		tbr.setRequestedCapacityUnits(3);
		tbr.setRequestedStartTime(start);
		tbr.setRequestedTimeUnits(3 * 96);
		assertFalse(tbr.isAvailable());

		List<TimeBookingRequest> tbrsBack = BookingFinder.getBookingRequestNextTo(tbr);
		assertEquals(2, tbrsBack.size());
		TimeBookingRequest gotBack = tbrsBack.get(0);
		assertEquals(gotBack.getRequestedStartTime().getTimeInMillis(), end.getTimeInMillis());
		assertEquals(gotBack.getRequestedCapacityUnits(), tbr.getRequestedCapacityUnits());
		assertEquals(gotBack.getRequestedTimeUnits(), tbr.getRequestedTimeUnits());
	}

}
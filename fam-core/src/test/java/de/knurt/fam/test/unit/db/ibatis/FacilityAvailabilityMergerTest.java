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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.util.time.FacilityAvailabilityMerger;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class FacilityAvailabilityMergerTest extends FamIBatisTezt {

	/**
     *
     */
	@Test
	public void facilitiesMergeTest() {
		ArrayList<FacilityAvailability> tfs = new ArrayList<FacilityAvailability>();
		TimeFrame fromTo = new SimpleTimeFrame(new GregorianCalendar(2008, 0, 1), new GregorianCalendar(2009, 0, 1));

		Calendar c1 = new GregorianCalendar(2008, 0, 1);
		Calendar c2 = new GregorianCalendar(2008, 2, 1);
		FacilityAvailability da1 = new FacilityAvailability("a", c1, c2);
		da1.setTimeStampSet(1);
		tfs.add(da1);

		c1 = new GregorianCalendar(2008, 1, 1);
		c2 = new GregorianCalendar(2008, 3, 1);
		FacilityAvailability da2 = new FacilityAvailability("b", c1, c2);
		da2.setTimeStampSet(2);
		tfs.add(da2);

		ArrayList<String> priority = new ArrayList<String>();
		priority.add("a");
		priority.add("b");
		List<FacilityAvailability> is = FacilityAvailabilityMerger.getMergedByFacilities(tfs, fromTo, priority);
		assertEquals(2, is.size());
		assertEquals(0, is.get(0).getBasePeriodOfTime().getCalendarStart().get(Calendar.MONTH));
		assertEquals(1, is.get(0).getBasePeriodOfTime().getCalendarEnd().get(Calendar.MONTH));
		assertEquals(1, is.get(1).getBasePeriodOfTime().getCalendarStart().get(Calendar.MONTH));
		assertEquals(3, is.get(1).getBasePeriodOfTime().getCalendarEnd().get(Calendar.MONTH));

		priority = new ArrayList<String>();
		priority.add("b");
		priority.add("a");
		is = FacilityAvailabilityMerger.getMergedByFacilities(tfs, fromTo, priority);
		assertEquals(2, is.size());
		assertEquals(0, is.get(1).getBasePeriodOfTime().getCalendarStart().get(Calendar.MONTH));
		assertEquals(2, is.get(1).getBasePeriodOfTime().getCalendarEnd().get(Calendar.MONTH));
		assertEquals(2, is.get(0).getBasePeriodOfTime().getCalendarStart().get(Calendar.MONTH));
		assertEquals(3, is.get(0).getBasePeriodOfTime().getCalendarEnd().get(Calendar.MONTH));
	}

	/**
     *
     */
	@Test
	public void timeIteratorMergeTest() {
		ArrayList<FacilityAvailability> tfs = new ArrayList<FacilityAvailability>();
		TimeFrame fromTo = new SimpleTimeFrame(new GregorianCalendar(2008, 0, 1), new GregorianCalendar(2009, 0, 1));

		List<FacilityAvailability> is = FacilityAvailabilityMerger.getMergedByTimeStampSet(tfs, fromTo);
		assertEquals(0, tfs.size());

		String facilityKey = "doesnotmatter";

		Calendar c1 = new GregorianCalendar(2008, 0, 2);
		Calendar c2 = new GregorianCalendar(2008, 0, 3);
		FacilityAvailability da1 = new FacilityAvailability(facilityKey, c1, c2);
		da1.setMonthly();
		da1.setTimeStampSet(1);
		tfs.add(da1);
		is = FacilityAvailabilityMerger.getMergedByTimeStampSet(tfs, fromTo);
		assertEquals(12, is.size());

		c1 = new GregorianCalendar(2008, 1, 1);
		c2 = new GregorianCalendar(2008, 6, 5);
		FacilityAvailability da2 = new FacilityAvailability(facilityKey, c1, c2);
		da2.setTimeStampSet(2);
		tfs.add(da2);
		is = FacilityAvailabilityMerger.getMergedByTimeStampSet(tfs, fromTo);
		assertEquals(7, is.size());

		da2.setTimeStampSet(1); // 1.2.2008 - 5.7.2008 - one time
		da1.setTimeStampSet(2); // 2.1.2008 - 3.1.2008 - monthly
		tfs = new ArrayList<FacilityAvailability>();
		tfs.add(da1);
		tfs.add(da2);
		is = FacilityAvailabilityMerger.getMergedByTimeStampSet(tfs, fromTo);
		assertEquals(19, is.size());
	}

	private final static int MAYBE_AVAILABLE = 2; 

	/**
     *
     */
	@Test
	public void timeSimpleMergeTest() {
		ArrayList<FacilityAvailability> tfs = new ArrayList<FacilityAvailability>();
		TimeFrame fromTo = new SimpleTimeFrame(new GregorianCalendar(2008, 0, 1), new GregorianCalendar(2009, 0, 1));

		List<FacilityAvailability> is = FacilityAvailabilityMerger.getMergedByTimeStampSet(tfs, fromTo);
		assertEquals(0, tfs.size());

		String facilityKey = "doesnotmatter";

		Calendar c1 = new GregorianCalendar(2008, 0, 1);
		Calendar c2 = new GregorianCalendar(2008, 6, 1);
		FacilityAvailability da1 = new FacilityAvailability(facilityKey, c1, c2);
		da1.setAvailable(FacilityAvailability.COMPLETE_AVAILABLE);
		da1.setTimeStampSet(1);
		tfs.add(da1);
		is = FacilityAvailabilityMerger.getMergedByTimeStampSet(tfs, fromTo);
		assertEquals(1, is.size());

		c1 = new GregorianCalendar(2008, 3, 1);
		c2 = new GregorianCalendar(2008, 9, 1);
		FacilityAvailability da2 = new FacilityAvailability(facilityKey, c1, c2);
		da2.setMaybeAvailable();
		da2.setTimeStampSet(2);
		tfs.add(da2);
		is = FacilityAvailabilityMerger.getMergedByTimeStampSet(tfs, fromTo);
		assertEquals(2, is.size());
		assertEquals(0, is.get(0).getBasePeriodOfTime().getCalendarStart().get(Calendar.MONTH));
		assertEquals(3, is.get(0).getBasePeriodOfTime().getCalendarEnd().get(Calendar.MONTH));
		assertEquals(FacilityAvailability.COMPLETE_AVAILABLE, (is.get(0)).getAvailable().intValue());
		assertEquals(3, is.get(1).getBasePeriodOfTime().getCalendarStart().get(Calendar.MONTH));
		assertEquals(9, is.get(1).getBasePeriodOfTime().getCalendarEnd().get(Calendar.MONTH));
		assertEquals(MAYBE_AVAILABLE, (is.get(1)).getAvailable().intValue());

		c1 = new GregorianCalendar(2008, 10, 1);
		c2 = new GregorianCalendar(2008, 11, 1);
		FacilityAvailability da3 = new FacilityAvailability(facilityKey, c1, c2);
		da3.setAvailable(FacilityAvailability.COMPLETE_AVAILABLE);
		da3.setTimeStampSet(3);
		tfs.add(da3);
		is = FacilityAvailabilityMerger.getMergedByTimeStampSet(tfs, fromTo);
		assertEquals(3, is.size());

		c1 = new GregorianCalendar(2008, 2, 1);
		c2 = new GregorianCalendar(2008, 4, 1);
		FacilityAvailability da4 = new FacilityAvailability(facilityKey, c1, c2);
		da4.setAvailable(FacilityAvailability.GENERAL_NOT_AVAILABLE);
		da4.setTimeStampSet(4);
		tfs.add(da4);
		is = FacilityAvailabilityMerger.getMergedByTimeStampSet(tfs, fromTo);

		assertEquals(4, is.size());

		assertEquals(4, is.get(0).getBasePeriodOfTime().getCalendarStart().get(Calendar.MONTH));
		assertEquals(9, is.get(0).getBasePeriodOfTime().getCalendarEnd().get(Calendar.MONTH));
		assertTrue(is.get(0).isMaybeAvailable());

		assertEquals(10, is.get(1).getBasePeriodOfTime().getCalendarStart().get(Calendar.MONTH));
		assertEquals(11, is.get(1).getBasePeriodOfTime().getCalendarEnd().get(Calendar.MONTH));
		assertTrue(is.get(1).isCompletelyAvailable());

		assertEquals(2, is.get(2).getBasePeriodOfTime().getCalendarStart().get(Calendar.MONTH));
		assertEquals(4, is.get(2).getBasePeriodOfTime().getCalendarEnd().get(Calendar.MONTH));
		assertTrue(is.get(2).isNotAvailableInGeneral());

		assertEquals(0, is.get(3).getBasePeriodOfTime().getCalendarStart().get(Calendar.MONTH));
		assertEquals(2, is.get(3).getBasePeriodOfTime().getCalendarEnd().get(Calendar.MONTH));
		assertTrue(is.get(3).isCompletelyAvailable());
	}

	/**
	 * test reengeneering driwen architektscho
	 */
	@Test
	public void debug_1() {
		this.clearDatabase();
		ArrayList<FacilityAvailability> mergeThis = new ArrayList<FacilityAvailability>();

		FacilityAvailability da1 = TeztBeanSimpleFactory.getValidFacilityAvailability(1);
		da1.getBasePeriodOfTime().setStart(new GregorianCalendar(2009, 4, 7, 8, 0, 0));
		da1.getBasePeriodOfTime().setEnd(new GregorianCalendar(2009, 4, 7, 11, 0, 0));
		da1.setDaily();
		da1.setFacilityKey(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE);
		mergeThis.add(da1);

		FacilityAvailability da2 = TeztBeanSimpleFactory.getValidFacilityAvailability(2);
		da2.getBasePeriodOfTime().setStart(new GregorianCalendar(2009, 4, 12, 0, 0, 0));
		da2.getBasePeriodOfTime().setEnd(new GregorianCalendar(2009, 4, 12, 5, 0, 0));
		da2.setDaily();
		da2.setFacilityKey(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE);
		mergeThis.add(da2);

		FacilityAvailability da3 = TeztBeanSimpleFactory.getValidFacilityAvailability(3);
		da3.getBasePeriodOfTime().setStart(new GregorianCalendar(2009, 4, 6, 22, 0, 0));
		da3.getBasePeriodOfTime().setEnd(new GregorianCalendar(2009, 4, 7, 0, 0, 0));
		da3.setDaily();
		da3.setFacilityKey(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE);
		mergeThis.add(da3);

		TimeFrame fromTo = new SimpleTimeFrame(new GregorianCalendar(2009, 4, 11, 0, 0, 0), new GregorianCalendar(2009, 4, 12, 0, 0, 0));

		ArrayList<String> orderedFacilityPriority = new ArrayList<String>();
		orderedFacilityPriority.add(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_PARENT);
		orderedFacilityPriority.add(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE);

		assertEquals(1, da1.getIntervalTimeFramesWithNoIteration(fromTo).size());
		assertEquals(1, da2.getSingleSimpleTimeFrames(fromTo).size());
		assertEquals(1, da2.getIntervalTimeFramesWithNoIteration(fromTo).size());
		assertEquals(1, da3.getIntervalTimeFramesWithNoIteration(fromTo).size());

		List<FacilityAvailability> is = FacilityAvailabilityMerger.getMergedByTimeStampSet(mergeThis, fromTo);
		assertEquals(3, is.size());

		is = FacilityAvailabilityMerger.getMergedByFacilities(mergeThis, fromTo, orderedFacilityPriority);
		assertEquals(3, is.size());
	}

	/**
     *
     */
	@Test
	public void timeInBetweenTest() {
		ArrayList<FacilityAvailability> tfs = new ArrayList<FacilityAvailability>();
		TimeFrame fromTo = new SimpleTimeFrame(new GregorianCalendar(2008, 0, 1), new GregorianCalendar(2009, 0, 1));

		List<FacilityAvailability> is = FacilityAvailabilityMerger.getMergedByTimeStampSet(tfs, fromTo);
		assertEquals(0, tfs.size());

		String facilityKey = "doesnotmatter";

		Calendar c1 = new GregorianCalendar(2008, 0, 1);
		Calendar c2 = new GregorianCalendar(2008, 6, 1);
		FacilityAvailability da1 = new FacilityAvailability(facilityKey, c1, c2);
		da1.setAvailable(FacilityAvailability.MAINTENANCE_NOT_AVAILABLE);
		da1.setTimeStampSet(1);
		tfs.add(da1);

		// in between another
		c1 = new GregorianCalendar(2008, 2, 1);
		c2 = new GregorianCalendar(2008, 3, 1);
		FacilityAvailability da5 = new FacilityAvailability(facilityKey, c1, c2);
		da5.setAvailable(FacilityAvailability.GENERAL_NOT_AVAILABLE);
		da5.setTimeStampSet(2);
		tfs.add(da5);
		is = FacilityAvailabilityMerger.getMergedByTimeStampSet(tfs, fromTo);

		assertEquals(3, is.size());

		assertEquals(2, is.get(0).getBasePeriodOfTime().getCalendarStart().get(Calendar.MONTH));
		assertEquals(3, is.get(0).getBasePeriodOfTime().getCalendarEnd().get(Calendar.MONTH));

		assertEquals(0, is.get(1).getBasePeriodOfTime().getCalendarStart().get(Calendar.MONTH));
		assertEquals(2, is.get(1).getBasePeriodOfTime().getCalendarEnd().get(Calendar.MONTH));

		assertEquals(3, is.get(2).getBasePeriodOfTime().getCalendarStart().get(Calendar.MONTH));
		assertEquals(6, is.get(2).getBasePeriodOfTime().getCalendarEnd().get(Calendar.MONTH));

	}

}
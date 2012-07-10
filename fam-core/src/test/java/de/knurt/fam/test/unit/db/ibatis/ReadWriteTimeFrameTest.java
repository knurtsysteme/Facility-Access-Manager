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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.FacilityDao;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.ibatis.FacilityDao4ibatis;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class ReadWriteTimeFrameTest extends FamIBatisTezt {

	/**
     *
     */
	@Test
	public void testUserMustBeSet() {
		this.clearDatabase();

		FacilityAvailability da = TeztBeanSimpleFactory.getValidFacilityAvailability();
		try {
			da.insert();
			assertTrue(true);
		} catch (Exception e) {
			fail("should not have thrown");
		}

		da.setUsernameSetThis("foo is not a user in database");
		try {
			da.insert();
			fail("should have thrown");
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	/**
     *
     */
	@Test
	public void idsSet() {
		this.clearDatabase();

		TeztBeanSimpleFactory.getValidFacilityAvailability().insert();
		List<FacilityAvailability> das = FamDaoProxy.facilityDao().getAll();

		assertTrue(das.size() == 1);

		FacilityAvailability got = das.get(0);
		assertTrue(got.getFacilityAvailabilityId() > 0);

		assertNotNull(got.getTimeStampSet());
		Date sometimesago = new Date(new Date().getTime() - 100000);
		Date dategot = got.getTimeStampSet();
		assertTrue(dategot.after(sometimesago));

		// start of same game again:

		TeztBeanSimpleFactory.getValidFacilityAvailability().insert();
		das = FamDaoProxy.facilityDao().getAll();

		assertTrue(das.size() == 2);

		assertNotSame(das.get(0).getFacilityAvailabilityId(), das.get(1).getFacilityAvailabilityId());
	}

	/**
     *
     */
	@Test
	public void constructDao() {
		FacilityDao dao = FamDaoProxy.facilityDao();
		assertTrue(dao.getClass().equals(FacilityDao4ibatis.class));
	}

	/**
     *
     */
	@Test
	public void getAvailabilitiesInOther() {
		this.clearDatabase();

		FacilityAvailability inda = TeztBeanSimpleFactory.getValidFacilityAvailability();

		List<FacilityAvailability> das = FamDaoProxy.facilityDao().getFacilityAvailabilitiesIgnoreParents(inda.getBasePeriodOfTime(), inda.getFacilityKey());
		assertTrue(das.size() == 0);

		inda.insert();

		das = FamDaoProxy.facilityDao().getFacilityAvailabilitiesIgnoreParents(inda.getBasePeriodOfTime(), inda.getFacilityKey());
		assertEquals(1, das.size());
	}

	/**
     *
     */
	@Test
	public void getAllAvailabilitiesForFacility() {
		this.clearDatabase();
		List<FacilityAvailability> das = FamDaoProxy.facilityDao().getFacilityAvailabilities(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE);
		assertEquals(0, das.size());

		FacilityAvailability inda = TeztBeanSimpleFactory.getValidFacilityAvailability();
		inda.insert();
		das = FamDaoProxy.facilityDao().getFacilityAvailabilities(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE);
		assertEquals(1, das.size());
		das = FamDaoProxy.facilityDao().getFacilityAvailabilities(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_2);
		assertEquals(0, das.size());

		FacilityAvailability other = TeztBeanSimpleFactory.getValidFacilityAvailability(2);
		other.insert();
		das = FamDaoProxy.facilityDao().getFacilityAvailabilities(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE);
		assertEquals(2, das.size());
		das = FamDaoProxy.facilityDao().getFacilityAvailabilities(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_2);
		assertEquals(0, das.size());

		FacilityAvailability otherFacility = TeztBeanSimpleFactory.getValidFacilityAvailability(2);
		otherFacility.setFacilityKey(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_2);
		assertTrue(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_2.equals("schoolbus")); // idiot
																			// netbeans
		otherFacility.insert();
		das = FamDaoProxy.facilityDao().getFacilityAvailabilities(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE);
		assertEquals(2, das.size());
		das = FamDaoProxy.facilityDao().getFacilityAvailabilities(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_2);
		assertEquals(1, das.size());

		das = FamDaoProxy.facilityDao().getFacilityAvailabilities("not set insane stuff");
		assertEquals(0, das.size());
	}

	/**
     *
     */
	@Test
	public void insertValidAndGetBack() {
		this.clearDatabase();
		FacilityDao dao = FamDaoProxy.getInstance().getFacilityDao();
		FacilityAvailability ava = TeztBeanSimpleFactory.getValidFacilityAvailability();
		dao.insert(ava);

		assertEquals(1, dao.getAll().size());
		FacilityAvailability tf = dao.getAll().get(0);
		FacilityAvailability gnaback = tf;
		assertEquals(gnaback.getFacilityKey(), TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE);
		assertEquals(gnaback.getBasePeriodOfTime().getStart() / 1000, ava.getBasePeriodOfTime().getStart() / 1000);
		assertEquals(gnaback.getBasePeriodOfTime().getEnd() / 1000, ava.getBasePeriodOfTime().getEnd() / 1000);
	}

	/**
     *
     */
	@Test
	public void directInsertStorable() {
		this.clearDatabase();
		FacilityAvailability testNotAvailablility = TeztBeanSimpleFactory.getValidFacilityAvailability();
		testNotAvailablility.insert();
		testNotAvailablility.update();
		assertEquals(1, FamDaoProxy.facilityDao().getAll().size());
		assertNotNull(testNotAvailablility.getFacilityAvailabilityId());
		assertTrue(testNotAvailablility.getFacilityAvailabilityId() > 0);
	}
}
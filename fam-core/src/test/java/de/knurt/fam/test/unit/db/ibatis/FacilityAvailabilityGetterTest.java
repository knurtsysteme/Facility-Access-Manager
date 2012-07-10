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

import static de.knurt.fam.test.utils.TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE;
import static de.knurt.fam.test.utils.TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_PARENT;
import static de.knurt.fam.test.utils.TeztBeanSimpleFactory.getValidFacilityAvailability;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class FacilityAvailabilityGetterTest extends FamIBatisTezt {

	/**
     *
     */
	@Test
	public void debug200909131523() {
		this.clearDatabase();
		// create da with starting 1 hour ago and ending in 1 hour
		FacilityAvailability da = new FacilityAvailability();
		Calendar start = Calendar.getInstance();
		start.add(Calendar.HOUR_OF_DAY, 1);
		Calendar end = (Calendar) start.clone();
		end.add(Calendar.HOUR_OF_DAY, 2);

		// the problem
		da.setEndOfBasePeriodOfTime(end.getTime());
		da.setStartOfBasePeriodOfTime(end.getTime());

		// other stuff
		da.setFacilityKey(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE);
		User user = TeztBeanSimpleFactory.getNewUniqueValidUser(end.hashCode() + "");
		user.insert();
		da.setUserSetThis(user);
		da.setAvailable(FacilityAvailability.COMPLETE_AVAILABLE);
		da.setNotice("testnotice");

		da.insert();

		// get it back
		List<FacilityAvailability> das = FamDaoProxy.facilityDao().getFacilityAvailabilitiesFollowingParents(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE);
		assertEquals(1, das.size());
		FacilityAvailability dagot = das.get(0);
		assertEquals("testnotice", dagot.getNotice());
	}

	/**
     *
     */
	@Test
	public void checkParentIsParentAndAdamAndEveIsAdamAndEve() {
		String adamAndEve = FacilityConfigDao.getInstance().getParentFacilityKey(KEY_FACILITY_BOOKABLE);
		assertEquals(KEY_FACILITY_BOOKABLE_PARENT, adamAndEve);
	}

	/**
     *
     */
	@Test
	public void getChildrenFacilitiesToo() {
		this.clearDatabase();
		FacilityAvailability daPut = new FacilityAvailability();
		daPut.setBasePeriodOfTime(SimpleTimeFrame.getToday());
		daPut.setFacilityKey(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_PARENT);

		User testuser = TeztBeanSimpleFactory.getNewValidUser();
		testuser.setUsername("testuser");
		testuser.insert();
		daPut.setUserSetThis(testuser);
		daPut.setAvailable(FacilityAvailability.GENERAL_NOT_AVAILABLE);
		daPut.insert();

		assertEquals(1, FamDaoProxy.facilityDao().getAll().size());
		assertEquals(1, FamDaoProxy.facilityDao().getFacilityAvailabilities(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_PARENT).size());
		assertEquals(1, FamDaoProxy.facilityDao().getFacilityAvailabilities(FacilityConfigDao.facility(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_PARENT)).size());

		List<FacilityAvailability> children = FamDaoProxy.facilityDao().getFacilityAvailabilitiesFollowingParents(SimpleTimeFrame.getToday(), TeztBeanSimpleFactory.getFacility1());
		assertEquals(1, children.size());
	}

	/**
     *
     */
	@Test
	public void allFacilityAvailabilitiesOfFacilityKey() {
		this.clearDatabase();
		List<FacilityAvailability> das = FamDaoProxy.facilityDao().getFacilityAvailabilitiesFollowingParents(KEY_FACILITY_BOOKABLE);
		assertEquals(0, das.size());

		FacilityAvailability da = getValidFacilityAvailability();
		da.setFacilityKey(KEY_FACILITY_BOOKABLE);
		da.insert();

		das = FamDaoProxy.facilityDao().getFacilityAvailabilitiesFollowingParents(KEY_FACILITY_BOOKABLE);
		assertEquals(1, das.size());

		da = getValidFacilityAvailability();
		da.setFacilityKey(KEY_FACILITY_BOOKABLE_PARENT);
		da.insert();

		das = FamDaoProxy.facilityDao().getFacilityAvailabilitiesFollowingParents(KEY_FACILITY_BOOKABLE);
		assertEquals(2, das.size());
	}
}

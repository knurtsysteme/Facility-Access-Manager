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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class UsersResponsibilitiesTest extends FamIBatisTezt {

	/**
     *
     */
	@Test
	public void getFacilitiesNo() {
		this.clearDatabase();
		assertEquals(0, FamDaoProxy.facilityDao().getFacilitiesUserIsResponsibleFor(UserFactory.me().blank()).size());
		assertFalse(FamDaoProxy.facilityDao().hasResponsibilityForAFacility(UserFactory.me().blank()));
	}

	@Test
	public void adminTasks() {
		this.clearDatabase();
		User testuser = TeztBeanSimpleFactory.getNewValidUser();
		testuser.insert();
		assertFalse(testuser.hasAdminTasks());
		assertFalse(testuser.hasResponsibilities4Facilities());
		assertFalse(testuser.hasResponsibility4Facility(TeztBeanSimpleFactory.getFacility1()));
	}

	@Test
	public void getUsersForFacility() {
		this.clearDatabase();
		User testuser = TeztBeanSimpleFactory.getNewValidUser();
		testuser.insert();
		Facility facility = TeztBeanSimpleFactory.getFacility1();
		List<Facility> facilities = new ArrayList<Facility>(1);
		facilities.add(facility);
		assertTrue(FamDaoProxy.facilityDao().updateResponsibility(testuser, facilities));

		List<User> users = FamDaoProxy.userDao().getResponsibleUsers(facility);
		assertNotNull(users);
		assertEquals(1, users.size());
		assertEquals(testuser.getUsername(), users.get(0).getUsername());
		assertEquals(1, testuser.getFacilitiesUserIsResponsibleFor().size());
	}

}
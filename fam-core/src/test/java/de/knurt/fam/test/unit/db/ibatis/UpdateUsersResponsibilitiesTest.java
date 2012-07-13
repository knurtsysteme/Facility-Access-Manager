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
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class UpdateUsersResponsibilitiesTest extends FamIBatisTezt {

	@Test
	public void insertEmptyIsOkay() {
		this.clearDatabase();
		User testuser = TeztBeanSimpleFactory.getNewUniqueValidUser("asv");
		testuser.insert();
		List<Facility> responsible = new ArrayList<Facility>(0);
		assertTrue(FamDaoProxy.facilityDao().updateResponsibility(testuser, responsible));
		assertEquals(0, testuser.getFacilitiesUserIsResponsibleFor().size());
	}

	@Test
	public void insert() {
		this.clearDatabase();
		User testuser = TeztBeanSimpleFactory.getNewUniqueValidUser("asv");
		testuser.insert();
		List<Facility> dbFs = testuser.getFacilitiesUserIsResponsibleFor();
		assertEquals(0, dbFs.size());

		// insert
		List<Facility> updateFs = new ArrayList<Facility>(1);
		updateFs.add(TeztBeanSimpleFactory.getFacility1());
		assertTrue(FamDaoProxy.facilityDao().updateResponsibility(testuser, updateFs));
		dbFs = testuser.getFacilitiesUserIsResponsibleFor();
		assertEquals(1, dbFs.size());
	}

	@Test
	public void update() {
		this.clearDatabase();
		User testuser = TeztBeanSimpleFactory.getNewUniqueValidUser("asv");
		testuser.insert();
		
		List<Facility> fs = FacilityConfigDao.getInstance().getAll();
		Facility f1 = fs.get(0);
		Facility f2 = fs.get(1);
		Facility f3 = fs.get(2);
		
		List<Facility> updateFs = new ArrayList<Facility>(1);
		updateFs.add(f1);
		assertTrue(FamDaoProxy.facilityDao().updateResponsibility(testuser, updateFs));
		assertEquals(1, testuser.getFacilitiesUserIsResponsibleFor().size());
		assertEquals(f1.getKey(), testuser.getFacilitiesUserIsResponsibleFor().get(0).getKey());

		// update with two other facility
		updateFs = new ArrayList<Facility>(2);
		updateFs.add(f2);
		updateFs.add(f3);
		assertTrue(FamDaoProxy.facilityDao().updateResponsibility(testuser, updateFs));
		assertEquals(2, testuser.getFacilitiesUserIsResponsibleFor().size());
		assertFalse(f1.getKey().equals(testuser.getFacilitiesUserIsResponsibleFor().get(0).getKey()));
		assertFalse(f1.getKey().equals(testuser.getFacilitiesUserIsResponsibleFor().get(1).getKey()));
		assertFalse(testuser.hasResponsibility4Facility(f1));
		assertTrue(testuser.hasResponsibility4Facility(f2));
		assertTrue(testuser.hasResponsibility4Facility(f3));
	}
}
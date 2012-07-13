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

import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.persistence.dao.config.RoleConfigDao;
import de.knurt.fam.core.view.text.FamText;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class AccessConfigObjectsTest extends FamIBatisTezt {

	@Autowired
	private FacilityConfigDao accessFacility;
	@Autowired
	private RoleConfigDao accessRole;
	@Autowired
	private FamText accessText;

	/**
     *
     */
	@Test
	public void accessRole() {
		assertEquals(accessRole.getClass(), RoleConfigDao.class);
	}

	/**
     *
     */
	@Test
	public void getFacilitiesBookable() {
		Collection<Facility> ads = FacilityConfigDao.getInstance().getCollectionOfAllConfigured();
		Collection<FacilityBookable> bds = FacilityConfigDao.getInstance().getBookableFacilities();
		assertTrue(ads.size() > 0);
		assertTrue(bds.size() > 0);
		assertTrue(bds.size() < ads.size());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	/**
     *
     */
	@After
	public void tearDown() {
	}

	/**
     *
     */
	@Test
	public void accessFacility() {
		assertEquals(accessFacility.getClass(), FacilityConfigDao.class);
	}

	/**
     *
     */
	@Test
	public void accessText() {
		assertEquals(accessText.getClass(), FamText.class);
		String key = TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE + ".label";
		String gotMessage = accessText.getMessage(key);
		assertNotNull(gotMessage);
		assertTrue(gotMessage.equals(key) == false);
	}

	/**
     *
     */
	@SuppressWarnings("unchecked")
	@Test
	public void accessFacilityLabels() {
		Properties labels = accessFacility.getLabels();

		assertTrue(labels.size() >= 1); // facility access
		Enumeration<String> names = (Enumeration<String>) labels.propertyNames();
		while (names.hasMoreElements()) {
			String key = names.nextElement();
			String desc = labels.getProperty(key);
			assertFalse("value " + desc + " is key", desc.startsWith(key));
			assertNotNull("desc of " + key + " is null", desc);
			assertFalse(desc.equals(""));
		}
	}
}
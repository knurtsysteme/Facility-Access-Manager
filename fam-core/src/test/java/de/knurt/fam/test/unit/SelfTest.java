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
package de.knurt.fam.test.unit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class SelfTest {

	public SelfTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Test
	public void configTestDirectoryExists() {
		File f = new File(FamConnector.getConfigDirectory());
		assertTrue("please init test configuration in dir: " + f.getAbsoluteFile(), f.exists());
		assertTrue("cannot read test configuration dir: " + f.getAbsoluteFile(), f.canRead());
		String[] neededFilesInDir = { "calendarDefaultViews.xml", "facilitiesConfigured.xml", "facilitiesPoolAbstract.xml", "facilitiesPoolBookable.xml", "facilitiesPoolBookingRulesAbstract.xml", "facilitiesPoolNoneBookable.xml", "fam_global.conf", "lang.properties",
				"loader.xml", "logbooksConfigured.xml", "logbooksPool.xml", "mail.xml", "rolesAndRights.xml" };
		for (String neededFileInDir : neededFilesInDir) {
			File f2 = new File(f.getAbsoluteFile() + System.getProperty("file.separator") + "config" + System.getProperty("file.separator") + neededFileInDir);
			assertTrue("missing test configuration file: " + f2.getAbsoluteFile(), f2.exists());
			assertTrue("cannot test configuration file: " + f2.getAbsoluteFile(), f2.canRead());
		}
		String[] neededDirs = { "files", "template", "plugins" };
		for (String neededDir : neededDirs) {
			File f2 = new File(f.getAbsoluteFile() + System.getProperty("file.separator") + neededDir);
			assertTrue("missing test configuration dir: " + f2.getAbsoluteFile(), f2.exists());
			assertTrue("cannot test configuration dir: " + f2.getAbsoluteFile(), f2.canRead());
			assertTrue("is not a dir: " + f2.getAbsoluteFile(), f2.isDirectory());
		}
	}

	@Test
	public void needDemoConfig() {
		String assertionFailHint = "THIS IS NOT THE TEST CONFIGURATION";
		assertTrue(assertionFailHint, FamConnector.isDev());
		assertNotNull(assertionFailHint, FacilityConfigDao.facility("indoor"));
		assertFalse(assertionFailHint, FacilityConfigDao.bookable("playground"));
		assertTrue(assertionFailHint, FacilityConfigDao.bookable("sportsHall"));
		assertTrue(assertionFailHint, FacilityConfigDao.bookable("ballBath"));
		assertTrue(assertionFailHint, FacilityConfigDao.bookable("slide"));
		assertTrue(assertionFailHint, FacilityConfigDao.bookable("teetertotter"));
		assertTrue(assertionFailHint, FacilityConfigDao.bookable("schoolbus"));
		assertNotNull(assertionFailHint, FacilityConfigDao.facility(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_PARENT));
		assertTrue(assertionFailHint, FacilityConfigDao.bookable(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE));
		assertTrue(assertionFailHint, FacilityConfigDao.bookable(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_QUEUE));
	}
}

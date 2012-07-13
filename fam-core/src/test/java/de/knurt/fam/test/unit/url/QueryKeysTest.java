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
package de.knurt.fam.test.unit.url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class QueryKeysTest {

	/**
     *
     */
	public QueryKeysTest() {
	}

	/**
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpClass() throws Exception {
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
	@Test
	public void getFacilityKey() {
		QueryKeys query = new QueryKeys();
		assertNull(query.getFacilityKey());

		assertTrue(FacilityConfigDao.getInstance().keyExists(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE));

		query.setA(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE);
		assertEquals(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE, query.getA());
		assertEquals(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE, query.getFacilityKey());
	}

	/**
     *
     */
	@Test
	public void getLogbookKey() {
		QueryKeys query = new QueryKeys();
		assertNull(query.getLogbookKey());
		query.setE(TeztBeanSimpleFactory.LOGBOOK_ID);
		assertEquals(TeztBeanSimpleFactory.LOGBOOK_ID, query.getLogbookKey());
	}
}
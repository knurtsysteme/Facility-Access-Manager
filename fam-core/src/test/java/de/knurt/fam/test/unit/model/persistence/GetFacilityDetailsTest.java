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
package de.knurt.fam.test.unit.model.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.template.controller.json.GetFacilityDetailsController;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class GetFacilityDetailsTest {

	/**
     *
     */
	public GetFacilityDetailsTest() {
	}

	/**
	 * 
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	/**
	 * 
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	/**
     *
     */
	@Before
	public void setUp() {
	}

	/**
     *
     */
	@After
	public void tearDown() {
	}

	@Test
	public void correctParametersGiven() throws JSONException {
		MockHttpServletRequest mockrq = new MockHttpServletRequest();
		User user = TeztBeanSimpleFactory.getAdmin();
		Facility d = FacilityConfigDao.bookableFacility(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE);
		BookingRule br = FacilityConfigDao.bookingRule(d.getKey());
		String data = String.format("{\"key\":\"%s\"}", d.getKey());
		mockrq.setParameter("data", data);

		JSONObject json = new GetFacilityDetailsController(user).getJSONObject(mockrq, new MockHttpServletResponse());
		JSONObject jsonD = json.getJSONObject("facility");
		JSONObject jsonBr = json.getJSONObject("br");

		// request successed
		assertEquals(true, json.get("succ"));

		// all objects set
		assertTrue(json != null);
		assertTrue(jsonD != null);
		assertTrue(jsonBr != null);

		// values exist and equals originals
		assertEquals(d.getLabel(), jsonD.get("label"));
		assertEquals(d.getKey(), jsonD.get("key"));
		assertEquals(true, jsonD.get("bookable"));
		assertEquals(br.getBookingStrategy(), jsonBr.get("strategy"));
		assertEquals("Red Bus", jsonBr.get("capacity_label_singular"));
		assertEquals("Red Busses", jsonBr.get("capacity_label_plural"));
		assertEquals(br.getMaxBookableCapacityUnits(user), jsonBr.get("max_bookable_capacity_units"));
		assertEquals(br.getMaxBookableTimeUnits(user), jsonBr.get("max_bookable_time_units"));
		assertEquals(br.getMinBookableCapacityUnits(user), jsonBr.get("min_bookable_capacity_units"));
		assertEquals(br.getMaxBookableTimeUnits(user), jsonBr.get("max_bookable_time_units"));
	}

	/**
	 * @throws JSONException
	 * 
	 */
	@Test
	public void invalidParametersGiven() throws JSONException {
		MockHttpServletRequest mockrq = new MockHttpServletRequest();
		String data = "{\"key\":\"invalid\"}";
		mockrq.setParameter("data", data);
		JSONObject json = new GetFacilityDetailsController(TeztBeanSimpleFactory.getAdmin()).getJSONObject(mockrq, new MockHttpServletResponse());
		assertTrue(json != null);
		assertEquals(false, json.get("succ"));
	}

	/**
	 * @throws JSONException
	 * 
	 */
	@Test
	public void noParametersGiven() throws JSONException {
		JSONObject json = new GetFacilityDetailsController(TeztBeanSimpleFactory.getAdmin()).getJSONObject(new MockHttpServletRequest(), new MockHttpServletResponse());
		assertTrue(json != null);
		assertEquals(false, json.get("succ"));
	}
}
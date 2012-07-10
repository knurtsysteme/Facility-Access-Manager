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

import de.knurt.fam.template.controller.json.GetEventsController;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class GetEventsTest {

	/**
     *
     */
	public GetEventsTest() {
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
		String data = "{\"month\":3,\"year\":2011,\"day_of_month\":7,\"facility\":\"" + TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE + "\",\"capacity_units\":1}";
		mockrq.setParameter("data", data);
		JSONObject events = new GetEventsController(TeztBeanSimpleFactory.getAdmin()).getJSONObject(mockrq, new MockHttpServletResponse());
		assertTrue(events != null);
		assertEquals(true, events.get("succ"));
	}

	@Test
	public void failIfParameterCapacityUnitsOutOfBounce() throws JSONException {
		MockHttpServletRequest mockrq = new MockHttpServletRequest();
		String data = "{\"month\":3,\"year\":2011,\"day_of_month\":7,\"facility\":\"" + TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE + "\",\"capacity_units\":10000}";
		mockrq.setParameter("data", data);
		JSONObject events = new GetEventsController(TeztBeanSimpleFactory.getAdmin()).getJSONObject(mockrq, new MockHttpServletResponse());
		assertTrue(events != null);
		assertEquals(false, events.get("succ"));
	}

	@Test
	public void failIfParameterCapacityUnitIsMissed() throws JSONException {
		MockHttpServletRequest mockrq = new MockHttpServletRequest();
		String data = "{\"month\":3,\"year\":2011,\"day_of_month\":7,\"facility\":\"" + TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE + "\"}";
		mockrq.setParameter("data", data);
		JSONObject events = new GetEventsController(TeztBeanSimpleFactory.getAdmin()).getJSONObject(mockrq, new MockHttpServletResponse());
		assertTrue(events != null);
		assertEquals(false, events.get("succ"));
	}

	/**
	 * @throws JSONException
	 * 
	 */
	@Test
	public void invalidParametersGiven() throws JSONException {
		MockHttpServletRequest mockrq = new MockHttpServletRequest();
		String data = "invalid";
		mockrq.setParameter("data", data);
		JSONObject events = new GetEventsController(TeztBeanSimpleFactory.getAdmin()).getJSONObject(mockrq, new MockHttpServletResponse());
		assertTrue(events != null);
		assertEquals(false, events.get("succ"));
	}

	/**
	 * @throws JSONException
	 * 
	 */
	@Test
	public void noParametersGiven() throws JSONException {
		JSONObject events = new GetEventsController(TeztBeanSimpleFactory.getAdmin()).getJSONObject(new MockHttpServletRequest(), new MockHttpServletResponse());
		assertTrue(events != null);
		assertEquals(false, events.get("succ"));
	}

	/**
	 * assume that the count of events (e) is the time of month (mt) devided to
	 * 60 (as smallest unit shown in calendar): <code>e = mt / 60</code>
	 * 
	 * use April 2011 with 30 days + 5 days to complete the week (first day of
	 * week = monday). or (30 + 5) * 24 = 840 events
	 * 
	 * @throws JSONException
	 */
	@Test
	public void numberOfEventsAreCorrect() throws JSONException {
		MockHttpServletRequest mockrq = new MockHttpServletRequest();
		String data = "{\"month\":3,\"year\":2011,\"day_of_month\":7,\"facility\":\"" + TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE + "\",\"capacity_units\":1}";
		mockrq.setParameter("data", data);

		JSONObject events = new GetEventsController(TeztBeanSimpleFactory.getAdmin()).getJSONObject(mockrq, new MockHttpServletResponse());

		assertEquals(840, events.getJSONArray("events").length());
	}
}
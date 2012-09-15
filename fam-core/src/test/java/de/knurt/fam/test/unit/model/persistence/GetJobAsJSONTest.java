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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.document.Job;
import de.knurt.fam.template.controller.json.GetJobController;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class GetJobAsJSONTest extends FamIBatisTezt {
	@Test
	public void noInfoOnNoAuthUser() {
		GetJobController gjc = new GetJobController(null);
		MockHttpServletRequest rq = new MockHttpServletRequest();
		JSONObject got = gjc.getJSONObject(rq, null);
		assertNotNull(got);
		assertEquals(got.toString(), "{}");
	}

	@Test
	public void noInfoOnNotExistingId() {
		this.clearDatabase();

		// no job inserted here
		GetJobController gjc = new GetJobController(TeztBeanSimpleFactory.getAdmin());
		MockHttpServletRequest rq = new MockHttpServletRequest();
		rq.setParameter("id", "321");
		JSONObject got = gjc.getJSONObject(rq, null);
		assertNotNull(got);
		assertEquals(got.toString(), "{}");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getJobWithId() {
		this.clearDatabase();

		// ↘ this is the job
		JSONObject jobSurvey = new JSONObject();
		try {
			jobSurvey.put("foo", "bar");
			List<String> animals = new ArrayList<String>();
			animals.add("dog");
			animals.add("cat");
			animals.add("mouse");
			jobSurvey.put("zoo", animals);
		} catch (JSONException e) {
			fail("should not throw " + e);
		}

		// ↘ get job of this booking as json
		Booking booking = TeztBeanSimpleFactory.getNewValidBooking();
		booking.setBooked();
		booking.insert();
		Job document = new Job();
		document.setJobId(booking.getId());
		document.setUsername(booking.getUsername());
		document.setStep(0);
		document.setIdJobDataProcessing("foo");
		document.setJobSurvey(jobSurvey);
		assertTrue(document.insertOrUpdate());

		// ↘ get json stuff being tested
		GetJobController gjc = new GetJobController(booking.getUser());
		MockHttpServletRequest rq = new MockHttpServletRequest();
		rq.setParameter("id", booking.getId() + "");
		JSONObject got = gjc.getJSONObject(rq, null);

		// ↘ test results
		try {
			assertEquals("bar", got.getString("foo"));
			Map zoo = (Map) got.get("zoo");
			assertEquals("dog", zoo.get("0"));
			assertEquals("cat", zoo.get("1"));
			assertEquals("mouse", zoo.get("2"));
		} catch (JSONException e) {
			fail("should not throw " + e);
		}
	}
}
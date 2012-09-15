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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.Cancelation;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.template.controller.json.GetBookingsController;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class GetBookingsAsJSONTest extends FamIBatisTezt {
	@Test
	public void noInfoOnNoAuthUser() {
		GetBookingsController gbc = new GetBookingsController(null);
		MockHttpServletRequest rq = new MockHttpServletRequest();
		JSONObject got = gbc.getJSONObject(rq, null);
		assertNotNull(got);
		assertEquals(got.toString(), "{}");
	}

	/**
	 * get own bookings as json
	 */
	@Test
	public void ownBookings() {
		this.clearDatabase();

		// ↘ get this booking as json - even if canceled ...
		Booking booking = TeztBeanSimpleFactory.getNewValidBooking();
		booking.setBooked();
		booking.insert();
		booking.cancel(new Cancelation(booking.getUser(), Cancelation.REASON_NO_REASON));

		// ↘ ... and ignore the booking of other users.
		User otherUsers = TeztBeanSimpleFactory.getNewUniqueValidUser("other salt");
		otherUsers.insert();
		TimeBooking otherUsersBooking = TeztBeanSimpleFactory.getNewValidBooking();
		otherUsersBooking.setUsername(otherUsers.getUsername());
		otherUsersBooking.setBooked();
		otherUsersBooking.insert();

		// ↘ check what is prepared
		assertEquals(2, FamDaoProxy.bookingDao().getAll().size());

		// ↘ get json stuff being tested
		GetBookingsController gbc = new GetBookingsController(booking.getUser());
		MockHttpServletRequest rq = new MockHttpServletRequest();
		rq.setParameter("flag", "all-own");
		JSONObject got = gbc.getJSONObject(rq, null);

		// ↘ test results
		assertNotNull(got);
		assertFalse(got.isNull("bookings"));
		try {
			JSONArray gotBookings = got.getJSONArray("bookings");
			assertEquals(1, gotBookings.length());
			JSONObject gotBooking = gotBookings.getJSONObject(0);
			assertFalse(gotBooking.isNull("id"));
			assertEquals(gotBooking.getInt(("id")), booking.getId().intValue());
		} catch (JSONException e) {
			fail("should not throw " + e);
		}
	}
}
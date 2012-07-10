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
package de.knurt.fam.test.unit.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.fam.core.util.bu.PdfLetterFromBooking;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class LetterTest extends FamIBatisTezt {

	public LetterTest() {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Test
	public void getLetterForFacility() {
		this.clearDatabase();
		// create TimeBooking
		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		TimeBooking booking = TeztBeanSimpleFactory.getNewValidBooking(br);
		booking.setSeton(new Date());
		booking.setBooked();
		booking.insert();
		JSONObject letter = new PdfLetterFromBooking(TeztBeanSimpleFactory.getAdmin()).process(booking);
		assertNotNull(letter);
		assertTrue(letter.length() > 0);
	}
}

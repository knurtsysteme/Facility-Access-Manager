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
package de.knurt.fam.test.unit.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.view.text.QueryStringAdapter4Templates;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class QueryStringAdapter4TemplatesTest extends FamIBatisTezt {

	/**
     *
     */
	@Test
	public void testAdd() {
		this.clearDatabase();
		Properties assertp = new Properties();
		Booking b = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		b.setBooked();
		b.insert();
		try {
			QueryStringAdapter4Templates.add(b, assertp);
		} catch (NullPointerException e) {
			assertTrue("should not have thrown exception", false);
		}
		assertEquals(b.getUsername(), assertp.getProperty("user_username"));
	}
}
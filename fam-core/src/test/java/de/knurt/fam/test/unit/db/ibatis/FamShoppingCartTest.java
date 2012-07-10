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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;
import de.knurt.heinzelmann.util.shopping.Purchasable;
import de.knurt.heinzelmann.util.shopping.ShoppingCart;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class FamShoppingCartTest extends FamIBatisTezt {

	/**
     *
     */
	@Test
	public void accessFacilityLabels() {
		this.clearDatabase();
		User user = TeztBeanSimpleFactory.getNewValidUser();
		user.insert();
		ShoppingCart sc = user.getShoppingCart();
		assertNotNull(sc);

		TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
		sc.addArticle(br);

		Purchasable pBack = sc.getArticles().get(br.getArticleNumber());
		assertNotNull(pBack);
		assertEquals(TimeBookingRequest.class, pBack.getClass());

		TimeBookingRequest brBack = (TimeBookingRequest) pBack;
		assertEquals(br.getFacility().getLabel(), brBack.getFacility().getLabel());
		assertEquals(br.getRequestedTimeUnits(), brBack.getRequestedTimeUnits());
		assertEquals(br.getRequestedStartTime() + "", brBack.getRequestedStartTime() + "");
		assertEquals(br.getRequestedTimeFrame() + "", brBack.getRequestedTimeFrame() + "");
		assertEquals(br.getUser().getUsername(), brBack.getUser().getUsername());
		assertEquals(br.getRequestedCapacityUnits(), brBack.getRequestedCapacityUnits());

		// some debug code to get sure not to block other users from booking
		assertEquals(0, FamDaoProxy.bookingDao().getAll().size());
		assertTrue(br.isValidRequest());
		assertTrue(TimeBooking.getNewBooking(brBack).isAvailableForInsertion());
	}

}
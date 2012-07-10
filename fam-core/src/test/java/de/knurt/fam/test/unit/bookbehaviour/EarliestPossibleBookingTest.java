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
package de.knurt.fam.test.unit.bookbehaviour;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.config.TimeBasedBookingRule;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * @since 07/26/2010
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class EarliestPossibleBookingTest extends FamIBatisTezt {

	@Test
	public void earliestPossiblityToBookFromNowIsTimeBookingRule() {
		this.clearDatabase();
		try {
			TimeBasedBookingRule br = (TimeBasedBookingRule) TeztBeanSimpleFactory.getFacilityBookable().getBookingRule();
			assertEquals(2880, br.getEarliestPossibilityToBookFromNow());
		} catch (ClassCastException e) {
			fail("booking rule is not time based");
		}
	}

	@Test
	public void externMustWait() {
		this.clearDatabase();
		FacilityBookable bd = TeztBeanSimpleFactory.getFacilityBookable();
		User extern = TeztBeanSimpleFactory.getNewValidUser();
		assertEquals("extern", extern.getRoleId());
		int got = FamAuth.getEarliestPossibilityToBookFromNow(extern, bd);
		assertEquals(2880, got);
	}

	@Test
	public void externCanNotBookWithoutWaiting() {
		this.clearDatabase();
		User extern = TeztBeanSimpleFactory.getNewValidUser();
		assertEquals("extern", extern.getRoleId());
		TimeBookingRequest tbr = TeztBeanSimpleFactory.getBookingRequest();
		tbr.setUser(extern);
		extern.insert();
		tbr.setRequestedStartTime(Calendar.getInstance());
		assertFalse("booking starting should NOT be available for extern", tbr.isAvailable());
		tbr.setRequestedStartTime(TeztBeanSimpleFactory.getTomorrow());
		assertFalse("booking starting should NOT be available for extern", tbr.isAvailable());
	}

	@Test
	public void internMustNotWait() {
		this.clearDatabase();
		FacilityBookable bd = TeztBeanSimpleFactory.getFacilityBookable();
		User intern = TeztBeanSimpleFactory.getNewValidUser();
		intern.setRoleId("intern");
		assertEquals("intern", intern.getRoleId());
		int got = FamAuth.getEarliestPossibilityToBookFromNow(intern, bd);
		assertEquals(0, got);
	}

	@Test
	public void internCanBookWithoutWaiting() {
		this.clearDatabase();
		User intern = TeztBeanSimpleFactory.getNewValidUser();
		intern.setRoleId("intern");
		intern.insert();
		TimeBookingRequest tbr = TeztBeanSimpleFactory.getBookingRequest();
		tbr.setUser(intern);
		tbr.setRequestedStartTime(TeztBeanSimpleFactory.getTomorrow());
		assertTrue("booking starting should be available for intern", tbr.isAvailable());
	}

	@Test
	public void adminMustNotWait() {
		this.clearDatabase();
		FacilityBookable bd = TeztBeanSimpleFactory.getFacilityBookable();
		int got = FamAuth.getEarliestPossibilityToBookFromNow(TeztBeanSimpleFactory.getAdmin(), bd);
		assertEquals(0, got);
	}

	@Test
	public void adminCanBookWithoutWaiting() {
		this.clearDatabase();
		TimeBookingRequest tbr = TeztBeanSimpleFactory.getBookingRequest();
		User admin = TeztBeanSimpleFactory.getAdmin();
		admin.insert();
		tbr.setUser(admin);
		tbr.setRequestedStartTime(TeztBeanSimpleFactory.getTomorrow());
		assertTrue("booking starting should be available for admin", tbr.isAvailable());
	}
}

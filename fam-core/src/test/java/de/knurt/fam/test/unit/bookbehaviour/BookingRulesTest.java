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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.config.SetOfRulesForARole;
import de.knurt.fam.core.model.config.TimeBasedBookingRule;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class BookingRulesTest extends FamIBatisTezt {

	/**
     *
     */
	@Test
	public void facilityHasBookingRule() {
		FacilityBookable massspectro = TeztBeanSimpleFactory.getFacilityBookable();
		BookingRule br = massspectro.getBookingRule();
		assertEquals(TimeBasedBookingRule.class, br.getClass());
	}

	@Test
	public void internUsersHaveAnotherMaxBookableCapacityUnit() {
		User intern = UserFactory.me().blank();
		intern.setRoleId("intern");
		User extern = UserFactory.me().blank();
		extern.setRoleId("extern");

		FacilityBookable schoolbus = (FacilityBookable) FacilityConfigDao.getInstance().getConfiguredInstance("schoolbus");
		SetOfRulesForARole defaultR4R = schoolbus.getBookingRule().getDefaultSetOfRulesForARole();
		SetOfRulesForARole r4Interns = schoolbus.getBookingRule().getSetOfRulesForARole(intern);
		SetOfRulesForARole r4Externs = schoolbus.getBookingRule().getSetOfRulesForARole(extern);

		// test intern may take 5 capacity units. others 3 units.
		assertEquals(3, defaultR4R.getMaxBookableCapacityUnits());
		assertEquals(8, r4Interns.getMaxBookableCapacityUnits());
		assertEquals(3, r4Externs.getMaxBookableCapacityUnits());

		// test extern has to take it for at least 5 time units. others 1 time
		// unit.
		assertEquals(1, defaultR4R.getMinBookableTimeUnits());
		assertEquals(1, r4Interns.getMinBookableTimeUnits());
		assertEquals(5, r4Externs.getMinBookableTimeUnits());
	}

	/**
     *
     */
	@Test
	public void isBookable() {
		assertTrue(FacilityConfigDao.bookable(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE));
		assertFalse(FacilityConfigDao.bookable(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_PARENT));
	}
}

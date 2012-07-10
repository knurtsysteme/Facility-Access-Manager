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
package de.knurt.fam.test.unit.aspects;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class ViewPageTest extends FamIBatisTezt {

	/**
     *
     */
	@Test
	public void testRightToViewPageOfSomeViews() {
		this.clearDatabase();
		User extern = TeztBeanSimpleFactory.getNewValidUser();
		User admin = TeztBeanSimpleFactory.getAdmin();
		User operator = TeztBeanSimpleFactory.getNewValidUser();
		operator.setRoleId("operator");

		assertTrue(admin.hasRight2ViewPage("home"));
		assertTrue(extern.hasRight2ViewPage("home"));
		assertTrue(operator.hasRight2ViewPage("home"));

		assertTrue(admin.hasRight2ViewPage("facilityemergency"));
		assertFalse(extern.hasRight2ViewPage("facilityemergency"));
		assertTrue(operator.hasRight2ViewPage("facilityemergency"));

		assertTrue(admin.hasRight2ViewPage("systemmodifyusers"));
		assertFalse(extern.hasRight2ViewPage("systemmodifyusers"));
		assertFalse(operator.hasRight2ViewPage("systemmodifyusers"));
	}
}
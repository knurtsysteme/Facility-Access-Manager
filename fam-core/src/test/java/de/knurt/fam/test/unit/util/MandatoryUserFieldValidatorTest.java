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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.Address;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.mvc.validator.InvalidRoleIdException;
import de.knurt.fam.core.util.mvc.validator.MandatoryUserFieldValidator;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class MandatoryUserFieldValidatorTest {

	public MandatoryUserFieldValidatorTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Test
	public void testConstruction() {
		assertEquals(MandatoryUserFieldValidator.class, MandatoryUserFieldValidator.getInstance().getClass());
	}

	/**
	 * assume configuration es given in security.xml bean2010310903
	 */
	@Test
	public void testConfiguration() {
		User extern = new User();
		extern.setRoleId("extern");
		User intern = new User();
		intern.setRoleId("intern");

		MandatoryUserFieldValidator mufv = MandatoryUserFieldValidator.getInstance();
		try {
			assertTrue(mufv.isMandatory(extern, "fname"));
			assertFalse(mufv.isSufficient(extern, "fname"));
			assertTrue(mufv.isMandatory(intern, "fname"));
			assertFalse(mufv.isSufficient(intern, "fname"));

			assertFalse(mufv.isMandatory(extern, "intendedResearch"));
			assertTrue(mufv.isSufficient(extern, "intendedResearch"));
			assertTrue(mufv.isMandatory(intern, "intendedResearch"));
			assertFalse(mufv.isSufficient(intern, "intendedResearch"));

			assertFalse(mufv.isMandatory(extern, "company"));
			assertTrue(mufv.isSufficient(extern, "company"));
			assertTrue(mufv.isMandatory(intern, "company"));
			assertFalse(mufv.isSufficient(intern, "company"));

			assertTrue(mufv.isMandatory(extern, "sname"));
			assertFalse(mufv.isSufficient(extern, "sname"));
			assertTrue(mufv.isMandatory(intern, "sname"));
			assertFalse(mufv.isSufficient(intern, "sname"));

			assertFalse(mufv.isMandatory(extern, "oneofphones"));
			assertTrue(mufv.isSufficient(extern, "oneofphones"));
			assertTrue(mufv.isMandatory(intern, "oneofphones"));
			assertFalse(mufv.isSufficient(intern, "oneofphones"));

			assertFalse(mufv.isMandatory(extern, "address"));
			assertTrue(mufv.isSufficient(extern, "address"));
			assertTrue(mufv.isMandatory(intern, "address"));
			assertFalse(mufv.isSufficient(intern, "address"));

			// phone
			intern.setPhone1("foo");
			assertTrue(mufv.isSufficient(intern, "oneofphones"));
			intern.setPhone1("");
			assertFalse(mufv.isSufficient(intern, "oneofphones"));
			intern.setPhone2("");
			assertFalse(mufv.isSufficient(intern, "oneofphones"));
			intern.setPhone2("234");
			assertTrue(mufv.isSufficient(intern, "oneofphones"));

			// address
			Address add = new Address();
			intern.setMainAddress(add);
			assertFalse(mufv.isSufficient(intern, "address"));
			add.setCity("city");
			intern.setMainAddress(add);
			assertFalse(mufv.isSufficient(intern, "address"));
			add.setStreet("street");
			add.setCountry("country");
			add.setStreetno("streetno");
			add.setZipcode("zip");
			extern.setMainAddress(add);
			assertTrue(mufv.isSufficient(intern, "address"));
		} catch (InvalidRoleIdException e) {
			fail("should not have been thrown");
		}
	}

	@Test
	public void testSufficient() {
		User user = new User();
		assertNull(user.getFname());
		user.setRoleId("extern");
		assertEquals("extern", user.getRoleId());
		try {
			MandatoryUserFieldValidator mufv = MandatoryUserFieldValidator.getInstance();
			assertTrue(mufv.isMandatory(user, "fname"));
			assertFalse(mufv.isSufficient(user, "fname"));
			user.setFname("    ");
			assertTrue(mufv.isMandatory(user, "fname"));
			assertFalse(mufv.isSufficient(user, "fname"));
			user.setFname("peter");
			assertTrue(mufv.isMandatory(user, "fname"));
			assertTrue(mufv.isSufficient(user, "fname"));
		} catch (InvalidRoleIdException e) {
			fail("should not have been thrown");
		}

	}
}

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
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.Address;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.mvc.Registration;
import de.knurt.fam.test.utils.FamIBatisTezt;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class RegistrationTest extends FamIBatisTezt {

	/**
	 * Test of getUser method, of class Registration.
	 */
	@Test
	public void testGetUser() {
		this.clearDatabase();
		Registration reg = new Registration();
		User result = reg.getUser();

		// birthdate
		reg.setBirthdate("24032009");
		result = reg.getUser();
		assertEquals("Tue Mar 24 00:00:00 CET 2009", result.getBirthdate().toString());
		// male
		reg.setMale("1");
		result = reg.getUser();
		assertEquals(true, result.isMale());
		reg.setMale("0");
		result = reg.getUser();
		assertEquals(false, result.isMale());
		// simple string values
		reg.setTitle("King of Asia");
		reg.setCompany("Asia");
		reg.setPhone1("110");
		reg.setPhone2("112");
		reg.setMail("foo@bar.foo");
		reg.setFname("fname");
		reg.setSname("sname");
		result = reg.getUser();
		assertEquals("King of Asia", result.getTitle());
		assertEquals("Asia", result.getCompany());
		assertEquals("110", result.getPhone1());
		assertEquals("112", result.getPhone2());
		assertEquals("foo@bar.foo", result.getMail());
		assertEquals("fname", result.getFname());
		assertEquals("sname", result.getSname());
		assertEquals("fnsname", result.getUsername());
		// address
		assertNull(result.getMainAddress());
		reg.setCity("foo town");
		result = reg.getUser();
		assertNotNull(result.getMainAddress());
		reg.setStreet("foo street");
		reg.setStreetno("3");
		reg.setZipcode("123");
		reg.setCountry("foo country");
		Address addres = reg.getUser().getMainAddress();
		assertEquals("foo town", addres.getCity());
		assertEquals("foo street", addres.getStreet());
		assertEquals("3", addres.getStreetno());
		assertEquals("123", addres.getZipcode());
		assertEquals("foo country", addres.getCountry());
		// password
		String pass = "abcABC02";
		reg.setPass1(pass);
		reg.setPass2(pass);
		result = reg.getUser();
		assertNotNull(result.getPassword());

	}
}
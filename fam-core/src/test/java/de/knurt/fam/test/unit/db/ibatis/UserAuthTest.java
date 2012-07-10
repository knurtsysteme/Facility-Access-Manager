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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.mvc.Login;
import de.knurt.fam.core.util.mvc.Registration;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class UserAuthTest extends FamIBatisTezt {

	/**
	 * test users born before 1970
	 */
	@Test
	public void testSomehowOldUser() { // hey, i'm 1976
		this.clearDatabase();
		User user = TeztBeanSimpleFactory.getNewValidUser();
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 1960);
		user.setBirthdate(c.getTime());
		user.insert();
	}

	/**
     *
     */
	@Test
	public void testPassword() {
		Registration r = new Registration();
		String pass1 = null;
		String pass2 = null;
		String expected = null;
		r.setPass1(pass1);
		r.setPass2(pass2);
		String result = r.getPassword();
		assertNull(result);

		// valid pass
		pass1 = "Super_good_Password_9898sdf98#+#_+23DSfh7h+234";
		pass2 = pass1;
		expected = pass1;
		r.setPass1(pass1);
		r.setPass2(pass2);
		result = r.getPassword();
		assertEquals(expected, result);

		// different passes
		pass1 = "Super_good_Password_9898sdf98#+#_+23DSfh7h+234";
		pass2 = "ups";
		r.setPass1(pass1);
		r.setPass2(pass2);
		result = r.getPassword();
		assertNull(result);
	}

	/**
     *
     */
	@Test
	public void testDepartmentKeyWithoutLabelIsUnknown() {
		Registration r = new Registration();
		r.setDepartmentKey("foo");
		assertEquals("unknown", r.getDepartmentKey());
	}

	@Test
	public void testRegistrationValidator_birthdate() {
		Registration r = new Registration();
		r.setBirthdate("asdf");
		assertNull(r.getBirtdateAsDate());

		String input = "13.12.2008";
		r.setBirthdate(input);
		Date result = r.getBirtdateAsDate();
		String expected = "Sat Dec 13 00:00:00 CET 2008";
		assertNotNull(result);
		assertEquals(expected, result.toString());

		// be lenient in the input
		input = "asdf1s3/12sdf20d08"; // somehow a date
		r.setBirthdate(input);
		result = r.getBirtdateAsDate();
		expected = "Sat Dec 13 00:00:00 CET 2008";
		assertNotNull(result);
		assertEquals(expected, result.toString());

		// invalid dates
		r.setBirthdate("18.03.1809"); // person should not be older then 200
		// years
		assertNull(r.getBirtdateAsDate());
		r.setBirthdate("31.02.2008"); // invalid date
		assertNull(r.getBirtdateAsDate());
		r.setBirthdate("01.02.3100"); // person should not be born in future
		result = r.getBirtdateAsDate();
		assertNull(result);
	}

	/**
	 * Test of isAuth method, of class User.
	 */
	@Test
	public void testIsAuth() {
		this.clearDatabase();

		User peter = TeztBeanSimpleFactory.getNewValidUser();
		String petersRawPass = peter.getPassword();
		assertEquals(false, peter.isAuth());
		FamDaoProxy.getInstance().getUserDao().insert(peter);

		peter.setCleanPassword(petersRawPass);
		assertEquals(true, peter.isAuth());

		User pwthief = new User();
		assertEquals(false, pwthief.isAuth());
		pwthief.setPassword(petersRawPass);
		pwthief.setUsername(peter.getUsername());
		assertFalse(pwthief.getPassword().equals(peter.getPassword()));
		assertEquals(pwthief.getUsername(), peter.getUsername());
		assertEquals(true, pwthief.isAuth());
	}

	@Test
	public void testAuthOnAccountExpiring() {
		this.clearDatabase();

		User peter = TeztBeanSimpleFactory.getNewValidUser();
		String petersRawPass = peter.getPassword();
		FamDaoProxy.getInstance().getUserDao().insert(peter);
		peter.setCleanPassword(petersRawPass);
		assertTrue(peter.isAuth());
		
		Calendar expiring = Calendar.getInstance();
		expiring.add(Calendar.YEAR, 1);
		peter.setAccountExpires(expiring.getTime());
		peter.update();
		peter.setAccountNeverExpires(); // set it to null
		peter.setCleanPassword(petersRawPass);
		assertTrue(peter.isAuth());

		expiring.add(Calendar.YEAR, -2);
		peter.setAccountExpires(expiring.getTime());
		peter.update();
		peter.setAccountNeverExpires(); // set it to null
		peter.setCleanPassword(petersRawPass);
		assertFalse(peter.isAuth());
	}
	
	/**
     *
     */
	@Test
	public void testLogin() {
		this.clearDatabase();
		User peter = TeztBeanSimpleFactory.getNewValidUser();
		String cleanpass = peter.getPassword();
		FamDaoProxy.getInstance().getUserDao().insert(peter);
		peter.setCleanPassword(cleanpass);
		assertTrue(peter.isAuth());
		List<User> ls = FamDaoProxy.getInstance().getUserDao().getObjectsLike(peter);
		String encpass = ls.get(0).getPassword();
		assertFalse(cleanpass.equals(encpass));
		assertTrue(peter.getPassword().equals(encpass));

		// check login
		Login login = new Login();
		assertTrue("no input values succeeded", login.fail());
		assertTrue("login with email and no pass", login.fail());

		login.setPassword(cleanpass);
		assertTrue("login should be valid", login.fail());

		login = new Login();
		login.setUsername(peter.getUsername());
		assertTrue(login.fail());

		login = new Login();
		login.setPassword(cleanpass);
		login.setUsername(peter.getUsername());
		assertFalse(login.fail());
	}
}
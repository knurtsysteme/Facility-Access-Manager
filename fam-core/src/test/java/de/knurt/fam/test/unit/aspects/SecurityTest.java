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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.aspects.security.encoder.FamCookiePassEncoderControl;
import de.knurt.fam.core.aspects.security.encoder.FamTmpAccessEncoderControl;
import de.knurt.fam.core.aspects.security.encoder.FamUserPassEncoderControl;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.RoleConfigDao;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.test.utils.AssertSomehowEquals;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class SecurityTest extends FamIBatisTezt {

	/**
     *
     */
	@Test
	public void getEncoder() {
		User user = UserFactory.me().blank();
		String teststring = "abc";
		user.setPassword(teststring);
		user.setMail("da@da.da");

		String encPass_1 = FamCookiePassEncoderControl.getInstance().encodePassword(user);
		assertFalse(encPass_1.isEmpty());
		assertFalse(encPass_1.equals(teststring));
		assertTrue(encPass_1, encPass_1.length() > 8);
		assertFalse(encPass_1.equals(user.getPassword() + user.getMail()));

		// encode it again
		String encPass_2 = FamUserPassEncoderControl.getInstance().encodePassword(user);
		assertFalse(encPass_2.isEmpty());
		assertFalse(teststring, encPass_2.equals(teststring));
		assertTrue(encPass_2, encPass_2.length() > 8);
		assertFalse(encPass_2.equals(user.getPassword() + user.getMail()));

		String encPass_3 = FamTmpAccessEncoderControl.getInstance().encodePassword(user);
		assertFalse(encPass_3.isEmpty());
		assertFalse(encPass_3.equals(teststring));
		assertTrue(encPass_3, encPass_3.length() > 8);
		assertFalse(encPass_3.equals(user.getPassword() + user.getMail()));
	}

	/**
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void encodedAfterAuth() throws NoSuchAlgorithmException {
		this.clearDatabase();

		User u1 = TeztBeanSimpleFactory.getNewValidUser();
		u1.setPassword("foobar");

		assertFalse(u1.isPasswordEncoded());
		assertTrue(u1.getPassword().equals("foobar"));
		assertFalse(u1.isAuth());
		assertTrue(u1.isPasswordEncoded());
		assertFalse(u1.getPassword().equals("foobar"));
		String encPass1 = u1.getPassword();
		u1.encodePassword();
		String encPass2 = u1.getPassword();
		assertEquals(encPass1, encPass2);
	}

	/**
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void encodedAfterStoring() throws NoSuchAlgorithmException {
		this.clearDatabase();

		User u1 = TeztBeanSimpleFactory.getNewValidUser();
		u1.setPassword("foobar");

		assertFalse(u1.isPasswordEncoded());
		assertTrue(u1.getPassword().equals("foobar"));
		FamDaoProxy.getInstance().getUserDao().insert(u1);
		assertTrue(u1.isPasswordEncoded());
		assertFalse(u1.getPassword().equals("foobar"));
	}

	/**
      *
      */
	@Test
	public void encoded() {
		User user = UserFactory.me().blank();
		user.setPassword("a");
		user.encodePassword();
		assertTrue(user.isPasswordEncoded());
	}

	/**
      *
      */
	@Test
	public void getTmpEncrypter() {
		User user = UserFactory.me().blank();
		String teststring = "abc";
		user.setUsername(teststring);
		String enc = FamTmpAccessEncoderControl.getInstance().encodePassword(user);
		assertFalse(enc.isEmpty());
		assertFalse(enc.equals(teststring));
		assertTrue(enc, enc.length() > 8);
		assertFalse(enc.equals(user.getPassword() + user.getMail()));
		assertTrue(enc.endsWith("_" + teststring));
	}

	/**
      *
      */
	@Test
	public void isSecure() {
		User user = UserFactory.me().blank();
		String teststring = "abc";
		user.setUsername(teststring);
		String enc = FamTmpAccessEncoderControl.getInstance().encodePassword(user);
		enc = enc.substring(0, 20);
		assertTrue(enc, enc.matches(".*[a-zA-Z].*")); // contains chars
		assertTrue(enc, enc.matches(".*[0-9].*")); // contains numbers
	}

	/**
      *
      */
	@Test
	public void getTmpDecoder() {
		this.clearDatabase();
		// store the user
		User user = TeztBeanSimpleFactory.getNewUniqueValidUser("getTmpDecoder");
		FamDaoProxy.getInstance().getUserDao().insert(user);

		// get encrypted tmp pass
		String enc = FamTmpAccessEncoderControl.getInstance().encodePassword(user);

		// get user from this pass
		User got = FamTmpAccessEncoderControl.getInstance().getUser(enc);
		AssertSomehowEquals.test(user, got);
		assertTrue(FamTmpAccessEncoderControl.getInstance().isPasswordValid(enc));

		// same user, other code
		got = FamTmpAccessEncoderControl.getInstance().getUser("foo" + enc);
		assertNull(got);
		assertFalse(FamTmpAccessEncoderControl.getInstance().isPasswordValid("foo" + enc));

		// other user, same code
		got = FamTmpAccessEncoderControl.getInstance().getUser(enc + "foo");
		assertNull(got);
		assertFalse(FamTmpAccessEncoderControl.getInstance().isPasswordValid(enc + "foo"));

		// the null test
		got = FamTmpAccessEncoderControl.getInstance().getUser(null);
		assertNull(got);
		assertFalse(FamTmpAccessEncoderControl.getInstance().isPasswordValid(null));
	}

	/**
      *
      */
	@Test
	public void usersRights() {
		User u = TeztBeanSimpleFactory.getAdmin();
		assertTrue(u.isAdmin());
		u.setRoleId("intern");
		assertFalse(u.isAdmin());
		u.setRoleId(RoleConfigDao.getInstance().getAdminId());
		assertTrue(u.isAdmin());
		String adminUsername = u.getUsername();
		u.setUsername("noneAdmin");
		assertFalse(u.isAdmin());
		u.setUsername(adminUsername);
		assertTrue(u.isAdmin());
	}

	/**
      *
      */
	@Test
	public void setUsersRights() {
		User u = UserFactory.me().blank();
		u.setRoleId("not a valid right");
		assertNull(u.getRoleId());
	}
}
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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.Address;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.test.utils.AssertSomehowEquals;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * insert and update is doing in on operation "store" in db4o - so only
 * inserting is not seen as a problem here.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class ReadWriteUserTest extends FamIBatisTezt {

	/**
     *
     */
	@Test
	public void excludedIsSetRight() {
		this.clearDatabase();
		User u = TeztBeanSimpleFactory.getNewValidUser();
		u.insert();

		assertTrue(u.hasVarifiedActiveAccount());

		User example = new User();
		example.setUsername(u.getUsername());
		example.setMail(u.getMail());

		User got = FamDaoProxy.userDao().getObjectsLike(example).get(0);
		assertTrue(got.hasVarifiedActiveAccount());

	}

	@Test
	public void writeReadAccountExpiring() {
		this.clearDatabase();
		User u = TeztBeanSimpleFactory.getNewValidUser();
		Calendar now = Calendar.getInstance();
		u.setAccountExpires(now.getTime());
		u.insert();

		User gotBack = FamDaoProxy.userDao().getUserFromUsername(u.getUsername());
		String back = FamDateFormat.getCustomDate(gotBack.getAccountExpires(), "MM/dd/yyyy");
		String today = FamDateFormat.getCustomDate(now.getTime(), "MM/dd/yyyy");
		assertEquals(back, today);
	}

	/**
     *
     */
	@Test
	public void specialChars() {
		this.clearDatabase();
		User test = TeztBeanSimpleFactory.getNewUniqueValidUser("1");
		test.setFname("Ötze");
		test.setSname("Müller");
		test.setUsername(null);
		test.setUniqueUsernameForInsertion();
		test.insert();

		User got = FamDaoProxy.userDao().getAll().get(0);
		assertEquals("Ötze", got.getFname());
		assertEquals("Müller", got.getSname());
		assertEquals("oemuelle", got.getUsername());
	}

	/**
     *
     */
	@Test
	public void uniqueUsername() {
		this.clearDatabase();
		String ununiqueUsername = "daoltman";
		User u1 = TeztBeanSimpleFactory.getNewUniqueValidUser("1");
		u1.setUsername(ununiqueUsername);
		u1.insert();

		User u2 = TeztBeanSimpleFactory.getNewUniqueValidUser("1");
		u2.setUsername(ununiqueUsername);
		assertEquals(u1.getUsername(), u2.getUsername());
		u2.setUniqueUsernameForInsertion();
		assertNotSame(u1.getUsername(), u2.getUsername());
		assertFalse(u2.getUsername().equals(ununiqueUsername));
		assertEquals("daoltma1", u2.getUsername());
	}

	/**
     *
     */
	@Test
	public void userHasAddress() {
		this.clearDatabase();
		User u = TeztBeanSimpleFactory.getNewUniqueValidUser("id");
		Address a = new Address();
		a.setCity("city of foo");
		u.setMainAddress(a);
		u.insert();
		List<User> all = FamDaoProxy.getInstance().getUserDao().getAll();
		assertNotNull(all.get(0));
		assertNotNull(all.get(0).getMainAddress());
		assertNotNull(all.get(0).getMainAddress().getCity());
		assertEquals("city of foo", all.get(0).getMainAddress().getCity());
		assertNotNull(all.get(0).getMainAddress().getId());
		assertNotNull(all.get(0).getMainAddressId());
	}

	/**
     *
     */
	@Test
	public void excludeUser() {
		this.clearDatabase();
		User u = TeztBeanSimpleFactory.getNewUniqueValidUser("id");
		u.insert();

		List<User> all = FamDaoProxy.getInstance().getUserDao().getAll();
		List<User> actives = FamDaoProxy.getInstance().getUserDao().getNotExcludedUsersWithAccount();
		assertEquals(1, all.size());
		assertEquals(1, actives.size());

		u.exclude();
		u.update();

		all = FamDaoProxy.getInstance().getUserDao().getAll();
		actives = FamDaoProxy.getInstance().getUserDao().getNotExcludedUsersWithAccount();
		assertEquals(1, all.size());
		assertEquals(0, actives.size());
	}

	@Test
	public void writeUserDirectly() {
		this.clearDatabase();
		User u = TeztBeanSimpleFactory.getNewUniqueValidUser("id");
		u.insert();
		User got = FamDaoProxy.getInstance().getUserDao().getOneLike(u);
		AssertSomehowEquals.test(u, got);
	}

	@Test
	public void readWriteIntendedResearchSuccess() {
		this.clearDatabase();
		User u = TeztBeanSimpleFactory.getNewUniqueValidUser("id");
		String intendedResearch = "foo";
		u.setIntendedResearch(intendedResearch);
		u.insert();
		User got = FamDaoProxy.userDao().getUserFromUsername(u.getUsername());
		assertEquals("foo", got.getIntendedResearch());
	}

	@Test
	public void storeUserWithSamePassesAndNamesAndEmails() {
		this.clearDatabase();
		User u = TeztBeanSimpleFactory.getNewValidUser();
		FamDaoProxy.userDao().insert(u);
		User copy = TeztBeanSimpleFactory.getNewUniqueValidUser("copy");
		try {
			copy.setMail(u.getMail());
			FamDaoProxy.userDao().insert(copy);
			fail("should have thrown DataIntegrityViolationException");
		} catch (DataIntegrityViolationException dive) {
			assertTrue("right exception", true);
		}
		try {
			copy.setMail("old@as.as");
			copy.setUsername(u.getUsername());
			FamDaoProxy.userDao().insert(copy);
			fail("should have thrown DataIntegrityViolationException");
		} catch (DataIntegrityViolationException dive) {
			assertTrue("right exception", true);
		}

	}

	/**
	 * 
	 * @throws java.lang.Exception
	 */
	@Test
	public void getUser() throws Exception {
		this.clearDatabase();
		User exampleuser = new User();
		exampleuser.setFname("peter");
		exampleuser.setSname("mueller");

		User testuser1 = new User();
		testuser1.setFname("peter");
		testuser1.setSname("mueller");
		testuser1.setMail("mail_2@la.le");
		testuser1.setStandardUser();
		testuser1.setUsername("foo");
		testuser1.setPassword("foo");
		testuser1.setRoleId("extern");

		List<User> users = FamDaoProxy.userDao().getObjectsLike(exampleuser);
		assertTrue(users.size() == 0);

		FamDaoProxy.userDao().insert(testuser1);

		users = FamDaoProxy.userDao().getObjectsLike(exampleuser);
		assertTrue(users.size() == 1);

		// store 2nd object with same name
		User testuser2 = new User();
		testuser2.setFname("peter");
		testuser2.setSname("mueller");
		testuser2.setMail("mail_1@la.le");
		testuser2.setStandardUser();
		testuser2.setUsername("bar");
		testuser2.setPassword("foo");
		testuser2.setRoleId("extern");
		FamDaoProxy.userDao().insert(testuser2);
		users = FamDaoProxy.userDao().getObjectsLike(exampleuser);
		assertTrue(users.size() == 2);
	}

	/**
     *
     */
	@Test
	public void getSameUser() {
		this.clearDatabase();
		User u1 = TeztBeanSimpleFactory.getNewValidUser();
		FamDaoProxy.userDao().insert(u1);
		User uc1 = FamDaoProxy.getInstance().getUserDao().getObjectsLike(u1).get(0);
		AssertSomehowEquals.test(u1, uc1);

		// now want to update
		u1.setLastLogin(new Date());
		try {
			FamDaoProxy.userDao().update(u1);
		} catch (DataIntegrityViolationException dive) {
			fail("should NOT have thrown DataIntegrityViolationException: " + dive.getMessage());
		}
	}

	/**
     *
     */
	@Test
	public void insertUser() {
		this.clearDatabase();
		User testuser = new User();
		List<User> users = FamDaoProxy.userDao().getObjectsLike(testuser);
		assertTrue(users.size() == 0);

		try {
			FamDaoProxy.userDao().insert(testuser);
			fail("should have thrown DataIntegrityViolationException");
		} catch (DataIntegrityViolationException dive) {
		}

		// a integrity user
		testuser.setMail("as@as.as");
		testuser.setUsername("username");
		testuser.setPassword("lalelu");
		testuser.setRoleId("extern");
		testuser.setStandardUser();
		try {
			FamDaoProxy.userDao().insert(testuser);
		} catch (DataIntegrityViolationException dive) {
			fail("should NOT have thrown DataIntegrityViolationException: " + dive.getMessage());
		}

		// try user with one thing missed
		testuser.setMail(null);
		try {
			FamDaoProxy.userDao().insert(testuser);
			fail("should have thrown DataIntegrityViolationException");
		} catch (DataIntegrityViolationException dive) {
		}
		testuser.setMail("as@as.as");

		// try user with one thing missed
		testuser.setUsername(null);
		try {
			FamDaoProxy.userDao().insert(testuser);
			fail("should have thrown DataIntegrityViolationException");
		} catch (DataIntegrityViolationException dive) {
		}
		testuser.setUsername("username");

		// try user with one thing missed
		String roleIdBefore = testuser.getRoleId();
		testuser.setRoleId(null); // should fail to set
		assertEquals(roleIdBefore, testuser.getRoleId());

		// try user with one thing missed
		testuser.setRegistration(null);
		try {
			FamDaoProxy.userDao().insert(testuser);
			fail("should have thrown DataIntegrityViolationException");
		} catch (DataIntegrityViolationException dive) {
		}
		testuser.setRegistration(new Date());

		// try user with missing password
		testuser.setPassword(null);
		try {
			FamDaoProxy.userDao().insert(testuser);
			fail("should have thrown DataIntegrityViolationException");
		} catch (DataIntegrityViolationException dive) {
		}
		testuser.setPassword("lalelu");

		try {
			testuser.setUsername(testuser.getUsername() + "a");
			testuser.setMail(testuser.getMail() + "a");
			FamDaoProxy.userDao().insert(testuser);
		} catch (DataIntegrityViolationException dive) {
			fail("should NOT have thrown DataIntegrityViolationException");
		}
		List<User> dbusers = FamDaoProxy.userDao().getObjectsLike(testuser);
		assertEquals(1, dbusers.size());
		assertEquals(testuser.getUserId(), dbusers.get(0).getUserId());

	}

	/**
     *
     */
	@Test
	public void readCleanPassAndGetEncoded() {
		String rawPass = "foo";
		User user = TeztBeanSimpleFactory.getNewValidUser();
		user.setPassword(rawPass);
		assertEquals(user.getPassword(), rawPass);
		FamDaoProxy.getInstance().getUserDao().insert(user);
		assertFalse(user.getPassword(), user.getPassword().equals("foo"));
		assertFalse(user.getPassword(), user.getPassword().equals(""));
	}

	@Test
	public void usernameSpecialChars() {
		this.clearDatabase();
		User user1 = new User();
		user1.setStandardUser();
		user1.setMail("foo@bar.foos");
		user1.setFname("Ähr");
		user1.setSname("Müller");
		user1.setPassword("foo");
		user1.encodePassword();
		assertEquals("aemuelle", FamDaoProxy.userDao().getUniqueUsername(user1));

		User user2 = new User();
		user2.setStandardUser();
		user2.setMail("foo@bar.foos");
		user2.setFname("Aχhl");
		user2.setSname("Mülτωχer");
		user2.setPassword("foo");
		user2.encodePassword();
		assertEquals("ahmueler", FamDaoProxy.userDao().getUniqueUsername(user2));

		User user3 = new User();
		user3.setStandardUser();
		user3.setMail("foo@bar.foos");
		user3.setFname("ÄÄ");
		user3.setSname("Üτωχüür");
		user3.setPassword("foo");
		user3.encodePassword();
		assertEquals("aeueueue", FamDaoProxy.userDao().getUniqueUsername(user3));

		// on users without any ascii name, use user's email as username
		User user4 = new User();
		user4.setStandardUser();
		user4.setMail("foo@bar.foos");
		user4.setFname("τωχ");
		user4.setSname("τωχτωχτωχ");
		user4.setPassword("foo");
		user4.encodePassword();
		assertEquals("userfoobarfoos", FamDaoProxy.userDao().getUniqueUsername(user4));
	}

	/**
     *
     */
	@Test
	public void usernameOnManyUsers() {
		this.clearDatabase();
		User daoltman = new User();
		daoltman.setStandardUser();
		daoltman.setMail("foo@bar.foos");
		daoltman.setFname("Daniel");
		daoltman.setSname("Oltmanns");
		daoltman.setRoleId("extern");
		daoltman.setPassword("foo");
		daoltman.encodePassword();
		String username = FamDaoProxy.userDao().getUniqueUsername(daoltman);
		assertEquals("daoltman", username);

		// store this user and ask again
		daoltman.setUsername(username);
		FamDaoProxy.userDao().insert(daoltman);
		username = FamDaoProxy.userDao().getUniqueUsername(daoltman);
		assertEquals("daoltma1", username);

		// another user has same username
		User another = new User();
		another.setUsername("daoltman");
		username = FamDaoProxy.userDao().getUniqueUsername(another);
		assertEquals("daoltma1", username);

		// another user name already exists for user
		daoltman.setUsername("foo");
		username = FamDaoProxy.userDao().getUniqueUsername(daoltman);
		assertEquals("foo", username);

		// create another user with same name ...
		User testuser1 = new User();
		testuser1.setStandardUser();
		testuser1.setMail("bar@bar.foo");
		testuser1.setFname("Daniel");
		testuser1.setSname("Oltmanns");
		testuser1.setRoleId("extern");
		username = FamDaoProxy.userDao().getUniqueUsername(testuser1);
		assertEquals("daoltma1", username);
		testuser1.setUsername(username);
		testuser1.setPassword("foo");
		testuser1.encodePassword();
		// .. and is stored as well
		FamDaoProxy.userDao().insert(testuser1);

		// create some nother users with equal name
		int i = 0;
		while (i < 20) {
			User kimlee = new User();
			kimlee.setStandardUser();
			kimlee.setMail(i + "foobar@bar.foo");
			kimlee.setFname("Kim");
			kimlee.setSname("Lee");
			kimlee.setRoleId("extern");
			kimlee.setPassword("foo");
			kimlee.encodePassword();
			username = FamDaoProxy.userDao().getUniqueUsername(kimlee);
			String expected = "kilee";
			if (i > 0) {
				expected += i;
			}
			assertEquals(expected, username);
			kimlee.setUsername(username);
			FamDaoProxy.userDao().insert(kimlee);
			i++;
		}

		// names not set
		User mrx = new User();
		username = FamDaoProxy.userDao().getUniqueUsername(mrx);
		assertEquals("user", username);
		this.clearDatabase();
	}

	/**
     *
     */
	@Test
	public void userRole() {
		this.clearDatabase();
		User testuser = new User();
		assertNull(testuser.getRoleId());
		assertNull(testuser.getUsedPlattformLang());
		assertNull(testuser.getRegistration());
		testuser.setExcluded(true);
		assertTrue(testuser.isExcluded());
		testuser.setStandardUser();
		assertNotNull(testuser.getRoleId());
		assertNotNull(testuser.getUsedPlattformLang());
		assertNull(testuser.isExcluded());
		assertNotNull(testuser.getRegistration());

	}
}

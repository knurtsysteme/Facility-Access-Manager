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
package de.knurt.fam.test.unit.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.model.persist.document.Job;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.couchdb.FamCouchDBDao;
import de.knurt.fam.template.controller.json.DeleteUserFromUsersManagerController;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class DeleteUserTest extends FamIBatisTezt {

	private static DeleteUserMock mockAdmin() {
		return new DeleteUserMock(TeztBeanSimpleFactory.getAdmin());
	}

	@Test
	public void noAdminThere() {
		this.clearDatabase();
		User user = TeztBeanSimpleFactory.getNewValidUser();
		user.insert();
		assertFalse(FamAuth.hasRight(user, FamAuth.DELETE_USERS_DATA, null));
		assertFalse(new DeleteUserMock(user).totalDestroy(user));
	}

	@Test
	public void generatingNiceFailMessages() {
		this.clearDatabase();
		User user = TeztBeanSimpleFactory.getNewValidUser();
		user.insert();
		assertFalse(FamAuth.hasRight(user, FamAuth.DELETE_USERS_DATA, null));
		DeleteUserMock dmock = new DeleteUserMock(user);
		dmock.putUserToTrash(user);
		assertNotNull(dmock.getMessages());
		assertEquals(1, dmock.getMessages().length());
		try {
			assertNotNull(dmock.getMessages().getJSONObject(0).get("0"));
		} catch (JSONException e) {
			fail("Should not throw " + e);
		}
	}

	@Test
	public void generatingNiceSuccMessages() {
		this.clearDatabase();
		User user = TeztBeanSimpleFactory.getNewValidUser();
		user.insert();
		assertFalse(FamAuth.hasRight(user, FamAuth.DELETE_USERS_DATA, null));
		DeleteUserMock dmock = mockAdmin();
		assertNotNull(dmock.getMessages());
		dmock.putUserToTrash(user);
		assertEquals(1, dmock.getMessages().length());
		try {
			assertNotNull(dmock.getMessages().getJSONObject(0).get("1"));
		} catch (JSONException e) {
			fail("Should not throw " + e);
		}
	}

	@Test
	public void moveToTrash() {
		this.clearDatabase();
		User u = TeztBeanSimpleFactory.getNewValidUser();
		u.insert();
		assertFalse(u.isExcluded());
		assertTrue(mockAdmin().putUserToTrash(u));
		assertTrue(u.isExcluded());
	}

	@Test
	public void destroyUser() {
		this.clearDatabase();

		User user1 = TeztBeanSimpleFactory.getNewValidUser();
		String username = user1.getUsername();
		user1.insert();
		Job job1 = this.insertJob(user1);
		LogbookEntry logbookEntry1 = this.insertLogbookEntry(user1);
		assertNotNull(user1);
		assertNotNull(job1);
		assertNotNull(logbookEntry1);
		assertTrue(FamDaoProxy.jobsDao().getJobs(user1, true).size() > 0);
		assertTrue(FamDaoProxy.bookingDao().getAllBookingsOfUser(user1).size() > 0);
		assertTrue(FamDaoProxy.logbookEntryDao().getAllLogbookEntriesOfUser(user1).size() > 0);

		User user2 = TeztBeanSimpleFactory.getNewValidUser();
		user2.setUniqueUsernameForInsertion();
		user2.setMail("mail_2@la.le");
		user2.insert();
		Job job2 = this.insertJob(user2);
		LogbookEntry logbookEntry2 = this.insertLogbookEntry(user2);
		assertNotNull(user2);
		assertNotNull(job2);
		assertNotNull(logbookEntry2);
		assertTrue(FamDaoProxy.jobsDao().getJobs(user2, true).size() > 0);
		assertTrue(FamDaoProxy.bookingDao().getAllBookingsOfUser(user2).size() > 0);
		assertTrue(FamDaoProxy.logbookEntryDao().getAllLogbookEntriesOfUser(user2).size() > 0);

		assertTrue(mockAdmin().totalDestroy(user1));

		assertNull(FamDaoProxy.userDao().getUserFromUsername(username));
		assertEquals(0, FamDaoProxy.jobsDao().getJobs(user1, true).size());
		assertEquals(0, FamDaoProxy.bookingDao().getAllBookingsOfUser(user1).size());
		assertEquals(0, FamDaoProxy.logbookEntryDao().getAllLogbookEntriesOfUser(user1).size());
		assertTrue(FamDaoProxy.jobsDao().getJobs(user2, true).size() > 0);
		assertTrue(FamDaoProxy.bookingDao().getAllBookingsOfUser(user2).size() > 0);
		assertTrue(FamDaoProxy.logbookEntryDao().getAllLogbookEntriesOfUser(user2).size() > 0);
	}

	@Test
	public void trashAndAnonym() {
		this.clearDatabase();
		User u = TeztBeanSimpleFactory.getNewValidUser();
		String originalUsername = u.getUsername();
		String anonymUsername = "anonym";
		u.insert();
		Job job = this.insertJob(u);
		LogbookEntry logbookEntry = this.insertLogbookEntry(u);

		assertNotNull(u);
		assertNotNull(job);
		assertNotNull(logbookEntry);

		assertTrue(mockAdmin().putUserToTrashAndAnonym(u));

		assertNotNull(u);
		assertNotNull(job);
		assertNotNull(logbookEntry);
		assertFalse(u.getUsername().equals(originalUsername));
		assertEquals(anonymUsername, u.getUsername());
		assertNull(FamDaoProxy.userDao().getUserFromUsername(originalUsername));

		User back = FamDaoProxy.userDao().getUserFromUsername(anonymUsername);
		assertNotNull(back);

		// check data
		boolean entryStillExists = false;
		for (LogbookEntry le : FamDaoProxy.logbookEntryDao().getAll()) {
			assertFalse(le.getOfUserName().equals(originalUsername));
			if (le.getOfUserName().equals(anonymUsername)) {
				entryStillExists = true;
			}
		}
		assertTrue(entryStillExists);

		entryStillExists = false;
		for (Booking b : FamDaoProxy.bookingDao().getAll()) {
			assertFalse(b.getUsername().equals(originalUsername));
			if (b.getUsername().equals(anonymUsername)) {
				entryStillExists = true;
			}
		}
		assertTrue(entryStillExists);

		entryStillExists = false;
		for (Job j : FamDaoProxy.jobsDao().getJobs(u, false)) {
			assertFalse(j.getUsername().equals(originalUsername));
			if (j.getUsername().equals(anonymUsername)) {
				entryStillExists = true;
			}
		}
		assertTrue(entryStillExists);
	}

	@Test
	public void adminNotDeletable() {
		this.clearDatabase();
		User u = TeztBeanSimpleFactory.getAdmin();
		u.insert();
		assertFalse(mockAdmin().putUserToTrash(u));
		assertFalse(mockAdmin().putUserToTrashAndAnonym(u));
		assertFalse(mockAdmin().putUserToTrashAndAnonymizeAndDeleteJobs(u));
		assertFalse(mockAdmin().totalDestroy(u));
	}

	@Test
	public void userCanOnlyBeAnonymizedOnce() {
		this.clearDatabase();
		User u = TeztBeanSimpleFactory.getNewValidUser();
		u.insert();
		assertFalse(u.isAnonym());
		assertTrue(mockAdmin().putUserToTrashAndAnonym(u));
		assertTrue(u.isAnonym());
		assertFalse(mockAdmin().putUserToTrashAndAnonym(u));
	}

	@Test
	public void differentAnonymUsersHasDifferentUsernames() {
		this.clearDatabase();

		User u1 = TeztBeanSimpleFactory.getNewValidUser();
		u1.insert();
		assertFalse(u1.isAnonym());

		User u2 = TeztBeanSimpleFactory.getNewUniqueValidUser("salt");
		u2.insert();
		assertFalse(u2.isAnonym());

		User auth = TeztBeanSimpleFactory.getAdmin();
		assertTrue(u1.anonymize(auth));
		assertTrue(u2.anonymize(auth));

		assertEquals("anonym", u1.getUsername());
		assertEquals("anonym1", u2.getUsername());
	}

	@Test
	public void putUserToTrashAndAnonymizeAndDeleteJobs() {
		this.clearDatabase();
		this.clearCouchDB();
		User u = TeztBeanSimpleFactory.getNewValidUser();
		String originalUsername = u.getUsername();
		u.insert();
		Job job = this.insertJob(u);
		LogbookEntry logbookEntry = this.insertLogbookEntry(u);

		assertNotNull(u);
		assertNotNull(job);
		assertNotNull(logbookEntry);

		assertTrue(mockAdmin().putUserToTrashAndAnonymizeAndDeleteJobs(u));

		assertNotNull(u);
		assertFalse(u.getUsername().equals(originalUsername));
		assertNull(FamDaoProxy.userDao().getUserFromUsername(originalUsername));
		assertNotNull(FamDaoProxy.userDao().getUserFromUsername(u.getUsername()));

		// check data
		boolean entryStillExists = false;
		for (LogbookEntry le : FamDaoProxy.logbookEntryDao().getAll()) {
			assertFalse(le.getOfUserName().equals(originalUsername));
			if (u.getUsername().equals(le.getOfUserName())) {
				entryStillExists = true;
			}
		}
		assertTrue(entryStillExists);

		entryStillExists = false;
		for (Booking b : FamDaoProxy.bookingDao().getAll()) {
			assertFalse(b.getUsername().equals(originalUsername));
			if (b.getUsername().equals(u.getUsername())) {
				entryStillExists = true;
			}
		}
		assertTrue(entryStillExists);

		for (Job j : FamDaoProxy.jobsDao().getJobs(u, true)) {
			assertFalse(j.getUsername().equals(originalUsername));
			assertFalse(j.getUsername().equals(u.getUsername()));
		}
	}

	private LogbookEntry insertLogbookEntry(User u) {
		LogbookEntry le = TeztBeanSimpleFactory.getNewValidLogbookEntry();
		le.setOfUserName(u.getUsername());
		le.insert();
		return le;
	}

	private Job insertJob(User u) {
		TimeBooking booking = TeztBeanSimpleFactory.getNewValidBooking();
		booking.getSessionTimeFrame().add(Calendar.YEAR, 1);
		booking.setApplied();
		booking.setUsername(u.getUsername());
		booking.insert();

		Job document = new Job();
		document.setJobId(booking.getId());
		document.setUsername(u.getUsername());
		document.setStep(0);
		document.setIdJobDataProcessing("foo");
		document.setJobSurvey(new JSONObject());
		assertTrue(FamCouchDBDao.getInstance().createDocument(document));
		return document;
	}

}

/**
 * override protected methods to public
 */
class DeleteUserMock extends DeleteUserFromUsersManagerController {
	public DeleteUserMock(User auth) {
		super(auth);
	}

	@Override
	public boolean putUserToTrash(User user) {
		return super.putUserToTrash(user);
	}

	@Override
	public boolean totalDestroy(User user) {
		return super.totalDestroy(user);
	}

	@Override
	public boolean putUserToTrashAndAnonym(User user) {
		return super.putUserToTrashAndAnonym(user);
	}

	@Override
	public boolean putUserToTrashAndAnonymizeAndDeleteJobs(User user) {
		return super.putUserToTrashAndAnonymizeAndDeleteJobs(user);
	}
}
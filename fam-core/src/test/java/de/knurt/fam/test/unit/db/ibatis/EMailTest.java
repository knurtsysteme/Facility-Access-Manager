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
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.content.text.FamText;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.UserMail;
import de.knurt.fam.core.model.persist.booking.Cancelation;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.core.util.mail.OutgoingUserMailBox;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class EMailTest extends FamIBatisTezt {

	// @Autowired
	// private ReloadableResourceBundleMessageSource messageSource;

	/**
     *
     */
	@Test
	public void placeholderTest() {
		this.clearDatabase();
		// prepare objects needed to send mails
		User user = TeztBeanSimpleFactory.getNewValidUser();
		TimeBooking bookedBooking = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		bookedBooking.setBooked();
		TimeBooking canceledBooking = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		canceledBooking.setApplied();
		canceledBooking.cancel(new Cancelation(user, "lala"));

		// insert all mails
		OutgoingUserMailBox.insert_BookingMade(bookedBooking);
		OutgoingUserMailBox.insert_ApplicationConfirmation(user, bookedBooking, "nice work");
		OutgoingUserMailBox.insert_BookingCancelation(canceledBooking);
		OutgoingUserMailBox.insert_Registration(user);
		OutgoingUserMailBox.insert_ForgottenPassword(user);

		// get it back
		List<UserMail> mails = FamDaoProxy.userDao().getAllUserMails();

		// check message
		for (UserMail mail : mails) {
			assertFalse(mail.getSubject(), mail.getSubject().contains("}"));
			assertFalse(mail.getMsg(), mail.getMsg().contains("}"));
		}
	}

	@Test
	public void sendMail_adminInitPasswordTest() {
		this.clearDatabase();
		User testuser = UserFactory.me().blank();
		testuser.setMail("foo@bar.foo");

		UserMail mail = OutgoingUserMailBox.sendMail_adminInitPassword(testuser, "very_unique_string *yeah*");
		assertNotNull(mail);
		assertTrue(mail.getMsg().indexOf("very_unique_string") < 0);
		assertTrue(mail.getMsg().indexOf("*****") > 0);
	}

	/**
     *
     */
	@Test
	public void debugging_1() {
		String[] args = new String[5];
		args[0] = "a";
		args[1] = "b";
		args[2] = "c";
		args[3] = "d";
		args[4] = "e";
		String message = FamText.message("mail.registration.msg", args);
		assertFalse(message, message.contains("}"));
	}

	/**
     *
     */
	@Test
	public void sendMailLater() {
		this.clearDatabase();
		TimeBooking booking = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		booking.setBooked();
		booking.cancel(new Cancelation(TeztBeanSimpleFactory.getNewValidUser(), "sd"));
		// OutgoingUserMailBox.insert_BookingCancelation(booking);

		List<UserMail> all = FamDaoProxy.userDao().getAllUserMails();
		assertEquals(1, all.size());
		assertFalse(all.get(0).getToSendDate().before(new Date()) || all.get(0).getToSendDate().equals(new Date()));
		assertFalse(all.get(0).hasBeenSent());
		assertFalse(all.get(0).mustBeSendNow());

		List<UserMail> um = FamDaoProxy.userDao().getUserMailsThatMustBeSendNow();
		assertEquals(0, um.size()); // must be sent
	}

	/**
     *
     */
	@Test
	public void hasRightMessage() {
		this.clearDatabase();
		TimeBooking booking = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		booking.setBooked();
		OutgoingUserMailBox.insert_BookingMade(booking);
		List<UserMail> all = FamDaoProxy.userDao().getAllUserMails();
		assertEquals(1, all.size());
		assertEquals(FamText.message("mail.bookingmade.subject"), all.get(0).getSubject());
	}

	/**
     *
     */
	@Test
	public void insertApplication() {
		this.clearDatabase();
		TimeBooking booking = TeztBeanSimpleFactory.getNewValidBooking4TomorrowSameTimeAsNow();
		booking.setApplied();
		OutgoingUserMailBox.insert_BookingMade(booking);
		List<UserMail> all = FamDaoProxy.userDao().getAllUserMails();
		assertEquals(1, all.size());
	}

	/**
     *
     */
	@Test
	public void conformity() {
		this.clearDatabase();
		User testuser = UserFactory.me().blank();
		testuser.setMail("foo@bar.foo");

		OutgoingUserMailBox.insert_Registration(testuser);
		List<UserMail> all = FamDaoProxy.userDao().getAllUserMails();
		UserMail toCheck = all.get(0);
		assertEquals(testuser.getMail(), toCheck.getTo());
		assertEquals(testuser.getUsername(), toCheck.getUsername());
	}

	/**
     *
     */
	@Test
	public void sendMailNow() {
		this.clearDatabase();
		User testuser = UserFactory.me().blank();
		testuser.setMail("foo@bar.foo");

		OutgoingUserMailBox.insert_Registration(testuser);
		List<UserMail> all = FamDaoProxy.userDao().getAllUserMails();
		assertEquals(1, all.size());
		assertTrue(all.get(0).getToSendDate().toString() + "<>" + new Date().toString(), all.get(0).getToSendDate().before(new Date()) || all.get(0).getToSendDate().equals(new Date()));
		assertTrue(all.get(0).hasBeenSent());
		assertFalse(all.get(0).mustBeSendNow());

		List<UserMail> um = FamDaoProxy.userDao().getUserMailsThatMustBeSendNow();
		assertEquals(0, um.size()); // must be sent
	}
}

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
package de.knurt.fam.core.util.mail;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.UserMail;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;

/**
 * A simple text mail with a field for when to send in future
 * 
 * Use the factory to get one.
 * 
 * @see OutgoingUserMailBox
 * @author Daniel Oltmanns
 * @since 0.20090325
 */
public class UserMailSender {

	private String fromMail, fromName, hostName, authPass, authName;
	private int smtpPort;

	public String getFromMail() {
		return fromMail;
	}

	/** one and only instance of me */
	private volatile static UserMailSender me;

	/** construct me */
	private UserMailSender() {
	}

	/**
	 * return the one and only instance of UserMailSender
	 * 
	 * @return the one and only instance of UserMailSender
	 */
	public static UserMailSender getInstance() {
		if (me == null) { // no instance so far
			synchronized (UserMailSender.class) {
				if (me == null) { // still no instance so far
					me = new UserMailSender(); // the one and only
					me.fromMail = FamConnector.getGlobalProperty("mail_from_mail");
					me.fromName = FamConnector.getGlobalProperty("mail_from_name");
					me.hostName = FamConnector.getGlobalProperty("mail_host_name");
					try {
						me.smtpPort = Integer.parseInt(FamConnector.getGlobalProperty("mail_smtp_port"));
					} catch (NumberFormatException ex) {
						FamLog.exception("smtp port not set or not a number", ex, 201111091249l); // INTLANG
					}
					me.authPass = FamConnector.getGlobalProperty("mail_auth_pass");
					if (me.authPass != null && me.authPass.equals("null")) {
						me.authPass = null;
					}
					me.authName = FamConnector.getGlobalProperty("mail_auth_name");
					if (me.authName != null && me.authName.equals("null")) {
						me.authName = null;
					}
				}
			}
		}
		return me;
	}

	/**
	 * send all mails, that have to be sent now.
	 * 
	 * @see UserMail#getToSendDate()
	 * @return number of sent mails
	 */
	public static int sendUserMails() {
		int result = 0;
		for (UserMail mail : FamDaoProxy.userDao().getUserMailsThatMustBeSendNow()) {
			if (getInstance().sendingIssueIsOver(mail)) {
				mail.setNeverSendDate(new Date());
				FamDaoProxy.userDao().update(mail);
			} else {
				boolean wasSent = send(mail);
				if (wasSent) {
					result++;
					mail.setWasSentDate(new Date());
					if (mail.getMsgAfterSent() != null) {
						mail.setMsg(mail.getMsgAfterSent());
					}
					FamDaoProxy.userDao().update(mail);
				}
			}
		}
		return result;
	}

	private boolean sendingIssueIsOver(UserMail mail) {
		boolean result = false;
		if (mail.getType() != null) {
			User user = FamDaoProxy.userDao().getUserFromUsername(mail.getUsername());
			boolean userIsValidAndActive = user != null && !user.isExcluded() && !user.isAccountExpired() && !user.isAnonym();
			if (mail.getType() == UserMail.TYPE_NEEDS_VALID_BOOKING && mail.getFid() != null) {
				Booking booking = FamDaoProxy.bookingDao().getBookingWithId(mail.getFid());
				result = booking == null || booking.isCanceled() || !userIsValidAndActive || !user.isAllowedToAccess(booking.getFacility());
			} else if (mail.getType() == UserMail.TYPE_NEEDS_VALID_ACTIVE_USER) {
				result = !userIsValidAndActive;
			}
		}
		return result;
	}
	
	/**
	 * just a meter for sending mails without userbox
	 */
	public static int SEND_WITHOUT_METER = 0;

	/**
	 * send an email without need to put it into the userbox (without saved in
	 * database). put only smtp port and host to it as configured in
	 * {@link UserMailSender}. do not change anything else but send the email!
	 * 
	 * @param raw
	 *            all content (subject, msg, attachments, from, to) must be set
	 * @return true if sending succeeded.
	 */
	public static boolean sendWithoutUserBox(Email raw) {
		boolean result = true;
		UserMailSender dse = getInstance();

		raw.setHostName(dse.hostName);
		raw.setSmtpPort(dse.smtpPort);

		if (FamConnector.isDev() == false) {
			try {
				raw.send();
			} catch (EmailException e) {
				result = false;
				FamLog.exception("sendWithoutUserBox: " + e.getMessage(), e, 201106131753l);
			}
		}
		if(result) {
		  if(SEND_WITHOUT_METER == Integer.MAX_VALUE) {
		    SEND_WITHOUT_METER = 0;
		  }
		  SEND_WITHOUT_METER++;
		}
		return result;
	}

	private static boolean send(UserMail um) {
		boolean sendSucc = false;
		UserMailSender dse = getInstance();
		if (um.hasBeenSent() == false) {
			if (um.mustBeSendNow()) {
				// prepare
				SimpleEmail email = new SimpleEmail();
				email.setHostName(dse.hostName);
				email.setSmtpPort(dse.smtpPort);
				// mail server using pass
				if (dse.authName != null) {
					email.setAuthentication(dse.authName, dse.authPass);
				}
				Map<String, String> headers = new Hashtable<String, String>();
				// headers.put("Subject", um.getSubject());
				email.setSubject(um.getSubject());
				headers.put("Content-Type", "text/plain; charset=utf-8");
				headers.put("Content-Transfer-Encoding", "base64");
				email.setHeaders(headers);
				boolean creatingSucc = false;
				try {
					email.addTo(um.getTo(), um.getUsername());
					email.setFrom(dse.fromMail, dse.fromName);
					email.setMsg(um.getMsg());
					creatingSucc = true;
				} catch (EmailException ex) {
					FamLog.logException(UserMailSender.class, ex, "creating mail failed::" + um.getTo() + "::" + um.getUsername() + "::" + um.getId(), 200904031116l);
				}

				if (creatingSucc && FamConnector.isDev() == false) {
					try {
						email.send();
						sendSucc = true;
					} catch (EmailException ex) {
						FamLog.exception("sending a mail failed: " + ex.getMessage() + "-" + dse.fromMail + "-" + dse.fromName, ex, 200904031018l);
					}
				} else { // just dev mode - do not send any mails
					sendSucc = true;
				}
			}
		} else {
			FamLog.logException(UserMailSender.class, new DataIntegrityViolationException("try to send a mail twice"), "try to send a mail twice", 200908201836l);
		}
		return sendSucc;
	}
}

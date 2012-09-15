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

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.encoder.FamTmpAccessEncoderControl;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.UserMail;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.Cancelation;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.UserDao;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.core.util.mvc.RedirectResolver;
import de.knurt.fam.core.view.text.FamDateFormat;
import de.knurt.fam.core.view.text.FamText;
import de.knurt.fam.template.util.TemplateHtml;

/**
 * a mail box producing and insert messages.<br />
 * <p>
 * this class has to tasks:<br />
 * 1. produce {@link UserMail}s to send to {@link User}s<br />
 * 2. store {@link UserMail}s for sending them yet or later.
 * </p>
 * all basics must be injected (see setters),<br />
 * all specifics are part of the product (the mail).
 * 
 * @see UserMailSender
 * @see UserMail
 * @see UserDao#insert(de.knurt.fam.core.model.persist.UserMail)
 * @author Daniel Oltmanns
 * @since 0.20090325
 */
public class OutgoingUserMailBox { // INTLANG (entire class)

	/**
	 * send a mail to confirm the registration of a user. this is sent if an
	 * account is verified or the user is known and has been logged in the first
	 * time.
	 * 
	 * @param to
	 *            recipient of the mail
	 */
	public static void insert_Registration(User to) {
		insert(getInstance().getMail_Registration(to));
	}

	private static void insert_BookingReminder(Booking booking) {
		UserMail mail = getInstance().getMail_BookingReminder(booking);
		if (mail != null) {
			insert(mail);
		}
	}

	/**
	 * send a mail to confirm a users booking.
	 * 
	 * @param booking
	 *            confirmed in the mail
	 */
	public static void insert_BookingMade(TimeBooking booking) {
		insert(getInstance().getMail_BookingMade(booking));
		insert_BookingReminder(booking);
	}

	/**
	 * send a mail to confirm a users booking.
	 * 
	 * @param booking
	 *            confirmed in the mail
	 */
	public static boolean insert_BookingMade(QueueBooking booking) {
		return insert(getInstance().getMail_BookingMade(booking));
	}

	private static boolean insert(UserMail mail) {
		boolean result = FamDaoProxy.userDao().insert(mail);
		if (mail.mustBeSendNow()) {
			UserMailSender.sendUserMails();
		}
		return result;
	}

	/**
	 * insert a cancelation mail into the outbox. if the booking does not
	 * contain a {@link Cancelation}, log it and do nothing.
	 * 
	 * @param booking
	 *            that is canceled
	 */
	public static void insert_BookingCancelation(TimeBooking booking) {
		insert(getInstance().getMail_bookingCancelation(booking));
	}

	/**
	 * insert a cancelation mail into the outbox. if the booking does not
	 * contain a {@link Cancelation}, log it and do nothing.
	 * 
	 * @param booking
	 *            that is canceled
	 */
	public static void insert_BookingCancelation(QueueBooking booking) {
		insert(getInstance().getMail_bookingCancelation(booking));
	}

	/**
	 * send a mail to a user requested a new password.
	 * 
	 * @param to
	 *            recipient of the mail
	 */
	public static void insert_ForgottenPassword(User to) {
		insert(getInstance().getMail_ForgottenPassword(to));
	}

	/**
	 * insert information mail when a booking has been processed.
	 * 
	 * @param booking
	 *            information mail when a booking has been processed.
	 */
	public static void insert_BookingProcessed(Booking booking) {
		insert(getInstance().getMail_BookingProcessed(booking));
	}

	private OutgoingUserMailBox() {
	}

	private volatile static OutgoingUserMailBox me;

	/**
	 * return the one and only factory instance
	 * 
	 * @return the one and only factory instance
	 */
	private static OutgoingUserMailBox getInstance() {
		if (me == null) { // no instance so far
			synchronized (OutgoingUserMailBox.class) {
				if (me == null) { // still no instance so far
					me = new OutgoingUserMailBox(); // the one and only
				}
			}
		}
		return me;
	}

	private int timeUnitsBaseInMinutes;
	private int timeUnitsSend_Registration, timeUnitsSend_BookingMade, timeUnitsSend_ApplicationConfirmation, timeUnitsSend_ForgottenPassword, timeUnitsSend_BookingCancelation;

	private UserMail getMail_BookingMade(TimeBooking booking) {
		String[] args = new String[4];
		args[0] = booking.getUser().getFullName();
		args[1] = booking.getFacility().getLabel(); // for facility
		args[2] = FamDateFormat.getDateFormattedWithTime(booking, false);
		args[3] = TemplateHtml.href("mybookings");
		return this.getMailFromMessageSource(booking.getUser(), "bookingmade", args, this.timeUnitsSend_BookingMade, null, UserMail.TYPE_NEEDS_VALID_BOOKING, booking.getId());
	}

	private UserMail getMail_BookingMade(QueueBooking booking) {
		String[] args = new String[4];
		args[0] = booking.getUser().getFullName();
		args[1] = booking.getFacility().getLabel(); // for facility
		args[2] = FamDateFormat.getDateFormattedWithTime(booking.getExpectedSessionTimeFrame(), false); // expected
		// time
		args[3] = TemplateHtml.href("mybookings");
		return this.getMailFromMessageSource(booking.getUser(), "queueBookingmade", args, this.timeUnitsSend_BookingMade, null, UserMail.TYPE_NEEDS_VALID_BOOKING, booking.getId());
	}

	private UserMail getMail_BookingReminder(Booking booking) {
		UserMail result = null;
		Calendar startOfSession = booking.getSessionTimeFrame().getCalendarStart();
		if (startOfSession.get(Calendar.DAY_OF_YEAR) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR) || startOfSession.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR)) {
			// ↖ session is not today today

			// ↓ set to send date
			Calendar mailToSendDate = booking.getSessionTimeFrame().getCalendarStart();
			int reminderMailBeforeStarting = booking.getBookingRule().getSetOfRulesForARole(booking.getUser()).getReminderMailMinutesBeforeStarting();
			if (reminderMailBeforeStarting != -1) {
				// ↖ reminder mail is configured
				mailToSendDate.add(Calendar.MINUTE, -reminderMailBeforeStarting);
				if (mailToSendDate.after(Calendar.getInstance())) {
					// ↖ mail is send in future
					// ↓ prepare mail
					String[] args = new String[4];
					args[0] = booking.getUser().getFullName();
					args[1] = booking.getFacility().getLabel(); // for facility
					args[2] = FamDateFormat.getDateFormattedWithTime(booking.getSessionTimeFrame().getDateStart());
					args[3] = TemplateHtml.href("mybookings");
					result = this.getMailFromMessageSource(booking.getUser(), "bookingreminder", args, 0, null, UserMail.TYPE_NEEDS_VALID_BOOKING, booking.getId());
					result.setToSendDate(mailToSendDate.getTime());
				}
			}
		}
		return result;
	}

	private UserMail getMail_ApplicationConfirmation(User to, Booking booking, String notification) {
		notification = notification.trim();
		String[] args = new String[5];
		args[0] = to.getFullName();
		args[1] = booking.getFacility().getLabel(); // for facility
		args[2] = FamDateFormat.getDateFormattedWithTime(booking.getSessionTimeFrame(), false);
		args[3] = TemplateHtml.href("mybookings");
		args[4] = notification.isEmpty() ? "" : "You got a notification with this confirmation: \"" + notification + "\""; // INTLANG
		if (booking.isQueueBased()) {
			args[2] += " (expected to be - real time may vary tremendously)";
		}
		return this.getMailFromMessageSource(to, "applicationconfirmation", args, this.timeUnitsSend_ApplicationConfirmation, null, UserMail.TYPE_NEEDS_VALID_ACTIVE_USER, to.getId());
	}

	private UserMail getMail_ForgottenPassword(User to) {
		String[] args = new String[4];
		args[0] = to.getFullName();
		args[1] = TemplateHtml.href("setnewpassword");
		args[2] = FamTmpAccessEncoderControl.getInstance().encodePassword(to);
		args[3] = to.getUsername();
		return this.getMailFromMessageSource(to, "forgottenpassword", args, this.timeUnitsSend_ForgottenPassword, null, UserMail.TYPE_NEEDS_VALID_ACTIVE_USER, to.getId());
	}

	/**
	 * send a mail to a user that there are applications for a facility.
	 * 
	 * @param to
	 *            recipient of the mail
	 * @param count
	 *            of applications for the facility
	 * @param facility
	 *            the applications are for
	 */
	public static void sendMail_applicationsForResource(User to, Facility facility, int count) {
		String[] args = new String[3];
		args[0] = count + "";
		args[1] = facility.getLabel();
		args[2] = TemplateHtml.href("systemmodifyapplications");
		UserMail um = getInstance().getMailFromMessageSource(to, "applicationsforafacility", args, 0, null, UserMail.TYPE_NEEDS_VALID_ACTIVE_USER, to.getId());
		insert(um);
	}

	/**
	 * send a mail to a user, that there are applications for the system.
	 * 
	 * @param to
	 *            recipient of the mail
	 * @param count
	 *            of applications for the system
	 */
	public static void sendMail_applicationsForSystem(User to, int count) {
		String[] args = new String[2];
		args[0] = count + "";
		args[1] = TemplateHtml.href("users");
		UserMail um = getInstance().getMailFromMessageSource(to, "applicationsforsystem", args, 0, null, UserMail.TYPE_NEEDS_VALID_ACTIVE_USER, to.getId());
		insert(um);
	}

	private UserMail getMailFromMessageSource(User to, String key, String[] args4msg, int timeUnitsToSend, String msgAfterSent, Integer type, Integer fid) {
		String subject = FamText.message("mail." + key + ".subject");
		String message = FamText.message("mail." + key + ".msg", args4msg);
		Calendar toSent = Calendar.getInstance();
		toSent.add(Calendar.MINUTE, timeUnitsToSend * timeUnitsBaseInMinutes);
		return this.getMail(subject, message, to, toSent.getTime(), msgAfterSent, type, fid);
	}

	/**
	 * return a email for notifying a registered user
	 * 
	 * @param to
	 *            recipient user
	 * @return a email for notifying a registered user
	 */
	private UserMail getMail_Registration(User to) {
		String[] args = new String[4];
		args[0] = to.getFullName();
		args[1] = TemplateHtml.href("corehome");
		args[2] = to.getUsername();
		args[3] = TemplateHtml.href("forgottenpassword");
		return this.getMailFromMessageSource(to, "registration", args, this.getTimeUnitsSend_Registration(), null, UserMail.TYPE_NEEDS_VALID_ACTIVE_USER, to.getId());
	}

	/**
	 * return the configured {@link UserMail}
	 * 
	 * @param subject
	 *            of the mail
	 * @param message
	 *            of the mail
	 * @param to
	 *            recipient of the mail
	 * @param toSendDate
	 *            date of when the e-mail shall be sent
	 * @param msgAfterSent
	 *            {@link UserMail#getMsgAfterSent()}
	 * @param type
	 *            {@link UserMail#getType()}
	 * @param fid
	 *            {@link UserMail#getFid()}
	 * @return the configured mail
	 */
	private UserMail getMail(String subject, String message, User to, Date toSendDate, String msgAfterSent, Integer type, Integer fid) {
		UserMail result = new UserMail();
		result.setMsg(message + FamText.message("mail.footer"));
		result.setSubject(subject);
		result.setUsername(to.getUsername());
		result.setTo(to.getMail());
		result.setToSendDate(toSendDate);
		result.setMsgAfterSent(msgAfterSent);
		result.setType(type);
		result.setFid(fid);
		return result;
	}

	/**
	 * return the mail for a booking cancelation. if the booking does not
	 * contain a {@link Cancelation}, log it and return null.
	 * 
	 * @param booking
	 *            that is canceled. must contain a {@link Cancelation}
	 * @return the mail for a booking cancelation.
	 */
	private UserMail getMail_bookingCancelation(TimeBooking booking) {
		if (!booking.isCanceled()) {
			DataIntegrityViolationException ex = new DataIntegrityViolationException("a canceled booking is needed here");
			FamLog.logException(OutgoingUserMailBox.class, ex, "got an uncanceled booking " + booking + ".", 200908091609l);
			throw ex;
		} else {
			User to = booking.getUser();
			String[] args = new String[4];
			args[0] = to.getFullName();
			args[1] = FacilityConfigDao.label(booking.getFacility()); // facility
			// name
			args[2] = booking.getCancelation().getReason(); // reason
			args[3] = FamDateFormat.getDateFormattedWithTime(booking, false); // from
			// to
			UserMail um = this.getMailFromMessageSource(to, "bookingCancelation", args, this.timeUnitsSend_BookingCancelation, null, UserMail.TYPE_NEEDS_VALID_ACTIVE_USER, to.getId());
			return um;
		}
	}

	/**
	 * return the mail for a booking cancelation. if the booking does not
	 * contain a {@link Cancelation}, log it and return null.
	 * 
	 * @param booking
	 *            that is canceled. must contain a {@link Cancelation}
	 * @return the mail for a booking cancelation.
	 */
	private UserMail getMail_bookingCancelation(QueueBooking booking) {
		if (!booking.isCanceled()) {
			DataIntegrityViolationException ex = new DataIntegrityViolationException("a canceled booking is needed here");
			FamLog.logException(OutgoingUserMailBox.class, ex, "got an uncanceled booking " + booking + ".", 200909270716l);
			throw ex;
		} else {
			User to = booking.getUser();
			String[] args = new String[3];
			args[0] = to.getFullName();
			args[1] = FacilityConfigDao.label(booking.getFacility()); // facility
			// name
			args[2] = booking.getCancelation().getReason(); // reason
			UserMail um = this.getMailFromMessageSource(to, "queueBookingCancelation", args, this.timeUnitsSend_BookingCancelation, null, UserMail.TYPE_NEEDS_VALID_ACTIVE_USER, to.getId());
			return um;
		}
	}

	private UserMail getMail_BookingProcessed(Booking booking) {
		if (booking.isCanceled()) {
			DataIntegrityViolationException ex = new DataIntegrityViolationException("an uncanceled booking is needed here");
			FamLog.logException(OutgoingUserMailBox.class, ex, "an uncanceled booking is needed here " + booking + ".", 200909011059l);
			throw ex;
		} else {
			User to = booking.getUser();
			String[] args = new String[2];
			args[0] = to.getFullName();
			args[1] = RedirectResolver.redirectLink("viewrequest", QueryStringBuilder.getQueryString(booking));
			UserMail um = this.getMailFromMessageSource(to, "bookingProcessed", args, 0, null, UserMail.TYPE_NEEDS_VALID_ACTIVE_USER, to.getId());
			return um;
		}
	}

	/**
	 * insert the mail for an user applied for a facility that is sent from the
	 * operator to say: "yes, you got it".
	 * 
	 * @param user
	 *            receiving this mail
	 * @param booking
	 *            of user
	 * @param mailMessage
	 *            extended mail message from operator
	 */
	public static void insert_ApplicationConfirmation(User user, Booking booking, String mailMessage) {
		insert(getInstance().getMail_ApplicationConfirmation(user, booking, mailMessage));
	}

	/**
	 * @return the timeUnitsBaseInMinutes
	 */
	public int getTimeUnitsBaseInMinutes() {
		return timeUnitsBaseInMinutes;
	}

	/**
	 * @param timeUnitsBaseInMinutes
	 *            the timeUnitsBaseInMinutes to set
	 */
	@Required
	public void setTimeUnitsBaseInMinutes(int timeUnitsBaseInMinutes) {
		this.timeUnitsBaseInMinutes = timeUnitsBaseInMinutes;
	}

	/**
	 * @return the timeUnitsSend_BookingMade
	 */
	public int getTimeUnitsSend_BookingMade() {
		return timeUnitsSend_BookingMade;
	}

	/**
	 * @param timeUnitsSend_BookingMade
	 *            the timeUnitsSend_BookingMade to set
	 */
	@Required
	public void setTimeUnitsSend_BookingMade(int timeUnitsSend_BookingMade) {
		this.timeUnitsSend_BookingMade = timeUnitsSend_BookingMade;
	}

	/**
	 * @return the timeUnitsSend_ApplicationConfirmation
	 */
	public int getTimeUnitsSend_ApplicationConfirmation() {
		return timeUnitsSend_ApplicationConfirmation;
	}

	/**
	 * @param timeUnitsSend_ApplicationConfirmation
	 *            the timeUnitsSend_ApplicationConfirmation to set
	 */
	@Required
	public void setTimeUnitsSend_ApplicationConfirmation(int timeUnitsSend_ApplicationConfirmation) {
		this.timeUnitsSend_ApplicationConfirmation = timeUnitsSend_ApplicationConfirmation;
	}

	/**
	 * @return the timeUnitsSend_ForgottenPassword
	 */
	public int getTimeUnitsSend_ForgottenPassword() {
		return timeUnitsSend_ForgottenPassword;
	}

	/**
	 * @param timeUnitsSend_ForgottenPassword
	 *            the timeUnitsSend_ForgottenPassword to set
	 */
	@Required
	public void setTimeUnitsSend_ForgottenPassword(int timeUnitsSend_ForgottenPassword) {
		this.timeUnitsSend_ForgottenPassword = timeUnitsSend_ForgottenPassword;
	}

	/**
	 * @return the timeUnitsSend_BookingCancelation
	 */
	public int getTimeUnitsSend_BookingCancelation() {
		return timeUnitsSend_BookingCancelation;
	}

	/**
	 * @param timeUnitsSend_BookingCancelation
	 *            the timeUnitsSend_BookingCancelation to set
	 */
	@Required
	public void setTimeUnitsSend_BookingCancelation(int timeUnitsSend_BookingCancelation) {
		this.timeUnitsSend_BookingCancelation = timeUnitsSend_BookingCancelation;
	}

	/**
	 * @return the timeUnitsSend_Registration
	 */
	public int getTimeUnitsSend_Registration() {
		return timeUnitsSend_Registration;
	}

	/**
	 * @param timeUnitsSend_Registration
	 *            the timeUnitsSend_Registration to set
	 */
	@Required
	public void setTimeUnitsSend_Registration(int timeUnitsSend_Registration) {
		this.timeUnitsSend_Registration = timeUnitsSend_Registration;
	}

	/**
	 * send a password via email if administrator want that. use ***** for
	 * password after email has been sent successfully
	 * 
	 * @param to
	 *            recipient password is set for
	 * @param newpass
	 *            password set to recipient
	 * @return the usermail after sending
	 */
	public static UserMail sendMail_adminInitPassword(User to, String newpass) {
		String key = "admininitpassword";
		String[] args = new String[3];
		args[0] = to.getFullName();
		args[1] = "********";
		args[2] = TemplateHtml.href("changepassword");
		String msgAfterSent = FamText.message("mail." + key + ".msg", args);
		args[1] = newpass;
		UserMail um = getInstance().getMailFromMessageSource(to, key, args, 0, msgAfterSent, null, null);
		insert(um);
		return FamDaoProxy.userDao().getUserMailWithId(um.getId());
	}

	public static void sendMail_yourAccountExpired(User user) {
		String[] args = new String[2];
		args[0] = user.getFullName();
		args[1] = user.getAccountExpiresFormatted();
		UserMail um = getInstance().getMailFromMessageSource(user, "youraccountexpired", args, 0, null, null, null);
		insert(um);
		// send somebody in bcc if configured
		String youraccountexpired_bcc = FamConnector.getGlobalProperty("mail_youraccountexpired_bcc");
		if (youraccountexpired_bcc != null && !youraccountexpired_bcc.trim().isEmpty()) {
			User bccUser = FamDaoProxy.userDao().getUserFromUsername(youraccountexpired_bcc);
			if (bccUser != null) {
				UserMail um_blindcopy = getInstance().getMailFromMessageSource(bccUser, "youraccountexpired", args, 0, null, null, null);
				um_blindcopy.setMsg("A blindcopy for your information: " + um.getMsg());
				insert(um_blindcopy);
			}
		}
	}

	public static void sendMail_expiredAccountReopened(User user) {
		String[] args = new String[2];
		args[0] = user.getFullName();
		args[1] = user.getAccountExpiresFormatted();
		UserMail um = getInstance().getMailFromMessageSource(user, "expiredaccountreopened", args, 0, null, null, null);
		insert(um);
	}

}

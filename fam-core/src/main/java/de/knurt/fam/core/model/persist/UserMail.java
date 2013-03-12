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
package de.knurt.fam.core.model.persist;

import java.util.Date;

import de.knurt.fam.core.util.mail.OutgoingUserMailBox;
import de.knurt.fam.core.util.mail.UserMailSender;
import de.knurt.heinzelmann.util.adapter.ViewableObject;
import de.knurt.heinzelmann.util.query.Identificable;

/**
 * a data holder for a person's mail. it is not storable directly but can be
 * inserted over {@link OutgoingUserMailBox} and updated over
 * {@link UserMailSender#sendUserMails()}
 * 
 * @author Daniel Oltmanns
 * @since 0.20090820
 */
public class UserMail implements ViewableObject, Identificable {

	/**
	 * flag to tag this mail with: only send if booking and booker is valid.
	 * 
	 * @see #getType()
	 */
	public static final int TYPE_NEEDS_VALID_BOOKING = 1;
	/**
	 * flag to tag this mail with: only send if user is valid.
	 * 
	 * @see #getType()
	 */
	public static final int TYPE_NEEDS_VALID_ACTIVE_USER = 2;
	private Date toSendDate, wasSentDate, neverSendDate;
	private String username, subject, msg, to, msgAfterSent;
	private Integer id, type, fid;

	/**
	 * if and only if not null, this message is set after a email has been send.
	 * this is useful in case of emails including security relevant information
	 * like passwords.
	 */
	public String getMsgAfterSent() {
		return msgAfterSent;
	}

	/**
	 * @see #getMsgAfterSent()
	 */
	public void setMsgAfterSent(String msgAfterSent) {
		this.msgAfterSent = msgAfterSent;
	}

	/**
	 * empty constructor
	 */
	public UserMail() {
	}

	/**
	 * @return the toSendDate
	 */
	public Date getToSendDate() {
		return toSendDate;
	}

	/**
	 * set the date it must be sent.
	 * 
	 * @param toSendDate
	 *            the date it must be sent.
	 */
	public void setToSendDate(Date toSendDate) {
		this.toSendDate = toSendDate;
	}

	/**
	 * return the date it was sent.
	 * 
	 * @return wasSentDate the date it was sent.
	 */
	public Date getWasSentDate() {
		return wasSentDate;
	}

	/**
	 * set the date it was sent.
	 * 
	 * @param wasSentDate
	 *            the date it was sent.
	 */
	public void setWasSentDate(Date wasSentDate) {
		this.wasSentDate = wasSentDate;
	}

	/**
	 * return the username of recipient
	 * 
	 * @return the username of recipient
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * set the username of recipient
	 * 
	 * @param username
	 *            of recipient
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * return subject of the mail
	 * 
	 * @return the subject of the mail
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * set subject of the mail
	 * 
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * return message of the mail
	 * 
	 * @return the message of the mail
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * set message of the mail
	 * 
	 * @param msg
	 *            the message to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

	/**
	 * return recipient
	 * 
	 * @return the recipient
	 */
	public String getTo() {
		return to;
	}

	/**
	 * set recipient
	 * 
	 * @param to
	 *            the recipient
	 */
	public void setTo(String to) {
		this.to = to;
	}

	/**
	 * return true, if the mails has been sent.
	 * 
	 * @return true, if the mails has been sent.
	 */
	public boolean hasBeenSent() {
		return this.wasSentDate != null;
	}

	/**
	 * return true, if the mail must be send now. it must be send, if now is
	 * equal or after {@link #getToSendDate()}
	 * 
	 * @return true, if the mail must be send now.
	 */
	public boolean mustBeSendNow() {
		Date now = new Date();
		return this.hasBeenSent() == false && (this.toSendDate.before(now) || this.toSendDate.equals(now));
	}

	@Override
  public Integer getId() {
		return id;
	}

	@Override
  public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * return the date of the decision that this email is "never send". if this
	 * is not null, do not send the email. reasons for not sending are emails
	 * with a delay that has been settled.
	 * 
	 * @return <code>null</code> if the email has to be sent or the date of the
	 *         decision that this email is "never send"
	 */
	public Date getNeverSendDate() {
		return neverSendDate;
	}

	public void setNeverSendDate(Date neverSendDate) {
		this.neverSendDate = neverSendDate;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public void setFid(Integer fid) {
		this.fid = fid;
	}

	/**
	 * return an optional foreign id. the meaning of the id depends on the
	 * {@link #type}
	 * 
	 * @return an optional foreign id. the meaning of the id depends on the
	 *         {@link #type}
	 */
	public Integer getFid() {
		return fid;
	}

}

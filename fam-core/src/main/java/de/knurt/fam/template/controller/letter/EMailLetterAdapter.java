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
 */package de.knurt.fam.template.controller.letter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.validator.EmailValidator;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.util.mail.UserMailSender;
import de.knurt.fam.template.model.TemplateResource;

/**
 * a simple util for sending pdf emails
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.4.0 (06/21/2011)
 * 
 */

public class EMailLetterAdapter {

	private String from, to, subject, msg;

	protected String getFrom() {
		return from;
	}

	protected String getTo() {
		return to;
	}

	protected String getSubject() {
		return subject;
	}

	protected String getMsg() {
		return msg;
	}

	public EMailLetterAdapter(String from, String to, String subject, String msg) {
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.msg = msg;

		if (this.from == null || !EmailValidator.getInstance().isValid(this.from)) {
			this.from = UserMailSender.getInstance().getFromMail();
		}
		if (this.subject == null || this.subject.trim().isEmpty()) {
			this.subject = "[FAM] no subject"; // INTLANG
		}
		if (this.msg == null || this.msg.trim().isEmpty()) {
			this.msg = "[FAM] no message"; // INTLANG
		}

	}

	protected EMailLetterAdapter(TemplateResource tr) {
		// set from
		from = tr.getRequest().getParameter("email_from");
		if (from == null || !EmailValidator.getInstance().isValid(from)) {
			from = UserMailSender.getInstance().getFromMail();
		}

		// to
		to = tr.getRequest().getParameter("email_recipient");

		// subject
		subject = tr.getRequest().getParameter("email_subject");
		if (subject == null || subject.trim().isEmpty()) {
			subject = "[FAM] no subject"; // INTLANG
		}

		// message
		msg = tr.getRequest().getParameter("email_message");
		if (msg == null || msg.trim().isEmpty()) {
			msg = "[FAM] no message"; // INTLANG
		}
	}

	protected boolean isValid() {
		return this.to != null && EmailValidator.getInstance().isValid(to);
	}

	/**
	 * send the email and return an errormessage. if errormessage is empty,
	 * sending succeeded.
	 * 
	 * @param post
	 *            getting the input stream from
	 * @param customid
	 *            for the to send via email
	 * @return an errormessage (may empty on success)
	 */
	public String send(PostMethod post, String customid) {
		String errormessage = "";
		if (this.isValid()) {

			File file = null;
			try {
				file = this.getTmpFile(customid);

			} catch (IOException e) {
				FamLog.exception(e, 201106131728l);
				errormessage += "Fail: Create tmp file [201106131729].";
			}

			try {
				InputStream is = post.getResponseBodyAsStream();

				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);

				int bite = 0;
				while ((bite = is.read()) >= 0) {
					bos.write(bite);
				}
				bos.flush();
				bos.close();
			} catch (IOException e) {
				errormessage += "Fail: Write pdf to tmp file [201106141055].";
				FamLog.exception(e, 201106131733l);
			}

			Email mail = this.getEMail(file, this);
			if (mail == null) {
				errormessage += "Fail: Create e-mail object. Please check log files [201106141058].";
			}
			boolean succ = UserMailSender.sendWithoutUserBox(mail);
			if (!succ) {
				errormessage += "Fail: Send email through configured server. Please check log files [201106131756].";
			}

		} else {
			if (this.getTo() == null) {
				errormessage += "Fail: Find an recipient - form email_recipient sent? [201106131757]";
			} else {
				errormessage += "Invalid email address. Recheck email recipient.";
			}
		}
		return errormessage;
	}

	private DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

	private File getTmpFile(String customid) throws IOException {
		String fullpath = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + this.df.format(new Date()) + "-" + customid + ".pdf";
		File result = new File(fullpath);
		result.createNewFile();
		result.setWritable(true);
		return result;
	}

	private Email getEMail(File file, EMailLetterAdapter mailIn) {
		// attachment
		EmailAttachment attachment = new EmailAttachment();
		attachment.setPath(file.getAbsolutePath());
		attachment.setDisposition(EmailAttachment.ATTACHMENT);
		attachment.setDescription("Attachment"); // INTLANG
		attachment.setName(file.getName()); // INTLANG

		// email message
		MultiPartEmail email = new MultiPartEmail();
		try {
			// basics
			email.addTo(mailIn.getTo());
			email.setFrom(mailIn.getFrom());
			email.setSubject(mailIn.getSubject());
			email.setMsg(mailIn.getMsg());

			// add attachment
			email.attach(attachment);
		} catch (EmailException e) {
			FamLog.exception(e, 201106131739l);
			email = null;
		}
		return email;
	}
}
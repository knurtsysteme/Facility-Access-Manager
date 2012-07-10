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
package de.knurt.fam.core.model.config;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.util.mail.UserMailSender;

/**
 * send emails that are saved in past to send it later.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090913
 */
class CronjobActionMailsSendSavedAndUnsent implements CronjobAction {
	/** one and only instance of CronjobActionMailsSendSavedAndUnsent */
	private volatile static CronjobActionMailsSendSavedAndUnsent me;

	/** construct CronjobActionMailsSendSavedAndUnsent */
	private CronjobActionMailsSendSavedAndUnsent() {
	}

	/**
	 * return the one and only instance of CronjobActionMailsSendSavedAndUnsent
	 * 
	 * @return the one and only instance of CronjobActionMailsSendSavedAndUnsent
	 */
	public static CronjobActionMailsSendSavedAndUnsent getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (CronjobActionMailsSendSavedAndUnsent.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new CronjobActionMailsSendSavedAndUnsent();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of CronjobActionMailsSendSavedAndUnsent
	 */
	public static CronjobActionMailsSendSavedAndUnsent me() {
		return getInstance();
	}

	/**
	 * call {@link UserMailSender#sendUserMails()}. log count of sent mails when
	 * mails have been sent.
	 * 
	 * @param ca
	 *            action not needed here
	 */
	@Override
	public void resolve() {
		int sent = UserMailSender.sendUserMails();
		if (sent > 0) {
			FamLog.info("mails: " + sent + " sent", 201012031755l);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getDescription() {
		return "mail: send saved and unsent"; // INTLANG
	}

	/** {@inheritDoc} */
	@Override
	public int resolveEvery() {
		return 5;
	}
}

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

import java.util.Calendar;
import java.util.List;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.util.mail.OutgoingUserMailBox;

/**
 * inform users that account expired last 24 hours an email.
 * 
 * @author Daniel Oltmanns
 * @since 1.6.1 (01/11/2012)
 */
public class CronjobActionMailsAccountExpired implements CronjobAction {

	/** one and only instance of me */
	private volatile static CronjobActionMailsAccountExpired me;

	/** construct me */
	private CronjobActionMailsAccountExpired() {
	}

	/**
	 * return the one and only instance of CronjobActionMailsAccountExpired
	 * 
	 * @return the one and only instance of CronjobActionMailsAccountExpired
	 */
	protected static CronjobActionMailsAccountExpired getInstance() {
		if (me == null) { // no instance so far
			synchronized (CronjobActionMailsAccountExpired.class) {
				if (me == null) { // still no instance so far
					me = new CronjobActionMailsAccountExpired(); // the one and
					// only
				}
			}
		}
		return me;
	}

	/**
	 * send emails to every person which account expired yesterday.
	 */
	@Override
  public void resolve() {
		Calendar yesterday = Calendar.getInstance();
		yesterday.add(Calendar.DAY_OF_YEAR, -1);
		List<User> users = FamDaoProxy.userDao().getUsersAccountExpired(yesterday.getTime());
		if (users.size() > 0) {
			for (User user : users) {
				OutgoingUserMailBox.sendMail_yourAccountExpired(user);
			}
		}
		FamLog.info("cronjob mails: " + users.size() + " inform about account expired ", 201101111459l);
	}

	/** {@inheritDoc} */
	@Override
	public String getDescription() {
		return "mail: inform users about their expired account"; // INTLANG
	}

	/** {@inheritDoc} */
	@Override
	public int resolveEvery() {
		return 1440;
	}
}

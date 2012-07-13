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

import java.util.List;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.RoleConfigDao;
import de.knurt.fam.core.util.mail.OutgoingUserMailBox;

/**
 * send emails to administrators if there are applications for the system.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090913
 */
public class CronjobActionMailsApplicationsForSystem implements CronjobAction {
	/** one and only instance of CronjobActionMailsApplicationsForSystem */
	private volatile static CronjobActionMailsApplicationsForSystem me;

	/** construct CronjobActionMailsApplicationsForSystem */
	private CronjobActionMailsApplicationsForSystem() {
	}

	/**
	 * return the one and only instance of
	 * CronjobActionMailsApplicationsForSystem
	 * 
	 * @return the one and only instance of
	 *         CronjobActionMailsApplicationsForSystem
	 */
	public static CronjobActionMailsApplicationsForSystem getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (CronjobActionMailsApplicationsForSystem.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new CronjobActionMailsApplicationsForSystem();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of
	 *         CronjobActionMailsApplicationsForSystem
	 */
	public static CronjobActionMailsApplicationsForSystem me() {
		return getInstance();
	}

	/**
	 * send emails to administrators if there are applications for the system.
	 * applicants for the system needs a decision (if it is spam or not). this
	 * sends reminder emails to all administrators, when applications for the
	 * system are there.
	 * 
	 * @param ca
	 *            the action (not needed here)
	 */
	@Override
	public void resolve() {
		List<User> users = FamDaoProxy.userDao().getAllUsersWithoutAccount();
		if (users.size() > 0) {
			List<User> admins = RoleConfigDao.getInstance().getAdmins();
			for (User admin : admins) {
				OutgoingUserMailBox.sendMail_applicationsForSystem(admin, users.size());
			}
			FamLog.info("mails: " + users.size() + " users sent to " + admins.toString(), 201012031749l);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getDescription() {
		return "mail: applications for the fam"; // INTLANG
	}

	/** {@inheritDoc} */
	@Override
	public int resolveEvery() {
		return 5;
	}
}

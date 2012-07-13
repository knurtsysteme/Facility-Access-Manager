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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.persistence.dao.BookingDao;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.util.mail.OutgoingUserMailBox;

/**
 * send emails to operators for applications made on its facilities.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090913
 */
public class CronjobActionMailsApplicationsForResource implements CronjobAction {

	/** one and only instance of CronjobActionMailsApplicationsForResource */
	private volatile static CronjobActionMailsApplicationsForResource me;

	/** construct CronjobActionMailsApplicationsForResource */
	private CronjobActionMailsApplicationsForResource() {
	}

	/**
	 * return the one and only instance of
	 * CronjobActionMailsApplicationsForResource
	 * 
	 * @return the one and only instance of
	 *         CronjobActionMailsApplicationsForResource
	 */
	public static CronjobActionMailsApplicationsForResource getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (CronjobActionMailsApplicationsForResource.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new CronjobActionMailsApplicationsForResource();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of
	 *         CronjobActionMailsApplicationsForResource
	 */
	public static CronjobActionMailsApplicationsForResource me() {
		return getInstance();
	}

	/**
	 * send emails to operators for applications made on its facilities. check
	 * all uncanceled applications not made and if there are some, get the
	 * operators and send an email to them to remind, that they have to decide
	 * about the applications.
	 * 
	 * @see BookingDao#getAllUncanceledApplicationsNotMade()
	 * @param ca
	 *            to resolve. not needed here.
	 */
	@Override
	public void resolve() {
		List<Booking> applications = FamDaoProxy.bookingDao().getAllUncanceledApplicationsNotMade();
		if (applications.size() > 0) {
			// get all facilities
			List<String> facilityKeysWithApplications = new ArrayList<String>();
			for (Booking application : applications) {
				if (!facilityKeysWithApplications.contains(application.getFacilityKey())) {
					facilityKeysWithApplications.add(application.getFacilityKey());
				}
			}

			// count applications per facility
			Map<String, Integer> facilityKeyApplicationsCounter = new HashMap<String, Integer>();
			for (String facilityKey : facilityKeysWithApplications) {
				int tmp = 0;
				for (Booking application : applications) {
					if (application.getFacilityKey().equals(facilityKey)) {
						tmp++;
					}
				}
				facilityKeyApplicationsCounter.put(facilityKey, tmp);
			}

			boolean mailSent = false;
			for (String facilityKey : facilityKeysWithApplications) {
				List<User> recipients =  FamDaoProxy.userDao().getResponsibleUsers(FacilityConfigDao.facility(facilityKey));
				if (recipients.size() > 0) {
					Facility facility = FacilityConfigDao.facility(facilityKey);
					for (User recipient : recipients) {
						if (recipient != null) {
							mailSent = true;
							OutgoingUserMailBox.sendMail_applicationsForResource(recipient, facility, facilityKeyApplicationsCounter.get(facilityKey));
						}
					}
				}
			}
			if (mailSent) {
				FamLog.info("mails: applications: " + applications.size() + " sent", 201012031756l);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getDescription() {
		return "mail: applications for resource"; // INTLANG
	}

	/** {@inheritDoc} */
	@Override
	public int resolveEvery() {
		return 1440;
	}
}

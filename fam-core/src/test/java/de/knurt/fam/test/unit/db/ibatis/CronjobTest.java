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

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.config.CronjobAction;
import de.knurt.fam.core.model.config.CronjobActionContainer;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.UserMail;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class CronjobTest extends FamIBatisTezt {

	/**
     *
     */
	@Test
	public void sendMailOnApplication4System() {
		this.clearDatabase();

		// create and save applicant
		User applicant = TeztBeanSimpleFactory.getNewUniqueValidUser("foo");
		applicant.setExcluded(null);
		applicant.insert();

		// create and save admin
		User admin = TeztBeanSimpleFactory.getAdmin();
		admin.insert();

		// assert no mails yet
		List<UserMail> mails = FamDaoProxy.userDao().getAllUserMails();
		assertEquals(0, mails.size());

		// resolve cronjob action
		CronjobActionContainer.getInstance().resolveAll();

		// assert one mail for admin now
		mails = FamDaoProxy.userDao().getAllUserMails();
		assertEquals(1, mails.size());
		UserMail got = mails.get(0);
		assertTrue("wrong username: " + got.getUsername(), FamDaoProxy.userDao().getUserFromUsername(got.getUsername()).isAdmin());

	}

	@Test
	public void constructCronjobActions() {
		this.clearDatabase();
		List<CronjobAction> cas = CronjobActionContainer.getInstance().getAllCronjobActions();
		assertTrue(cas.size() > 0);
		for (CronjobAction ca : cas) {
			String description = ca.getDescription();
			assertNotNull(description);
			assertFalse(description.isEmpty());
		}
	}
}

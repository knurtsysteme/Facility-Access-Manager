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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.document.SoaActivationDocument;
import de.knurt.fam.template.util.TermsOfUseResolver;
import de.knurt.fam.test.utils.AssertSomehowEquals;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class DiscardAcceptanceForUsersTest extends FamIBatisTezt {

	@Test
	public void discardAcceptance() {
		this.clearDatabase();

		User extern = this.insert("extern");
		assertTrue(extern.isAcceptedStatementOfAgreement());

		User intern = this.insert("intern");
		assertTrue(intern.isAcceptedStatementOfAgreement());

		// discard for intern
		SoaActivationDocument newSoaActivationDocument = new SoaActivationDocument();
		newSoaActivationDocument.setRoleId("intern");
		newSoaActivationDocument.setActivatedOn(10l);
		new TermsOfUseResolver(TeztBeanSimpleFactory.getAdmin()).discardAcceptanceForUsers(newSoaActivationDocument);

		List<User> users = FamDaoProxy.userDao().getAll();
		assertEquals(2, users.size());
		if (users.get(0).getRoleId().equals("extern")) {
			extern = users.get(0);
			intern = users.get(1);
		} else {
			intern = users.get(0);
			extern = users.get(1);
		}

		// intern is not accepted, extern still is
		assertTrue(extern.isAcceptedStatementOfAgreement());
		assertFalse(intern.isAcceptedStatementOfAgreement());
	}

	@Test
	public void userProperties() {
		this.clearDatabase();
		
		User admin = TeztBeanSimpleFactory.getAdmin();

		User internUserBefore = this.insert("intern");
		assertTrue(internUserBefore.isAcceptedStatementOfAgreement());

		// discard for intern
		SoaActivationDocument newSoaActivationDocument = new SoaActivationDocument();
		newSoaActivationDocument.setRoleId("intern");
		newSoaActivationDocument.setActivatedOn(10l);
		new TermsOfUseResolver(admin).discardAcceptanceForUsers(newSoaActivationDocument);

		List<User> users = FamDaoProxy.userDao().getAll();

		User internUserAfter = users.get(0);
		assertFalse(internUserAfter.isAcceptedStatementOfAgreement());

		internUserBefore.setAcceptedStatementOfAgreement(false);

		AssertSomehowEquals.test(internUserBefore, internUserAfter);
	}

	private User insert(String roleId) {
		User user = TeztBeanSimpleFactory.getNewUniqueValidUser("a" + roleId);
		user.setRoleId(roleId);
		user.setAcceptedStatementOfAgreement(true);
		user.insert();
		return user;
	}
}
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.config.RoleConfigDao;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class AccessRoleTest extends FamIBatisTezt {

	/**
     *
     */
	@Test
	public void getAdmins() {
		this.clearDatabase();
		User admin = TeztBeanSimpleFactory.getAdmin();
		admin.insert();
		List<User> admins = RoleConfigDao.getInstance().getAdmins();
		assertTrue(admins.size() > 0);

		for (User adminGot : admins) {
			assertNotNull(adminGot);
			assertTrue(adminGot.isAdmin());
		}
		List<User> admins2 = RoleConfigDao.getInstance().getAdmins();
		assertEquals(admins, admins2);
	}

	/**
     *
     */
	@Test
	public void getEncoder() {
		User user = UserFactory.me().blank();
		user.setUsername("daoltman");
		assertEquals("admin", RoleConfigDao.getInstance().getRoleId(user));
		user.setUsername("not_daoltman");
		assertEquals("extern", RoleConfigDao.getInstance().getRoleId(user));
	}
}

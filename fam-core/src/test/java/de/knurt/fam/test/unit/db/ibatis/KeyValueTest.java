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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.test.utils.FamIBatisTezt;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class KeyValueTest extends FamIBatisTezt {

	@Test
	public void insertUpdateDelete() {
		this.clearDatabase();
		assertEquals(0, FamDaoProxy.keyValueDao().getAll().size());
		assertFalse(FamDaoProxy.keyValueDao().exists("a"));
		FamDaoProxy.keyValueDao().put("a", "b");
		assertEquals(1, FamDaoProxy.keyValueDao().getAll().size());
		assertTrue(FamDaoProxy.keyValueDao().exists("a"));
		assertEquals("b", FamDaoProxy.keyValueDao().value("a"));
		FamDaoProxy.keyValueDao().put("a", "c");
		assertEquals(1, FamDaoProxy.keyValueDao().getAll().size());
		assertEquals("c", FamDaoProxy.keyValueDao().value("a"));
		FamDaoProxy.keyValueDao().delete("a");
		assertEquals(0, FamDaoProxy.keyValueDao().getAll().size());
	}
}

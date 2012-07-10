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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.config.LogbookConfigDao;
import de.knurt.fam.core.model.config.Logbook;
import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class AccessLogbookTest extends FamIBatisTezt {

	@Test
	public void getNewestEntry() {
		this.clearDatabase();
		assertEquals(0, FamDaoProxy.logbookEntryDao().getAll().size());
		String logbookKey = TeztBeanSimpleFactory.LOGBOOK_ID1;
		for (Logbook logbook : LogbookConfigDao.getInstance().getAll()) {
			logbook.setEntryCount(-1);
			logbook.setNewestEntry(null);
		}
		LogbookEntry e1 = LogbookConfigDao.getInstance().getNewestEntry(logbookKey);
		assertEquals(0, FamDaoProxy.logbookEntryDao().getAll().size());
		String mess = "";
		if (e1 != null) {
			mess = e1.getLogbookId() + " / " + e1.getHeadline() + " / " + e1.getContent();
		}
		assertNull("is not null: " + mess, e1);

		LogbookEntry insert1 = TeztBeanSimpleFactory.getNewValidLogbookEntry();
		insert1.setLogbookId(logbookKey);
		insert1.insert();
		e1 = LogbookConfigDao.getInstance().getNewestEntry(logbookKey);

		assertNotNull(e1);
		assertEquals(e1.getClass(), LogbookEntry.class);
		assertEquals(e1, insert1);

		// all that again with another logbookKey
		logbookKey = TeztBeanSimpleFactory.LOGBOOK_ID2;
		LogbookEntry e2 = LogbookConfigDao.getInstance().getNewestEntry(logbookKey);
		assertNull(e2);

		LogbookEntry insert2 = TeztBeanSimpleFactory.getNewValidLogbookEntry();
		insert2.setLogbookId(logbookKey);
		insert2.insert();
		e2 = LogbookConfigDao.getInstance().getNewestEntry(logbookKey);

		assertNotNull(e2);
		assertEquals(e2.getClass(), LogbookEntry.class);
		assertEquals(e2, insert2);

		assertFalse(e1.equals(e2));
	}

	@Autowired
	private LogbookConfigDao accessLogbook;

	/**
     *
     */
	@Test
	public void accessLogbook() {
		assertEquals(LogbookConfigDao.class, accessLogbook.getClass());
		assertSame(LogbookConfigDao.getInstance(), accessLogbook);
	}

	/**
     *
     */
	@Test
	public void shortcuts() {
		String shorty = accessLogbook.getLabel(TeztBeanSimpleFactory.LOGBOOK_ID);
		assertNotNull(shorty);
		assertTrue(shorty.equals("") == false);

		shorty = accessLogbook.getDescription(TeztBeanSimpleFactory.LOGBOOK_ID);
		assertNotNull(shorty);
		assertTrue(shorty.equals("") == false);
	}

	/**
     *
     */
	@SuppressWarnings("unchecked")
	@Test
	public void accessLogbookLabels() {
		Properties labels = accessLogbook.getLabels();

		assertTrue(labels.size() >= 1); // facility access
		Enumeration<String> names = (Enumeration<String>) labels.propertyNames();
		while (names.hasMoreElements()) {
			String key = names.nextElement();
			String desc = labels.getProperty(key);
			assertFalse("value " + desc + " is key", desc.startsWith(key));
			assertNotNull(desc);
			assertFalse(desc.equals(""));
		}
	}

	/**
     *
     */
	@Test
	public void accessLogbookKeys() {
		Set<String> keys = accessLogbook.getKeys();
		assertEquals(accessLogbook.getLabels().size(), keys.size());
	}

	/**
     *
     */
	@SuppressWarnings("unchecked")
	@Test
	public void accessLogbookDescriptions() {
		Properties descs = accessLogbook.getDescriptions();

		assertTrue(descs.size() >= 1); // facility access
		Enumeration<String> names = (Enumeration<String>) descs.propertyNames();
		while (names.hasMoreElements()) {
			String key = names.nextElement();
			String desc = descs.getProperty(key);
			assertFalse("value " + desc + " is key", desc.startsWith(key));
			assertNotNull(desc);
			assertFalse(desc.equals(""));
		}
	}

	/**
     *
     */
	@Test
	public void accessLogbookEntryCount_askInsert() {
		this.clearDatabase();
		int entryCount = accessLogbook.getEntryCount(TeztBeanSimpleFactory.LOGBOOK_ID4);
		// no entries so far
		assertEquals(0, entryCount);
		LogbookEntry le = TeztBeanSimpleFactory.getNewValidLogbookEntry();
		le.setLogbookId(TeztBeanSimpleFactory.LOGBOOK_ID4);
		le.insert();
		entryCount = accessLogbook.getEntryCount(TeztBeanSimpleFactory.LOGBOOK_ID4);
		assertEquals(1, entryCount);
		LogbookConfigDao.getInstance().addLastEntry(le);
		assertEquals(2, LogbookConfigDao.getInstance().getEntryCount(TeztBeanSimpleFactory.LOGBOOK_ID4));
		LogbookConfigDao.getInstance().addLastEntry(le);
		assertEquals(3, LogbookConfigDao.getInstance().getEntryCount(TeztBeanSimpleFactory.LOGBOOK_ID4));
	}
}

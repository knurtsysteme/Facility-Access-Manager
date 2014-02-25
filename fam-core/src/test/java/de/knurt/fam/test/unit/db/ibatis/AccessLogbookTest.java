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

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.config.Logbook;
import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.LogbookConfigDao;
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
  public void getUniqueNewestEntry() {
    this.clearDatabase();
    assertEquals(0, FamDaoProxy.logbookEntryDao().getAll().size());
    Logbook lb1 = LogbookConfigDao.getInstance().get(TeztBeanSimpleFactory.LOGBOOK_ID1);
    Logbook lb2 = LogbookConfigDao.getInstance().get(TeztBeanSimpleFactory.LOGBOOK_ID2);
    LogbookEntry le1 = TeztBeanSimpleFactory.getNewValidLogbookEntry();
    le1.setLogbookId(lb1.getKey());
    le1.insert();
    assertEquals(1, FamDaoProxy.logbookEntryDao().getAll().size());
    assertEquals(lb1.getNewestEntry().getId(), le1.getId());
    assertNull(lb2.getNewestEntry());
  }
  @Test
  public void getNewestEntry() {
    this.clearDatabase();
    assertEquals(0, FamDaoProxy.logbookEntryDao().getAll().size());
    String logbookKey = TeztBeanSimpleFactory.LOGBOOK_ID1;
    LogbookEntry e1 = LogbookConfigDao.getInstance().get(logbookKey).getNewestEntry();
    assertEquals(0, FamDaoProxy.logbookEntryDao().getAll().size());
    String mess = "";
    if (e1 != null) {
      mess = e1.getLogbookId() + " / " + e1.getHeadline() + " / " + e1.getContent();
    }
    assertNull("is not null: " + mess, e1);

    LogbookEntry insert1 = TeztBeanSimpleFactory.getNewValidLogbookEntry();
    insert1.setLogbookId(logbookKey);
    insert1.insert();
    e1 = LogbookConfigDao.getInstance().get(logbookKey).getNewestEntry();

    assertNotNull(e1);
    assertEquals(e1.getClass(), LogbookEntry.class);

    // all that again with another logbookKey
    logbookKey = TeztBeanSimpleFactory.LOGBOOK_ID2;
    LogbookEntry e2 = LogbookConfigDao.getInstance().get(logbookKey).getNewestEntry();
    assertNull(e2);

    LogbookEntry insert2 = TeztBeanSimpleFactory.getNewValidLogbookEntry();
    insert2.setLogbookId(logbookKey);
    insert2.insert();
    e2 = LogbookConfigDao.getInstance().get(logbookKey).getNewestEntry();

    assertNotNull(e2);
    assertEquals(e2.getClass(), LogbookEntry.class);

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
    Logbook l = accessLogbook.get(TeztBeanSimpleFactory.LOGBOOK_ID);
    String shorty = l.getLabel();
    assertNotNull(shorty);
    assertTrue(shorty.equals("") == false);

    shorty = l.getDescription();
    assertNotNull(shorty);
    assertTrue(shorty.equals("") == false);
  }

  /**
     *
     */
  @Test
  public void accessLogbookEntryCount_askInsert() {
    this.clearDatabase();
    Logbook l = accessLogbook.get(TeztBeanSimpleFactory.LOGBOOK_ID4);
    int entryCount = l.getEntryCount();
    // no entries so far
    assertEquals(0, entryCount);
    LogbookEntry le = TeztBeanSimpleFactory.getNewValidLogbookEntry();
    le.setLogbookId(TeztBeanSimpleFactory.LOGBOOK_ID4);
    le.insert();
    entryCount = l.getEntryCount();
    assertEquals(1, entryCount);
  }

  @Test
  public void logbookHasVisibility() {
    User admin = TeztBeanSimpleFactory.getAdmin();
    User extern = TeztBeanSimpleFactory.getNewValidUser();
    Logbook adminLogbook = TeztBeanSimpleFactory.getAdminLogbook();
    assertTrue(adminLogbook.isVisibleFor(admin));
    assertFalse(adminLogbook.isVisibleFor(extern));
    List<Logbook> adminsLBs = LogbookConfigDao.getInstance().getAllVisibleFor(admin);
    assertEquals(7, adminsLBs.size());
    List<Logbook> externsLBs = LogbookConfigDao.getInstance().getAllVisibleFor(extern);
    assertEquals(6, externsLBs.size());
  }
}

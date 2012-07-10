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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.LogbookEntryDao;
import de.knurt.fam.core.control.persistence.dao.ibatis.LogbookEntryDao4ibatis;
import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.test.utils.AssertSomehowEquals;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * insert and update is doing in on operation "store" in db4o - so 
 * only inserting is not seen as a problem here.
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class ReadWriteLogbookEntryTest extends FamIBatisTezt {


    @Test
    public void constructDao() {
        this.clearDatabase();
        LogbookEntryDao ledao = FamDaoProxy.getInstance().getLogbookEntryDao();
            assertEquals(LogbookEntryDao4ibatis.class, ledao.getClass());
    }

    @Test
    public void logLang() {
        this.clearDatabase();
        LogbookEntry le = TeztBeanSimpleFactory.getNewValidLogbookEntry();
        assertEquals(le.getLanguage(), Locale.ENGLISH);
        le.insert();
        LogbookEntry back = FamDaoProxy.logbookEntryDao().getNewestEntry();
        assertEquals(back.getLanguage(), Locale.ENGLISH);
    }

    @Test
    public void getFromTo() {
        this.clearDatabase();

        // insert ten entries
        int size = 10;
        ArrayList<LogbookEntry> putLes = new ArrayList<LogbookEntry>();
        while (size-- > 0) {
            LogbookEntry le = TeztBeanSimpleFactory.getNewValidLogbookEntry();
            le.setLogbookId(TeztBeanSimpleFactory.LOGBOOK_ID);
            le.setDate(new Date(size * 100000 + 2)); // from new to old
            putLes.add(le);
            le.insert();
        }

        // insert a new entry of another logbook
        LogbookEntry leNew = TeztBeanSimpleFactory.getNewValidLogbookEntry();
        leNew.setLogbookId(TeztBeanSimpleFactory.LOGBOOK_ID2);
        leNew.setDate(new Date(1)); // new!

        // got first 3 entries
        List<LogbookEntry> gotLes = FamDaoProxy.logbookEntryDao().get(TeztBeanSimpleFactory.LOGBOOK_ID, 0, 3);
        assertEquals(3, gotLes.size());
        AssertSomehowEquals.test(putLes.get(0), gotLes.get(0));
        AssertSomehowEquals.test(putLes.get(1), gotLes.get(1));
        AssertSomehowEquals.test(putLes.get(2), gotLes.get(2));

        // got entries from 4 to 8
        gotLes = FamDaoProxy.logbookEntryDao().get(TeztBeanSimpleFactory.LOGBOOK_ID, 4, 8);
        assertEquals(4, gotLes.size());
        AssertSomehowEquals.test(putLes.get(4), gotLes.get(0));
        AssertSomehowEquals.test(putLes.get(5), gotLes.get(1));
        AssertSomehowEquals.test(putLes.get(6), gotLes.get(2));
        AssertSomehowEquals.test(putLes.get(7), gotLes.get(3));

        // check insane entries
        gotLes = FamDaoProxy.logbookEntryDao().get(TeztBeanSimpleFactory.LOGBOOK_ID, 8, 4);
        assertEquals(new ArrayList<LogbookEntry>(), gotLes);
        gotLes = FamDaoProxy.logbookEntryDao().get(TeztBeanSimpleFactory.LOGBOOK_ID, -1, 4);
        assertEquals(new ArrayList<LogbookEntry>(), gotLes);
        gotLes = FamDaoProxy.logbookEntryDao().get(TeztBeanSimpleFactory.LOGBOOK_ID, 0, 9999999);
        assertEquals(putLes.size(), gotLes.size());

        // check first entry only
        gotLes = FamDaoProxy.logbookEntryDao().get(TeztBeanSimpleFactory.LOGBOOK_ID, 0, 1);
        assertEquals(1, gotLes.size());
    }

    @Test
    public void writeSimple() {
        this.clearDatabase();
        LogbookEntry le = TeztBeanSimpleFactory.getNewValidLogbookEntry();
        le.insert();
        List<LogbookEntry> les = FamDaoProxy.getInstance().getLogbookEntryDao().getObjectsLike(le);
        assertEquals(1, les.size());
    }

    @Test
    public void idSet() {
        this.clearDatabase();
        LogbookEntry le1 = TeztBeanSimpleFactory.getNewValidLogbookEntry();
        le1.insert();
        assertTrue(le1.getId() >= 0);

        LogbookEntry le2 = TeztBeanSimpleFactory.getNewValidLogbookEntry();
        le2.insert();
        assertTrue(le2.getId() >= 1);

        assertTrue(le1.getId() + 1 == le2.getId());
    }

    @Test
    public void readAll() {
        this.clearDatabase();
        LogbookEntry le1 = TeztBeanSimpleFactory.getNewValidLogbookEntry();
        LogbookEntry le2 = TeztBeanSimpleFactory.getNewValidLogbookEntry();
        le1.setDate(new Date(1)); // old
        le2.setDate(new Date()); // new
        le1.insert();
        le2.insert();
        List<LogbookEntry> les = FamDaoProxy.getInstance().getLogbookEntryDao().getAll();
        // test newest first
        assertEquals(le2.getDate().toString(), les.get(0).getDate().toString());
        assertEquals(le1.getDate().toString(), les.get(1).getDate().toString());

        // set new to older
        le1.setDate(new Date()); // new
        le2.setDate(new Date(1)); // old
        le1.update();
        le2.update();
        les = FamDaoProxy.getInstance().getLogbookEntryDao().getAll();
        // test newest first
        assertEquals(le1.getDate().toString(), les.get(0).getDate().toString());
        assertEquals(le2.getDate().toString(), les.get(1).getDate().toString());
    }

    @Test
    public void setget() {
        LogbookEntry le = new LogbookEntry();

        try {
            le.insert();
            fail("should thrown");
        } catch (DataIntegrityViolationException e) {
            assertTrue("thrown!", true);
        }

        le = TeztBeanSimpleFactory.getNewValidLogbookEntry();
        try {
            le.insert();
            assertTrue("not thrown!", true);
        } catch (DataIntegrityViolationException e) {
            fail("should not thrown: " + e.getMessage());
        }

        le.setLogbookId("foo is not valid");
        try {
            le.insert();
            fail("should thrown");
        } catch (DataIntegrityViolationException e) {
            assertTrue("thrown!", true);
        }

        le = TeztBeanSimpleFactory.getNewValidLogbookEntry();
        le.setOfUserName("foo is not valid");
        try {
            le.insert();
            fail("should thrown");
        } catch (DataIntegrityViolationException e) {
            assertTrue("thrown!", true);
        }

        le = TeztBeanSimpleFactory.getNewValidLogbookEntry();
        le.setTagsFromCsv(null); // must have tags
        try {
            le.insert();
            fail("should thrown");
        } catch (DataIntegrityViolationException e) {
            assertTrue("thrown!", true);
        }
    }
}

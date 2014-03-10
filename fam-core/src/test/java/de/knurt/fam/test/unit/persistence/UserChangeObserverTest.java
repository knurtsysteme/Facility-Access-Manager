/*
 * Copyright 2014 by KNURT Systeme (http://www.knurt.de)
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
package de.knurt.fam.test.unit.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.config.LogbookUserObserver;
import de.knurt.fam.core.model.persist.ContactDetail;
import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class UserChangeObserverTest extends FamIBatisTezt {

  /**
     *
     */
  public UserChangeObserverTest() {

  }

  /**
   * 
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  /**
   * 
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  /**
     *
     */
  @Before
  public void setUp() {
  }

  /**
     *
     */
  @After
  public void tearDown() {
  }

  /**
     *
     */
  @Test
  public void insertUser_logbookKnowsThat() {
    this.clearDatabase();
    LogbookUserObserver logbook = TeztBeanSimpleFactory.getUserObserverLogbook();
    int entryCountBefore = logbook.getEntryCount();
    User user = TeztBeanSimpleFactory.getAdmin();
    user.insert();
    int entryCountAfter = logbook.getEntryCount();
    assertEquals(entryCountBefore + 1, entryCountAfter);
  }

  @Test
  public void userCrud_logbookKnowsThat() {
    this.clearDatabase();
    LogbookUserObserver logbook = TeztBeanSimpleFactory.getUserObserverLogbook();
    User user = TeztBeanSimpleFactory.getAdmin();

    // test insertion
    user.insert();
    String content = logbook.getNewestEntry().getContent();
    assertEquals(user.getFullName() + " was inserted with the role " + user.getRoleLabel(), content);
    logbook.getNewestEntry().delete();

    // test simpe update
    int entryCountBefore = logbook.getEntryCount();
    String newCity = "another city";
    User user2 = FamDaoProxy.userDao().getUserFromUsername(user.getUsername());
    user2.setCity(newCity);
    user2.update();
    int entryCountAfter = logbook.getEntryCount();
    assertEquals(entryCountBefore + 1, entryCountAfter);
    assertTrue(logbook.getNewestEntry().getContent().contains(newCity));
    content = logbook.getNewestEntry().getContent();
    assertEquals("added value for city: " + newCity, content);
    logbook.getNewestEntry().delete();

    // test complex update
    user2.setCity("Springfield");
    user2.setSname("Miller");
    JSONObject customFields = TeztBeanSimpleFactory.getCustomFields();
    user2.setCustomFields(customFields);
    user2.update();
    content = logbook.getNewestEntry().getContent();
    assertTrue(content.contains("changed value of sname from Meier to Miller"));
    assertTrue(content.contains("changed value of city from another city to Springfield"));
    assertTrue(content.contains("a new field foo = [\"foo value\"]"));
    assertTrue(content.contains("a new field bar = [\"bar value\"]"));
    assertFalse(content.contains("fname"));
    logbook.getNewestEntry().delete();
    assertEquals(0, logbook.getAllEntries().size());

    // test user delete
    user2.delete();
    assertEquals(1, logbook.getAllEntries().size());
    content = logbook.getNewestEntry().getContent();
    assertEquals(content, "Peter Miller was deleted");
  }

  @Test
  public void anonymizeUser() {
    this.clearDatabase();

    User u1 = TeztBeanSimpleFactory.getNewValidUser();
    u1.insert();
    String usernameBefore = u1.getUsername();
    assertFalse(u1.isAnonym());

    User auth = TeztBeanSimpleFactory.getAdmin();

    LogbookUserObserver logbook = TeztBeanSimpleFactory.getUserObserverLogbook();
    for (LogbookEntry le : logbook.getAllEntries()) {
      le.delete();
    }
    assertEquals(0, logbook.getAllEntries().size());
    assertTrue(u1.anonymize(auth));
    assertEquals("anonym", u1.getUsername());
    assertEquals(1, logbook.getAllEntries().size());
    String content = logbook.getNewestEntry().getContent();
    assertEquals(usernameBefore + " is anonym now", content);
  }

  @Test
  public void addContactDetail() {
    this.clearDatabase();
    LogbookUserObserver logbook = TeztBeanSimpleFactory.getUserObserverLogbook();
    User user = TeztBeanSimpleFactory.getAdmin();
    user.insert();
    logbook.getNewestEntry().delete();
    ContactDetail cd = new ContactDetail();
    cd.setUsername(user.getUsername());
    cd.setDetail("puff");
    cd.setTitle("paff");
    cd.insert();
    String content = logbook.getNewestEntry().getContent();
    assertTrue(content.contains("puff"));
    assertTrue(content.contains("paff"));
  }
}

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
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.config.LogbookUserObserver;
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
  public void updateUser_logbookKnowsThat() {
    this.clearDatabase();
    LogbookUserObserver logbook = TeztBeanSimpleFactory.getUserObserverLogbook();
    User user = TeztBeanSimpleFactory.getAdmin();
    user.insert();
    logbook.getNewestEntry().delete();
    int entryCountBefore = logbook.getEntryCount();
    String newCity = "another city";
    User user2 = FamDaoProxy.userDao().getUserFromUsername(user.getUsername());
    user2.setCity(newCity);
    user2.update();
    int entryCountAfter = logbook.getEntryCount();
    assertEquals(entryCountBefore + 1, entryCountAfter);
    assertTrue(logbook.getNewestEntry().getContent().contains(newCity));
    // TODO test logbook content
    // TODO test user update
    // TODO test user delete
    // TODO test user anonymize
    // TODO test user contact detail change
  }
}

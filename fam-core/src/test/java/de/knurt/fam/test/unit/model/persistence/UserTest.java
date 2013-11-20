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
package de.knurt.fam.test.unit.model.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.UserFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
public class UserTest {

  /**
     *
     */
  public UserTest() {
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
  public void fullname() {
    User test = UserFactory.me().blank();
    test.setFname("Peter");
    test.setSname("Schneider");
    assertEquals("Peter Schneider", test.getFullName());
    test.setTitle("Dr.");
    assertEquals("Dr. Peter Schneider", test.getFullName());
  }

  @Test
  public void accountExpires() {
    User test = UserFactory.me().blank();

    // default is null and valid
    Date date = test.getAccountExpires();
    assertNull(date);
    assertFalse(test.isAccountExpired());

    // set date now - is expired
    Date now = new Date();
    test.setAccountExpires(now);
    date = test.getAccountExpires();
    assertEquals(now, date);
    assertTrue(test.isAccountExpired());

    // set date in one year - is valid
    Calendar nextYear = Calendar.getInstance();
    nextYear.add(Calendar.YEAR, 1);
    test.setAccountExpires(nextYear.getTime());
    assertFalse(test.isAccountExpired());
  }

  @Test
  public void setAndGetCustomFields() {
    User test = UserFactory.me().blank();
    JSONObject customFields = new JSONObject();
    try {
      customFields.put("foo", "bar");
      customFields.put("off", "rab");
      test.setCustomFields(customFields);
      assertEquals(test.getCustomField("foo"), "bar");
      assertEquals(test.getCustomField("off"), "rab");
      assertNull(test.getCustomField("bla"));
    } catch (JSONException e) {
      fail("should not throw exception");
    }
  }

  @Test
  public void settingSetStreetWithStreetno() {
    User test = UserFactory.me().blank();
    test.setStreetWithStreetno("foo bar 1b ");
    assertEquals("1b", test.getStreetno());
    assertEquals("foo bar", test.getStreet());
    assertEquals("foo bar 1b", test.getStreetWithStreetno());

    test.setStreetWithStreetno("foo");
    assertTrue(test.getStreetno().isEmpty());
    assertEquals("foo", test.getStreet());
  }

  @Test
  public void settingGetStreetWithStreetno() {
    User test = UserFactory.me().blank();
    test.setStreet("foo bar");
    test.setStreetno("23");
    assertEquals("23", test.getStreetno());
    assertEquals("foo bar", test.getStreet());
    assertEquals("foo bar 23", test.getStreetWithStreetno());
  }
}
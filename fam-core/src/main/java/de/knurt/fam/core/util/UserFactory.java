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
package de.knurt.fam.core.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.Address;
import de.knurt.fam.core.model.persist.ContactDetail;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.mvc.Registration;
import de.knurt.fam.template.util.ValueUtil;

/**
 * produce users
 * 
 * @see User
 * @author Daniel Oltmanns
 * @since 1.20 (08/08/2010)
 */
public class UserFactory {
  /** one and only instance of UserFactory */
  private volatile static UserFactory me;

  /** construct UserFactory */
  private UserFactory() {
  }

  /**
   * return the one and only instance of UserFactory
   * 
   * @return the one and only instance of UserFactory
   */
  public static UserFactory getInstance() {
    if (me == null) {
      // ↖ no instance so far
      synchronized (UserFactory.class) {
        if (me == null) {
          // ↖ still no instance so far
          // ↓ the one and only me
          me = new UserFactory();
        }
      }
    }
    return me;
  }

  /**
   * short for {@link #getInstance()}
   * 
   * @return the one and only instance of UserFactory
   */
  public static UserFactory me() {
    return getInstance();
  }

  /**
   * produces a "joe bloggs" user registered today, 
   * account expired in 6 months and his favorite color 
   * is green and he likes tea.
   * 
   * @return job bloggs
   */
  public User getJoeBloggs() {
    User result = this.blank();
    result.setTitle("Dr.");
    result.setFname("Max");
    result.setSname("Mustermann");
    result.setIntendedResearch("Muster Forschungs-Projekt");
    result.setCompany("Muster-Firma");
    result.setDepartmentLabel("Muster-Department");
    result.setMainAddress(this.getJoeBloggsAddress());
    result.setBirthdate("01.01.1970");
    result.setMail("mustermann@musterprovider.de");
    result.setPhone1("555 12345");
    result.setPhone2("555 12345");
    result.setRegistration(new Date());
    result.setUsername("mamuster");
    result.setAcceptedStatementOfAgreement(true);
    result.setMale(true);
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.MONTH, 6);
    result.setAccountExpires(cal.getTime());
    result.setRoleId("extern");
    result.addCustomField("favorite_color", "green");
    result.addCustomField("favorite_drink", "tea");
    return result;
  }

  private Address getJoeBloggsAddress() {
    Address address = new Address();
    address.setCity("Musterstadt");
    address.setCountry("Musterland");
    address.setStreet("Musterstrasse");
    address.setStreetno("1");
    address.setZipcode("12345");
    return address;
  }

  public User blank() {
    return new User();
  }

  public User getUserWithUsername(String username) {
    User result = this.blank();
    result.setUsername(username);
    return result;
  }

  /**
   * return a user from a json object. assert needed attributes set. throw JSONException otherwise
   * 
   * @param user
   * @return
   * @throws JSONException
   * @throws NullPointerException
   */
  public User getNewUserForRegistration(JSONObject user) throws JSONException {
    return this.getRegistration(user).getUser();
  }

  public Registration getRegistration(JSONObject user) throws JSONException {
    Registration result = new Registration();
    result.setTitle(this.getValue(user, "title"));
    result.setFname(this.getValue(user, "fname"));
    result.setIntendedResearch(this.getValue(user, "intendedResearch"));
    result.setSname(this.getValue(user, "sname"));
    result.setCompany(this.getValue(user, "company"));
    result.setDepartmentLabel(this.getValue(user, "departmentlabel"));
    result.setDepartmentKey(this.getValue(user, "departmentkey"));
    result.setMail(this.getValue(user, "mail"));
    result.setPhone1(this.getValue(user, "phone1"));
    result.setPhone2(this.getValue(user, "phone2"));
    result.setPass1(this.getValue(user, "pass"));
    result.setPass2(this.getValue(user, "pass"));
    result.setMale(this.getValue(user, "male"));
    result.setBirthdate(this.getValue(user, "birthdate"));
    result.setAccountExpires(this.getValue(user, "account_expires"));
    result.setCity(this.getValue(user, "city"));
    result.setStreet(this.getValue(user, "street"));
    result.setStreetno(this.getValue(user, "streetno"));
    result.setZipcode(this.getValue(user, "zipcode"));
    result.setCountry(this.getValue(user, "country"));
    if (user.has("customFields")) {
      result.setCustomFields(user.getJSONObject("customFields"));
    }
    return result;
  }

  private String getValue(JSONObject user, String key) {
    String result = null;
    try {
      result = user.get(key).toString();
    } catch (JSONException e) {
      FamLog.exception(e, 201109191150l);
    }
    return result;
  }

  private boolean isValueSet(JSONObject user, String key) {
    boolean result = false;
    try {
      result = user.get(key) != null && !ValueUtil.me().isNullOrWhitspace(user.getString(key));
    } catch (JSONException e) {
      FamLog.info(key + " - " + user.toString(), 201011141207l);
    }
    return result;
  }

  /**
   * return a user object with all values set in json object. if no value is set, let it <code>null</code> in the user object as well. use
   * {@link JSONFactory#getUser(User)} to create a user.
   * 
   * @see JSONFactory#getUser(User)
   * @param user represented a user.
   * @return a user object with all values set in json object.
   */
  public User getUser(JSONObject user) {
    User result = this.blank();
    try {
      if (this.isValueSet(user, "title")) {
        result.setTitle(user.get("title").toString());
      }
      if (this.isValueSet(user, "fname")) {
        result.setFname(user.get("fname").toString());
      }
      if (this.isValueSet(user, "intendedResearch")) {
        result.setIntendedResearch(user.get("intendedResearch").toString());
      }
      if (this.isValueSet(user, "account_expires")) {
        result.setAccountExpires(user.get("account_expires").toString());
      }
      if (this.isValueSet(user, "sname")) {
        result.setSname(user.get("sname").toString());
      }
      if (this.isValueSet(user, "company")) {
        result.setCompany(user.get("company").toString());
      }
      if (this.isValueSet(user, "departmentlabel")) {
        result.setDepartmentLabel(user.get("departmentlabel").toString());
      }
      if (this.isValueSet(user, "departmentkey")) {
        result.setDepartmentKey(user.get("departmentkey").toString());
      }
      if (this.isValueSet(user, "mail")) {
        result.setMail(user.get("mail").toString());
      }
      if (this.isValueSet(user, "phone1")) {
        result.setPhone1(user.get("phone1").toString());
      }
      if (this.isValueSet(user, "phone2")) {
        result.setPhone2(user.get("phone2").toString());
      }
      if (this.isValueSet(user, "pass")) {
        result.setPassword(user.get("pass").toString());
      }
      if (this.isValueSet(user, "male")) {
        result.setMale(user.get("male").toString().equals("1") ? true : false);
      }
      if (this.isValueSet(user, "birthdate")) {
        result.setBirthdate(user.get("birthdate").toString());
      }
      if (this.isValueSet(user, "excluded")) {
        result.setExcluded(user.get("excluded").equals("1") ? true : false);
      }
      if (this.isValueSet(user, "username")) {
        result.setUsername(user.getString("username"));
      }
      if (this.isValueSet(user, "roleid")) {
        result.setRoleId(user.getString("roleid"));
      }
      if (this.isValueSet(user, "id")) {
        try {
          result.setId(Integer.parseInt(user.get("id").toString()));
        } catch (NumberFormatException e) {
          FamLog.exception(user.get("id").toString(), e, 201011141210l);
        }
      }

      Address address = new Address();
      boolean hasAddressDetails = false;
      if (this.isValueSet(user, "city")) {
        address.setCity(user.get("city").toString());
        hasAddressDetails = true;
      }
      if (this.isValueSet(user, "street")) {
        address.setStreet(user.get("street").toString());
        hasAddressDetails = true;
      }
      if (this.isValueSet(user, "streetno")) {
        address.setStreetno(user.get("streetno").toString());
        hasAddressDetails = true;
      }
      if (this.isValueSet(user, "zipcode")) {
        address.setZipcode(user.get("zipcode").toString());
        hasAddressDetails = true;
      }
      if (this.isValueSet(user, "country")) {
        address.setCountry(user.get("country").toString());
        hasAddressDetails = true;
      }
      if (hasAddressDetails) {
        result.setMainAddress(address);
      }
    } catch (JSONException e) {
      FamLog.exception(e, 201011141206l);
      result = null;
    }
    return result;
  }

  public List<ContactDetail> getContactDetails(JSONObject user) {
    List<ContactDetail> result = null;
    if (this.isValueSet(user, "contactDetails")) {
      try {
        JSONArray contactDetails = user.getJSONArray("contactDetails");
        result = new ArrayList<ContactDetail>();
        for (int i = 0; i < contactDetails.length(); i++) {
          JSONObject contactDetail = contactDetails.getJSONObject(i);
          if (this.isValueSet(contactDetail, "title") && this.isValueSet(contactDetail, "detail")) {
            ContactDetail cd = new ContactDetail();
            cd.setTitle(contactDetail.getString("title"));
            cd.setDetail(contactDetail.getString("detail"));
            result.add(cd);
          }
        }
      } catch (JSONException e) {
        FamLog.exception(e, 201011141227l);
        result = null;
      }
    }
    return result;
  }

  /**
   * a blank user set as a standard user.
   * 
   * @see User#setStandardUser()
   * @return a blank user set as a standard user.
   */
  public User getBlankStandardUser() {
    User result = this.blank();
    result.setStandardUser();
    return result;
  }
}

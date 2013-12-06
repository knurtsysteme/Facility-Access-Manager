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
package de.knurt.fam.core.util.mvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.config.Department;
import de.knurt.fam.core.model.persist.Address;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.KnownDepartmentConfigDao;
import de.knurt.fam.core.util.JSONFactory;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.template.util.ContactDetailsRequestHandler;

/**
 * a data holder for registration input.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090308 (03/08/2009)
 */
public class Registration {

  private String showJustDefaultTermsOfUseAgreements;

  /**
   * nothing set here
   */
  public Registration() {
  }

  /**
   * return password input or null, if no password is given or password input of field 1 is not the same password in field 2.
   * 
   * @return password input or null
   */
  public String getPassword() {
    String result = null;
    if (this.getPass1() != null && this.getPass2() != null) {
      if (this.getPass1().equals("") == false && this.getPass2().equals("") == false) {
        if (this.getPass1().equals(this.getPass2())) {
          result = this.getPass1();
        }
      }
    }
    return result;
  }

  public void setShowJustDefaultTermsOfUseAgreements(String showJustDefaultTermsOfUseAgreements) {
    this.showJustDefaultTermsOfUseAgreements = showJustDefaultTermsOfUseAgreements;
  }

  public String getShowJustDefaultTermsOfUseAgreements() {
    return showJustDefaultTermsOfUseAgreements;
  }

  private String intendedResearch, departmentKey, departmentLabel, male, pass1, pass2, birthdate, accountExpires, phone1, phone2, company, fname, title, acceptedStatementOfAgreement, mail, sname, street, streetno, zipcode, city, country, userId;

  public String getDepartmentKey() {
    if (this.departmentLabel == null || this.departmentLabel.trim().isEmpty()) {
      return Department.UNKNOWN_KEY;
    } else {
      return departmentKey;
    }
  }

  public String getIntendedResearch() {
    return intendedResearch;
  }

  public void setIntendedResearch(String intendedResearch) {
    this.intendedResearch = intendedResearch;
  }

  public String getDepartmentLabel() {
    return ContactDetailsRequestHandler.getDepartmentLabel(this.departmentLabel, this.departmentKey);
  }

  public void setDepartmentKey(String departmentKey) {
    this.departmentKey = departmentKey;
  }

  public void setDepartmentLabel(String departmentLabel) {
    this.departmentLabel = departmentLabel;
  }

  /**
   * return the birthdate input as date
   * 
   * @return birthdate input as date
   */
  public Date getBirtdateAsDate() {
    Date result = null;
    if (this.getBirthdate() != null) {
      result = ContactDetailsRequestHandler.correctBirthdate(this.getBirthdate());
    }
    return result;
  }

  /**
   * @return the male
   */
  public String getMale() {
    return male;
  }

  /**
   * return a user from given input.
   * 
   * @see User#setStandardUser()
   * @see User#setUniqueUsernameForInsertion()
   * @return a user from given input
   */
  public User getUser() {
    User result = UserFactory.me().blank();
    result.setBirthdate(this.getBirtdateAsDate());
    result.setAccountExpires(this.getAccountExpiresAsDate());
    result.setMale(this.getMale() != null && this.getMale().equals("1"));
    result.setTitle(this.getTitle());
    result.setCompany(this.getCompany());
    result.setPhone1(this.getPhone1());
    result.setDepartmentKey(this.getDepartmentKey());
    result.setDepartmentLabel(this.getDepartmentLabel());
    result.setRoleId(KnownDepartmentConfigDao.getInstance().getRole(this.getDepartmentKey()).getKey());
    result.setPhone2(this.getPhone2());
    result.setIntendedResearch(this.getIntendedResearch());
    result.setMail(this.getMail());
    result.setFname(this.getFname());
    result.setSname(this.getSname());
    result.setCustomFields(this.customFields);
    result.setAcceptedStatementOfAgreement(this.getAcceptedStatementOfAgreement() != null && !this.getAcceptedStatementOfAgreement().equals(""));
    if (this.mainAddressIsPutIn()) {
      Address mainAddress = new Address();
      mainAddress.setStreet(this.getStreet());
      mainAddress.setStreetno(this.getStreetno());
      mainAddress.setZipcode(this.getZipcode());
      mainAddress.setCity(this.getCity());
      mainAddress.setCountry(this.getCountry());
      result.setMainAddress(mainAddress);
    }
    try {
      result.setUniqueUsernameForInsertion();
    } catch (IllegalArgumentException e) {
      // No SqlMapClient specified here
      // set username explicitly null
      result.setUsername(null);
    } catch (CannotGetJdbcConnectionException e) {
      result.setUsername(null);
    } catch (Exception e) {
      result.setUsername(null);
    }
    result.setPassword(this.getPassword());
    return result;
  }

  private Date getAccountExpiresAsDate() {
    Date result = null;
    if (this.getAccountExpires() != null) {
      result = ContactDetailsRequestHandler.getDate(this.getAccountExpires());
    }
    return result;
  }

  /**
   * return true, if a user with same mail address already exists in db. return false if the user in the database is the user logged in or no user
   * with this mail exist.
   * 
   * @return true, if a user with same mail address already exists in db.
   */
  public Boolean mailExists() {
    // XXX use UserFactory here
    User testuser = UserFactory.me().blank();
    testuser.setMail(this.getMail());
    Boolean result = false;
    if (FamDaoProxy.getInstance().getUserDao().userLikeExists(testuser)) {
      if ((FamDaoProxy.userDao().getOneLike(testuser).getUserId() + "").equals(this.getUserId()) == false) {
        result = true;
      }
    }
    return result;
  }

  /**
   * return true, if user forgot to check the Terms of Use Agreement
   * 
   * @return true, if user forgot to check the Terms of Use Agreement
   */
  public Boolean missedCheckedSoABox() {
    if (this.getShowJustDefaultTermsOfUseAgreements() == null) {
      System.err.println("missed input 201008201412");
      return true;
    } else if (this.getShowJustDefaultTermsOfUseAgreements().equals("f")) {
      String soboxval = this.getAcceptedStatementOfAgreement();
      return soboxval == null;
    } else {
      return false;
    }
  }

  /**
   * @param male the male to set
   */
  public void setMale(String male) {
    this.male = male;
  }

  /**
   * @return the pass1
   */
  public String getPass1() {
    return pass1;
  }

  /**
   * @param pass1 the pass1 to set
   */
  public void setPass1(String pass1) {
    this.pass1 = pass1;
  }

  /**
   * @return the phone1
   */
  public String getPhone1() {
    return phone1;
  }

  /**
   * @param phone1 the phone1 to set
   */
  public void setPhone1(String phone1) {
    this.phone1 = phone1;
  }

  /**
   * @return the phone2
   */
  public String getPhone2() {
    return phone2;
  }

  /**
   * @param phone2 the phone2 to set
   */
  public void setPhone2(String phone2) {
    this.phone2 = phone2;
  }

  /**
   * @return the company
   */
  public String getCompany() {
    return company;
  }

  /**
   * @param company the company to set
   */
  public void setCompany(String company) {
    this.company = company;
  }

  /**
   * @return the fname
   */
  public String getFname() {
    return fname;
  }

  /**
   * @param fname the fname to set
   */
  public void setFname(String fname) {
    this.fname = fname;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the acceptedStatementOfAgreement
   */
  public String getAcceptedStatementOfAgreement() {
    return acceptedStatementOfAgreement;
  }

  /**
   * @param acceptedStatementOfAgreement the acceptedStatementOfAgreement to set
   */
  public void setAcceptedStatementOfAgreement(String acceptedStatementOfAgreement) {
    this.acceptedStatementOfAgreement = acceptedStatementOfAgreement;
  }

  /**
   * @return the mail
   */
  public String getMail() {
    return mail;
  }

  /**
   * @param mail the mail to set
   */
  public void setMail(String mail) {
    this.mail = mail;
  }

  /**
   * @return the sname
   */
  public String getSname() {
    return sname;
  }

  /**
   * @param sname the sname to set
   */
  public void setSname(String sname) {
    this.sname = sname;
  }

  /**
   * @return the birthdate
   */
  public String getBirthdate() {
    return birthdate;
  }

  /**
   * @param birthdate the birthdate to set
   */
  public void setBirthdate(String birthdate) {
    this.birthdate = birthdate;
  }

  /**
   * @return the street
   */
  public String getStreet() {
    return street;
  }

  /**
   * @param street the street to set
   */
  public void setStreet(String street) {
    this.street = street;
  }

  /**
   * @return the streetno
   */
  public String getStreetno() {
    return streetno;
  }

  /**
   * @param streetno the streetno to set
   */
  public void setStreetno(String streetno) {
    this.streetno = streetno;
  }

  /**
   * @return the zipcode
   */
  public String getZipcode() {
    return zipcode;
  }

  /**
   * @param zipcode the zipcode to set
   */
  public void setZipcode(String zipcode) {
    this.zipcode = zipcode;
  }

  /**
   * @return the city
   */
  public String getCity() {
    return city;
  }

  /**
   * @param city the city to set
   */
  public void setCity(String city) {
    this.city = city;
  }

  /**
   * @return the country
   */
  public String getCountry() {
    return country;
  }

  /**
   * @param country the country to set
   */
  public void setCountry(String country) {
    this.country = country;
  }

  /**
   * @return the pass2
   */
  public String getPass2() {
    return pass2;
  }

  /**
   * @param pass2 the pass2 to set
   */
  public void setPass2(String pass2) {
    this.pass2 = pass2;
  }

  private boolean mainAddressIsPutIn() {
    return this.getCity() != null || this.getCountry() != null || this.getStreet() != null || this.getStreetno() != null || this.getZipcode() != null;
  }

  /**
   * @return the userId
   */
  public String getUserId() {
    return userId;
  }

  /**
   * @param userId the userId to set
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setAccountExpires(String accountExpires) {
    this.accountExpires = accountExpires;
  }

  public void setAccount_expires(String accountExpires) {
    this.accountExpires = accountExpires;
  }

  public String getAccountExpires() {
    return accountExpires;
  }

  private JSONObject customFields = new JSONObject();

  /**
   * put all parameters sent but not part of a user into the custom field attribute
   * 
   * @see User#getCustomFields()
   * @since 20.11.2013
   * @param request
   */
  public void setCustomFields(HttpServletRequest request) {
    Enumeration<?> params = request.getParameterNames();
    try {
      List<String> knownParams = this.getKnownUserParams();
      while (params.hasMoreElements()) {
        String param = params.nextElement().toString();
        String value = request.getParameter(param);
        if (!knownParams.contains(param)) {
          this.customFields.put(param, value);
        }
      }
    } catch (JSONException e) {
      FamLog.exception(e, 201311291441l);
    }
  }

  private List<String> getKnownUserParams() throws JSONException {
    JSONObject jsontmp = JSONFactory.me().getUser(UserFactory.me().getBlankStandardUser());
    @SuppressWarnings({ "unchecked", "rawtypes" })
    List<String> result = new ArrayList(Arrays.asList(JSONObject.getNames(jsontmp)));
    result.add("password");
    result.add("responsibilities");
    return result;
  }

  public void setCustomFields(JSONObject customFields) {
    this.customFields = new JSONObject();
    try {
      List<String> knownParams = this.getKnownUserParams();
      for (String param : JSONObject.getNames(customFields)) {
        if (!knownParams.contains(param)) {
          this.customFields.put(param, customFields.get(param));
        }
      }
    } catch (JSONException e) {
      FamLog.exception(e, 201311291440l);
    }
  }

}

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
package de.knurt.fam.core.persistence.dao.ibatis;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.validator.GenericValidator;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.Role;
import de.knurt.fam.core.model.persist.Address;
import de.knurt.fam.core.model.persist.ContactDetail;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.UserMail;
import de.knurt.fam.core.model.persist.document.Job;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.UserDao;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * dao for users stored in sql
 * 
 * @author Daniel Oltmanns
 * @since 0.20090828
 */
public class UserDao4ibatis extends UserDao {

  /**
   * construct me and set the object container
   * 
   * @see Db4oServletContextListener#getObjectContainer4users()
   */
  public UserDao4ibatis() {
  }

  /**
   * delete the given user.
   * 
   * @param user to delete
   * @see User#excluded
   * @throws org.springframework.dao.DataIntegrityViolationException if it is not possible to delete this user
   */
  @Override
  public synchronized boolean delete(User user) {
    boolean result = false;
    try {
      this.sqlMap().delete("User.delete.usermail", user);
      this.sqlMap().delete("User.delete.logbookentry", user);
      this.sqlMap().delete("User.delete.contactdetail", user);
      this.sqlMap().delete("User.delete.booking", user);
      this.sqlMap().delete("User.delete.address", user);
      this.sqlMap().delete("User.delete.user", user);
      result = FamDaoProxy.facilityDao().updateResponsibility(user, new ArrayList<Facility>());
      if (result) {
        user.setJustBeenDeleted();
        setChanged();
        notifyObservers(user);
      }
    } catch (Exception e) {
      FamLog.exception(e, 201204231012l);
    }
    return result;
  }

  /**
   * return all users stored
   * 
   * @return all users stored
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<User> getAll() {
    return this.sqlMap().queryForList("User.select.all");
  }

  /**
   * return all users that equals the example.
   * 
   * @param example user
   * @return all users that equals the example
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<User> getObjectsLike(User example) {
    return this.sqlMap().queryForList("User.select.select_like", example);
  }

  /** {@inheritDoc} */
  @Override
  protected synchronized boolean internInsert(User entry) {
    boolean result = false;
    try {
      assert entry.isPasswordEncoded() == true; // !!!!!
      assert entry.getId() != null;
      this.insertOrUpdateAddresses(entry);
      this.sqlMap().insert("User.insert", entry);
      result = true;
    } catch (Exception e) {
      FamLog.exception(e, 201205071120l);
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  protected synchronized boolean internUpdate(User entry) {
    boolean result = false;
    try {
      this.insertOrUpdateAddresses(entry);
      this.sqlMap().update("User.update", entry);
      result = true;
    } catch (Exception e) {
      FamLog.exception(e, 201205071121l);
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public synchronized boolean insert(UserMail mail) {
    boolean result = false;
    try {
      FamSqlMapClientDaoSupport.sqlMap().insert("UserMail.insert", mail);
    } catch (Exception e) {
      FamLog.exception(e, 201205071203l);
    }
    return result;

  }

  /** {@inheritDoc} */
  @Override
  public synchronized boolean update(UserMail mail) {
    boolean result = false;
    try {
      FamSqlMapClientDaoSupport.sqlMap().update("UserMail.update", mail);
    } catch (Exception e) {
      FamLog.exception(e, 201205071204l);
    }
    return result;

  }

  /** {@inheritDoc} */
  @Override
  protected void setIdToNextId(User user) {
    User userWithId = (User) FamSqlMapClientDaoSupport.sqlMap().queryForObject("User.select.max_id");
    if (userWithId != null && userWithId.getId() != null) {
      user.setId(userWithId.getId() + 1);
    } else {
      if (this.getAll().size() == 0) {
        // hello first user!
        user.setId(1);
      } else {
        FamLog.error("sql mapping did not work", 201011151242l);
      }
    }
  }

  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  @Override
  public List<UserMail> getUserMailsThatMustBeSendNow() {
    return FamSqlMapClientDaoSupport.sqlMap().queryForList("UserMail.select.mustBeSendNow", new Date());
  }

  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  @Override
  public List<UserMail> getAllUserMails() {
    return FamSqlMapClientDaoSupport.sqlMap().queryForList("UserMail.select.all");
  }

  private SqlMapClientTemplate sqlMap() {
    return FamSqlMapClientDaoSupport.sqlMap();
  }

  /** {@inheritDoc} */
  @Override
  public Address getAddress(Integer id) {
    return (Address) this.sqlMap().queryForObject("Address.select.id", id);
  }

  protected synchronized void insertOrUpdateAddresses(User entry) {
    if (entry == null) {
      FamLog.info("try to insert or update a address of a none user", 201011151123l);
    } else if (entry.getUserId() == null) {
      FamLog.info("try to insert or update a address without user id", 201011151124l);
    } else if (entry.getMainAddress() == null) {
      FamLog.info("try to insert or update a user without an address", 201011151122l);
      entry.setMainAddress(new Address());
      FamLog.info("set a new empty address on: " + entry, 201011211055l);
      this.insertOrUpdateAddresses(entry);
    } else {
      Map<String, Object> hashMap = new HashMap<String, Object>();
      boolean insert = true; // assert insert;
      if (entry.getMainAddress().getId() != null) {
        insert = false; // update
        hashMap.put("id", entry.getMainAddress().getId());
      }
      hashMap.put("userId", entry.getId());
      hashMap.put("zipcode", entry.getMainAddress().getZipcode());
      hashMap.put("street", entry.getMainAddress().getStreet());
      hashMap.put("streetno", entry.getMainAddress().getStreetno());
      hashMap.put("city", entry.getMainAddress().getCity());
      hashMap.put("country", entry.getMainAddress().getCountry());
      if (insert) {
        this.sqlMap().insert("Address.insert", hashMap);
      } else { // update
        this.sqlMap().update("Address.update", hashMap);
      }
      entry.setMainAddressWithId(Integer.parseInt(hashMap.get("id") + ""));
    }

  }

  /** {@inheritDoc} */
  @Override
  public synchronized boolean insert(ContactDetail contactDetail) {
    boolean result = false;
    setChanged();
    notifyObservers(contactDetail);
    try {

      FamSqlMapClientDaoSupport.sqlMap().insert("ContactDetail.insert", contactDetail);
    } catch (Exception e) {
      FamLog.exception(e, 201205071200l);
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public synchronized boolean update(ContactDetail contactDetail) {
    boolean result = false;
    setChanged();
    notifyObservers(contactDetail);
    try {
      FamSqlMapClientDaoSupport.sqlMap().update("ContactDetail.update", contactDetail);
    } catch (Exception e) {
      FamLog.exception(e, 201205071201l);
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public synchronized boolean delete(ContactDetail contactDetail) {
    boolean result = false;
    setChanged();
    notifyObservers(contactDetail);
    try {
      FamSqlMapClientDaoSupport.sqlMap().delete("ContactDetail.delete", contactDetail);
      result = true;
    } catch (Exception e) {
      FamLog.exception(e, 201205071202l);
    }
    return result;
  }

  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  @Override
  public List<ContactDetail> getAllLike(ContactDetail example) {
    return FamSqlMapClientDaoSupport.sqlMap().queryForList("ContactDetail.select_like", example);
  }

  @SuppressWarnings("unchecked")
  private List<User> getWhere(String where) {
    return this.sqlMap().queryForList("User.select.where", where);
  }

  /** {@inheritDoc} */
  @Override
  public List<User> getUsersAccountExpired(Date day) {
    String where = String.format("account_expires = '%s'", Util4Daos4ibatis.SDF_4_DATE.format(day));
    return this.getWhere(where);
  }

  /** {@inheritDoc} */
  @Override
  public List<User> getUserAccountExpiresIn(TimeFrame timeframe) {
    String where = String.format("account_expires >= '%s' AND account_expires <= '%s'", Util4Daos4ibatis.SDF_4_DATE.format(timeframe.getDateStart()), Util4Daos4ibatis.SDF_4_DATE.format(timeframe.getDateEnd()));
    return this.getWhere(where);
  }

  /** {@inheritDoc} */
  @Override
  public List<User> getUsersRegistrationIsIn(TimeFrame timeframe) {
    String biggerEqual = Util4Daos4ibatis.SDF_4_TIMESTAMP.format(timeframe.getDateStart());
    String smallerEqual = Util4Daos4ibatis.SDF_4_TIMESTAMP.format(timeframe.getDateEnd());
    String where = String.format("registration >= '%s' AND registration <= '%s'", biggerEqual, smallerEqual);
    return this.getWhere(where);
  }

  /** {@inheritDoc} */
  @Override
  public List<User> getUsersWithEMail(String email) throws InvalidParameterException {
    if (GenericValidator.isEmail(email)) {
      String where = String.format("mail = \"%s\"", email);
      return this.getWhere(where);
    } else {
      throw new InvalidParameterException(email + " is not an email");
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<User> getUsersWithRealName(String firstname, String sirname) throws InvalidParameterException {
    String regexp = "^[^=><'\"]+$";
    if (GenericValidator.matchRegexp(firstname, regexp) && GenericValidator.matchRegexp(sirname, regexp)) {
      String where = String.format("fname = \"%s\" AND sname = \"%s\"", firstname, sirname);
      return this.getWhere(where);
    } else {
      throw new InvalidParameterException(firstname + "; " + sirname + " is not a real name");
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean anonymize(User user, User auth) {
    boolean result = false;
    if (FamAuth.hasRight(auth, FamAuth.ANONYMIZE_USER, null)) {
      try {
        List<Job> jobs = FamDaoProxy.jobsDao().getJobs(user, false);

        this.sqlMap().delete("User.delete.usermail", user);
        this.sqlMap().delete("User.delete.contactdetail", user);

        String oldUsername = user.getUsername();
        user.setUsername("anonym");
        user.setUniqueUsernameForInsertion();
        user.setMail(String.format("%s@anonym.de", user.getUsername()));

        boolean tmp = false;
        result = true;
        for (Job job : jobs) {
          job.setUsername(user.getUsername());
          tmp = job.insertOrUpdate();
          if (!tmp) result = false;
        }
        if (result) {
          result = false;

          Map<String, String> adapterMap = new HashMap<String, String>();
          adapterMap.put("old_username", oldUsername);
          adapterMap.put("new_username", user.getUsername());

          this.sqlMap().delete("User.anonymize.logbookentry", adapterMap);
          this.sqlMap().delete("User.anonymize.booking", adapterMap);
          this.sqlMap().delete("User.anonymize.address", user);
          this.sqlMap().delete("User.anonymize.user", user);
          result = true;
          user.setAnonymizedName(oldUsername);
          setChanged();
          notifyObservers(user);
        }
      } catch (Exception e) {
        FamLog.exception(e, 201205071050l);
      }
    }
    return result;
  }

  /**
   * {@inheritDoc} username must be only lower case with numbers.
   */
  @Override
  public User getUserFromUsername(String username) {
    User result = null;
    if (!GenericValidator.isBlankOrNull(username)) {
      username = username.trim();
      String regexp = "^[a-z0-9]+$";
      if (GenericValidator.matchRegexp(username, regexp)) {
        List<User> users = this.getWhere(String.format("username = '%s'", username));
        if (users != null && users.size() == 1) {
          result = users.get(0);
        } else if (users.size() > 1) {
          FamLog.error(String.format("found %s users with username %s", users.size(), username), 201205091021l);
        } else {
          FamLog.info("no user found with username " + username, 201205091338l);
        }
      }
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public List<User> getResponsibleUsers(Facility facility) {
    String where = String.format("username IN (SELECT username FROM facility_responsibility WHERE facility_key = \"%s\")", facility.getKey());
    return this.getWhere(where);
  }

  /** {@inheritDoc} */
  @Override
  public List<User> getUserWithRole(Role role) {
    String where = String.format("roleid = \"%s\"", role.getKey());
    return this.getWhere(where);
  }

}

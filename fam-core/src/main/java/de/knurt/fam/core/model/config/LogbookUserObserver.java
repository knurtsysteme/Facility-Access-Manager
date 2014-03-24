// © 2014 by IT-Power GmbH (http://www.it-power.org)
package de.knurt.fam.core.model.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.ContactDetail;
import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.RoleConfigDao;
import de.knurt.fam.core.util.JSONFactory;

/**
 * COMMENTME
 * 
 * @author itp1dol
 * @since 06.03.2014
 */
public class LogbookUserObserver extends Logbook implements Observer {

  public void setObservable(Observable observable) {
    observable.addObserver(this);
  }

  private boolean is(Object obj, Class<?> clazz) {
    return obj.getClass().equals(clazz);
  }

  /**
   * return an {@link LogbookEntry} with an admin as author, current date, english as language.
   * 
   * @since 06.03.2014
   * @return base {@link LogbookEntry} for all entries made here
   */
  private LogbookEntry getNewBaseEntry() {
    LogbookEntry result = new LogbookEntry();
    result.setOfUserName(RoleConfigDao.getInstance().getUsernamesOfAdmins()[0]);
    result.setLogbookId(this.getKey());
    result.setTagsFromCsv("system");
    result.setDate();
    result.setLanguage(Locale.ENGLISH);
    return result;
  }

  private LogbookEntry getEntryForUserNew(User newUser) {
    LogbookEntry result = this.getNewBaseEntry();
    result.setHeadline("New user: " + newUser.getUsername());
    result.setContent(String.format("%s was inserted with the role %s", newUser.getFullName(), newUser.getRoleLabel()));
    return result;
  }

  private LogbookEntry getEntryForUserAnonymized(String username) {
    LogbookEntry result = this.getNewBaseEntry();
    result.setHeadline("Anonymized user" + username);
    result.setContent(String.format("%s is anonym now", username));
    return result;
  }

  private LogbookEntry getEntryForUserDeleted(User user) {
    LogbookEntry result = this.getNewBaseEntry();
    result.setHeadline("Deleted user: " + user.getUsername());
    result.setContent(String.format("%s was deleted", user.getFullName()));
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public void update(Observable userDao, Object object) {
    LogbookEntry newEntry = null;
    if (this.is(object, User.class)) {
      User user = (User) object;
      if (user.hasBeenAnonymized()) {
        newEntry = this.getEntryForUserAnonymized(user.getUsernameBeforeAnonym());
      } else if (user.hasJustBeenInserted()) {
        // a new user was inserted
        newEntry = this.getEntryForUserNew(user);
      } else if (user.hasJustBeenDeleted()) {
        newEntry = this.getEntryForUserDeleted(user);
      } else {
        User old = FamDaoProxy.userDao().getUserFromUsername(user.getUsername());
        newEntry = this.getEntryForUserUpdate(old, user);
      }
    } else if (this.is(object, ContactDetail.class)) {
      ContactDetail cd = (ContactDetail) object;
      newEntry = this.getEntryForContactDetails(cd);
    }
    if (newEntry != null) {
      newEntry.insert();
    } else {
      FamLog.error("observed an unknown event: " + object.getClass(), 201403061006l);
    }
  }

  private LogbookEntry getEntryForContactDetails(ContactDetail cd) {
    LogbookEntry result = this.getNewBaseEntry();
    result.setHeadline("contact details changed of user " + cd.getUsername());
    result.setContent(String.format("%s: %s", cd.getTitle(), cd.getDetail()));
    return result;
  }

  private boolean isJSONObject(JSONObject subject, String key) {
    boolean result = true;
    try {
      subject.getJSONObject(key);
    } catch (JSONException e) {
      result = false;
    }
    return result;
  }

  private boolean isJSONArray(JSONObject json, String key) {
    boolean result = true;
    try {
      json.getJSONArray(key);
    } catch (JSONException e) {
      result = false;
    }
    return result;
  }

  private List<String> getBeforeAfterMessages(JSONArray newJson, JSONArray oldJson) throws JSONException {
    List<String> result = new ArrayList<String>();
    int i = 0;
    while (i < newJson.length()) {
      if (oldJson.length() < i) {
        result.add("a new value: " + newJson.get(i));
      } else if (this.isJSONObject(newJson, i)) {
        result.addAll(this.getBeforeAfterMessages(newJson.getJSONObject(i), oldJson.getJSONObject(i)));
      } else if (this.isJSONArray(newJson, i)) {
        result.addAll(this.getBeforeAfterMessages(newJson.getJSONArray(i), oldJson.getJSONArray(i)));
      } else if (!newJson.equals(oldJson)) {
        result.add(this.getMessage(newJson, oldJson, i));
      }
      i++;
    }
    return result;
  }

  private boolean isJSONObject(JSONArray json, int i) {
    boolean result = true;
    try {
      json.getJSONObject(i);
    } catch (JSONException e) {
      result = false;
    }
    return result;
  }

  private boolean isJSONArray(JSONArray json, int i) {
    boolean result = true;
    try {
      json.getJSONArray(i);
    } catch (JSONException e) {
      result = false;
    }
    return result;
  }

  private String getMessage(String oldValue, String newValue, String key) {
    String result = null;
    if (oldValue.isEmpty()) {
      result = String.format("added value for %s: %s", key, newValue);
    } else {
      result = String.format("changed value of %s from %s to %s", key, oldValue, newValue);
    }
    return result;
  }

  private String getMessage(JSONObject jsonNew, JSONObject jsonOld, String key) throws JSONException {
    return this.getMessage(jsonOld.getString(key), jsonNew.getString(key), key);
  }

  private String getMessage(JSONArray jsonNew, JSONArray jsonOld, int key) throws JSONException {
    return this.getMessage(jsonOld.getString(key), jsonNew.getString(key), key + "");
  }

  /**
   * CODESMELL this has nothing to do with this logbook (but it is used)
   * 
   * @since 06.03.2014
   * @param newJson
   * @param oldJson
   * @return
   * @throws JSONException
   */
  private List<String> getBeforeAfterMessages(JSONObject newJson, JSONObject oldJson) throws JSONException {
    List<String> result = new ArrayList<String>();
    Iterator<?> keyIterator = newJson.keys();
    while (keyIterator.hasNext()) {
      String jsonKey = (String) keyIterator.next();
      if (oldJson.has(jsonKey) == false) {
        result.add("a new field " + jsonKey + " = " + newJson.get(jsonKey).toString());
      } else if (this.isJSONObject(newJson, jsonKey)) {
        result.addAll(this.getBeforeAfterMessages(newJson.getJSONObject(jsonKey), oldJson.getJSONObject(jsonKey)));
      } else if (this.isJSONArray(newJson, jsonKey)) {
        result.addAll(this.getBeforeAfterMessages(newJson.getJSONArray(jsonKey), oldJson.getJSONArray(jsonKey)));
      } else if (!newJson.getString(jsonKey).equals(oldJson.getString(jsonKey))) {
        result.add(this.getMessage(newJson, oldJson, jsonKey));
      }
    }
    return result;
  }

  private LogbookEntry getEntryForUserUpdate(User oldUser, User newUser) {
    LogbookEntry result = this.getNewBaseEntry();
    result.setHeadline("Update user: " + newUser.getUsername());
    List<String> messages = new ArrayList<String>();
    try {
      JSONObject newUserJson = JSONFactory.me().getUser(newUser);
      JSONObject oldUserJson = JSONFactory.me().getUser(oldUser);
      messages.addAll(this.getBeforeAfterMessages(newUserJson, oldUserJson));
    } catch (JSONException e) {
      FamLog.exception(e, 201402061143l);
      messages.add("please report error 201402061143");
    }
    result.setContent(StringUtils.join(messages.toArray(), "\r\n")); // FIXME check delimiter
    return result;
  }

  public List<LogbookEntry> getAllEntries() {
    LogbookEntry le = new LogbookEntry();
    le.setLogbookId(this.getKey());
    return FamDaoProxy.logbookEntryDao().getObjectsLike(le);
  }
}
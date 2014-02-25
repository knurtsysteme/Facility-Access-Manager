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
package de.knurt.fam.core.model.config;

import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.LogbookConfigDao;
import de.knurt.fam.core.view.text.FamText;

/**
 * a logbook. please make sure, this does not contain any language specific strings being injected, because this is not possible.
 * 
 * use the access class {@link LogbookConfigDao} for this mission.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090410
 */
public class Logbook {

  private String visibility = null;

  /**
   * a logbook. must always be injected!
   */
  private Logbook() {
  }

  /**
   * return the count of all logbook entries
   * 
   * @return the count of all logbook entries
   */
  public int getEntryCount() {
    return FamDaoProxy.logbookEntryDao().getEntryCount(this);
  }

  /**
   * return the newest entry made in this logbook.
   * 
   * @return the newest entry made in this logbook.
   */
  public LogbookEntry getNewestEntry() {
    return FamDaoProxy.logbookEntryDao().getNewestEntry(this);
  }

  /**
   * return the key, representing this logbook
   * 
   * @return key, representing this logbook
   */
  public String getKey() {
    return LogbookConfigDao.getInstance().getKey(this);
  }

  /**
   * return the label of the logbook (defined in config/lang.properties)
   * 
   * @return the label of the logbook
   */
  public String getLabel() {
    return FamText.message(this.getKey() + ".label");
  }

  /**
   * return the description of the logbook (defined in config/lang.properties)
   * 
   * @return the description of the logbook
   */
  public String getDescription() {
    return FamText.message(this.getKey() + ".description");
  }

  /**
   * return tags of the given logbook
   * 
   * @param key representing the logbook
   * @return tags of the given logbook
   */
  public String[] getTags() {
    return FamText.message(this.getKey() + ".tags").split(",");
  }

  /**
   * return true, if not visibility for this logbook is set or the visibility for this logbook equals the role label of the given user.- otherwise
   * return false.
   * 
   * @param user
   * @return true, if user is allowed to view this logbook
   */
  public boolean isVisibleFor(User user) {
    return this.visibility == null || this.visibility.equals(user.getRoleId());
  }

  /**
   * set the visibility for the logbook
   * 
   * @param visibility a string representation of the label {@link Role#getLabel()} of the role
   * @see Role#getLabel()
   */
  public void setVisibility(String visibility) {
    this.visibility = visibility;
  }

}
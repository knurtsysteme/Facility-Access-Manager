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
package de.knurt.fam.core.persistence.dao.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.model.config.Logbook;
import de.knurt.fam.core.model.persist.User;

/**
 * a data holder for all defined roles and specific roles
 * 
 * for adding new Logbooks, please add text for every get-method into {@link "de.knurt.fam.core.view.text.logbooks.properties"} in all languages. The
 * messageBase, used for all these messages is the key used for this Logbook in {@link LogbookConfigDao}.
 * 
 * the roles all have to be injected
 * 
 * @author Daniel Oltmanns
 * @since 0.20090412 (04/12/2009)
 */
public class LogbookConfigDao extends AbstractConfigDao<Logbook> {

  private volatile static LogbookConfigDao me;
  private Map<String, Logbook> configuredInstances;

  /** {@inheritDoc} */
  @Override
  protected Map<String, Logbook> getConfiguredInstances() {
    return this.configuredInstances;
  }

  private LogbookConfigDao() {
  }

  /**
   * return the one and only instance of RoleConfigDao
   * 
   * @return the one and only instance of RoleConfigDao
   */
  public static LogbookConfigDao getInstance() {
    if (me == null) { // no instance so far
      synchronized (LogbookConfigDao.class) {
        if (me == null) { // still no instance so far
          me = new LogbookConfigDao(); // the one and only
        }
      }
    }
    return me;
  }

  /**
   * @param configuredInstances to set
   */
  @Required
  @Override
  public void setConfiguredInstances(Map<String, Logbook> configuredInstances) {
    this.configuredInstances = configuredInstances;
  }
  
  /**
   * alias for {@link #getConfiguredInstance(String)}
   */
  public Logbook get(String key) {
    return this.getConfiguredInstance(key);
  }

  public List<Logbook> getAllVisibleFor(User user) {
    List<Logbook> logbooks = getAll();
    List<Logbook> result = new ArrayList<Logbook>();
    for (Logbook logbook : logbooks) {
      if (logbook.isVisibleFor(user)) {
        result.add(logbook);
      }
    }
    return result;
  }

  public boolean isVisibleFor(String logbookKey, User user) {
    return this.getConfiguredInstance(logbookKey).isVisibleFor(user);
  }
}
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
package de.knurt.fam.core.model.persist;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * object, that can be inserted and updated. <br />
 * these are all data holders for user inputs. <br />
 * all storable classes have to implement {@link Storeable}. other classes in this package must be part of at least one storable class, but are not
 * storable itself. <br />
 * e.g. a {@link User} has a main {@link Address}. {@link User} is {@link Storeable} but {@link Address} is not, because it is not possible to store a
 * single {@link Address} without a {@link User}. <br />
 * ALL CLASSES IN THIS PACKAGE MUST NOT BE CONTAIN A CONFIGURATION OBJECT (a class in the package {@link "de.knurt.fam.core.model.config"}. <br />
 * thus, the data holder objects must work together with the access objects for the configuration, and this is why they have a key then. <br />
 * e.g. a {@link User} has a Role. Role is only accessable by {@link RoleConfigDao}, because it must be not stored with a user and must be injected.
 * If the rights of a Role change, the User get this new rights over the id of the role as well, (because the old rights are not stored with the user
 * itself).
 * 
 * @see <a href="./doc-files/classes_persist.png">class diagram</a>
 * @author Daniel Oltmanns
 * @since 0.20090325
 */
public interface Storeable {
  /**
   * insert it into somewhere
   * 
   * @throws DataIntegrityViolationException if the state is not storable
   */
  public boolean insert() throws DataIntegrityViolationException;

  /**
   * return true, if this booking has just been inserted by a dao. use this method in classes observing the booking dao.
   * 
   * @return true, if this booking has just been inserted by a dao.
   */
  public boolean hasJustBeenInserted();

  /**
   * set justBeenInserted
   * 
   * @see #hasJustBeenInserted()
   */
  public void setJustBeenInserted();

  /**
   * update it on somewhere
   * 
   * @throws DataIntegrityViolationException if the state is not storable
   */
  public boolean update() throws DataIntegrityViolationException;
}

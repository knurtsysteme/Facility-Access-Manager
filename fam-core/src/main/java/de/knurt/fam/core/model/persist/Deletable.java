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
 * object, that can be deleted.
 * @author Daniel Oltmanns
 * @since 0.20090424
 */
interface Deletable {
    /**
     * delete it from somewhere
     */
    public boolean delete() throws DataIntegrityViolationException;
    /**
     * return true, if this instance has just been deleted by a dao. 
     * 
     * @return true, if this instance has just been deleted by a dao. 
     */
    public boolean hasJustBeenDeleted();

    /**
     * set hasJustBeenDeleted
     * 
     * @see #hasJustBeenDeleted()
     */
    public void setJustBeenDeleted();
}

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
package de.knurt.fam.core.control.persistence.dao;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.core.model.persist.Storeable;

/**
 * An interface for all data access objects resolving interactions of {@link Storeable} objects.
 * @param <K> A storable bean being resolved
 * @author Daniel Oltmanns
 * @since 0.20090323
 */
public interface FamDao<K extends Storeable> {

    /**
     * update data of an object.
     * @param dataholder that is updated
     * @throws org.springframework.dao.DataIntegrityViolationException
     *	the given dataholder violates integrity of the database
     */
    public boolean update(K dataholder) throws DataIntegrityViolationException;

    /**
     * insert the given dataholder or throw {@link DataIntegrityViolationException}
     * if insertion would break the rules.
     * @param dataholder
     * @throws org.springframework.dao.DataIntegrityViolationException
     */
    public boolean insert(K dataholder) throws DataIntegrityViolationException;

    /**
     * delete all objects that are like the given object
     * @param like this object all objects will delete in the database
     * @throws org.springframework.dao.DataIntegrityViolationException
     *	deletion violates integrity of the database
     */
    public boolean delete(K like);

    /**
     * return all objects from the database that are like the example object
     * @param example object for comparsion with objects in the database
     * @return list of objects, that are equal to the example
     */
    public List<K> getObjectsLike(K example);

    /**
     * return the first found object from the database that are like the example object
     * @param example object for comparsion with objects in the database
     * @return first found object, that are equal to the example
     */
    public K getOneLike(K example);

    /**
     * return all objects managed by the dao.
     * @return all objects managed by the dao.
     */
    public List<K> getAll();
}

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
package de.knurt.fam.core.persistence.dao;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.core.model.persist.Storeable;

/**
 * an abstract dao as base for all daos. provice the basic insert, update
 * methods, but not a delete (which may not be wanted).
 * 
 * @param <K>
 *            the main storable object the dao is resolving. there may exist
 *            other storables resolved by the dao. but every dao has a main type
 *            of {@link Storeable} working with.<br />
 *            all other objects being resolved with this dao must depend from
 *            this main storable. e.g. the user dao also resolves mails stored
 *            for the user.
 * @author Daniel Oltmanns
 * @since 0.20090412
 */
public abstract class AbstractFamDao<K extends Storeable> implements FamDao<K> {

	/**
	 * return true, if the state of dataholder violates the data integrity.
	 * 
	 * @param dataholder
	 *            that shall be inserted or updated.
	 * @param onInsert
	 *            true, if dataholder shall be inserted. false, if it shall be
	 *            updated.
	 * @return true, if the state of dataholder violates the data integrity.
	 */
	protected abstract boolean isDataIntegrityViolation(K dataholder, boolean onInsert);

	/**
	 * log an excpetion and throw it.
	 * 
	 * @param dataholder
	 *            the exception is thrown for
	 * @throws DataIntegrityViolationException
	 *             thrown because of given dataholder.
	 */
	protected abstract void logAndThrowDataIntegrityViolationException(K dataholder) throws DataIntegrityViolationException;

	/**
	 * log that the given dataholder has been inserted into the database.
	 * 
	 * @param dataholder
	 *            the has been inserted.
	 */
	protected abstract void logInsert(K dataholder);

	/**
	 * log that the given dataholder has been updated in the database.
	 * 
	 * @param dataholder
	 *            the has been updated.
	 */
	protected abstract void logUpdate(K dataholder);

	/**
	 * set the id for the given dataholder. this assumes that every dataholder
	 * needs an id before it is written into the database. this is a method to
	 * do that. note: there may exist no need for that if it is done by the
	 * database rule (as it is on autoincrement values on relational databases).
	 * 
	 * @param dataholder
	 *            the id is set to
	 */
	protected abstract void setIdToNextId(K dataholder);

	/** {@inheritDoc} */
	@Override
	public synchronized boolean insert(K dataholder) throws DataIntegrityViolationException {
		boolean result = false;
		this.setIdToNextId(dataholder);
		if (this.isDataIntegrityViolation(dataholder, true)) {
			this.logAndThrowDataIntegrityViolationException(dataholder);
		} else {
			logInsert(dataholder);
			result = this.internInsert(dataholder);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized boolean update(K dataholder) throws DataIntegrityViolationException {
		boolean result = false;
		if (this.isDataIntegrityViolation(dataholder, false)) {
			this.logAndThrowDataIntegrityViolationException(dataholder);
		} else {
			logUpdate(dataholder);
			result = this.internUpdate(dataholder);
		}
		return result;
	}

	/**
	 * insert the given dataholder into the database.
	 * 
	 * @param dataholder
	 *            to store
	 */
	protected abstract boolean internInsert(K dataholder);

	/**
	 * update the given dataholder into the database.
	 * 
	 * @param dataholder
	 *            to store
	 */
	protected abstract boolean internUpdate(K dataholder);

	/**
	 * return first found object like example. or <code>null</code>, if no
	 * object like given stored
	 * 
	 * @param example
	 *            as example
	 * @return first found object like example.
	 */
	@Override
  public K getOneLike(K example) {
		K result = null;
		List<K> examples = this.getObjectsLike(example);
		if (examples.size() > 0) {
			result = examples.get(0);
		}
		return result;
	}
}

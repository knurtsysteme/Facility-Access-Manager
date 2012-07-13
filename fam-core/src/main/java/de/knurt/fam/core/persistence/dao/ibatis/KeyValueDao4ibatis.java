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

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.KeyValue;
import de.knurt.fam.core.persistence.dao.KeyValueDao;
import de.knurt.fam.core.persistence.dao.UserDao;

/**
 * dao for every {@link KeyValue} stored in sql
 * 
 * @author Daniel Oltmanns
 * @since 0.20090828
 */
public class KeyValueDao4ibatis extends KeyValueDao {

	/** {@inheritDoc} */
	@Override
	protected boolean internInsert(KeyValue dataholder) {
		boolean result = false;
		try {
			FamSqlMapClientDaoSupport.sqlMap().insert("KeyValue.insert", dataholder);
		} catch (Exception e) {
			FamLog.exception(e, 201205071127l);
		}
		return result;

	}

	/** {@inheritDoc} */
	@Override
	protected boolean internUpdate(KeyValue dataholder) {
		boolean result = false;
		try {
			FamSqlMapClientDaoSupport.sqlMap().update("KeyValue.update", dataholder);
		} catch (Exception e) {
			FamLog.exception(e, 201205071128l);
		}
		return result;

	}

	/** {@inheritDoc} */
	@Override
	protected boolean isDataIntegrityViolation(KeyValue dataholder, boolean onInsert) {
		boolean result = dataholder.getK() == null || dataholder.getV() == null;
		if (result && onInsert) {
			// ↘ key already exists
			result = this.value(dataholder.key()) != null;
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	protected void logAndThrowDataIntegrityViolationException(KeyValue dataholder) throws DataIntegrityViolationException {
		String mess = "insert fail on " + dataholder + ".";
		DataIntegrityViolationException ex = new DataIntegrityViolationException(mess);
		FamLog.logException(UserDao.class, ex, mess, 201201111135l);
		throw ex;
	}

	/** {@inheritDoc} */
	@Override
	protected void logInsert(KeyValue dataholder) {
		FamLog.info(String.format("insert: " + dataholder), 201201111134l);

	}

	/** {@inheritDoc} */
	@Override
	protected void logUpdate(KeyValue dataholder) {
		FamLog.debug(String.format("update: " + dataholder), 201201111133l);
	}

	/** {@inheritDoc} */
	@Override
	protected void setIdToNextId(KeyValue dataholder) {
		dataholder.setId(this.getAll().size() + 1);
	}

	/** {@inheritDoc} */
	@Override
	public boolean delete(KeyValue like) {
		boolean result = false;
		try {
			FamSqlMapClientDaoSupport.sqlMap().delete("KeyValue.delete", like);
		} catch (Exception e) {
			FamLog.exception(e, 201204231013l);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public List<KeyValue> getAll() {
		return (List<KeyValue>) FamSqlMapClientDaoSupport.sqlMap().queryForList("KeyValue.select.all");
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public List<KeyValue> getObjectsLike(KeyValue example) {
		return (List<KeyValue>) FamSqlMapClientDaoSupport.sqlMap().queryForList("KeyValue.select.where_key", example);
	}

}
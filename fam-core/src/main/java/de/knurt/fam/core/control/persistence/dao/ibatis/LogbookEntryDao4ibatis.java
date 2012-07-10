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
package de.knurt.fam.core.control.persistence.dao.ibatis;

import java.util.ArrayList;
import java.util.List;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.control.persistence.dao.LogbookEntryDao;
import de.knurt.fam.core.model.config.Logbook;
import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.model.persist.User;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * dao for {@link LogbookEntry}s stored in sql
 * 
 * @author Daniel Oltmanns
 * @since 0.20090828
 */
public class LogbookEntryDao4ibatis extends LogbookEntryDao {

	/**
	 * construct me and set the object container
	 * 
	 * @see Db4oServletContextListener#getObjectContainer4users()
	 */
	public LogbookEntryDao4ibatis() {
	}

	/**
	 * anonymanize the given entry
	 * 
	 * @param entry
	 *            to delete
	 * @throws org.springframework.dao.DataIntegrityViolationException
	 *             if it is not possible to delete this user
	 */
	@Override
	public synchronized boolean delete(LogbookEntry entry) {
		boolean result = false;
		try {
			FamSqlMapClientDaoSupport.sqlMap().update("LogbookEntry.delete", entry);
			result = true;
		} catch (Exception e) {
			FamLog.exception(e, 201204231014l);
		}
		return result;
	}

	/**
	 * return all entries stored. sorted by date - newest first.
	 * 
	 * @return all entries stored
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<LogbookEntry> getAll() {
		return FamSqlMapClientDaoSupport.sqlMap().queryForList("LogbookEntry.select.all");
	}

	/**
	 * return all entries that equals the example. this result list is unsorted!
	 * 
	 * @param example
	 *            entry
	 * @return all entries that equals the example
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<LogbookEntry> getObjectsLike(LogbookEntry example) {
		return FamSqlMapClientDaoSupport.sqlMap().queryForList("LogbookEntry.select.like", example);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized boolean internInsert(LogbookEntry entry) {
		boolean result = false;
		try {
			FamSqlMapClientDaoSupport.sqlMap().insert("LogbookEntry.insert", entry);
		} catch (Exception e) {
			FamLog.exception(e, 201205071131l);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	protected synchronized boolean internUpdate(LogbookEntry entry) {
		boolean result = false;
		try {
			FamSqlMapClientDaoSupport.sqlMap().update("LogbookEntry.update", entry);
		} catch (Exception e) {
			FamLog.exception(e, 201205071130l);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public LogbookEntry getNewestEntry() {
		LogbookEntry result = null;
		List<LogbookEntry> all = this.getAll();
		if (all.size() > 0) {
			result = all.get(0);
		}
		return result;
	}

	/**
	 * return logbook entries <code>from</code> m <code>to</code> n. if to is
	 * smaller then from, return an empty set. if from and/or to is greater then
	 * size of all hits, then return hits from to last hit. (or empty set, if
	 * from is greater then size of all hits). sort hits from newest to oldest
	 * before cutting results.
	 * 
	 * @see Logbook#newestEntry
	 * @param logbookId
	 *            name of logbook
	 * @param from
	 *            index of hit from (inclusive)
	 * @param to
	 *            index of hit to (exclusive)
	 * @return logbook entries <code>from</code> m <code>to</code> n.
	 */
	@Override
	public List<LogbookEntry> get(String logbookId, int from, int to) {
		List<LogbookEntry> result;
		if (from < 0 || to < 0 || to < from) {
			result = new ArrayList<LogbookEntry>();
		} else {
			LogbookEntry example = new LogbookEntry();
			example.setLogbookId(logbookId);
			List<LogbookEntry> all = this.getObjectsLike(example);
			if (all.size() < to) {
				to = all.size();
			}
			result = all.subList(from, to);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public LogbookEntry getNewestEntry(String logbookKey) {
		LogbookEntry result = null;
		List<LogbookEntry> all = this.get(logbookKey, 0, 1);
		if (all.size() > 0) {
			result = all.get(0);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private List<LogbookEntry> getWhere(String where) {
		return FamSqlMapClientDaoSupport.sqlMap().queryForList("LogbookEntry.select.where", where);
	}

	/** {@inheritDoc} */
	@Override
	public List<LogbookEntry> getEntriesMadeIn(TimeFrame timeframe) {
		String biggerEqual = Util4Daos4ibatis.SDF_4_TIMESTAMP.format(timeframe.getDateStart());
		String smallerEqual = Util4Daos4ibatis.SDF_4_TIMESTAMP.format(timeframe.getDateEnd());
		String where = String.format("dateMade >= '%s' AND dateMade <= '%s'", biggerEqual, smallerEqual);
		return this.getWhere(where);
	}

	/** {@inheritDoc} */
	@Override
	public List<LogbookEntry> getAllLogbookEntriesOfUser(User user) {
		String where = String.format("ofUserName = '%s'", user.getUsername().toLowerCase().replaceAll("[^a-z0-9\\-]", ""));
		return this.getWhere(where);
	}
}

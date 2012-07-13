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

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.config.LogbookConfigDao;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * a dao resolving all about {@link LogbookEntry}s.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090323
 */
public abstract class LogbookEntryDao extends AbstractFamDao<LogbookEntry> {

	private String message;

	/**
	 * return a list of logbook entries matching given terms.
	 * 
	 * @param logbookKey
	 *            representing the logbook entries are in
	 * @param from
	 *            representing the start of entries to return
	 * @param to
	 *            representing the end of entries to return
	 * @return a list of logbook entries matching given terms.
	 */
	public abstract List<LogbookEntry> get(String logbookKey, int from, int to);

	/**
	 * return the newest entry of all logbookes.
	 * 
	 * @return the newest entry of all logbookes.
	 */
	public abstract LogbookEntry getNewestEntry();

	/**
	 * return the newest entry of given logbook.
	 * 
	 * @param logbookKey
	 *            representing a logbook
	 * @return the newest entry of given logbook.
	 */
	public abstract LogbookEntry getNewestEntry(String logbookKey);

	/**
	 * return true, if object is null or equals ""
	 * 
	 * @param o
	 *            to check
	 * @return true, if object is null or equals ""
	 */
	private boolean isEmpty(Object o) {
		return o == null || o.equals("");
	}

	/** {@inheritDoc} */
	@Override
	protected boolean isDataIntegrityViolation(LogbookEntry entry, boolean onInsert) {
		boolean result = false;
		this.message = ""; // this is important!
		if (this.isEmpty(entry)) {
			this.message = "entry is null";
		} else if (this.isEmpty(entry.getContent())) {
			this.message = "entry needs content";
		} else if (this.isEmpty(entry.getHeadline())) {
			this.message = "entry needs headline";
		} else if (this.isEmpty(entry.getLogbookId())) {
			this.message = "entry needs logbookId";
		} else if (this.isEmpty(entry.getDate())) {
			this.message = "entry needs date";
		} else if (this.isEmpty(entry.getLanguage())) {
			this.message = "entry needs language";
		} else if (this.isEmpty(entry.getOfUserName())) {
			this.message = "entry needs username";
		} else if (this.isEmpty(entry.getTags()) || entry.getTags().size() == 0) {
			this.message = "entry needs username";
		}
		if (this.isEmpty(this.message) == false) {
			result = true;
		} else { // special validations
			User example = UserFactory.me().blank();
			example.setUsername(entry.getOfUserName());
			if (FamDaoProxy.getInstance().getUserDao().userLikeExists(example) == false) {
				this.message = "username of entry must exist";
				result = true;
			} else if (LogbookConfigDao.getInstance().keyExists(entry.getLogbookId()) == false) {
				this.message = "logbookId must exist";
				result = true;
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	protected void logAndThrowDataIntegrityViolationException(LogbookEntry entry) throws DataIntegrityViolationException {
		String mess = "insert fail on " + entry + ".";
		DataIntegrityViolationException ex = new DataIntegrityViolationException(mess);
		FamLog.logException(LogbookEntryDao.class, ex, mess, 200904121718l);
		throw ex;
	}

	/** {@inheritDoc} */
	@Override
	protected void logInsert(LogbookEntry entry) {
		FamLog.logInfo(LogbookEntryDao.class, "insert " + entry + ".", 200911181628l);
	}

	/** {@inheritDoc} */
	@Override
	protected void logUpdate(LogbookEntry entry) {
		FamLog.logInfo(LogbookEntryDao.class, "update " + entry + ".", 200904121719l);
	}

	/** {@inheritDoc} */
	@Override
	protected void setIdToNextId(LogbookEntry entry) {
		entry.setId(this.getAll().size() + 1);
	}

	/**
	 * insert given entry if and only if it does not violate the data integrity.
	 * 
	 * @param entry
	 *            to store
	 * @throws org.springframework.dao.DataIntegrityViolationException
	 *             if storing would violate data integrity
	 */
	@Override
	public synchronized boolean insert(LogbookEntry entry) throws DataIntegrityViolationException {
		LogbookConfigDao.getInstance().addLastEntry(entry);
		return super.insert(entry);
	}

	/**
	 * return all logbook entries made in the given timeframe including start
	 * and end point of time. return an empty array if nothing found.
	 * 
	 * @see LogbookEntry#getDate()
	 * @param timeframe
	 *            requested
	 * @return all logbook entries made in the given timeframe
	 */
	public abstract List<LogbookEntry> getEntriesMadeIn(TimeFrame timeframe);

	/**
	 * return all entries made by the given user
	 * 
	 * @param user
	 *            given
	 * @return all entries made by the given user
	 */
	public abstract List<LogbookEntry> getAllLogbookEntriesOfUser(User user);
}

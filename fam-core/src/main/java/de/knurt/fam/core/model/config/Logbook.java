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

import de.knurt.fam.core.control.persistence.dao.config.LogbookConfigDao;
import de.knurt.fam.core.model.persist.LogbookEntry;

/**
 * a logbook. please make sure, this does not contain any language specific
 * strings being injected, because this is not possible.
 * 
 * use the access class {@link LogbookConfigDao} for this mission.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090410
 */
public class Logbook {

	private int entryCount;

	private LogbookEntry newestEntry;

	/**
	 * a logbook. must always be injected!
	 */
	private Logbook() {
		this.entryCount = -1;
	}

	/**
	 * return the count of all logbook entries
	 * 
	 * @return the count of all logbook entries
	 */
	public int getEntryCount() {
		return entryCount;
	}

	/**
	 * set the count of all logbook entries
	 * 
	 * @param entryCount
	 *            the count of all logbook entries
	 */
	public void setEntryCount(int entryCount) {
		this.entryCount = entryCount;
	}

	/**
	 * return the newest entry made in this logbook.
	 * 
	 * @return the newest entry made in this logbook.
	 */
	public LogbookEntry getNewestEntry() {
		return newestEntry;
	}

	/**
	 * set the newest entry made in this logbook.
	 * 
	 * @param newestEntry
	 *            the newest entry made in this logbook.
	 */
	public void setNewestEntry(LogbookEntry newestEntry) {
		this.newestEntry = newestEntry;
	}

	/**
	 * return the key, representing this logbook
	 * 
	 * @return key, representing this logbook
	 */
	public String getKey() {
		return LogbookConfigDao.getInstance().getKey(this);
	}

}
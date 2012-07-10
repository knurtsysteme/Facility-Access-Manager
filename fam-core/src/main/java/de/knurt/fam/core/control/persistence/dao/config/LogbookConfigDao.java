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
package de.knurt.fam.core.control.persistence.dao.config;

import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.content.text.FamText;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.config.Logbook;
import de.knurt.fam.core.model.persist.LogbookEntry;

/**
 * a data holder for all defined roles and specific roles
 * 
 * for adding new Logbooks, please add text for every get-method into
 * {@link "de.knurt.fam.core.content.text.logbooks.properties"} in all
 * languages. The messageBase, used for all these messages is the key used for
 * this Logbook in {@link LogbookConfigDao}.
 * 
 * the roles all have to be injected
 * 
 * @author Daniel Oltmanns
 * @since 0.20090412 (04/12/2009)
 */
public class LogbookConfigDao extends AbstractConfigDao<Logbook> {

	private volatile static LogbookConfigDao me;
	private Properties labels, desciptions;
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
	 * return all descriptions of all logooks configured.
	 * 
	 * @return all descriptions of all logooks.
	 */
	public Properties getDescriptions() {
		if (this.desciptions == null) {
			this.desciptions = new Properties();
			for (String key : this.configuredInstances.keySet()) {
				desciptions.put(key, FamText.getInstance().getMessage(key + ".description"));
			}
		}
		return this.desciptions;
	}

	/**
	 * return the latest entry made for the given logbook.
	 * 
	 * @param key
	 *            representing the logbook
	 * @return the latest entry made for the given logbook.
	 */
	public LogbookEntry getNewestEntry(String key) {
		LogbookEntry result = null;
		if (this.configuredInstances.containsKey(key)) {
			result = this.configuredInstances.get(key).getNewestEntry();
			if (result == null) { // no entry so far or first time querying this
				// after server starts
				if (this.getEntryCount(key) > 0) { // first time querying this
					// after server starts
					result = FamDaoProxy.getInstance().getLogbookEntryDao().getNewestEntry(key);
					this.configuredInstances.get(key).setNewestEntry(result);
				}
				// else no entry so far - return null
			}
		}
		return result;
	}

	/**
	 * return the size of entries in the logbook with the given key.
	 * 
	 * @param key
	 *            of the logbook the size is requested
	 * @return the size of entries in the logbook with the given key.
	 */
	public int getEntryCount(String key) {
		if (this.configuredInstances.get(key).getEntryCount() == -1) {
			LogbookEntry example = new LogbookEntry();
			example.setLogbookId(key);
			int entryCount = FamDaoProxy.getInstance().getLogbookEntryDao().getObjectsLike(example).size();
			this.configuredInstances.get(key).setEntryCount(entryCount);
		}
		return this.configuredInstances.get(key).getEntryCount();
	}

	/**
	 * return tags of the given logbook
	 * 
	 * @param key
	 *            representing the logbook
	 * @return tags of the given logbook
	 */
	public String[] getTags(String key) {
		return FamText.getInstance().getMessage(key + ".tags").split(",");
	}

	/**
	 * return a label of the given logbook
	 * 
	 * @param key
	 *            representing the logbook
	 * @return a label of the given logbook
	 */
	public String getLabel(String key) {
		return this.getLabels().get(key).toString();
	}

	/**
	 * return a description of the given logbook
	 * 
	 * @param key
	 *            representing the logbook
	 * @return a description of the given logbook
	 */
	public String getDescription(String key) {
		return this.getDescriptions().get(key).toString();
	}

	/**
	 * return all labels of all facilities. the message base is the beginning of a
	 * property key. all messages are in a properties files for
	 * internationalization that can be found in
	 * {@link "de.knurt.fam.core.content.text"}. this one is the file
	 * "facilities.properties".
	 * 
	 * @return all labels of all facilities
	 */
	public Properties getLabels() {
		if (this.labels == null) {
			this.labels = new Properties();
			for (String key : this.configuredInstances.keySet()) {
				labels.put(key, FamText.getInstance().getMessage(key + ".label"));
			}
		}
		return this.labels;
	}

	/**
	 * @param configuredInstances
	 *            to set
	 */
	@Required
	@Override
	public void setConfiguredInstances(Map<String, Logbook> configuredInstances) {
		this.configuredInstances = configuredInstances;
	}

	/**
	 * set the last entry of the logbook ent increment entry count.
	 * 
	 * @param entry
	 */
	public void addLastEntry(LogbookEntry entry) {
		if (this.configuredInstances.containsKey(entry.getLogbookId())) {
			this.configuredInstances.get(entry.getLogbookId()).setNewestEntry(entry);
			this.configuredInstances.get(entry.getLogbookId()).setEntryCount(this.getEntryCount(entry.getLogbookId()) + 1);
		}
	}
}
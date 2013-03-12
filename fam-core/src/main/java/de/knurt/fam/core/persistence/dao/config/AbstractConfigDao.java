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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * abstract access to all config daos.<br />
 * a config dao provides access to model-objects, that are configured in the
 * application context.<br />
 * <ul>
 * <li>All access classes in this package ...
 * <ul>
 * <li>... are singletons</li>
 * <li>... are injected</li>
 * <li>... implement {@link AbstractConfigDao}</li>
 * <li>... contain all injected model objects</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author Daniel Oltmanns
 * @since 0.20090412 (04/12/2009)
 */
abstract class AbstractConfigDao<T> implements Access<T> {

	/**
	 * return a list of all configured instances.
	 * 
	 * @return a list of all configured instances.
	 */
	public List<T> getAll() {
		List<T> result = new ArrayList<T>();
		result.addAll(this.getCollectionOfAllConfigured());
		return result;
	}

	/**
	 * return all keys defined in context for the different objects managed.
	 * 
	 * @return all keys defined in context for the different objects managed.
	 */
	@Override
  public Set<String> getKeys() {
		return this.getConfiguredInstances().keySet();
	}

	/**
	 * return a collection of all configured object mangaged by the dao.
	 * 
	 * @return a collection of all configured object mangaged by the dao.
	 */
	public Collection<T> getCollectionOfAllConfigured() {
		return this.getConfiguredInstances().values();
	}

	/**
	 * return a single configured object managed by the dao.
	 * 
	 * @param key
	 *            of the object
	 * @return a single configured object managed by the dao.
	 */
	public T getConfiguredInstance(String key) {
		return this.getConfiguredInstances().get(key);
	}

	/**
	 * return all configured objects managed by the dao.
	 * 
	 * @return all configured objects managed by the dao.
	 */
	protected abstract Map<String, T> getConfiguredInstances();

	/**
	 * return the key of the configured instance.
	 * 
	 * @param configuredInstance
	 *            the key representing this instance must be returned.
	 * @return the key of the configured instance.
	 */
	public String getKey(T configuredInstance) {
		String result = "";
		for (String key : this.getKeys()) {
			if (this.getConfiguredInstance(key).equals(configuredInstance)) {
				result = key;
				break;
			}
		}
		return result;
	}

	/**
	 * return true, if an object with given key is managed by the dao.
	 * 
	 * @param key
	 *            to check
	 * @return true, if an object with given key is managed by the dao.
	 */
	@Override
  public boolean keyExists(String key) {
		return this.getKeys().contains(key);
	}
}

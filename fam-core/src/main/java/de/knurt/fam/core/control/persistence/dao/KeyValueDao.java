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

import de.knurt.fam.core.model.persist.KeyValue;

/**
 * a table just storing key values
 * 
 * @author Daniel Oltmanns
 * @since 1.6.1 (01/11/2012)
 */
public abstract class KeyValueDao extends AbstractFamDao<KeyValue> {
	public void put(String key, String value) {
		if (this.exists(key)) {
			KeyValue kv = this.get(key);
			kv.setV(value);
			kv.update();
		} else {
			KeyValue kv = new KeyValue();
			kv.setK(key);
			kv.setV(value);
			kv.insert();
		}
	}

	private KeyValue get(String key) {
		KeyValue example = new KeyValue();
		example.setK(key);
		List<KeyValue> kvs = this.getObjectsLike(example);
		if (kvs.size() == 1) {
			return kvs.get(0);
		} else {
			return null;
		}
	}

	public String value(String key) {
		KeyValue result = this.get(key);
		return result != null ? result.value() : null;
	}

	public void delete(String key) {
		this.delete(this.get(key));
	}

	public boolean exists(String key) {
		return this.value(key) != null;
	}
}

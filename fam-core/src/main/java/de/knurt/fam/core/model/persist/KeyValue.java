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

import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.heinzelmann.util.query.Identificable;

/**
 * for mapping table key_value
 * 
 * @author Daniel Oltmanns
 * @since 1.6.1 (01/11/2012)
 */
public class KeyValue implements Storeable, Identificable {

	private String k, v;

	/**
	 * construct with a key and a value
	 * @author "Daniel Oltmanns <daniel.oltmanns@it-power.org>"
	 * @since 19.11.2013
	 * @param k
	 * @param v
	 */
	public KeyValue(String k, String v) {
	  this.setK(k);
	  this.setV(v);
  }

	/**
	 * construct with null key and null value
	 * @author "Daniel Oltmanns <daniel.oltmanns@it-power.org>"
	 * @since 19.11.2013
	 */
  public KeyValue() {
  }

  /** {@inheritDoc} */
	@Override
	public Integer getId() {
		return this.id;
	}

	/** {@inheritDoc} */
	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	private Integer id;

	public String getK() {
		return k;
	}

	public void setK(String k) {
		this.k = k;
	}

	/** return a string looks like json (but it is not) */
	@Override
	public String toString() {
		return String.format("{'%s': '%s'}", this.k, this.v);
	}

	public String getV() {
		return v;
	}

	public void setV(String v) {
		this.v = v;
	}

	/** {@inheritDoc} */
	@Override
	public boolean insert() throws DataIntegrityViolationException {
		return FamDaoProxy.keyValueDao().insert(this);

	}

	/** {@inheritDoc} */
	@Override
	public boolean update() throws DataIntegrityViolationException {
		return FamDaoProxy.keyValueDao().update(this);
	}

	public String key() {
		return this.getK();
	}

	public String value() {
		return this.getV();
	}

}

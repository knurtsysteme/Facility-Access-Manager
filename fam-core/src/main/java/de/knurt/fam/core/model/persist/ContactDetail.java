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

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.heinzelmann.util.query.Identificable;

/**
 * unspecified contact detail of a user.
 * 
 * @author Daniel Oltmanns
 * @since 0.20100123
 */
public class ContactDetail implements Storeable, Identificable, Deletable {

	private String username, title, detail;
	private Integer id;

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

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the detail
	 */
	public String getDetail() {
		return detail;
	}

	/**
	 * @param detail
	 *            the detail to set
	 */
	public void setDetail(String detail) {
		this.detail = detail;
	}

	/** {@inheritDoc} */
	@Override
	public boolean insert() throws DataIntegrityViolationException {
		return FamDaoProxy.userDao().insert(this);
	}

	/** {@inheritDoc} */
	@Override
	public boolean update() throws DataIntegrityViolationException {
		return FamDaoProxy.userDao().update(this);
	}

	/** {@inheritDoc} */
	@Override
	public boolean delete() throws DataIntegrityViolationException {
		return FamDaoProxy.userDao().delete(this);
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
}

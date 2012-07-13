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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.util.mvc.LogbookEntryForm;
import de.knurt.heinzelmann.util.query.Identificable;

/**
 * an entry for a logbook.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090410
 */
public class LogbookEntry implements Storeable, Identificable {

	private String logbookId, content, headline, ofUserName;
	private Locale language;
	private Date date;
	private List<String> tags;
	private Integer id;

	/** {@inheritDoc} */
	@Override
	public boolean insert() throws DataIntegrityViolationException {
		return FamDaoProxy.getInstance().getLogbookEntryDao().insert(this);
	}

	/** {@inheritDoc} */
	@Override
	public boolean update() throws DataIntegrityViolationException {
		return FamDaoProxy.getInstance().getLogbookEntryDao().update(this);
	}

	/**
	 * set the language the entry has been made.
	 * 
	 * @param language
	 *            the entry has been made
	 */
	public void setLanguageAsString(String language) {
		this.setLanguage(new Locale(language));
	}

	/**
	 * return the language the entry has been made.
	 * 
	 * @return the language the entry has been made.
	 */
	public String getLanguageAsString() {
		return this.language == null ? null : this.language.toString();
	}

	/**
	 * set the tags as given.
	 * 
	 * @param csv
	 *            comma seperated tags. must have the format
	 *            <code>a, b, c</code> to set <code>{a, b, c}</code> as tags for
	 *            this entry.
	 */
	public void setTagsFromCsv(String csv) {
		this.tags = LogbookEntryForm.getTags(csv);
	}

	/**
	 * @return the logbookId
	 */
	public String getLogbookId() {
		return logbookId;
	}

	/**
	 * @param logbookId
	 *            the logbookId to set
	 */
	public void setLogbookId(String logbookId) {
		this.logbookId = logbookId;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the headline
	 */
	public String getHeadline() {
		return headline;
	}

	/**
	 * @param headline
	 *            the headline to set
	 */
	public void setHeadline(String headline) {
		this.headline = headline;
	}

	/**
	 * @return the ofUser
	 */
	public String getOfUserName() {
		return ofUserName;
	}

	/**
	 * set the username of the user that made this entry.
	 * 
	 * @param ofUserName
	 *            the username of the user that made this entry.
	 */
	public void setOfUserName(String ofUserName) {
		this.ofUserName = ofUserName;
	}

	/**
	 * @return the language
	 */
	public Locale getLanguage() {
		return language;
	}

	/**
	 * @param language
	 *            the language to set
	 */
	public void setLanguage(Locale language) {
		this.language = language;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(Date date) {
		if (date.before(new Date(1000))) { // avoid exception: --- Cause:
			// com.mysql.jdbc.MysqlDataTruncation:
			// Data truncation: Incorrect
			// datetime value: '1970-01-01
			// 01:00:00' for column 'dateMade'
			// at row 1; nested exception is
			// com.ibatis.common.jdbc.exception.NestedSQLException:
			date = new Date(1000);
		}
		this.date = date;
	}

	/**
	 * @return the tags
	 */
	public List<String> getTags() {
		return tags;
	}

	/**
	 * @param tags
	 *            the tags to set
	 */
	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	/**
	 * return the existing tags as comma separeted values.
	 * 
	 * @return the existing tags as comma separeted values.
	 */
	public String getTagsAsCsv() {
		String result = "";
		if (this.tags != null) {
			for (String tag : this.tags) {
				result += ", " + tag;
			}
		}
		return result.isEmpty() ? null : result.substring(2);
	}

	/** {@inheritDoc} */
	@Override
	public Integer getId() {
		return id;
	}

	/** {@inheritDoc} */
	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	public boolean delete() {
		return FamDaoProxy.logbookEntryDao().delete(this);
	}
}

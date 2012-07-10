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
package de.knurt.fam.core.util.mvc;

import java.util.ArrayList;
import java.util.List;

import de.knurt.fam.core.model.persist.LogbookEntry;

/**
 * A data holder for login inputs
 * 
 * @author Daniel Oltmanns
 * @since 0.20090308 (03/08/2009)
 */
public class LogbookEntryForm extends LogbookEntry {

	private String tagoptional, musttag;

	/**
	 * melt tag inputs and set it to
	 * {@link LogbookEntry#setTags(java.util.List)}
	 * 
	 * @see LogbookEntry#setTags(java.util.List)
	 */
	public void meltTags() {
		String csv = this.getMusttag();
		if (this.getTagoptional() != null && this.getTagoptional().equals("") == false) {
			csv += ", " + this.getTagoptional();
		}
		this.setTags(getTags(csv));
	}

	/**
	 * return tags set for the logbook entry. assume that tags are seperated
	 * with commas. at base operation do something like
	 * <code>csv.split(",")</code>
	 * 
	 * @param csv
	 *            comma seperatad values representing tags
	 * @return tags set for the logbook entry.
	 */
	public static List<String> getTags(String csv) {
		List<String> meltTags = new ArrayList<String>();
		if (csv != null) {
			for (String tag : csv.split(",")) {
				meltTags.add(tag.trim());
			}
		}
		return meltTags;
	}

	/**
	 * @return the tagoptional
	 */
	public String getTagoptional() {
		return tagoptional;
	}

	/**
	 * @param tagoptional
	 *            the tagoptional to set
	 */
	public void setTagoptional(String tagoptional) {
		this.tagoptional = tagoptional;
	}

	/**
	 * @return the musttag
	 */
	public String getMusttag() {
		return musttag;
	}

	/**
	 * @param musttag
	 *            the musttag to set
	 */
	public void setMusttag(String musttag) {
		this.musttag = musttag;
	}
}

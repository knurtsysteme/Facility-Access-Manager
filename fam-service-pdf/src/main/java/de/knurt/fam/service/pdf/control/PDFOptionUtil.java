/*
 * Copyright 2009-2011 by KNURT Systeme (http://www.knurt.de)
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
package de.knurt.fam.service.pdf.control;

import de.knurt.fam.service.pdf.model.PDFOption;


/**
 * configuration adapter for PDFOption
 * 
 * @see PDFOption
 * @author Daniel Oltmanns
 * @since 1.4.0 (06/06/2011)
 */
public class PDFOptionUtil {
	private String defaultTemplate = null;

	public void setDefaultTemplate(String defaultTemplate) {
		this.defaultTemplate = defaultTemplate;
	}

	public String getDefaultTemplateUrl() {
		return defaultTemplate;
	}

	/** one and only instance of PDFOptionUtil */
	private volatile static PDFOptionUtil me;

	/** construct PDFOptionUtil */
	private PDFOptionUtil() {
	}

	/**
	 * return the one and only instance of PDFOptionUtil
	 * 
	 * @return the one and only instance of PDFOptionUtil
	 */
	public static PDFOptionUtil getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (PDFOptionUtil.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new PDFOptionUtil();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of PDFOptionUtil
	 */
	public static PDFOptionUtil me() {
		return getInstance();
	}


}

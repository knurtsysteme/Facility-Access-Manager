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
package de.knurt.fam.template.util;

import org.jdom.Element;

import de.knurt.fam.template.model.TemplateContentPropertiesDefault;
import de.knurt.fam.template.model.TemplatePage;
import de.knurt.fam.template.model.TemplatePageDefault;

/**
 * util for an easy access to any page over velocity releazid as a simple factory.
 * 
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/14/2010)
 */
public class PageUtil {
	/** one and only instance of PageUtil */
	private volatile static PageUtil me;

	/** construct PageUtil */
	private PageUtil() {
	}

	/**
	 * return the one and only instance of PageUtil
	 * 
	 * @return the one and only instance of PageUtil
	 */
	public static PageUtil getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (PageUtil.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new PageUtil();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of PageUtil
	 */
	public static PageUtil me() {
		return getInstance();
	}
	
	public TemplatePage get(String resourceName) {
			Element page = TemplateContentPropertiesDefault.me().getCustomLanguagePage(resourceName);
			return new TemplatePageDefault(page);
	}
}

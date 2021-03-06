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
package de.knurt.fam.template.model;

import org.jdom.Element;

/**
 * produce content properties for the custom configuration and language. the
 * configuration is stored in xml files.
 * 
 * @author Daniel Oltmanns
 * @since 1.21 (09/30/2010)
 */
public interface TemplateContentProperties {
	/**
	 * reload the template content and configuration properties
	 */
	public void reload();

	/**
	 * return the entire xml configuration
	 * 
	 * @return the entire xml configuration
	 */
	public Element getCustomConfig();

	/**
	 * return the entire xml language
	 * 
	 * @return the entire xml language
	 */
	public Element getCustomLanguage();

	/**
	 * return the xml configuration for the given page
	 * 
	 * @param name
	 *            of the page
	 * @return the xml configuration for the given page
	 */
	public Element getCustomConfigPage(String name);

	/**
	 * return the xml language for the given page
	 * 
	 * @param name
	 *            of the page
	 * @return the xml language for the given page
	 */
	public Element getCustomLanguagePage(String name);

}

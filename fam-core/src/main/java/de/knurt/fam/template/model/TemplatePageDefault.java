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

import de.knurt.fam.template.util.TemplateHtml;

/**
 * a bean for a page
 * 
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/14/2010)
 */
public class TemplatePageDefault implements TemplatePage {
	private Element page;

	public TemplatePageDefault(Element page) {
		this.page = page;
	}

	@Override
  public String getHeadline() {
		return this.getValue("headline");
	}

	@Override
  public String getTitle() {
		return this.getValue("title");
	}

	@Override
  public String getValue(String key) {
		return this.page.getChildText(key);
	}

	@Override
  public String getHref() {
		return TemplateHtml.me().getHref(this);
	}

	@Override
  public String getResourceName() {
		return this.page.getAttributeValue("name");
	}

}

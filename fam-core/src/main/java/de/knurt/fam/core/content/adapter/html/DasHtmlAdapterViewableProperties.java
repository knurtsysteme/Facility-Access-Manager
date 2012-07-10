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
package de.knurt.fam.core.content.adapter.html;

import de.knurt.fam.core.model.config.ViewableProperties;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;

/**
 * adapt a nice looking for viewable properties
 * 
 * @author Daniel Oltmanns
 * @since 1.21 (09/27/2010)
 */
@Deprecated
public class DasHtmlAdapterViewableProperties extends FamHtmlAdapter<ViewableProperties> {

	public HtmlElement getKey() {
		return this.get(this.getOriginal().getKey());
	}

	private HtmlElement get(String content) {
		return HtmlFactory.get("span", this.split(content)).style("font-family", "monospace");
	}

	private String split(String key) {
		String result = "";
		if (key == null) {
			result = "null";
		} else if (key.length() > 0) {
			int pointer = 0;
			int steps = 30;
			while (pointer + steps < key.length()) {
				result += key.substring(pointer, pointer + steps);
				if (pointer % 20 == 0) {
					result += "<br />";
				}
				pointer += steps;
			}
			result += key.substring(pointer, key.length());
		}
		return result;
	}

	public HtmlElement getValue() {
		return this.get(this.getOriginal().getValue());
	}

	public DasHtmlAdapterViewableProperties(ViewableProperties original) {
		super(original);
	}

	public String getCategory() {
		return this.getOriginal().getCategory();
	}
}

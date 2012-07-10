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
package de.knurt.fam.core.model.config;

import de.knurt.heinzelmann.util.adapter.ViewableObject;

/**
 * properties that are viewable
 * 
 * @author Daniel Oltmanns
 * @since 1.21 (09/27/2010)
 */
public class ViewableProperties implements ViewableObject {

	private static final long serialVersionUID = 1L;

	private String category, key, value;

	public ViewableProperties(String category, String key, String value) {
		super();
		this.key = key;
		this.value = value;
		this.category = category;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}
	
	public String getCategory() {
		return category;
	}

}

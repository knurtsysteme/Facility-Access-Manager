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

import de.knurt.fam.core.model.persist.Address;

/**
 * resolve country names and codes.
 * 
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/18/2010)
 */
public class CountryResolver {
	
	/** one and only instance of CountryResolver */
	private volatile static CountryResolver me;

	/** construct CountryResolver */
	private CountryResolver() {
	}

	/**
	 * return the one and only instance of CountryResolver
	 * 
	 * @return the one and only instance of CountryResolver
	 */
	public static CountryResolver getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (CountryResolver.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new CountryResolver();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of CountryResolver
	 */
	public static CountryResolver me() {
		return getInstance();
	}
	
	private String getCountryCode(Address address) {
		return address == null || address.getCountry() == null || address.getCountry().trim().equals("-1") || address.getCountry().isEmpty() ? null : address.getCountry().trim();
	}

	public String getCountryName(Address address) {
		String result = null;
		String code = this.getCountryCode(address);
		if(code != null) {
			for(Object element : TemplateConfig.me().getContentProperties(). getCustomLanguage().getChild("global").getChild("countries").getChildren()) {
				if(((Element)element).getAttributeValue("code").equalsIgnoreCase(code)) {
					result = ((Element)element).getAttributeValue("name");
					break;
				}
			}
		}
		return result;
	}
}

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
package de.knurt.fam.core.content.adapter.plaintext;

import de.knurt.fam.core.model.persist.Address;

/**
 * adapter to adapt a address into plain text
 * 
 * @author Daniel Oltmanns
 * @since 0.20090916 (09/16/2009)
 */
public class PlaintextAdapterAddress {
	private Address address;

	public PlaintextAdapterAddress(Address address) {
		super();
		this.address = address;
	}

	/**
	 * return the address in one row.
	 * 
	 * example 1 full address:
	 * 
	 * <pre>
	 * Mühlenstr. 1a, 24989 Dollerup, Germany
	 * </pre>
	 * 
	 * example 2 missing country
	 * 
	 * <pre>
	 * Mühlenstr. 1a, 24989 Dollerup
	 * </pre>
	 * 
	 * example 3: missing zipcode or city
	 * 
	 * <pre>
	 * Mühlenstr. 1a, Germany
	 * </pre>
	 * 
	 * example 4: missing street
	 * 
	 * <pre>
	 * 24989 Dollerup, Germany
	 * </pre>
	 * 
	 * example 5: missing all
	 * 
	 * <pre>
	 * [empty string]
	 * </pre>
	 * 
	 * example 6: missing street number
	 * 
	 * <pre>
	 * Mühlenstr., 24989 Dollerup, Germany
	 * </pre>
	 * 
	 * @return the address in one row. for example:
	 */
	public String getInOneRow() {
		String result = "";
		if (this.address != null) {
			if (this.address.getStreet().isEmpty() == false) {
				result += this.address.getStreet(); // INTLANG
				if (this.address.getStreetno().isEmpty() == false) {
					result += " " + this.address.getStreetno(); // INTLANG
				}
			}
			if (this.address.getZipcode().isEmpty() == false && this.address.getCity().isEmpty() == false) {
				if (!result.isEmpty()) {
					result += ", ";
				}
				result += this.address.getZipcode(); // INTLANG
				result += " " + this.address.getCity(); // INTLANG
			}
			if (this.address.hasCountry()) {
				if (!result.isEmpty()) {
					result += ", ";
				}
				result += this.address.getCountry(); // INTLANG
			}
		}
		return result;
	}

}

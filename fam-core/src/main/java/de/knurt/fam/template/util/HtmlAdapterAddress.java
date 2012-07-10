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

import de.knurt.fam.core.content.adapter.html.FamHtmlAdapter;
import de.knurt.fam.core.model.persist.Address;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;

/**
 * adapter for putting out an {@link Address}
 * 
 * @author Daniel Oltmanns
 * @since 0.20090818 (08/18/2009)
 */
@Deprecated
public class HtmlAdapterAddress extends FamHtmlAdapter<Address> {

	private final Address address;

	/**
	 * adapter for an {@link Address}
	 * 
	 * @param address
	 *            to adapt
	 */
	public HtmlAdapterAddress(Address address) {
		super(address);
		this.address = address;
	}

	/**
	 * return the full address as html table.
	 * 
	 * @return the full address as html table.
	 */
	public HtmlElement getFullAsHtml() {
		HtmlElement table = super.getHtmlTable();
		boolean withInfo = false;
		if (this.address != null) {
			if (this.address.getStreet().isEmpty() == false) {
				table.add(HtmlFactory.getInstance().get_tr("street", this.address.getStreet())); // INTLANG
				withInfo = true;
			}
			if (this.address.getStreetno().isEmpty() == false) {
				table.add(HtmlFactory.getInstance().get_tr("street number", this.address.getStreetno())); // INTLANG
				withInfo = true;
			}
			if (this.address.getZipcode().isEmpty() == false) {
				table.add(HtmlFactory.getInstance().get_tr("zip", this.address.getZipcode())); // INTLANG
				withInfo = true;
			}
			if (this.address.getCity().isEmpty() == false) {
				table.add(HtmlFactory.getInstance().get_tr("city", this.address.getCity())); // INTLANG
				withInfo = true;
			}
			if (this.address.hasCountry()) {
				table.add(HtmlFactory.getInstance().get_tr("country", this.address.getCountry())); // INTLANG
				withInfo = true;
			}
		}
		return withInfo ? table : null;
	}

	/**
	 * return a compact view of the address without name. this may like:<br />
	 * <code>
	 *  Musterstrasse 5
	 *  46524 Oberhausen
	 *  Deutschland
     * </code>
	 * 
	 * @param lineBreak
	 *            kind of line break, like <code>&lt;br /&gt;</code> on html
	 * @return
	 */
	public String getFullAsText(String lineBreak) {
		String result = address.getStreet();
		if (result == null || result.trim().isEmpty()) {
			result = "";
		} else {
			String sno = address.getStreetno() == null ? "" : address.getStreetno().trim();
			result = result.trim() + " " + sno + lineBreak;
		}
		String zipcode = address.getZipcode();
		boolean hasZipcodeOrCity = false;
		if (zipcode != null && !zipcode.trim().isEmpty()) {
			result += zipcode.trim() + " ";
			hasZipcodeOrCity = true;
		}
		String city = address.getCity() == null ? "" : address.getCity().trim();
		if (city != null && !city.trim().isEmpty()) {
			result += city.trim();
			hasZipcodeOrCity = true;
		}
		if (hasZipcodeOrCity) {
			result += lineBreak;
		}
		String countryName = CountryResolver.me().getCountryName(address);
		if(countryName != null){
			result += countryName;
		}
		return result;
	}
}

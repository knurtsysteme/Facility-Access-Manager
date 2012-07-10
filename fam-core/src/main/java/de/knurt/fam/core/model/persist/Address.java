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
package de.knurt.fam.core.model.persist;

import de.knurt.fam.template.util.CountryResolver;
import de.knurt.heinzelmann.util.adapter.ViewableObject;
import de.knurt.heinzelmann.util.query.Identificable;

/**
 * an address bean
 * 
 * @author Daniel Oltmanns
 * @since 0.20090303
 */
public class Address implements ViewableObject, Identificable {

	private String zipcode, street, streetno, city, country;

	/**
	 * @return the zipcode
	 */
	public String getZipcode() {
		return zipcode == null ? "" : zipcode;
	}

	/**
	 * @param zipcode
	 *            the zipcode to set
	 */
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getCountryName() {
		return CountryResolver.me().getCountryName(this);
	}

	/**
	 * @return the street
	 */
	public String getStreet() {
		return street == null ? "" : street;
	}

	/**
	 * @param street
	 *            the street to set
	 */
	public void setStreet(String street) {
		this.street = street;
	}

	/**
	 * @return the streetno
	 */
	public String getStreetno() {
		return streetno == null ? "" : streetno;
	}

	/**
	 * @param streetno
	 *            the streetno to set
	 */
	public void setStreetno(String streetno) {
		this.streetno = streetno;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city == null ? "" : city;
	}

	/**
	 * @param city
	 *            the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		if (country == null) {
			country = "-1";
		}
		return country;
	}

	/**
	 * @param country
	 *            the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * return true, if the country is set for this address. this assumes, that a
	 * not set country equals "-1", as it is on form inputs for countries.
	 * 
	 * @return true, if the country is set for this address.
	 */
	public boolean hasCountry() {
		return !this.getCountry().equals("-1");
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	private Integer id;

	/**
	 * return the street with street number. if street is <code>null</code>
	 * return <code>null</code> (even if number is not <code>null</code>). if
	 * streetno is <code>null</code> return only the street name.
	 * 
	 * if both value are set, return street in format: "[name] [number]". E.g. "Baker Street 10".
	 * 
	 * do not care about international conventions
	 * 
	 * @see #getStreet()
	 * @see #getStreetno()
	 * @return a String of the street with street number
	 */
	public String getStreetWithStreetno() {
		String result = null;
		String no = this.getStreetno();
		String name = this.getStreet();
		if (name != null) {
			result = name;
		}
		if (result != null && no != null) {
			result += " " + no;
		}
		return result;
	}

	public void setStreetWithStreetno(String streetWithStreetno) {
		if (streetWithStreetno != null) {
			streetWithStreetno = streetWithStreetno.trim();
			int delimeterIndex = streetWithStreetno.lastIndexOf(" ");
			if (delimeterIndex > 0) {
				// ↖ a (maybe) number exists
				String streetName = streetWithStreetno.substring(0, delimeterIndex).trim();
				String no = streetWithStreetno.substring(delimeterIndex).trim();
				this.setStreet(streetName);
				this.setStreetno(no);
			} else {
				this.setStreet(streetWithStreetno);
				this.setStreetno(null);
			}
		}
	}

}

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
package de.knurt.fam.core.content.adapter.json;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.Address;
import de.knurt.heinzelmann.util.adapter.JSONAdapter;
import de.knurt.heinzelmann.util.adapter.StringAdapter;

/**
 * adapter to adapt a address into json format.
 * 
 * @author Daniel Oltmanns
 * @since 1.6.1 (01/09/2012)
 */
public class JSONAdapterAddress implements StringAdapter<Address>, JSONAdapter<Address> {

	/** {@inheritDoc} */
	@Override
	public String getAsString(Address address) {
		return this.getAsJSONObject(address).toString();
	}

	/** {@inheritDoc} */
	@Override
	public String getAsString(List<Address> addresses) {
		return this.getAsJSONArray(addresses).toString();
	}

	/** {@inheritDoc} */
	@Override
	public JSONArray getAsJSONArray(List<Address> addresses) {
		JSONArray result = new JSONArray();
		for (Address address : addresses) {
			result.put(this.getAsJSONObject(address));
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public JSONObject getAsJSONObject(Address address) {
		JSONObject result = null;
		if (address != null) {
			try {
				result = new JSONObject();
				result.put("city", address.getCity());
				result.put("country_name", address.getCountryName());
				result.put("country", address.getCountry());
				result.put("street", address.getStreet());
				result.put("street_with_streetno", address.getStreetWithStreetno());
				result.put("streetno", address.getStreetno());
				result.put("zipcode", address.getZipcode());
			} catch (JSONException e) {
				FamLog.exception(e, 201201091102l);
			}
		}
		return result;
	}

}

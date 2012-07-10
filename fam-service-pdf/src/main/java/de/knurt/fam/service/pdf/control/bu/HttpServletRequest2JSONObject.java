package de.knurt.fam.service.pdf.control.bu;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.service.pdf.control.ebc.BoardUnit;

/**
 * create a json object from a request using attribute "json".
 * 
 * @author Daniel Oltmanns
 * @since 1.4.0 (06/07/2011)
 */

class HttpServletRequest2JSONObject implements BoardUnit<HttpServletRequest, JSONObject> {

	@Override
	public JSONObject process(HttpServletRequest datum) {
		JSONObject result = new JSONObject();
		if (datum.getParameter("json") != null) {
			try {
				result = new JSONObject(datum.getParameter("json"));
			} catch (JSONException e) {
			}
		}
		return result;
	}

}

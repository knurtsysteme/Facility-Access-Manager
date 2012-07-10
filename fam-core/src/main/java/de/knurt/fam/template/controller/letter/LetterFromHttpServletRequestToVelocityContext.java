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
package de.knurt.fam.template.controller.letter;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.json.JSONStringer;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.heinzelmann.util.nebc.BoardUnit;

/**
 * create a velocity context to use with templates used for fam-service-pdf
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.4.0 (06/13/2011)
 * 
 */
public class LetterFromHttpServletRequestToVelocityContext implements BoardUnit<HttpServletRequest, VelocityContext> {

	/** {@inheritDoc} */
	@Override
	public VelocityContext process(HttpServletRequest datum) {
		VelocityContext context = new VelocityContext();
		for (Object key : datum.getParameterMap().keySet()) {
			String value = datum.getParameter(key.toString());
			context.put(key.toString(), this.getJSONEscaped(value));
		}
		context.put("customid", customid);
		context.put("config", FamConnector.getGlobalProperties());
		return context;
	}

	public LetterFromHttpServletRequestToVelocityContext(String customid) {
		this.customid = customid;
	}

	private String customid = "";

	private String getJSONEscaped(String value) {
		String result = "";
		try {
			result = new JSONStringer().object().key("a").value(value).endObject().toString();
			result = result.substring(6, result.length() - 2);
		} catch (JSONException exception) {
			FamLog.exception(exception, 201106131527l);
		}
		return result;
	}

}

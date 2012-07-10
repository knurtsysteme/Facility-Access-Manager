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

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.velocity.VelocityContext;
import org.json.JSONObject;

import de.knurt.heinzelmann.util.nebc.BoardUnit;

/**
 * create a post method to request the webservice.
 * 
 * @see fam-service-pdf
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.4.0 (06/13/2011)
 * 
 */
public class LetterFromHttpServletRequestToPostMethod implements BoardUnit<HttpServletRequest, PostMethod> {

	/** {@inheritDoc} */
    @Override
	public PostMethod process(HttpServletRequest datum) {
		VelocityContext context = new LetterFromHttpServletRequestToVelocityContext(customid).process(datum);
		JSONObject json = new LetterFromVelocityContextToJSONObject().process(context);
		return new FamServicePDFResolver().process(json);
	}

	public LetterFromHttpServletRequestToPostMethod(String customid) {
		this.customid = customid;
	}

	private String customid = "";

}

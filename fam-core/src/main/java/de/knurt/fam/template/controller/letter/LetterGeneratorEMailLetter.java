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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.template.model.TemplateResource;

/**
 * resolve a request for sending a letter via email. send pdf file and let
 * client know something about your success. :-)
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.4.0 (06/13/2011)
 * 
 */
public class LetterGeneratorEMailLetter {

	public ModelAndView process(HttpServletResponse response, TemplateResource tr) {
		// prepare result
		JSONObject result = new JSONObject();

		EMailLetterAdapter ema = new EMailLetterAdapter(tr);
		String customid = tr.getAuthUser().getUsername() + "-m";
		PostMethod post = new LetterFromHttpServletRequestToPostMethod(customid).process(tr.getRequest());
		String errormessage = ema.send(post, customid);

		try {

			if (errormessage.isEmpty()) {
				result.put("succ", true);

				InvoiceBookingResolver lgub = new InvoiceBookingResolver(tr);
				result.put("invoiced", lgub.invoice());
			} else {
				result.put("errormessage", errormessage);
				result.put("succ", false);
			}
		} catch (JSONException e) {
			FamLog.exception(e, 201106131728l);
		}

		// response answer
		PrintWriter pw = null;
		try {
			response.setHeader("Content-type", "application/json");
			pw = response.getWriter();
			IOUtils.write(result.toString(), pw);
		} catch (IOException ex) {
			FamLog.exception(ex, 201106131727l);
		} finally {
			IOUtils.closeQuietly(pw);
		}
		return null;
	}

}
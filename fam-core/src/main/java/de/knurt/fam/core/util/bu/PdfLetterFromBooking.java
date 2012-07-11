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
package de.knurt.fam.core.util.bu;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.generic.MathTool;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.content.text.AccessGlobalTemplate;
import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.heinzelmann.util.nebc.BoardUnit;

/**
 * produce content for letters.
 * 
 * @author Daniel Oltmanns
 * @since 1.4.0 (06/12/2010)
 */
public class PdfLetterFromBooking implements BoardUnit<Booking, JSONObject> {
	public PdfLetterFromBooking(User auth) {
		this.auth = auth;
	}

	private User auth = null;

	/** {@inheritDoc} */
	@Override
	public JSONObject process(Booking datum) {
		JSONObject result = new JSONObject();

		VelocityContext context = new VelocityContext();
		context.put("booking", datum);
		context.put("FamDateFormat", FamDateFormat.class);
		context.put("math", new MathTool());
		context.put("config", FamConnector.getGlobalProperties());
		// TODO @see #16 Whitspace between "EUR" and Price is missed
		NumberFormat euro = DecimalFormat.getCurrencyInstance(Locale.GERMANY);
		euro.setCurrency(Currency.getInstance("EUR"));
		context.put("euro", euro);
		context.put("authuser", this.auth);

		String letterAsJson = AccessGlobalTemplate.getInstance().getContent("custom/letter_booking.json", context);
		try {
			result = new JSONObject(letterAsJson);
		} catch (JSONException e) {
			FamLog.exception(e, 201106121711l);
		}
		return result;
	}

}

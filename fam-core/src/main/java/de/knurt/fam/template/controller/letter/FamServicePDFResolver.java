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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.json.JSONObject;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.heinzelmann.util.nebc.BoardUnit;
import de.knurt.heinzelmann.util.query.QueryStringFactory;

/**
 * resolve the fam-service-pdf
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.4.0 (06/13/2011)
 * 
 */
public class FamServicePDFResolver implements BoardUnit<JSONObject, PostMethod> {

	/** {@inheritDoc} */
    @Override
	@SuppressWarnings("deprecation") // TODO #11 kill uses of deprecations
	public PostMethod process(JSONObject datum) {

		// create connection
		Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
		Protocol.registerProtocol("https", easyhttps);

		// request configured fam-service-pdf
		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod(FamConnector.uri2service("pdf"));
		post.setQueryString(QueryStringFactory.get("json", datum.toString()).toString());

		// execute
		try {
			client.executeMethod(post);
		} catch (HttpException e) {
			FamLog.exception(e, 201106131407l);
		} catch (IOException e) {
			FamLog.exception(e, 201106131406l);
		}
		return post;
	}

}

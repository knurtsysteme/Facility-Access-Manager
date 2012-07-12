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
package de.knurt.fam.connector;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.heinzelmann.util.urlcontent.SimpleURLContent;

/**
 * compare this version with infos got from the official homepage of the
 * facility access manager and provide it.
 * 
 * @see InternalResourceViewResolver
 * @author Daniel Oltmanns
 * @since 1.8.0 (04/26/2012)
 */
public class FamSystemUpdateNotifier {
	
	private final static String UPDATE_URI = "http://facility-access-manager.com/update/";

	public static Properties currentVersion() {
		Properties result = new Properties();
		Boolean checkUpdate = FamConnector.getGlobalPropertyAsBoolean("system.check_update");
		if(checkUpdate == null || checkUpdate) {
			result.put("update_active", true);
			try {
				String jsonstr = SimpleURLContent.getInstance().getContent(UPDATE_URI);
				try {
					JSONObject json = new JSONObject(jsonstr);
					result.put("current_version", json.get("current_version"));
					result.put("is_current", json.get("current_version").equals(FamSystemMeta.CURRENT_VERSION));
					result.put("download_page", json.get("download_page"));
					result.put("error", false);
				} catch (JSONException e) {
					FamLog.info("got an invalid json answer from update url: " + jsonstr, 201204261037l);
				}
			} catch (MalformedURLException e) {
				FamLog.info("could not contact update url", 201204261036l);
				result.put("error", true);
			} catch (IOException e) {
				FamLog.info("could not contact update url", 201204261035l);
				result.put("error", true);
			}
		} else {
			result.put("update_active", false);
		}
		result.put("update_url", UPDATE_URI);
		return result;
	}

}

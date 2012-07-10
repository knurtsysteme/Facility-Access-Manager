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
package de.knurt.fam.template.controller.json;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.control.boardunits.JobDataProcessingFromRequest;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.document.JobDataProcessing;

/**
 * store a jobsurvey in the sessio of the user
 * 
 * @author Daniel Oltmanns
 * @since 1.7.0 (02/17/2012)
 */
public class JobSurveyPreviewSessionStoreController extends JSONController {

	public JobSurveyPreviewSessionStoreController(User user) {
		this.user = user;
	}

	private User user = null;

	/** {@inheritDoc} */
	@Override
	public JSONObject getJSONObject(HttpServletRequest rq, HttpServletResponse rs) {
		JSONObject result = new JSONObject();
		boolean succ = false;
		try {
			JobDataProcessing jdp = new JobDataProcessingFromRequest(user).process(rq);
			if (jdp != null) {
				this.user.getShoppingCart().addArticle(jdp);
				result.put("articleno", jdp.getArticleNumber());
				succ = true;
			}
			result.put("succ", succ);
		} catch (JSONException e) {
			FamLog.exception(e, 201202170822l);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		FamLog.exception(ex, 201202170821l);
	}

}

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

import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.document.Job;
import de.knurt.fam.core.persistence.dao.couchdb.CouchDBDao4Jobs;
import de.knurt.fam.core.util.mvc.RequestInterpreter;

/**
 * controller to get a job as json.
 * 
 * @author Daniel Oltmanns
 * @since 1.9.0 (09/15/2012)
 */
public class GetJobController extends JSONController {

	private User authUser;

	public GetJobController(User authUser) {
		this.authUser = authUser;
	}

	private enum KnownFlag {
		ID
	};

	/**
	 * return a specific job as json depending on flags
	 * <code>rq.getParameter("flag")</code>.
	 * 
	 * if flag is not set use flag <code>id</code>. if nothing found or on
	 * invalid requests return <code>{}</code>.
	 * 
	 * known flags by now as example queries:
	 * <ul>
	 * <li><code>?flag=id&id=234</code>: return the job step 0 with id 234</li>
	 * </ul>
	 * 
	 * @see RequestInterpreter#getTimeFrame(HttpServletRequest)
	 */
	@Override
	public JSONObject getJSONObject(HttpServletRequest rq, HttpServletResponse rs) {
		JSONObject result = new JSONObject();
		if (authUser != null) {
			switch (this.getFlag(rq)) {
			default: // ← KnownFlag.ID
				result = this.getJobWithID(rq);
				break;
			}
		}
		return result;
	}

	/**
	 * return the job of step 0 with the given id. only first step is supported
	 * yet
	 */
	private JSONObject getJobWithID(HttpServletRequest rq) {
		JSONObject result = new JSONObject();
		int id = -1;
		try {
			id = Integer.parseInt(rq.getParameter("id"));
			Job job = CouchDBDao4Jobs.me().getJob(id, 0);
			if (authUser.is(job.getUsername())) {
				result = new JSONObject(job.getJobSurvey());
			}
		} catch (Exception e) {
			// ignore npe, nfe or whatever - result stays empty
		}
		return result;
	}

	private KnownFlag getFlag(HttpServletRequest rq) {
		// ↘ only flag id is supported yet
		return KnownFlag.ID;
		/*
		 * ↘ use this code for other flags to come (e.g. a current job)
		 * KnownFlag result = KnownFlag.ID; String flag =
		 * rq.getParameter("flag"); if (flag != null) { if
		 * (flag.equalsIgnoreCase("current")) { result = KnownFlag.CURRENT; } }
		 * return result;
		 */
	}

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		FamLog.logException(this.getClass(), ex, "creating json fails", 200909160828l);
	}
}

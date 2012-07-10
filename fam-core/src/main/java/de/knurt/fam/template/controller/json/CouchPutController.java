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
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.jcouchdb.db.Response;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.control.persistence.dao.couchdb.FamCouchDBDao;
import de.knurt.fam.core.model.persist.User;

/**
 * get <code>body</code> as parameter from request and post it without any
 * validation into the database. of course, this is not allowed to everybody. by
 * now, only admins have the right to use this controller.
 * 
 * after handling the request, a response as json is returned.
 * 
 * this class is perfectly for use with javascript objects, collected in js
 * files.
 * 
 * @author Daniel Oltmanns
 * @since 1.20 (08/18/2010)
 */
public class CouchPutController implements Controller {

	public ModelAndView handleRequest(HttpServletRequest rq, HttpServletResponse rs) {
		PrintWriter pw = null;
		try {
			rs.setContentType("application/json");
			pw = rs.getWriter();
			String result = "{}";
			if (rq.getParameter("body") != null && this.isAllowedToShow(rq, rq.getParameter("body"))) {
				Response response = FamCouchDBDao.getInstance().put(rq.getParameter("body"));
				result = response != null ? response.getContentAsString() : "{error: \"unknown\"}";
			}
			IOUtils.write(result, pw);
		} catch (IOException ex) {
			FamLog.exception(ex, 201204191240l);
		} finally {
			IOUtils.closeQuietly(pw);
		}
		return null;
	}

	private boolean isAllowedToShow(HttpServletRequest rq, String doc) {
		User user = SessionAuth.user(rq);
		return user != null && user.isAdmin();
	}
}

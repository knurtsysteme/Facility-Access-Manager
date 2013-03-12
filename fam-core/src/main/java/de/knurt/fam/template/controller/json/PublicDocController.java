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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.couchdb.FamCouchDBDao;

/**
 * return a document with given id out of couchdb. check, if that is allowed and
 * if not response {}! by now, this is only allowed by admins.
 * 
 * @author Daniel Oltmanns
 * @since 1.20 (08/16/2010)
 */
public class PublicDocController implements Controller {

	@Override
  public ModelAndView handleRequest(HttpServletRequest rq, HttpServletResponse rs) {
		PrintWriter pw = null;
		try {
			rs.setContentType("application/json");
			pw = rs.getWriter();
			String result = "{}";
			if (rq.getParameter("doc") != null && this.isAllowedToShow(rq, rq.getParameter("doc"))) {
				result = FamCouchDBDao.getInstance().getContentAsString(rq.getParameter("doc"));
			}
			IOUtils.write(result, pw);
		} catch (IOException ex) {
			FamLog.exception(ex, 201204191241l);
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

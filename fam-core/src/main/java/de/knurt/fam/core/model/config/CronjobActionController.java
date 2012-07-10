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
package de.knurt.fam.core.model.config;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.core.aspects.logging.FamLog;

/**
 * this is the controller for the cronjob.
 * 
 * check out, which action has to be done now and do that.
 * 
 * ask the database for possible actions and delegate it to a
 * {@link CronjobAction}.
 * 
 * for this, make sure, your server contacts the following address every 5
 * minutes:
 * 
 * http://www.yourdomain.com/fam-core/cronjob.html
 * 
 * This page is a pseude page because not responding html. This controller could
 * be a stand alone jar file as well, but needs the same connection to the
 * database.
 * 
 * So please create a crontab:
 * 
 * <code>
 * [root@localhost ~]# crontab -l
 * *\/5 * * * * wget -S http://localhost/fam-core/cronjob.html --no-cookies --no-cache -O -
 * </code>
 * 
 * see <code>man wget</code> for further information.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090913
 */
public class CronjobActionController {

	/**
	 * a pseudo model and view, that does nothing. this controller is requested
	 * from a cronjob, that does not need a response. however, if you are
	 * calling it in the browser, you got the messages logged. before this
	 * pseude response, delegate all cronjob actions injected to the
	 * {@link CronjobActionContainer}.
	 * 
	 * @param rq
	 *            request got
	 * @param rs
	 *            response give
	 * @return a pseude response
	 */
	public ModelAndView handleRequest(HttpServletRequest rq, HttpServletResponse rs) {
		CronjobActionContainer.getInstance().resolveAll();
		return this.pseudoResponse(rs);
	}

	private ModelAndView pseudoResponse(HttpServletResponse rs) {
		PrintWriter pw = null;
		try {
			rs.setContentType("text/plain;charset=UTF-8");
			pw = rs.getWriter();
			IOUtils.write("done", pw);
		} catch (IOException ex) {
			FamLog.exception(ex, 200911182012l);
		} finally {
			IOUtils.closeQuietly(pw);
		}
		return null;
	}
}

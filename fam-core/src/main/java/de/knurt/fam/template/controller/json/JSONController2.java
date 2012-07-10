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
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;

/**
 * a new version of {@link JSONController} for easier testing and Spring3
 * 
 * @author Daniel Oltmanns
 * @since 0.20120306 (03/06/2012)
 */
public abstract class JSONController2 {

	/**
	 * print out json with the key and values got from
	 * {@link JSONController2#getKeyAndValues(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
	 * 
	 * @param rq
	 *            request
	 * @param rs
	 *            response
	 * @return null
	 */
	public ModelAndView handleRequest(HttpServletRequest rq, HttpServletResponse rs) {
		PrintWriter pw = null;
		try {
			rs.setHeader("Content-type", "application/json");
			pw = rs.getWriter();
			IOUtils.write(this.getJSONObject().toString(), pw);
		} catch (IOException ex) {
			this.onIOException(ex);
		} finally {
			IOUtils.closeQuietly(pw);
		}
		return null;
	}

	/**
	 * return {@link Properties} representing json.
	 * 
	 * @param rq
	 *            request
	 * @param rs
	 *            response
	 * @return {@link Properties} representing json.
	 */
	public abstract JSONObject getJSONObject();

	/**
	 * handle {@link IOException} if thrown
	 * 
	 * @param exception
	 *            thrown
	 */
	public abstract void onIOException(IOException ex);
}
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

import org.apache.commons.dbcp.BasicDataSource;

/**
 * provide a {@link BasicDataSource} and setting the values as configured in
 * global configuration properties file.
 * 
 * @author Daniel Oltmanns
 * @since 1.6.0 (11/14/2011)
 */
public class BasicDataSourceFactory {

	private BasicDataSource bds = null;

	/**
	 * create a basic data source. if the url has not a autoReconnect=true.
	 */
	public BasicDataSourceFactory() {
		super();
		this.bds = new BasicDataSource();
		this.bds.setDriverClassName(FamConnector.getGlobalProperty("sql_driver_class_name"));
		String url = FamConnector.getGlobalProperty("sql_url");
		String autoReconnect = "autoReconnect=true";
		if (url.matches(".*" + autoReconnect + ".*") == false) {
			if (url.matches(".+\\?.+")) {
				// there is already a query
				url += "&" + autoReconnect;
			} else {
				// no query so far
				url += "?" + autoReconnect;
			}
		}
		this.bds.setUrl(url);
		this.bds.setUsername(FamConnector.getGlobalProperty("sql_username"));
		this.bds.setPassword(FamConnector.getGlobalProperty("sql_password"));
	}

	public BasicDataSource getBds() {
		return bds;
	}
}

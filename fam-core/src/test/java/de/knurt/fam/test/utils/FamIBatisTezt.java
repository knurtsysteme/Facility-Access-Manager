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
package de.knurt.fam.test.utils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import org.jcouchdb.db.Database;
import org.jcouchdb.db.Response;
import org.jcouchdb.db.ServerImpl;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.persistence.dao.ibatis.FamSqlMapClientDaoSupport;

/**
 * test the system as it is configured with a relative database using ibatis.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090829
 */
public class FamIBatisTezt extends IBatisTezt {

	/**
	 * 
	 * @return
	 */
	@Override
	public String[] getTablesToDeleteAfterTest() {
		String[] result = { "address", "booking", "logbookentry", "facility_responsibility", "facility_availability", "usermail", "user", "contactdetail", "key_value" };
		return result;
	}

	public FamIBatisTezt() {
		super();
		testConnection = new Properties();
		testConnection.setProperty("driverClassName", FamConnector.getGlobalProperty("sql_driver_class_name"));
		testConnection.setProperty("url", FamConnector.getGlobalProperty("sql_url"));
		testConnection.setProperty("username", FamConnector.getGlobalProperty("sql_username"));
		testConnection.setProperty("password", FamConnector.getGlobalProperty("sql_password"));
		this.clearDatabase();
	}

	private Properties testConnection = null;

	/**
	 * @return
	 */
	@Override
	public Properties getTestConnection() {
		return testConnection;
	}

	/**
	 * like {@link IBatisTezt#clearDatabase()} but delete couchdb documents that
	 * are deleteable as well
	 * 
	 * @param with_couchdb
	 */
	@SuppressWarnings("unchecked")
	public synchronized void clearCouchDB() {
		String host = FamConnector.getGlobalProperty("couchdb_ip");
		int port = Integer.parseInt(FamConnector.getGlobalProperty("couchdb_port"));
		String name = FamConnector.getGlobalProperty("couchdb_dbname");
		Database db = new Database(new ServerImpl(host, port), name);

		String uri = "_all_docs";
		Response response = db.getServer().get("/" + FamConnector.getGlobalProperty("couchdb_dbname") + "/" + uri);
		try {
			Map docs = response.getContentAsMap();
			ArrayList<Map> aa = (ArrayList<Map>) docs.get("rows");
			for (Map key : aa) {
				try {
					db.delete(key.get("id").toString(), ((Map) key.get("value")).get("rev").toString());
				} catch (Exception e) {
				}
			}
		} catch (ClassCastException e) {
			FamLog.exception(e, 201205031134l);
		} finally {
			if (response != null) {
				response.destroy();
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public SqlMapClientDaoSupport getSqlMapClientDaoSupport() {
		return FamSqlMapClientDaoSupport.getInstance();
	}
}

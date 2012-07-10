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
package de.knurt.fam.template.model;

import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.web.servlet.support.RequestContextUtils;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.aspects.security.encoder.FamEncoder;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.config.RoleConfigDao;
import de.knurt.fam.core.control.persistence.dao.couchdb.FamCouchDBDao;
import de.knurt.fam.core.model.config.CronjobAction;
import de.knurt.fam.core.model.config.CronjobActionContainer;
import de.knurt.fam.core.model.persist.KeyValue;
import de.knurt.heinzelmann.util.text.DurationAdapter;

/**
 * produce the model for terms of use pages
 * 
 * @see TemplateContentProperties#getTemplateModel(TemplateResource)
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/19/2010)
 */
public class ViewSystemConfigurationModelFactory {

	private DurationAdapter durationAdapter = new DurationAdapter(DurationAdapter.SupportedLanguage.ENGLISH);

	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		result.put("configuration_reloaded", templateResource.configurationReloadIsRequested());
		Properties props = new Properties();
		props.put("config_dir", FamConnector.getConfigDirectory());
		props.putAll(FamConnector.getGlobalProperties());
		List<KeyValue> kvs = FamDaoProxy.keyValueDao().getAll();
		for (KeyValue kv : kvs) {
			props.put("kv_" + kv.key(), kv.value());
		}
		props.put("dao_booking_class", FamDaoProxy.bookingDao().getClass());
		props.put("dao_facility_availability_class", FamDaoProxy.facilityDao().getClass());
		props.put("dao_logbook_class", FamDaoProxy.logbookEntryDao().getClass());
		props.put("dao_user_class", FamDaoProxy.userDao().getClass());
		props.put("dao_kv_class", FamDaoProxy.keyValueDao().getClass());
		props.put("role_standard", RoleConfigDao.getInstance().getStandardId());
		props.put("role_admin", RoleConfigDao.getInstance().getAdminId());
		props.put("authentication_module", FamAuth.me().getUserAuthentication().getClass());

		// put in cronjobs
		List<CronjobAction> cronjobs = CronjobActionContainer.getInstance().getAllCronjobActions();
		int i = 0;
		for (CronjobAction cronjob : cronjobs) {
			props.put(++i + "_cronjob_desc", cronjob.getDescription());
			props.put(i + "_cronjob_class", cronjob.getClass());
			props.put(i + "_cronjob_every", durationAdapter.getText(cronjob.resolveEvery()));
		}

		try {
			BasicDataSource bds = ((BasicDataSource) RequestContextUtils.getWebApplicationContext(templateResource.getRequest()).getBean("dataSource"));
			props.put("sql_url", bds.getUrl());
			props.put("sql_username", bds.getUsername());
		} catch (Exception e) {
			props.put("sql_ERROR", e.getMessage());
		}

		props.put("password_encoding_algorithm", FamEncoder.getInstance().getEncoder().getAlgorithm());

		boolean couchdb_test_query_succ = false;
		try {
			props.put("couchdb_real_name", FamCouchDBDao.getInstance().databaseName());
			props.put("couchdb_document_count", FamCouchDBDao.getInstance().documentCount());
			couchdb_test_query_succ = true;
		} catch (Exception e) {
		}
		props.put("couchdb_test_query_succ", couchdb_test_query_succ);
		result.put("props", props);
		return result;
	}

}

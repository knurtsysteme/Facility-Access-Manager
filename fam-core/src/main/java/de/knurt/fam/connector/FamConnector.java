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

import java.util.Properties;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import de.knurt.fam.core.aspects.logging.FamLog;

/**
 * central class to access the fam configuration, template and everything
 * defined in user config files.
 * 
 * @author Daniel Oltmanns
 * @since 1.21 (09/27/2010)
 */
public class FamConnector {

	/** one and only instance of FamConnector */
	private volatile static FamConnector me;

	public static String templateDirectory() {
		return getConfigDirectory() + System.getProperty("file.separator") + "template" + System.getProperty("file.separator");
	}

	public static boolean isPreviewSystem() {
		return me().config.isPreview();
	}

	public static boolean isDev() {
		return me().config.isDev();
	}

	private final FamConfig config;

	/** construct FamConnector */
	private FamConnector() {
	  ClassPathResource resource = new ClassPathResource("test-dependencies.xml");
	  if(resource.exists() == false) {
	    resource = new ClassPathResource("dependencies.xml");
	  }
		this.config = new XmlBeanFactory(resource).getBean("config", FamConfig.class);
	}

	/**
	 * return the one and only instance of FamConnector
	 * 
	 * @return the one and only instance of FamConnector
	 */
	public static FamConnector getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (FamConnector.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new FamConnector();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of FamConnector
	 */
	public static FamConnector me() {
		return getInstance();
	}

	public static String baseUrlPublic() {
		return me().getConfigProperty("base_url_public", true);
	}

	private static String getWithTrailingFileSeparator(String rawUrl) {
		if (!rawUrl.matches(".+" + System.getProperty("file.separator") + "$")) {
			rawUrl += System.getProperty("file.separator");
		}
		return rawUrl;
	}

	public static String baseUrlProtected() {
		return me().getConfigProperty("base_url_protected", true);
	}

	public static String baseUrlAdmin() {
		return me().getConfigProperty("base_url_admin", true);
	}

	public static String baseUrlContent() {
		return me().getConfigProperty("base_url_content", true);
	}

	public String getPluginDirectory() {
		return getConfigDirectory() + System.getProperty("file.separator") + "plugins" + System.getProperty("file.separator");
	}

	public static String baseUrlDoc() {
		return me().getConfigProperty("base_url_doc", true);
	}

	public static String fileExchangeDir() {
		return getConfigDirectory() + System.getProperty("file.separator") + "files" + System.getProperty("file.separator");
	}

	public String getConfigProperty(String key) {
		return getConfigProperty(key, false);
	}

	public String getConfigProperty(String key, boolean withTrailingFileSeparator) {
		String result = config.getGlobalProperty(key);
		if (withTrailingFileSeparator) {
			result = getWithTrailingFileSeparator(result);
		}
		return result;
	}

	public static String uri2service(String id) {
		return me().getConfigProperty("service_" + id + "__url", false);
	}

	/**
	 * return the directory, the files, templates, plugins and custom
	 * configuration is in. as default, this is <code>/opt/knurt/fam/</code>
	 * <span style="font-weight: bold;">and not</span>
	 * <code>/opt/knurt/fam/config/</code>.
	 * 
	 * @return the directory, the files, templates, plugins and custom
	 *         configuration is in.
	 */
	public static String getConfigDirectory() {
		return getWithTrailingFileSeparator(me().config.getConfigDirectory());
	}

	public static Properties getGlobalProperties() {
		return me().config.getGlobalProperties();
	}

	public static String getGlobalProperty(String key) {
		return getGlobalProperties().getProperty(key);
	}

	/**
	 * return a list of comma separated values for the given key. if key is not
	 * set, return an String array with 0 elements.
	 * 
	 * @param key
	 *            of the property comma separated values are defined under.
	 * @return a list of comma separated values
	 */
	public static String[] getGlobalPropertyAsList(String key) {
		try {
			return getGlobalProperties().getProperty(key).split(",");
		} catch (Exception e) {
			FamLog.exception("nothing defined for: " + key, e, 201204241519l);
			return new String[] {};
		}
	}

	/**
	 * return true, if property value is "true" or "1". return false in any
	 * other case.
	 * 
	 * @param key
	 *            of the property
	 * @return true, if property value is "true" or "1". return false in any
	 *         other case.
	 */
	public static boolean getGlobalPropertyAsBoolean(String key) {
		String value = getGlobalProperty(key);
		return value != null && (value.equals("true") || value.equals("1"));
	}

	/**
	 * return true if a sql database is configured. on public servers it might
	 * be not configured. a database is configured if the value for sql_url is
	 * longer then "sql://a".
	 * 
	 * @return true if a sql database is configured
	 */
	public static boolean sqlConfigured() {
		return getGlobalProperty("sql_url").length() > "sql://a".length();
	}

}

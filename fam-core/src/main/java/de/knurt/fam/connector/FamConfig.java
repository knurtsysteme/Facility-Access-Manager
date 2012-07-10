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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.PropertyResourceBundle;

import de.knurt.fam.core.aspects.logging.FamLog;

/**
 * container for all config vars
 * 
 * @author Daniel Oltmanns
 * @since 1.21 (09/29/2010)
 */
class FamConfig {

	private Properties globalProperties;
	private String configDirectory = null;

	protected FamConfig() {
		this("/opt/knurt/fam/");
	}

	protected String getConfigDirectory() {
		return configDirectory;
	}

	protected FamConfig(String configDirectory) {
		FamLog.info("use config dir: " + configDirectory, 201111061912l);
		assert configDirectory != null;
		assert this.globalProperties == null;
		this.configDirectory = configDirectory;
		if (this.globalProperties == null) {
			this.globalProperties = new Properties();
			try {
				PropertyResourceBundle p = new PropertyResourceBundle(new FileInputStream(configDirectory + System.getProperty("file.separator") + "config" + System.getProperty("file.separator") + "fam_global.conf"));
				for (String key : p.keySet()) {
					this.globalProperties.put(key, p.getString(key));
				}
			} catch (IOException ex) {
				FamLog.exception(ex, 201111041300l);
			}
		}
	}

	protected String getGlobalProperty(String key) {
		return this.globalProperties.getProperty(key);
	}

	protected Properties getGlobalProperties() {
		return globalProperties;
	}

	/**
	 * Return true, if this is the development environment.
	 * 
	 * @return true, if this is the development environment.
	 */
	protected final boolean isDev() {
		return this.propertyValueEquals("env_dev", "true");
	}

	private boolean propertyValueEquals(String key, String value) {
		return this.getGlobalProperty(key) != null && this.getGlobalProperty(key).equalsIgnoreCase(value);
	}

	protected boolean isPreview() {
		return this.propertyValueEquals("env_preview", "true");
	}
}

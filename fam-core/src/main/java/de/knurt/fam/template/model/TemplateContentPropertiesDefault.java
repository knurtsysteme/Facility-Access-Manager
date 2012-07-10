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

import java.io.File;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;

/**
 * produce content properties as a default. override this for individual
 * templates or implement your own.
 * 
 * @author Daniel Oltmanns
 * @since 1.21 (09/30/2010)
 */
public class TemplateContentPropertiesDefault implements TemplateContentProperties {

	/** one and only instance of TemplateContentPropertiesDefault */
	private volatile static TemplateContentPropertiesDefault me;

	/** construct TemplateContentPropertiesDefault */
	private TemplateContentPropertiesDefault() {
		this.reload();
	}

	/**
	 * return the one and only instance of TemplateContentPropertiesDefault
	 * 
	 * @return the one and only instance of TemplateContentPropertiesDefault
	 */
	public static TemplateContentPropertiesDefault getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (TemplateContentPropertiesDefault.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new TemplateContentPropertiesDefault();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of TemplateContentPropertiesDefault
	 */
	public static TemplateContentPropertiesDefault me() {
		return getInstance();
	}

	private Element getRootElementOf(String file) {
		SAXBuilder builder;
		Document root = null;

		try {
			builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser");
			root = builder.build(FamConnector.templateDirectory() + file);
		} catch (Exception ex) {
			FamLog.exception(ex, 201204141010l);
		}
		return root.getRootElement();
	}

	private Element customConfig, customLanguage;

	/** {@inheritDoc} */
	@Override
	public Element getCustomConfig() {
		return customConfig;
	}

	/** {@inheritDoc} */
	@Override
	public Element getCustomLanguage() {
		return customLanguage;
	}

	/** {@inheritDoc} */
	@Override
	public Element getCustomLanguagePage(String resourceName) {
		return this.getPage(customLanguage, resourceName);
	}

	/** {@inheritDoc} */
	@Override
	public Element getCustomConfigPage(String resourceName) {
		return this.getPage(customConfig, resourceName);
	}

	private Element getPage(Element root, String resourceName) {
		Element result = null;
		for (Object candidate : root.getChild("pages").getChildren("page")) {
			if (((Element) candidate).getAttributeValue("name").equals(resourceName)) {
				result = (Element) candidate;
				break;
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void reload() {
		this.customConfig = this.getRootElementOf("custom" + File.separator + "config.xml");
		this.customLanguage = this.getRootElementOf("custom" + File.separator + "language.xml");
	}

}

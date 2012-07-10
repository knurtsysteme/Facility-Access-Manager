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
package de.knurt.fam.template.util;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.template.controller.AnswerFactory;
import de.knurt.fam.template.controller.ResourceController;
import de.knurt.fam.template.model.TemplateContentProperties;
import de.knurt.fam.template.parser.ContentFactory;

/**
 * configurations about the contents
 * 
 * @author Daniel Oltmanns
 * @since 1.21 (09/28/2010)
 */
public class TemplateConfig {

	private ContentFactory contentFactory = null;
	private AnswerFactory answerFactory = null;
	private ResourceController resourceController = null;
	private TemplateContentProperties contentProperties = null;
	private Properties utilities = null;

	@Required
	public void setContentProperties(TemplateContentProperties contentProperties) {
		this.contentProperties = contentProperties;
	}

	public TemplateContentProperties getContentProperties() {
		return contentProperties;
	}

	@Required
	public void setAnswerFactory(AnswerFactory answerFactory) {
		this.answerFactory = answerFactory;
	}

	@Required
	public void setResourceController(ResourceController resourceController) {
		this.resourceController = resourceController;
	}

	public ResourceController getResourceController() {
		return resourceController;
	}

	public AnswerFactory getAnswerFactory() {
		return answerFactory;
	}

	@Required
	public void setContentFactory(ContentFactory contentFactory) {
		this.contentFactory = contentFactory;
	}

	public ContentFactory getContentFactory() {
		return contentFactory;
	}

	/** one and only instance of TemplateConfig */
	private volatile static TemplateConfig me;

	/** construct TemplateConfig */
	private TemplateConfig() {
		this.utilities = new Properties();
		this.utilities.put("html", TemplateHtml.me());
		this.utilities.put("page", PageUtil.me());
		this.utilities.put("sort", HtmlTableSortationUtil.me());
		this.utilities.put("value", ValueUtil.me());
	}

	/**
	 * return the one and only instance of TemplateConfig
	 * 
	 * @return the one and only instance of TemplateConfig
	 */
	public static TemplateConfig getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (TemplateConfig.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new TemplateConfig();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of TemplateConfig
	 */
	public static TemplateConfig me() {
		return getInstance();
	}

	/**
	 * different utilities to use with your templates:
	 * <ul>
	 * <li><code>html</code>: {@link TemplateHtml}</li>
	 * <li><code>page</code>: {@link PageUtil}</li>
	 * <li><code>sort</code>: {@link HtmlTableSortationUtil}</li>
	 * <li><code>value</code>: {@link ValueUtil}</li>
	 * </ul>
	 */
	public Properties getUtilities() {
		return this.utilities;
	}

}

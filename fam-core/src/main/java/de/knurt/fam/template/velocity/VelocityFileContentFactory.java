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
package de.knurt.fam.template.velocity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.jdom.Element;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.connector.FamSystemMeta;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.content.text.FamText;
import de.knurt.fam.template.model.TemplateModelFactory;
import de.knurt.fam.template.model.TemplateResource;
import de.knurt.fam.template.parser.ContentFactory;
import de.knurt.fam.template.util.TemplateConfig;

/**
 * produce contents
 * 
 * @author Daniel Oltmanns
 * @since 1.21 (09/28/2010)
 */
public class VelocityFileContentFactory implements ContentFactory {

	/** one and only instance of TemplateConfig */
	private volatile static VelocityFileContentFactory me;

	/** construct TemplateConfig */
	private VelocityFileContentFactory() {
		String templateDirectory = FamConnector.templateDirectory();
		Properties props = new Properties();
		props.put(Velocity.INPUT_ENCODING, "UTF-8");
		props.put("file.resource.loader.path", templateDirectory);
		props.put("userdirective", "com.googlecode.htmlcompressor.velocity.HtmlCompressorDirective");
		try {
			Velocity.init(props);
		} catch (Exception e) {
			FamLog.exception(e, 201104201301l);
		}
	}

	/**
	 * return the one and only instance of TemplateConfig
	 * 
	 * @return the one and only instance of TemplateConfig
	 */
	public static VelocityFileContentFactory getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (VelocityFileContentFactory.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new VelocityFileContentFactory();
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
	public static VelocityFileContentFactory me() {
		return getInstance();
	}

	private VelocityContext getVelocityContext(TemplateResource templateResource) {
		VelocityContext result = new VelocityContext();

		Properties config = FamConnector.getGlobalProperties();
		config.put("version", FamSystemMeta.ACTUAL_VERSION);
		config.put("deploydate", FamSystemMeta.getDateDeployed());
		config.put("preview", FamSystemMeta.isPreview());
		if (templateResource.isRequestForContent()) {
			Element configXml = TemplateConfig.me().getContentProperties().getCustomConfig();
			if (configXml != null) {
				Properties configXmlProps = new Properties();
				configXmlProps.put("xml", configXml);
				try {
					configXmlProps.put("page", TemplateConfig.me().getContentProperties().getCustomConfigPage(templateResource.getName()));
				} catch (Exception e) {
					FamLog.exception(templateResource.getName(), e, 201011041432l);
				}
				try {
					configXmlProps.put("model", new TemplateModelFactory(templateResource).getProperties());
				} catch (Exception e) {
					FamLog.exception(templateResource.getName(), e, 201011041433l);
				}
				configXmlProps.put("resource", templateResource);
				config.putAll(configXmlProps);
			}
			try {
				config.put("user", templateResource.getAuthUser() == null ? false : templateResource.getAuthUser());
			} catch (Exception e) {
				FamLog.exception(templateResource.getName(), e, 201011041434l);
			}

			Properties lang = new Properties();
			Element langXml = TemplateConfig.me().getContentProperties().getCustomLanguage();
			if (langXml != null) {
				Properties langXmlProps = new Properties();
				langXmlProps.put("xml", langXml);
				try {
					langXmlProps.put("page", TemplateConfig.me().getContentProperties().getCustomLanguagePage(templateResource.getName()));
				} catch (Exception e) {
					FamLog.exception(templateResource.getName(), e, 201011041435l);
				}
				try {
					lang.putAll(langXmlProps);
				} catch (Exception e) {
					FamLog.exception(templateResource.getName(), e, 201011041436l);
				}
			}

			result.put("lang", lang);
			result.put("visibility", templateResource.getVisibility().toString().toLowerCase());
		}
		result.put("config", config);
		result.put("util", TemplateConfig.me().getUtilities());
		result.put("resource_name", templateResource.getName());
		// some statics
		result.put("FamDateFormat", FamDateFormat.class);
		result.put("Math", Math.class);
		result.put("FamText", FamText.class);
		return result;
	}

	public String getTemplateFile(TemplateResource templateResource) {
		String result = "";
		boolean found = false;
		if (templateResource.getSuffix().equals("html")) {
			result = templateResource.getTemplateFile(); // answer relative to
			found = result != null && !result.isEmpty();
		} else if (templateResource.getSuffix().equals("js") || templateResource.getSuffix().equals("css")) {
			String candidate = templateResource.getSuffix().equals("js") ? "scripts" : "styles";
			candidate += File.separator + templateResource.getName() + "." + templateResource.getSuffix();
			found = new File(FamConnector.templateDirectory() + candidate).exists();
			if (found) {
				result = candidate;
			}
		}
		if (!found) {
			Properties p = new Properties();
			try {
				p.load(new FileInputStream(String.format("%smapping.properties", FamConnector.templateDirectory())));
				result = p.getProperty(String.format("%s.%s", templateResource.getSuffix(), templateResource.getName()));
			} catch (IOException e) {
				FamLog.logException(this.getClass(), e, "could not get " + String.format("%smapping.properties", FamConnector.templateDirectory()), 201010071656l);
			}
		}
		return result;
	}

	public String getContent(TemplateResource templateResource) {
		String result = null;
		VelocityContext context = null;
		Template template = null;
		Writer writer = null;
		try {
			context = this.getVelocityContext(templateResource);
			template = Velocity.getTemplate(this.getTemplateFile(templateResource));
			writer = new StringWriter();
			template.merge(context, writer);
			result = writer.toString();
		} catch (ResourceNotFoundException e) {
			FamLog.logException(this.getClass(), e, "no resource found", 201010071653l);
		} catch (ParseErrorException e) {
			FamLog.logException(this.getClass(), e, "could not parse it", 201010071655l);
		} catch (Exception e) {
			FamLog.logException(this.getClass(), e, "unknown: " + templateResource + " | " + context + " | " + template + " | " + (templateResource != null ? templateResource.getTemplateFile() + " | " + this.getTemplateFile(templateResource) : "") + " | " + FamConnector.templateDirectory(),
					201010071654l);
		}
		return result;
	}
}

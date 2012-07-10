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

import java.net.URLEncoder;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.validator.UrlValidator;
import org.jdom.Element;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.template.model.TemplateContentPropertiesDefault;
import de.knurt.fam.template.model.TemplatePage;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.ui.html.Text2Html;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * produce html from content properties.
 * 
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/03/2010)
 */
public class TemplateHtml {

	/** one and only instance of TemplateHtml */
	private volatile static TemplateHtml me;
	private UrlValidator urlValidator;

	/** construct TemplateHtml */
	private TemplateHtml() {
		String[] schemes = { "http", "https" };
		urlValidator = new UrlValidator(schemes, UrlValidator.ALLOW_ALL_SCHEMES);
	}

	/**
	 * return the one and only instance of TemplateHtml
	 * 
	 * @return the one and only instance of TemplateHtml
	 */
	public static TemplateHtml getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (TemplateHtml.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new TemplateHtml();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of TemplateHtml
	 */
	public static TemplateHtml me() {
		return getInstance();
	}

	/**
	 * shortcut in velocity. write
	 * 
	 * <pre>
	 * $util.html.a('imprint', $lang.xml, $config.xml)
	 * </pre>
	 * 
	 * instead of
	 * 
	 * <pre>
	 * <a href="$config.base_url_public$lang.xml.getChild('pages').getChild('imprint').getChildText('href')" title="$lang.xml.getChild('pages').getChild('imprint').getChildText('title')">$lang.xml.getChild('pages').getChild('imprint').getChildText('headline')</a>
	 * </pre>
	 * 
	 * @param hrefBase
	 * @param resourceName
	 * @return
	 */
	public HtmlElement a(String resourceName, Element langXmlRoot, Element configXmlRoot) {
		Element page = TemplateContentPropertiesDefault.me().getCustomLanguagePage(resourceName);
		if (page == null) {
			return this.getNotFound(resourceName);
		} else {
			return this.a(resourceName, page.getChildText("headline"), langXmlRoot, configXmlRoot);
		}
	}

	public String formatUserInput(String string) {
		String result = Text2Html.me().linkUrls(urlValidator, string, "_parent");
		result = Text2Html.me().nl2br(result);
		return result;
	}

	private HtmlElement getNotFound(String resourceName) {
		return HtmlFactory.get("span", String.format("page <code>%s</code> is not configured (correctly)", resourceName)).cla("warning");
	}

	public HtmlElement a(String resourceName) {
		Element page = TemplateContentPropertiesDefault.me().getCustomLanguagePage(resourceName);
		String href = this.getHref(resourceName);
		if (href != null) {
			return this.a(href, page.getChildText("headline"), page.getChildText("title"));
		} else {
			return this.getNotFound(resourceName);
		}
	}
	public HtmlElement aWithAQuery(String resourceName, String query) {
		Element page = TemplateContentPropertiesDefault.me().getCustomLanguagePage(resourceName);
		String href = this.getHref(resourceName);
		if (href != null) {
			return this.a(href + query, page.getChildText("headline"), page.getChildText("title"));
		} else {
			return this.getNotFound(resourceName);
		}
	}
	public String hrefWithAQuery(String resourceName, String query) {
		return href(resourceName) + query;
	}

	public String getEntities(String raw) {
		return StringEscapeUtils.escapeHtml(raw);
	}

	public HtmlElement a(String resourceName, String content, Element langXmlRoot, Element configXmlRoot) {
		Element page = TemplateContentPropertiesDefault.me().getCustomLanguagePage(resourceName);
		String href = this.getHref(resourceName, page, configXmlRoot);
		if (href != null) {
			return this.a(href, content, page.getChildText("title"));
		} else {
			return this.getNotFound(resourceName);
		}
	}

	private HtmlElement a(String href, String content, String title) {
		return HtmlFactory.get_a(href, content).title(title);
	}

	public String getHref(String resourceName, Element langPage, Element configXmlRoot) {
		assert langPage.getName().equals("page");
		String result = null;
		String hrefBase = this.getHrefBase(resourceName, configXmlRoot);
		if (!hrefBase.isEmpty()) {
			result = hrefBase;
			if (langPage.getChild("href") == null) {
				String tmphref = langPage.getChildText("title").toLowerCase().replaceAll(" ", "-").replaceAll(",", "").replaceAll("\\?", "").replaceAll("!", "");
				try {
					result += String.format("%s-%s.html", URLEncoder.encode(tmphref, "UTF-8"), resourceName);
				} catch (Exception e) {
					result += tmphref;
				}
			} else {
				result += langPage.getChildText("href");
			}
		}
		return result;
	}

	private String getHrefBase(String resourceName, Element configXmlRoot) {
		String result = "";
		Element page = TemplateContentPropertiesDefault.me().getCustomConfigPage(resourceName);
		if (page != null) {
			if (page.getAttributeValue("visibility").equals("public")) {
				result = FamConnector.baseUrlPublic();
			} else if (page.getAttributeValue("visibility").equals("protected")) {
				result = FamConnector.baseUrlProtected();

			} else if (page.getAttributeValue("visibility").equals("admin")) {
				result = FamConnector.baseUrlAdmin();
			}
		}
		return result;
	}

	public String getHref(String resourceName) {
		return this.getHref(resourceName, TemplateContentPropertiesDefault.me().getCustomLanguagePage(resourceName), TemplateContentPropertiesDefault.me().getCustomConfig());
	}

	public String getHref(TemplatePage templatePage) {
		return this.getHref(templatePage.getResourceName());
	}

	public static String href(String resourceName) {
		return me().getHref(resourceName);
	}

	/**
	 * return a url like {@link #getHref(String)} but as relative path. this is
	 * only needed in very specific cases where a reverse proxy is working on a
	 * different port then the application and does not map it right.
	 */
	public String getRelativeHref(String resourceName) {
		String result = this.getHref(resourceName);
		return result.replaceFirst("http(s){0,1}:\\/\\/[^\\/]+\\/", "/");
	}

	public String getHref(String string, QueryString qs) {
		return this.getHref(string) + qs.getAsHtmlLinkHref(false);
	}

}

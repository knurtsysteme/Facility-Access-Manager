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
package de.knurt.fam.test.unit.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.template.model.TemplateContentProperties;
import de.knurt.fam.template.util.TemplateConfig;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class TemplateConfigTest {

	private String[] mustPageNames = { "home", "corehome", "adminhome", "login", "register", "registersent", "termsofuse", "users", "plugins", "systembookings", "systemlistofusermails", "systemlistofrolesandrights", "contactdetails", "systemlistofconfiguredfacilities", "editfeedback",
			"forgottenpassword", "setnewpassword", "unknownerror", "book", "book2", "bookfacilitiesdone", "systemfacilityavailability", "jobsmanager", "mybookings", "filemanager", "jobsurvey", "configjobsurvey" };

	@SuppressWarnings("unchecked")
	@Test
	public void xmlPagesDefined() {
		TemplateContentProperties tcp = TemplateConfig.me().getContentProperties();
		Element langRoot = tcp.getCustomLanguage();
		Element configRoot = tcp.getCustomConfig();
		assertNotNull(configRoot.getChild("pages"));
		assertNotNull(langRoot.getChild("pages"));
		assertNotNull(configRoot.getChild("pages").getChildren());
		assertNotNull(langRoot.getChild("pages").getChildren());

		List<String> pageNames = new ArrayList<String>();
		for (Element page : (List<Element>) configRoot.getChild("pages").getChildren()) {
			assertEquals("page", page.getName());
			String name = page.getAttributeValue("name");
			String visibility = page.getAttributeValue("visibility");
			assertNotNull(name);
			assertNotNull(visibility);
			assertTrue(visibility.matches("(public|protected|admin)"));
			pageNames.add(name);
		}

		// check, if every language has a headline and a title
		for (Element langPage : (List<Element>) langRoot.getChild("pages").getChildren()) {
			assertNotNull(langPage.getChildText("headline"));
			assertNotNull(langPage.getChildText("title"));
			assertFalse(langPage.getChildText("headline").isEmpty());
			assertFalse(langPage.getChildText("title").isEmpty());
		}

		// check, if every config page has a language page
		assertEquals(pageNames.size(), langRoot.getChild("pages").getChildren().size());
		for (String pageName : pageNames) {
			boolean hasALangPage = false;
			for (Element langPage : (List<Element>) langRoot.getChild("pages").getChildren()) {
				if (langPage.getAttributeValue("name").equals(pageName)) {
					hasALangPage = true;
					break;
				}
			}
			assertTrue(hasALangPage);
		}

		// check must pages
		for (String mustPageName : mustPageNames) {
			assertTrue(mustPageName, this.isValidLocation(mustPageName));
		}
	}

	private boolean isValidLocation(String templateResourceName) {
		return TemplateConfig.me().getContentProperties().getCustomConfigPage(templateResourceName) != null;
	}
}

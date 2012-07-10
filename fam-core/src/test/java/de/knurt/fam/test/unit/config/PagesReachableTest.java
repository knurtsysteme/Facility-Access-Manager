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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.EasyMock;
import org.jdom.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.template.model.TemplateContentProperties;
import de.knurt.fam.template.model.TemplateResource;
import de.knurt.fam.template.model.TemplateResource.Visibility;
import de.knurt.fam.template.util.TemplateConfig;
import de.knurt.fam.template.velocity.VelocityFileContentFactory;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * application must be reachable over tomcat here!
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class PagesReachableTest {

	private TemplateResource getTemplateResource(String filename, String resourceName, String suffix, String visibility) {
		TemplateResource templateResource = EasyMock.createMock(TemplateResource.class);
		User admin = TeztBeanSimpleFactory.getAdmin();
		MockHttpServletRequest mockrq = new MockHttpServletRequest();
		EasyMock.expect(templateResource.getSuffix()).andReturn(suffix).anyTimes();
		EasyMock.expect(templateResource.getName()).andReturn(resourceName).anyTimes();
		EasyMock.expect(templateResource.getFilename()).andReturn(filename).anyTimes();
		EasyMock.expect(templateResource.getAuthUser()).andReturn(admin).anyTimes();
		EasyMock.expect(templateResource.hasAuthUser()).andReturn(true).anyTimes();
		EasyMock.expect(templateResource.isInvalidSession()).andReturn(false).anyTimes();
		EasyMock.expect(templateResource.isInvalidAuth()).andReturn(false).anyTimes();
		EasyMock.expect(templateResource.isUnknownUser()).andReturn(false).anyTimes();
		EasyMock.expect(templateResource.isAccountExpired()).andReturn(false).anyTimes();
		EasyMock.expect(templateResource.isLoggedOut()).andReturn(false).anyTimes();
		EasyMock.expect(templateResource.isLostSession()).andReturn(false).anyTimes();
		EasyMock.expect(templateResource.getRequest()).andReturn(mockrq).anyTimes();
		EasyMock.expect(templateResource.getWritingResultProperties()).andReturn(null).anyTimes();
		EasyMock.expect(templateResource.getVisibility()).andReturn(this.getVisibility(visibility)).anyTimes();
		EasyMock.expect(templateResource.getTemplateFile()).andReturn("page.html").anyTimes();
		EasyMock.expect(templateResource.isRequestForContent()).andReturn(true).anyTimes();
		EasyMock.expect(templateResource.configurationReloadIsRequested()).andReturn(false).anyTimes();
		EasyMock.expect(templateResource.getSession()).andReturn(null).anyTimes();
		return templateResource;
	}

	private Visibility getVisibility(String visibility) {
		Visibility result = Visibility.PUBLIC;
		if (visibility.equals("protected")) {
			result = Visibility.PROTECTED;
		} else if (visibility.equals("admin")) {
			result = Visibility.ADMIN;
		}
		return result;
	}

	private String getContent(String filename, String resourceName, String suffix, String visibility) {
		return this.getContent(filename, resourceName, suffix, this.getTemplateResource(filename, resourceName, suffix, visibility));
	}

	private String getContent(String filename, String resourceName, String suffix, TemplateResource templateResource) {
		EasyMock.replay(templateResource);
		return VelocityFileContentFactory.me().getContent(templateResource);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void xmlPagesAreCorrect() throws Exception {
		TemplateContentProperties tcp = TemplateConfig.me().getContentProperties();
		List<Element> configpages = (List<Element>) tcp.getCustomConfig().getChild("pages").getChildren();
		loop_resourceName: for (int i = 0; i < configpages.size(); i++) {
			Element configpage = configpages.get(i);
			String resourceName = configpage.getAttributeValue("name");
			for (String notTestable : new String[] { "jobsurveypreview", "jobsurvey", "bookfacilitiesdone", "editfeedback", "viewrequest", "viewfeedback", "editrequest", "eventsofday", "editfacilityavailability", "systemfacilityavailability", "agreementde", "agreementen", "systemlistofusermails" }) {
				// these pages must have given parameters and are thus not
				// testable here
				if (notTestable.equals(resourceName)) {
					continue loop_resourceName;
				}
			}
			String filename = "does-not-matter";
			String suffix = "html";
			String visibility = configpage.getAttributeValue("visibility");
			String content = this.getContent(filename, resourceName, suffix, visibility);
			assertNotNull("is null: " + resourceName, content);
			for (String notTestable : new String[] { "editfeedback" }) {
				// these pages must have a facility given
				if (notTestable.equals(resourceName)) {
					continue loop_resourceName;
				}
			}
			this.assertCorrectContent(content, resourceName);
		}
	}

	private void assertCorrectContent(String content, String resourceName) {
		FamLog.debug(resourceName, 201205181951l);
		assertNotNull("content is null: " + resourceName, content);
		assertTrue("wrong page: " + resourceName, content.indexOf("body_" + resourceName) > 0);
		for (String p : new String[] { "$util", "$lang" }) {
			// XXX "$config" missed - some reason false alarms
			assertTrue(resourceName + " - " + p + " - " + content.indexOf(p), content.indexOf(p) < 0);
		}
	}
}

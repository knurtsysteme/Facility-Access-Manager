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
package de.knurt.fam.test.web.controller;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.view.text.FamDateFormat;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.fam.test.web.MolybdenumAction;
import de.knurt.fam.test.web.MolybdenumAssert;
import de.knurt.fam.test.web.TestPropertiesGetter;

/**
 * delegate get requests to the right content.
 * 
 * [resource]/[filename]/[suffix]/delegate.fam
 * 
 * @author Daniel Oltmanns
 * @since 1.21 (09/28/2010)
 */
@Controller
public final class MolybdenumController {

	private void init() {
		Properties props = TestPropertiesGetter.me().getTestProperties();
		props.put(Velocity.INPUT_ENCODING, "UTF-8");
		File templateDir = new File(props.getProperty("molybdenum.template.dir"));
		try {
			String pathToResource = templateDir.getCanonicalPath();
			props.put("file.resource.loader.path", pathToResource);
			Velocity.init(props);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/molybdenum-suite__xml__delegate.fam", method = RequestMethod.GET)
	public final ModelAndView handleGetRequests(HttpServletResponse response, HttpServletRequest request) {
		this.init();
		try {
			VelocityContext context = new VelocityContext();
			Template template = Velocity.getTemplate("molybdenum-suite.xml");
			context.put("html", TemplateHtml.me());
			context.put("assert", MolybdenumAssert.me());
			context.put("action", MolybdenumAction.me());
			context.put("FamConnector", FamConnector.class);
			context.put("FamDateFormat", FamDateFormat.class);
			FamLog.error("hallo!!!", 16l);//FIXME raus
			Writer writer = new StringWriter();
			template.merge(context, writer);
			response.setContentType("text/xml;charset=UTF-8");
			response.getWriter().print(writer.toString());
		} catch (ResourceNotFoundException e) {
			FamLog.logException(this.getClass(), e, "no resource found", 201010251423l);
		} catch (ParseErrorException e) {
			FamLog.logException(this.getClass(), e, "could not parse it", 201010251424l);
		} catch (Exception e) {
			FamLog.logException(this.getClass(), e, "unknown", 201010251421l);
		}
		return null;
	}

}
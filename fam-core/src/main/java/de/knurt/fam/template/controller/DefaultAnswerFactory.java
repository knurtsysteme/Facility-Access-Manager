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
package de.knurt.fam.template.controller;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.connector.RedirectTarget;
import de.knurt.fam.core.util.mvc.RedirectResolver;
import de.knurt.fam.template.model.TemplateResource;
import de.knurt.fam.template.util.TemplateConfig;

/**
 * default answer factory of the system delegating all requests to
 * {@link TemplateConfig#getContentFactory()}. if content found, put it. if not,
 * put an error message out.
 * 
 * @author Daniel Oltmanns
 * @since 1.3.0 (09/28/2010)
 */
public class DefaultAnswerFactory implements AnswerFactory {
	/** one and only instance of DefaultAnswerFactory */
	private volatile static DefaultAnswerFactory me;

	/** construct DefaultAnswerFactory */
	private DefaultAnswerFactory() {
	}

	/**
	 * return the one and only instance of DefaultAnswerFactory
	 * 
	 * @return the one and only instance of DefaultAnswerFactory
	 */
	public static DefaultAnswerFactory me() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (DefaultAnswerFactory.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new DefaultAnswerFactory();
				}
			}
		}
		return me;
	}

	private ModelAndView answerAll(TemplateResource templateResource, HttpServletResponse response, HttpServletRequest request, PrintWriter pw) {
		String content = TemplateConfig.me().getContentFactory().getContent(templateResource);
		if (content == null) {
			return this.answerNotFound(templateResource, response, pw);
		} else {
			pw.write(content);
			return null;
		}
	}

	private ModelAndView answerNotFound(TemplateResource templateResource, HttpServletResponse response, PrintWriter pw) {
		ModelAndView mav = null;
		if (FamConnector.isDev()) {
			response.setContentType("text/plain; charset=UTF-8");
			pw.write(String.format("there is an error on rendering this resource: %s\n", templateResource));
			pw.write("check the velocity log file!\n");
			pw.write("----\n");
			pw.write(String.format("template-directory: %s\n", FamConnector.templateDirectory()));
			pw.write("----\n");
			pw.write("Is the page defined in config and lang xml? Is the visibility set correctly in both?");
			// TODO #20 create dtd for template system
		} else {
			mav = RedirectResolver.redirect(RedirectTarget.PUBLIC_HOME);
		}
		return mav;
	}

	public ModelAndView answerHTML(TemplateResource templateResource, HttpServletResponse response, HttpServletRequest request, PrintWriter pw) {
		return this.answerAll(templateResource, response, request, pw);
	}

	public ModelAndView answerCSS(TemplateResource templateResource, HttpServletResponse response, HttpServletRequest request, PrintWriter pw) {
		return this.answerAll(templateResource, response, request, pw);
	}

	public ModelAndView answerJS(TemplateResource templateResource, HttpServletResponse response, HttpServletRequest request, PrintWriter pw) {
		return this.answerAll(templateResource, response, request, pw);
	}

	public ModelAndView answerUnknown(TemplateResource templateResource, HttpServletResponse response, HttpServletRequest request, PrintWriter pw) {
		return this.answerAll(templateResource, response, request, pw);
	}

	public ModelAndView answerJSON(TemplateResource templateResource, HttpServletResponse response, HttpServletRequest request, PrintWriter pw) {
		return this.answerAll(templateResource, response, request, pw);
	}
}

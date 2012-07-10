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

import de.knurt.fam.template.model.TemplateResource;

/**
 * answer requests with given resource and filename for specific suffixes.
 * 
 * @author Daniel Oltmanns
 * @since 1.3.0 (09/28/2010)
 */
public interface AnswerFactory {

    public ModelAndView answerHTML(TemplateResource templateResource, HttpServletResponse response, HttpServletRequest request, PrintWriter pw);

    public ModelAndView answerCSS(TemplateResource templateResource, HttpServletResponse response, HttpServletRequest request, PrintWriter pw);

    public ModelAndView answerJS(TemplateResource templateResource, HttpServletResponse response, HttpServletRequest request, PrintWriter pw);

    public ModelAndView answerJSON(TemplateResource templateResource, HttpServletResponse response, HttpServletRequest request, PrintWriter pw);

    public ModelAndView answerUnknown(TemplateResource templateResource, HttpServletResponse response, HttpServletRequest request, PrintWriter pw);

    

}

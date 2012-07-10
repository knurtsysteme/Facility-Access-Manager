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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.template.model.TemplateResource;

/**
 * this implements the behaviour of the Facility Access Manager in working with data input
 * and output.
 * 
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/06/2010)
 */
public interface ResourceController {
	public ModelAndView handleGetRequests(TemplateResource templateResource, HttpServletResponse response, HttpServletRequest request);
	public ModelAndView handleJSONRequest(String resourceName, HttpServletResponse response, HttpServletRequest request);
	public ModelAndView handleImageRequest(String resourceName, HttpServletResponse response, HttpServletRequest request);
}

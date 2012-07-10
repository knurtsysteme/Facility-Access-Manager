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
package de.knurt.fam.template.parser;

import de.knurt.fam.template.model.TemplateResource;

/**
 * produce contents
 * 
 * @author Daniel Oltmanns
 * @since 1.21 (09/28/2010)
 */
public interface ContentFactory {
	/**
	 * return the content for a given resource or <code>null</code>, if no
	 * content is available for this resource.
	 * 
	 * @param resource
	 *            the resource given
	 * @return the content for the given resource or <code>null</code>, if no
	 *         content is available for this resource.
	 */
    public String getContent(TemplateResource templateResource);
}

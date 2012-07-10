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
package de.knurt.fam.core.control.persistence.dao.config;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

/**
 * provide access to a set of configured instances.
 * 
 * @since 0.20090410 (04/10/2009)
 * @author Daniel Oltmanns <info@knurt.de>
 */
interface Access<T> {
	@Required
	void setConfiguredInstances(Map<String, T> configuredInstances);

	Set<String> getKeys();

	boolean keyExists(String key);
}

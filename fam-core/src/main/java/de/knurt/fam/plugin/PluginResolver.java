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
package de.knurt.fam.plugin;

/**
 * class that supports specific interfaces to override default behaviour of fam
 * within a plugin.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.3.0 (10/09/2010)
 */
public interface PluginResolver {

	/**
	 * override {@link DefaultRegisterSubmission} behaviour
	 * 
	 * @return {@link DefaultRegisterSubmission} behaviour
	 */
	public RegisterSubmission getRegisterSubmission();
}

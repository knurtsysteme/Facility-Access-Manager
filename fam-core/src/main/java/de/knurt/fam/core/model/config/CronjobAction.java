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
package de.knurt.fam.core.model.config;


/**
 * a dataholder for a cronjob action. for every cronjob action, a cronjob
 * resolver must resolve it. this is nothing but the action description.
 * 
 * @see CronjobActionResolver
 * @author Daniel Oltmanns
 * @since 0.20090913
 */
public interface CronjobAction {

	/**
	 * do the action
	 */
	public void resolve();

	/**
	 * return the time in minutes the action shall be done.
	 * 
	 * @return the time in minutes the action shall be done.
	 */
	public int resolveEvery();

	/**
	 * describe what you are
	 * 
	 * @return description of the cronjob
	 */
	public String getDescription();

}

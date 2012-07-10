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
 * interface for a plugin. these information are shown on the page for all
 * plugins overview.
 * 
 * @author Daniel Oltmanns
 * @since 1.3.0 (11/06/2010)
 */
public interface Plugin {

	/**
	 * return an id for this plugin.
	 * 
	 * @return an id for this plugin.
	 */
	public int getID();

	/**
	 * return the name of the plugin
	 * 
	 * @return the name of the plugin
	 */
	public String getName();

	/**
	 * return a short description of the plugin
	 * 
	 * @return a short description of the plugin
	 */
	public String getShortDescription();

	/**
	 * return the version of the plugin
	 * 
	 * @return the version of the plugin
	 */
	public String getVersion();

	/**
	 * return an image url shown in plugins overview page
	 * 
	 * @return an image url shown in plugins overview page
	 */
	public String getImageURL();

	/**
	 * return the url to the publisher of this plugin
	 * 
	 * @return the url to the publisher of this plugin
	 */
	public String getPublisherURL();

	/**
	 * return the name of the publisher of this plugin
	 * 
	 * @return the name of the publisher of this plugin
	 */
	public String getPublisher();

	/**
	 * return a license hint
	 * 
	 * @return a license hint
	 */
	public String getLicenseHint();

	/**
	 * return which version of the facility access manager is needed for this
	 * plugin.
	 * 
	 * @return which version of the facility access manager is needed for this
	 *         plugin.
	 */
	public Compatibility getCompatibilityFrom();

	/**
	 * return which version of the facility access manager is needed for this
	 * plugin.
	 * 
	 * @return which version of the facility access manager is needed for this
	 *         plugin.
	 */
	public Compatibility getCompatibilityTo();

	/**
	 * start the plugin. this method is called by the facility access manager
	 * when the plugin is loaded. typically you override a default class with
	 * your own class here.
	 */
	public void start();

	/**
	 * stop the plugin. this method is called by the facility access manager
	 * when the plugin is unloaded by clicking (stop by the user). this is not
	 * supported by now but is for later usage. make sure, everything that
	 * {@link #start()} did is eliminated.
	 */
	public void stop();

	/**
	 * return true, if the plugin has been started.
	 * 
	 * @return true, if the plugin has been started.
	 */
	public boolean isActive();

	/**
	 * return a long description of this plugin
	 * 
	 * @return a long description of this plugin
	 */
	public String getLongDescription();
}

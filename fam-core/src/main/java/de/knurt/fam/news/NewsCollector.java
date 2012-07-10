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
package de.knurt.fam.news;


/**
 * collect news from registered sources. is a news source as well
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.5.0 (07/29/2011)
 * 
 */
public interface NewsCollector extends NewsSource {

	/**
	 * register a source
	 * @param source to register
	 * @return true on registration success, otherwise false
	 */
	public boolean add(NewsSource source);
	/**
	 * cancel a registered source.
	 * @param source to cancel
	 * @return true on cancelation successed, otherwise false (espacially if the given source is not registered)
	 */
	public boolean remove(NewsSource source);
}

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
 * exception where a not bookable facility is given and a bookable is needed.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090923
 */
public class FacilityNotBookableException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * construct it with given message
	 * 
	 * @param message
	 *            explaining exception
	 */
	public FacilityNotBookableException(String message) {
		super(message);
	}

}

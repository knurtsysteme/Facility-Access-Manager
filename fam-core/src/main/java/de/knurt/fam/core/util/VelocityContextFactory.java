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
package de.knurt.fam.core.util;

import org.apache.velocity.VelocityContext;

import de.knurt.fam.core.model.persist.User;

/**
 * produce velocity contexts
 * 
 * @see VelocityContext
 * @author Daniel Oltmanns
 * @since 1.20 (08/08/2010)
 */
public class VelocityContextFactory {
	/** one and only instance of UserFactory */
	private volatile static VelocityContextFactory me;

	/** construct UserFactory */
	private VelocityContextFactory() {
	}

	/**
	 * return the one and only instance of UserFactory
	 * 
	 * @return the one and only instance of UserFactory
	 */
	public static VelocityContextFactory getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (VelocityContextFactory.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new VelocityContextFactory();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of UserFactory
	 */
	public static VelocityContextFactory me() {
		return getInstance();
	}

	public VelocityContext getUser(User user) {
		VelocityContext result = new VelocityContext();
		result.put("user", user);
		return result;
	}
}

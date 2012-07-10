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
package de.knurt.fam.template.util;

import java.io.File;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.security.encoder.FamEncoder;

/**
 * util for checking values
 * 
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/18/2010)
 */
public class ValueUtil {
	/** one and only instance of PageUtil */
	private volatile static ValueUtil me;

	/** construct PageUtil */
	private ValueUtil() {
	}

	/**
	 * return the one and only instance of PageUtil
	 * 
	 * @return the one and only instance of PageUtil
	 */
	public static ValueUtil getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (ValueUtil.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new ValueUtil();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of PageUtil
	 */
	public static ValueUtil me() {
		return getInstance();
	}

	/**
	 * a careless encoding of strings needed in templates if a id is needed.
	 */
	public static String encode(String string) {
		return FamEncoder.getInstance().encodeSomething(string, "a");
	}

	public boolean isNull(Object object) {
		return object == null;
	}

	public boolean isNullOrWhitspace(String string) {
		return this.isNull(string) || this.isWhitespace(string);
	}

	/**
	 * return true, if the file exists. otherwise false.
	 * 
	 * @param file
	 *            relative to the velocity template directory
	 * @return true, if the file exists. otherwise false.
	 */
	public boolean fileExists(String file) {
		return new File(FamConnector.templateDirectory() + file).exists();
	}

	private boolean isWhitespace(String string) {
		return string.trim().isEmpty() || string.trim().matches("\\W*");
	}
}

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
package de.knurt.fam.core.aspects.logging;

import org.apache.log4j.Logger;

/**
 * central point to log information.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090327 (03/27/2009)
 */
public class FamLog {

	/**
	 * log an info
	 * 
	 * @param clazz
	 *            as in {@link Logger#getLogger(java.lang.Class)}
	 * @param message
	 *            as in {@link Logger#info(java.lang.Object)}
	 * @param id
	 *            set as identifier for the message
	 */
	public static final void logInfo(Class<?> clazz, Object message, long id) {
		Logger.getRootLogger().info(getMessage(clazz, message, id));
	}

	/**
	 * log an exception
	 * 
	 * @param clazz
	 *            as in {@link Logger#getLogger(java.lang.Class)}
	 * @param exception
	 *            to log
	 * @param message
	 *            as in {@link Logger#info(java.lang.Object)}
	 * @param id
	 *            set as identifier for the message
	 */
	public static final void logException(Class<?> clazz, Exception exception, Object message, long id) {
		Logger.getRootLogger().fatal(getMessage(clazz, message, id, exception));
	}

	private static final String getMessage(Class<?> clazz, Object message, long id, Exception exception) {
		return getMessage(clazz, message + " / " + exception.toString(), id);
	}
	private static final String getMessage(Object message, long id, Exception exception) {
		return getMessage(message + " / " + exception.toString(), id);
	}

	private static final String getMessage(Class<?> clazz, Object message, long id) {
		return String.format("[%s] %s @%s", id, message, clazz);
	}
	private static final String getMessage(Object message, long id) {
		return String.format("%s @%s", id, message);
	}

	private FamLog() {
	}

	public static void error(Class<?> clazz, String message, long id) {
		Logger.getRootLogger().error(getMessage(clazz, message, id));
	}

	public static void exception(Exception exception, long id) {
		exception("no message", exception, id);
	}

	public static void exception(String message, Exception e, long id) {
		Logger.getRootLogger().fatal(getMessage(message, id, e));
	}

	public static void info(String message, long id) {
		Logger.getRootLogger().info(getMessage(message, id));
	}

	public static void error(String message, long id) {
		Logger.getRootLogger().error(getMessage(message, id));
	}

	public static void debug(String message, long id) {
		Logger.getRootLogger().debug(getMessage(message, id));
	}

	public static void warn(String message, long id) {
		Logger.getRootLogger().warn(getMessage(message, id));
	}

}

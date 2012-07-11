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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.digester.plugins.PluginConfigurationException;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;

/**
 * resolve loading plugins and support interfaces to set specific behaviour.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.3.0 (10/09/2010)
 */
public class DefaultPluginResolver implements PluginResolver {

	private RegisterSubmission registerSubmission = null;
	private List<Plugin> plugins = new ArrayList<Plugin>();

	public List<Plugin> getPlugins() {
		return plugins;
	}

	/** {@inheritDoc} */
	@Override
	public RegisterSubmission getRegisterSubmission() {
		return this.registerSubmission;
	}

	private boolean implementz(Class<?> clazz, Class<?> interfaze) {
		return this.implementz(clazz, interfaze.getName());
	}

	private boolean implementz(Class<?> clazz, String interfaceName) {
		boolean result = false;
		Class<?>[] interfaces = clazz.getInterfaces();
		for (Class<?> interfaze : interfaces) {
			if (interfaze.getName().equals(interfaceName)) {
				result = true;
				break;
			}
		}
		return result;
	}

	private boolean isPlugin(Class<?> cl) {
		return this.implementz(cl, Plugin.class);
	}

	/** {@inheritDoc} */
	@Override
	protected void finalize() throws Throwable {
		for (Plugin plugin : this.plugins) {
			plugin.stop();
		}
	}

	/** one and only instance of DefaultPluginResolver */
	private volatile static DefaultPluginResolver me;

	/** construct DefaultPluginResolver */
	private DefaultPluginResolver() {
		this.initPlugins();
	}

	private void initPlugins() {
		File pluginDirectory = new File(FamConnector.me().getPluginDirectory());
		if (pluginDirectory.exists() && pluginDirectory.isDirectory() && pluginDirectory.canRead()) {
			File[] files = pluginDirectory.listFiles();
			ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
			for (File file : files) {
				if (file.isFile() && file.getName().toLowerCase().endsWith("jar")) {
					try {
						JarFile jar = new JarFile(file.getAbsoluteFile().toString());
						Enumeration<JarEntry> jarEntries = jar.entries();
						while (jarEntries.hasMoreElements()) {
							JarEntry entry = jarEntries.nextElement();
							if (entry.getName().toLowerCase().endsWith("class")) {
								String className = entry.getName().replaceAll("/", ".").replaceAll("\\.class$", "");
								Class<?> cl = new URLClassLoader(new URL[] { file.toURI().toURL() }, currentThreadClassLoader).loadClass(className);
								if (this.isPlugin(cl)) {
									Plugin plugin = (Plugin) cl.newInstance();
									this.plugins.add(plugin);
								}
							}
						}
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						FamLog.logException(this.getClass(), e, "failed to load plugin", 201010091426l);
					} catch (InstantiationException e) {
						e.printStackTrace();
						FamLog.logException(this.getClass(), e, "failed to load plugin", 201010091424l);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						FamLog.logException(this.getClass(), e, "failed to load plugin", 201010091425l);
					} catch (IOException e) {
						e.printStackTrace();
						FamLog.logException(this.getClass(), e, "failed to load plugin", 201010091351l);
					}
				}
			}
			for (Plugin plugin : this.plugins) {
				boolean found = false;
				if (this.implementz(plugin.getClass(), RegisterSubmission.class)) {
					if (found == true) {
						throw new PluginConfigurationException("Found more than one RegisterSubmission classes");
						// TODO #19 supply a solution Ticket
					}
					this.registerSubmission = (RegisterSubmission) plugin;
					found = true;
				}
			}
			for (Plugin plugin : this.plugins) {
				plugin.start();
			}
		}
		// search plugin
		if (this.registerSubmission == null) {
			this.registerSubmission = new DefaultRegisterSubmission();
		}
	}

	/**
	 * return the one and only instance of DefaultPluginResolver
	 * 
	 * @return the one and only instance of DefaultPluginResolver
	 */
	public static DefaultPluginResolver getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (DefaultPluginResolver.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new DefaultPluginResolver();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of DefaultPluginResolver
	 */
	public static DefaultPluginResolver me() {
		return getInstance();
	}

	/**
	 * init plugins as far as it is not initialized by now
	 */
	public static void init() {
		getInstance();
	}
}
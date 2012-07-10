/*
 * Copyright 2009-2011 by KNURT Systeme (http://www.knurt.de)
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
package de.knurt.fam.service.pdf.control;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.io.ClassPathResource;

/**
 * configuration adapter
 * 
 * @author Daniel Oltmanns
 * @since 1.4.0 (05/30/2011)
 */
public class FileFactoryUtil {
	/** one and only instance of FamServiceConfiguration */
	private volatile static FileFactoryUtil me;

	/** construct FamServiceConfiguration */
	private FileFactoryUtil() {
	}

	/**
	 * return the one and only instance of FamServiceConfiguration
	 * 
	 * @return the one and only instance of FamServiceConfiguration
	 */
	public static FileFactoryUtil getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (FileFactoryUtil.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new FileFactoryUtil();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of FamServiceConfiguration
	 */
	public static FileFactoryUtil me() {
		return getInstance();
	}

	public static File outputDirectory() {
		return outputDirectory;
	}

	private static File outputDirectory = null;

	public void setOutputDirectory(String directory) {
		assert directory != null;
		outputDirectory = new File(directory);
		serno = outputDirectory().listFiles().length;
		assert outputDirectory.exists();
		assert outputDirectory.canRead();
		assert outputDirectory.canWrite();
		assert outputDirectory.isDirectory();
	}

	public static String filename(String customid) {
		return me().getFilename("pdf", customid);
	}

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	private static long serno = 0;
	private File sorryFile = null;

	public File getSorryFile() {
		return sorryFile;
	}

	public void setSorryFileUrl(ClassPathResource url) throws IOException {
		assert url != null;
		this.sorryFile = url.getFile();
		assert sorryFile.exists();
		assert sorryFile.canRead();
		assert sorryFile.isFile();
	}

	private String getFilename(String suffix, String customid) {
		return outputDirectory() + File.separator + String.format("%s-%s-%s.%s", sdf.format(new Date()), serno++, customid, suffix);
	}

}

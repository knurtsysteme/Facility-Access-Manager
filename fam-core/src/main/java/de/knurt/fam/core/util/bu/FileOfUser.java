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
package de.knurt.fam.core.util.bu;

import java.io.File;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.config.FileUploadController;
import de.knurt.fam.core.model.persist.User;
import de.knurt.heinzelmann.util.nebc.BoardUnit;

/**
 * create a {@link File} from a filename as uploaded in
 * {@link FileUploadController}.
 * 
 * @see FileUploadController
 * @author Daniel Oltmanns
 * @since 1.8.0 (04/20/2012)
 */
public class FileOfUser implements BoardUnit<String, File> {

	public FileOfUser(User user) {
		this.user = user;
		this.setUploadDir();
	}

	private File uploadDir = null;

	/**
	 * return the upload directory for the user
	 * 
	 * @return the upload directory for the user
	 */
	public File getUploadDir() {
		return uploadDir;
	}

	private void setUploadDir() {
		String upload_dir = FamConnector.fileExchangeDir() + File.separator + "users" + File.separator + user.getUsername();
		File result = null;
		File test = new File(upload_dir);
		if (!test.isDirectory()) {
			if (!test.mkdir()) {
				FamLog.error("could not create directory: " + upload_dir, 201204170958l);
			}
		}
		if (test.isDirectory() && !test.canWrite()) {
			if (!test.setWritable(true)) {
				FamLog.error("could not set directory to writable: " + upload_dir, 201204171001l);
			}
		}
		if (test.isDirectory() && test.canWrite()) {
			result = new File(upload_dir);
		}
		this.uploadDir = result;
	}

	private User user = null;

	/**
	 * return the file, if it exists for the {@link #user} in
	 * {@link #getUploadDir()} and is readable.
	 * 
	 * @see File#canRead()
	 * @param filename
	 *            searched for
	 * @return
	 */
	@Override
	public File process(String filename) {
		File result = new File(this.uploadDir.getAbsoluteFile() + File.separator + filename);
		return result.exists() && result.canRead() ? result : null;
	}

}

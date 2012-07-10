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
package de.knurt.fam.template.model;

import java.text.DecimalFormat;
import java.util.Properties;

import de.knurt.fam.core.model.config.FileUploadController;

/**
 * produce the model for filemanager.html
 * 
 * @author Daniel Oltmanns
 * @since 1.8.0 (04/18/2012)
 */
public class FileManagerModelFactory {

	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		FileUploadController.reinitOptions();
		result.put("accept_file_types", FileUploadController.ACCEPT_FILE_TYPES);
		result.put("max_file_size", this.readableFileSize(FileUploadController.MAX_FILE_SIZE));
		result.put("min_file_size", this.readableFileSize(FileUploadController.MIN_FILE_SIZE));
		result.put("min_file_size_sum", this.readableFileSize(FileUploadController.MAX_FILE_SIZE_SUM));
		result.put("max_number_file_size", FileUploadController.MAX_NUMBER_OF_FILES);
		return result;
	}

	// thanks to
	// http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
	private String readableFileSize(long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

}

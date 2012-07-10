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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.util.bu.FileOfUser;
import de.knurt.fam.template.model.TemplateResource;
import de.knurt.fam.template.util.TemplateConfig;

/**
 * control file uploads. use default system settings for max memory size and
 * temp directory.
 * 
 * @see DiskFileItemFactory
 * 
 * @author Daniel Oltmanns
 * @since 1.8.0 (04/17/2012)
 */
public class FileUploadController {
	private TemplateResource tr;
	private HttpServletResponse response;
	private FileItemFactory factory;
	private MimetypesFileTypeMap mftm;
	private String error = null;

	public static String[] ACCEPT_FILE_TYPES;
	public static long MAX_FILE_SIZE, MIN_FILE_SIZE;
	public static int MAX_NUMBER_OF_FILES, MAX_FILE_SIZE_SUM;

	public FileUploadController(TemplateResource tr, HttpServletResponse response) {
		reinitOptions();
		this.tr = tr;
		this.response = response;
		this.factory = new DiskFileItemFactory();
		this.mftm = new MimetypesFileTypeMap();
		this.uploadDir = new FileOfUser(tr.getAuthUser()).getUploadDir();
	}

	/**
	 * re-init the configuration for file uploads (sizes, suffixes et cetera)
	 */
	public static void reinitOptions() {
		try {
			ACCEPT_FILE_TYPES = FamConnector.getGlobalPropertyAsList("fileupload.accepted_file_types");
			MAX_FILE_SIZE = Integer.parseInt(FamConnector.getGlobalProperty("fileupload.max_single_file_size"));
			MIN_FILE_SIZE = Integer.parseInt(FamConnector.getGlobalProperty("fileupload.min_single_file_size"));
			MAX_NUMBER_OF_FILES = Integer.parseInt(FamConnector.getGlobalProperty("fileupload.max_total_number_of_files"));
			MAX_FILE_SIZE_SUM = Integer.parseInt(FamConnector.getGlobalProperty("fileupload.max_total_file_size"));
		} catch (Exception e) {
			FamLog.exception("error in global configuration", e, 201204241532l);
		}
	}

	public File[] getExistingFileNames() {
		File[] result = new File[0];
		File upload_dir = this.uploadDir;
		if(upload_dir == null) {
			FamLog.error("no upload dir configured or present", 201207101224l);
		} else {
			result = upload_dir.listFiles();
		}
		return result;
	}

	/**
	 * return the size of all files in bytes
	 * 
	 * @see File#length()
	 * @return the size of all files in bytes
	 */
	public long getSizeOfAllFiles() {
		File[] files = this.getExistingFileNames();
		long result = 0;
		for (File file : files) {
			result += file.length();
		}
		return result;
	}

	private String getNewFilename(String filename) {
		String suffix = filename.replaceFirst(".*\\.", "");
		String result = filename.substring(0, filename.length() - suffix.length() - 1).replaceAll("[^a-zA-Z0-9]", "_");
		while (result != result.replaceAll("__", "_"))
			result = result.replaceAll("__", "_");
		result += "." + suffix.toLowerCase();
		// check if this one exists
		boolean exists = false;
		for (File e : this.getExistingFileNames()) {
			if (e.getName().equalsIgnoreCase(result)) {
				exists = true;
				break;
			}
		}
		if (exists) {
			result = result.substring(0, result.length() - suffix.length() - 1) + "_copy." + suffix;
			return this.getNewFilename(result);
		} else {
			return result;
		}
	}

	private File getExistingFile(String filename) {
		File result = null;
		File[] candidates = this.getExistingFileNames();
		for (File candidate : candidates) {
			if (filename.equals(candidate.getName())) {
				result = candidate;
			}
		}
		return result;
	}

	public ModelAndView getModelAndView() {
		ModelAndView result = null;
		if (tr.getFilename().equalsIgnoreCase("get") && tr.getRequest().getMethod().equalsIgnoreCase("GET") && tr.getSuffix().equalsIgnoreCase("html")) {
			// initial call for the fileupload page (<iframe
			// src="get-fileupload.html" ...)
			tr.setTemplateFile("page_fileupload.html");
			result = TemplateConfig.me().getResourceController().handleGetRequests(tr, response, tr.getRequest());
		} else if (tr.getFilename().equalsIgnoreCase("put") && tr.getRequest().getMethod().equalsIgnoreCase("GET") && tr.getSuffix().equalsIgnoreCase("json")) {
			// requesting existing files
			JSONArray json = new JSONArray();
			File[] existings = this.getExistingFileNames();
			for (File existing : existings) {
				json.put(this.getJSONObject(existing));
			}
			this.write(json);
		} else if (tr.getFilename().equalsIgnoreCase("delete") && (tr.getRequest().getMethod().equalsIgnoreCase("POST") || tr.getRequest().getMethod().equalsIgnoreCase("DELETE")) && tr.getSuffix().equalsIgnoreCase("json")) {
			// ↖ delete
			boolean succ = false;
			File fileToDelete = this.getExistingFile(tr.getRequest().getParameter("file"));
			if (fileToDelete != null) {
				succ = fileToDelete.delete();
			}
			this.write(succ ? "true" : "false");
		} else if (tr.getFilename().equalsIgnoreCase("download") && tr.getRequest().getMethod().equalsIgnoreCase("GET") && tr.getSuffix().equalsIgnoreCase("json")) {
			File fileToDownload = this.getExistingFile(tr.getRequest().getParameter("file"));
			if (fileToDownload != null) {
				// ↘ force "save as" in browser
				response.setHeader("Content-Disposition", "attachment; filename=" + fileToDownload.getName());
				// ↘ it is a pdf
				response.setContentType(mftm.getContentType(fileToDownload));
				ServletOutputStream outputStream = null;
				FileInputStream inputStream = null;
				try {
					outputStream = response.getOutputStream();
					inputStream = new FileInputStream(fileToDownload);
					int nob = IOUtils.copy(inputStream, outputStream);
					if (nob < 1)
						FamLog.error("fail to download: " + fileToDownload.getAbsolutePath(), 201204181310l);
				} catch (IOException e) {
					FamLog.exception(e, 201204181302l);
				} finally {
					IOUtils.closeQuietly(inputStream);
					IOUtils.closeQuietly(outputStream);
				}
			}
		} else if (tr.getFilename().equalsIgnoreCase("put") && tr.getRequest().getMethod().equalsIgnoreCase("POST") && tr.getSuffix().equalsIgnoreCase("json") && ServletFileUpload.isMultipartContent(tr.getRequest())) {
			// ↖ insert

			JSONObject json_result = new JSONObject();
			// Parse the request
			try {
				// Create a new file upload handler
				ServletFileUpload upload = new ServletFileUpload(factory);
				@SuppressWarnings("unchecked")
				List<FileItem> items = upload.parseRequest(tr.getRequest());
				this.setError(items);
				if (this.error == null) {
					// Process the uploaded items
					for (FileItem item : items) {
						if (!item.isFormField()) {

							File uploadedFile = null;
							File upload_dir = this.uploadDir;
							if (upload_dir != null) {
								uploadedFile = new File(upload_dir.getAbsolutePath() + File.separator + this.getNewFilename(item.getName()));
							}
							try {
								item.write(uploadedFile);
								json_result = this.getJSONObject(uploadedFile);
							} catch (Exception e) {
								FamLog.exception(e, 201204170945l);
								json_result.put("error", "unknown");
							}
						}
					}
				} else { // ← error
					json_result.put("error", this.error);
				}
			} catch (FileUploadException e) {
				FamLog.exception(e, 201204170928l);
				try {
					json_result.put("error", "unknown");
				} catch (JSONException e1) {
					FamLog.exception(e1, 201204171037l);
				}
			} catch (JSONException e) {
				FamLog.exception(e, 201204171036l);
			}
			JSONArray json_wrapper = new JSONArray();
			json_wrapper.put(json_result);
			this.write(json_wrapper);
		}
		return result;
	}

	private JSONObject getJSONObject(File file) {
		JSONObject result = new JSONObject();
		try {
			result.put("name", file.getName());
			result.put("size", file.length());
			result.put("type", mftm.getContentType(file));
			result.put("url", String.format("download-fileupload.json?file=%s", file.getName()));
			result.put("delete_url", String.format("delete-fileupload.json?file=%s", file.getName()));
			result.put("delete_type", "DELETE");
		} catch (JSONException e) {
			FamLog.exception(e, 201204171115l);
		}
		return result;
	}

	private void setError(List<FileItem> items) {
		long size_sum = 0;
		if (items.size() > this.getExistingFileNames().length + MAX_NUMBER_OF_FILES) {
			this.error = "maxNumberOfFiles";
		} else {
			for (FileItem item : items) {
				size_sum += item.getSize();
				if (item.getSize() > MAX_FILE_SIZE) {
					this.error = "maxFileSize";
					break;
				}
				if (item.getSize() < MIN_FILE_SIZE) {
					this.error = "minFileSize";
					break;
				}
				boolean acceptedFileType = false;
				for (String suffix : ACCEPT_FILE_TYPES) {
					if (item.getName().endsWith(suffix)) {
						acceptedFileType = true;
						break;
					}
				}
				if (!acceptedFileType) {
					this.error = "acceptFileTypes";
				}
			}
		}
		if (this.error == null && this.getSizeOfAllFiles() + size_sum > MAX_FILE_SIZE_SUM) {
			this.error = "maxFileSize";
		}
	}

	private void write(String output) {
		PrintWriter pw = null;
		try {
			response.setContentType("application/json");
			pw = response.getWriter();
			IOUtils.write(output, pw);
		} catch (IOException ex) {
			FamLog.exception(ex, 201204171241l);
		} finally {
			IOUtils.closeQuietly(pw);
		}
	}

	private void write(JSONArray json) {
		this.write(json.toString());
	}

	private File uploadDir = null;

}

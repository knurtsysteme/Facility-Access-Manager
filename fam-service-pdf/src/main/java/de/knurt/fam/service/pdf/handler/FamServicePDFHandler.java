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
package de.knurt.fam.service.pdf.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.service.pdf.control.bu.HttpServletRequest2File;

/**
 * handler and controller for requests on generating pdf files.
 * 
 * @author Daniel Oltmanns
 * @since 1.4.0 (05/30/2011)
 */
@Controller
public final class FamServicePDFHandler {

	@RequestMapping(value = "/generated.pdf", method = RequestMethod.POST)
	public final ModelAndView generatePDF(HttpServletResponse response, HttpServletRequest request) {
		ServletOutputStream output = null;
		FileInputStream input = null;
		try {
			File file2write = new HttpServletRequest2File().process(request);
			input = new FileInputStream(file2write);
			output = response.getOutputStream();

			response.setContentType("application/pdf;charset=UTF-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + file2write.getName());

			int nob = IOUtils.copy(input, output);
			if (nob < 1) {
				Logger.getRootLogger().fatal("201204231353");
			}
		} catch (IOException ex) {
			Logger.getRootLogger().fatal("201105301032");
		} finally {
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(output);
		}
		return null;
	}

}

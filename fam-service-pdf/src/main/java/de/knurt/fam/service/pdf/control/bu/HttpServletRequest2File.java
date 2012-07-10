package de.knurt.fam.service.pdf.control.bu;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import de.knurt.fam.service.pdf.control.ebc.BoardUnit;
import de.knurt.fam.service.pdf.control.ebc.Pipe;
import de.knurt.fam.service.pdf.model.PDFOption;

/**
 * generate pdf file from a request, containing pdf options
 * 
 * @author Daniel Oltmanns
 * @since 1.4.0 (05/30/2011)
 */
public class HttpServletRequest2File implements BoardUnit<HttpServletRequest, File> {

	@Override
	public File process(HttpServletRequest datum) {
		return new Pipe<HttpServletRequest, PDFOption, File>(new HttpServletRequest2PDFOption(), new PDFOption2File()).process(datum);
	}
}

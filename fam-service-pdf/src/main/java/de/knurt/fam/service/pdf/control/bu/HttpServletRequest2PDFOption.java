package de.knurt.fam.service.pdf.control.bu;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import de.knurt.fam.service.pdf.control.ebc.BoardUnit;
import de.knurt.fam.service.pdf.control.ebc.Pipe;
import de.knurt.fam.service.pdf.model.PDFOption;

/**
 * create a pdf options from a request
 * 
 * @author Daniel Oltmanns
 * @since 1.4.0 (05/30/2011)
 */

class HttpServletRequest2PDFOption implements BoardUnit<HttpServletRequest, PDFOption> {

	@Override
	public PDFOption process(HttpServletRequest datum) {
		return new Pipe<HttpServletRequest, JSONObject, PDFOption>(new HttpServletRequest2JSONObject(), new JSONObject2PDFOption()).process(datum);
	}

}

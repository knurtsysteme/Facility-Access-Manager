package de.knurt.fam.service.pdf.control.bu;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.servlet.http.HttpServletRequest;

import de.knurt.fam.service.pdf.control.ebc.BoardUnit;
import de.knurt.fam.service.pdf.control.ebc.Pipe;

/**
 * generate pdf file from a request, containing pdf options
 * 
 * @author Daniel Oltmanns
 * @since 1.4.0 (05/30/2011)
 */
public class HttpServletRequest2ByteArrayOutputStream implements BoardUnit<HttpServletRequest, ByteArrayOutputStream> {

	@Override
	public ByteArrayOutputStream process(HttpServletRequest datum) {
		return new Pipe<HttpServletRequest, File, ByteArrayOutputStream>(new HttpServletRequest2File(), new File2ByteArrayOutputStream()).process(datum);
	}
}

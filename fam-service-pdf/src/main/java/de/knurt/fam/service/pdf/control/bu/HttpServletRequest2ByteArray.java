package de.knurt.fam.service.pdf.control.bu;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import de.knurt.fam.service.pdf.control.ebc.BoardUnit;
import de.knurt.fam.service.pdf.control.ebc.Pipe;

/**
 * generate a byte array from a given httpservlet request using a pipe from {@link HttpServletRequest2File} to {@link File2ByteArray}
 * 
 * @author Daniel Oltmanns
 * @since 1.4.0 (06/07/2011)
 */
public class HttpServletRequest2ByteArray implements BoardUnit<HttpServletRequest, Byte[]> {

	@Override
	public Byte[] process(HttpServletRequest datum) {
		return new Pipe<HttpServletRequest, File, Byte[]>(new HttpServletRequest2File(), new File2ByteArray()).process(datum);
	}
}

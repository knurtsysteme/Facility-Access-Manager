package de.knurt.fam.service.pdf.control.bu;

import java.io.ByteArrayOutputStream;
import java.io.File;

import de.knurt.fam.service.pdf.control.ebc.BoardUnit;

/**
 * generate a byte array from a given file
 * 
 * @author Daniel Oltmanns
 * @since 1.4.0 (06/07/2011)
 */
public class File2ByteArray implements BoardUnit<File, Byte[]> {

	@Override
	public Byte[] process(File datum) {
		ByteArrayOutputStream os = new File2ByteArrayOutputStream().process(datum);

		byte[] preresult = os.toByteArray();
		Byte[] result = new Byte[preresult.length];
		for (int i = 0; i < preresult.length; i++) {
			result[i] = new Byte(preresult[i]);
		}

		return result;
	}
}

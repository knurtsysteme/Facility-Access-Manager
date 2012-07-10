package de.knurt.fam.service.pdf.control.bu;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import de.knurt.fam.service.pdf.control.ebc.BoardUnit;

/**
 * generate a byte array output stream from a given file
 * 
 * @author Daniel Oltmanns
 * @since 1.4.0 (06/07/2011)
 */
public class File2ByteArrayOutputStream implements BoardUnit<File, ByteArrayOutputStream> {

	@Override
	public ByteArrayOutputStream process(File datum) {
		try{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			InputStream is = new FileInputStream(datum.getAbsoluteFile());
			for (int bytee; (bytee = is.read()) != -1;) {
				os.write(bytee);
			}
			is.close();
			os.flush();
			os.close();
			return os;
		} catch (FileNotFoundException e) {
			Logger.getRootLogger().fatal("201106071317");
			return null;
		} catch (IOException e) {
			Logger.getRootLogger().fatal("201106071316");
			return null;
		}
	}
}

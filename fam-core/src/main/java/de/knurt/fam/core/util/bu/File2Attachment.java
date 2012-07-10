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

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

import org.jcouchdb.document.Attachment;

import de.knurt.heinzelmann.util.nebc.BoardUnit;
import de.knurt.heinzelmann.util.nebc.bu.File2ByteArray;

/**
 * produce an {@link Attachment} from a {@link File}.
 * 
 * @author Daniel Oltmanns
 * @since 1.8.0 (04/20/2012)
 */
public class File2Attachment implements BoardUnit<File, Attachment> {

	/**
	 * using {@link MimetypesFileTypeMap} and {@link File2ByteArray} here.
	 */
	public File2Attachment() {
		this(new MimetypesFileTypeMap(), new File2ByteArray());
	}

	private FileTypeMap ftm = null;
	private BoardUnit<File, Byte[]> bu = null;

	public File2Attachment(FileTypeMap ftm, BoardUnit<File, Byte[]> bu) {
		this.ftm = ftm;
		this.bu = bu;
	}

	/** {@inheritDoc} */
	@Override
	public Attachment process(File datum) {
		String contentType = ftm.getContentType(datum);
		Byte[] preresult = bu.process(datum);
		byte[] data = new byte[preresult.length];
		for (int i = 0; i < preresult.length; i++) {
			data[i] = preresult[i];
		}
		return new Attachment(contentType, data);

	}

}

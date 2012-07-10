package de.knurt.fam.service.pdf.control.bu;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

import de.knurt.fam.service.pdf.control.ebc.BoardUnit;
import de.knurt.fam.service.pdf.model.TextContent;

public class TextContent2BaseFont implements BoardUnit<TextContent, BaseFont> {

	private BaseFont defaultBaseFont;

	private BaseFont createFont(String fontFamily) throws DocumentException, IOException {
		return BaseFont.createFont(fontFamily, BaseFont.WINANSI, BaseFont.EMBEDDED);
	}

	public TextContent2BaseFont() {
		try {
			defaultBaseFont = this.createFont(BaseFont.HELVETICA);
		} catch (DocumentException e) {
			Logger.getRootLogger().fatal("201106061115");
		} catch (IOException e) {
			Logger.getRootLogger().fatal("201106061114");
		}
	}

	@Override
	public BaseFont process(TextContent datum) {
		BaseFont result = null;
		Object fontFamily = datum.getStyle("font-family");
		if (fontFamily == null) {
			result = this.defaultBaseFont;
		} else {
			try {
				result = this.createFont(fontFamily.toString());
			} catch (DocumentException e) {
				result = this.defaultBaseFont;
			} catch (IOException e) {
				result = this.defaultBaseFont;
			}
		}
		return result;
	}

}

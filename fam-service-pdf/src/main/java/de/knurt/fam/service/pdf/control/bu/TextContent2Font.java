package de.knurt.fam.service.pdf.control.bu;

import java.awt.Color;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;

import de.knurt.fam.service.pdf.control.ebc.BoardUnit;
import de.knurt.fam.service.pdf.model.TextContent;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 07.06.2011
 * 
 */
public class TextContent2Font implements BoardUnit<TextContent, Font> {

	private final Font defaultBaseFont;

	private Font createFont(String fontname, int size, int style, Color color) {
		return FontFactory.getFont(fontname, size, style, color);
	}

	private Font createFont(String fontname, float size, int style, Color color) {
		return this.createFont(fontname, (int) size, style, color);
	}

	public TextContent2Font() {
		defaultBaseFont = this.createFont(BaseFont.HELVETICA, 12, Font.NORMAL, Color.BLACK);
	}

	@Override
	public Font process(TextContent datum) {
		Object fontFamily = datum.getStyle("font-family");

		if (fontFamily == null) {
			fontFamily = this.defaultBaseFont.getFamilyname();
		}
		return this.createFont(fontFamily.toString(), datum.getFontSize(), datum.getFontStyle(), datum.getFontColor());
	}

}

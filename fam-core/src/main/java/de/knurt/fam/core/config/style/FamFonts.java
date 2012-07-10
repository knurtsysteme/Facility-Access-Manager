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
package de.knurt.fam.core.config.style;

import java.awt.Font;

import de.knurt.heinzelmann.util.graphics.text.StringMetrics;
import de.knurt.heinzelmann.util.graphics.text.StringMetricsGraphics;
import de.knurt.heinzelmann.util.graphics.text.TextSplitter;
import de.knurt.heinzelmann.util.graphics.text.TextSplitterOnWidth;

/**
 * font factory for fonts used by the system
 * 
 * @author Daniel Oltmanns
 * @since 0.20090423 (04/23/2009)
 */
public class FamFonts {

	private static StringMetrics metrics = null;

	/**
	 * return the string metrics for the standard font.
	 * 
	 * @return the string metrics for the standard font.
	 */
	public static StringMetrics getStringMetrics() {
		if (metrics == null) {
			metrics = new StringMetricsGraphics(FamFonts.getFont());
		}
		return metrics;
	}

	/**
	 * return the standard font
	 * 
	 * @return standard font
	 */
	public static Font getFont() {
		return STANDARD_FONT;
	}

	private final static String STANDARD_FONT_NAME = "Arial";
	private final static Font STANDARD_FONT = new Font(STANDARD_FONT_NAME, Font.PLAIN, 10);

	/**
	 * return standard font in given size and weight
	 * 
	 * @param weight
	 *            of the font
	 * @param size
	 *            of the font
	 * @return standard font in given size and weight
	 */
	public static Font getFont(int weight, int size) {
		return new Font(STANDARD_FONT_NAME, weight, size);
	}

	/**
	 * return standard line height of the standard font
	 * 
	 * @see #getFont()
	 * @return standard line height of the standard font
	 */
	public static int getLineHeight() {
		return getLineHeight(getFont());
	}

	/**
	 * return a text splitter for the default font splitting on blanks for the
	 * given width.
	 * 
	 * @param width
	 *            the text shall be splitted at
	 * @return a text splitter for the default font splitting on blanks for the
	 *         given width.
	 */
	public static TextSplitter getTextSplitterOnWidth(int width) {
		return new TextSplitterOnWidth(width, ' ', FamFonts.getStringMetrics());
	}

	private FamFonts() {
	}

	/**
	 * return the standard line height for the given font. this is 120 % of the
	 * font size.
	 * 
	 * @param font
	 *            as the reference for the line height
	 * @return the standard line height for the given font.
	 */
	public static int getLineHeight(Font font) {
		return Math.round(font.getSize() * 1.2f) + 1;
	}
}

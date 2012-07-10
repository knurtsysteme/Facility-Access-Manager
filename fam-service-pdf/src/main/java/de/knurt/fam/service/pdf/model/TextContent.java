package de.knurt.fam.service.pdf.model;

import java.awt.Color;
import java.util.Properties;

import com.lowagie.text.Element;
import com.lowagie.text.Font;

/**
 * content and style for a specific paragraph
 * 
 * @author Daniel Oltmanns
 * @since 1.4.0 (05/30/2011)
 */
public class TextContent {
	private int pageNumber = 1;
	private String text = null;
	private Properties style = new Properties();

	/**
	 * forward of {@link Properties#put(Object, Object)}
	 * 
	 * @param key
	 * @param value
	 */
	public void putStyle(Object key, Object value) {
		this.style.put(key, value);
	}

	/**
	 * forward of {@link Properties#get(Object)}
	 * 
	 * @param key
	 * @return value of key in style properties or null if key does not exist
	 */
	public String getStyle(String key) {
		if (this.style.containsKey(key)) {
			return this.style.get(key).toString();
		} else {
			return null;
		}
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	/**
	 * set the page number options are for
	 * 
	 * @param pagenumber
	 *            options are for
	 */
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	/**
	 * return the page number options are for
	 * 
	 * @return page number options are for
	 */
	public int getPageNumber() {
		return pageNumber;
	}

	public float getFontSize() {
		float result = 12f;
		String fontSize = this.getStyle("font-size");
		if (fontSize != null) {
			try {
				result = Float.parseFloat(fontSize);
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}

	public int getAlignment() {
		int result = Element.ALIGN_LEFT;
		String textAlign = this.getStyle("text-align");
		if (textAlign != null) {
			if (textAlign.equalsIgnoreCase("right")) {
				result = Element.ALIGN_RIGHT;
			} else if (textAlign.equalsIgnoreCase("center")) {
				result = Element.ALIGN_CENTER;
			} else if (textAlign.equalsIgnoreCase("justify")) {
				result = Element.ALIGN_JUSTIFIED;
			}
		}
		return result;
	}

	public int getLowerLeftX() {
		int result = 0;
		try {
			result = Integer.parseInt(this.getStyle("left"));
		} catch (NumberFormatException e) {
		} catch (NullPointerException e) {
		}
		return result;
	}

	public int getLowerLeftY() {
		int result = 0;
		try {
			result = Integer.parseInt(this.getStyle("bottom"));
		} catch (NumberFormatException e) {
		} catch (NullPointerException e) {
		}
		return result;
	}

	public int getUpperRightX() {
		int width = 500;
		try {
			width = Integer.parseInt(this.getStyle("width"));
		} catch (NumberFormatException e) {
		} catch (NullPointerException e) {
		}
		return width + this.getLowerLeftX();
	}

	public int getUpperRightY() {
		int height = 500;
		try {
			height = Integer.parseInt(this.getStyle("height"));
		} catch (NumberFormatException e) {
		}
		return height + this.getLowerLeftY();
	}

	/**
	 * return the font style as set in font-style and font-weight and
	 * text-decoration or "normal" if not set.
	 * 
	 * @see Font#getStyleValue(String)
	 */
	public int getFontStyle() {
		String fontStyleHelper = this.getStyle("font-style") + " " + this.getStyle("font-weight") + " " + this.getStyle("text-decoration");
		return Font.getStyleValue(fontStyleHelper);
	}

	/**
	 * return color from value in hexadecimal or black if not set!
	 * 
	 */
	public Color getFontColor() {
		String color = this.getStyle("color");
		if (color != null) {
			return Color.decode("0x" + color.replaceFirst("#", ""));
		} else {
			return Color.BLACK;
		}
	}

	/**
	 * return the absolute line-height to use with this text.
	 * set <code>line-height</code> for this.
	 */
	public float getLeading() {
		float result = this.getFontSize() * 1.2f;
		String lineHeight = this.getStyle("line-height");
		if (lineHeight != null) {
			try {
				result = Float.parseFloat(lineHeight);
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}
}
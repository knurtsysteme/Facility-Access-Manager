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
package de.knurt.fam.core.util.graphics;

import java.util.List;

import de.knurt.heinzelmann.util.graphics.text.TextSplitter;

/**
 * a textarea with a x position, y position and a text - but without a width and
 * height. strongly orientate to a vertical textcol. e.g. use y position for
 * <code>compareTo</code> method.
 * 
 * @see TextCol
 * @author Daniel Oltmanns
 * @since 1.20100210
 */
public class TextArea implements Comparable<TextArea> {

	private int posX, posY, lineHeight;
	private String text;
	private TextSplitter textSplitter;

	public TextArea(int posX, int posY, TextSplitter textSplitter, int lineHeight) {
		this.posX = posX;
		this.posY = posY;
		this.text = "";
		this.textSplitter = textSplitter;
		this.lineHeight = lineHeight;
	}

	public String addText(String add) {
		this.text += add;
		return this.text;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public String getText() {
		return text;
	}

	public int getButtom() {
		return this.getPosY() + this.getHeight();
	}

	public int compareTo(TextArea o) {
		int result = 0;
		if (this.getPosY() > o.getPosY()) {
			result = 1;
		} else if (this.getPosY() < o.getPosY()) {
			result = -1;
		}
		return result;
	}

	public void setPosY(int posY) {
		this.posY = posY;
	}

	public int getLineHeight() {
		return this.lineHeight;
	}

	public int getHeight() {
		return this.textSplitter.split(this.getText()).size() * this.getLineHeight();
	}

	public List<String> getTextSplitted() {
		return this.textSplitter.split(this.getText());
	}
}

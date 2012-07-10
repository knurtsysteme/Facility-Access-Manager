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

import java.util.ArrayList;
import java.util.Collections;

/**
 * a vertical textcol containing textareas.
 * 
 * @see TextArea
 * @author Daniel Oltmanns
 * @since 1.20100210
 */
public class TextCol extends ArrayList<TextArea> {

	private static final long serialVersionUID = 1201002100804L;

	public TextCol() {
		super();
	}

	/**
	 * return all textareas that have the given x position
	 * 
	 * @param posX
	 *            all returned textarea have.
	 * @return all textareas that have the given x position
	 */
	public TextCol getPosX(int posX) {
		TextCol result = new TextCol();
		for (TextArea ta : this) {
			if (ta.getPosX() == posX) {
				result.add(ta);
			}
		}
		return result;
	}

	/**
	 * move down overlapping textareas. as a side effect, sort me.
	 * 
	 * @see TextArea#compareTo(de.knurt.fam.core.util.graphics.TextArea)
	 * @return this
	 */
	public TextCol doNotOverlap_moveDown() {
		Collections.sort(this);
		TextCol result = new TextCol();
		int posYNextTextLine = 0;
		for (TextArea ta : this) {
			if (ta.getPosY() < posYNextTextLine) {
				ta.setPosY(posYNextTextLine); // move down
			}
			result.add(ta);
			posYNextTextLine = ta.getButtom();
		}
		return result;
	}
}

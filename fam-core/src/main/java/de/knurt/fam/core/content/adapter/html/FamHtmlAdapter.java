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
package de.knurt.fam.core.content.adapter.html;

import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.util.adapter.ViewableObject;

/**
 * wrap an object to something viewable.
 * generate information html for the object being adapted.
 * this is the interface for converting content into view.
 * by now, this is nothing but an empty superclass typically used by a DasAdapterFactory
 * @param <T> the object type that is adapted here.
 * @see DasHtmlAdapterAbstractFactory
 * @author Daniel Oltmanns
 * @since 0.20090813 (08/13/2009)
 */
@Deprecated
public abstract class FamHtmlAdapter<T extends ViewableObject> {

    /**
     * construct it without doing anything by now
     */
    protected FamHtmlAdapter() {
    }
    private T original;

    /**
     * construct this adapter.
     * @param original the original object being adapted here.
     */
	public FamHtmlAdapter(T original) {
		this.original = original;
	}

    /**
     * return the original object, that is adapted here.
     * @return the original object, that is adapted here.
     */
    public T getOriginal() {
        return original;
    }

    /**
     * return an empty html table with class name <code>justtext</code>.
     * @return an empty html table with class name <code>justtext</code>.
     */
    protected HtmlElement getHtmlTable() {
        HtmlElement result = HtmlFactory.get("table");
        result.addClassName("justtext");
        return result;
    }

    /**
     * return the given content centred.
     * @param content to be centred
     * @return the given content centred.
     */
    protected String centerIt(Object content) {
        return String.format("<div class=\"center maxSize\">%s</div>", content);
    }
}

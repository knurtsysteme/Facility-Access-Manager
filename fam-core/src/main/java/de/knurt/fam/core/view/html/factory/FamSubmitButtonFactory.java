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
package de.knurt.fam.core.view.html.factory;

import de.knurt.heinzelmann.ui.html.HtmlButtonFactory;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;

/**
 * produce submit-buttons for different forms.
 * @author Daniel Oltmanns
 * @since 0.20090624 (06/24/2009)
 */
public class FamSubmitButtonFactory extends HtmlButtonFactory {

    /**
     * return <code>&lt;button [...] class="withpic[...]">label&lt;/button&gt;</code>
     * @param label of the button
     * @return <code>&lt;button [...] class="withpic[...]">label&lt;/button&gt;</code>
     */
    public static HtmlElement getButtonWithPic(String label) {
        HtmlElement result = getButton(label);
        result.addClassName("withpic");
        return result;
    }

    /**
     * return <code>&lt;button type="submit">label&lt;/button&gt;</code>
     * @param label of the button
     * @return <code>&lt;button type="submit">label&lt;/button&gt;</code>
     */
    public static HtmlElement getButton(String label) {
        HtmlElement result = HtmlFactory.get("button", label);
        result.setAttribute("type", "submit");
        return result;
    }

    /**
     * return same as {@link #getButtonWithPic(java.lang.String)}.
     * use given element instead of a label.
     * @param element used as content for the button
     * @return same as {@link #getButtonWithPic(java.lang.String)}.
     */
    public static HtmlElement getButtonWithPic(HtmlElement element) {
        return getButtonWithPic(element.toString());
    }

    /**
     * return <code>&lt;button [...] disabled="disabled" title="not possible!">&lt;img [...] src="[...]image.toString()" /&gt;&lt;/button&gt;</code>
     * @param image used for the image
     * @see #getButtonWithPic(java.lang.String)
     * @return <code>&lt;button [...] disabled="disabled" title="not possible!">&lt;img [...] src="[...]image.toString()" /&gt;&lt;/button&gt;</code>
     */
    protected static HtmlElement getDisabledButtonWithPic(Object image) {
        HtmlElement button = getButtonWithPic(image.toString());
        button.setAttribute("disabled", "disabled");
        button.setAttribute("title", "not possible!"); // INTLANG
        return button;
    }

    /**
     * return a button to edit something
     * @return a button to edit something
     */
    public static HtmlElement getEditButton() {
        return getIconButton("Edit", "edit"); // INTLANG
    }

    /**
     * return a button to delete something
     * @return a button to delete something
     */
    public static HtmlElement getDeleteButton() {
        return getIconButton("Delete", "delete"); // INTLANG
    }

    private static HtmlElement getIconButton(String label, String kind) {
        HtmlElement result = getButton(label);
        result.addClassName("icon").addClassName(kind);
        return result;
    }

    /**
     * return a button to add something
     * @param label of the button
     * @return a button to add something
     */
    public static HtmlElement getAddButton(String label) {
        return getIconButton(label, "add");
    }
    /**
     * return a button to add something
     * @return a button to add something
     */
    public static HtmlElement getAddButton() {
        return getAddButton("Add"); // INTLANG
    }

    /**
     * return a button to change something
     * @param label of the button
     * @return a button to change something
     */
    public static HtmlElement getChangeButton(String label) {
        return getIconButton(label, "change");
    }
    /**
     * return a button to change something
     * @return a button to change something
     */
    public static HtmlElement getChangeButton() {
        return getAddButton("Change"); // INTLANG
    }

    /**
     * return a button to back something
     * @param label of the button
     * @return a button to back something
     */
    public static HtmlElement getBackButton(String label) {
        return getIconButton(label, "back");
    }
    /**
     * return a button to back something
     * @return a button to back something
     */
    public static HtmlElement getBackButton() {
        return getAddButton("Back"); // INTLANG
    }

    /**
     * return a button to next something
     * @param label of the button
     * @return a button to next something
     */
    public static HtmlElement getNextButton(String label) {
        return getIconButton(label, "next");
    }
    /**
     * return a button to next something
     * @return a button to next something
     */
    public static HtmlElement getNextButton() {
        return getAddButton("Next"); // INTLANG
    }
}

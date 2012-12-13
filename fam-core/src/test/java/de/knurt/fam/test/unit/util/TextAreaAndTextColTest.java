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
package de.knurt.fam.test.unit.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Font;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.knurt.fam.core.util.graphics.TextArea;
import de.knurt.fam.core.util.graphics.TextCol;
import de.knurt.heinzelmann.util.graphics.text.StringMetricsGraphics;
import de.knurt.heinzelmann.util.graphics.text.TextSplitter;
import de.knurt.heinzelmann.util.graphics.text.TextSplitterOnWidth;

public class TextAreaAndTextColTest {

    public TextAreaAndTextColTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testTextArea() {
        TextArea ta = new TextArea(1, 2, this.getTextSplitter(), 12);
        assertEquals(1, ta.getPosX());
        assertEquals(2, ta.getPosY());
        assertEquals("", ta.getText());
        ta.addText("foo");
        assertEquals("foo", ta.getText());
        ta.addText("bar");
        assertEquals("foobar", ta.getText());
        ta.setPosY(150);
        assertEquals(150, ta.getPosY());
        assertEquals(12, ta.getLineHeight());
        assertEquals(12, ta.getHeight());
        ta.addText("we have a width of 100 - so lets test the splitter.");
        // 36 or 48 - it depends on the plattform
        assertTrue(36 == ta.getHeight() || 48 == ta.getHeight());
    }

    @Test
    public void testTextCol() {
        TextCol tc = new TextCol();
        TextArea ta = new TextArea(1, 2, this.getTextSplitter(), 12);
        tc.add(ta);
        assertEquals(1, tc.size());
    }

    private TextSplitter getTextSplitter() {
        return new TextSplitterOnWidth(100, ' ', new StringMetricsGraphics(this.getFontForText()));
    }

    private Font getFontForText() {
        return new Font("Arial", Font.PLAIN, 10);
    }

    /**
     * add 5 textareas. 4 on same x position. 1 on another.
     * get it back from x position.
     * restring the 4 textareas and check, if there are not overlapping.
     */
    @Test
    public void testTextColOrder_1() {
        TextCol tc = new TextCol();
        int lineHeight = 12;

        // add 5 textareas to col
        TextArea ta1 = new TextArea(0, 200, this.getTextSplitter(), lineHeight);
        ta1.addText("0 200 1");
        tc.add(ta1);

        TextArea ta2 = new TextArea(1, 0, this.getTextSplitter(), lineHeight);
        ta2.addText("1 0 2");
        tc.add(ta2);

        TextArea ta3 = new TextArea(1, 0, this.getTextSplitter(), lineHeight);
        ta3.addText("1 0 3");
        tc.add(ta3);

        TextArea ta4 = new TextArea(1, 50, this.getTextSplitter(), lineHeight);
        ta4.addText("1 50 4");
        tc.add(ta4);

        TextArea ta5 = new TextArea(1, 5, this.getTextSplitter(), lineHeight);
        ta5.addText("1 5 5");
        tc.add(ta5);

        // check getPosX function
        TextCol tcBack = tc.getPosX(1);
        assertEquals(4, tcBack.size());

        tcBack = tc.getPosX(0);
        assertEquals(1, tcBack.size());

        tcBack = tc.getPosX(56546);
        assertEquals(0, tcBack.size());

        for (TextArea ta : tcBack) {
            assertEquals(1, this.getTextSplitter().split(ta.getText()).size());
        }

        // check getPosX function
        tcBack = tc.doNotOverlap_moveDown();
        assertEquals(5, tcBack.size());

        assertEquals(0, tcBack.get(0).getPosY());
        assertEquals("1 0 2", tcBack.get(0).getText());

        assertEquals(12, tcBack.get(1).getPosY());
        assertEquals("1 0 3", tcBack.get(1).getText());

        assertEquals(24, tcBack.get(2).getPosY());
        assertEquals("1 5 5", tcBack.get(2).getText());

        assertEquals(50, tcBack.get(3).getPosY());
        assertEquals("1 50 4", tcBack.get(3).getText());

        assertEquals(200, tcBack.get(4).getPosY());
        assertEquals("0 200 1", tcBack.get(4).getText());
    }

    @Test
    public void testTextColOrder_2() {
        TextCol tc = new TextCol();
        int lineHeight = 17;

        // add 5 textareas to col
        TextArea ta1 = new TextArea(1, 22, this.getTextSplitter(), lineHeight);
        ta1.addText("foo");
        tc.add(ta1);

        assertEquals(17, ta1.getHeight());
        assertEquals(39, ta1.getButtom());

        TextArea ta2 = new TextArea(1, 22, this.getTextSplitter(), lineHeight);
        ta2.addText("bar");
        tc.add(ta2);


        TextCol tcBack = tc.doNotOverlap_moveDown();
        assertEquals(22, tcBack.get(0).getPosY());
        assertEquals(39, tcBack.get(1).getPosY());
    }
    @Test
    public void getTextSplittet() {
        TextArea ta1 = new TextArea(1, 22, this.getTextSplitter(), 12);
        ta1.addText("this is a very, very, very, very, very, very, very, very, very, very, very, very, very long string");
        List<String> splitted = ta1.getTextSplitted();
        assertTrue(splitted.size() > 1);
        // 5 or 6 - it depends on the plattform
        assertTrue(5 == splitted.size() || 6 == splitted.size());
    }
}

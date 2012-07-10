/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.knurt.fam.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.service.pdf.control.PDFOptionUtil;
import de.knurt.fam.service.pdf.control.bu.JSONObject2PDFOption;
import de.knurt.fam.service.pdf.model.PDFOption;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/config.xml" })
public class JSONObject2PDFOptionTest {

	public JSONObject2PDFOptionTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void empty() {
		JSONObject json = new JSONObject();
		PDFOption o = new JSONObject2PDFOption().process(json);
		assertEquals("unknown", o.getCustomId());
		assertEquals(PDFOptionUtil.me().getDefaultTemplateUrl(), o.getTemplateurl());
	}

	@Test
	public void templateurl() {
		JSONObject json = new JSONObject();
		try {
			json.put("templateurl", "/tmp/foo.pdf");
			PDFOption o = new JSONObject2PDFOption().process(json);
			assertEquals("/tmp/foo.pdf", o.getTemplateurl());
		} catch (JSONException e) {
			assertTrue(false);
		}
	}
	@Test
	public void secure_templateurl() {
		JSONObject json = new JSONObject();
		try {
			json.put("templateurl", "/../../etc/passwd");
			PDFOption o = new JSONObject2PDFOption().process(json);
			assertEquals(PDFOptionUtil.me().getDefaultTemplateUrl(), o.getTemplateurl());
		} catch (JSONException e) {
			assertTrue(false);
		}
	}
	@Test
	public void customid() {
		JSONObject json = new JSONObject();
		try {
			json.put("customid", "bla");
			PDFOption o = new JSONObject2PDFOption().process(json);
			assertEquals("bla", o.getCustomId());
		} catch (JSONException e) {
			assertTrue(false);
		}
	}
	@Test
	public void secure_customid() {
		JSONObject json = new JSONObject();
		try {
			json.put("customid", "/../../etc/passwd");
			PDFOption o = new JSONObject2PDFOption().process(json);
			assertEquals("unknown", o.getCustomId());
		} catch (JSONException e) {
			assertTrue(false);
		}
	}
	@Test
	public void textcontent() {
		JSONObject json = new JSONObject();
		JSONArray contents = new JSONArray();
		JSONObject c1 = new JSONObject();
		try {
			c1.put("pagenumber", 5);
			contents.put(c1);
			json.put("contents", contents);
			PDFOption o = new JSONObject2PDFOption().process(json);
			assertNotNull(o.getTextContents());
			assertEquals(1, o.getTextContents().size());
			assertEquals(5, o.getTextContents().get(0).getPageNumber());
		} catch (JSONException e) {
			assertTrue(false);
		}
	}
	@Test
	public void style() {
		JSONObject json = new JSONObject();
		JSONArray contents = new JSONArray();
		JSONObject c1 = new JSONObject();
		JSONObject style = new JSONObject();
		try {
			style.put("color", "#990099");
			style.put("height", "700");
			c1.put("style", style);
			contents.put(c1);
			json.put("contents", contents);
			PDFOption o = new JSONObject2PDFOption().process(json);
			assertNotNull(o.getTextContents());
			assertEquals(1, o.getTextContents().size());
			assertEquals(Color.decode("0x990099"), o.getTextContents().get(0).getFontColor());
			assertEquals("700", o.getTextContents().get(0).getStyle("height"));
		} catch (JSONException e) {
			assertTrue(false);
		}
	}
}

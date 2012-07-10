/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.knurt.fam.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Color;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.service.pdf.control.PDFOptionUtil;
import de.knurt.fam.service.pdf.model.PDFOption;
import de.knurt.fam.service.pdf.model.TextContent;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/config.xml" })
public class PDFOptionTest {

	public PDFOptionTest() {
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

	/**
	 * PDFOption: return a custom id in any case
	 */
	@Test
	public void hasCustomId() {
		PDFOption o = new PDFOption();
		assertEquals("unknown", o.getCustomId());
		o.setCustomId("foo");
		assertEquals("foo", o.getCustomId());
	}

	@Test
	public void getRightColor() {
		TextContent tc = new TextContent();
		tc.putStyle("color", "#FF0000");
		assertEquals(Color.RED, tc.getFontColor());
		tc.putStyle("color", "00FF00");
		assertEquals(Color.GREEN, tc.getFontColor());
	}

	/**
	 * PDFOption: return pages in any case
	 */
	@Test
	public void hasPages() {
		PDFOption o = new PDFOption();
		List<TextContent> pageoptions = o.getTextContents();
		assertNotNull(pageoptions);
		assertEquals(0, pageoptions.size());
		o.add(new TextContent());
		assertEquals(1, o.getTextContents().size());
	}

	/**
	 * PDFOption: return pagenumber (1) in any case
	 */
	@Test
	public void hasPagenumber() {
		TextContent o = new TextContent();
		assertEquals(1, o.getPageNumber());
		o.setPageNumber(2);
		assertEquals(2, o.getPageNumber());
	}

	@Test
	public void hasTemplateurl() {
		PDFOption o = new PDFOption();
		assertEquals(PDFOptionUtil.me().getDefaultTemplateUrl(), o.getTemplateurl());
		String str = "/tmp/foo.pdf";
		o.setTemplateurl(str);
		assertEquals(str, o.getTemplateurl());
	}
}

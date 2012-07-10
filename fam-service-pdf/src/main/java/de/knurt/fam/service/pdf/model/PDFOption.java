package de.knurt.fam.service.pdf.model;

import java.util.ArrayList;
import java.util.List;

import de.knurt.fam.service.pdf.control.PDFOptionUtil;

/**
 * pdf options. containing metadata, content and styles of a pdf document
 * 
 * @author Daniel Oltmanns
 * @since 1.4.0 (05/30/2011)
 */
public class PDFOption {
	private String customId = "unknown";
	private List<TextContent> textContents = new ArrayList<TextContent>();
	private String templateurl = null;

	public void setTemplateurl(String templateurl) {
		this.templateurl = templateurl;
	}

	public String getTemplateurl() {
		if (templateurl == null) {
			return PDFOptionUtil.me().getDefaultTemplateUrl();
		} else {
			return templateurl;
		}
	}

	public List<TextContent> getTextContents() {
		return textContents;
	}

	public void setCustomId(String customId) {
		this.customId = customId;
	}

	public String getCustomId() {
		return customId;
	}

	public void add(TextContent textContents) {
		this.textContents.add(textContents);
	}

	public void setTextContent(List<TextContent> textContents) {
		this.textContents = textContents;
	}
}
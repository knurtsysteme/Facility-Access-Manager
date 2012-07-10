package de.knurt.fam.service.pdf.control.bu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.service.pdf.control.ebc.BoardUnit;
import de.knurt.fam.service.pdf.model.PDFOption;
import de.knurt.fam.service.pdf.model.TextContent;

/**
 * create a pdf options from a request
 * 
 * @author Daniel Oltmanns
 * @since 1.4.0 (05/30/2011)
 */

public class JSONObject2PDFOption implements BoardUnit<JSONObject, PDFOption> {

	/**
	 * create and return a {@link PDFOption} from {@link JSONObject}
	 * representing the request. extract contents, styles and options from
	 * request. return a {@link PDFOption} in any case (blank page on totaly
	 * nonesense paramers). if templateurl given and is a local file on server,
	 * block using it. if customid matches directory changes ("../../") block
	 * using it as well.
	 */
	@Override
	public PDFOption process(JSONObject datum) {
		PDFOption result = new PDFOption();
		try {
			if (datum.has("templateurl")) {
				String val = datum.getString("templateurl");
				if (!val.matches(".*\\.\\..*") && !new File(val).isFile()) {
					result.setTemplateurl(val);
				}
			}
		} catch (JSONException e) {
			Logger.getRootLogger().fatal("201106071402");
		}
		try {
			if (datum.has("customid")) {
				String val = datum.getString("customid");
				if (!val.matches(".*\\.\\..*")) {
					result.setCustomId(datum.getString("customid"));
				}
			}
		} catch (JSONException e) {
			Logger.getRootLogger().fatal("201106071403");
		}
		result.setTextContent(this.getTextContents(datum));
		return result;
	}

	private List<TextContent> getTextContents(JSONObject datum) {
		List<TextContent> result = new ArrayList<TextContent>();
		try {
			if (datum.has("contents")) {
				JSONArray contents = datum.getJSONArray("contents");
				for (int i = 0; i < contents.length(); i++) {

					TextContent unit = new TextContent();

					JSONObject content = contents.getJSONObject(i);

					if (content.has("text")) {
						unit.setText(content.getString("text"));
					}

					if (content.has("pagenumber")) {
						unit.setPageNumber(content.getInt("pagenumber"));
					}

					if (content.has("style")) {
						JSONObject contentstyle = content.getJSONObject("style");
						String[] keys = JSONObject.getNames(contentstyle).clone();
						for (String key : keys) {
							unit.putStyle(key, contentstyle.get(key));
						}
					}

					result.add(unit);
				}
			}
		} catch (JSONException e) {
			Logger.getRootLogger().fatal("201106071418");
		}
		return result;
	}

}

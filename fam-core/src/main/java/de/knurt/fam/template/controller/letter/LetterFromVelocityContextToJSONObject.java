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
package de.knurt.fam.template.controller.letter;

import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.view.text.AccessGlobalTemplate;
import de.knurt.heinzelmann.util.nebc.BoardUnit;

/**
 * create a velocity context to use with templates used for fam-service-pdf
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.4.0 (06/13/2011)
 * 
 */
public class LetterFromVelocityContextToJSONObject implements BoardUnit<VelocityContext, JSONObject> {

  private String fileResourceNameOfJSONStyle;

  /**
   * construct a letter with the style defined in template folder <code>custom/letter_style.json</code>
   * 
   * @since 08.04.2014
   */
  public LetterFromVelocityContextToJSONObject() {
    this("custom/letter_style.json");
  }

  /**
   * construct a letter with the style defined in the given path
   * 
   * @since 08.04.2014
   * @param fileResourceNameOfJSONStyle path to a json file describing the style to use
   */
  public LetterFromVelocityContextToJSONObject(String fileResourceNameOfJSONStyle) {
    this.fileResourceNameOfJSONStyle = fileResourceNameOfJSONStyle;
  }

  /** {@inheritDoc} */
  @Override
  public JSONObject process(VelocityContext datum) {
    JSONObject result = new JSONObject();
    String parameters = AccessGlobalTemplate.getInstance().getContent(this.fileResourceNameOfJSONStyle, datum);
    try {
      result = new JSONObject(parameters);
    } catch (JSONException e) {
      FamLog.exception(e, 201106131354l);
    }
    return result;
  }

}

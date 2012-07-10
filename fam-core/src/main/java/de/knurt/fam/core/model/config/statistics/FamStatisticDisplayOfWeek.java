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
package de.knurt.fam.core.model.config.statistics;

/**
 * a week overview presented as a pic. this does only return the base name for
 * it.
 * 
 * @author Daniel Oltmanns
 * @since 0.20091011
 */
public class FamStatisticDisplayOfWeek extends FamStatisticPic {

	/** {@inheritDoc} */
	@Override
	public String getBaseSrcName() {
		return "statisticdisplayofweekimage";
	}

}

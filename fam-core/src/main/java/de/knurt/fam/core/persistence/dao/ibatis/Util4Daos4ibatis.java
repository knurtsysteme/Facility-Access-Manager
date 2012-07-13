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
package de.knurt.fam.core.persistence.dao.ibatis;

import java.text.SimpleDateFormat;

/**
 * some utils for the ibatis daos
 * 
 * @author Daniel Oltmanns
 * @since 1.8.0 (04/26/2012)
 */
class Util4Daos4ibatis {
	static final SimpleDateFormat SDF_4_DATE = new SimpleDateFormat("yyyy-MM-dd");
	static final SimpleDateFormat SDF_4_TIMESTAMP = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
}

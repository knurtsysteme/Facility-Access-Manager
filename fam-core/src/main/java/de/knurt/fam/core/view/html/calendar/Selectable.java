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
package de.knurt.fam.core.view.html.calendar;

import java.util.Calendar;

import de.knurt.heinzelmann.util.query.QueryString;

/**
 * a calendar must connect the different dates.
 * this is the interface for all of the html generators, 
 * that do this. mostly, this is done by a simple link.
 * @author Daniel Oltmanns
 * @since 0.20090516 (05/16/2009)
 */
interface Selectable {

    /**
     * return the {@link QueryString} of the given {@link Calendar}.
     * @param calendar given
     * @return the {@link QueryString} of the given {@link Calendar}.
     */
    public QueryString getQueryString(Calendar calendar);
}

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
package de.knurt.fam.core.util.time;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.util.mvc.QueryKeys;

/**
 * resolve the default calendar view of the given facility.
 * @author Daniel Oltmanns
 * @since 0.20090930
 */
public class CalendarViewResolver {

    /** one and only instance of me */
    private volatile static CalendarViewResolver me;

    /** construct me */
    private CalendarViewResolver() {
    }

    /**
     * return the one and only instance of me
     * @return the one and only instance of me
     */
    public static CalendarViewResolver getInstance() {
        if (me == null) { // no instance so far
            synchronized (CalendarViewResolver.class) {
                if (me == null) { // still no instance so far
                    me = new CalendarViewResolver(); // the one and only
                }
            }
        }
        return me;
    }
    private Map<String, String> defaultView_onFacility = null;
    private String defaultViewOnNotDefined = null;

    /**
     * @param defaultView_onFacility the defaultView_onFacility to set
     */
    @Required
    public void setDefaultView_onFacility(Map<String, String> defaultView_onFacility) {
        this.defaultView_onFacility = defaultView_onFacility;
    }

    /**
     * return the default calendar view for the given facility as configured.
     * by now, the view is one of {@link QueryKeys#WEEK}, {@link QueryKeys#OVERVIEW} or {@link QueryKeys#MONTH}.
     * if facility is unknown, return default view of unknown facility as configured.
     * @param facility the default view is returned of
     * @return the default view for a facility
     */
    public String getDefaultCalendarView(Facility facility) {
        String result = this.defaultViewOnNotDefined;
        if (this.defaultView_onFacility.containsKey(facility.getKey())) {
            result = this.defaultView_onFacility.get(facility.getKey());
        }
        return result;
    }

    /**
     * @param defaultViewOnNotDefined the defaultViewOnNotDefined to set
     */
    @Required
    public void setDefaultViewOnNotDefined(String defaultViewOnNotDefined) {
        this.defaultViewOnNotDefined = defaultViewOnNotDefined;
    }
}

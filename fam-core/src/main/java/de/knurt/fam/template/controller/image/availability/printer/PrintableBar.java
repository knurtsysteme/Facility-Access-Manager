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
package de.knurt.fam.template.controller.image.availability.printer;

/**
 * a printable bar.
 * used e.g. in {@link FacilityAvailabilityPrinter} to print the bar of
 * availability of facilities.
 * @author Daniel Oltmanns <info@knurt.de>
 */
public interface PrintableBar {

    /**
     * return x pos of bar.
     * @return x pos of bar,
     */
    public int getBarX();

    /**
     * return y pos of bar.
     * @return y pos of bar,
     */
    public int getBarY();

    /**
     * return width of bar.
     * @return width of bar,
     */
    public int getBarWidth();

    /**
     * return height of bar.
     * @return height of bar,
     */
    public int getBarHeight();

    /**
     * return true, if the bar is horizontal. return false, if it is vertical.
     * @return true, if the bar is horizontal. return false, if it is vertical.
     */
    public boolean isHorizontal();
}

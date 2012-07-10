/*!  JavaScript Scrollwork, version 0.20101005
 *  (c) 2010 KNURT Systeme
 *
 * This file is part of JavaScript Scrollwork.
 * For details, see http://www.knurt.de/default/javascript-scrollwork
 *
 * JavaScript Scrollwork is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaScript Scrollwork is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaScript Scrollwork.  If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 */

var SWRegExp = {
        is : {
            nothingOrWhitespace : function(string)
            {
                if(string)
                    return string.search(/^(\s)*$/) != -1;
                else
                    return true;
            },

            regularEmail : function(string)
            {
                return string.match(/^.+@.+\..+$/) && ! SWRegExp.contains.whitespace(string);
            },

            date_YYYYMMDD : function(string)
            {
                result = false;
                day = string.substring(6, 8);
                month = string.substring(4, 6);
                year = string.substring(0, 4);

                // months and years
                if(	month.match(/^01$/) ||
                    month.match(/^03$/) ||
                    month.match(/^05$/) ||
                    month.match(/^07$/) ||
                    month.match(/^08$/) ||
                    month.match(/^10$/) ||
                    month.match(/^12$/)) // month with max 31 days
                    {
                    result = day.match(/^[0-3][0-9]$/) && parseInt(day) > 0 && parseInt(day) < 32;
                }
                else if(	month.match(/^01$/) ||
                    month.match(/^04$/) ||
                    month.match(/^06$/) ||
                    month.match(/^09$/) ||
                    month.match(/^11$/)) // month with max 30 days
                    {
                    result = day.match(/^[0-3][0-9]$/) && parseInt(day) > 0 && parseInt(day) < 31;
                }
                else if(month.match(/^02$/))// february
                {
                    if(parseInt(year) % 4 == 0) // 	leap-year
                    {
                        result = day.match(/^[0-2][0-9]$/) && parseInt(day) > 0 && parseInt(day) < 30;
                    }
                    else
                    {
                        result = day.match(/^[0-2][0-9]$/) && parseInt(day) > 0 && parseInt(day) < 29;
                    }
                }
                else // invalid month
                {
                    result = false;
                }

                // check year
                if(result == true)
                {
                    result = year.search(/^[0-9][0-9][0-9][0-9]$/) != -1;
                }
                return result;
            }
        },
        contains :
        {
            whitespace : function(string)
            {
                if(string)
                    return string != "" && string.search(/(\s)+/) != -1;
                else
                    return false;
            }
        }
    };
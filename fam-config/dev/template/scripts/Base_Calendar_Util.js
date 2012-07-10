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
/*! KNURT Systeme - part of the Facility Access Manager */
Base.Calendar = Base.Calendar || {};
Base.Calendar.Util = {};
Base.Calendar.Util.getDayAsText = function(date) {
	return date.format("dddd mmmm dd yyyy");
};
Base.Calendar.Util.getUnitsLabelAsText = function(units, emptyOnFacilityLabel, withNumber) {
	emptyOnFacilityLabel = emptyOnFacilityLabel == "undefined" ? true : emptyOnFacilityLabel;
	withNumber = withNumber == "undefined" ? false : withNumber;
	var result = units == 1 ? BookingRule.capacity_label_singular : BookingRule.capacity_label_plural;
	if(emptyOnFacilityLabel && result == Facility.label) {
		result = "";
	}
	if(withNumber) {
		result = units + " " + result;
	}
	return $.trim(result);
};
Base.Calendar.Util.getWeekAsText = function(date) {
	return date.getWeekOfTheYear() + " CW " + date.format("yyyy");
};
Base.Calendar.Util.getMonthAsText = function(date) {
	return date.format("mmmm yyyy");
};
Base.Calendar.Util.dateAsJson = function(date) {
	var result = {};
	result.month = date.getMonth();
	result.year = date.getFullYear();
	result.day_of_month = date.getDate();
	return result;
};

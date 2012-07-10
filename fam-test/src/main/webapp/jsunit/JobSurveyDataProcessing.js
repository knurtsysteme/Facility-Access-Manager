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
var JobDataProcessing = {};
JobDataProcessing.getStructure = function(callback_succ, callback_error, facilityKey, useParent, parseVelocity) {
	useParent = useParent || true;
	parseVelocity = parseVelocity || true;
	var data = {};
	data.useParent = useParent;
	data.parseVelocity = parseVelocity;
	data.facilityKey = facilityKey;
	$.ajax({
		type : 'GET',
		url : 'do-jobsurveygetter.json',
		dataType : 'json',
		data : "data=" + JSON.stringify(data),
		success : function(r) {
			if(r.succ) {
				callback_succ(r);
			} else {
				callback_error(r);
			}
		},
		error : function(r) {
			callback_error(r);
		}
	});
}

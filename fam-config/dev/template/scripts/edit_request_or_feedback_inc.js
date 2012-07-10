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
var STEP = Booking.step || 0;
$(document).ready(function(){
	$('button#jobsurvey_update').click(function(){
		Base.WaitingIcon.showOnPage();
		JobSurveyIO.post({
			step: STEP,
			succ: function(r) {
				$.n.success("Job Survey has been updated!"); // INTLANG
				$('.show_after_update').show('slow');
				Base.WaitingIcon.hideOnPage();
			},
			fail: function(r) {
				Base.WaitingIcon.hideOnPage();
				if(r && r.reason) {
					$.n.error("Fails! Reason: " + r.reason); // INTLANG
				} else {
					$.n.error("please report error 201203291155"); // INTLANG
				}
			}, 
			error: function(r) {
				Base.WaitingIcon.hideOnPage();
				$.n.error("please report error 201203291154"); // INTLANG
			},
			invalid: function() {
				Base.WaitingIcon.hideOnPage();
			}
		});
	});
	
	JobDataProcessing.getStructure(
		function(result) { // callback_succ
			if(result.has_job_data_processing) {
				$('#input_idJobDataProcessing').val(result.idJobDataProcessing);
				if(result.templates && result.templates[STEP] && result.templates[STEP].behaviour) { // may have no template
					try {
					jQuery.globalEval(result.templates[STEP].behaviour);
					}catch(err) {
					}
				}
			}
		}, function(result) { // callback_error
			$.n.error("please report error 201203291210"); // INTLANG
		},
		Booking.facilityKey, true, true
	);

});

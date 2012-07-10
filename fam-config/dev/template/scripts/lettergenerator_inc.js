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
if (typeof (Base) == "undefined") {
	throw "this needs base_jq.js";
}
if (typeof (JSON) == "undefined") {
	throw "this needs json2.js";
}
$(document).ready(function() {
	LetterGenerator.checkEmailForm();
	$('input[name=event]').bind("change", function() {
		LetterGenerator.checkEmailForm();
	});
	LetterGenerator.submitAction();
});
var LetterGenerator = {};
LetterGenerator.checkEmailForm = function() {
	$('#email_form').show();
	if ($('#event_email_id:checked').length == 1) {
		$('#invoiced_id').attr('checked', true);
		$('#email_form').show('slow');
	} else {
		$('#invoiced_id').attr('checked', false);
		$('#email_form').hide('slow');
	}
};
LetterGenerator.submitAction = function() {
	$('#letterform').bind('submit', function() {
		if ($('#event_email_id:checked').length == 1) {
			var label_before = $("#letterform button[type=submit]").html();
			Base.WaitingIcon.show($("#letterform button[type=submit]"));
			$.ajax( {
				type : 'POST',
				url : 'send-2-lettergenerator.json',
				data : $(this).serialize(),
				success : function(answer) {
					if (answer && answer.succ) {
						if(answer.invoiced) {
							$('#info_last_invoiced').html("Job has been invoiced!");
							$.n.success("E-Mail sent and job invoiced!");
						}
						else {
							$.n.success("E-Mail sent!");
						}
						$("#letterform button[type=submit]").html(label_before);
					} else if (answer && answer.errormessage) {
						$.n.error(answer.errormessage);
					} else {
						$.n.error("Unexpected Server-Answer (" + JSON.stringify(answer) + "). Please send this message to info@knurt.de [201106140941].");
					}
				},
				error : function(r) {
					$.n.error("Error 201106171026: " + r.status + " " + r.statusText);
				}
			});

			return false;
		} // else do nothing -> submit form
		});
};
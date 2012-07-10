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
/**
 * this is only for the page configjobsurvey.html!!!
 * for processing jobs use JobSurveyIO.js
 */
var JobSurvey = {};
JobSurvey.Text = {};
JobSurvey.Text.hasNoInput = "nothing so far";
JobSurvey.init = function() {
	$('#js_facility_choosen').change(function() {
		var continu = true;
		if(JobSurvey.status.formChanged) {
			continu = confirm("There are changes getting lost! Continue?");
		}
		if(continu) {
			$('#facility_selection').submit();
			Base.WaitingIcon.show($('#content_main'));
			Base.WaitingIcon.showOnPage();
		}
	});
	$('.jui_tabs').tabs();
	JobSurvey.Textarea.init();
	$('#container_show > .buttons > button.edit').click(function(i, el) {
		$('#container_show').hide("slide", {
			duration : "left"
		}, 500, function() {
			$('#container_edit').show("slide", {
				duration : "left"
			}, 500);
		});
	});
	$('#back_2_show').click(function(i, el) {
		$('#container_edit').hide("slide", {
			duration : "left"
		}, 500, function() {
			$('#container_show').show("slide", {
				duration : "left"
			}, 500);
		});
	});
	$('#back_2_edit').click(function(i, el) {
		$('#container_preview').hide("slide", {
			duration : "left"
		}, 500, function() {
			$('#container_edit').show("slide", {
				duration : "left"
			}, 500);
		});
	});
	$('#action_preview').click(JobSurvey.IO.showPreview);
	$('#action_publish').click(JobSurvey.IO.publish);
	$('#container_edit, #container_preview').hide();
};
JobSurvey.status = {};
JobSurvey.status.formChanged = false;
JobSurvey.Textarea = {};
JobSurvey.Textarea.init = function() {
	$('textarea').keyup(function() {
		JobSurvey.status.formChanged = true;
		if(JobSurvey.Textarea.hasInput($(this))) {
			$(this).removeClass('has_no_input').addClass("has_input");
		} else {
			$(this).removeClass('has_input').addClass("has_no_input");
		}
	}).focus(function() {
		if(!JobSurvey.Textarea.hasInput($(this))) {
			$(this).val("");
		}
	}).blur(function() {
		if(!JobSurvey.Textarea.hasInput($(this))) {
			$(this).val(JobSurvey.Text.hasNoInput);
		}
	});
	$('textarea').each(function(i, textarea) {
		if(JobSurvey.Textarea.hasInput($(textarea))) {
			$(textarea).removeClass('has_no_input').addClass("has_input");
		} else {
			$(textarea).val(JobSurvey.Text.hasNoInput).removeClass('has_input').addClass("has_no_input");
		}
	});
};
JobSurvey.Textarea.hasInput = function(textarea) {
	return $.trim($(textarea).val()) != "" && $.trim($(textarea).val()) != JobSurvey.Text.hasNoInput;
};
JobSurvey.IO = {};
JobSurvey.IO.getJobSurvey = function() {
	var result = {};
	result.facilityKey = $('#js_facility_choosen').val();
	result.templates = [];
	var step = 0;
	while(step < 4) {
		result.templates[step] = {};
		result.templates[step].step = step;
		result.templates[step].structure = JobSurvey.Textarea.hasInput($('#structure_step_' + step)) ? $('#structure_step_' + step).val() : null;
		result.templates[step].behaviour = JobSurvey.Textarea.hasInput($('#behaviour_step_' + step)) ? $('#behaviour_step_' + step).val() : null;
		step++;
	}
	return result;
};
JobSurvey.IO.showPreview = function() {
	$('#container_edit').hide("slide", {
		duration : "left"
	}, 500, function() {
		$.ajax({
			type : 'POST',
			url : 'go-jobsurveypreviewsessionstore.json',
			contentType : "application/json; charset=utf-8",
			dataType : "json",
			data : JSON.stringify(JobSurvey.IO.getJobSurvey()),
			success : function(r) {
				if(r.succ && r.articleno) {
					$('#preview_jobsurvey_0').attr('src', '/fam-core/get-jobsurveypreview.html?step=0&articleno=' + r.articleno)
					$('#preview_jobsurvey_1').attr('src', '/fam-core/get-jobsurveypreview.html?step=1&articleno=' + r.articleno)
					$('#preview_jobsurvey_2').attr('src', '/fam-core/get-jobsurveypreview.html?step=2&articleno=' + r.articleno)
					$('#preview_jobsurvey_3').attr('src', '/fam-core/get-jobsurveypreview.html?step=3&articleno=' + r.articleno)
					$('#container_preview').show("slide", {
						duration : "right"
					});
				} else {
					$.n.error("please report error 201111281451");
				}
			},
			error : function(r) {
				$.n.error("error [code 201111281452]: " + r.responseText);
			}
		});
	});
};
JobSurvey.IO.publish = function() {
	$.ajax({
		type : 'POST',
		url : 'go-jobsurveypublish.json',
		contentType : "application/json; charset=utf-8",
		dataType : "json",
		data : JSON.stringify(JobSurvey.IO.getJobSurvey()),
		success : function(r) {
			if(r.succ) {
				JobSurvey.status.formChanged = false;
				$.n.success("Your configuration has been publish");
				$('#action_reload').show().effect('highlight').click(function(){
					window.location=window.location;
				});
			} else {
				$.n.error("please report error 201112071657");
			}
		},
		error : function(r) {
			$.n.error("error [code 201112071656]: " + r.responseText);
		}
	});
};

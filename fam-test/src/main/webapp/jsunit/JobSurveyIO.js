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
JobSurveyIO = {};
JobSurveyIO.URL_GET_JOBSURVEY = "get-jobsurvey.html";
JobSurveyIO.URL_SAVE_JOBSURVEY = "save-jobsurvey.json";
JobSurveyIO.getIFrameSrc = function(facilityKey) {
	facilityKey = facilityKey || Facility.key;
	if(facilityKey) {
		return "{0}?step=0&facility={1}".format(JobSurveyIO.URL_GET_JOBSURVEY, facilityKey);
	} else {
		return null;
	}
};
/**
 * return true, if form can be sent.
 * otherwise do something (typically pointing
 * 	out the missing fields) and return false.
 * this is the method to override in behaviour
 * on data job processing definitions if user
 * inputs must match a specific forms.
 */
JobSurveyIO.validate = function() {
	return true;
};
/**
 * return the value of the job survey form.
 * by default use serializeObject called for the iframe of the page.
 * this is the method to override in behaviour
 * on data job processing definitions if user
 * inputs shall be modified before sending.
 */
JobSurveyIO.getValue = function() {
	return $('iframe').contents().find("html").serializeObject();
};
/**
 * select an element in the iframe.
 * this is useful to select elements in the behaviour form of
 * data job processing.
 */
JobSurveyIO.selector = function(selector) {
	return $(selector, $('iframe').contents().find("html"));
}
/**
 * shows a missing input effect on the given element
 */
JobSurveyIO.effectOnMissingInput = function(element) {
	if($(element).length == 0) {
		element = JobSurveyIO.selector(element);
	}
	$(element).effect('highlight', {
		color : '#ff3333'
	}, 3000);
};
/**
 * validate the form and post results to the server if form is valid. otherwise call callback function invalid.
 * options:
 * - <code>step</code> of data processing (0 for user input, 1 for operator answer ...). default 0
 * - <code>succ</code> callback function(response) for a success. default: <code>function(){AsDialog.show("Got it!", {title : "Post form"})};</code>
 * - <code>fail</code> callback function(response) for a data input failure. default: <code>function(){AsDialog.show("Fail!", {title : "Post form fails"})};</code>
 * - <code>error</code> callback function(response) for an unexpected server response. default: <code>function(){AsDialog.show("Fail!", {title : "Unexpected server response"})};</code>
 * - <code>invalid</code> callback function(response) for an unexpected server response. default: do nothing
 */
JobSurveyIO.post = function(options) {
	var forminputs = {};
	options = options || {};
	options.step = options.step || 0;
	options.succ = options.succ ||
	function() {
		AsDialog.show("Got it!", {
			title : "Post form"
		})
	};

	options.fail = options.fail ||
	function() {
		AsDialog.show("Fail!", {
			title : "Post form fails"
		})
	};

	options.error = options.error ||
	function() {
		AsDialog.show("Fail!", {
			title : "Unexpected server response"
		})
	};
	options.invalid = options.invalid || function() {};
	options.valid = options.valid || function() {};

	if(JobSurveyIO.validate()) {
		forminputs.main = $("html").serializeObject();
		forminputs.main.step = options.step;
		if(Booking.article_number && !forminputs.main.v) {
			forminputs.main.v = Booking.article_number;
		}
		forminputs.jobSurvey = JobSurveyIO.getValue();
		$.ajax({
			type : 'POST',
			url : JobSurveyIO.URL_SAVE_JOBSURVEY,
			dataType : "json",
			data : JSON.stringify(forminputs),
			success : function(r) {
				if(r && r.succ) {
					options.succ(r);
				} else {
					options.fail(r);
				}
			},
			error : function(r) {
				options.error(r);
			}
		});
	} else {
		options.invalid();
	}
};

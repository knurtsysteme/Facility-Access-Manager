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
var JobSurveyFileUpload = {};
JobSurveyFileUpload.RELOAD_BUTTON = '<button class="icon reload" onclick="javascript:JobSurveyFileUpload.init();"><span class="image"></span>reload</button>';
JobSurveyFileUpload.init = function() {
	var selects = 0;
	var select_html = '';
	var checked_html = '';
	if(typeof(FileUpload) == 'object') {
		if(FileUpload.selector !== null && $(FileUpload.selector).length == 1) {
			FileUpload.suffixes = FileUpload.suffixes || null;
			FileUpload.min_files = FileUpload.min_files || 1;
			FileUpload.max_files = FileUpload.max_files || 1;
			Base.WaitingIcon.show($(FileUpload.selector));
			$.ajax({
				type : 'GET',
				url : 'put-fileupload.json',
				success : function(files) {
					$(FileUpload.selector).html("");
					$(files).each(function(j, file) {
						if(FileUpload.suffixes === null || jQuery.inArray(file.name.split('.').pop(), FileUpload.suffixes) >= 0) {
							selects++;
							checked_html = '';
							if(selects <= FileUpload.min_files) {
								checked_html = ' checked="checked"';
							}
							select_html += '<li><input type="{2}" name="attachments" id="attachment_{3}" value="{0}" onchange="javascript:JobSurveyFileUpload.update(this);"{4} />&nbsp;<label for="attachment_{3}">{0}</label> (<a href="{1}">download</a>)</li>'.format(file.name, file.url, JobSurveyFileUpload.getInputType(), selects, checked_html);
						}
					});
					if(selects >= FileUpload.min_files) {
						$(FileUpload.selector).append('<ul class="attachments">{0}</ul>'.format(select_html));
						$(FileUpload.selector).append('Not there want you want? <a href="my-filemanager.html" target="_target">Add files here</a> and click {0}!'.format(JobSurveyFileUpload.RELOAD_BUTTON));
						if(selects == FileUpload.min_files) {
							$("input:checked", FileUpload.selector).attr("checked", true);
						}
					} else {
						$(FileUpload.selector).append('<div class="warning">You do not have enough files requested for this Job Survey?<br /><a href="my-filemanager.html" target="_target">Please upload matching files here</a> and click {0}!</div>'.format(JobSurveyFileUpload.RELOAD_BUTTON));
					}
				},
				error : function(r) {
					AsDialog.show("Error", {title: "There are network problems [code 201204191022]: " + r.responseText});
				}
			});
		}
	}
};
JobSurveyFileUpload.getInputType = function() {
	return (FileUpload.min_files == FileUpload.max_files && FileUpload.min_files == 1) ? "radio" : "checkbox";
};
JobSurveyFileUpload.update = function(input) {
	var title, message = "";
	if(JobSurveyFileUpload.getInputType() == "checkbox") {
		if($("input:checked", FileUpload.selector).length > FileUpload.max_files) {
			title = "Only {0} {1} allowed".format(FileUpload.max_files, FileUpload.max_files == 1 ? "file" : "files");
			message = "Please deactivate another file first";
			AsDialog.show(message, {title: title});
			$(input).attr("checked", false);
		} else if($("input:checked", FileUpload.selector).length < FileUpload.min_files) {
			title = "At least {0} {1} needed".format(FileUpload.min_files, FileUpload.min_files == 1 ? "file" : "files");
			message = "Please activate another file first";
			AsDialog.show(message, {title: title});
			$(input).attr("checked", true);
		}
	}
};
$(document).ready(function() {
	JobSurveyFileUpload.init();
});
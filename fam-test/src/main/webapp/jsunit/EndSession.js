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
// end session ahead of a schedulre on forms $("form.stopCurrentSession")
$(document).ready(function() {
	var es = new EndSession();
	es.init();
});
var EndSession = function() {
	function postAndShowAnswer(e) {
		console.log($(e.currentTarget).serializeObject());
		$.ajax( {
			type : 'POST',
			url : 'endsession.json',
			contentType : "application/json; charset=utf-8",
			dataType : "json",
			data : JSON.stringify($(e.currentTarget).serializeObject()),
			success : function(r) {
				if (r.succ) {
					if ($("form.stopCurrentSession").length == 1) {
						$(".stopCurrentSession").hide('slow', function() {
							$(this).remove();
						});
					} else {
						$(e.currentTarget).hide('slow', function() {
							$(this).remove();
						});
					}
					$.n.success(r.succmessage);
				} else if (r.errormessage) {
					$.n.error(r.errormessage);
				} else {
					$.n.error("please report error 201207031203");
				}
			},
			error : function(r) {
				$.n.error("error [code 201207031204]: " + r.responseText);
			}
		});
		return false;
	}
	this.init = function() {
		$("form.stopCurrentSession").submit(postAndShowAnswer);
	}
};
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
Base.Base_NoticeUpdate = {};
Base.Base_NoticeUpdate.showDialog = function(booking_id, notice_value, callback_suc) {
	callback_suc = callback_suc ||
	function(r) {
		$.n.success(r.message);
	};
	$('#jedit_notice_of_booking_text').html(notice_value);
	$('#jedit_notice_of_booking').dialog({
		modal : true,
		title : "Update notice", // INTLANG
		width : "800px",
		closeOnEscape : false,
		close : function(event, ui) {
		},
		buttons : {
			'Update Notice' : function() {
				$.ajax({
					type : 'POST',
					url : "do-bookingnoticeupdate.json",
					dataType : 'json',
					data : "v=" + booking_id + "&w=" + encodeURIComponent($('#jedit_notice_of_booking_text').val()),
					success : callback_suc,
					error : function(r) {
						$.n.error("please report error 201203080904");
					}
				});
				$(this).dialog('close');
			},
			'Cancel' : function() {
				$(this).dialog('close');
			}
		}
	});
};

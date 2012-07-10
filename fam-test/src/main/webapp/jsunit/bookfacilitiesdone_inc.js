$(document).ready(function() {
	$('#edit_notice').click(function() {
		Base.Base_NoticeUpdate.showDialog($.url.param("jobId"), $('#notice_value').html(), function(r) {
			$('#notice_value').html($('#jedit_notice_of_booking_text').val());
			$.n.success(r.message);
		});
	});
});

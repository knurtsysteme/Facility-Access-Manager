TransferBooking = {};
TransferBooking.init = function() {
  $('#transferBookingForm').submit(function() {
    $.ajax({
      type : "POST",
      url : "post-transferbooking.html",
      data : $('#transferBookingForm').serialize(),
      success : function(answer) {
        if (answer.succ) {
          $.n.success("Booking successful transfered to user"); // INTLANG
          $('#transferBookingForm').hide('slow');
        } else {
          $.n.error(answer.errormessage);
        }
      }
    });
    return false;
  });
}

$(document).ready(function() {
  TransferBooking.init();
});
var FamPrintButton = function() {

  var getPrintButton = function() {
    var style = {};
    style.position = 'fixed';
    style.zIndex = 9999999;
    style.height = '50px';
    style.top = 0;
    style.right = 0;
    var button = $('<button />').css(style).addClass('icon').addClass('print').html('Print').click(window.print);
    return button;
  };

  this.show = function() {
    $('body').prepend(getPrintButton());
  };
};

var famPB = new FamPrintButton();
if (top.location == self.location) {
  $(document).ready(famPB.show);
}
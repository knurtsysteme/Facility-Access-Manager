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
$(document).ready(function() {
	QuicksandControl.init();
});

var QuicksandControl = {
	clickItems : null,
	init : function() {
		if (!($.browser.msie && parseInt($.browser.version) < 7) && Base.useJavaScript()) {

			// prepare content panel
	$('#left_block').hide();
	$('#right_block').hide();
	$('#quicksand').show();

	// style quicksand
	$('#quicksand_clickitems');
	$('#quicksand_clickitems li').css("display", "inline-block").css("text-align", "center").css("width", "50px").css("margin", "20px 20px 0px 20px").css("cursor", "pointer").css("vertical-align", "top").css("font-size", "13px");

	// prepare quicksand
	QuicksandControl.clickItems = $('#quicksand_clickitems').clone();
	var $tabs = $('#jui_tabs').tabs( {
		select : function(event, ui) {
			var filteredData = QuicksandControl.clickItems.find('li.' + $("input", $(ui.tab).parent()).attr("value"));
			$('#quicksand_clickitems').quicksand(filteredData, {
				duration : 800,
				attribute : 'id',
				easing : 'easeInOutQuad'
			}, QuicksandControl.initClickItems);
			return true;
		}
	});
	QuicksandControl.initClickItems();
}
},
initClickItems : function() {
// add bubbles to quicksend click items
	$('#quicksand_clickitems li').each(function() {
		$(this).CreateBubblePopup( {
			innerHtml : $("img", this).attr("alt"),
			position : 'top',
			align : 'bottom',
			themeName : 'black',
			hideTail : false,
			width : '200px',
			themePath : 'jquerybubblepopup-theme/'
		});
	});
	// fix ie 7 - break after 7 clickitems
	if ($.browser.msie && parseInt($.browser.version) == 7) {
		$('#quicksand_clickitems li').css("display", "inline").css("float", "left").css("clear", "none").css("height", "100px");
		$('#quicksand_clickitems li').each(function(i, li) {
			if (i % 6 == 0) {
				$(li).css("clear", "left");
			}
		});
	}
}
}

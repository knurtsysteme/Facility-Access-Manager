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
$.ajaxSetup({
	cache : false
});
$(document).ready(function() {
	Base.init();
});
var Base = {};
Base.useJavaScript = function() {
	var result = true;
	if($.browser.mozilla && parseFloat($.browser.version) < 1.8) {
		result = false;
	} else if($.browser.msie && parseInt($.browser.version) < 6) {
		result = false;
	}
	return result;
};
Base.images = {};
Base.images.help = new Image();
Base.images.help.src = "icons/help-browser.png";
Base.images.help.w = 16;
Base.images.help.h = 16;
Base.images.wait = new Image();
Base.images.wait.src = "icons/wait.gif";
Base.images.wait.width = 16;
Base.images.wait.height = 9;
Base.images.loading = new Image();
Base.images.loading.src = "icons/loading.gif";
Base.images.loading.width = 128;
Base.images.loading.height = 128;
Base.init = function() {
	$('.js_hide').hide();
	$('.js_show').show();
	$('#news_notification').show();
	try { Base.NewsNotification.init(); } catch(e){$('#news_notification').hide();};
	$('button.icon span.image,span.icon span.image').remove();
	$('button.icon,span.icon').prepend("<span class='image'></span>");
	// ↓ show help
	$(Base.images.help).attr('height', Base.images.help);
	$(Base.images.help).attr('width', Base.images.help);
	$(Base.images.help).css({
		'width' : Base.images.help.w + "px",
		'height' : Base.images.help.h + "px"
	});
	$('.js_help').each(function(i, el) {
		$(el).html("<span style=\"display: none;\">" + $(el).html() + "</span>").css({
			"cursor" : "pointer",
			"position" : "relative",
			"top" : "3px"
		});
	});
	$('.js_help > span').before(Base.images.help);
	$('.js_help').click(function() {
		var tmp = $('<span />').html(Base.images.help).append(" Help");
		AsDialog.show($('span', this).html(), {
			title : tmp
		});
	});
	$('.js_help').show();

	if($.browser.msie && parseInt($.browser.version) == 6) {
		$('.js_hideIE').hide();
		$('.js_showIE').show();
	}

	$('#famjs_ilr_show').hide();
	$('#famjs_ilr_hide').hide();
};
Base.NewsNotification = {};
Base.NewsNotification.store = false;
Base.NewsNotification.init = function() {
	/* news set by the server.
	 * this is simply the count of news got from server - even if read or not or something ...
	 */
	var news_count = $('#news_notification').html();
	var news_count_read = news_count;
	var tmp;
	
	/*
	 * set the last update of the news (if available)
	 */
	if(LastNewsUpdate) {
		$.cookie("LastNewsUpdate", LastNewsUpdate);
	}
	
	/*
	 * diff news item ids in cookie with ids shown.
	 * decrement news_notification if id already in cookie.
	 */
	var news_ids_shown = [];
	var news_ids_stored = $.cookie("nis") ? $.cookie("nis").split(",") : [];
	$('#famtab_news tr').each(function(i,ni) {
		news_ids_shown.push($(ni).attr("id"));
	});
	$(news_ids_shown).each(function(i,news_id_shown) {
		if(news_ids_stored.indexOf(news_id_shown) >= 0) {
			news_count_read--;
		} else {
			$('#'+news_id_shown).addClass("new");
		}
	});
	$('#news_notification').html(news_count_read);
	if(news_count_read == 0) {
		$('#news_notification').remove();
	}
	
	/*
	 * on 2 seconds shown store news ids in cookie and
	 * hide notification.
	 */
	$($('#famtab_news').parent()).hover(
		function(){
			if($('#news_notification')) {
				Base.NewsNotification.store = true;
				setTimeout(function(){
					if(Base.NewsNotification.store) {
						$('#famtab_news tr').each(function(i,ni) {
							tmp = $(ni).attr("id");
							if(news_ids_stored.indexOf(tmp) < 0) {
								news_ids_stored.push(tmp);
							}
						});
						$.cookie("nis", news_ids_stored, { expires: 1 });
						$('#news_notification').fadeOut('slow', this.remove);
					}
				}, 1000);
			}
		},
		function(){
			Base.NewsNotification.store = false;
		}
	);
	/*
	 * aktion suche cookie, vergleiche alle gespeicherten news mit den angezeigten news.
	 * blende ggf. die notification aus
	 * 
	 * aktion news-tap onhover - setTimeout 2 sek und dann:
	 * blende den news cound aus.
	 * gehe alle vorhandenen nachrichten durch, speichere deren id in einen cookie
	 */
};
Base.iFrameHeightInterval = null;
Base.setIFrameHeightInterval = function() {
	if(Base.iFrameHeightInterval) {
		window.clearInterval(Base.iFrameHeightInterval);
	}
	Base.iFrameHeightInterval = window.setInterval(function() {
		$("iframe").each(function(i, e) {
				if($(e).contents().find("body").length > 0) {
					var new_height = $(this).contents().find("body").height() + 30;
					$(e).css('overflow', 'visible').height(new_height);
				} else {
					$(e).height(200).css('overflow', 'scroll');
				}
		});
	}, 1000);
};

Base.WaitingIcon = {};
Base.WaitingIcon.show = function(el) {
	$(Base.images.wait).attr('height', Base.images.wait.height);
	$(Base.images.wait).attr('width', Base.images.wait.width);
	$(Base.images.wait).css({
		'width' : Base.images.wait.width + "px",
		'height' : Base.images.wait.height + "px"
	});
	el.html(Base.images.wait);
};
/**
 * time to show icons at least in seconds
 */
Base.WaitingIcon.showAtLeast=1;
/**
 * container to save timestamps
 */
Base.WaitingIcon.timeShown=null;
/**
 * show a loading image on the entire page
 */
Base.WaitingIcon.showOnPage = function() {
	if($('#page_waiting_icon').length == 0) {
		$(document.body).append("<div id='page_waiting_icon'><span></span></div>");
		$('#page_waiting_icon').css({
		    'background-color': 'black',
		    'height': '100%',
		    'width': '100%',
		    'opacity': 0.5,
		    'position': 'absolute',
		    'text-align': 'center',
		    'top': 0,
		    'width': '100%',
		    'z-index': 10000
		});
		$(Base.images.loading).attr('height', Base.images.loading.height);
		$(Base.images.loading).attr('width', Base.images.loading.width);
		$(Base.images.loading).css({
			'width' : Base.images.loading.width + "px",
			'height' : Base.images.loading.height + "px"
		});
		$('#page_waiting_icon span').css({
			top : '50%',
			position: 'relative',
			marginTop: Base.images.loading.height / -2 + "px"
		}).html(Base.images.loading);
	}
	$('#page_waiting_icon').show().css('top', $(window).scrollTop() + "px");
	$(document.body).css('overflow', 'hidden');
	Base.WaitingIcon.timeShown=new Date().getTime();
};
/**
 * hide the loading image on the entire page shown with Base.WaitingIcon.showOnPage.
 */
Base.WaitingIcon.hideOnPage = function() {
	if(Base.WaitingIcon.timeShown + Base.WaitingIcon.showAtLeast * 1000 <= new Date().getTime()) {
		$('#page_waiting_icon').hide();
		$(document.body).css('overflow', 'visible');
	} else {
		window.setTimeout(Base.WaitingIcon.hideOnPage,500);
	}
};

Base.Calendar = {};
Base.Calendar.getPopUpCalendar = function() {
	calendar = new CalendarPopup();
	calendar = Base.Calendar.privateGet(calendar);
	return calendar;
};
Base.Calendar.privateGet = function(calendar) {
	calendar.showYearNavigation();
	calendar.setCssPrefix("das_");
	// INTLANG : cal.setMonthAbbreviations("Jan","Feb","Mar",...);
	// INTLANG : cal.setMonthNames("January","February","March",...);
	// INTLANG : cal.setDayHeaders("S","M","T",...);
	// INTLANG : cal.setTodayText("Today");
	return calendar;
};
Base.hide = function(el, callback) {
	callback = callback ||
	function() {
	};
	el.hide('slow');
	callback();
}
Base.show = function(el, callback) {
	callback = callback ||
	function() {
	};


	el.show('slow');
	callback();
}

Base.nl2br = function(raw) {
	return (raw + '').replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g, '$1<br />$2');
}
var DocumentAction = {};

DocumentAction.createTabs = function() {
	return $('#jui_tabs').tabs();
};

DocumentAction.messageInit = function() {
	var FamStatus = FamStatus || false;
	if(FamStatus) {
		alert(FamStatus);
	}
};

DocumentAction.showTinyMCE = function() {
	$('textarea.tinymce').tinymce({
		script_url : 'tiny_mce/tiny_mce.js',
		theme : "advanced",
		plugins : "pagebreak,style,layer,table,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,advlist",
		theme_advanced_buttons1 : "bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,styleselect,formatselect,fontselect,fontsizeselect",
		theme_advanced_buttons2 : "cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,cleanup,help,code,|,insertdate,inserttime,|,forecolor,backcolor",
		theme_advanced_buttons3 : "tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,emotions,iespell,media,advhr,|,print,|,ltr,rtl,|,fullscreen",
		theme_advanced_toolbar_location : "top",
		theme_advanced_toolbar_align : "left",
		theme_advanced_statusbar_location : "bottom",
		theme_advanced_resizing : true
	});
};
var AsDialog = {};
AsDialog.show = function(message, options) {
	var options = options || {};
	options.draggable = options.draggable || true;
	options.modal = options.modal || true;
	options.title = options.title || "Message";
	// INTLANG
	options.buttons = options.buttons || {
		'OK' : function() {
			$(this).dialog('close');
		}
	};
	if($('#as_dialog').length == 0) $(document.body).append('<div id="as_dialog"></div>');
	AsDialog.dialog = $('#as_dialog').clone().attr("id", "").html(message).show().dialog(options);
};
AsDialog.dialog = null;

var AsTable = {
	init : {
		dataTables : function(options) {
			var dataTables = $('#content_main table.standard');
			var options = options || ( typeof (SortTableOptions) == "undefined" ? {} : SortTableOptions);
			options.sDom = options.sDom || '<"top"pirlf>t';
			options.sPaginationType = options.sPaginationType || "full_numbers";
			options.aLengthMenu = options.aLengthMenu || [[5, 10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]];
			options.oLanguage = options.oLanguage || {
				"sLengthMenu" : "Display _MENU_ records per page",
				"sZeroRecords" : "Nothing found",
				"sInfo" : "_START_ to _END_ of _TOTAL_",
				"sInfoEmpty" : "0 to 0 of 0",
				"sInfoFiltered" : "(filtered from _MAX_ total)"
			};
			return dataTables.dataTable(options);
		},
		buttonConfirmation : function(parent_element) {
			var TableOptions = TableOptions || {};
			TableOptions.reloadPageOnSent = TableOptions.reloadPageOnSent || false;
			TableOptions.dialogMessage = TableOptions.dialogMessage || "<div>Do you really want to proceed?</div>";
			// INTLANG
			if(TableOptions.reloadPageOnSent == false) {
				$(parent_element + ' form').submit(function() {
					return false;
				});
				$(parent_element + ' form button').click(function(event) {
					var $buttonClickedOn = $(this);
					var $form = $buttonClickedOn.parents("form");
					if($form.length == 0) {
						alert("fehler 201008121151");
					} else {
						AsDialog.show(TableOptions.dialogMessage, {
							draggable : true,
							modal : true,
							title : TableOptions.dialogTitle,
							buttons : {
								'Cancel' : function() {
									$(this).dialog('close');
								},
								'Yes' : function() {
									$(this).dialog('close');
									var parameters = $form.serialize() + "&" + $buttonClickedOn.get(0).name + "=" + $buttonClickedOn.get(0).value;
									var url2contact = window.location.href;
									Base.WaitingIcon.show($form);
									$.ajax({
										type : 'POST',
										url : url2contact,
										data : parameters,
										success : function(r) {
											$form.html("<p>Changed!</p><p><a href=\"" + window.location.pathname + window.location.search + "\">Click here to refresh page.</a></p>");
										},
										error : function(r) {
											$('#as_dialog').clone().attr("id", "").html("error [code 200908141438]: " + r.responseText).show().dialog();
										}
									});
								}
							}
						});
						$('.ui-dialog-buttonpane :button').each(function() {
							if($(this).text() == 'Cancel') {
								$(this).attr("id", "jqdialog_cancel_button");
							}
							if($(this).text() == 'Yes') {
								$(this).attr("id", "jqdialog_yes_button");
							}
						});
					}
					return false;
				});
			}
		}
	}
};

var AsCouch = {};

AsCouch.doc = function(doc_id, callback) {
	if(callback != null && doc_id != null) {
		$.ajax({
			type : 'GET',
			url : "get-publicdoc.json",
			data : "doc=" + doc_id,
			success : function(r) {
				callback(r);
			},
			error : function() {
				callback(null);
			}
		});
	} else {
		throw "[201008161605] miss callback function";
	}
};
AsCouch.put = function(object, callback) {
	if( typeof JSON == 'undefined') {
		throw "this needs json2.js";
	} else {
		callback = callback ||
		function(answer) {
			if(!answer || !answer.ok) {
				AsDialog.show('error: ' + answer && answer.error ? answer.error : "unknown");
				// INTLANG
			} else {
				// ↖ answer okay, document inserted
				AsDialog.show("document inserted");
				// INTLANG
			}
		};


		$.ajax({
			type : "POST",
			url : "couchput.json",
			data : "body=" + escape(JSON.stringify(object)),
			success : callback,
			error : function(msg) {
				AsDialog.show('unknown error [201008181107]');
				// INTLANG
			}
		});
	}
};
var AsDom = {};
AsDom.appendHiddenInput = function(element, name, value) {
	$(element).append($(document.createElement('input')).attr("name", name).attr("type", "hidden").attr("value", value));
};
var FacilityOverviewTree = {};
FacilityOverviewTree.init = function(rootfacilities) {
	var url = typeof (FacilityOverviewTreeUrlBase) == "undefined" ? "facility-to-book2.html" : FacilityOverviewTreeUrlBase;
	$('.jFileTreeWrapper').fileTree({
		root : rootfacilities,
		multiFolder : false,
		script : 'js-jqueryfacilitytree.html'
	}, function(file) {
		window.location = url + "?a=" + file.replace("/", "");
	});
};
/*!
 * jQuery Cookie Plugin
 * https://github.com/carhartl/jquery-cookie
 *
 * Copyright 2011, Klaus Hartl
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.opensource.org/licenses/GPL-2.0
 */
(function($) {
    $.cookie = function(key, value, options) {

        // key and at least value given, set cookie...
        if (arguments.length > 1 && (!/Object/.test(Object.prototype.toString.call(value)) || value === null || value === undefined)) {
            options = $.extend({}, options);

            if (value === null || value === undefined) {
                options.expires = -1;
            }

            if (typeof options.expires === 'number') {
                var days = options.expires, t = options.expires = new Date();
                t.setDate(t.getDate() + days);
            }

            value = String(value);

            return (document.cookie = [
                encodeURIComponent(key), '=', options.raw ? value : encodeURIComponent(value),
                options.expires ? '; expires=' + options.expires.toUTCString() : '', // use expires attribute, max-age is not supported by IE
                options.path    ? '; path=' + options.path : '',
                options.domain  ? '; domain=' + options.domain : '',
                options.secure  ? '; secure' : ''
            ].join(''));
        }

        // key and possibly options given, get cookie...
        options = value || {};
        var decode = options.raw ? function(s) { return s; } : decodeURIComponent;

        var pairs = document.cookie.split('; ');
        for (var i = 0, pair; pair = pairs[i] && pairs[i].split('='); i++) {
            if (decode(pair[0]) === key) return decode(pair[1] || ''); // IE saves cookies with empty string as "c; ", e.g. without "=" as opposed to EOMB, thus pair[1] may be undefined
        }
        return null;
    };
})(jQuery);

/*! thanks 2 http://stackoverflow.com/questions/784012/javascript-equivalent-of-phps-in-array */
if (!Array.prototype.indexOf)
{
  Array.prototype.indexOf = function(elt /*, from*/)
  {
    var len = this.length >>> 0;

    var from = Number(arguments[1]) || 0;
    from = (from < 0)
         ? Math.ceil(from)
         : Math.floor(from);
    if (from < 0)
      from += len;

    for (; from < len; from++)
    {
      if (from in this &&
          this[from] === elt)
        return from;
    }
    return -1;
  };
}
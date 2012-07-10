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
Event.observe(window, 'load', function() {
	Base.init();
});
var Base = {
	useJavaScript : function() {
		return !JSSWBrowser.isGeckoSmallerThan(1.8) && !JSSWBrowser.isFirefox1_0() && !JSSWBrowser.isIE5x();
	},
	init : function() {
		$$('.js_hide').each(function(el) {
			el.hide()
		});
		$$('.js_show').each(function(el) {
			el.show()
		});
		if (JSSWBrowser.isIE6x()) {
			$$('.js_hideIE').each(function(el) {
				el.hide()
			});
			$$('.js_showIE').each(function(el) {
				el.show()
			});
		}
		if ($("canvas_2")) {
			$("canvas_2").setStyle( {
				marginTop : "-20px"
			});
		}
	},
	WaitingIcon : {
		show : function(el) {
			el.innerHTML = "<img src=\"icons/wait.gif\" />";
		}
	},
	Ajax : function(url, options) {
		var before = new Date();
		before = before.getTime();
		var delay = 2000;
		var originalOnSuc = options.onSuccess;
		options.onSuccess = function(r) {
			var after = new Date();
			after = after.getTime();
			var wait = delay - (after - before);
			if (wait > 0) {
				window.setTimeout(function() {
					originalOnSuc(r);
				}, wait);
			} else {
				originalOnSuc(r);
			}
		};
		new Ajax.Request(url, options);
	},
	Calendar : {
		getPopUpCalendar : function() {
			calendar = new CalendarPopup();
			calendar = Base.Calendar.privateGet(calendar);
			return calendar;
		},
		privateGet : function(calendar) {
			calendar.showYearNavigation();
			calendar.setCssPrefix("das_");
			// INTLANG : cal.setMonthAbbreviations("Jan","Feb","Mar",...);
		// INTLANG : cal.setMonthNames("January","February","March",...);
		// INTLANG : cal.setDayHeaders("S","M","T",...);
		// INTLANG : cal.setTodayText("Today");
		return calendar;
	}
	}
}
Base.nl2br = function(raw) {
	return (raw + '').replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g, '$1<br />$2');
}
Base.Effects = {};

Base.Effects.hide = function(el) {
	if(el) {
		Effect.Fade(el, {
			duration : Base.Effects.duration,
			from : 1,
			to : 0
		});
	}
};
Base.Effects.show = function(el) {
	if(el) {
		el.setOpacity(0).show();
		Effect.Fade(el, {
			duration : Base.Effects.duration,
			from : 0,
			to : 1
		});
	}
};
Base.Effects.duration = 1.0;
Base.Effects.showAndHide = function(el2show, el2hide) {
	Effect.Fade(el2hide, {
		duration : Base.Effects.duration,
		from : 1,
		to : 0,
		afterFinish : function() {
			el2show.setOpacity(0).show();
			Effect.Fade(el2show, {
				duration : Base.Effects.duration,
				from : 0,
				to : 1,
				afterFinish : function() {
					Effect.ScrollTo(el2show, {
						offset : -100
					});
				}
			});
		}

	});
};

Base.DepartmentInput = {};
Base.DepartmentInput.init = function() {
	Base.DepartmentInput.toggleFreeInput();
	Event.observe($('departmentKey_id'), 'change', Base.DepartmentInput.toggleFreeInput);
};
Base.DepartmentInput.toggleFreeInput = function() {
	if ($('departmentKey_id') && $('departmentKey_id').getValue() == 'unknown') {
		Base.Effects.show($('js_department_free_input_wrapper'));
	} else {
		Base.Effects.hide($('js_department_free_input_wrapper'));
	}
};

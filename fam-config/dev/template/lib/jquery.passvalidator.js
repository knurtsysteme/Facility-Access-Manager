/*!  JavaScript Scrollwork, version 0.20101005
 *  (c) 2010 KNURT Systeme
 *
 * This file is part of JavaScript Scrollwork.
 * For details, see http://www.knurt.de/default/javascript-scrollwork
 *
 * JavaScript Scrollwork is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaScript Scrollwork is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaScript Scrollwork.  If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 */

if(typeof(jQuery)=='undefined')
    throw("scrollwork_base.js requires jquery.js");
/**
* check the password strength of the first given password field and check if all password fields contain the same text.
* call a callback function with the results.
* this does not generate any html and only differs three steps weak, medium, strong.
*/
(function($){
    var passels = [];

    $.fn.passvalidator = function(options) {
        // extend default options with overrides
        var opts = $.extend({}, $.fn.passvalidator.defaultOptions, options);

        passels = this;
        if(passels.length < 2) {
            throw "need at least two password fields here";
        } else {
            $.each(passels, function(i, passel) {
                $(passel).keyup(function(){
                    checkValues(passels, opts);
                });
            });
        }
    }


    $.fn.passvalidator.defaultOptions = {
        callback:       function(strength, same){
            alert('{ "strength":'+strength + ',"same":' + same+'}');
        },
        weak:0,
        medium:1,
        strong:2,
        mediumRegex : /(?=.{8,})(?=.*[a-zA-Z].*[a-zA-Z])(?=.*[^a-zA-Z].*[^a-zA-Z])/g,
        strongRegex : /(?=.{13,})(?=.*[a-z].*[a-z])(?=.*[A-Z].*[A-Z])(?=.*[0-9].*[0-9])(?=.*[^a-zA-Z0-9].*[^a-zA-Z0-9])/g
    }

    function checkValues(passels, options) {
        var strength = getStrength($(passels[0]).val(), options);
        var same = areEqual(passels);
        options.callback(strength, same);
    }
    function getStrength(pass, options) {
        var result = options.weak;
        if(pass && pass.length > 0) {
            if(pass.match(options.strongRegex)) {
                result = options.strong;
            }
            else if(pass.match(options.mediumRegex)) {
                result = options.medium;
            }
        }
        return result;
    }
    function areEqual(passels) {
        var result = true;
        var pass = $(passels[0]).val();
        $.each(passels, function(i, passel) {
            result = pass == $(passel).val();
            return result;
        });
        return result;
    }

})(jQuery);

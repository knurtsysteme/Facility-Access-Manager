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
if(typeof(Base) == "undefined") {
    throw "this neede Base.js";
}
$(document).ready(function() {
    $('form').show();
    $('form').submit(function(){
        var company_value = $('#js_company_select').val();
        if(company_value == "unknown") {
            company_value = $("#company_id_unknown").val();
        } else {
        	company_value = $('#js_company_select :selected').html();
        }
        $('#js_real_value_company').val(company_value);

        var department_key = $("select.js_department:visible").val();
        if(department_key == null || department_key == "unknown") { // an unknown department has been chosen
        	var department_label = $("#department_id_unknown").val();
            $('#js_real_value_department').val(department_label);
            $('#js_departmentLabel').val(department_label);
            $("#js_departmentKey").val("unknown");
        } else { // a known department has been chosen
        	var department_label = $("option[value="+$("select.js_department:visible").val()+"]").html(); // same as $("select.js_department:visible :checked") - but did not work
            $('#js_real_value_department').val(department_label);
            $('#js_departmentLabel').val(department_label);
            $("#js_departmentKey").val(department_key);
        }
    });
    Base.Register.Department.init();
    Base.Register.Password.init();
    Base.Register.Datepicker.init();
    Base.Register.Hints.init();
	Base.Register.GenderTitleConnection.init();
    Base.Register.Charslimit.init();
    Base.Register.Validation.init();
});
Base.Register = {};
Base.Register.Department = {};
Base.Register.Department.set = function() {
    var value = $('select#js_company_select').val();
    Base.hide($('#js_company_unknown,#js_department_unknown,.js_department_input'), function(){
        if(value == "unknown") {
            Base.show($('#js_company_unknown,#js_department_unknown'), function(){
                Base.Register.Department.setDepartment($('#js_department_unknown'));
                $('#js_department_'+value).addClass("valid");
            });
        } else {
            Base.show($('#js_department_'+value), function(){
                Base.Register.Department.setDepartment($('#department_id_'+value));
            });
        }
    });
};
Base.Register.Department.setDepartment = function(depsel) {
    if(depsel.attr("id") == "js_department_unknown" || depsel.val() == "unknown") {
        Base.show($('#js_department_unknown'));
    } else {
        Base.hide($('#js_department_unknown'));
    }
    if($('.js_department:visible').val() == -1) {
        $('.js_department:visible').removeClass("valid");
    } else {
        $('.js_department:visible').addClass("valid");
    }
};

Base.Register.Department.init = function() {
    Base.Register.Department.set();
    $('select#js_company_select').bind("change", function(){
        Base.Register.Department.set();
        return false;
    });
    $('select.js_department').bind("change", function() {
        Base.Register.Department.setDepartment($(this));
        return false;
    });
};
Base.Register.Password = {};
Base.Register.Password.init = function() {
    Base.Register.Password.set(0, false);
    $('#pass1_id,#pass2_id').passvalidator({
        callback:Base.Register.Password.set
    });
};
Base.Register.Password.set = function(strength, same) {
	var passcandidate = $('#pass1_id').val();
	var validLength = passcandidate && passcandidate.length >= 8 && passcandidate.length <= 20 && passcandidate.replace(/[^(a-zA-Z0-9_\-\.\+,#)]/g, "").length == passcandidate.length;
    if((strength == $.fn.passvalidator.defaultOptions.medium || strength == $.fn.passvalidator.defaultOptions.strong) && same && validLength) {
        $('#js_scrollwork_password_ok').attr("class", "yes");
        Base.Register.Validation.domfieldsetvalid("fs_pw", true);
        Base.Register.Validation.domfieldvalid("pass1_id", true);
        Base.Register.Validation.domfieldvalid("pass2_id", true);
        Base.Register.Validation.fieldsetvalid.fs_pw = true;
    } else {
        $('#js_scrollwork_password_ok').attr("class", "no");
        Base.Register.Validation.domfieldsetvalid("fs_pw", false);
        Base.Register.Validation.domfieldvalid("pass1_id", false);
        Base.Register.Validation.domfieldvalid("pass2_id", false);
        Base.Register.Validation.fieldsetvalid.fs_pw = false;
    }
    $('#js_scrollwork_password_0,#js_scrollwork_password_1,#js_scrollwork_password_2').removeClass("invalid valid");
    $('#js_scrollwork_password_'+strength).addClass("valid");
};
Base.Register.Datepicker = {};
Base.Register.Datepicker.init = function() {
    var today = new Date();
    $("#birthdate_id").datepicker({
        "dateFormat":"dd.mm.yy",
        "changeYear":true,
        "yearRange":(today.getFullYear()-80)+":"+(today.getFullYear()-14),
        "defaultDate":'-25y'
    });
};
Base.Register.Hints = {};
Base.Register.Hints.init = function(){
	$('.hint').each(function(i, hint) {
		if($(hint).attr('id')) {
			var id_hint = $(hint).attr('id');
			var id_input = id_hint.substr(0, id_hint.length - '_hint'.length);
			if($('#' + id_input).length > 0 || $('#' + id_hint).length > 0) {
		    	$('#'+id_input).CreateBubblePopup({
		    		innerHtml: $('#'+id_hint).html(), 
		    		position: 'right',
		    		align: 'left',
		    		themeName: 'black',
		    		hideTail: false,
		    		width: '200px',
		    		themePath: 'jquerybubblepopup-theme/'
		    	});
		        $('#'+id_input).focus(function(){
		        	$('#'+id_input).ShowBubblePopup();
		        }).blur(function(){
		        	$('#'+id_input).HideBubblePopup();
		        });
			}
		}
	});
};
Base.Register.Validation = {};
Base.Register.Validation.init = function() {
    if(typeof(ValidationConfiguration)!="undefined") {
        $(':input').bind("keyup", function(){
            Base.Register.Validation.changedom();
            return false;
        });
        $(':input').bind("change",function(){
            Base.Register.Validation.changedom();
            return false;
        });
        $(':input').bind("blur", function(){
            Base.Register.Validation.changedom();
            return false;
        });
        Base.Register.Validation.changedom();
    }
};
Base.Register.Validation.domfieldsetvalid = function(fieldsetid, valid) {
	$('#'+fieldsetid).removeClass("invalid valid").addClass(valid ? "valid" : "invalid");
};
Base.Register.Validation.domfieldvalid = function(field, valid) {
	$('#'+field).removeClass("invalid valid").addClass(valid ? "valid" : "invalid");
};
Base.Register.Validation.fieldsetvalid = {};
Base.Register.Validation.fieldsetvalid.fs_cd = false;
Base.Register.Validation.fieldsetvalid.fs_irp = false;
Base.Register.Validation.fieldsetvalid.fs_pi = false;
Base.Register.Validation.fieldsetvalid.fs_pw = false;
Base.Register.Validation.completeFormIsValid = function() {
    return Base.Register.Validation.fieldsetvalid.fs_cd && Base.Register.Validation.fieldsetvalid.fs_irp && Base.Register.Validation.fieldsetvalid.fs_pi && Base.Register.Validation.fieldsetvalid.fs_pw;
};
Base.Register.Validation.changedom = function() {
    Base.Register.Validation.fieldsetvalid.fs_pi = Base.Register.Validation.changedomfieldset("fs_pi");
    Base.Register.Validation.fieldsetvalid.fs_cd = Base.Register.Validation.changedomfieldset("fs_cd");
    Base.Register.Validation.fieldsetvalid.fs_irp = Base.Register.Validation.changedomfieldset("fs_irp");
    if(Base.Register.Validation.completeFormIsValid()) {
        $('#register_submit').attr('disabled', false).html('<span class="image"></span>' + ValidationConfiguration.label.submit).removeClass("no").addClass("send");
    } else {
        $('#register_submit').attr('disabled', true).html('<span class="image"></span>' + ValidationConfiguration.label.nosubmit).removeClass("send").addClass("no");
    }
};
Base.Register.Validation.changedomfieldset = function(fieldsetid) {
    var totalresult = true;
    var fieldels = $('#'+fieldsetid+' :input');
    // general validation of all input fields
    $.each(fieldels, function(i, fieldel) {
        var fieldid = $(fieldel).attr("id");
        if(ValidationConfiguration.mustbevalid.join().indexOf(fieldid) >= 0) {
            var singlefieldisvalid = Base.Register.Validation.singlefieldisvalid(fieldid);
            // check of specific validation is configured
            var mustbetrue = null;
            $.each(ValidationConfiguration.mustbetrue, function(i, customvalid) {
            	if(customvalid.id == fieldid) mustbetrue = customvalid;
            });
            if(mustbetrue !== null) {
            	singlefieldisvalid = mustbetrue.validate($(fieldel).val());
            }
            Base.Register.Validation.domfieldvalid(fieldid, singlefieldisvalid);
            totalresult = totalresult ? singlefieldisvalid : false;
        } else {
        	$(fieldel).addClass('valid');
        }
    });
    $.each(ValidationConfiguration.oneofmustbevalid, function(i, oneofmustbevalids) {
		var result = false;
        $.each(oneofmustbevalids, function(i, oombv){
            result = Base.Register.Validation.singlefieldisvalid(oombv);
            if(result) {
                return false;
            }
        });
        $.each(oneofmustbevalids, function(i, oombv){
            Base.Register.Validation.domfieldvalid(oombv, result);
        });
        $.each(oneofmustbevalids, function(i, oombv){
		    $.each(fieldels, function(i, fieldel){
				if(fieldel == oombv) {
					totalresult = totalresult ? result : false;
					return false;
				}
		    });
			return false;
        });
    });
    Base.Register.Validation.domfieldsetvalid(fieldsetid, totalresult);
    return totalresult;
};
Base.Register.Validation.singlefieldisvalid = function(fieldid) {
    var result = true;
    if($('#'+fieldid).is(":visible")) {
        var check = $('#'+fieldid).val();
        result = !SWRegExp.is.nothingOrWhitespace(check);
        if(result) {
            result = check!="-1"
        }
        if(result&&fieldid=='mail_id') {
            result = SWRegExp.is.regularEmail(check);
        }
        // field id with characters limit
        if(result) {
			$.each(ValidationConfiguration.charslimit, function(i, charslimit_obj){
				if(fieldid == charslimit_obj.content_id) {
					var left = charslimit_obj.limit - $('#'+charslimit_obj.content_id).val().length;
					if(left < 0) {
						$('#'+charslimit_obj.view_id).css({'color': 'red', 'font-weight': 'bold', 'text-decoration': 'blink'});
						result = false;
					} else {
						$('#'+charslimit_obj.view_id).css({'color': 'black', 'font-weight': 'normal', 'text-decoration': 'none'});
						result = true;
					}
				}
			});
        }
    }
    return result;
};
Base.Register.GenderTitleConnection = {};
Base.Register.GenderTitleConnection.init = function() {
	$('#male_id').change(function(){
		var titleValue = $('#title_id').val();
		if (titleValue != "Dr." && titleValue != "Prof.") {
			if ($('#male_id').val() == "0") {
				$('#title_id').val("Mrs.");
			} else if ($('#male_id').val() == "1") {
				$('#title_id').val("Mr.");
			}
		}
	});
	
	$('#title_id').change(function(){
		var titleValue = $('#title_id').val();
		if (titleValue == "Mrs.") {
			$('#male_id').val("0");
		} else if (titleValue == "Mr.") {
			$('#male_id').val("1");
		}
	});
	
};

Base.Register.Charslimit = {};
Base.Register.Charslimit.exec = function() {
		$.each(ValidationConfiguration.charslimit, function(i, charslimit_obj){
			if($('#'+charslimit_obj.content_id).length > 0) {
				var left = charslimit_obj.limit - $('#'+charslimit_obj.content_id).val().length;
				$('#'+charslimit_obj.view_id).html(left);
			}
		});
};
Base.Register.Charslimit.init = function() {
	if(ValidationConfiguration.charslimit) {
		Base.Register.Charslimit.exec();
		$.each(ValidationConfiguration.charslimit, function(i, charslimit_obj){
			$('#'+charslimit_obj.content_id).bind("keyup", function() {
				Base.Register.Charslimit.exec();
		        return false;
		    });
			if(charslimit_obj.min && $('#' + charslimit_obj.view_id_min).length > 0) {
				$('#' + charslimit_obj.view_id_min).html(charslimit_obj.min);
			}
		});
	}
};
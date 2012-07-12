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
  throw "this needs base_jq.js";
}
if(typeof(JSON) == "undefined") {
  throw "this needs json2.js";
}

//set a facility here (if needed)
var Facility = {};
var BookingRule = {};
var Booking = {};

/*! getWeekOfTheYear() was originally developed by Nick Baicoianu at MeanFreePath: http://www.meanfreepath.com */
/**
 * Returns the week number for this date.
 * @return int
 */
Date.prototype.getWeekOfTheYear = function () {

  var newYear = new Date(this.getFullYear(),0,1);
  var day = newYear.getDay() - Base.Calendar.FIRST_DAY_OF_WEEK; //the day of week the year begins on
  day = (day >= 0 ? day : day + 7);
  var daynum = Math.floor((this.getTime() - newYear.getTime() - 
  (this.getTimezoneOffset()-newYear.getTimezoneOffset())*60000)/86400000) + 1;
  var weeknum;
  //if the year starts before the middle of a week
  if(day < 4) {
    weeknum = Math.floor((daynum+day-1)/7) + 1;
    if(weeknum > 52) {
      nYear = new Date(this.getFullYear() + 1,0,1);
      nday = nYear.getDay() - Base.Calendar.FIRST_DAY_OF_WEEK;
      nday = nday >= 0 ? nday : nday + 7;
      /*if the next year starts before the middle of
         the week, it is week #1 of that year*/
      weeknum = nday < 4 ? 1 : 53;
    }
  }
  else {
    weeknum = Math.floor((daynum+day-1)/7);
  }
  return weeknum == 0 ? 52 : weeknum;
};
/*! KNURT Systeme - part of the Facility Access Manager */
$(document).ready(function() {
  Base.Calendar.init();
});

Base.Calendar = {};
Base.Calendar.FIRST_DAY_OF_WEEK = 1; // monday!
Base.Calendar.URL_EVENTS = "do-getevents.json";
Base.Calendar.getUrlBookSucc = function(jobId) {
  return "thanks-for-your-booking--bookfacilitiesdone.html?jobId={0}".format(jobId);
};
Base.Calendar.URL_FACILITY_DETAILS = "do-getfacilitydetails.json";
Base.Calendar.INFO_ICON = "icons/kuba_information_icons_set_3.gif";
Base.Calendar.COLORS = [
                               "32C832", // available @see FacilityAvailability#COMPLETE_AVAILABLE
                               "AAAAAA", // time beyond working hours or yesterdays @see FacilityAvailability#GENERAL_NOT_AVAILABLE
                               "32C832", // partly available @see FacilityAvailability#MAYBE_AVAILABLE (colored as available - partly color "FA9632") 
                               "FA0032", // booked up @see FacilityAvailability#BOOKED_NOT_AVAILABLE
                               "AAAAAA", // maintenance @see FacilityAvailability#MAINTENANCE_NOT_AVAILABLE
                               "0000FF", // sudden failures @see FacilityAvailability#SUDDEN_FAILURE_NOT_AVAILABLE
                               "C8FF32" // impossible to start a session here @see FacilityAvailability#BOOKING_MUST_NOT_START_HERE
                               ];

Base.Calendar.timeAsText = function(minutes) {
  var result = "";
  var w = minutes / 60 / 24 / 7;
  w = Math.floor(w);
  minutes %= 60 * 24 * 7;
  var d = minutes / 60 / 24;
  d = Math.floor(d);
  minutes %= 60 * 24;
  var h = minutes / 60;
  h = Math.floor(h);
  minutes %= 60;
  if(w > 0) {
    result += w == 1 ? "1 week" : w + " weeks"; // INTLANG
  }
  if(d > 0) {
    if(w > 0) result += (h > 0 || minutes > 0) ? ", " : " and "; // INTLANG
    result += d == 1 ? "1 day" : d + " days"; // INTLANG
  }
  if(h > 0) {
    if(w > 0 || d > 0) result += (minutes > 0) ? ", " : " and "; // INTLANG
    result += h == 1 ? "1 hour" : h + " hours"; // INTLANG
  }
  if(minutes > 0) {
    if(w > 0 || d > 0 || h > 0) result += " and "; // INTLANG
    result += minutes == 1 ? "1 minute" : minutes + " minutes"; // INTLANG
  }
  return result;
}
Base.Calendar.init = function() {
  // prepare waiting icon
  Base.Calendar.Dialog.BookingRequest.init();
  // check, if a key is given
  if(Facility.key == null) {
    FacilityOverviewTree.init(Facilities.keys);
    var urlkey = $.url.param("a");
    if(urlkey == "") {
      $('#jchoose_facility_content').show();
    } else {
      Facility.key = urlkey;
    }
  }
  if(Facility.key != null) {
    Base.WaitingIcon.showOnPage();
    // get facility infos
    $.ajax( {
      type : 'GET',
      url : Base.Calendar.URL_FACILITY_DETAILS,
      contentType : "application/json; charset=utf-8",
      dataType : "json",
      data : "data=" + escape(JSON.stringify(Facility)),
      success : function(r) {
        if(r && r.succ) {
          Booking = r;
          Facility = r.facility;
          if(!r.user_is_allowed_to_access) {
            $.n.error("you are not allowed to access this facility"); // INTLANG
          } else {
            BookingRule = r.br;
            $('.'+(BookingRule.must_apply ? 'must_apply' : 'must_not_apply')).show();
            $('.'+(BookingRule.must_apply ? 'must_not_apply' : 'must_apply')).hide();
            
            if(BookingRule.strategy == 1) { // time based
              if($('#jhelp_content_first_time_booking').length==1){
                Base.Calendar.Dialog.Help.show();
              }
              $('.max_bookable_time_units').html(Base.Calendar.timeAsText(BookingRule.max_bookable_time_units * BookingRule.smallest_minutes_bookable));
              $('.min_bookable_time_units').html(Base.Calendar.timeAsText(BookingRule.min_bookable_time_units * BookingRule.smallest_minutes_bookable));
              Base.Calendar.View.init();
            } else if(r.br.strategy == 2) { // queue based
              Base.Queue.init();
            } else {
              $.n.error("please report error 201104121100-"+r.br+r.br.strategy); // => unknown booking strategy
              Base.WaitingIcon.hideOnPage();
            }
          }
        } else {
          $.n.error("please report error 201104121058");
          Base.WaitingIcon.hideOnPage();
        }
      },
      error : function(r) {
        $.n.error("please report error 201203080901");
        Base.WaitingIcon.hideOnPage();
      }
    });
  } else {
    Base.WaitingIcon.hideOnPage();
  }
  $('#ui-datepicker-div').hide();
}

Base.Calendar.A_DAY_IN_MINUTES = 24 * 60;
Base.Calendar.A_MINUTE_IN_MS = 60 * 1000;
Base.Calendar.A_DAY_IN_MS = Base.Calendar.A_DAY_IN_MINUTES * Base.Calendar.A_MINUTE_IN_MS;

Base.Calendar.Datepicker = {};
Base.Calendar.Datepicker.getCurrentDate = function() {
  return Base.Calendar.Datepicker.object == null ? null : Base.Calendar.Datepicker.object.datepicker("getDate");
};
Base.Calendar.Datepicker.object = null;
Base.Calendar.Datepicker.init = function() {
  Base.Calendar.Datepicker.object = $('#js_datepicker').datepicker({
    firstDay: Base.Calendar.FIRST_DAY_OF_WEEK,
    onSelect: function(a, b) {
      Base.Calendar.Tabs.update();
      Base.Calendar.View.update();
    }
  });
},

Base.Calendar.Tabs = {};
Base.Calendar.Tabs.booking_request_needs = {};
Base.Calendar.Tabs.init = function() {
  $('#jui_tabs').tabs();
  Base.Calendar.Tabs.booking_request_needs = $('#js_booking_request_needs').tabs({
      select: function(event, ui) {
          if(ui.index == 1) {
            Base.Calendar.Tabs.loadAndShowInfo();
          }
      }
  });
  Base.Calendar.Tabs.update();
};
/**
 * generate the notices to the time clicked and show it in the dialog
 */
Base.Calendar.Tabs.loadAndShowInfo = function() {
  Base.WaitingIcon.show($('#js_booking_request_tab_2'));
  var data = {};
  data.facility = Facility;
  // assert function Base.Calendar.Dialog.BookingRequest.show called before 
  data.start = Base.Calendar.Dialog.BookingRequest.dateStart;
  data.end = Base.Calendar.Dialog.BookingRequest.dateEnd;
  $.ajax( {
    type : 'GET',
    url : 'want-2-getdetailsofbooking.json',
        dataType: "json",
    data : "data=" + JSON.stringify(data),
    success : function(r) {
      if(r && r.succ) {
        var content = "";
        if(r.details.length == 0) {
          content = "no bookings so far between<br />{0} and<br />{1}".format(data.start, data.end);
        } else {
          var template = "<h3>$username</h3><ul><li>$statustext</li><li>From $start_date_and_time to $end_date_and_time</li><li class='notice'>notice: $notice<form class='js_update_notice'><input type='hidden' name='id_booking' value='$id' /><input type='hidden' name='notice_value' value='$notice' /><button>edit</button></form></li></ul>";
          if(!r.anonymize_personal_info){
            template = "<h3><a href='#'>$fullname ($start_date_and_time_short - $end_date_and_time_short)</a></h3><ul><li>$statustext</li><li>From $start_date_and_time to $end_date_and_time</li><li><a href='mailto:$mail'>$mail</a></li><li>$street $streetno<br />$zipcode $city<br />$country_name</li><li>Phone: $phone1</li><li>Mobile: $phone2</li><li>Department: $department</li><li class='js_notice'>Public notice: <span class='notice_value' id='js_notice_value_$id'>$notice</span><span style='display:none' class='booking_id'>$id</span><button>edit</button></li>$custom_details</ul>";
          }
          $(r.details).each(function(i, detail) {
            var tmp = template.replace(/\$username/g, detail.username);
            tmp = tmp.replace(/\$start_date_and_time_short/g, detail.start_date_and_time_short);
            tmp = tmp.replace(/\$end_date_and_time_short/g, detail.end_date_and_time_short);
            tmp = tmp.replace(/\$start_date_and_time/g, detail.start_date_and_time);
            tmp = tmp.replace(/\$end_date_and_time/g, detail.end_date_and_time);
            tmp = tmp.replace(/\$statustext/g, detail.statustext);
            tmp = tmp.replace(/\$fullname/g, detail.fullname);
            tmp = tmp.replace(/\$mail/g, detail.mail);
            if(detail.main_address) {
              tmp = tmp.replace(/\$streetno/g, detail.main_address.streetno);
              tmp = tmp.replace(/\$street/g, detail.main_address.street);
              tmp = tmp.replace(/\$zipcode/g, detail.main_address.zipcode);
              tmp = tmp.replace(/\$city/g, detail.main_address.city);
              tmp = tmp.replace(/\$country_name/g, detail.main_address.country_name);
            }
            tmp = tmp.replace(/\$phone1/g, detail.phone1);
            tmp = tmp.replace(/\$phone2/g, detail.phone2);
            tmp = tmp.replace(/\$department/g, detail.department);
            tmp = tmp.replace(/\$notice/g, detail.notice);
            tmp = tmp.replace(/\$id/g, detail.id);
            if(detail.cds) {
              var custom_details_content = "";
              $(detail.cds).each(function(i, custom_detail) {
                custom_details_content = "<li>$title: $detail</li>".replace(/\$title/g, custom_detail.title).replace(/\$detail/g, custom_detail.detail);
              });
              tmp = tmp.replace(/\$custom_details/g, custom_details_content); //
            }
            content += tmp;
          });
        }
        // prepare accordion
        content = "<div id='js_accordion'>" + content + "</div>";
        $('#js_booking_request_tab_2').html(content);
        if(r.details.length != 0) {
          $('#js_accordion').accordion();
          // behavior update notice
          $('.js_notice button').click(function(){
            var booking_id = $(this).parent(".js_notice").children(".booking_id").html();
            var notice_value = $(this).parent(".js_notice").children(".notice_value").html();
            Base.Calendar.Dialog.Notice.show(booking_id, notice_value);
          });
        }
      } else {
        $.n.error("please report error 201201090949");
      }
    },
    error : function(r) {
      $.n.error("please report error 201203080900");
    }
  });
}
Base.Calendar.Tabs.update = function() {
  var date = Base.Calendar.Datepicker.getCurrentDate();
  $('#js_tab_day span').html(Base.Calendar.Util.getDayAsText(date));
  $('#js_tab_week span').html(Base.Calendar.Util.getWeekAsText(date));
  $('#js_tab_month span').html(Base.Calendar.Util.getMonthAsText(date));
};
Base.Queue = {};
Base.Queue.init = function() {
  if(Booking.current_queue_length==0) {
    $('.queue_does_not_exist').show();
    $('.queue_exists').hide();
  }else {
    $('.queue_does_not_exist').hide();
    $('.queue_exists').show();
  }
  $('.js_current_queue_length').html(Booking.current_queue_length);
  Base.JobSurvey.init("queue");
};
Base.JobSurvey = {};
Base.JobSurvey.init = function(strat) {
  var wrapper = "#jobsurvey_{0}_wrapper".format(strat);
  $('#jobsurvey_queue_wrapper, #jobsurvey_time_wrapper, #jchoose_facility_content, #book_calendar').hide();
  $(wrapper + ", #job_survey_wrapper").show();
  $('.facility_label').html(Facility.label);
  Base.WaitingIcon.show($(wrapper + ' .waiting_icon'));
  $('#waiting_icon').hide();
  $('.change_facility').click(function(){
    $('#job_survey_wrapper').hide();
    $('#jchoose_facility_content').show();
  });
  JobDataProcessing.getStructure(
    function(result) { // callback_succ
      if(result.has_job_data_processing) {
        Base.WaitingIcon.showOnPage();
        $('#idJobDataProcessing').val(result.idJobDataProcessing);
        $(wrapper + ' iframe').attr('src', JobSurveyIO.getIFrameSrc(result.facilityKey));
        $('.has_job_data_processing').show();
        $('.has_no_job_data_processing').hide();
        if(result.templates && result.templates[0] && result.templates[0].behaviour) { // may have no template
          try {
          jQuery.globalEval(result.templates[0].behaviour);
          }catch(err) {
          }
        }
        Base.setIFrameHeightInterval();
        Base.WaitingIcon.hideOnPage();
      } else {
        $('.has_job_data_processing').hide();
        $('.has_no_job_data_processing').show();
        Base.WaitingIcon.hideOnPage();
      }
    }, 
    function(result) { // callback_error
      $('.has_job_data_processing').hide();
      $('.has_no_job_data_processing').show();
      $(wrapper + ' .validation_result').html("please report error [code 201203050839]: " + result.responseText).show();
      Base.WaitingIcon.hideOnPage();
    }, 
    Facility.key, true, true
  );
  $(wrapper + ' .submit').click(function(){
    Base.WaitingIcon.showOnPage();
    JobSurveyIO.post({
      step: 0,
      succ: function(r) {
        window.location = Base.Calendar.getUrlBookSucc(r.jobId);
      },
      fail: function(r) {
        Base.WaitingIcon.hideOnPage();
        if(r && r.reason) {
          $.n.error("Fails! Reason: " + r.reason); // INTLANG
        } else {
          $.n.error("please report error 201203061010");
        }
      }, 
      error: function(r) {
        Base.WaitingIcon.hideOnPage();
        $.n.error("please report error 201203080905");
      },
      invalid: function() {
        Base.WaitingIcon.hideOnPage();
      }
    });
    return false;
  });
};

Base.Calendar.View = {};
Base.Calendar.View.dateShown = null;
Base.Calendar.View.init = function() {
  Base.WaitingIcon.showOnPage();
  Base.Calendar.BarControl.allbars = null;
  if(Booking.capacity_units == null) {
    Booking.capacity_units = BookingRule.min_bookable_capacity_units;
  }
  $('.facility_label').html(Facility.label);
  $('.change_facility').click(function(){
    $('#book_calendar').hide();
    $('#book_queue').hide();
    $('#jchoose_facility_content').show();
  });
  $('.booking_capacity_units').html(Booking.capacity_units);
  $('.booking_capacity_units_label').html(Base.Calendar.Util.getUnitsLabelAsText(Booking.capacity_units, false, false));
  $('.booking_capacity_units_with_label').html(Base.Calendar.Util.getUnitsLabelAsText(Booking.capacity_units, false, true));
  if(BookingRule.min_bookable_capacity_units != BookingRule.max_bookable_capacity_units) {
    $('.change_capacity_units').click(function(){
      Base.Calendar.Dialog.CapacityUnits.getInput();
    });
  } else {
    $('.change_capacity_units').hide();
    if(BookingRule.min_bookable_capacity_units == 1) {
      $('.hide_if_1_unit').hide();
    }
  }
  $('#book_calendar').show();
  
  Base.Calendar.Datepicker.init();
  Base.Calendar.Container.init();
  Base.Calendar.Tabs.init();
  Base.Calendar.Dialog.Help.init();
  Base.Calendar.View.update(true);
};
Base.Calendar.View.update = function(force) {
  force = force || false;
  var dateRequested = Base.Calendar.Datepicker.getCurrentDate();
  var refreshDay = Base.Calendar.View.dateShown == null || Base.Calendar.Util.getDayAsText(Base.Calendar.View.dateShown) != Base.Calendar.Util.getDayAsText(dateRequested);
  var refreshWeek = Base.Calendar.View.dateShown == null || Base.Calendar.Util.getWeekAsText(Base.Calendar.View.dateShown) != Base.Calendar.Util.getWeekAsText(dateRequested);
  var refreshMonth = Base.Calendar.View.dateShown == null || Base.Calendar.Util.getMonthAsText(Base.Calendar.View.dateShown) != Base.Calendar.Util.getMonthAsText(dateRequested);
  if(force || refreshDay || refreshWeek || refreshMonth) {
    var data = Base.Calendar.Util.dateAsJson(dateRequested);
    data.facility = Facility.key;
    data.capacity_units = Booking.capacity_units;
    Base.WaitingIcon.showOnPage();
    $.ajax( {
      type : 'GET',
      url : Base.Calendar.URL_EVENTS,
        dataType: "json",
      data : "data=" + JSON.stringify(data),
      success : function(r) {
        if(r && r.succ) {
          var cEvents = r.events; // calendar events
          // do not let the dom explode on month changes
          if(force || refreshMonth) $('.ezpz_tooltip').remove(); 
          if(force || refreshDay) {
            Base.Calendar.Day.update(cEvents);
            $('#js_tab_day span').effect("highlight", {}, 3000);
          }
          if(force || refreshWeek) {
            Base.Calendar.Week.update(cEvents);
            $('#js_tab_week span').effect("highlight", {}, 3000);
          }
          if(force || refreshMonth) {
            Base.Calendar.Month.update(cEvents);
            $('#js_tab_month span').effect("highlight", {}, 3000);
          }
          Base.Calendar.View.dateShown = dateRequested;
          Base.Calendar.updateClassOfColumnOfDateRequested();
        } else {
          $.n.error("please report error 201104121047");
        }
        Base.WaitingIcon.hideOnPage();
      },
      error : function(r) {
        $.n.error("please report error 201203080906");
        Base.WaitingIcon.hideOnPage();
      }
    });
  } else {
    Base.WaitingIcon.hideOnPage();
  }
};
Base.Calendar.updateClassOfColumnOfDateRequested = function() {
    // set class of day in week calendar
    $('#week_table th').removeClass("date_shown");
    $('#week_table th.'+Base.Calendar.View.dateShown.format("dddmmddyyyy")).addClass("date_shown");
    // set class of day in month calendar
    $('#month_table div.day_number').removeClass("date_shown");
    $('#month_table div.day_number_'+Base.Calendar.View.dateShown.getDate()).addClass("date_shown");
};
Base.Calendar.Container = {};
Base.Calendar.Container.init = function() {

  // fill in the example week (Base.Calendar.Container.exampleWeekDays)
  var exampleWeekDay = Base.Calendar.Datepicker.getCurrentDate();
  while(exampleWeekDay.getDay() != Base.Calendar.FIRST_DAY_OF_WEEK) {
    exampleWeekDay = new Date(exampleWeekDay.getTime() - Base.Calendar.A_DAY_IN_MS);
  }
  // ↘ create array with the 7 days of the week
  Base.Calendar.Container.exampleWeekDays = new Array();
  for(var i = 0; i < 7; i++) {
    Base.Calendar.Container.exampleWeekDays.push(new Date(exampleWeekDay.getTime() + Base.Calendar.A_DAY_IN_MS * i));
  }
};
Base.Calendar.Container.exampleWeekDays = null;

Base.Calendar.Day = {};
Base.Calendar.Day.update = function(cEvents) {
};
Base.Calendar.Week = {};
Base.Calendar.Week.update = function(cEvents) {
  
  // ↓ get bars needed for week view
  // ↘ go back from choosen date to the first day of week as defined by the server(!).
  // ↘ assert that first element is first element of the week (@see java heinzelmann TimeFrameFactory#getMonthWithFullWeeks())
  var firstDayOfWeek = Base.Calendar.Datepicker.getCurrentDate();
  var datebefore = null;
  while(firstDayOfWeek.getDay() != Base.Calendar.FIRST_DAY_OF_WEEK) {
    datebefore = firstDayOfWeek.getDate();
    firstDayOfWeek = new Date(firstDayOfWeek.getTime() - Base.Calendar.A_DAY_IN_MS);
    // ↓ correct error on daylight save time
    if(firstDayOfWeek.getDate() == datebefore) {
      // ↖ a "blind hour" on daylight saving time
      firstDayOfWeek = new Date(firstDayOfWeek.getTime() + (Base.Calendar.A_DAY_IN_MS / 24));
    }
    firstDayOfWeek.setHours(0);
  }

  // ↘ create array with the 7 days of the week
  var daysOfWeek = new Array();
  for(var i = 0; i < 7; i++) {
    daysOfWeek.push(new Date(firstDayOfWeek.getTime() + Base.Calendar.A_DAY_IN_MS * i));
  }

  // fill in first row
  $('#week_table th.year').html(firstDayOfWeek.format("yyyy"));
  for(var i = 0; i < 7; i++) {
    $('#week_table th.day_' + i).html(daysOfWeek[i].format("ddd mm/dd/yyyy")).addClass(daysOfWeek[i].format("dddmmddyyyy"));
  }
  
  // fill in bars
  $(daysOfWeek).each(function(j, dayOfWeek) {
    window.setTimeout(function(){
      Base.Calendar.BarControl.showBarInElement(Base.Calendar.BarControl.getBar(cEvents, dayOfWeek), $('#week_table td.day_' + j), "week");
    },0);
  });
  $('.ezpz_tooltip').hide();
};
Base.Calendar.Month = {};
Base.Calendar.Month.update = function(cEvents) {

  var dateRequested = Base.Calendar.Datepicker.getCurrentDate();
  // ↖ current date
  var dateWalker = dateRequested;
  // ↖ temp walker to work with

  // go back to 1st of month
  while(dateWalker.getDate() != 1) {
    dateWalker = new Date(dateWalker.getTime() - Base.Calendar.A_DAY_IN_MS);
  }

  // ↓ create array with the n days of the month
  var daysOfMonth = new Array();
  daysOfMonth.push(dateWalker); 
  // ↖ push 1st day
  dateWalker = new Date(dateWalker.getTime() + Base.Calendar.A_DAY_IN_MS);
  // ↖ increment +1 day
  // ↘ create array with the n days of the month
  while(dateWalker.getDate() > 1) {
    daysOfMonth.push(dateWalker);
    var datebefore = dateWalker.getDate();
    dateWalker = new Date(dateWalker.getTime() + Base.Calendar.A_DAY_IN_MS);
    if(dateWalker.getDate() == datebefore) {
      // ↖ a "blind hour" on daylight saving time
      dateWalker = new Date(dateWalker.getTime() + (Base.Calendar.A_DAY_IN_MS / 24));
    }
    dateWalker.setHours(0);
    // ↖ for double hours on daylight saving time
  }

  // ↓ fill in first row
  $('#month_table .month_year').html(dateRequested.format("yyyy"));
  for(var i = 0; i < 7; i++) {
    $('#month_table thead th.day_' + i).html(Base.Calendar.Container.exampleWeekDays[i].format("dddd"));
  }

  // ↓ fill in next rows with bars
  $('#month_table td').html("");
  // ↖ delete everything in all content cells
  $('#month_table tr').show();
  // ↖ show all rows
  // ↓ number of row and col as tool for finding correct cell
  var weekRow = 0;
  var dayCol = -1;
  $(daysOfMonth).each(function(j, dayOfMonth) {
    if(dayCol == -1) {
      // ↖ first category cell
      // ↘ set calendar week
      $('#month_table th.week_' + weekRow).html(dayOfMonth.getWeekOfTheYear() + " CW"); // INTLANG
      if(weekRow == 0) {
        dayCol = dayOfMonth.getDay() - Base.Calendar.FIRST_DAY_OF_WEEK;
        if(dayCol < 0) dayCol += 7;
      } else {
        dayCol = 0;
      }
    }
    var dayElement = $('#month_table tr.week_' + weekRow + ' td.day_' + dayCol);
    window.setTimeout(function(){
      Base.Calendar.BarControl.showBarInElement(Base.Calendar.BarControl.getBar(cEvents, dayOfMonth), dayElement, "month");
    },0);
    $(dayElement).prepend('<div class="day_number day_number_'+dayOfMonth.getDate()+'" style="position: absolute; text-align: right; width: 15px; margin-left: ' + (parseInt(dayElement.css('width')) - 30) + 'px;">' + dayOfMonth.getDate() + '</div>');
    // ↓ increment cell counter
    if(dayCol == 6) {
      dayCol = -1;
      weekRow++;
    }
    else {
      dayCol++;
    }
  });
  // ↘ set new line
  if(dayCol != -1) {
    // ↖ already new line
    weekRow++;
  }
  while(weekRow < 6) {
    $('#month_table tr.week_' + weekRow).hide();
    weekRow++;
  };
  $('.ezpz_tooltip').hide();
};

Base.Calendar.BarControl = {};
Base.Calendar.BarControl.allbars = null;
Base.Calendar.BarControl.getBar = function(cEvents, date) {
  if(Base.Calendar.BarControl.allbars == null || Base.Calendar.BarControl.allbars[date.format("yyyymmdd")] == null) {
    Base.Calendar.BarControl.allbars = new Object();
    $(cEvents).each(function(j, cEvent) {
      var id = new Date(cEvent.year, cEvent.month, cEvent.day_of_month).format("yyyymmdd");
      if(Base.Calendar.BarControl.allbars[id] == null) {
        Base.Calendar.BarControl.allbars[id] = new Array();
      }
      Base.Calendar.BarControl.allbars[id].push(cEvent);
    });
  }
  var bar = {};
  bar.sections = Base.Calendar.BarControl.allbars[date.format("yyyymmdd")];
  return bar;
};
/**
* return an id for the given section.
* prepend the salt to the id.
* param salt should be "month", "week" or "day" to prevent doubled ids on different calendars.
*/
Base.Calendar.BarControl.getSectionId = function(section, salt) {
  return salt + "_id_section_" + new Date(section.year, section.month, section.day_of_month).format("yyyymmdd") + "_" + section.start + "_" + section.end;
};
Base.Calendar.BarControl.mouseDownCache = null;
Base.Calendar.BarControl.showBarInElement = function(bar, element, dayWeekMonth) {
  var barhtml = "";
  $(bar.sections).each(function(i, section) {
    var sectionhtml = "<div id=\"$id\" style=\"height: $heightpx; width: 100%; background-color: #$color; overflow: hidden; margin: 0; padding: 0;\"></div>";
    sectionhtml = sectionhtml.replace(/\$id/g, Base.Calendar.BarControl.getSectionId(section, dayWeekMonth));
    sectionhtml = sectionhtml.replace(/\$height/g, Base.Calendar.BarControl.getSectionHeight(section, element, dayWeekMonth));
    sectionhtml = sectionhtml.replace(/\$color/g, Base.Calendar.COLORS[section.event]);
    barhtml += sectionhtml;
  });

  $(element).html(barhtml);

  $(bar.sections).each(function(i, section) {
    var secid = Base.Calendar.BarControl.getSectionId(section, dayWeekMonth);
    var tooltipid = secid + "-content";
    if($("#" + tooltipid).length == 0) {
      var tooltip = "<div class=\"ezpz_tooltip\" id=\"$id\" style=\"display: none; position: absolute; z-index: 1500;\"><p class=\"infoicon\"><img src=\"$infoicon\" width=\"16\" height=\"16\" alt=\"Info Icon\" /></p><p class=\"time\">$time</p><p class=\"label\">$label</p><p class=\"clickhint\">$clickhint</p></p></div>";
      tooltip = tooltip.replace(/\$id/g, tooltipid);
      tooltip = tooltip.replace(/\$label/g, section.label);
      tooltip = tooltip.replace(/\$time/g, section.timeFrameLabel);
      tooltip = tooltip.replace(/\$infoicon/g, Base.Calendar.INFO_ICON);
      tooltip = tooltip.replace(/\$clickhint/g, "Click for reservation and details!"); // INTLANG
      $(document.body).prepend(tooltip);
    }
    $("#" + secid).ezpz_tooltip({contentId:tooltipid});
    $("#" + secid).mouseup(function() {
      var date_start = new Date();
      date_start.setFullYear(section.year);
      date_start.setMonth(section.month);
      date_start.setDate(section.day_of_month);
      date_start.setHours(section.start / 60);
      date_start.setMinutes(section.start % 60);
      var date_end = null;
      if(Base.Calendar.BarControl.mouseDownCache != null && Base.Calendar.BarControl.mouseDownCache != section) {
        date_end = new Date();
        date_end.setFullYear(Base.Calendar.BarControl.mouseDownCache.year);
        date_end.setMonth(Base.Calendar.BarControl.mouseDownCache.month);
        date_end.setDate(Base.Calendar.BarControl.mouseDownCache.day_of_month);
        date_end.setHours(Base.Calendar.BarControl.mouseDownCache.start / 60);
        date_end.setMinutes(Base.Calendar.BarControl.mouseDownCache.start % 60);
      } else {
        date_end = new Date(date_start.getTime() + BookingRule.smallest_minutes_bookable * BookingRule.min_bookable_time_units * Base.Calendar.A_MINUTE_IN_MS);
      }
      if(date_end.format("yyyymmddHHMM") < date_start.format("yyyymmddHHMM")) {
        var tmp = date_end;
        date_end = date_start;
        date_start = tmp;
      }
      Base.Calendar.Dialog.BookingRequest.show(date_start.format("mm/dd/yyyy HH:MM"), date_end.format("mm/dd/yyyy HH:MM"));
      Base.Calendar.BarControl.mouseDownCache = null;
    }).mousedown(function(){
      /* save the mouse down element
       * XXX does not work in all browsers */
      Base.Calendar.BarControl.mouseDownCache = section;
    });
  });
};

/**
* return the height of the given section if put in into given element.
* element must be visible at this point, otherwise height will be 0.
*/
Base.Calendar.BarControl.getSectionHeight = function(section, element, dayWeekMonth) {
  // XXX assume, that height of month td is 120 and height of week td is 480 and playing with nothing but hours
  return dayWeekMonth == "week" ? 20 : 5;
};
Base.Calendar.Dialog = {};
Base.Calendar.Dialog.Notice = {};
Base.Calendar.Dialog.Notice.show = function(booking_id, notice_value) {
  Base.Base_NoticeUpdate.showDialog(booking_id, notice_value, function(r) {
    $('#js_notice_value_' + booking_id).html($('#jedit_notice_of_booking_text').val());
    $.n.success(r.message);
  });
};

Base.Calendar.Dialog.Help = {};
Base.Calendar.Dialog.Help.init = function() {
  $('.jhelp_button').click(Base.Calendar.Dialog.Help.show);
};
Base.Calendar.Dialog.Help.show = function() {
  $('#jhelp_content').dialog({
    modal: true,
    width: '600px',
    title : "Support", // INTLANG
    buttons : {
      'Close' : function() {
        $(this).dialog('close');
      }
    }
  });
};

Base.Calendar.Dialog.CapacityUnits = {};
Base.Calendar.Dialog.CapacityUnits.getInput = function() {
  var htmlOptions = "";
  var tmp = BookingRule.min_bookable_capacity_units;
  while(tmp <= BookingRule.max_bookable_capacity_units) {
    htmlOptions += "<option value=\"$val\">$val $text</option>".replace(/\$val/g, tmp).replace(/\$text/g, Base.Calendar.Util.getUnitsLabelAsText(tmp, false));
    tmp++;
  }
  $('#jchoose_capacity_units_content select').html(htmlOptions);
  // show dialog to choose the right units
  $('#jchoose_capacity_units_content').dialog({
    modal: true,
    title : "Options", // INTLANG
    closeOnEscape: false,
    close: function(event, ui) {
    },
    buttons : {
      'Update Request' : function() {
        Booking.capacity_units = $('#jchoose_capacity_units_content select').val();
        Base.Calendar.View.init();
        $(this).dialog('close');
      },
      'Cancel' : function() {
        $(this).dialog('close');
      }
    }
  });
};

Base.Calendar.Dialog.BookingRequest = {};
Base.Calendar.Dialog.BookingRequest.init = function() {
  // prepare date and time picker
  $('#j_start_date').datetimepicker();
  $('#j_end_date').datetimepicker();
  $('#js_booking_request').hide();
  $('.request_facility').click(function() {
    Base.Calendar.Dialog.BookingRequest.show();
  });
  $('#cancel_time_reservation').click(function() {
    window.location = window.location;
  });
};
Base.Calendar.Dialog.BookingRequest.dateStart = null;
Base.Calendar.Dialog.BookingRequest.dateEnd = null;
Base.Calendar.Dialog.BookingRequest.show = function(datetime_start, datetime_end) {
  if(datetime_start == null || datetime_end == null) {
    var date_start = Base.Calendar.Datepicker.object.datepicker("getDate");
    var date_end = new Date(date_start.getTime() + BookingRule.smallest_minutes_bookable * BookingRule.min_bookable_time_units * Base.Calendar.A_MINUTE_IN_MS);
    Base.Calendar.Dialog.BookingRequest.show(date_start.format("mm/dd/yyyy HH:MM"), date_end.format("mm/dd/yyyy HH:MM"));
  } else {
    Base.Calendar.Dialog.BookingRequest.dateStart = datetime_start;
    Base.Calendar.Dialog.BookingRequest.dateEnd = datetime_end;
    $('#js_booking_request').dialog({
      open: function(event, ui) {
        $('#j_start_date').val(datetime_start);
        $('#j_end_date').val(datetime_end);
        // select first tab
        Base.Calendar.Tabs.booking_request_needs.tabs("select", 0);
      },
      modal: true,
      focusSelector: false,
      width : '600px',
      title : "Request " + Base.Calendar.Util.getUnitsLabelAsText(Booking.capacity_units, false, true), // INTLANG
      closeOnEscape: false,
      buttons : {
        'Check' : function(e) {
          Base.WaitingIcon.showOnPage();
          Base.Calendar.Tabs.booking_request_needs.tabs("select", 0);
          $('#js_booking_request_possibilities_wrapper, #js_booking_request_no_possibilities').hide();
          var data = {};
          var thisDialog = $(this);
          data.facility = Facility;
          data.request = {};
          data.request.start = $('#j_start_date').val();
          data.request.end = $('#j_end_date').val();
          data.request.capacity_units = Booking.capacity_units;
          $.ajax( {
            type : 'GET',
            url : "do-directbookingrequest2.json",
            dataType: 'json',
            data : "data=" + JSON.stringify(data),
            success : function(r) {
              try {
              if(r && r.succ) {
                if(r.possibilities.length == 0) {
                  $('#js_booking_request_no_possibilities').show('slow').effect("highlight", {color: 'red'}, 3000);
                } else {
                  var lis = '';
                  var is_perfect_match = r.possibilities.length == 1 && r.possibilities[0].capacity_units == data.request.capacity_units && r.possibilities[0].start == data.request.start && r.possibilities[0].end == data.request.end;
                  if(r.possibilities.length == 1) {
                      var li_format = '<li id="$article_number" class="clickable_article"><input type="hidden" name="v" value="$article_number" id="$article_number_id"  />$capacity_units between <span class="point_out">$start</span> and <span class="point_out">$end</span></li>';
                      li_format = li_format.replace(/\$article_number/g, r.possibilities[0].article_number);
                      li_format = li_format.replace(/\$capacity_units/g, Base.Calendar.Util.getUnitsLabelAsText(r.possibilities[0].capacity_units, false, true));
                      li_format = li_format.replace(/\$start/g, r.possibilities[0].start);
                      li_format = li_format.replace(/\$end/g, r.possibilities[0].end);
                      lis += li_format;
                      Booking.article_number = r.possibilities[0].article_number;
                  } else {
                    $(r.possibilities).each(function(i,possibility){
                      var li_format = '<li id="$article_number" class="clickable_article"><input type="radio" name="v" value="$article_number" id="$article_number_id" $checked/><label for="$article_number_id">$capacity_units between <span class="point_out">$start</span> and <span class="point_out">$end</span></label></li>';
                      li_format = li_format.replace(/\$checked/g, i == 0 ? 'checked="checked"' : '');
                      li_format = li_format.replace(/\$article_number/g, possibility.article_number);
                      li_format = li_format.replace(/\$capacity_units/g, Base.Calendar.Util.getUnitsLabelAsText(possibility.capacity_units, false, true));
                      li_format = li_format.replace(/\$start/g, possibility.start);
                      li_format = li_format.replace(/\$end/g, possibility.end);
                      lis += li_format;
                    });
                  }
                  $('#js_is_perfect_match, #js_is_not_perfect_match_1_unit, #js_is_not_perfect_match_many_units').hide();
                  if(is_perfect_match) {
                    $('#js_is_perfect_match').show();
                  } else if(r.possibilities.length == 1) {
                    $('#js_is_not_perfect_match_1_unit').show();
                  } else {
                    $('#js_is_not_perfect_match_many_units').show();
                  }
                  $('#js_booking_request_possibilities').html(lis);
                  Base.JobSurvey.init('time');
                  thisDialog.dialog('close');
                }
                Base.WaitingIcon.hideOnPage();
              } else if(!r) {
                $.n.error("Server antwortet nicht");
                Base.WaitingIcon.hideOnPage();
              } else if(r.message) {
                $.n.error(r.message);
                Base.WaitingIcon.hideOnPage();
              } else {
                $.n.error("please report error 201203080903");
                Base.WaitingIcon.hideOnPage();
              }
              } catch(e) {
                Base.WaitingIcon.hideOnPage();
              }
            },
            error : function(r) {
              $.n.error("please report error 201203080902");
              Base.WaitingIcon.hideOnPage();
            }
          });
        },
        'Cancel' : function() {
          $('#js_booking_request_possibilities_wrapper, #js_booking_request_no_possibilities').hide();
          $(this).dialog('close');
        }
      }
    });
  }
};
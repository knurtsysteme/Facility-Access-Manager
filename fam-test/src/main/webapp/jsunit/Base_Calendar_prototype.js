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
    DasCalendar.init();
});
var DasCalendar = {
    instance : null,
    init: function() {
        DasCalendar.Forms.init();
        if($$("div.weekHtml").length > 0 && $("body_book")) {
            DasCalendar.Notices.init();
            DasCalendar.Booking.init();
        }
        DasCalendar.Style.setSameColWidths();
        DasCalendar.Style.replaceEventButton();
    },
    Style : {
        replaceEventButton: function() {
            if($("body_book") && ($$(".monthHtml").size() > 0)) {
                $$(".calContent").each(function(cc){
                    var iicon = new Element('img', {
                        'src': 'icons/kuba_information_icons_set_3.gif'
                    });
                    iicon.setStyle({
                        cursor: 'pointer',
                        width: '20px',
                        height: '20px',
                        backgroundColor: 'transparent'
                    });
                    var hrefOnClick = cc.down(".event_link").href;
                    iicon.onclick = function(){
                        window.location = hrefOnClick;
                    }
                    cc.update(iicon);
                });
            }
        },
        setSameColWidths : function() {
            var totalWidth = 0;
            if($$("#content_main .monthHtml").length > 0) {
                var counter = 1; // 0 = week of year
                var tds = $$("#content_main .monthHtml > table > tbody > tr > td");
                while(counter < 8) {
                    totalWidth += parseInt(Element.getWidth(tds[counter++]));
                }
                counter = 0;
                tds.each(function(td){
                    if(counter != 0) {
                        td.setStyle({
                            width: (totalWidth / 7) +"px"
                        });
                        if(counter == 7) {
                            counter = 0;
                        } else {
                            counter++;
                        }
                    } else {
                        counter++;
                    }
                });
            } else { // week view
                $$("#content_main .weekHtml table td").each(function(td){
                    totalWidth += parseInt(Element.getWidth(td));
                });
                $$("#content_main .weekHtml table td").each(function(td){
                    td.setStyle({
                        width: (totalWidth / 7) +"px"
                    });
                });
            }
        }
    },
    Booking : {
        DirectBookingPanel : {
            init : function() {
                $$("#body_book #canvas_2 .monthHtml table td").each(function(monthtd){
                    monthtd.setStyle({
                        overflow: "hidden",
                        width: Element.getStyle(monthtd, "width")
                    });
                    monthtd.observe("mouseover", function(){
                        if(monthtd.down("div.calContent")) {
                            monthtd.down("div.calContent").show();
                        }
                    });
                    monthtd.observe("mouseout", function(){
                        if(monthtd.down("div.calContent")) {
                            monthtd.down("div.calContent").hide();
                        }
                    });
                });
                $$("#body_book #canvas_2 .monthHtml table td .calContent").each(function(calContent){
                    calContent.hide();
                });
                // move request panel into body
                var tmp = $("js_booking_request_panel");
                $("js_booking_request_panel").remove();
                Element.insert(document.body, tmp);
                $("js_booking_request").setStyle({
                    backgroundColor: "white",
                    width: "100%",
                    padding: "3px",
                    height: ""
                });
                var cumulativeOffset = $("js_booking_request").down("a").cumulativeOffset();
                tmp.setStyle({
                    top: cumulativeOffset.top + "px",
                    left: cumulativeOffset.left + "px",
                    width: "350px",
                    padding: "3px",
                    height: "",
                    position: "absolute",
                    zIndex: "999",
                    cursor: "move",
                    textAlign: "center"
                });
                // add close
                var close = DasCalendar.getCloseLink($("js_booking_request_panel"));
                $("js_booking_request_panel").insert({
                    top: new Element("div", {
                        "class": "bubble_navi"
                    }).insert(close)
                });

                $("js_booking_request").down("a").onclick = function(){
                    return false;
                }
                new Draggable($("js_booking_request_panel"));
                tmp.down("form").onsubmit = function(){
                    return false;
                };
                tmp.down("button[type=submit]").observe("click", function(){
                    var params = Form.serialize($$("#js_booking_request_panel button[type=submit]")[0].up("form"));
                    var responsePanel = $("js_responsePanel");
                    if(!responsePanel) {
                        responsePanel = new Element("div", {
                            id: "js_responsePanel"
                        });
                        $("js_booking_request_panel").insert(responsePanel);
                    }
                    Base.WaitingIcon.show(responsePanel);
                    window.setTimeout(function() { // dont stress user with information overload
                        Base.Ajax("directbookingrequest.json", {
                            parameters: params,
                            method: "post",
                            onSuccess: function(response) {
                                responsePanel.hide();
                                responsePanel.update("");
                                if(response.responseJSON == null) {
                                    responsePanel.update("Changes NOT done. Your session may be timed out."); // INTLANG
                                    window.setTimeout(function(){window.location.reload()}, 5000);
                                } else if(response.responseJSON.statusid == -1) { // booked up
                                    responsePanel.insert(response.responseJSON.statusmessage_long);
                                } else if(response.responseJSON.statusid == 1) { // BOOKING_AVAILABLE
                                    response.responseJSON.availableBookings.each(function(availableBooking){
                                        responsePanel.insert(availableBooking.statusmessage_long);
                                    });
                                } else { // e.g. NOT_POSSIBLE_THIS_WAY
                                    responsePanel.insert(response.responseJSON.statusmessage_long);
                                }
                                Effect.BlindDown(responsePanel);
                            },
                            onFailure: function() {
                                responsePanel.update("sorry for a failure. please try later. we fix it soon."); // INTLANG
                            }
                        });
                    }, 100);
                });

                // init select onchange behaviour
                if($("js_booking_request_panel_select_anchor")) {
                    if(JSSWBrowser.isOpera()) {
                        $("js_booking_request_panel_select_anchor").remove();
                    } else {
                        $("js_booking_request_panel_select_anchor").onclick = function(){
                            var query4 =$("js_booking_request_panel_select").value;
                            var divccs = [];
                            if($$("#content_main .monthHtml").length > 0) {
                                $$("div.calContent").each(function(divcc){
                                    divccs.push(divcc.up("td"));
                                });
                            } else { // week view
                                divccs = $$("#content_main .weekHtml table td div");
                            }
                            divccs.each(function(pictd){
                                var bgimg = pictd.getStyle("background-image");
                                var query = bgimg.toQueryParams();
                                if(query.a) { // it's a td with pic'
                                    query.i = query4;
                                    query.ajax = "1";
                                    query.nocache = new Date().getTime();
                                    var newBgImage = bgimg.substr(0,bgimg.indexOf("?") + 1) + Object.toQueryString(query);
                                    pictd.setStyle({
                                        backgroundImage: newBgImage
                                    });
                                }
                            });
                            var oldHTML = $("js_booking_request_panel_select_anchor").innerHTML;
                            Base.WaitingIcon.show($("js_booking_request_panel_select_anchor"));
                            window.setTimeout(function(){
                                $("js_booking_request_panel_select_anchor").innerHTML = oldHTML
                            }, 2000);
                            return false;
                        };
                    }
                }
            },
            doRequestClick : function() {
                if($("js_booking_request_panel").visible()) {
                    Effect.BlindUp($("js_booking_request_panel"));
                } else {
                    var cumulativeOffset = $("js_booking_request").down("a").cumulativeOffset();
                    $("js_booking_request_panel").setStyle({
                        left: cumulativeOffset.left + "px",
                        top: (cumulativeOffset.top + $("js_booking_request").getHeight() + 2) + "px"
                    });
                    Effect.BlindDown($("js_booking_request_panel"));
                }
            }
        },
        doBooking : function(event, canvas, eldown, elup, frame, left, top, width, height) {
            if(height < 0) {
                height = Math.abs(height);
                top = top - height;
            }
            if(width < 0) {
                width = Math.abs(width);
                left = left - width;
            }
            if(height > 3) {
                var tddown = eldown.up("td");
                if(tddown) {
                    // get td of mouse up
                    var tdup = elup.up("td");
                    if(!tdup) { // up outside calendar
                        var tmp;
                        if(frame.cumulativeOffset()[0] < DasCalendar.Notices.Tmp.firstTdLeft) { // left from calendar
                            tdup = $$('div.weekHtml > table td')[0];
                            tmp = DasCalendar.Notices.Tmp.firstTdLeft;
                            width= width + tmp - left;
                            left = tmp;
                        } else if((frame.cumulativeOffset()[0] + width) > DasCalendar.Notices.Tmp.lastTdRight) { // right from calendar
                            tdup = $$('div.weekHtml > table td')[6];
                            width = DasCalendar.Notices.Tmp.firstTdRight - left;
                        } else {
                            var tds = $$('div.weekHtml > table td');
                            var i=0;
                            while(tds[i] && tds[i].cumulativeOffset()[0] < left) {
                                i++;
                            }
                            tdup = i==0 ? tds[0] : tds[i-1];
                        }
                        if((top + ScrollingOffset.getY()) < tdup.cumulativeOffset()[1]) {
                            tmp = tdup.cumulativeOffset()[1] - ScrollingOffset.getY();
                            height -= (tmp - top);
                            top = tmp;
                        }

                    }
                    if(tdup) {
                        var booking = new Element("div").setStyle({
                            position: "absolute",
                            zIndex: "999", // go behind menu bar
                            backgroundColor: "white",
                            marginTop: "0px",
                            marginLeft: "0px",
                            border: "2px outset black",
                            padding: "5px",
                            left: left + "px",
                            top: top + "px"
                        });
                        var cumulativeOffset = tddown.cumulativeOffset();
                        booking.setStyle({
                            left: cumulativeOffset.left + "px",
                            top: cumulativeOffset.top + "px",
                            width: tddown.getWidth() + "px",
                            height: tddown.getHeight() + "px"
                        });
                        var bookingTopStartOfDay = parseInt(booking.getStyle("top"));
                        var paddingAndBorder = 14;
                        var newHeight = (height - paddingAndBorder) < 50 ? 50 : (height - paddingAndBorder);
                        booking.setStyle({
                            height: newHeight + "px",
                            top: top + parseInt(ScrollingOffset.getY()) + "px",
                            width: (parseInt(booking.style.width) - paddingAndBorder) + "px"
                        });
                        var bookingTopSet = parseInt(booking.getStyle("top"));
                        Base.WaitingIcon.show(booking);
                        var dayMinutesShown = (CalendarMetrics.endMinutesOfDay - CalendarMetrics.startMinutesOfDay);
                        var posRelDay = bookingTopSet - bookingTopStartOfDay;
                        var dayHeight = parseInt(Element.getHeight(tdup.down("div")));
                        var tfstart = posRelDay / dayHeight  * dayMinutesShown;
                        var tfend = (posRelDay + height) / dayHeight * dayMinutesShown;
                        var timeStart = DasCalendar.MinutesAndPixels.getTime(tfstart);
                        var timeEnd = DasCalendar.MinutesAndPixels.getTime(tfend);
                        booking.insert("&nbsp;checking time<br />" + timeStart + " - " + timeEnd); // INTLANG
                        document.body.insert(booking);
                        window.setTimeout(function() { // dont stress user with information overload
                            Base.Ajax("directbookingrequest.json", {
                                parameters: window.location.search + "&tdid=" + tddown.identify() + "&tfstart=" + Math.floor(tfstart) + "&tfend=" + Math.floor(tfend),
                                method: "post",
                                onSuccess: function(response) {
                                    booking.update("");
                                    booking.insert(DasCalendar.getCloseLink(booking));
                                    if(response.responseJSON.statusid == -2) { // booking not possible
                                        booking.insert(response.responseJSON.statusmessageShort);
                                        var useThisLink = new Element("a", {
                                            href: "#"
                                        }).update("Please use this link."); // INTLANG
                                        useThisLink.onclick = function() {
                                            Effect.ScrollTo($("js_booking_request").down("a"), {
                                                afterFinish: function() {
                                                    new Effect.Highlight($("js_booking_request"), {
                                                        startcolor: "#9C0F0F",
                                                        duration: 3.0
                                                    });
                                                },
                                                offset: -100
                                            });
                                        };
                                        booking.insert(useThisLink);
                                        booking.setStyle({
                                            height: ""
                                        });
                                    }
                                    if(response.responseJSON.statusid == -1) { // booked up
                                        booking.insert(response.responseJSON.statusmessageShort);
                                        booking.setStyle({
                                            height: ""
                                        });
                                        window.setTimeout(function(){
                                            Effect.Shake(booking);
                                        }, 500);
                                    }
                                    if(response.responseJSON.statusid == 1) { // available
                                        var json = response.responseJSON.availableBookings[0];
                                        booking.insert(json.statusmessageShort.toString());
                                        var diff2startOfDayY1 = DasCalendar.MinutesAndPixels.getY(json.newMinutesY1, tdup.down("div"));
                                        var newTop = diff2startOfDayY1 + bookingTopStartOfDay;
                                        var diff2startOfDayY2 = DasCalendar.MinutesAndPixels.getY(json.newMinutesY2, tdup.down("div"));
                                        var newHeight = diff2startOfDayY2 - diff2startOfDayY1 - paddingAndBorder;
                                        var oldTop = booking.getStyle("top");
                                        booking.setStyle({
                                            top: newTop + "px",
                                            height: "",
                                            overflowX: "visible",
                                            zIndex: "999"
                                        });
                                        var showRealHeight = function() {
                                            booking.setStyle({
                                                height: newHeight + "px",
                                                overflowX: "hidden",
                                                zIndex: "998"
                                            });
                                        }
                                        if(parseInt(booking.getHeight()) > newHeight) {
                                            window.setTimeout(showRealHeight, 3000);
                                            booking.onmouseover = function() {
                                                booking.setStyle({
                                                    height: "",
                                                    overflowX: "visible",
                                                    zIndex: "999"
                                                });
                                            }
                                            booking.onmouseout = showRealHeight;
                                        } else {
                                            showRealHeight();
                                        }
                                        if(Math.abs(parseInt(oldTop) - parseInt(newTop)) > 200) {
                                            window.setTimeout(function(){
                                                Effect.ScrollTo(booking, {
                                                    afterFinish: function() {
                                                        new Effect.Highlight(booking);
                                                    },
                                                    offset: -100
                                                });
                                            }, 500);
                                        }
                                    }
                                },
                                onFailure: function() {
                                    var mana = new Element("a", {
                                        "href": $$("a[title~=month view]")[0].href
                                    }).update("sorry for a failure. please try to book here or try later. we fix it soon."); // INTLANG
                                    booking.update(mana);
                                }
                            });
                        }, 2000);
                    }
                }
            }
        },
        init : function() {
            DragFrame.init($$("div.weekHtml > table")[0], DasCalendar.Booking.doBooking);
        }
    },
    MinutesAndPixels : {
        getTime: function(minutesOfDay) {
            return Math.floor(minutesOfDay / 60) + ":" + (Math.floor(minutesOfDay % 60) < 10 ? "0" : "") + Math.floor(minutesOfDay % 60);
        },
        getY: function(minutes, dayElement) {
            return (minutes - CalendarMetrics.startMinutesOfDay) / CalendarMetrics.endMinutesOfDay * Element.getHeight(dayElement);
        }
    },
    getCloseLink : function(el2c, functionOnClick) {
        functionOnClick = functionOnClick ? functionOnClick : function(){};
        var text = "close"; // INTLANG
        var close = new Element("a", {
            "href": "#"
        }).update(text);
        close.onclick = function() {
            Effect.BlindUp(el2c);
            functionOnClick(el2c);
            return false;
        }
        return close;
    },
    Forms : {
        init : function() {
            if($("js_select_date")) {
                Element.show($("js_select_date"));
                DasCalendar.instance = Base.Calendar.getPopUpCalendar();
                DasCalendar.instance.setReturnFunction("DasCalendar.gotodate");
                $("js_select_date").onclick = function(){
                    DasCalendar.instance.select($("hidden_pseudo_id"),"js_select_date","dd.MM.yyyy");
                }
            }
            if($("js_booking_request")) {
                Element.show($("js_booking_request"));
                DasCalendar.Booking.DirectBookingPanel.init();
                $("js_booking_request").down("a").onclick = function(){
                    DasCalendar.Booking.DirectBookingPanel.doRequestClick();
                    return false;
                };
            }
        }
    },
    Notices : {
        getHeight : function() {
            return ((DasCalendar.Notices.Tmp.booking.end_minutes - DasCalendar.Notices.Tmp.booking.start_minutes) / (CalendarMetrics.endMinutesOfDay - CalendarMetrics.startMinutesOfDay) * Element.getHeight(DasCalendar.Notices.Tmp.dayPanel)) - 5 + "px"; // -5 = padding
        },
        getY : function() {
            return ((DasCalendar.Notices.Tmp.booking.start_minutes - CalendarMetrics.startMinutesOfDay) / CalendarMetrics.endMinutesOfDay * Element.getHeight(DasCalendar.Notices.Tmp.dayPanel)) + "px";

        },
        insert : function() {
            var booking = DasCalendar.Notices.Tmp.booking;
            var id = booking.start_minutes + "" + booking.dayOfYear;
            var iiconid = 'iicon_' + id;
            var bubbleid = 'bubble_' + id;
            var bubble = null;
            var iicon = null;
            var sep = "&nbsp;";
            if($(iiconid)) {
                bubble = $(bubbleid);
                iicon = $(iiconid);
            } else { // icon on this pos does not exist so far
                // set position dummy relative to day
                var posDummy = new Element("div");
                posDummy.setStyle({
                    position: 'relative',
                    top: DasCalendar.Notices.getY(),
                    padding: "0",
                    margin: "0",
                    height: "20px",
                    width: "20px"
                });
                DasCalendar.Notices.Tmp.dayPanel.update(posDummy);
                // create info icon and pos it absolute cloned to dummy
                iicon = new Element('img', {
                    'src': 'icons/kuba_information_icons_set_3.gif',
                    'id': iiconid
                });
                iicon.setStyle({
                    position: 'absolute',
                    top: DasCalendar.Notices.getY(),
                    padding: "0",
                    margin: "0",
                    cursor: 'pointer',
                    width: '20px',
                    height: '20px'
                });
                document.body.insert(iicon);
                var cumulativeOffset = posDummy.cumulativeOffset();
                iicon.setStyle({
                    left: cumulativeOffset.left + "px",
                    top: cumulativeOffset.top + "px",
                    width: posDummy.getWidth() + "px",
                    height: posDummy.getHeight() + "px"
                });
                iicon.onclick = function() {
                    if($(bubbleid).visible()) {
                        Effect.BlindUp($(bubbleid));
                    } else {
                        $$("div.bubble").each(function(bubble){
                            bubble.hide();
                        });
                        Effect.BlindDown($(bubbleid));
                    }
                }
                bubble = new Element('div', {
                    "class": "bubble",
                    "id" : bubbleid
                });

                document.body.insert(bubble);
                bubble.setStyle({
                    position: "absolute"
                });
                cumulativeOffset = iicon.cumulativeOffset();
                bubble.setStyle({
                    left: cumulativeOffset.left + "px",
                    top: cumulativeOffset.top + "px",
                    width: iicon.getWidth() + "px",
                    height: iicon.getHeight() + "px"
                });
                bubble.setStyle({
                    position: "absolute",
                    zIndex: "999", // go behind menu bar
                    marginTop: "22px",
                    marginLeft: "22px",
                    padding: "5px",
                    width: "auto",
                    height: "auto",
                    display: "none",
                    opacity: "0.8"
                });
                if(JSSWBrowser.isIE6x()) {
                    bubble.setStyle({
                        backgroundColor: "white",
                        height: "300px",
                        opacity: "1.0",
                        width: "250px",
                        padding: "0px"
                    });
                }

                // insert close | close all | show all
                var close = DasCalendar.getCloseLink(bubble);
                bubble.insert(new Element("div", {
                    "class": "bubble_navi"
                }).insert(close));

                // mouse over and out behaviour
                bubble.onmouseover = function() {
                    bubble.setStyle({
                        zIndex: 999,
                        opacity: "1.0"
                    });
                }
                bubble.onmouseout = function() {
                    bubble.setStyle({
                        zIndex: 980,
                        opacity: JSSWBrowser.isIE() ? "1.0" : "0.8"

                    });
                }

            }
            // from now on booking specific infos

            
            // insert status | notices | contact
            var status = new Element("a", {
                "href": "#status",
                "class" : "tab_single_status tab_statuses tab"
            }).update("status"); // INTLANG
            var notice = new Element("a", {
                "href": "#notice",
                "class" : "tab_single_notice tab_notices tab"
            }).update("notice"); // INTLANG
            var contact = new Element("a", {
                "href": "#contact",
                "class" : "tab_single_contact tab_contacts tab"
            }).update("contact"); // INTLANG
            [status, notice, contact].each(function(tab){
                tab.onclick = function(ewent){ // do not name it event!w
                    var clickedEl = tab.hash.substr(1);
                    $$("#bubble_navi_" + booking.id + " a").each(function(a){
                        a.setStyle({
                            color: "",
                            borderBottom: "1px solid black"
                        })
                    });
                    ["status", "contact", "notice"].each(function(key){
                        $("bubble_tab_panel_" + key + booking.id).hide();
                    });
                    $("bubble_tab_panel_" + clickedEl + booking.id).show();
                    $$("#bubble_navi_" + booking.id + " a.tab_single_"+clickedEl)[0].setStyle({
                        color: "black",
                        borderBottom: "1px solid white"
                    });
                    if(ewent && ewent.findElement()) {
                        ewent.findElement().blur();
                    }
                    return false;
                };
            });
            bubble.insert(new Element("div", {
                "class": "bubble_navi",
                "id": "bubble_navi_" + booking.id
            }).insert(status).insert(sep).insert(notice).insert(sep).insert(contact)); // INTLANG

            // insert time
            var time = new Element("p").setStyle({
                fontWeight: "bolder"
            }).update(booking.start_time + " - " + booking.end_time + " (" + booking.username + ")");
            bubble.insert(time);


            // insert tab panels
            var statusDetails = new Element("div").update(new Element("div").setStyle({"float": "left"}).insert(booking.status)).insert(new Element("div").update(booking.capacityUnits).insert(new Element("p").update(booking.statustext)));
            var contactDetails = new Element("ul").insert(new Element("li").update(booking.fullname));
            if(booking.phone1 != null && !booking.phone1.blank()) {
                contactDetails.insert(new Element("li").update("landline: " + booking.phone1)); // INTLANG
            }
            if(booking.phone2 != null && !booking.phone2.blank()) {
                contactDetails.insert(new Element("li").update("mobile: " + booking.phone2)); // INTLANG
            }
            if(booking.address != null && !booking.address.blank()) {
                contactDetails.insert(new Element("li").update("address: " + booking.address)); // INTLANG
            }
            if(booking.mail != null && !booking.mail.blank()) {
                contactDetails.insert(new Element("li").update("mail: " + booking.mail)); // INTLANG
            }
            if(booking.cds != null) {
                booking.cds.each(function(cd){
                    contactDetails.insert(new Element("li").update(cd.title + ": " + cd.detail));
                });
            }
            
            // build notice element
            var noticeTextarea = new Element("textarea", {
                "name" : "js_notice_"+booking.id,
                "id": "js_notice_"+booking.id,
                "rows": "20",
                "cols" : "20"
            }).insert(booking.notice);
            var submitButton = null;
            if(booking.sessionAlreadyBegun) {
                submitButton = new Element("span", {
                    "type": "submit"
                }).update("not changeable anymore"); // INTLANG
                noticeTextarea.writeAttribute("readonly", "readonly");
                noticeTextarea.setStyle({
                    border: "0px solid black"
                });
            } else {
                submitButton = new Element("button", {
                    "type": "submit"
                }).update("update notice"); // INTLANG
            }
            submitButton.onclick = function() {
                var oldSubmitButtonInnerHtml = submitButton.innerHTML;
                Base.WaitingIcon.show(submitButton);
                Base.Ajax("bookingnoticeupdate.json", {
                    parameters: "v=" + booking.id + "&w=" + encodeURIComponent($("js_notice_"+booking.id).value),
                    method: "post",
                    onSuccess : function(response) {
                        submitButton.innerHTML = response.responseJSON.message;
                        window.setTimeout(function(){
                            submitButton.innerHTML = oldSubmitButtonInnerHtml;
                        }, 5000);
                    },
                    onFailure : function() {
                        submitButton.innerHTML = "sorry for a failure"; // INTLANG
                        window.setTimeout(function(){
                            submitButton.innerHTML = oldSubmitButtonInnerHtml;
                        }, 5000);
                    }
                });
            }
            noticeTextarea.setStyle({
                width: "200px",
                height: "100px"
            });
            var noticeElement = new Element("div").insert("Notice processable by all").insert(new Element("br")).insert(noticeTextarea).insert(new Element("br")).insert(submitButton);

            var btp_status = new Element("div", {
                "class" : "bubble_tab_panel bubble_tab_panel_statuses",
                "id" :  "bubble_tab_panel_status" + booking.id
            }).insert(statusDetails);
            bubble.insert(btp_status);
            var btp_contact = new Element("div", {
                "class" : "bubble_tab_panel bubble_tab_panel_contacts",
                "id" :  "bubble_tab_panel_contact" + booking.id
            }).insert(contactDetails);
            bubble.insert(btp_contact);
            var btp_notices = new Element("div", {
                "class" : "bubble_tab_panel bubble_tab_panel_notices",
                "id" :  "bubble_tab_panel_notice" + booking.id
            }).insert(noticeElement);
            bubble.insert(btp_notices);
            btp_status.show();
            status.setStyle({
                color: "black",
                borderBottom: "1px solid white"
            });
            btp_contact.hide();
            btp_notices.hide();
            return iicon;
        },
        Tmp : {
            booking : null,
            day : null,
            dayPanel : null,
            lastTdRight : null,
            firstTdLeft : null
        },
        setTmpVars : function(booking) {
            DasCalendar.Notices.Tmp.day = $("dayOfYear_" + booking.dayOfYear);
            DasCalendar.Notices.Tmp.firstTdLeft = parseInt($$('div.weekHtml > table td')[0].cumulativeOffset()[0]);
            DasCalendar.Notices.Tmp.lastTdRight = parseInt($$('div.weekHtml > table td')[6].cumulativeOffset()[0]) + parseInt(Element.getWidth($$('div.weekHtml > table td')[6]));
            DasCalendar.Notices.Tmp.dayPanel = DasCalendar.Notices.Tmp.day.down("div");
            DasCalendar.Notices.Tmp.booking = booking;
        },
        initMenuBar : function() {
            $("js_showOnViewname_bookfacilities").show();
            new Effect.Highlight($("js_showOnViewname_bookfacilities"));

            // set menu item functions
            [$("js_mbi_bookfacilities_statuses"), $("js_mbi_bookfacilities_notices"), $("js_mbi_bookfacilities_contacts")].each(function(menuitem){
                menuitem.onclick = function(ewent){
                    $$(".bubble").each(function(bubble){
                        bubble.hide();
                    });
                    var clickedElKey = menuitem.hash.substr(1);
                    $$(".bubble_tab_panel").each(function(tabpan){
                        tabpan.hide();
                    });
                    $$(".bubble_navi a").each(function(nav){
                        nav.setStyle({
                            color: "",
                            borderBottum: "0px solid black"
                        })
                    });
                    $$(".bubble_navi a.tab_"+clickedElKey).each(function(nav){
                        nav.setStyle({
                            color: "black",
                            borderBottum: "1px solid white"
                        })
                    });
                    $$(".bubble_tab_panel_" + clickedElKey).each(function(tabpan){
                        tabpan.show();
                    });
                    $$(".bubble").each(function(bubble){
                        Effect.BlindDown(bubble);
                    });
                    return false;
                };
            });

            $("js_mbi_bookfacilities_closeall").onclick = function() {
                $$(".bubble").each(function(bubble){
                    Effect.BlindUp(bubble);
                });
                return false;
            }
            $("js_mbi_bookfacilities_showall").onclick = function(){
                $$(".bubble").each(function(bubble){
                    Effect.BlindDown(bubble);
                });
                return false;
            }


        },
        init : function() {
            var planB1 = $$("div.weekHtml")[0].innerHTML;
            $$('div.weekHtml > table td').each(function(td) {
                td.down("div").setStyle({
                    padding: "0",
                    margin: "0"
                });
                Base.WaitingIcon.show(td.down("div"));
            });

            Base.Ajax("getbookings.json", {
                parameters: window.location.search,
                method: "post",
                onSuccess : function(response) {
                    $$('div.weekHtml > table td').each(function(td) {
                        td.down("div").innerHTML = "";
                    });
                    response.responseJSON.bookings.each(function(booking){
                        if($("dayOfYear_" + booking.dayOfYear)) { // is booking from this week
                            DasCalendar.Notices.setTmpVars(booking);
                            DasCalendar.Notices.insert();
                        }
                    });
                    if(response.responseJSON.bookings.length > 0) {
                        DasCalendar.Notices.initMenuBar();
                    }
                },
                onFailure : function() {
                    $$('div.weekHtml')[0].innerHTML = planB1;
                }
            });
        }
    },
    gotodate: function(y, m, d) {
        var params = window.location.search.toQueryParams();
        params.b=y;
        params.c=m-1;
        params.d=d;
        loc = window.location+"";
        window.location.replace(loc.substr(0, loc.indexOf("?")) + "?" + $H(params).toQueryString());
    }
}

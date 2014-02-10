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
  ContactDetails.init();
});
var ContactDetails = {
  init : function() {
    ContactDetails.HTMLCustomFields = $('customFields');
    ContactDetails.EditDetail.init();
    ContactDetails.DeleteDetail.init();
    ContactDetails.AddFreeDetail.init();
    ContactDetails.BirthdayCalendar.init();
    ContactDetails.Charslimit.init();
    Base.DepartmentInput.init();
  }
}
// cache for custom fields input, not inserted after ajax request
ContactDetails.HTMLCustomFields = null;
ContactDetails.ChangeUser = {};
ContactDetails.Elements = {};
ContactDetails.EditDetail = {};
ContactDetails.DeleteDetail = {};
ContactDetails.AddFreeDetail = {};
ContactDetails.BirthdayCalendar = {};
ContactDetails.Charslimit = {};

ContactDetails.Charslimit.init = function() {
  if (ValidationConfiguration.charslimit) {
    ContactDetails.Charslimit.exec();
    ValidationConfiguration.charslimit.each(function(charslimit_obj) {
      if($(charslimit_obj.content_id) != null) {
        Event.observe($(charslimit_obj.content_id), "keyup", function() {
          ContactDetails.Charslimit.exec();
          return false;
        });
      }
    });
  }
};
ContactDetails.Charslimit.exec = function() {
  ValidationConfiguration.charslimit.each(function(charslimit_obj) {
    if ($(charslimit_obj.content_id) != null && $(charslimit_obj.view_id) != null) {
      var left = charslimit_obj.limit - $F(charslimit_obj.content_id).length;
      $(charslimit_obj.view_id).update(left);
      if (left < 0) {
        $(charslimit_obj.view_id).addClassName("warning");
      } else {
        $(charslimit_obj.view_id).removeClassName("warning");
      }
    }
  });
};

ContactDetails.BirthdayCalendar.init = function() {
  if ($("js_birthday")) {
    $("js_birthday").show();
    var calendar = Base.Calendar.getPopUpCalendar();
    var ynow = new Date().getFullYear();
    calendar.addDisabledDates("Jan 01, " + ynow, null);
    $("js_birthday").onclick = function() {
      calendar.select($('birthdate_id'), 'js_birthday', 'dd.MM.yyyy', '01.01.' + (ynow - 25));
      return false;
    }
  }
};

ContactDetails.show = function(el2show) {
  $('js_actionPanel').update('');
  el2show.show();
  $('js_actionPanel').insert(el2show).insert(new Element("button", {
    href : "javascript:return false;",
    id : "js_back"
  }).update("<span class='image'></span>Cancel").addClassName('icon').addClassName('cancel'));
  ContactDetails.BirthdayCalendar.init();
  ContactDetails.Charslimit.init();
  Base.Effects.showAndHide($('js_actionPanel'), $('js_overview'));
  Event.observe($('js_back'), 'click', function() {
    Base.Effects.showAndHide($('js_overview'), $('js_actionPanel'));
  });
};

ContactDetails.EditDetail.init = function() {
  $$('.js_edit').each(function(editel) {
    editel.innerHTML = '<span class="image"></span>Edit';
    Event.observe(editel, 'click', function() {
      var el2show = new Element('div', {
        id : 'js_editContainer'
      });
      var classKey = editel.up('form').getInputs('hidden', 'aa').shift().getValue();
      if (classKey.match(/^cd_.*/)) {
        var cdId = classKey.substr(3);
        var freeInputTitle = new Element('input', {
          type : 'text',
          value : $F($$('input[name=title_' + cdId + ']')[0]),
          name : 'title_' + cdId
        });
        var freeInputDetail = new Element('input', {
          type : 'text',
          value : $F($$('input[name=detail_' + cdId + ']')[0]),
          name : 'detail_' + cdId
        });
        el2show.insert(new Element('p').update('Title:<br />').insert(freeInputTitle));
        el2show.insert(new Element('p').update('Contact:<br />').insert(freeInputDetail));
      } else {
        $$('td.js_' + classKey).each(function(tdel) {
          el2show.insert(new Element('p').update(tdel.innerHTML));
        });
      }
      var sendButton = new Element('button', {
        href : "javascript:return false;"
      }).update("<span class='image'></span>Save").addClassName('icon').addClassName('save');
      el2show.insert(sendButton);
      Event.observe(sendButton, 'click', function() {
        var params = editel.up('form').serialize() + '&' + Form.serialize(el2show);
        // TODO ValidationConfiguration should be used to validate the field, but it's written in jQuery
        new Ajax.Request('savecontactdetail.json', {
          parameters : params,
          method : "post",
          onSuccess : function(r) {
            if (r.responseJSON.succ == 1) {
              if (r.responseJSON.customFieldUpdated) {
                Effect.Pulsate('content_sub', {
                  pulses : 1,
                  duration : 0.5
                });
              }
              var table = new Element('table').update(r.responseJSON.summaryTable);
              table.appendChild(ContactDetails.HTMLCustomFields);
              table.setAttribute('id', 'contactDetailsSummary');
              $('contactDetailsSummary_container').update(table);
              $('js_overview').down('h3').update(r.responseJSON.fullName);
              Base.init();
              ContactDetails.EditDetail.init();
              ContactDetails.DeleteDetail.init();
              ContactDetails.AddFreeDetail.init();
              Base.Effects.showAndHide($('js_overview'), $('js_actionPanel'));
              if (r.responseJSON.showCompleteMessage) {
                window.setTimeout(function() {
                  $('home_link').setStyle({
                    fontSize : "100%"
                  });
                  $('home_link').down('a').update("Contact details are complete. This way to home page.");
                  Effect.Pulsate('home_link', {
                    pulses : 5,
                    duration : 3.0
                  });
                }, 3500);
              }
            } else if (r.responseJSON.succ == 2) {
              alert("This input is not allowed. Are all entries made?");
            } else {
              alert("bad request [code 201001301329]");
            }
          },
          onFailure : function(r) {
            alert("error [code 201001301328]: " + r.responseText);
          }
        });
      });
      ContactDetails.show(el2show);
    });
  });
};

ContactDetails.DeleteDetail.init = function() {
  $$('.delete').each(function(delel) {
    Event.observe(delel, 'click', function() {
      Base.WaitingIcon.show(delel);
    });
  });
};

ContactDetails.AddFreeDetail.init = function() {
  $$('.js_add').each(function(addel) {
    Event.observe(addel, 'click', function() {
      var el2show = new Element('div', {
        id : 'js_editContainer'
      }).update(new Element('h1').update('Add contact details'));
      var freeInputTitle = new Element('input', {
        type : 'text',
        value : '',
        name : 'title_new'
      });
      var freeInputDetail = new Element('input', {
        type : 'text',
        value : '',
        name : 'detail_new'
      });
      el2show.insert(new Element('p').update('Title:<br />').insert(freeInputTitle));
      el2show.insert(new Element('p').update('Contact:<br />').insert(freeInputDetail));
      var sendButton = new Element('button', {
        href : "javascript:return false;"
      }).update("<span class='image'></span>Save").addClassName('icon').addClassName('save');
      el2show.insert(sendButton);
      Event.observe(sendButton, 'click', function() {
        var titleSet = $F(freeInputTitle);
        var detailSet = $F(freeInputDetail);
        var params = Form.serialize(el2show) + "&aa=cd_new&user_id=" + $F($$('input[name=user_id]')[0]);
        new Ajax.Request('savecontactdetail.json', {
          parameters : params,
          method : "post",
          onSuccess : function(r) {
            if (r.responseJSON.succ == 1) {
              var table = new Element('table').update(r.responseJSON.summaryTable);
              table.appendChild(ContactDetails.HTMLCustomFields);
              table.setAttribute('id', 'contactDetailsSummary');
              $('contactDetailsSummary_container').update(table);
              Base.init();
              ContactDetails.EditDetail.init();
              ContactDetails.DeleteDetail.init();
              ContactDetails.AddFreeDetail.init();
              var freeInputTitle = new Element('input', {
                type : 'text',
                value : titleSet,
                name : 'title_' + r.responseJSON.updatedId
              });
              var freeInputDetail = new Element('input', {
                type : 'text',
                value : detailSet,
                name : 'detail_' + r.responseJSON.updatedId
              });
              $('changeContactDetails').insert(freeInputTitle).insert(freeInputDetail);
              Base.Effects.showAndHide($('js_overview'), $('js_actionPanel'));
            } else if (r.responseJSON.succ == 2) {
              alert("This input is not allowed. Are all entries made?");
            } else {
              alert("bad request [code 201001301327]");
            }
          },
          onFailure : function(r) {
            alert("error [code 201001301326]: " + r.responseText);
          }
        });
      });
      ContactDetails.show(el2show);
    });
  });

};
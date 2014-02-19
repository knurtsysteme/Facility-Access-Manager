/*! KNURT Systeme - part of the Facility Access Manager */
$(document).ready(function() {
	Base.User.Dialog.NewUser.init();
	Base.User.Dialog.DeleteUser.init();
	Base.User.Dialog.ShowUser.init();
	Base.User.Dialog.EditUser.init();
	Base.User.Dialog.InitPass.init();

	var today = new Date();
	$("#birthdate_id").datepicker( {
		"dateFormat" : "dd.mm.yy",
		"changeYear" : true,
		"yearRange" : (today.getFullYear() - 80) + ":" + (today.getFullYear() - 14),
		"defaultDate" : '-25y'
	});
	$("#account_expires_id").datepicker( {
		"dateFormat" : "dd.mm.yy",
		"changeYear" : true,
		"yearRange" : (today.getFullYear() - 1) + ":" + (today.getFullYear() + 100),
		"defaultDate" : '+2y'
	});
	$("#edituser_control_birthdate").datepicker( {
		"dateFormat" : "dd.mm.yy",
		"changeYear" : true,
		"yearRange" : (today.getFullYear() - 80) + ":" + (today.getFullYear() - 14),
		"defaultDate" : '-25y'
	});
	$("#edituser_control_account_expires").datepicker( {
		"dateFormat" : "dd.mm.yy",
		"changeYear" : true,
		"yearRange" : (today.getFullYear() - 1) + ":" + (today.getFullYear() + 100),
		"defaultDate" : '+2y'
	});

	Base.User.Charslimit.init();
	Base.User.AccountDefaultExpirationDates.init();
	Base.User.ResponsibilityInput.init();
});

Base.User = {};

Base.User.Dialog = {};
Base.User.Dialog.show = function(dialog) {
	dialog.dialog('open').dialog('moveToTop');
};
Base.User.Dialog.defaultCancel = function(dialog) {
	$(dialog).dialog('close');
};

Base.User.Dialog.DeleteUser = {};
Base.User.Dialog.DeleteUser.dialog = null;
Base.User.Dialog.DeleteUser.init = function() {
	var tmp, data = null;
	Base.User.Dialog.DeleteUser.dialog = $('#as_dialog').clone().attr("id", "").addClass("delete_user_dialog").html($("#deleteuser_control").html()).show().dialog( {
		title : "Delete existing user",
		draggable : true,
		width : '830px',
		modal : false,
		buttons : {
			'Cancel' : function() {
				Base.User.Dialog.defaultCancel(this);
			},
			'OK' : function(e) {
				data = $('.delete_user_dialog').serializeObject();
				if (data.type == 0 || window.confirm("This cannot be undone! Really do it?")) {
					Base.WaitingIcon.showOnPage();
					$.ajax( {
						contentType : "application/json; charset=utf-8",
						dataType : "json",
						data : JSON.stringify(data),
						type : 'POST',
						url : 'want-2-deleteuser.json',
						success : function(r) {
							if (r.messages && r.messages.length > 0) {
								$(r.messages).each(function(i, m) {
									if (m['0']) {
										$.n.error(m['0']);
									} else if (m['1']) {
										$.n.success(m['1']);
									}
								});
							} else {
								$.n.error("An unknown error occured!");
							}
							if (r.succ) {
								$('#a_deleteuser_' + data.user_id + '_' + data.user_username).remove();
								Base.User.Dialog.DeleteUser.dialog.dialog('close');
								$('#please_reload_page').show();
							}
							Base.WaitingIcon.hideOnPage();
						},
						error : function(r) {
							$.n.error("Error 201205021235: " + r.status + " " + r.statusText);
							Base.WaitingIcon.hideOnPage();
						}
					});
				}
			}
		}
	});
	Base.User.Dialog.DeleteUser.dialog.dialog('close');
	$("#deleteuser_control").remove();
	$('a.a_deleteuser').click(function() {
		Base.WaitingIcon.showOnPage();
		tmp = $(this).attr("id").split("_");
		$.ajax( {
			type : 'GET',
			url : 'want-2-getuser.json',
			data : 'user_id=' + tmp[2],
			success : function(answer) {
				$('.js_hide_if_user_excluded').show();
				$('.js_hide_if_user_anonym').show();
				if (typeof answer.succ != 'undefined' && answer.succ) {
					if (answer.user.excluded) {
						$('.js_hide_if_user_excluded').hide();
					}
					if (answer.user.anonym) {
						$('.js_hide_if_user_anonym').hide();
					}
				}
				$('.deleteuser_user_id:input').val(tmp[2]);
				$('.deleteuser_user_username:input').val(tmp[3]);
				$('h3.deleteuser_user_username').html("Delete " + tmp[3]); // INTLANG
			$('span.deleteuser_user_username').html(tmp[3]);
			$('#delete_user_0').attr("checked", true);
			Base.User.Dialog.show(Base.User.Dialog.DeleteUser.dialog);
			Base.WaitingIcon.hideOnPage();
		},
		error : function(r) {
			$.n.error("Error 201205101248: " + r.status + " " + r.statusText);
			Base.WaitingIcon.hideOnPage();
		}
		});
	});
};

Base.User.Dialog.NewUser = {};
Base.User.Dialog.NewUser.init = function() {
	Base.User.Dialog.NewUser.dialog = $('#as_dialog').clone().attr("id", "").html($("#newuser_control").html()).show().dialog( {
		title : "Add a new user",
		draggable : true,
		width : '830px',
		modal : false,
		buttons : {
			'Cancel' : function() {
				Base.User.Dialog.defaultCancel(this);
			},
			'Insert new user' : function(e) {
				Base.WaitingIcon.showOnPage();
				var user = $('.newuser').serializeObject();
				user.male = $('#male_id').val();
				user.title = $('#title_id').val();
				user.fname = $('#fname_id').val();
				user.sname = $('#sname_id').val();
				user.birthdate = $('#birthdate_id').val();
				user.intendedResearch = $('#intendedResearch_id').val();
				user.responsibilities = $('input[name=responsibility]:visible').serializeArray();
				user.mail = $('#mail_id').val();
				user.city = $('#city_id').val();
				user.company = $('#company_id').val();
				user.country = $('#country_id').val();
				user.pass = $('#password_id').val();
				user.street = $('#street_id').val();
				user.streetno = $('#streetno_id').val();
				user.zipcode = $('#zipcode_id').val();
				user.phone1 = $('#phone1_id').val();
				user.phone2 = $('#phone2_id').val();
				user.departmentlabel = $('#departmentlabel_id').val();
				user.account_expires = $('#account_expires_id').val();
				user.roleid = $('#roleid_id').val();
				user.departmentkey = $('#departmentkey_id').val();
				// 
				var data = JSON.stringify(user);
				$.ajax( {
					contentType : "application/json; charset=utf-8",
					dataType : "json",
					data : data,
					type : 'POST',
					url : 'want-2-precheckuserinsertion.json',
					success : function(r) {
						if (r.insertion_impossible) {
							window.alert(r.insertion_impossible_message);
						} else {
							var insert_user = true;
							if (r.user_exists) {
								$.n.error(r.insert_anyway_message);
							}
							if (insert_user) {
								$.ajax( {
									contentType : "application/json; charset=utf-8",
									dataType : "json",
									data : data,
									type : 'POST',
									url : 'want-2-insertuser.json',
									success : function(r) {
										if (r && r.succ) {
											Base.User.Dialog.NewUser.show(r);
											$.n.success("User inserted!");
											Base.User.Dialog.defaultCancel(Base.User.Dialog.NewUser.dialog);
											$('#please_reload_page').show();
										} else {
											if (r.message) {
												$.n.error(r.message);
												Base.User.Dialog.defaultCancel(Base.User.Dialog.NewUser.dialog);
											} else {
												$.n.error("Could not get a response. Please reload page (Strg + F5)");
												Base.User.Dialog.defaultCancel(Base.User.Dialog.NewUser.dialog);
											}
										}
									},
									error : function(r) {
										if (r == null) {
											$.n.error("Error 201111020931: Lost server connection. Reloading page (F5) may deliver more information.");
										} else {
											$.n.error("Error 201111020908: " + r.status + " " + r.statusText);
										}
									}
								});
							}
						}
						Base.WaitingIcon.hideOnPage();
					},
					error : function(r) {
						$.n.error("Error 201011071045: " + r.status + " " + r.statusText);
						Base.WaitingIcon.hideOnPage();
					}
				});
			}
		}
	});
	Base.User.Dialog.NewUser.dialog.dialog('close');
	$("#newuser_control").html("");
	if (window.location.hash == "#newuser") {
		Base.User.Dialog.show(Base.User.Dialog.NewUser.dialog);
	}
	$('a.a_newuser').click(function() {
		Base.User.Dialog.show(Base.User.Dialog.NewUser.dialog);
	});
};
Base.User.Dialog.NewUser.dialog = null;
Base.User.Dialog.NewUser.show = function(user) {
	Base.User.Dialog.show(Base.User.Dialog.NewUser.dialog);
	user = user || {};
	window.location.hash = '#newuser';
	$('#male_id').val(user.male || '');
	$('#title_id').val(user.title || '');
	$('#fname_id').val(user.fname || '');
	$('#sname_id').val(user.sname || '');
	$('#birthdate_id').val(user.birthdate || '');
	$('#intendedResearch_id').val(user.intendedResearch || '');
	$('#mail_id').val(user.mail || '');
	$('#city_id').val(user.city || '');
	$('#company_id').val(user.company || '');
	$('#country_id').val(user.country || '');
	$('#password_id').val(user.pass || '');
	$('#street_id').val(user.street || '');
	$('#streetno_id').val(user.streetno || '');
	$('#zipcode_id').val(user.zipcode || '');
	$('#phone1_id').val(user.phone1 || '');
	$('#phone2_id').val(user.phone2 || '');
	$('#roleid_id').val(user.roleid || '');
	$('#departmentlabel_id').val(user.departmentlabel || '');
	$('#account_expires_id').val(user.account_expires || '');
	$('#departmentkey_id').val(user.departmentkey || '');
	$.each(user.customFields, function(key, value) {
	    // XXX only support text inputs and checkboxes by now
	    if($('#' + key + '_id').attr('type') == 'checkbox') {
	      $('#' + key + '_id').attr('checked', value.length > 0);
	    } else {
	        $('#' + key + '_id').val(value || '');
	    }
	});
	Base.User.AccountDefaultExpirationDates.exec();
};

Base.User.Dialog.EditUser = {};
Base.User.Dialog.EditUser.init = function() {
	Base.User.Dialog.EditUser.dialog = $('#as_dialog').clone().attr("id", "").html($("#edituser_control").html()).show().dialog( {
		title : "Edit user",
		draggable : true,
		width : '550px',
		height : 400,
		modal : false,
		buttons : {
			'Cancel' : function() {
				Base.User.Dialog.defaultCancel(this);
			},
			'Duplicate' : function() {
				Base.User.Dialog.NewUser.show(Base.User.Dialog.EditUser.getUserFromForm());
				$.n.warning('Please change at least email address and set a password');
			},
			'Save changes' : function() {
				Base.WaitingIcon.showOnPage();
				var newuser = Base.User.Dialog.EditUser.getUserFromForm();
				var olduser = Base.User.Dialog.EditUser.userLastShown;
				newuser.username = olduser.username;
				newuser.departmentkey = olduser.departmentkey;
				var updatejson = {};
				updatejson.newuser = newuser;
				updatejson.olduser = olduser;
				$.ajax( {
					contentType : "application/json; charset=utf-8",
					dataType : "json",
					data : JSON.stringify(updatejson),
					type : 'POST',
					url : 'want-2-updateuser.json',
					success : function(r) {
						if (r && r.succ) {
							$.n.success("User updated!");
							if (r.reopened_account_mail_sent) {
								$.n.success("Send mail to inform user about reopened account!");
							}
							$('#please_reload_page').show();
							Base.User.Dialog.defaultCancel(Base.User.Dialog.EditUser.dialog);
						} else {
							if (r && r.message) {
								$.n.error(r.message);
							} else {
								$.n.error("An unknown error occured! Server down?!");
							}
						}
						Base.WaitingIcon.hideOnPage();
					},
					error : function(r) {
						$.n.error("Error 201011071044: " + r.status + " " + r.statusText);
						Base.WaitingIcon.hideOnPage();
					}
				});
			}
		}
	});
	$("#edituser_control").html("");
	$('#edituser_control_contactDetails_add').click(function() {
		var contactDetail = {};
		contactDetail.title = "";
		contactDetail.detail = "";
		var cdhtml = Base.User.Dialog.EditUser.ContactDetail.getTrForm(contactDetail);
		$('#edituser_control_tbody_contactDetails').append(cdhtml);
	});
	$('a.a_edituser').click(function() {
		var user_id = this.id.substr(this.id.lastIndexOf("_") + 1);
		Base.User.Dialog.EditUser.show(user_id);
	});
	Base.User.Dialog.EditUser.dialog.dialog('close');
};
Base.User.Dialog.tableOddEvenize = function(tableid) {
	// $('#'+tableid+' td').css({padding:'15px 0', borderTop: '1px solid
	// black'});
	$('#' + tableid).addClass('standard');
	$('#' + tableid + ' tr').each(function(i, tr) {
		if (i % 2 == 0) {
			$(tr).addClass('odd');
		} else {
			$(tr).addClass('even');
		}
	});
};
Base.User.Dialog.EditUser.dialog = null;
Base.User.Dialog.EditUser.userLastShown = null;
Base.User.Dialog.EditUser.show = function(user_id) {
	$.ajax( {
		type : 'GET',
		url : 'want-2-getuser.json',
		data : 'user_id=' + user_id,
		success : function(answer) {
			if (answer && answer.succ) {
				Base.User.Dialog.EditUser.ContactDetail.id = 0;
				var user = answer.user;
				Base.User.Dialog.EditUser.userLastShown = user;
				$('#edituser_control_fname').val(user.fname || "");
				$('#edituser_control_sname').val(user.sname || "");
				$('#edituser_control_title').val(user.title || "");
				$('#edituser_control_street').val(user.street || "");
				$('#edituser_control_streetno').val(user.streetno || "");
				$('#edituser_control_zipcode').val(user.zipcode || "");
				$('#edituser_control_city').val(user.city || "");
				$('#edituser_control_country').val(user.country || "");
				$('#edituser_control_roleid').val(user.roleid || "");
				if (user.roleid == 'admin') {
					$('#edituser_control_roleid').attr('disabled', true);
				} else {
					$('#edituser_control_roleid').attr('disabled', false);
				}

				$('#edituser_control_responsibility input').attr("checked", false);
				$(user.responsible4facilities).each(function(i, responsible4facility) {
					$('#id_edituser_responsibility_' + responsible4facility.key).attr("checked", true);
				});

				$('#edituser_control_male').val(user.male || "");
				$('#edituser_control_company').val(user.company || "");
				$('#edituser_control_departmentlabel').val(user.departmentlabel || "");
				$('#edituser_control_account_expires').val(user.account_expires || "");
				$('#edituser_control_mail').val(user.mail || "");
				$('#edituser_control_phone1').val(user.phone1 || "");
				$('#edituser_control_username').html(user.username || "-");
				$('#edituser_control_phone2').val(user.phone2 || "");
				$('#edituser_control_pass').val("");
				$('#edituser_control_birthdate').val(user.birthdate || "");
				$('#edituser_control_intendedResearch').val(user.intendedResearch || "");
				$('#edituser_control_excluded').val(user.excluded == null ? 0 : (user.excluded ? 1 : 0));

				$('#edituser_control_tbody_contactDetails').html("");
				$(user.contactDetails).each(function(i, contactDetail) {
					$('#edituser_control_tbody_contactDetails').append(Base.User.Dialog.EditUser.ContactDetail.getTrForm(contactDetail));
				});
                  $.each(user.customFields, function(key, value) {
                    // XXX only support text inputs and checkboxes by now
                  var selector = '#edituser_control_' + key;
                    if($(selector).attr('type') == 'checkbox') {
                      $(selector).attr('checked', value.length > 0);
                    } else {
                        $(selector).val(value || valueNotSet);
                    }
                });
				Base.User.Dialog.tableOddEvenize('edituser_control_table');
				Base.User.Dialog.show(Base.User.Dialog.EditUser.dialog);
			} else {
				$.n.error("Could not find the user.");
			}
		},
		error : function(r) {
			$.n.error("Error 201011071044: " + r.status + " " + r.statusText);
		}
	});
	Base.User.Dialog.show(Base.User.Dialog.EditUser.dialog);
};
Base.User.Dialog.EditUser.getUserFromForm = function() {
	var result = {};
	result.fname = $('#edituser_control_fname').val();
	result.sname = $('#edituser_control_sname').val();
	result.title = $('#edituser_control_title').val();
	result.street = $('#edituser_control_street').val();
	result.streetno = $('#edituser_control_streetno').val();
	result.zipcode = $('#edituser_control_zipcode').val();
	result.city = $('#edituser_control_city').val();
	result.country = $('#edituser_control_country').val();
	result.roleid = $('#edituser_control_roleid').val();
	result.male = $('#edituser_control_male').val();
	result.company = $('#edituser_control_company').val();
	result.departmentlabel = $('#edituser_control_departmentlabel').val();
	result.account_expires = $('#edituser_control_account_expires').val();
	result.mail = $('#edituser_control_mail').val();
	result.phone1 = $('#edituser_control_phone1').val();
	result.responsibilities = $('input[name=responsibility]:visible').serializeArray();
	result.username = $('#edituser_control_username').val();
	result.phone2 = $('#edituser_control_phone2').val();
	result.pass = $('#edituser_control_pass').val();
	result.birthdate = $('#edituser_control_birthdate').val();
	result.intendedResearch = $('#edituser_control_intendedResearch').val();
	result.excluded = $('#edituser_control_excluded').val();
	var i = 0;
	var contactDetails = [];
	while ($('#contactdetail_title_' + i).size() == 1) {
		var contactDetail = {};
		contactDetail.title = $('#contactdetail_title_' + i).val();
		contactDetail.detail = $('#contactdetail_detail_' + i).val();
		if (!SWRegExp.is.nothingOrWhitespace(contactDetail.title) && !SWRegExp.is.nothingOrWhitespace(contactDetail.detail)) {
			contactDetails.push(contactDetail);
		}
		i++;
	}
	result.contactDetails = contactDetails;
	return result;
};
Base.User.Dialog.EditUser.ContactDetail = {};
Base.User.Dialog.EditUser.ContactDetail.id = 0;
Base.User.Dialog.EditUser.ContactDetail.getTrForm = function(contactDetail) {
	var tr = '<tr class="$oddeven"><td>Custom field title (no value = delete)<br /><input type="text" id="contactdetail_title_$id" value="$title" /></td><td>Custom field detail (no value = delete)<br /><input type="text" id="contactdetail_detail_$id" value="$detail" /></td></tr>';
	tr = tr.replace(/\$id/g, Base.User.Dialog.EditUser.ContactDetail.id);
	tr = tr.replace(/\$oddeven/g, $('#edituser_control_table tr').size() % 2 == 0 ? "odd" : "even");
	tr = tr.replace(/\$title/g, contactDetail.title);
	tr = tr.replace(/\$detail/g, contactDetail.detail);
	Base.User.Dialog.EditUser.ContactDetail.id++;
	return tr;
};

Base.User.Dialog.ShowUser = {};
Base.User.Dialog.ShowUser.idLastShown = null;
Base.User.Dialog.ShowUser.init = function() {
	Base.User.Dialog.ShowUser.dialog = $('#as_dialog').clone().attr("id", "").html($("#showuser_control").html()).show().dialog( {
		title : "User Details",
		draggable : true,
		width : '500px',
		height : 400,
		modal : false,
		buttons : {
			'Cancel' : function() {
				Base.User.Dialog.defaultCancel(this);
			},
			'Edit' : function() {
				Base.User.Dialog.EditUser.show(Base.User.Dialog.ShowUser.idLastShown);
			}
		}
	});
	Base.User.Dialog.ShowUser.dialog.dialog('close');
	$("#showuser_control").html("");
	$('a.a_showuser').click(function() {
		var user_id = this.id.substr(this.id.lastIndexOf("_") + 1);
		Base.User.Dialog.ShowUser.show(user_id);
	});
};
Base.User.Dialog.ShowUser.dialog = null;
Base.User.Dialog.ShowUser.show = function(user_id) {
	var valueNotSet = "-";
	$.ajax( {
		type : 'GET',
		url : 'want-2-getuser.json',
		data : 'user_id=' + user_id,
		success : function(answer) {
			if (answer && answer.succ) {
				var user = answer.user;
				// delete all td-elements first
				$('#showuser_control_table td[id*=showuser]').html('');
				// put in given user
				$('#showuser_control_fname').html(user.fname || valueNotSet);
				$('#showuser_control_sname').html(user.sname || valueNotSet);
				$('#showuser_control_title').html(user.title || valueNotSet);
				$('#showuser_control_street').html(user.street || valueNotSet);
				$('#showuser_control_streetno').html(user.streetno || valueNotSet);
				$('#showuser_control_zipcode').html(user.zipcode || valueNotSet);
				$('#showuser_control_city').html(user.city || valueNotSet);
				$('#showuser_control_country').html(user.country || valueNotSet);
				$('#showuser_control_rolelabel').html(user.rolelabel || valueNotSet);
				$('#showuser_control_male').html(user.male == null ? valueNotSet : (user.male == 1 ? "Male" : "Female"));
				$('#showuser_control_company').html(user.company || valueNotSet);
				$('#showuser_control_departmentlabel').html(user.departmentlabel || valueNotSet);
				$('#showuser_control_account_expires').html(user.account_expires || "never");
				$('#showuser_control_mail').html(user.mail || valueNotSet);
				$('#showuser_control_phone1').html(user.phone1 || valueNotSet);
				$('#showuser_control_username').html(user.username || valueNotSet);
				$('#showuser_control_phone2').html(user.phone2 || valueNotSet);
				$('#showuser_control_pass').html(user.pass ? "yes" : "no");
				$('#showuser_control_birthdate').html(user.birthdate || valueNotSet);
				$('#showuser_control_intendedResearch').html(Base.nl2br(user.intendedResearch) || valueNotSet);
				$('#showuser_control_id').html(user.id || valueNotSet);
				$('#showuser_control_registrationdate').html(user.registrationdate || valueNotSet);
				$('#showuser_control_lastlogindate').html(user.lastlogindate || valueNotSet);
				$('#showuser_control_excluded').html(user.excluded == null ? valueNotSet : (user.excluded ? "yes" : "no"));
				$('#showuser_control_accepted_statement_of_agreement').html(user.accepted_statement_of_agreement == "no" ? valueNotSet : (user.accepted_statement_of_agreement ? "yes" : "no"));

				$('#showuser_control_tbody_contactDetails').html("");
				$(user.contactDetails).each(function(i, contactDetail) {
					var tr = "<tr><td>$title</td><td>$detail</td></tr>";
					tr = tr.replace(/\$title/g, contactDetail.title);
					tr = tr.replace(/\$detail/g, contactDetail.detail);
					$('#showuser_control_tbody_contactDetails').append(tr);
				});
				
			    $.each(user.customFields, function(key, value) {
			        // XXX only support text inputs and checkboxes by now
			      var selector = '#showuser_control_' + key;
			        if($(selector).attr('type') == 'checkbox') {
                      $(selector).attr('checked', value.length > 0);
			        } else {
			            $(selector).html(value || valueNotSet);
			        }
			    });

				
				
				// hide headline if no contact detail there
		if ($(user.contactDetails).length <= 0) {
			$('#headline_contactDetails').hide();
		} else {
			$('#headline_contactDetails').show();
		}

		$('#showuser_control_tbody_responsible4facilities').html("");
		$(user.responsible4facilities).each(function(i, responsible4facility) {
			var tr = "<tr><td>Responsible for</td><td>$facility</td></tr>";
			tr = tr.replace(/\$facility/g, responsible4facility.label);
			$('#showuser_control_tbody_responsible4facilities').append(tr);
		});

		window.location.hash = "#showuser";
		Base.User.Dialog.ShowUser.idLastShown = user.id;
		Base.User.Dialog.show(Base.User.Dialog.ShowUser.dialog);
		Base.User.Dialog.tableOddEvenize('showuser_control_table');
	} else {
		$.n.error("Could not find the user.");
	}
},
error : function(r) {
	$.n.error("Error 201011131119: " + r.status + " " + r.statusText);
}
	});
};

Base.User.Dialog.InitPass = {};
Base.User.Dialog.InitPass.idShown = null;
Base.User.Dialog.InitPass.init = function() {
	Base.User.Dialog.InitPass.dialog = $('#as_dialog').clone().attr("id", "").html($("#initpass_control").html()).show().dialog( {
		title : "Init and Send user a new password",
		draggable : true,
		width : '350px',
		modal : false,
		buttons : {
			'Cancel' : function() {
				Base.User.Dialog.defaultCancel(this);
			},
			'Send new password now' : function() {
				Base.WaitingIcon.showOnPage();
				$.ajax( {
					type : 'POST',
					url : 'want-2-admininitpass.json',
					data : 'user_id=' + Base.User.Dialog.InitPass.idShown,
					success : function(answer) {
						if (answer && answer.succ) {
							$.n.success(answer.message);
							Base.User.Dialog.defaultCancel(Base.User.Dialog.InitPass.dialog);
						} else {
							if (answer.message) {
								$.n.error(answer.message + " (201012031113)");
								Base.User.Dialog.InitPass.dialog.dialog('close');
							} else {
								$.n.error("Changes NOT made. Your session may be timed out. (Code 201012031028). Reload page in 5 seconds."); // INTLANG
					window.setTimeout(function() {
						window.location.reload()
					}, 5000);
				}
			}
			Base.WaitingIcon.hideOnPage();
		},
		error : function(r) {
			$.n.error("Error 201012031029: " + r.status + " " + r.statusText);
			Base.WaitingIcon.hideOnPage();
		}
				});
			}
		}
	});
	Base.User.Dialog.InitPass.dialog.dialog('close');
	$("#initpass_control").html("");
	$('a.a_initpass').click(function() {
		Base.User.Dialog.InitPass.idShown = this.id.substr(this.id.lastIndexOf("_") + 1);
		Base.User.Dialog.show(Base.User.Dialog.InitPass.dialog);
	});
};
Base.User.Dialog.InitPass.dialog = null;

Base.User.Charslimit = {};
Base.User.Charslimit.exec = function() {
	$.each(ValidationConfiguration.charslimit, function(i, charslimit_obj) {
	  if($('#' + charslimit_obj.content_id).length > 0) {
		var left = charslimit_obj.limit - $('#' + charslimit_obj.content_id).val().length;
		$('#' + charslimit_obj.view_id).html(left);
		if (left < 0) {
			$('#' + charslimit_obj.view_id).addClass('warning');
		} else {
			$('#' + charslimit_obj.view_id).removeClass('warning');
		}
	  }
	});
};
Base.User.Charslimit.init = function() {
	if (ValidationConfiguration.charslimit) {
		Base.User.Charslimit.exec();
		$.each(ValidationConfiguration.charslimit, function(i, charslimit_obj) {
			$('#' + charslimit_obj.content_id).bind("keyup", function() {
				Base.User.Charslimit.exec();
				return false;
			});
		});
	}
};

Base.User.AccountDefaultExpirationDates = {};
Base.User.AccountDefaultExpirationDates.init = function() {
	$('#roleid_id').change(Base.User.AccountDefaultExpirationDates.exec);
	Base.User.AccountDefaultExpirationDates.exec();
};
Base.User.AccountDefaultExpirationDates.exec = function() {
	if (AccountDefaultExpirationDates != null) {
		var role_key = $('#roleid_id').val();
		if (AccountDefaultExpirationDates[role_key]) {
			var date = new Date();
			date = new Date(date.getTime() + (24 * 60 * 60 * 1000 * AccountDefaultExpirationDates[role_key]));
			$('#account_expires_id').val(date.format("dd.mm.yyyy"));
		} else {
			$('#account_expires_id').val("");
		}
	}
};
Base.User.ResponsibilityInput = {};
Base.User.ResponsibilityInput.init = function() {
	$('#roleid_id').change(Base.User.ResponsibilityInput.exec);
	Base.User.ResponsibilityInput.exec();
};
Base.User.ResponsibilityInput.exec = function() {
	if ($('#roleid_id').val() === "operator") {
		$('tr.responsibility').show();
	} else {
		$('tr.responsibility').hide();
	}
};
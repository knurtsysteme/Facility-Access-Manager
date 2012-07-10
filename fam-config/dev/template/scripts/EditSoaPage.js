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
var Tabs = null;
$(document).ready(function() {
	Tabs = DocumentAction.createTabs();
	DocumentAction.showTinyMCE();
	DocumentAction.messageInit();
	AsTable.init.dataTables( {
		aaSorting : [ [ 0, "desc" ] ]
	});
	$('.dataTables_wrapper').css('margin-top', '20px');
	EditSoaPage.checkFamStatus();
	EditSoaPage.initPrintIcons();
	EditSoaPage.setExistingAgreementIdBehaviour();
	EditSoaPage.jActivate.init();
	$('#existing_agreement_id, #title_id, #jactivation_agreements_selection_id').addClass('long');
});

var FamStatus = FamStatus || false;
var EditSoaPage = {};
EditSoaPage.checkFamStatus = function() {
	if (FamStatus) {
		if (FamStatus.animate_row) {
			var $newrow = $('#' + FamStatus.animate_row);
			$newrow.hide();
			window.setTimeout(function() {
				$newrow.show('slow');
			}, 2000);
		}

		if (FamStatus.show_message) {
			AsDialog.show(FamStatus.show_message); // INTLANG
		}

		if (FamStatus.select) {
			Tabs.tabs('select', FamStatus.select);
		}
	}
};
EditSoaPage.initPrintIcons = function() {
	var content = "<span class=\"print\"></span>: Printing is forced on this agreement<br /><span class=\"none_print\"></span>: Printing is not forced on this agreement";
	$('span.print, span.none_print').click(function() {
		AsDialog.show(content, {
			title : Base.images.help + " Help"
		});
	});
};
EditSoaPage.setExistingAgreementIdBehaviour = function() {
	$('#existing_agreement_id').change(function(el) {
		var $select = $(this);
		if (EditSoaPage.setExistingAgreementIdBehaviour.changed) {
			var options = {};
			options.title = "Discard changes?"; // INTLANG
			options.buttons = {
				'Yes! Discard changes!' : function() {
					EditSoaPage.setExistingAgreementIdBehaviour.setContentAndTitle($select);
					$(this).dialog('close');
				},
				'No!' : function() {
					$(this).dialog('close');
				}
			};
			AsDialog.show("All changes made get lost.<br />Do you really want to do that?", options); // INTLANG
		} else {
			EditSoaPage.setExistingAgreementIdBehaviour.setContentAndTitle($select);
		}
	});
	$('#content_id').change(function() {
		EditSoaPage.setExistingAgreementIdBehaviour.changed = true;
	});
};
EditSoaPage.setExistingAgreementIdBehaviour.setContentAndTitle = function(select) {
	if ($('#existing_agreement_id').val() == -1) {
		$('#content_id').val("");
		$('#title_id').val("");
	} else {
		AsCouch.doc(select.val().substring(19), function(doc) {
			var doc = doc || {};
			$('#content_id').val(doc.content || "unknown");
			$('#title_id').val(doc.title || "unknown");
		});
	}
	EditSoaPage.setExistingAgreementIdBehaviour.changed = false;
};
EditSoaPage.setExistingAgreementIdBehaviour.changed = false;

EditSoaPage.jActivate = {};
EditSoaPage.jActivate.switchSteps = function(oldstep, newstep) {
	oldstep.hide('slow', function() {
		newstep.show('slow');
	});
};
EditSoaPage.jActivate.init = function() {
	// hide next steps
	$('#jactivation_step2').hide();
	$('#jactivation_step3').hide();
	$('#jactivation_step4').hide();
	$('#jactivation_step5').hide();

	// ↓ behaviour next buttons

	// ↘ step 1
	$('#step2_role').html($('#role_selection_id').val());
	$('#button_next_step1').click(function() {
		EditSoaPage.jActivate.renderStep2();
		EditSoaPage.jActivate.switchSteps($('#jactivation_step1'), $('#jactivation_step2'));
		$('#button_next_step2').attr('disabled', $('#jactivation_agreements_selection_id').val() == null);
		return false;
	});

	// ↘ step 2
	$('#jactivation_step3_panel').css( {
		"margin-bottom" : "20px",
		"list-style-type" : "decimal",
		"margin-left" : "20px",
		"list-style-position" : "inside"
	});
	$('#button_next_step2').click(function() {
		if (EditSoaPage.jActivate.renderStep3()) {
			EditSoaPage.jActivate.switchSteps($('#jactivation_step2'), $('#jactivation_step3'));
		}
		return false;
	});
	$('#jactivation_agreements_selection_id').change(function() {
		$('#button_next_step2').attr('disabled', $(this).val() == null);
	});

	// ↘ step 3
	$('#button_next_step3').click(function() {
		EditSoaPage.jActivate.renderStep4();
		EditSoaPage.jActivate.switchSteps($('#jactivation_step3'), $('#jactivation_step4'));
		return false;
	});

	// ↘ step 4
	$('#button_next_step4').click(function() {
		EditSoaPage.jActivate.renderStep5();
		EditSoaPage.jActivate.switchSteps($('#jactivation_step4'), $('#jactivation_step5'));
		return false;
	});

	// ↘ step 5 - final post!
	$('#jactivationform_send_button').click(function() {
		EditSoaPage.soaActivationDocument.activatedOn = new Date().getTime();
		EditSoaPage.soaActivationDocument.created = EditSoaPage.soaActivationDocument.activatedOn;
		EditSoaPage.soaActivationDocument.deactivatedOn = null;
		EditSoaPage.soaActivationDocument.type = "SOA_ACTIVATION";
		$('#jactivation_step5_formpanel').html("");
		AsDom.appendHiddenInput($('#jactivation_step5_formpanel'), "body", JSON.stringify(EditSoaPage.soaActivationDocument));
	});

	// behaviour prev buttons
	$('#button_prev_step2').click(function() {
		EditSoaPage.jActivate.switchSteps($('#jactivation_step2'), $('#jactivation_step1'));
		return false;
	});
	$('#button_prev_step3').click(function() {
		EditSoaPage.jActivate.switchSteps($('#jactivation_step3'), $('#jactivation_step2'));
		return false;
	});
	$('#button_prev_step4').click(function() {
		EditSoaPage.jActivate.switchSteps($('#jactivation_step4'), $('#jactivation_step3'));
		return false;
	});
	$('#button_prev_step5').click(function() {
		EditSoaPage.jActivate.switchSteps($('#jactivation_step5'), $('#jactivation_step4'));
		return false;
	});
};
EditSoaPage.jActivate.summaryPoints = [];
EditSoaPage.jActivate.add2Summary = function(fname, fvalue, fnamedesc, fvaluedesc) {
	var fnameexists = false;
	$.each(EditSoaPage.jActivate.summaryPoints, function() {
		if (this.fname == fname) {
			this.fvalue = fvalue;
			this.fnamedesc = fnamedesc;
			this.fvaluedesc = fvaluedesc;
			fnameexists = true;
		}
	});
	if (!fnameexists) {
		var tmp = new Object();
		tmp.fname = fname;
		tmp.fvalue = fvalue;
		tmp.fnamedesc = fnamedesc;
		tmp.fvaluedesc = fvaluedesc;
		EditSoaPage.jActivate.summaryPoints.push(tmp);
	}
	;
};
EditSoaPage.jActivate.renderStep2 = function() {
	EditSoaPage.soaActivationDocument.roleId = $('#role_selection_id option:selected').attr('name');
	$('#step2_role').html($('#role_selection_id').val());
};
EditSoaPage.jActivate.renderStep3 = function() {
	var rendered = false;
	if ($('#jactivation_agreements_selection_id').val() == null) {
		AsDialog.show("Please select at least one agreement"); // INTLANG
	} else {
		var nameAndIds = $('#jactivation_agreements_selection_id').val();
		$('#jactivation_step3_panel').html("");
		$.each(nameAndIds, function() {
			var nameAndId = "step2items_" + this.substring(19);
			$('#jactivation_step3_panel').append("<li class=\"sortable_item ui-state-default\" name=\"" + nameAndId + "\" id=\"" + nameAndId + "\"><span class=\"ui-icon ui-icon-arrowthick-2-n-s\" style=\"float:right;\"></span>" + $('#' + this).html() + "</li>");
		});
		$('.sortable_item').css( {
			"border" : "1px solid black",
			"margin-bottom" : "2px",
			"padding" : "2px",
			"background-color" : "white"
		});
		$('#jactivation_step3_panel').sortable();
		$('#jactivation_step3 li').css("cursor", "row-resize");
		rendered = true;
	}
	return rendered;
};
EditSoaPage.jActivate.renderStep4 = function() {
	var ids = $('#jactivation_step3_panel').sortable("toArray");
	// ↖ e.g. ["step2items_014c1f2691cff156cce6728ec8b5436d",
	// ↖ "step2items_58447e6eddaabf1d1d29b2a7758c9529"]
	var newContent = "<ul style=\"list-style-type: none;\">";
	EditSoaPage.soaActivationDocument.soaActivePages = [];
	$.each(ids, function() {
		var doc_id = this.substring(11);
		var input_id = "step3item_" + doc_id + "_id";
		var label = $('#existing_agreement_' + doc_id).html();
		newContent += "<li><input type=\"checkbox\" name=\"step3items\" value=\"" + doc_id + "\" id=\"" + input_id + "\" />&nbsp;<label for=\"" + input_id + "\">" + label + "</label></li>";

		var soaActivePage = new Object();
		soaActivePage.soaId = doc_id;
		soaActivePage.forcePrinting = false; // js_link 201008191221
			EditSoaPage.soaActivationDocument.soaActivePages.push(soaActivePage);
		});
	newContent += "</ul>";
	$("#jactivation_step4_panel").html(newContent);
};
EditSoaPage.jActivate.renderStep5 = function() {
	// set soa activation pages
	$.each(EditSoaPage.soaActivationDocument.soaActivePages, function() {
		var soaActivePage = this;
		soaActivePage.forcePrinting = false;
		$.each($('#jactivation_step4_panel ul :checked'), function() {
			var doc_id_of_force_printing = $(this).val();
			if (soaActivePage.soaId == doc_id_of_force_printing) {
				soaActivePage.forcePrinting = true;
			}
		});
	});
	// collect summary
	// collect role ...
	var fnamedesc = "Activation for role"; // INTLANG
	var fvaluedesc = $('#role_selection_id').val();
	var fname = "role";
	var fvalue = EditSoaPage.soaActivationDocument.roleId;
	EditSoaPage.jActivate.add2Summary(fname, fvalue, fnamedesc, fvaluedesc);
	// collect soa activation pages ...
	var page = 1;
	$.each(EditSoaPage.soaActivationDocument.soaActivePages, function() {
		fnamedesc = "Page " + page; // INTLANG
			fvaluedesc = $("#existing_agreement_" + this.soaId).html() + ". Options: " + (this.forcePrinting ? "force printing" : "do not force printing");
			fname = "page" + page;
			fvalue = page;
			EditSoaPage.jActivate.add2Summary(fname, fvalue, fnamedesc, fvaluedesc);
			page++;
		});
	// show summary
	$('#jactivation_step5_summary').html("");
	$.each(EditSoaPage.jActivate.summaryPoints, function() {
		$('#jactivation_step5_summary').append('<li><span style="font-weight: bolder;">' + this.fnamedesc + '</span> ' + this.fvaluedesc + '</li>');
	});
};
EditSoaPage.soaActivationDocument = new Object();
EditSoaPage.soaActivationDocument.soaActivePages = [];
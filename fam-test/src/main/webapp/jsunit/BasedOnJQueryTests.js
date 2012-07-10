var Facilities = {
	keys : "playschool"
}
var BookingRule = {};
var Facility = {};

$(document).ready(function() {

	module("base_jq");
	test("assert not null", function() {
		expect(9);
		ok(Base != null, "Base");
		ok(Base.useJavaScript != null, "Base.useJavaScript");
		ok(Base.init != null, "Base.init");
		ok(Base.WaitingIcon != null, "Base.WaitingIcon");
		ok(Base.WaitingIcon.show != null, "Base.WaitingIcon.show");
		ok(Base.Calendar != null, "Base.Calendar");
		ok(Base.Calendar.getPopUpCalendar != null, "Base.Calendar.getPopUpCalendar");
		ok(Base.Calendar.privateGet != null, "Base.Calendar.privateGet");
		ok(Base.useJavaScript() == true, "Base.useJavaScript");
	});
	module("base_jq");
	test("assert not null", function() {
		expect(2);
		ok(FacilityOverviewTree != null);
		ok(FacilityOverviewTree.init != null);
	});
	module("quicksand");
	test("assert not null", function() {
		expect(4);
		ok(QuicksandControl != null, "QuicksandControl");
		ok(QuicksandControl.init != null, "QuicksandControl.init");
		ok(QuicksandControl.initClickItems != null, "QuicksandControl.initClickItems");
		ok(QuicksandControl.clickItems != null, "QuicksandControl.clickItems");
	});
	module("edit documents");
	test("assert everything there", function() {
		ok(DocumentAction != null, "DocumentAction");
		ok(DocumentAction.showTinyMCE != null, "DocumentAction.showTinyMCE");
		ok(DocumentAction.showTinyMCE + "".match(/^function.*$/), "DocumentAction.showTinyMCE is not a function");
		ok(DocumentAction.createTabs != null, "DocumentAction.createTabs");
		ok(DocumentAction.createTabs + "".match(/^function.*$/), "DocumentAction.createTabs is not a function");
	});
	test("step by step", function() {
		ok(EditSoaPage.jActivate.summaryPoints.length == 0);
		EditSoaPage.jActivate.add2Summary("a", "b", "c", "d");
		ok(EditSoaPage.jActivate.summaryPoints.length == 1);
		EditSoaPage.jActivate.add2Summary("a", "b", "c", "d");
		ok(EditSoaPage.jActivate.summaryPoints.length == 1, "should not be add duplicate document");
		EditSoaPage.jActivate.add2Summary("e", "f", "g", "h");
		ok(EditSoaPage.jActivate.summaryPoints.length == 2);
	});
	module("booking");
	test("base calendar", function() {
		BookingRule.capacity_label_singular = "Green bus unit";
		BookingRule.capacity_label_plural = "Green bus units";
		Facility.label = "Green bus";
		Facility.key = "bus2";
		ok(Base.Calendar.Util.getUnitsLabelAsText(1, false, true) == "1 Green bus unit");
		ok(Base.Calendar.Util.getUnitsLabelAsText(2, false, true) == "2 Green bus units");
		ok(Base.Calendar.Util.getUnitsLabelAsText(1, false, false) == "Green bus unit");
		ok(Base.Calendar.Util.getUnitsLabelAsText(2, false, false) == "Green bus units");
		BookingRule.capacity_label_singular = Facility.label;
		ok(Base.Calendar.Util.getUnitsLabelAsText(1, true, true) == "1");
		ok(Base.Calendar.Util.getUnitsLabelAsText(1, true, false) == "");
	});
});

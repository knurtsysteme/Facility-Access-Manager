/*! KNURT Systeme - part of the Facility Access Manager */
var ValidationConfiguration = {
	mustbevalid : ['fname_id', 'sname_id','mail_id'],
	oneofmustbevalid : [ [ 'phone1_id', 'phone2_id' ] ],
	label : {
		submit : "Next step",
		nosubmit : "Please complete your input"
	},
	charslimit : [ {
		'content_id' : 'intendedResearch_id',
		'limit' : 2000,
		'view_id' : 'js_chars_left'
	} ]
};
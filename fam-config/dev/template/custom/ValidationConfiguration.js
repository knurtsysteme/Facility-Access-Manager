/* change to your own validation configuration in this file and be safe in template updates */
var ValidationConfiguration = {
  // must have a not empty value
  mustbevalid : [ 'fname_id', 'sname_id', 'mail_id', 'male_id', 'title_id' ],
  // one of the key of the inner array must have a not empty value
  oneofmustbevalid : [ [ 'phone1_id', 'phone2_id' ] ],
  label : {
    submit : "Next step",
    nosubmit : "Please complete your input"
  },
  // a custom function must return true on given id
  mustbetrue : [ {
    'id' : 'intendedResearch_id',
    'validate' : function(value) {
      // write your own validation for 
	  // the value of #intendedResearch_id here
    return true;
  }
  } ],
  // set character limits
  charslimit : [ {
    'content_id' : 'intendedResearch_id',
    'min' : 0, // not supported yet
    'limit' : 2000,
    'view_id' : 'js_chars_left'
  } ]
};
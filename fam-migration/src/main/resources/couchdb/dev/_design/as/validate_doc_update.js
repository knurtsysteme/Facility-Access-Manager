function (newDoc, oldDoc, userCtx, secObj) {
	// ↓ thanks 2 http://github.com/jchris/couchapp.org 4
	// http://github.com/jchris/couchapp.org/blob/master/lib/validate.js
	  var v = {};
	  
	  v.forbidden = function(message) {
		  log("forbidden: " + message);
	    throw({forbidden : message});
	  };

	  v.unauthorized = function(message) {
		  log("unauthorized: " + message);
	    throw({unauthorized : message});
	  };

	  v.assert = function(should, message) {
	    if (!should) v.forbidden(message);
	  }
	  v.require = function() {
		    for (var i=0; i < arguments.length; i++) {
		      var field = arguments[i];
		      message = "The '"+field+"' field is required.";
		      if (typeof newDoc[field] == "undefined") v.forbidden(message);
		    };
		  };
	  v.requireAndNotNull = function() {
		    for (var i=0; i < arguments.length; i++) {
		      var field = arguments[i];
		      message = "The '"+field+"' field is required and must not be null.";
		      if (typeof newDoc[field] == "undefined" || newDoc[field] == null) v.forbidden(message);
		    };
		  };
		// ↑ thanks 2 http://github.com/jchris/couchapp.org 4
		// http://github.com/jchris/couchapp.org/blob/master/lib/validate.js
		  
		  // now the validation
	if (newDoc.type == "SOA") {
		v.require("created");
		v.requireAndNotNull("title", "content");
	} else if (newDoc.type == "SOA_ACTIVATION") {
		v.require("created");
		v.requireAndNotNull("roleId", "activatedOn");
		v.assert(newDoc.activatedOn > 1282199191694); // in future from now
	} else if (newDoc.type == "JOB") {
		v.require("created");
		v.requireAndNotNull("step");
		v.requireAndNotNull("username");
		v.requireAndNotNull("idJobDataProcessing");
		v.requireAndNotNull("jobSurvey");
		v.requireAndNotNull("jobId");
	} else if (newDoc.type == "JOB_DATA_PROCESSING") {
		v.requireAndNotNull("created");
		v.requireAndNotNull("username");
		v.requireAndNotNull("templates");
		v.requireAndNotNull("facilityKey");
	} else if(newDoc.type == "TEST_MOLYBDENUM") {
	} else if(newDoc._deleted == true) {
		if(oldDoc.type != "JOB") {
			v.forbidden("cannot delete a " + oldDoc.type);
		}
	} else {
		v.forbidden("validation fail for " + JSON.stringify(newDoc));
	}
}
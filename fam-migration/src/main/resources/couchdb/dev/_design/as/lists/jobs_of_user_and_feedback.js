function(head, req) {
	var results = new Array();
	var feedback_candidates = new Array();
	var job_ids = new Array();
	var doc;
	var i=0;
	var j=0;
	while( doc = getRow()) {
		if(doc.value.jobId != null && doc.value.username == req.query.username) {
			results.push(doc.value);
			job_ids.push(doc.value.jobId);
		}
		if(doc.value.jobId != null && doc.value.step != null && doc.value.step > 0) {
			feedback_candidates.push(doc.value);
		}
	}
	for(i=0; i<job_ids.length; i++) {
		for(j=0; j<feedback_candidates.length;j++) {
			if(feedback_candidates[j].jobId == job_ids[i]) {
				results.push(feedback_candidates[j]);
			}
		}
	}
	
	send(toJSON(results));
}

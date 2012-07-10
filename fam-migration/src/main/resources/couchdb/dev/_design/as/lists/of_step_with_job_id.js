function(head, req) {
	var result = null;
	while( doc = getRow()) {
		if(doc.value.jobId != null && doc.value.step != null && doc.value.jobId == req.query.jobId && doc.value.step == req.query.step) {
			result = doc.value;
		}
	}
	send(toJSON(result));
}
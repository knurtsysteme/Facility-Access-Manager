function(head, req) {
	var result = [];
	while( doc = getRow()) {
		if(doc.value.jobId != null && doc.value.jobId == req.query.jobId) {
			result.push(doc.value);
		}
	}
	send(toJSON(result));
}
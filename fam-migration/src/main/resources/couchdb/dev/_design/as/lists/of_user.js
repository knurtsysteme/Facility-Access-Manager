function(head, req) {
	var results = new Array();
	var doc;
	while( doc = getRow()) {
		if(doc.value.username == req.query.username) {
			results.push(doc.value);
		}
	}
	send(toJSON(results));
}

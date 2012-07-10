function(head, req) {
	var result = null;
	while(doc = getRow()) {
		if(result && doc.value.created && doc.value.created > result.value.created) {
			result = doc;
		} else if(!result && doc.value.created) {
			result = doc;
		}
	}
	send(toJSON(result));
}

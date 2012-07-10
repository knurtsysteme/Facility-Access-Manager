function(head, req) {
	var result = null;
	while( doc = getRow()) {
		if(doc.value.type == "JOB_DATA_PROCESSING" && doc.value.facilityKey != null && doc.value.facilityKey == req.query.facilityKey && (result == null || doc.value.created > result.created)) {
			result = new Object();
			result.facilityKey = doc.value.facilityKey;
			result.created = doc.value.created;
			result.username = doc.value.username;
			result.templates = doc.value.templates;
			result.idJobDataProcessing = doc.value._id;
		}
	}
	send(toJSON(result));
}
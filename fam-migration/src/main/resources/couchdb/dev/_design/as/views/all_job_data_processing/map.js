function(doc) {
	if(doc.created && doc.type == "JOB_DATA_PROCESSING") {
		emit(doc.created, doc);
	}
}
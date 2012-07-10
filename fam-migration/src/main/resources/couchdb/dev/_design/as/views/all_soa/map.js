function(doc) {
	if(doc.created && doc.type == "SOA") {
		emit(doc.created, doc);
	}
}
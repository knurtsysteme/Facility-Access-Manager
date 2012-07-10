function(doc) {
	if(doc.type == "SOA_ACTIVATION" && doc.created && doc.roleId && doc.soaActivePages && doc.soaActivePages.length > 0) {
		emit(doc.created, doc);
	}
}
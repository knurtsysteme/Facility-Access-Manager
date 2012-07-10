function(doc) {
	if(doc.type == "SOA_ACTIVATION" && doc.activatedOn && doc.deactivatedOn == null && doc.roleId && doc.soaActivePages && doc.soaActivePages.length > 0) {
		emit(doc.activatedOn, doc);
	}
}
function(doc) {
	if(doc.type == "SOA_ACTIVATION" && doc.created && doc.roleId && doc.soaActivePages && doc.soaActivePages.length > 0 && doc.activatedOn && doc.activatedOn > 0 && doc.deactivatedOn && doc.deactivatedOn > doc.activatedOn) {
		emit(doc.created, doc);
	}
}
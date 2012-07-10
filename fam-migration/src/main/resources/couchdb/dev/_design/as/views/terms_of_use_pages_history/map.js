function(doc) {
	if(doc.type == "SOA_ACTIVATION" && doc.created && doc.roleId && doc.soaActivePages && doc.soaActivePages.length > 0 && doc.activatedOn && doc.activatedOn > 0 && doc.deactivatedOn && doc.deactivatedOn > doc.activatedOn) {
		var pageno = 0;
		for each(var soaActivePage in doc.soaActivePages) {
			var termsOfUsePage = new Object();
			termsOfUsePage.htmlContent = soaActivePage.soaDoc.content ? soaActivePage.soaDoc.content : "unknown"; // INTLANG
			termsOfUsePage.pageno  = pageno;
			termsOfUsePage.pagetotalno  = doc.soaActivePages.length;
			termsOfUsePage.activatedOn  = doc.activatedOn;
			termsOfUsePage.deactivatedOn  = doc.deactivatedOn;
			termsOfUsePage.title  = soaActivePage.soaDoc.title ? soaActivePage.soaDoc.title : "unknown";
			termsOfUsePage.forcePrinting = soaActivePage.forcePrinting;
			termsOfUsePage.roleId = doc.roleId;
			emit(doc.activatedOn + "page:" + pageno, termsOfUsePage);
			pageno++;
		}
	}
}
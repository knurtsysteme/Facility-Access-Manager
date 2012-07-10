function(doc, req) {
	provides("html", function() {
		return doc.content;
	});
}

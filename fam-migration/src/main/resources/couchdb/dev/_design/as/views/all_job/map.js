function(doc) {
	if(doc.created && doc.type == "JOB" && doc.jobSurvey && typeof doc.jobSurvey == typeof {}) {
		emit(doc.created, doc);
	}
}
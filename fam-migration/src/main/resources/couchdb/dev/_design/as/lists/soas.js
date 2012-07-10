function(head, req) {
	provides("json", function() {
		var soas = new Array();
		var soa;
		while(soa = getRow()) {
			var overviewSoa = new Object();
			overviewSoa.created = soa.value.created; 
			overviewSoa.title = soa.value.title; 
			soas.push(overviewSoa);
		}
		send(toJSON(soas));
	});
	provides("html", function() {
		if(req.query.render == 'overview') {
			// ↖ want table overview html
			var result = "<table class=\"standard\"><thead><tr><th>Created on</th><th>Title</th></tr></thead><tbody>"; // INTLANG
			var soa;
			var hasRow = false;
			var rowCount = 0;
			while(soa = getRow()) {
				if(soa.value.created && soa.value.title) {
					hasRow = true;
					var date = new Date(soa.value.created).toLocaleString(); 
					result += "<tr id=\"edit_soa_overview_"+soa.value._id+"\" class=\"" + (rowCount++ % 2 == 1 ? "even" : "odd") + "\"><td><span style=\"display: none;\">sort_"+soa.value.created+"</span>"+date+"</td>";
					result += "<td><a target=\"_blank\" href=\"singletermsofuseadminview.html?aa="+soa.value._id+"\">"+soa.value.title+"</a></td></tr>";
				}
			}
			if(hasRow) {
				result += "</tbody></table>";
			} else {
				result = "<div>no entries</div>"; // INTLANG
			}
			send(result);
		} else if(req.query.render == 'select') {
			// ↖ want select html

			// ↓ get options
			var options = new Object();
			options.selectname = req.query.selectname || "existing_agreement";
			options.selectid = req.query.selectid || "existing_agreement_id";
			options.extraoptionhtml = req.query.extraoptionhtml || "";
			options.selectspecialoptions = "";
			if(req.query.selectspecialoptions) {
				options.selectspecialoptions = " " + req.query.selectspecialoptions;
			}
			
			// ↓ generate select
			var result = "<select name=\""+options.selectname+"\" id=\""+options.selectid+"\""+options.selectspecialoptions+">";
			result += options.extraoptionhtml;
			while(soa = getRow()) {
				if(soa.value.created && soa.value.title) {
					var date = new Date(soa.value.created).toLocaleString(); 
					result += "<option id=\"existing_agreement_"+soa.value._id+"\" value=\"existing_agreement_"+soa.value._id+"\" \">"+soa.value.title+" ("+date+")</option>";
				}
			}
			result += "</select>";
			send(result);
		} else {
			// ↖ unknown render
			// ↓ log and show error
			log("[201008161425]", "Must be invoked with 'render=?' with one of 'select' or 'overview'. got:" + req.query.render)
			send("[201008161425] Must be invoked with 'render=?' with one of 'select' or 'overview'. got:" + toJSON(req.query))
		}
	});
}

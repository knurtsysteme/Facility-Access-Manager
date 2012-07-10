function(head, req) {
	provides("html", function() {
		// ↖ want table overview html
		var result = "<table class=\"standard\"><thead><tr><th>Scope</th><th>Activated</th><th>Deactivated</th><th>Agreements</th></tr></thead><tbody>"; // INTLANG
		var soa_activation;
		var hasRow = false;
		var rowCount = 0;
		while(soa_activation = getRow()) {
			hasRow = true;
			
			var date = "";
			
			var scope = soa_activation.value.roleId;
			
			var activated = "not yet";
			if(soa_activation.value.activatedOn) {
				activated = new Date(soa_activation.value.activatedOn).toLocaleString();
			}
			
			var deactivated = "not yet";
			if(soa_activation.value.deactivatedOn) {
				deactivated = new Date(soa_activation.value.deactivatedOn).toLocaleString();
			}
			
			var agreements = "<ul>";
			
			if(soa_activation.value.soaActivePages) {
				var i;
				for(i=0; i<soa_activation.value.soaActivePages.length; i++) {
					agreements += "<li><a href=\"/termsofuse.html?aa="+soa_activation.value._id+"&s=editsoa&f="+i+"\">Page " + (i+1) + ": " + soa_activation.value.soaActivePages[i].soaDoc.title + "</a></li>";
					// ↖ @see AsQueryKeys#QUERY_KEY_OF for key "aa"
					// ↖ @see AsQueryKeys#QUERY_KEY_PAGENO for key "f"
				}
			} 
			else {
				agreements = "no agreement defined"; // INTLANG
			}
			agreements += "</ul>";
			
			
			result += "<tr id=\"soa_activation_"+soa_activation.value._id+"\" class=\"" + (rowCount++ % 2 == 1 ? "even" : "odd") + "\">";
			result += "<td id=\"soa_activation_scope_"+soa_activation.value._id+"\">"+scope+"</td>";
			result += "<td id=\"soa_activation_activated_"+soa_activation.value._id+"\"><span style=\"display: none;\">sort_"+soa_activation.value.activatedOn+"</span>"+activated+"</td>"
			result += "<td id=\"soa_activation_deactivated_"+soa_activation.value._id+"\"><span style=\"display: none;\">sort_"+soa_activation.value.deactivatedOn+"</span>"+deactivated+"</td>"
			result += "<td id=\"soa_activation_agreements_"+soa_activation.value._id+"\">"+agreements+"</td>"
			result += "</tr>";
		}
		if(hasRow) {
			result += "</tbody></table>";
		} else {
			result = "<div>no entries</div>"; // INTLANG
		}
		send(result);
	});
}

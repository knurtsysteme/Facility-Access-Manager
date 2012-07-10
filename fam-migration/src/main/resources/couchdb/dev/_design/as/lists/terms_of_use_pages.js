function(head, req) {
	provides("html", function() {
		// ↖ want table overview html
		var result = "<table class=\"standard\"><thead><tr><th>Scope</th><th>Activated</th><th>Deactivated</th><th>Agreement</th></tr></thead><tbody>"; // INTLANG
		var page;
		var hasRow = false;
		var rowCount = 0;
		while(page = getRow()) {
			
			hasRow = true;
			
			var date = "";
			
			var scope = page.value.roleId;
			
			var activated = "not yet";
			if(page.value.activatedOn) {
				activated = new Date(page.value.activatedOn).toLocaleString().replace(/ /g, "&nbsp;");
			}
			
			var deactivated = "not yet";
			if(page.value.deactivatedOn) {
				deactivated = new Date(page.value.deactivatedOn).toLocaleString().replace(/ /g, "&nbsp;");
			}
			
			var agreement = "<a target=\"_blank\" href=\"termsofuse.html?f="+page.value.pageno+"&x="+scope+"\">Page " + (page.value.pageno + 1) + " of " + page.value.pagetotalno + "<br />" + page.value.title + "</a>";
			if(page.value.forcePrinting == true) {
				agreement += "<br />Options: <span class=\"print\"></span>";
			}
			else {
				agreement += "<br />Options: <span class=\"none_print\"></span>";
			}
			result += "<tr id=\"page_"+page.id+"\" class=\"" + (rowCount++ % 2 == 1 ? "even" : "odd") + "\">";
			result += "<td id=\"page_scope_"+page.id+"\">"+scope+"</td>";
			result += "<td id=\"page_activated_"+page.id+"\"><span style=\"display: none;\">sort_"+page.value.activatedOn+"</span>"+activated+"</td>"
			result += "<td id=\"page_deactivated_"+page.id+"\"><span style=\"display: none;\">sort_"+page.value.deactivatedOn+"</span>"+deactivated+"</td>"
			result += "<td id=\"page_agreements_"+page.id+"\"><span style=\"display: none;\">sort_"+page.value.title+"</span>"+agreement+"</td>"
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

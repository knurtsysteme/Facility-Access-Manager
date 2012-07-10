#if($util.value.isNull($config.model.statistic) || $config.model.statistic.id==1)
	#include("lib/prototype.js")
	#include("lib/effects.js")
	#include("lib/scrollwork.js")
	#include("scripts/Base_prototype.js")
#else
	var SortTableOptions = { aaSorting : [ [ 1, "desc" ] ] };
	#include("lib/jquery-1.4.2.min.js")
	#include("lib/jquery-ui.custom.min.js")
	#include("scripts/Base.js")
	#include("scripts/tables_jq.js")
	#include("lib/jquery.dataTables.min.js")
#end
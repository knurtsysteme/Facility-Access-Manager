## a place to define global vars parsed on any page of the application
#if($config.model.asTableOptions)
	/*! KNURT Systeme - part of the Facility Access Manager */
	var SortTableOptions = $config.model.asTableOptions;
#end
#if($config.model.jsonfacilities)
	/*! KNURT Systeme - part of the Facility Access Manager */
	var Facilities = $config.model.jsonfacilities;
#end
#if($config.model.jsoncalendarmetrics)
	/*! KNURT Systeme - part of the Facility Access Manager */
	var CalendarMetrics = $config.model.jsoncalendarmetrics;
#end
#if($config.model.jsonvar)
	/*! KNURT Systeme - part of the Facility Access Manager */
	$config.model.jsonvar;
#end
#if($config.model.newsitems_last_update && $FamDateFormat)
	var LastNewsUpdate = $FamDateFormat.getCustomDate($config.model.newsitems_last_update, "m");
#end
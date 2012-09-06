#include("lib/jquery-1.7.1.min.js")
#include("lib/jquery-ui.min.1.8.16.js")
#include("scripts/Base.js")
#include("lib/json2.js")
#include("lib/string_format.js")
#include("lib/jquery.notifications-1.1.min.js")
#include("scripts/JobSurvey.js")
#include("lib/jquery.serializeobject.js")
/*! KNURT Systeme - part of the Facility Access Manager */
$(document).ready(function(){JobSurvey.init();});
Base.setIFrameHeightInterval();
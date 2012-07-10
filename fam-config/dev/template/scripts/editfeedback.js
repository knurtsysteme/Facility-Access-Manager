#include("lib/jquery-1.7.1.min.js")
#include("lib/jquery-ui.min.1.8.16.js")
#include("lib/jquery.serializeobject.js")
#include("lib/jquery.notifications-1.1.min.js")
#include("scripts/Base.js")
#include("scripts/JobSurveyDataProcessing.js")
#include("scripts/JobSurveyIO.js")
var Booking = { facilityKey : "$config.model.booking.facilityKey", id : $config.model.booking.id, step : 2 };
#include("scripts/edit_request_or_feedback_inc.js")
Base.setIFrameHeightInterval();
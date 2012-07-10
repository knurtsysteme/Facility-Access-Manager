#include("lib/jquery-1.4.2.min.js")
#include("lib/jquery-ui.custom.min.js")
#include("lib/date.format.js")
#include("lib/jquery.dataTables.min.js")
#include("lib/jquery.notifications-1.1.min.js")
#include("lib/json2.js")
#include("lib/jquery.serializeobject.js")
#include("lib/regexp.js")
## IMPORTANT: do not change the order
#include("scripts/Base.js")
#include("scripts/Base_User.js")
#include("scripts/tables_jq.js")
#if($util.value.fileExists("custom/ValidationConfiguration.js"))
#include("custom/ValidationConfiguration.js")
#else
#include("scripts/ValidationConfiguration.js")
#end
## the default expiration dates of an account of specific roles in days
var AccountDefaultExpirationDates = {'extern': 183,'intern': 1095,'operator': 1095};
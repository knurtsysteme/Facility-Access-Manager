#include("lib/prototype.js")
#include("lib/effects.js")
#include("lib/scrollwork.js")
#if($util.value.fileExists("custom/ValidationConfiguration.js"))
#include("custom/ValidationConfiguration.js")
#else
#include("scripts/ValidationConfiguration.js")
#end
#include("scripts/Base_prototype.js")
#include("lib/CalendarPopupCompact.js")
#include("scripts/contactdetails_inc.js")
#if($util.value.fileExists("custom/contactdetails_append.js"))
#include("custom/contactdetails_append.js")
#end
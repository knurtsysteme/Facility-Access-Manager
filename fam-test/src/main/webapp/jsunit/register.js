#include("lib/jquery-1.7.1.min.js")
#include("lib/jquery-ui.custom.min.js")
#include("lib/jquery.bubblepopup.v2.1.5.min.js")
#include("scripts/Base.js")
#include("lib/jquery.passvalidator.js")
#include("lib/regexp.js")
#include("scripts/Base_Register.js")
#if($util.value.fileExists("custom/ValidationConfiguration.js"))
#include("custom/ValidationConfiguration.js")
#else
#include("scripts/ValidationConfiguration.js")
#end
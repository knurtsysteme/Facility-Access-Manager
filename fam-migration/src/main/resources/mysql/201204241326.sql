UPDATE booking SET cancelation_reason = NULL WHERE cancelation_reason = "";
ALTER TABLE booking CHANGE COLUMN `cancelation_reason` `cancelation_reason` varchar(1500) default NULL;

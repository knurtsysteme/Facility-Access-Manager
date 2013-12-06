ALTER TABLE user ADD COLUMN `customFields` TEXT DEFAULT NULL;
UPDATE user SET customFields = "{}";

ALTER TABLE deviceavailability RENAME facilityavailability;
ALTER TABLE facilityavailability CHANGE COLUMN deviceKey facilityKey VARCHAR(255) NOT NULL;
ALTER TABLE booking CHANGE COLUMN deviceKey facilityKey VARCHAR(255) NOT NULL;

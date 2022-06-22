ALTER TABLE core_user ADD privateEmail VARCHAR(255) NULL DEFAULT NULL;
ALTER TABLE as_cloudsafeshare ADD restrictDownload BIT NOT NULL DEFAULT 0;

ALTER TABLE as_cloudsafe ADD group_dc_id INTEGER NULL DEFAULT NULL;
ALTER TABLE as_cloudsafe ADD CONSTRAINT FK_AS_PROP_GROUP
FOREIGN KEY (group_dc_id)
REFERENCES core_group (dc_id);
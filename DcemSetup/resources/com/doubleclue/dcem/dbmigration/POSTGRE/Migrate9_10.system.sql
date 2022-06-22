ALTER TABLE core_user ADD privateEmail character varying(255) NULL DEFAULT NULL;
ALTER TABLE as_cloudsafeshare ADD COLUMN restrictDownload BOOLEAN DEFAULT false NOT NULL;

ALTER TABLE as_cloudsafe ADD COLUMN group_dc_id INTEGER NULL DEFAULT NULL;
ALTER TABLE as_cloudsafe ADD CONSTRAINT FK_AS_PROP_GROUP
FOREIGN KEY (group_dc_id)
REFERENCES core_group (dc_id);

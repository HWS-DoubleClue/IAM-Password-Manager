ALTER TABLE CORE_USER ADD privateEmail varchar(255) DEFAULT 'null';
ALTER TABLE as_cloudsafeshare ADD restrictDownload BOOLEAN NOT NULL DEFAULT FALSE;

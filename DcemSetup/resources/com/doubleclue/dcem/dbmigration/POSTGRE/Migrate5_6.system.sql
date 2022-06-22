ALTER TABLE oauth_client ADD COLUMN idp_settings VARCHAR(4096);
ALTER TABLE core_user ADD COLUMN objectGuid bytea NULL DEFAULT NULL;
ALTER TABLE as_reporting ADD COLUMN dc_loc VARCHAR(255) NULL DEFAULT NULL;
ALTER TABLE saml_sp_metadata RENAME COLUMN attributes TO idp_settings;
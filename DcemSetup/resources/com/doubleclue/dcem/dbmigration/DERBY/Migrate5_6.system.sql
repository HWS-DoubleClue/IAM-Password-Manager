ALTER TABLE oauth_client ADD idp_settings VARCHAR(4096);
ALTER TABLE core_user ADD objectGuid varchar(32) DEFAULT NULL;
ALTER TABLE as_reporting ADD dc_loc VARCHAR(255) DEFAULT NULL;
RENAME COLUMN saml_sp_metadata.attributes TO idp_settings;
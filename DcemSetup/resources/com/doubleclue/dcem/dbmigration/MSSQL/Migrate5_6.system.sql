ALTER TABLE oauth_client ADD idp_settings VARCHAR(4096);
ALTER TABLE core_user ADD objectGuid VARBINARY(255) NULL DEFAULT NULL;
ALTER TABLE as_reporting ADD dc_loc VARCHAR(255) NULL DEFAULT NULL;
EXEC sp_RENAME 'saml_sp_metadata.attributes' , 'idp_settings', 'COLUMN';
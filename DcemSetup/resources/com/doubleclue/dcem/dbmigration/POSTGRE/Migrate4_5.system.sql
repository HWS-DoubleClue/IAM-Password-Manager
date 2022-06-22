ALTER TABLE core_user ADD userPrincipalName character varying(255) NULL DEFAULT NULL;
ALTER TABLE core_ldap ADD dc_rank integer NULL DEFAULT '0';
ALTER TABLE core_ldap ADD mapEmailDomains character varying(255) NULL DEFAULT NULL;
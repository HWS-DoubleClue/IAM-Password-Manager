ALTER TABLE "CORE_USER" ADD "userPrincipalName" varchar(255) DEFAULT 'null';
ALTER TABLE "CORE_LDAP" ADD "dc_rank" int DEFAULT 0;
ALTER TABLE "CORE_LDAP" ADD "mapEmailDomains" varchar(255) DEFAULT 'null';
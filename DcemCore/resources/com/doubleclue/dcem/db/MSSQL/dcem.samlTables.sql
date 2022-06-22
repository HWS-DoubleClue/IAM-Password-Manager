create table saml_sp_metadata (
dc_id int not null,
acs_location varchar(255) not null,
certificateString varchar(MAX),
dc_disabled bit not null,
display_name varchar(255) not null,
entityId varchar(255) not null,
idp_settings varchar(4096),
logout_is_post bit not null,
logout_location varchar(255),
dc_metadata varchar(MAX),
name_id_format int not null,
requests_signed bit not null,
primary key (dc_id)
);

alter table saml_sp_metadata
add constraint UK_SP_METADATA_ENTITYID unique (entityId);

alter table saml_sp_metadata
add constraint UK_SP_METADATA_DISPLAY_NAME unique (display_name);

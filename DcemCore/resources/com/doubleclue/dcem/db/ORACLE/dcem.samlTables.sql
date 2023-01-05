
create table saml_sp_metadata (
dc_id number(10,0) not null,
acs_location varchar2(255 char) not null,
certificateString clob,
dc_disabled number(1,0) not null,
display_name varchar2(255 char) not null,
entityId varchar2(255 char) not null,
idp_settings long,
logout_is_post number(1,0) not null,
logout_location varchar2(255 char),
dc_metadata clob,
name_id_format number(10,0) not null,
requests_signed number(1,0) not null,
primary key (dc_id)
);

alter table saml_sp_metadata
add constraint UK_SP_METADATA_ENTITYID unique (entityId);

alter table saml_sp_metadata
add constraint UK_SP_METADATA_DISPLAY_NAME unique (display_name);
